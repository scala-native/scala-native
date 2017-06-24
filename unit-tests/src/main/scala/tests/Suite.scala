package tests

import sbt.testing.{EventHandler, Logger, Status}

import scala.collection.mutable
import scala.reflect.ClassTag

final case object AssertionFailed extends Exception

final case class Test(name: String, run: () => Boolean)

abstract class Suite {
  private val tests = new mutable.UnrolledBuffer[Test]

  def assert(cond: Boolean): Unit =
    if (!cond) throw AssertionFailed else ()

  def assertNot(cond: Boolean): Unit =
    if (cond) throw AssertionFailed else ()

  def assertThrowsAnd[T: ClassTag](f: => Unit)(fe: T => Boolean): Unit = {
    try {
      f
    } catch {
      case exc: Throwable =>
        if (exc.getClass.equals(implicitly[ClassTag[T]].runtimeClass) &&
            fe(exc.asInstanceOf[T]))
          return
        else
          throw AssertionFailed
    }
    throw AssertionFailed
  }

  def assertThrows[T: ClassTag](f: => Unit): Unit =
    assertThrowsAnd[T](f)(_ => true)

  def assertEquals[T](left: T, right: T): Unit =
    assert(left == right)

  private def assertThrowsImpl(cls: Class[_], f: => Unit): Unit = {
    try {
      f
    } catch {
      case exc: Throwable =>
        if (exc.getClass.equals(cls))
          return
        else
          throw AssertionFailed
    }
    throw AssertionFailed
  }

  def expectThrows[T <: Throwable, U](expectedThrowable: Class[T],
                                      code: => U): Unit =
    assertThrowsImpl(expectedThrowable, code)

  def test(name: String)(body: => Unit): Unit =
    tests += Test(name, { () =>
      try {
        body
        true
      } catch {
        case _: Throwable => false
      }
    })

  def testFails(name: String, issue: Int)(body: => Unit): Unit =
    tests += Test(name, { () =>
      try {
        body
        false
      } catch {
        case _: Throwable => true
      }
    })

  def run(eventHandler: EventHandler, loggers: Array[Logger]): Boolean = {
    val className = this.getClass.getName
    loggers.foreach(_.info("* " + className))
    var success = true

    tests.foreach { test =>
      val testSuccess = test.run()
      val (status, statusStr, color) =
        if (testSuccess) (Status.Success, "  [ok] ", Console.GREEN)
        else (Status.Failure, "  [fail] ", Console.RED)
      val event = NativeEvent(className, test.name, NativeFingerprint, status)
      loggers.foreach(_.info(color + statusStr + test.name + Console.RESET))
      eventHandler.handle(event)
      success = success && testSuccess

    }

    success
  }
}
