// Ported from Scala.js, revision c473689, dated 2012-06-05

package org.scalanative.testsuite.javalib.math

import java.math.{BigInteger, BigDecimal}

import org.junit.Test
import org.junit.Assert._

class BigDecimalToStringTest {

  @Test def testToStringWithCornerCaseScales(): Unit = {
    val bigIntOne = BigInteger.valueOf(1)

    assertEquals("1", new BigDecimal(bigIntOne, 0).toString())

    assertEquals("0.01", new BigDecimal(bigIntOne, 2).toString())
    assertEquals("0.000001", new BigDecimal(bigIntOne, 6).toString())
    assertEquals("1E-7", new BigDecimal(bigIntOne, 7).toString())
    assertEquals(
      "1E-2147483647",
      new BigDecimal(bigIntOne, 2147483647).toString()
    )

    assertEquals("1E+1", new BigDecimal(bigIntOne, -1).toString())
    assertEquals("1E+2", new BigDecimal(bigIntOne, -2).toString())
    assertEquals("1E+15", new BigDecimal(bigIntOne, -15).toString())
    assertEquals(
      "1E+2147483647",
      new BigDecimal(bigIntOne, -2147483647).toString()
    )
    assertEquals(
      "1E+2147483648",
      new BigDecimal(bigIntOne, -2147483648).toString()
    ) // #4088

    val bigInt123 = BigInteger.valueOf(123)

    assertEquals("123", new BigDecimal(bigInt123, 0).toString())

    assertEquals("1.23", new BigDecimal(bigInt123, 2).toString())
    assertEquals("0.000123", new BigDecimal(bigInt123, 6).toString())
    assertEquals("0.00000123", new BigDecimal(bigInt123, 8).toString())
    assertEquals("1.23E-7", new BigDecimal(bigInt123, 9).toString())
    assertEquals(
      "1.23E-2147483645",
      new BigDecimal(bigInt123, 2147483647).toString()
    )

    assertEquals("1.23E+3", new BigDecimal(bigInt123, -1).toString())
    assertEquals("1.23E+4", new BigDecimal(bigInt123, -2).toString())
    assertEquals("1.23E+17", new BigDecimal(bigInt123, -15).toString())
    assertEquals(
      "1.23E+2147483649",
      new BigDecimal(bigInt123, -2147483647).toString()
    ) // #4088
    assertEquals(
      "1.23E+2147483650",
      new BigDecimal(bigInt123, -2147483648).toString()
    ) // #4088
  }

  // Ported from Scala.js, commit 3851c2d, dated: 2020-06-19
  // Adapted for Scala Native

  @Test def testToStringWithRoundingMode(): Unit = {
    import java.math.RoundingMode
    import RoundingMode._
    import org.scalanative.testsuite.utils.AssertThrows.assertThrows

    val group1: Seq[RoundingMode] = Seq(UP, CEILING, HALF_UP)
    val group2: Seq[RoundingMode] = Seq(DOWN, FLOOR, HALF_DOWN, HALF_EVEN)

    val decimal = BigDecimal.valueOf(1.2345)
    group1.foreach { mode =>
      assertEquals("1.235", decimal.setScale(3, mode).toString)
    }
    group2.foreach { mode =>
      assertEquals("1.234", decimal.setScale(3, mode).toString)
    }
    assertThrows(classOf[ArithmeticException], decimal.setScale(3, UNNECESSARY))
  }
}
