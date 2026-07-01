// Ported from Scala.js, revision 0d6509a, dated 2026-06-28
// Additional tests for Scala Native

package org.scalanative.testsuite.javalib.math

import java.math._

import org.junit.Assert._
import org.junit.Test

class BigDecimalTest {

  // tests from Scala.js

  @noinline
  private def assertBDExactEquals(
      expectedUnscaledValue: BigInteger,
      expectedScale: Int,
      actual: BigDecimal
  ): Unit = {
    assertEquals(
      (expectedUnscaledValue, expectedScale),
      (actual.unscaledValue(), actual.scale())
    )
  }

  @Test def valueOfLong(): Unit = {
    assertBDExactEquals(BigInteger.valueOf(3L), 0, BigDecimal.valueOf(3L))
    assertBDExactEquals(
      BigInteger.valueOf(999999999L),
      0,
      BigDecimal.valueOf(999999999L)
    )
    assertBDExactEquals(
      BigInteger.valueOf(9999999999L),
      0,
      BigDecimal.valueOf(9999999999L)
    )
    assertBDExactEquals(
      BigInteger.valueOf(-999999999L),
      0,
      BigDecimal.valueOf(-999999999L)
    )
    assertBDExactEquals(
      BigInteger.valueOf(-9999999999L),
      0,
      BigDecimal.valueOf(-9999999999L)
    )
  }

  @Test def ctorString(): Unit = {
    assertBDExactEquals(BigInteger.valueOf(3L), 0, new BigDecimal("3"))
    assertBDExactEquals(BigInteger.valueOf(99L), 0, new BigDecimal("99"))
    assertBDExactEquals(
      BigInteger.valueOf(999999999L),
      0,
      new BigDecimal("999999999")
    )
    assertBDExactEquals(
      BigInteger.valueOf(9999999999L),
      0,
      new BigDecimal("9999999999")
    )
    assertBDExactEquals(BigInteger.valueOf(-99L), 0, new BigDecimal("-99"))
    assertBDExactEquals(
      BigInteger.valueOf(-999999999L),
      0,
      new BigDecimal("-999999999")
    )
    assertBDExactEquals(
      BigInteger.valueOf(-9999999999L),
      0,
      new BigDecimal("-9999999999")
    )

    assertBDExactEquals(BigInteger.valueOf(99L), 1, new BigDecimal("9.9"))
    assertBDExactEquals(BigInteger.valueOf(9999L), 2, new BigDecimal("99.99"))
    assertBDExactEquals(
      BigInteger.valueOf(999999L),
      3,
      new BigDecimal("999.999")
    )
    assertBDExactEquals(
      BigInteger.valueOf(99999999L),
      4,
      new BigDecimal("9999.9999")
    )
  }

  @Test def ctorDouble(): Unit = {
    assertBDExactEquals(new BigInteger("0"), 0, new BigDecimal(0.0))
    assertBDExactEquals(new BigInteger("15"), 1, new BigDecimal(1.5))
    assertBDExactEquals(
      new BigInteger("329999999999999982236431605997495353221893310546875"),
      50,
      new BigDecimal(3.3)
    )
    assertBDExactEquals(
      new BigInteger("999899999999999948840923025272786617279052734375"),
      46,
      new BigDecimal(99.99)
    )
    assertBDExactEquals(
      new BigInteger("9999999900000000707223080098628997802734375"),
      39,
      new BigDecimal(9999.9999)
    )
    assertBDExactEquals(
      new BigInteger("9999999999999998509883880615234375"),
      26,
      new BigDecimal(99999999.99999999)
    )
    assertBDExactEquals(
      new BigInteger("1000000000"),
      0,
      new BigDecimal(999999999.999999999)
    )
    assertBDExactEquals(
      new BigInteger("10000000000"),
      0,
      new BigDecimal(9999999999.9999999999)
    )
    assertBDExactEquals(
      new BigInteger("10000000000000000000"),
      0,
      new BigDecimal(1e19)
    )

    assertBDExactEquals(new BigInteger("0"), 0, new BigDecimal(-0.0))
    assertBDExactEquals(new BigInteger("-15"), 1, new BigDecimal(-1.5))
    assertBDExactEquals(
      new BigInteger("-329999999999999982236431605997495353221893310546875"),
      50,
      new BigDecimal(-3.3)
    )
    assertBDExactEquals(
      new BigInteger("-999899999999999948840923025272786617279052734375"),
      46,
      new BigDecimal(-99.99)
    )
    assertBDExactEquals(
      new BigInteger("-9999999900000000707223080098628997802734375"),
      39,
      new BigDecimal(-9999.9999)
    )
    assertBDExactEquals(
      new BigInteger("-9999999999999998509883880615234375"),
      26,
      new BigDecimal(-99999999.99999999)
    )
    assertBDExactEquals(
      new BigInteger("-1000000000"),
      0,
      new BigDecimal(-999999999.999999999)
    )
    assertBDExactEquals(
      new BigInteger("-10000000000"),
      0,
      new BigDecimal(-9999999999.9999999999)
    )
    assertBDExactEquals(
      new BigInteger("-10000000000000000000"),
      0,
      new BigDecimal(-1e19)
    ) // Scala.js #5381, also identical SN Issue #4967
  }

  // tests from Scala Native

  @Test def bigDecimalEqualEqualBigDecimal(): Unit = {
    val token = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)

    // Depending upon possible caching, they may or may not be eq.
    assertTrue(jbd1 == jbd2)
  }

  @Test def bigDecimalEqualsBigDecimal(): Unit = {
    val token = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)

    // Depending upon possible caching, they may or may not be reference eq.
    assertTrue(jbd1.equals(jbd2))
  }

  @Test def bigDecimalDoesNotEqualEqualBigDecimalWithDifferentValue(): Unit = {
    val token = 2046.5
    val jbd1: java.math.BigDecimal = java.math.BigDecimal.valueOf(token)
    val jbd2: java.math.BigDecimal = java.math.BigDecimal.valueOf(token + 1.0)

    assertFalse(jbd1 == jbd2)
  }

  // issue #2553
  @Test def bigDecimalSupportsDivideOperation(): Unit = {
    val rangeBD = BigDecimal.valueOf(1000000000)
    val valueBD = BigDecimal.valueOf(500000000)
    val fraction: BigDecimal = valueBD.divide(rangeBD, 9, RoundingMode.FLOOR)
    assertEquals(0.5, fraction.floatValue(), 0.000001)
  }
}
