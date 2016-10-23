package tests

import scala.collection.mutable
import scala.reflect.ClassTag

final case object AssertionFailed extends Exception

final case class Test(name: String, run: () => Boolean)

abstract class Suite {
  private val tests = new mutable.UnrolledBuffer[Test]

  def assert(cond: Boolean): Unit =
    if (!cond) throw AssertionFailed else ()

  def assert(cond: Boolean, msg: String): Unit =
    if (!cond) {
      println(msg)
      throw AssertionFailed
    } else ()

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
    var allPassed = true
    tests.foreach { test =>
      val res = test.run()
      println((if (res) "  [ok] " else "  [fail] ") + test.name)
      allPassed = allPassed && res
    }
    allPassed
  }
}
