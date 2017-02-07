package tests

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

  def assertThrows[T: ClassTag](f: => Unit): Unit = {
    try {
      f
    } catch {
      case exc: Throwable =>
        if (exc.getClass.equals(implicitly[ClassTag[T]].runtimeClass))
          return
        else
          throw AssertionFailed
    }
    throw AssertionFailed
  }

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

  def run(): Boolean = {
    println("* " + this.getClass.getName)
    var success = true

    tests.foreach { test =>
      val testSuccess = test.run()
      val status      = if (testSuccess) "  [ok] " else "  [fail] "
      println(status + test.name)
      success = success && testSuccess
    }

    success
  }
}
