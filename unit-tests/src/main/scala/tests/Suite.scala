package tests

import sbt.testing.{EventHandler, Logger, Status}

import scala.collection.mutable
import scala.reflect.ClassTag

final case class AssertionFailed(msg: String) extends Exception(msg)

final case class TestResult(status: Boolean, thrown: Option[Throwable])

final case class Test(name: String, run: () => TestResult)

abstract class Suite {
  private val tests = new mutable.UnrolledBuffer[Test]

  def assert(cond: Boolean): Unit =
    assertTrue(cond)

  def assert(cond: Boolean, message: String): Unit =
    if (!cond) throw AssertionFailed(message) else ()

  def assertTrue(cond: Boolean): Unit =
    if (!cond) {
      throw AssertionFailed(s"condition is false")
    }

  def assertNot(cond: Boolean): Unit =
    assertFalse(cond)

  def assertFalse(cond: Boolean): Unit =
    if (cond) {
      throw AssertionFailed(s"condition is true")
    }

  def assertNull[A](a: A): Unit =
    if (a != null) {
      throw AssertionFailed(s"$a != null")
    }

  def assertNotNull[A](a: A): Unit =
    if (a == null) {
      throw AssertionFailed(s"$a == null")
    }

  def assertEquals[T](left: T, right: T): Unit =
    if (left != right) {
      throw AssertionFailed(s"${left} != ${right}")
    }

  def assertEquals(expected: Double, actual: Double, delta: Double): Unit =
    if (Math.abs(expected - actual) > delta) {
      throw AssertionFailed(s"$expected - $actual > $delta")
    }

  def expectThrows[T <: Throwable, U](expectedThrowable: Class[T],
                                      code: => U): Unit =
    assertThrowsImpl(expectedThrowable, code, (exc: T) => true)

  def assertThrows[T: ClassTag](f: => Unit): Unit =
    assertThrowsAnd(f)((exc: T) => true)

  def assertThrowsAnd[T: ClassTag](f: => Unit)(pred: T => Boolean): Unit = {
    val cls = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    assertThrowsImpl[T](cls, f, pred)
  }

  private def assertThrowsImpl[T](expected: Class[T],
                                  f: => Unit,
                                  pred: T => Boolean): Unit = {
    try {
      f
    } catch {
      case exc: Throwable =>
        if (expected.isInstance(exc) && pred(exc.asInstanceOf[T]))
          return
        else
          throw AssertionFailed(
            s"expected ${expected.getName} but got ${exc.getClass.getName}")
    }
    throw AssertionFailed(s"expected to throw ${expected.getName} but didn't")
  }

  def test(name: String)(body: => Unit): Unit =
    tests += Test(name, { () =>
      try {
        body
        TestResult(true, None)
      } catch {
        case thrown: Throwable => TestResult(false, Option(thrown))
      }
    })

  def testFails(name: String, issue: Int)(body: => Unit): Unit =
    tests += Test(name, { () =>
      try {
        body
        TestResult(false, None)
      } catch {
        case thrown: Throwable => TestResult(true, None)
      }
    })

  @inline private[this] def getThrownString(thrown: Option[Throwable],
                                            color: String,
                                            indent: Int): String = {
    if (thrown.isEmpty) ""
    else {
      val exc          = thrown.get
      val indentSpaces = " " * indent
      val info         = exc.toString
      val writer       = new java.io.StringWriter
      val printer      = new java.io.PrintWriter(writer)
      exc.printStackTrace(printer)

      val stacktrace = writer.toString
        .split('\n')
        .map { line =>
          s"\n${color}${indentSpaces}${line}"
        }
        .mkString("")

      s"\n${color}${indentSpaces}${info}" + stacktrace
    }
  }

  def run(eventHandler: EventHandler, loggers: Array[Logger]): Boolean = {
    val className = this.getClass.getName
    loggers.foreach(_.info("* " + className))
    var success = true

    tests.foreach { test =>
      val (TestResult(testSuccess, thrown)) = test.run()
      val (status, statusStr, color) =
        if (testSuccess) (Status.Success, "  [ok] ", Console.GREEN)
        else (Status.Failure, "  [fail] ", Console.RED)
      val event = NativeEvent(className, test.name, NativeFingerprint, status)

      val outMsg = color + statusStr + test.name +
        getThrownString(thrown, color, statusStr.length) +
        Console.RESET

      loggers.foreach(_.info(outMsg))

      eventHandler.handle(event)
      success = success && testSuccess
    }

    success
  }
}
