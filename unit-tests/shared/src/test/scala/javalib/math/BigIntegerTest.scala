package javalib.math

import java.math._

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class BigIntegerTest {
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

//   __scala_==

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
