package scala.scalanative.native

import collection.mutable

object FinallySuite extends tests.Suite {

  private val events = new mutable.ArrayBuffer[String]()

  private val Try = "Try"
  private val Catch = "Catch"
  private val Finally = "Finally"

  test("try throw catch throw finally") {
    withExpectedEvents(Seq(Try, Catch, Finally)) {
      expectException {
        try {
          event(Try)
          throw new Exception
        } catch {
          case e: Exception =>
            event(Catch)
            throw e
        } finally {
          event(Finally)
        }
      }
    }
  }

  test("try throw finally") {
    withExpectedEvents(Seq(Try, Finally)) {
      expectException {
        try {
          event(Try)
          throw new Exception
        } finally {
          event(Finally)
        }
      }
    }
  }

  test("try finally") {
    withExpectedEvents(Seq(Try, Finally)) {
      try {
        event(Try)
      } finally {
        event(Finally)
      }
    }
  }

  test("try throw catch finally") {
    withExpectedEvents(Seq(Try, Catch, Finally)) {
      try {
        event(Try)
        throw new Exception
      } catch {
        case e: Exception =>
          event(Catch)
      } finally {
        event(Finally)
      }
    }
  }

  test("try (return) finally (return)") {
    // error: constant expression type mismatch
    // %src.12 = phi i32 [bitcast (%"class.scala.scalanative.runtime.BoxedUnit$"* @"scala.scalanative.runtime.BoxedUnit$" to i8*), %src.6]
    // assert(1() == 1, "tryFinally_1")

    assertInt(tryFinally_2(), 1, "tryFinally_2")
    assertInt(tryFinally_3(), 2, "tryFinally_3")
    assertInt(tryFinally_4(), 2, "tryFinally_4")
  }

  test("try throw catch (return) finally (return)") {
    // error: constant expression type mismatch
    // %src.20 = phi i32 [bitcast (%"class.scala.scalanative.runtime.BoxedUnit$"* @"scala.scalanative.runtime.BoxedUnit$" to i8*), %src.15]
    // assert(catchFinally_1() == 1, "catchFinally_1")

    assertInt(catchFinally_2(), 1, "catchFinally_2")
    assertInt(catchFinally_3(), 2, "catchFinally_3")
    assertInt(catchFinally_4(), 2, "catchFinally_4")
  }

  test("should execute finally only once when throw in finally 1") {
    withExpectedEvents(Seq(Try, Catch, Finally)) {
      expectException {
        try {
          event(Try)
          throw new Exception
        } catch {
          case e: Exception =>
            event(Catch)
        } finally {
          event(Finally)
          throw new Exception
        }
      }
    }
  }

  test("should execute finally only once when throw in finally 2") {
    withExpectedEvents(Seq(Try, Finally)) {
      expectException {
        try {
          event(Try)
          throw new Exception
        } finally {
          event(Finally)
          throw new RuntimeException
        }
      }
    }
  }

  test("should execute finally only once when throw in finally 3") {
    withExpectedEvents(Seq(Try, Finally)) {
      expectException {
        try {
          event(Try)
        } finally {
          event(Finally)
          throw new Exception
        }
      }
    }
  }

  test("throwing finally should shadow previous exception") {
    withExpectedEvents(Seq(Try, Finally)) {
      assertThrows[IllegalArgumentException] {
        try {
          event(Try)
          throw new IllegalStateException()
        } finally {
          event(Finally)
          throw new IllegalArgumentException
        }
      }
    }
  }

  def tryFinally_1(): Int =
    try {
      1
    } finally {
      2
    }

  def tryFinally_2(): Int =
    try {
      return 1
    } finally {
      2
    }

  def tryFinally_3(): Int =
    try {
      1
    } finally {
      return 2
    }

  def tryFinally_4(): Int =
    try {
      return 1
    } finally {
      return 2
    }

  def catchFinally_1(): Int = try {
    try {
      throw new Exception
    } catch {
      case e: Exception => 1
    } finally {
      2
    }
  }

  def catchFinally_2(): Int = try {
    try {
      throw new Exception
    } catch {
      case e: Exception => return 1
    } finally {
      2
    }
  }

  def catchFinally_3(): Int = try {
    try {
      throw new Exception
    } catch {
      case e: Exception => 1
    } finally {
      return 2
    }
  }

  def catchFinally_4(): Int = try {
    try {
      throw new Exception
    } catch {
      case e: Exception => return 1
    } finally {
      return 2
    }
  }

  private def event(s: String): Unit = events += s

  private def reset(): Unit = events.clear()

  private def assertEvents(expectedEvents: Seq[String]): Unit = {
    def sameElements(): Boolean = {
      events.zip(expectedEvents).forall { case (a, e) => a == e }
    }
    val sameLen = events.size == expectedEvents.size

    if (!(sameLen && sameElements())) {
      val e = expectedEvents.mkString("[", ", ", "]")
      val a = events.mkString("[", ", ", "]")
      assert(false, s"[Failed] Expected: $e, actual: $a")
    }
  }

  private def withExpectedEvents(expectedEvents: Seq[String])(
      f: => Unit): Unit = {
    reset()
    f
    assertEvents(expectedEvents)
  }

  private def expectException(fun: => Unit): Unit = {
    assertThrows[Exception](fun)
  }

  private def assertInt(actual: Int, expected: Int, msg: String): Unit =
    if (actual != expected) {
      assert(false, s"[Failed] $msg: expected: $expected, actual: $actual")
    }

}
