// Ported from Scala.js, revision c473689, dated 2012-06-05

package org.scalanative.testsuite.javalib.math

import java.math.BigInteger
import java.util.Arrays

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class BigIntegerTest {

  @Test def ctorArrayByte3(): Unit = {
    val bi = new BigInteger(Array[Byte](3))
    assertEquals(3, bi.intValue())
  }

  @Test def ctorArrayByte127(): Unit = {
    val bi = new BigInteger(Array[Byte](127))
    assertEquals(127, bi.intValue())
  }

  @Test def valueOfLong3(): Unit = {
    val bi = BigInteger.valueOf(3L)
    assertEquals(3, bi.intValue())
    assertEquals(3L, bi.longValue())
  }

  @Test def valueOfLong999999999(): Unit = {
    val bi = BigInteger.valueOf(999999999L)
    assertEquals(999999999, bi.intValue())
    assertEquals(999999999L, bi.longValue())
  }

  @Test def valueOfLong9999999999(): Unit = {
    val bi = BigInteger.valueOf(9999999999L)
    assertEquals(9999999999L, bi.longValue())
  }

  @Test def valueOfLongNegative999999999(): Unit = {
    val bi = BigInteger.valueOf(-999999999L)
    assertEquals(-999999999, bi.intValue())
    assertEquals(-999999999L, bi.longValue())
  }

  @Test def valueOfLongNegative9999999999(): Unit = {
    val bi = BigInteger.valueOf(-9999999999L)
    assertEquals(-9999999999L, bi.longValue())
  }

  @Test def ctorString99(): Unit = {
    val bi = new BigInteger("99")
    assertEquals(99, bi.intValue())
    assertEquals(99L, bi.longValue())
  }

  @Test def ctorString999999999(): Unit = {
    val bi = new BigInteger("999999999")
    assertEquals(999999999, bi.intValue())
    assertEquals(999999999L, bi.longValue())
  }

  @Test def ctorString9999999999(): Unit = {
    val bi = new BigInteger("9999999999")
    assertEquals(9999999999L, bi.longValue())
  }

  @Test def ctorStringNegative99(): Unit = {
    val bi = new BigInteger("-99")
    assertEquals(-99, bi.intValue())
    assertEquals(-99L, bi.longValue())
  }

  @Test def ctorStringNegative999999999(): Unit = {
    val bi = new BigInteger("-999999999")
    assertEquals(-999999999, bi.intValue())
    assertEquals(-999999999L, bi.longValue())
  }

  @Test def ctorStringNegative9999999999(): Unit = {
    val bi = new BigInteger("-9999999999")
    assertEquals(-9999999999L, bi.longValue())
  }

  @Test def ctorArrayBytePosTwosComplement(): Unit = {
    val eBytesSignum = Array[Byte](27, -15, 65, 39)
    val eBytes = Array[Byte](27, -15, 65, 39)
    val expSignum = new BigInteger(eBytesSignum)
    assertTrue(Arrays.equals(eBytes, expSignum.toByteArray))
  }

  @Test def ctorArrayByteNegTwosComplement(): Unit = {
    val eBytesSignum = Array[Byte](-27, -15, 65, 39)
    val eBytes = Array[Byte](-27, -15, 65, 39)
    val expSignum = new BigInteger(eBytesSignum)
    assertTrue(Arrays.equals(eBytes, expSignum.toByteArray))
  }

  @Test def ctorArrayByteSign1PosTwosComplement(): Unit = {
    val eBytes = Array[Byte](27, -15, 65, 39)
    val eSign = 1
    val exp = new BigInteger(eSign, eBytes)
    assertTrue(Arrays.equals(eBytes, exp.toByteArray))
  }

  @Test def ctorIntArrayByteSign0Zeros(): Unit = {
    val eBytes = Array[Byte](0, 0, 0, 0)
    val eSign = 0
    val exp = new BigInteger(eSign, eBytes)
    assertTrue(Arrays.equals(Array[Byte](0), exp.toByteArray))
  }

  @Test def ctorIntArrayByteSignNeg1(): Unit = {
    val eBytes = Array[Byte](27)
    val eSign = -1
    val eRes = Array[Byte](-27)
    val exp = new BigInteger(eSign, eBytes)
    assertTrue(Arrays.equals(eRes, exp.toByteArray))
  }

  @Test def ctorIntArrayByteSignNeg1PosTwosComplement(): Unit = {
    val eBytes = Array[Byte](27, -15, 65, 39)
    val eSign = -1
    val eRes = Array[Byte](-28, 14, -66, -39)
    val exp = new BigInteger(eSign, eBytes)
    assertTrue(Arrays.equals(eRes, exp.toByteArray))
  }

  @Test def ctorArrayByteSign1CompareNoSignTwosComplement(): Unit = {
    val eBytes = Array[Byte](27, -15, 65, 39)
    val eSign = 1
    val exp = new BigInteger(eSign, eBytes)
    val eBytesSignum = Array[Byte](27, -15, 65, 39)
    val expSignum = new BigInteger(eBytesSignum)

    assertEquals(0, expSignum.compareTo(exp))
    assertTrue(Arrays.equals(eBytes, exp.toByteArray))
    assertTrue(Arrays.equals(eBytes, expSignum.toByteArray))
    assertTrue(Arrays.equals(exp.toByteArray, expSignum.toByteArray))
  }

  @Test def ctorIntArrayByteCompareCtorArrayByte(): Unit = {
    val eBytes = Array[Byte](27, -15, 65, 39)
    val eSign = -1
    val eRes = Array[Byte](-28, 14, -66, -39)
    val exp = new BigInteger(eSign, eBytes)
    val eBytesSignum = Array[Byte](-28, 14, -66, -39)
    val expSignum = new BigInteger(eBytesSignum)

    assertEquals(exp.toString, expSignum.toString)
    assertTrue(Arrays.equals(eRes, exp.toByteArray))
    assertTrue(Arrays.equals(eBytesSignum, expSignum.toByteArray))
    assertTrue(Arrays.equals(exp.toByteArray, expSignum.toByteArray))
  }

  // original tests from Scala Native
  // byteValueExact

  val byteMaxBi = new BigInteger(java.lang.Byte.MAX_VALUE.toString)
  val byteMinBi = new BigInteger(java.lang.Byte.MIN_VALUE.toString)

  @Test def byteValueExactWithBigIntegerGreaterThanByteMaxValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = byteMaxBi.add(BigInteger.ONE)
        bi.byteValueExact()
      }
    )
  }

  @Test def byteValueExactWithBigIntegerEqualsByteMaxValueShouldNotThrow()
      : Unit = {
    assertTrue(byteMaxBi.byteValueExact() == java.lang.Byte.MAX_VALUE)
  }

  @Test def byteValueExactWithBigIntegerEqualEqualByteMinValueShouldNotThrow()
      : Unit = {
    assertTrue(byteMinBi.byteValueExact() == java.lang.Byte.MIN_VALUE)
  }

  @Test def byteValueExactWithBigIntegerLessThanByteMinValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = byteMinBi.subtract(BigInteger.ONE)
        bi.byteValueExact()
      }
    )
  }

// intValueExact

  val intMaxBi = new BigInteger(java.lang.Integer.MAX_VALUE.toString)
  val intMinBi = new BigInteger(java.lang.Integer.MIN_VALUE.toString)

  @Test def intValueExactWithBigIntegerGreaterThanIntegerMaxValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = intMaxBi.add(BigInteger.ONE)
        bi.intValueExact()
      }
    )
  }

  @Test def intValueExactWithBigIntegerEqualEqualIntegerMaxValueShouldNotThrow()
      : Unit = {
    assertTrue(intMaxBi.intValueExact() == java.lang.Integer.MAX_VALUE)
  }

  @Test def intValueExactWithBigIntegerEqualEqualIntegerMinValueShouldNotThrow()
      : Unit = {
    assertTrue(intMinBi.intValueExact() == java.lang.Integer.MIN_VALUE)
  }

  @Test def intValueExactWithBigIntegerLessThanIntegerMinValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = intMinBi.subtract(BigInteger.ONE)
        bi.intValueExact()
      }
    )
  }

// longValueExact

  val longMaxBi = new BigInteger(java.lang.Long.MAX_VALUE.toString)
  val longMinBi = new BigInteger(java.lang.Long.MIN_VALUE.toString)

  @Test def longValueExactWithBigIntegerGreaterThanLongMaxValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = longMaxBi.add(BigInteger.ONE)
        bi.longValueExact()
      }
    )
  }

  @Test def longValueExactWithBigIntegerEqualEqualLongMaxValueShouldNotThrow()
      : Unit = {
    assertTrue(longMaxBi.longValueExact() == java.lang.Long.MAX_VALUE)
  }

  @Test def longValueExactWithBigIntegerEqualEqualLongMinValueShouldNotThrow()
      : Unit = {
    assertTrue(longMinBi.longValueExact() == java.lang.Long.MIN_VALUE)
  }

  @Test def longValueExactWithBigIntegerLessThanLongMinValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = longMinBi.subtract(BigInteger.ONE)
        bi.longValueExact()
      }
    )
  }

// shortValueExact

  val shortMaxBi = new BigInteger(java.lang.Short.MAX_VALUE.toString)
  val shortMinBi = new BigInteger(java.lang.Short.MIN_VALUE.toString)

  @Test def shortValueExactWithBigIntegerGreaterThanShortMaxValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = shortMaxBi.add(BigInteger.ONE)
        bi.shortValueExact()
      }
    )
  }

  @Test def shortValueExactWithBigIntegerEqualEqualShortMaxValueShouldNotThrow()
      : Unit = {
    assertTrue(shortMaxBi.shortValueExact() == java.lang.Short.MAX_VALUE)
  }

  @Test def shortValueExactWithBigIntegerEqualEqualShortMinValueShouldNotThrow()
      : Unit = {
    assertTrue(shortMinBi.shortValueExact() == java.lang.Short.MIN_VALUE)
  }

  @Test def shortValueExactWithBigIntegerLessThanShortMinValueShouldThrow()
      : Unit = {
    assertThrows(
      classOf[ArithmeticException], {
        val bi = shortMinBi.subtract(BigInteger.ONE)
        bi.shortValueExact()
      }
    )
  }

  @Test def bigIntegerEqualEqualBigInteger(): Unit = {
    val token = 2047L
    val jbi1: java.math.BigInteger = java.math.BigInteger.valueOf(token)
    val jbi2: java.math.BigInteger = java.math.BigInteger.valueOf(token)

    // Depending upon possible caching, they may or may not be eq.
    assertTrue(jbi1 == jbi2)
  }

  @Test def bigIntegerEqualsBigInteger(): Unit = {
    val token = 2047L
    val jbi1: java.math.BigInteger = java.math.BigInteger.valueOf(token)
    val jbi2: java.math.BigInteger = java.math.BigInteger.valueOf(token)

    // Depending upon possible caching, they may or may not be reference eq.
    assertTrue(jbi1.equals(jbi2))
  }

  @Test def bigIntegerDoesNotEqualEqualBigIntegerWithDifferentValue(): Unit = {
    val token = 2047L
    val jbi1: java.math.BigInteger = java.math.BigInteger.valueOf(token)
    val jbi2: java.math.BigInteger = java.math.BigInteger.valueOf(token + 1)

    assertFalse(jbi1 == jbi2)
  }

}
