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

  def assertTrue(cond: Boolean): Unit =
    assert(cond)

  def assertNot(cond: Boolean): Unit =
    if (cond) throw AssertionFailed else ()

  def assertFalse(cond: Boolean): Unit =
    assertNot(cond)

  def assertNull[A](a: A): Unit =
    assert(a == null)

  def assertNotNull[A](a: A): Unit =
    assertNot(a == null)

  def assertEquals[T](left: T, right: T): Unit =
    assert(left == right)

  def assertEquals(expected: Double, actual: Double, delta: Double): Unit =
    assert(Math.abs(expected - actual) <= delta)

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
          throw AssertionFailed
    }
    throw AssertionFailed
  }

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
