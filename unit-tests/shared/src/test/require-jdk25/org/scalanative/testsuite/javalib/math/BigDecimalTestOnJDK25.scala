package org.scalanative.testsuite.javalib.math

import java.math.{BigDecimal, MathContext, RoundingMode}
import java.util.Arrays
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class BigDecimalTestOnJDK25 {

// sqrt

  @Test def sqrt_Exceptions(): Unit = {
    if (Platform.executingInJVMWithJDKIn(25 to Integer.MAX_VALUE)) {
      val bd = new BigDecimal("1.0")
      assertThrows(
        "sqrt(null) on Scala Native or JDK >= 25",
        classOf[NullPointerException],
        bd.sqrt(null)
      )
    }
  }
}
