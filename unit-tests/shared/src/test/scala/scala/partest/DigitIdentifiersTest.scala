package scala.partest

import org.junit.Assert._
import org.junit.Test

class DigitIdentifiersTest {

  object `1Test` {
    val `1S` = "One"
    val `-1L` = -1L

    sealed abstract class `0` {
      val `1` = 1

      def `2` = 2

      var `3` = `1` + `2`
    }

    sealed trait `-1` {
      val `-1` = -1
    }

    case class `1Foo`(`2Δ`: Double, `-2Δ`: Float) extends `0` with `-1`

  }

  @Test def handlesIdentifiersStartingWithDigit(): Unit = {

    import `1Test`._

    val x = new `1Foo`(2.0, -2.0f)
    x.`3` = 0

    assertTrue(`1S` == "One")
    assertTrue(`-1L` == -1L)
    assertEquals(2.0, x.`2Δ`, 0.00000001)
    assertEquals(-2.0, x.`-2Δ`, 0.00000001)
    assertTrue(x.`-1` == -1)
    assertTrue(x.`1` == 1)
    assertTrue(x.`2` == 2)
    assertTrue(x.`3` == 0)
  }
}
