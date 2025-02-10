package org.scalanative.testsuite.javalib.math

import java.math.BigInteger

import org.junit.Test
import org.junit.Assert._

class BigIntegerTestOnJDK19 {

  @Test def testFieldTWO(): Unit = {
    val twoS = "2"
    val twoI = 2
    assertEquals(twoS, BigInteger.TWO.toString)
    assertEquals(twoI, BigInteger.TWO.intValueExact())
  }
}
