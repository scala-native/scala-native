package scala

import org.junit.Assert._
import org.junit.Test

class DivisionOverflowTest {
  @noinline def intMinus1 = -1
  @noinline def longMinus1 = -1L

  @Test def integerMinValueDivideMinus1(): Unit = {
    assertTrue(
      (java.lang.Integer.MIN_VALUE / intMinus1) == java.lang.Integer.MIN_VALUE
    )
  }

  @Test def integerMinValueRemainderMinus1(): Unit = {
    assertTrue((java.lang.Integer.MIN_VALUE % intMinus1) == 0)
  }

  @Test def longMinValueDivideMinus1(): Unit = {
    assertTrue(
      (java.lang.Long.MIN_VALUE / longMinus1) == java.lang.Long.MIN_VALUE
    )
  }

  @Test def longMinValueRemainderMinus1(): Unit = {
    assertTrue((java.lang.Long.MIN_VALUE % longMinus1) == 0)
  }
}
