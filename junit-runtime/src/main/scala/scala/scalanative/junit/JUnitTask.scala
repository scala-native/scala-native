package scala.scalanative
package junit

import sbt.testing._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.scalanative.reflect.Reflect
import scala.util.{Failure, Success, Try}
import org.junit.TestCouldNotBeSkippedException

/* Implementation note: In JUnitTask we use Future[Try[Unit]] instead of simply
 * Future[Unit]. This is to prevent Scala's Future implementation to box/wrap
 * fatal errors (most importantly AssertionError) in ExecutionExceptions. We
 * need to prevent the wrapping in order to hide the fact that we use async
 * under the hood and stay consistent with JVM JUnit.
 */
private[junit] final class JUnitTask(
    _taskDef: TaskDef,
    runSettings: RunSettings
) extends Task {
  def taskDef(): TaskDef = _taskDef
  def tags(): Array[String] = Array.empty

  def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger],
      continuation: Array[Task] => Unit
  ): Unit = {
    val reporter = new Reporter(eventHandler, loggers, runSettings, taskDef())

    val result = loadBootstrapper(reporter).fold {
      Future.successful(())
    } { bootstrapper =>
      executeTests(bootstrapper, reporter)
    }

    result.foreach(_ => continuation(Array()))
  }

  private def executeTests(
      bootstrapper: Bootstrapper,
      reporter: Reporter
  ): Future[Unit] = {
    reporter.reportRunStarted()

    var failed = 0
    var ignored = 0
    var total = 0

    def runTests(
        tests: List[TestMetadata],
        testClass: TestClassMetadata
    ): Future[Try[Unit]] = {
      if (testClass.ignored) {
        reporter.reportIgnored(None)
        ignored += 1
        Future.successful(Success(()))
      } else {
        val (nextIgnored, other) = tests.span(_.ignored)
        nextIgnored.foreach(t => reporter.reportIgnored(Some(t.name)))
        ignored += nextIgnored.size

        other match {
          case t :: ts =>
            total += 1
            executeTestMethod(bootstrapper, t, reporter).flatMap { fc =>
              failed += fc
              runTests(ts, testClass)
            }

          case Nil =>
            Future.successful(Success(()))
        }
      }
    }

    val result = runTestLifecycle {
      Success(())
    } { _ => catchAll(bootstrapper.beforeClass()) } { _ =>
      runTests(bootstrapper.tests().toList, bootstrapper.testClassMetadata())
    } { _ => catchAll(bootstrapper.afterClass()) }

    for {
      (errors, timeInSeconds) <- result
    } yield {
      failed += reportExecutionErrors(reporter, None, timeInSeconds, errors)
      reporter.reportRunFinished(failed, ignored, total, timeInSeconds)
    }
  }

  private[this] def executeTestMethod(
      bootstrapper: Bootstrapper,
      test: TestMetadata,
      reporter: Reporter
  ): Future[Int] = {
    reporter.reportTestStarted(test.name)

    val result = runTestLifecycle {
      catchAll(bootstrapper.newInstance())
    } { instance =>
      catchAll(bootstrapper.before(instance))
    } { instance =>
      handleExpected(test.annotation.expected) {
        catchAll(bootstrapper.invokeTest(instance, test.name)) match {
          case Success(f) => f.recover { case t => Failure(t) }
          case Failure(t) => Future.successful(Failure(t))
        }
      }
    } { instance =>
      catchAll(bootstrapper.after(instance))
    }

    for {
      (errors, timeInSeconds) <- result
    } yield {
      val failed =
        reportExecutionErrors(reporter, Some(test.name), timeInSeconds, errors)
      reporter.reportTestFinished(test.name, errors.isEmpty, timeInSeconds)

      // Scala Native-specific: timeouts are warnings only, after the fact
      val timeout = test.annotation.timeout
      if (timeout != 0 && timeout <= timeInSeconds) {
        reporter.log(
          _.warn,
          "Timeout: took " + timeInSeconds + " sec, expected " +
            (timeout.toDouble / 1000) + " sec"
        )
      }

      failed
    }
  }

  private def reportExecutionErrors(
      reporter: Reporter,
      method: Option[String],
      timeInSeconds: Double,
      errors: List[Throwable]
  ): Int = {
    import org.junit.internal.AssumptionViolatedException
    import org.junit.TestCouldNotBeSkippedException

    errors match {
      case Nil =>
        // fast path
        0

      case (e: AssumptionViolatedException) :: Nil =>
        reporter.reportAssumptionViolation(method, timeInSeconds, e)
        0

      case _ =>
        val errorsPatchedForAssumptionViolations = errors.map {
          case error: AssumptionViolatedException =>
            new TestCouldNotBeSkippedException(error)
          case error =>
            error
        }
        reporter.reportErrors(
          "Test ",
          method,
          timeInSeconds,
          errorsPatchedForAssumptionViolations
        )
        errorsPatchedForAssumptionViolations.size
    }
  }

  private def loadBootstrapper(reporter: Reporter): Option[Bootstrapper] = {
    val bootstrapperName =
      taskDef().fullyQualifiedName() + "$scalanative$junit$bootstrapper$"

    try {
      val b = Reflect
        .lookupLoadableModuleClass(bootstrapperName)
        .getOrElse(
          throw new ClassNotFoundException(s"Cannot find $bootstrapperName")
        )
        .loadModule()

      b match {
        case b: Bootstrapper => Some(b)

        case _ =>
          throw new ClassCastException(
            s"Expected $bootstrapperName to extend Bootstrapper"
          )
      }
    } catch {
      case t: Throwable =>
        reporter.reportErrors(
          "Error while loading test class ",
          None,
          0,
          List(t)
        )
        None
    }
  }

  private def handleExpected(
      expectedException: Class[_ <: Throwable]
  )(body: => Future[Try[Unit]]) = {
    val wantException = expectedException != classOf[org.junit.Test.None]

    if (wantException) {
      for (r <- body) yield {
        r match {
          case Success(_) =>
            Failure(
              new AssertionError(
                "Expected exception: " + expectedException.getName
              )
            )

          case Failure(t) if expectedException.isInstance(t) =>
            Success(())

          case Failure(t) =>
            val expName = expectedException.getName
            val gotName = t.getClass.getName
            Failure(
              new Exception(
                s"Unexpected exception, expected<$expName> but was<$gotName>",
                t
              )
            )
        }
      }
    } else {
      body
    }
  }

  private def runTestLifecycle[T](build: => Try[T])(before: T => Try[Unit])(
      body: T => Future[Try[Unit]]
  )(after: T => Try[Unit]): Future[(List[Throwable], Double)] = {
    val startTime = System.nanoTime

    val exceptions: Future[List[Throwable]] = build match {
      case Success(x) =>
        val bodyFuture = before(x) match {
          case Success(()) => body(x)
          case Failure(t)  => Future.successful(Failure(t))
        }

        for (bodyResult <- bodyFuture) yield {
          val afterException = after(x).failed.toOption
          bodyResult.failed.toOption.toList ++ afterException.toList
        }

      case Failure(t) =>
        Future.successful(List(t))
    }

    for (es <- exceptions) yield {
      val timeInSeconds = (System.nanoTime - startTime).toDouble / 1000000000
      (es, timeInSeconds)
    }
  }

  private def catchAll[T](body: => T): Try[T] = {
    try {
      Success(body)
    } catch {
      case t: Throwable => Failure(t)
    }
  }

  override def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Array[Task] = throw new UnsupportedOperationException(
    "Supports Native only"
  )
}
