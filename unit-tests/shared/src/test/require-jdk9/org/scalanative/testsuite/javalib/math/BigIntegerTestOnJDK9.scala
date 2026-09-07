package org.scalanative.testsuite.javalib.math

import java.math._
import java.util.Arrays
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class BigIntegerTestOnJDK9 {

  @Test def testFieldTWO(): Unit = {
    val twoS = "2"
    val twoI = 2
    assertEquals(twoS, BigInteger.TWO.toString)
    assertEquals(twoI, BigInteger.TWO.intValueExact())
  }

  @Test def ctorArrayBytePosTwosComplement(): Unit = {
    val eBytesSignum = Array[Byte](0, 0, 0, 27, -15, 65, 39, 0, 0, 0)
    val eBytes = Array[Byte](27, -15, 65, 39)
    val expSignum = new BigInteger(eBytesSignum, 3, 4)
    assertTrue(Arrays.equals(eBytes, expSignum.toByteArray))
  }

  @Test def ctorArrayByteNegTwosComplement(): Unit = {
    val eBytesSignum = Array[Byte](0, 0, 0, -27, -15, 65, 39, 0, 0, 0)
    val eBytes = Array[Byte](-27, -15, 65, 39)
    val expSignum = new BigInteger(eBytesSignum, 3, 4)
    assertTrue(Arrays.equals(eBytes, expSignum.toByteArray))
  }

  @Test def ctorArrayByteSign1PosTwosComplement(): Unit = {
    val eBytes = Array[Byte](0, 0, 0, 27, -15, 65, 39, 0, 0, 0)
    val eSign = 1
    val exp = new BigInteger(eSign, eBytes, 3, 4)
    assertTrue(Arrays.equals(Arrays.copyOfRange(eBytes, 3, 7), exp.toByteArray))
  }

// sqrt

  @Test def sqrt_Exceptions(): Unit = {
    val bi = new BigInteger("-2")

    assertThrows(
      classOf[ArithmeticException],
      bi.sqrt()
    )
  }

  @Test def sqrtGeZero(): Unit = {
    locally {
      val expectedRoot = 5L
      val power = 2
      val testValue = jl.Math.pow(expectedRoot.toDouble, power.toDouble).toLong

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.sqrt().longValue

      assertEquals(expectedRoot, foundRoot)
    }

    locally {
      val expectedRoot = 0
      val testValue = expectedRoot // // 0^2 known to be 0

      val bi = new BigInteger(testValue.toString)

      val foundRoot = bi.sqrt().longValue

      assertEquals(expectedRoot, foundRoot)
    }
  }

// sqrtAndRemainder

  @Test def sqrtAndRemainder_Exceptions()(): Unit = {

    val bi = new BigInteger("-1")

    assertThrows(
      classOf[ArithmeticException],
      bi.sqrtAndRemainder()
    )
  }

  @Test def sqrtAndRemainderGeZero(): Unit = {
    locally {
      val expectedRoot = 5L
      val expectedRemainder = 0L
      val power = 2
      val testValue = jl.Math.pow(expectedRoot.toDouble, power.toDouble).toLong

      val bi = new BigInteger(testValue.toString)

      val result = bi.sqrtAndRemainder()

      assertEquals("a1_1", expectedRoot, result(0).longValue)
      assertEquals("a1_2", expectedRemainder, result(1).longValue)
    }

    locally {
      val expectedRoot = 7L
      val expectedRemainder = 1L
      val power = 2
      val testValue =
        jl.Math.pow(expectedRoot.toDouble, power.toDouble).toLong +
          expectedRemainder

      val bi = new BigInteger(testValue.toString)

      val result = bi.sqrtAndRemainder()

      assertEquals("a2_1", expectedRoot, result(0).longValue)
      assertEquals("a2_2", expectedRemainder, result(1).longValue)
    }

    locally {
      val expectedRoot = 0L
      val expectedRemainder = 0L
      val testValue = expectedRoot // 0^2 known to be 0

      val bi = new BigInteger(testValue.toString)

      val result = bi.sqrtAndRemainder()

      assertEquals("a4_1", expectedRoot, result(0).longValue)
      assertEquals("a4_2", expectedRemainder, result(1).longValue)
    }
  }
}
