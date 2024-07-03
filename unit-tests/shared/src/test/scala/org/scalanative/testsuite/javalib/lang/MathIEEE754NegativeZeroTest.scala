package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{lang => jl}
import java.lang._

class MathIEEE754NegativeZeroTest {
  /* Most Tests in this suite use 'AssertEquals' because it displays
   * the arguments when they do not match.
   *
   * Some tests use 'AssertTrue' with an explicit "==" when known and visible
   * numerical equality increases confidence in the Test.
   */

  @Test def mathDoubleMinWithNegativeZero(): Unit = {

    val min_A = Math.min(jl.Double.valueOf(-0.0d), 0.0d)
    assertTrue(
      s"min(-0.0D, 0.0D) expected: -0.0D got: ${min_A}",
      1.0d / min_A == Double.NEGATIVE_INFINITY
    )

    val min_B = Math.min(0.0d, jl.Double.valueOf(-0.0d))
    assertTrue(
      s"min(0.0D, -0.0D) expected: -0.0D got: ${min_B}",
      1.0d / min_B == Double.NEGATIVE_INFINITY
    )

    val min_C = Math.min(jl.Double.valueOf(-0.0d), jl.Double.valueOf(-0.0d))
    assertTrue(
      s"min(-0.0D, -0.0D) expected: -0.0D got: ${min_C}",
      1.0d / min_C == Double.NEGATIVE_INFINITY
    )
  }

  @Test def mathFloatMinWithNegativeZero(): Unit = {

    val min_A = Math.min(jl.Float.valueOf(-0.0f), 0.0f)
    assertTrue(
      s"min(-0.0F, 0.0F) expected: -0.0F got: ${min_A}F",
      1.0f / min_A == Float.NEGATIVE_INFINITY
    )

    val min_B = Math.min(0.0f, jl.Float.valueOf(-0.0f))
    assertTrue(
      s"min(0.0F, -0.0F) expected: -0.0F got: ${min_B}F",
      1.0f / min_B == Double.NEGATIVE_INFINITY
    )

    val min_C = Math.min(jl.Float.valueOf(-0.0f), jl.Float.valueOf(-0.0f))
    assertTrue(
      s"min(-0.0F, -0.0F) expected: -0.0D got: ${min_C}F",
      1.0f / min_C == Float.NEGATIVE_INFINITY
    )
  }

  @Test def mathDoubleMaxWithNegativeZero(): Unit = {

    val max_A = Math.max(jl.Double.valueOf(-0.0d), 0.0d)
    assertTrue(
      s"max(-0.0D, 0.0D) expected: 0.0D got: ${max_A}",
      1.0d / max_A == Double.POSITIVE_INFINITY
    )

    val max_B = Math.max(0.0d, jl.Double.valueOf(-0.0d))
    assertTrue(
      s"max(0.0D, -0.0D) expected: 0.0D got: ${max_B}",
      1.0d / max_B == Double.POSITIVE_INFINITY
    )

    val max_C = Math.max(jl.Double.valueOf(-0.0d), jl.Double.valueOf(-0.0d))
    assertTrue(
      s"max(-0.0D, -0.0D) expected: -0.0D got: ${max_C}",
      1.0d / max_C == Double.NEGATIVE_INFINITY
    )
  }

  @Test def mathFloatMaxWithNegativeZero(): Unit = {

    val max_A = Math.max(jl.Float.valueOf(-0.0f), 0.0f)
    assertTrue(
      s"max(-0.0F, 0.0F) expected: 0.0F got: ${max_A}F",
      1.0f / max_A == Float.POSITIVE_INFINITY
    )

    val max_B = Math.max(0.0f, jl.Float.valueOf(-0.0f))
    assertTrue(
      s"max(0.0F, -0.0F) expected: 0.0F got: ${max_B}F",
      1.0f / max_B == Float.POSITIVE_INFINITY
    )

    val max_C = Math.max(jl.Float.valueOf(-0.0f), jl.Float.valueOf(-0.0f))
    assertTrue(
      s"max(-0.0F, -0.0F) expected: -0.0F got: ${max_C}F",
      1.0f / max_C == Float.NEGATIVE_INFINITY
    )
  }

  /* Specifically test the Double and Float instance compareTo() and
   * static compare() methods with negative zero.
   * Their correctness is _critical_ to utility of negative zero in the wild.
   */

  final val negZeroD = jl.Double.valueOf(-0.0d)
  final val posZeroD = jl.Double.valueOf(+0.0d)

  final val negZeroF = jl.Float.valueOf(-0.0f)
  final val posZeroF = jl.Float.valueOf(+0.0f)

  // Check that proper negative zeros are created, even in Release-fast mode.
  @Test def validateIEEE754NegativeZeros(): Unit = {
    assertTrue(
      s"Expected a Double negative zero",
      (1.0d / negZeroD) == Double.NEGATIVE_INFINITY
    )

    assertTrue(
      s"Expected a Float negative zero",
      (1.0f / negZeroF) == Float.NEGATIVE_INFINITY
    )
  }

  @Test def doubleCompareToUsingNegativeZero(): Unit = {

    assertEquals(
      s"-0.0D.compareTo(+0.0D)",
      -1,
      negZeroD.compareTo(posZeroD)
    )

    assertEquals(
      s"-0.0D.compareTo(-0.0D)",
      0,
      negZeroD.compareTo(negZeroD)
    )

    assertEquals(
      s"+0.0D.compareTo(-0.0D)",
      1,
      posZeroD.compareTo(negZeroD)
    )
  }

  @Test def floatCompareToUsingNegativeZero(): Unit = {

    assertEquals(
      s"-0.0F.compareTo(+0.0F)",
      -1,
      negZeroF.compareTo(posZeroF)
    )

    assertEquals(
      s"-0.0F.compareTo(-0.0F)",
      0,
      negZeroF.compareTo(negZeroF)
    )

    assertEquals(
      s"+0.0F.compareTo(-0.0F)",
      1,
      posZeroF.compareTo(negZeroF)
    )
  }

  @Test def doubleCompareUsingNegativeZero(): Unit = {

    assertEquals(
      s"Double.compare(-0.0D, +0.0D)",
      -1,
      jl.Double.compare(negZeroD, posZeroD)
    )

    assertEquals(
      s"Double.compare(-0.0D, -0.0D)",
      0,
      jl.Double.compare(negZeroD, negZeroD)
    )

    assertEquals(
      s"Double.compare(+0.0D, -0.0D)",
      1,
      jl.Double.compare(posZeroD, negZeroD)
    )
  }

  @Test def floatCompareUsingNegativeZero(): Unit = {

    assertEquals(
      s"Float.compare(-0.0F, +0.0F)",
      -1,
      jl.Float.compare(negZeroF, posZeroF)
    )

    assertEquals(
      s"Float.compare(-0.0D, -0.0D)",
      0,
      jl.Float.compare(negZeroF, negZeroF)
    )

    assertEquals(
      s"Float.compare(+0.0D, -0.0D)",
      1,
      jl.Float.compare(posZeroF, negZeroF)
    )
  }

  /* To round out the lot, test other usual suspects: signum() & copySign().
   */
  @Test def doubleSignumUsingNegativeZero(): Unit = {

    val signumNZ = Math.signum(negZeroD)
    assertTrue(
      s"signum(-0.0D) expected: -0.0D got: ${signumNZ}",
      (1.0d / signumNZ) == Double.NEGATIVE_INFINITY
    )

    val signumPZ = Math.signum(posZeroD)
    assertTrue(
      s"signum(+0.0D) expected: +0.0D got: ${signumPZ}",
      (1.0d / signumPZ) == Double.POSITIVE_INFINITY
    )
  }

  @Test def floatSignumUsingNegativeZero(): Unit = {

    val signumNZ = Math.signum(negZeroF)
    assertTrue(
      s"signum(-0.0F) expected: -0.0f got: ${signumNZ}",
      (1.0f / signumNZ) == Float.NEGATIVE_INFINITY
    )

    val signumPZ = Math.signum(posZeroF)
    assertTrue(
      s"signum(+0.0F) expected: +0.0F got: ${signumPZ}",
      (1.0f / signumPZ) == Float.POSITIVE_INFINITY
    )
  }

  @Test def doubleCopySignUsingNegativeZero(): Unit = {

    val negCopiedToOne = Math.copySign(1.0d, negZeroD)
    assertEquals(
      s"copysign(1.0D, -0.0D)",
      -1.0d,
      negCopiedToOne,
      0.0d
    )

    val negCopiedToZero = Math.copySign(0.0d, negZeroD)
    assertEquals(
      s"copysign(0.0D, -0.0D)",
      Double.NEGATIVE_INFINITY,
      1.0d / negCopiedToZero,
      0.0d
    )

    val posCopied = Math.copySign(-1.0d, posZeroD)

    assertEquals(
      s"copysign(-1.0D, 0.0D)",
      1.0d,
      posCopied,
      0.0d
    )
  }

  @Test def floatCopySignUsingNegativeZero(): Unit = {

    val negCopiedToOne = Math.copySign(1.0f, negZeroF)
    assertEquals(
      s"copysign(1.0F, -0.0F)",
      -1.0f,
      negCopiedToOne,
      0.0f
    )

    val negCopiedToZero = Math.copySign(0.0f, negZeroF)
    assertEquals(
      s"copysign(0.0F, -0.0F)",
      Float.NEGATIVE_INFINITY,
      1.0f / negCopiedToZero,
      0.0f
    )

    val posCopied = Math.copySign(-1.0f, posZeroF)

    assertEquals(
      s"copysign(-1.0F, 0.0F)",
      1.0f,
      posCopied,
      0.0f
    )
  }
}
