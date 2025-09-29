package scala

import org.junit.Assert._
import org.junit.Test

class FloatingToIntegerOverflowTest {
  @noinline def tooSmallFloatToInt = java.lang.Integer.MIN_VALUE.toFloat - 42
  @noinline def tooSmallDoubleToLong = java.lang.Long.MIN_VALUE.toDouble - 42
  @noinline def tooBigFloatToInt = java.lang.Integer.MAX_VALUE.toFloat + 42
  @noinline def tooBigDoubleToLong = java.lang.Long.MAX_VALUE.toDouble + 42
  @noinline def floatNaN = java.lang.Float.NaN
  @noinline def doubleNaN = java.lang.Double.NaN

  @Test def nanFloatToInt(): Unit = {
    assertTrue(floatNaN.toInt == 0)
  }

  @Test def nanFloatToLong(): Unit = {
    assertTrue(floatNaN.toLong == 0L)
  }

  @Test def nanDoubleToInt(): Unit = {
    assertTrue(doubleNaN.toInt == 0)
  }

  @Test def nanDoubleToLong(): Unit = {
    assertTrue(doubleNaN.toLong == 0L)
  }

  @Test def floatTooSmallToFitInInt(): Unit = {
    assertTrue(tooSmallFloatToInt.toInt == java.lang.Integer.MIN_VALUE)
  }

  @Test def floatTooSmallToFitInLong(): Unit = {
    assertTrue(tooSmallDoubleToLong.toLong == java.lang.Long.MIN_VALUE)
  }

  @Test def floatTooBigToFitInInt(): Unit = {
    assertTrue(tooBigFloatToInt.toInt == java.lang.Integer.MAX_VALUE)
  }

  @Test def floatTooBigToFitInLong(): Unit = {
    assertTrue(tooBigDoubleToLong.toLong == java.lang.Long.MAX_VALUE)
  }
}
