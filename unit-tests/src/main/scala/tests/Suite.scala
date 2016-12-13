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
      case exc: Exception =>
        if (exc.getClass.equals(implicitly[ClassTag[T]].runtimeClass))
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
        case _: Exception => false
      }
    })

  def testNot(name: String)(body: => Unit): Unit =
    tests += Test(name, { () =>
      try {
        body
        false
      } catch {
        case _: Exception => true
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
