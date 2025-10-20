package scala.issues

import org.junit.Test
import org.junit.Assert.*

class Scala3IssuesTest:

  // Test itself does not have a large value, it does however assert that
  // usage of macros in the code, does not break compiler plugin
  @Test def canUseMacros(): Unit = {
    val result = Macros.test("foo")
    assertEquals(List(1, 2, 3), result)
  }

  @Test def test_Issue803(): Unit = {
    val x1: String = null
    var x2: String = "right"
    assertTrue(x1 + x2 == "nullright")

    val x3: String = "left"
    val x4: String = null
    assertTrue(x3 + x4 == "leftnull")

    val x5: AnyRef = new { override def toString = "custom" }
    val x6: String = null
    assertEquals("customnull", x5.toString + x6)

    val x7: String = null
    val x8: AnyRef = new { override def toString = "custom" }
    assertEquals("nullcustom", x7 + x8)

    val x9: String = null
    val x10: String = null
    assertEquals("nullnull", x9 + x10)

    // This syntax operation does not compile in Scala 3
    // When using `toString` on null it might throw NullPointerException
    // val x11: AnyRef = null
    // val x12: String = null
    // assertEquals("nullnull", x11 + x12)

    val x13: String = null
    val x14: AnyRef = null
    assertEquals("nullnull", x13 + x14)
  }

  @Test def issue2484(): Unit = {
    import scala.issues.issue2484.*
    assertEquals(
      5,
      CallByNeed.instance.map(CallByNeed(2))(_ + 3).value
    )
  }

  @Test def issue2543(): Unit = {
    import scala.reflect.Selectable.reflectiveSelectable
    def collectionClassName(i: Iterable[?]): String =
      i.asInstanceOf[{ def collectionClassName: String }].collectionClassName

    assertEquals("List", collectionClassName(List(1, 2, 3)))
  }

  @Test def issue2715(): Unit = {
    import reflect.Selectable.reflectiveSelectable
    class Foo {
      def bar(i: Int): String = (2 * i).toString
      def baz(i: Integer): String = (2 * i.intValue()).toString()
    }
    type Qux = {
      def bar(i: Int): String
      def baz(i: Integer): String
    }
    val z: Any = if true then new Foo else new AnyRef
    val q: Qux = z.asInstanceOf[Qux]
    assertEquals("42", q.bar(21))
    assertEquals("42", q.baz(21))
  }

  @Test def issue3014(): Unit = {
    import scala.issues.issue3014.*
    def useUnit(unit: TimeUnit): Long = {
      // Was throwing `MatchError` when calling `toNanos`
      unit.toNanos(1L)
    }

    assertEquals(1L, useUnit(TimeUnit.Nanos))
    assertThrows(classOf[NullPointerException], () => useUnit(null))
  }

end Scala3IssuesTest

private object issue2484 {
  final class CallByNeed[A] private (private var eval: () => A) {
    lazy val value: A = {
      val value0 = eval()
      eval = null
      value0
    }
  }

  object CallByNeed {
    def apply[A](a: => A): CallByNeed[A] = new CallByNeed[A](() => a)
    implicit val instance: Functor[CallByNeed] = new Functor[CallByNeed] {
      def map[A, B](fa: CallByNeed[A])(f: A => B) = CallByNeed(f(fa.value))
    }
  }

  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }
}

private object issue3014 {
  enum TimeUnit {
    case Millis
    case Nanos

    def toNanos(value: Long): Long = this match {
      case Millis => value * 1000000
      case Nanos  => value
    }
  }
}
