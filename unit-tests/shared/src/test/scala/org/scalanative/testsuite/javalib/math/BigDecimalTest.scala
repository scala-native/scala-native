// Ported from Scala.js, revision c473689, dated 06.05.2021

package org.scalanative.testsuite.javalib.math

import java.math.{BigDecimal, RoundingMode}

import org.junit.Assert._
import org.junit.Test

class BigDecimalTest {

  // java.lang.Math.BigDecimal Int/Long Constructors

  @Test def valueOfLong3(): Unit = {
    val bd = BigDecimal.valueOf(3L)
    assertEquals(3, bd.intValue())
    assertTrue(bd.longValue == 3L)
  }

  @Test def valueOfLong999999999(): Unit = {
    val bd = BigDecimal.valueOf(999999999L)
    assertEquals(999999999, bd.intValue())
    assertTrue(bd.longValue == 999999999L)
  }

  @Test def valueOfLong9999999999(): Unit = {
    val bd = BigDecimal.valueOf(9999999999L)
    assertTrue(bd.longValue == 9999999999L)
  }

  @Test def valueOfLongNegative999999999(): Unit = {
    val bd = BigDecimal.valueOf(-999999999L)
    assertEquals(-999999999, bd.intValue())
    assertTrue(bd.longValue == -999999999L)
  }

  @Test def valueOfLongNegative9999999999(): Unit = {
    val bd = BigDecimal.valueOf(-9999999999L)
    assertTrue(bd.longValue == -9999999999L)
  }

  @Test def ctorString3(): Unit = {
    val bd = new BigDecimal("3")
    assertEquals(3, bd.intValue())
    assertTrue(bd.longValue == 3L)
  }

  @Test def ctorString99(): Unit = {
    val bd = new BigDecimal("99")
    assertEquals(99, bd.intValue())
    assertTrue(bd.longValue == 99L)
  }

  @Test def ctorString999999999(): Unit = {
    val bd = new BigDecimal("999999999")
    assertEquals(999999999, bd.intValue())
    assertTrue(bd.longValue == 999999999L)
  }

  @Test def ctorString9999999999(): Unit = {
    val bd = new BigDecimal("9999999999")
    assertTrue(bd.longValue == 9999999999L)
  }

  @Test def ctorStringNegative99(): Unit = {
    val bd = new BigDecimal("-99")
    assertEquals(-99, bd.intValue())
    assertTrue(bd.longValue == -99L)
  }

  @Test def ctorStringNegative999999999(): Unit = {
    val bd = new BigDecimal("-999999999")
    assertEquals(-999999999, bd.intValue())
    assertTrue(bd.longValue == -999999999L)
  }

  @Test def ctorStringNegative9999999999(): Unit = {
    val bd = new BigDecimal("-9999999999")
    assertTrue(bd.longValue == -9999999999L)
  }

  @Test def ctorString9Point9(): Unit = {
    val bd = new BigDecimal("9.9")
    assertEquals("9.9", bd.toString)
    assertEquals(9.9, bd.doubleValue(), 0.0)
  }

  @Test def ctorString99Point99(): Unit = {
    val bd = new BigDecimal("99.99")
    assertEquals(99.99, bd.doubleValue(), 0.0)
  }

  @Test def ctorString999Point999(): Unit = {
    val bd = new BigDecimal("999.999")
    assertEquals(999.999, bd.doubleValue(), 0.0)
  }

  @Test def ctorString9999Point9999(): Unit = {
    val bd = new BigDecimal("9999.9999")
    assertEquals(9999.9999, bd.doubleValue(), 0.0)
  }

  // java.lang.Math.BigDecimal double Constructors

  @Test def ctorDouble3Point3(): Unit = {
    val d = 3.3
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDouble99Point99(): Unit = {
    val d = 99.99
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDouble9999Point9999: Unit = {
    val d: Double = 9999.9999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDouble99999999Point99999999(): Unit = {
    val d = 99999999.99999999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDouble999999999Point999999999(): Unit = {
    val d = 999999999.999999999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDouble9999999999Point9999999999(): Unit = {
    val d = 9999999999.9999999999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDoubleNegative3Point3(): Unit = {
    val d = -3.3
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDoubleNegative99Point99(): Unit = {
    val d = -99.99
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDoubleNegative99999999Point99999999(): Unit = {
    val d = -99999999.99999999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDoubleNegative999999999Point999999999(): Unit = {
    val d = -999999999.999999999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
  }

  @Test def ctorDoubleNegative9999999999Point9999999999(): Unit = {
    val d = -9999999999.9999999999
    val bd = new BigDecimal(d)
    assertEquals(d, bd.doubleValue(), 0.0)
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
