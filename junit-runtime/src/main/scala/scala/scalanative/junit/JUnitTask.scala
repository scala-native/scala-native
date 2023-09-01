package scala.scalanative
package junit

import sbt.testing._

import scala.annotation.tailrec
import scala.scalanative.reflect.Reflect
import scala.util.{Failure, Success, Try}
import org.junit.TestCouldNotBeSkippedException

private[junit] final class JUnitTask(
    _taskDef: TaskDef,
    runSettings: RunSettings
) extends Task {
  def taskDef(): TaskDef = _taskDef
  def tags(): Array[String] = Array.empty

  override def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Array[Task] = {
    val reporter = new Reporter(eventHandler, loggers, runSettings, taskDef())

    loadBootstrapper(reporter).foreach { bootstrapper =>
      executeTests(bootstrapper, reporter)
    }

    Array.empty
  }

  private def executeTests(
      bootstrapper: Bootstrapper,
      reporter: Reporter
  ): Unit = {
    reporter.reportRunStarted()

    var failed = 0
    var ignored = 0
    var total = 0

    @tailrec
    def runTests(
        tests: List[TestMetadata],
        testClass: TestClassMetadata
    ): Try[Unit] = {
      val (nextIgnored, other) = tests.span(_.ignored)

      if (testClass.ignored) {
        reporter.reportIgnored(None)
        ignored += 1
        Success(())
      } else {
        nextIgnored.foreach(t => reporter.reportIgnored(Some(t.name)))
        ignored += nextIgnored.size

        other match {
          case t :: ts =>
            total += 1

            val fc = executeTestMethod(bootstrapper, t, reporter)
            failed += fc
            runTests(ts, testClass)

          case Nil =>
            Success(())
        }
      }
    }

    val result = runTestLifecycle {
      Success(())
    } { _ => catchAll(bootstrapper.beforeClass()) } { _ =>
      runTests(bootstrapper.tests().toList, bootstrapper.testClassMetadata())
    } { _ => catchAll(bootstrapper.afterClass()) }

    val (errors, timeInSeconds) = result

    errors match {
      case e :: Nil if isAssumptionViolation(e) =>
        reporter.reportAssumptionViolation(None, timeInSeconds, e)

      case es =>
        val errorsWithSkipped = es.map {
          case error: org.junit.internal.AssumptionViolatedException =>
            new TestCouldNotBeSkippedException(error)
          case error =>
            error
        }
        failed += es.size
        reporter.reportErrors("Test ", None, timeInSeconds, errorsWithSkipped)
    }

    reporter.reportRunFinished(failed, ignored, total, timeInSeconds)
  }

  private[this] def executeTestMethod(
      bootstrapper: Bootstrapper,
      test: TestMetadata,
      reporter: Reporter
  ): Int = {
    reporter.reportTestStarted(test.name)

    val result = runTestLifecycle {
      catchAll(bootstrapper.newInstance())
    } { instance => catchAll(bootstrapper.before(instance)) } { instance =>
      handleExpected(test.annotation.expected) {
        catchAll(bootstrapper.invokeTest(instance, test.name)) match {
          case Success(f) => f.value.get.flatten
          case Failure(t) => Failure(t)
        }
      }
    } { instance => catchAll(bootstrapper.after(instance)) }

    val (errors, timeInSeconds) = result

    val failed = errors match {
      case e :: Nil if isAssumptionViolation(e) =>
        reporter.reportAssumptionViolation(Some(test.name), timeInSeconds, e)
        0

      case es =>
        val errorsWithSkipped = es.map {
          case error: org.junit.internal.AssumptionViolatedException =>
            new TestCouldNotBeSkippedException(error)
          case error =>
            error
        }
        reporter.reportErrors(
          "Test ",
          Some(test.name),
          timeInSeconds,
          errorsWithSkipped
        )
        es.size
    }

    reporter.reportTestFinished(test.name, failed == 0, timeInSeconds)

    failed
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
  )(body: => Try[Unit]): Try[Unit] = {
    val wantException = expectedException != classOf[org.junit.Test.None]

    if (wantException) {
      body match {
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
    } else {
      body
    }
  }

  private def runTestLifecycle[T](build: => Try[T])(
      before: T => Try[Unit]
  )(body: T => Try[Unit])(after: T => Try[Unit]): (List[Throwable], Double) = {
    val startTime = System.nanoTime

    val exceptions: List[Throwable] = build match {
      case Success(x) =>
        val bodyResult = before(x) match {
          case Success(()) => body(x)
          case Failure(t)  => Failure(t)
        }

        val afterException = after(x).failed.toOption
        bodyResult.failed.toOption.toList ++ afterException.toList

      case Failure(t) =>
        List(t)
    }

    val timeInSeconds = (System.nanoTime - startTime).toDouble / 1000000000
    (exceptions, timeInSeconds)
  }

  private def isAssumptionViolation(ex: Throwable): Boolean = {
    ex.isInstanceOf[org.junit.AssumptionViolatedException] ||
    ex.isInstanceOf[org.junit.internal.AssumptionViolatedException]
  }

  private def catchAll[T](body: => T): Try[T] = {
    try {
      Success(body)
    } catch {
      case t: Throwable => Failure(t)
    }
  }
}
