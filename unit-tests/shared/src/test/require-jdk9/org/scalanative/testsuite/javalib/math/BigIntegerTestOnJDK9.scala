package org.scalanative.testsuite.javalib.math

import java.math._
import java.util.Arrays

import org.junit.Test
import org.junit.Assert._

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

}
