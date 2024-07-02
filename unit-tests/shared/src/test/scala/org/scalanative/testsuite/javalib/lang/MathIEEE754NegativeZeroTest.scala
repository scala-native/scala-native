package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{lang => jl}
import java.lang._

class MathIEEE754NegativeZeroTest {

  @Test def mathDoubleMinWithNegativeZero(): Unit = {

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

  @Test def mathFloatMinWithNegativeZero(): Unit = {

    val min_A = Math.min(jl.Float.valueOf(-0.0f), 0.0f)

    assertTrue(
      "wrong min_A, min(-0.0F, 0.0F)",
      1.0 / min_A == Float.NEGATIVE_INFINITY
    )

    val min_B = Math.min(0.0f, jl.Float.valueOf(-0.0f))

    assertTrue(
      "wrong min_B, min(0.0F, -0.0F)",
      1.0 / min_B == Double.NEGATIVE_INFINITY
    )

    val min_C = Math.min(jl.Float.valueOf(-0.0f), jl.Float.valueOf(-0.0f))

    assertTrue(
      "wrong min, min(-0.0F, -0.0F)",
      1.0 / min_C == Float.NEGATIVE_INFINITY
    )
  }

  @Test def mathDoubleMaxWithNegativeZero(): Unit = {

    val max_A = Math.max(jl.Double.valueOf(-0.0d), 0.0d)

    assertTrue(
      "wrong max_A, max(-0.0D, 0.0D)",
      1.0 / max_A == Double.POSITIVE_INFINITY
    )

    val max_B = Math.max(0.0d, jl.Double.valueOf(-0.0d))

    assertTrue(
      "wrong max_B, max(0.0D, -0.0D)",
      1.0 / max_B == Double.POSITIVE_INFINITY
    )

    val max_C = Math.max(jl.Double.valueOf(-0.0d), jl.Double.valueOf(-0.0d))

    assertTrue(
      "wrong max_C, max(-0.0D, -0.0D)",
      1.0 / max_C == Double.NEGATIVE_INFINITY
    )
  }
  @Test def mathFloatMaxWithNegativeZero(): Unit = {

    val max_A = Math.max(jl.Float.valueOf(-0.0f), 0.0f)

    assertTrue(
      "wrong max_A, max(-0.0F, 0.0F)",
      1.0 / max_A == Float.POSITIVE_INFINITY
    )

    val max_B = Math.max(0.0f, jl.Float.valueOf(-0.0f))

    assertTrue(
      "wrong max_B, max(0.0F, -0.0F)",
      1.0 / max_B == Float.POSITIVE_INFINITY
    )

    val max_C = Math.max(jl.Float.valueOf(-0.0f), jl.Float.valueOf(-0.0f))

    assertTrue(
      "wrong max_C, max(-0.0F, -0.0F)",
      1.0 / max_C == Float.NEGATIVE_INFINITY
    )
  }
}
