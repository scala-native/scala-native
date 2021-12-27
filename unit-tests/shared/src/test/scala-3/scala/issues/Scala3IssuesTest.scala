package scala.issues

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.unsafe._

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

end Scala3IssuesTest
