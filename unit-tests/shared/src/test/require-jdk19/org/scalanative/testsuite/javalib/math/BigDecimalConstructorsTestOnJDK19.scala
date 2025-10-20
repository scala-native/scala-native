package org.scalanative.testsuite.javalib.math

import java.math.BigDecimal

import org.junit.Assert._
import org.junit.Test

class BigDecimalConstructorsTestOnJDK19 {

  @Test def testFieldTWO(): Unit = {
    val twoS = "2"
    val twoD = 2.0
    assertEquals(twoS, BigDecimal.TWO.toString)
    assertEquals(twoD, BigDecimal.TWO.doubleValue(), 0.0d)
  }
}
