package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{lang => jl}
import java.lang._

class NegativeZeroTestOnJDK21 {

  @Test def mathMinWithNegativeZero(): Unit = {

    val min_A = Math.min(jl.Double.valueOf(-0.0d), 0.0d)

    assertTrue(
      "wrong min_A, min(-0.0D, 0.0D)",
      1.0 / min_A == Double.NEGATIVE_INFINITY
    )

    //    /* Fails on CI, passes Clang 18 & JVM */
    val min_B = Math.min(0.0d, jl.Double.valueOf(-0.0d))

    assertTrue(
      "wrong min_B, min(0.0D, -0.0D)",
      1.0 / min_B == Double.NEGATIVE_INFINITY
    )

    // Seems to pass on CI Clang 14, as well as Windows Clang 17
    val min_C = Math.min(jl.Double.valueOf(-0.0d), jl.Double.valueOf(-0.0d))

    assertTrue(
      "wrong min, min(-0.0D, -0.0D)",
      1.0 / min_C == Double.NEGATIVE_INFINITY
    )
  }

}
