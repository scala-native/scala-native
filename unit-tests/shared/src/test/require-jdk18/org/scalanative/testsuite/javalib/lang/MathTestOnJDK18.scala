/* Ported from Scala.js commit: 7569c24 dated: 2025-05-20
 * Post JDK 8 methods added for Scala Native.
 *
 * For reasons internal to Scala.js practice, the Scala.js file was
 * named MathTestOnJDK21.
 * "require-jdk21/org/scalajs/testsuite/javalib/lang/MathTestOnJDK21.scala
 *
 * This file is in "required-jdk18/mumble/MathTestOnJDK18.scala" since
 * the method was introduced in JDK 18.
 */

package org.scalanative.testsuite.javalib.lang

import java.math.BigInteger
import java.util.SplittableRandom
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class MathTestOnJDK18 {

  @Test def testUnsignedMultiplyHigh(): Unit = {
    /* We fuzz-test by comparing to the "obvious" implementations based on
     * BigIntegers. We use a SplittableRandom generator, because Random cannot
     * generate all Long values.
     */

    val Seed = 909209754851418882L
    val Rounds = 1024

    val gen = new SplittableRandom(Seed)

    val mask = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)

    def ulongToBigInteger(a: Long): BigInteger =
      BigInteger.valueOf(a).and(mask)

    for (round <- 1 to Rounds) {
      val x = gen.nextLong()
      val y = gen.nextLong()

      val expected = {
        ulongToBigInteger(x)
          .multiply(ulongToBigInteger(y))
          .shiftRight(64)
          .longValue()
      }

      assertEquals(
        s"round $round, x = $x, y = $y",
        expected,
        Math.unsignedMultiplyHigh(x, y)
      )
    }
  }

  // ceilDiv

  case class CeilTestCase(
      dividend: scala.Long,
      divisor: scala.Long,
      expected: scala.Long
  )

  val ceilDivTestCases = Array(
    // from JVM 26 ceilDiv(i, i) example, ordered as in example
    CeilTestCase(-4L, 3L, -1L),
    CeilTestCase(4L, 3L, 2L),

    // Scala Native
    CeilTestCase(4L, 4L, 1L),
    CeilTestCase(4L, -4L, -1L),
    CeilTestCase(-4L, 4L, -1L),
    CeilTestCase(-4L, -4L, 1L),
    // Zero
    CeilTestCase(0L, 4L, 0L),
    CeilTestCase(0L, -4L, 0L),
    // check floating point quotient in (-1.0, 1.0) is rounded correctly.
    CeilTestCase(1L, 4L, 1L),
    CeilTestCase(1L, -4L, 0L),
    CeilTestCase(-1L, 4L, 0L),
    CeilTestCase(-1L, -4L, 1L)
  )

  @Test def ceilDiv_Exceptions(): Unit = {
    assertThrows(
      "ceilDiv(Int, Int)",
      classOf[ArithmeticException],
      Math.ceilDiv(4, 0)
    )

    assertThrows(
      "ceilDiv(Long, Int)",
      classOf[ArithmeticException],
      Math.ceilDiv(4L, 0)
    )

    assertThrows(
      "ceilDiv(Long, Long)",
      classOf[ArithmeticException],
      Math.ceilDiv(4L, 0L)
    )
  }

  @Test def ceilDiv_SpecialCases(): Unit = {
    assertEquals(
      "ceilDiv(Int, Int)",
      jl.Integer.MIN_VALUE,
      Math.ceilDiv(jl.Integer.MIN_VALUE, -1)
    )

    assertEquals(
      "ceilDiv(Long, Int)",
      jl.Long.MIN_VALUE,
      Math.ceilDiv(jl.Long.MIN_VALUE, -1)
    )

    assertEquals(
      "ceilDiv(Long, Long)",
      jl.Long.MIN_VALUE,
      Math.ceilDiv(jl.Long.MIN_VALUE, -1L)
    )
  }

  @Test def ceilDiv_IntInt(): Unit = {
    for (j <- 0 until ceilDivTestCases.length) {
      val tc = ceilDivTestCases(j)
      assertEquals(
        s" ceilDivII(${tc.dividend}, ${tc.divisor})",
        tc.expected,
        Math.ceilDiv(tc.dividend.toInt, tc.divisor.toInt)
      )
    }
  }

  @Test def ceilDiv_LongInt(): Unit = {
    for (j <- 0 until ceilDivTestCases.length) {
      val tc = ceilDivTestCases(j)
      assertEquals(
        s" ceilDivLI(${tc.dividend}, ${tc.divisor})",
        tc.expected,
        Math.ceilDiv(tc.dividend, tc.divisor.toInt)
      )
    }
  }

  @Test def ceilDiv_LongLong(): Unit = {
    for (j <- 0 until ceilDivTestCases.length) {
      val tc = ceilDivTestCases(j)
      assertEquals(
        s" ceilDivLL(${tc.dividend}, ${tc.divisor})",
        tc.expected,
        Math.ceilDiv(tc.dividend, tc.divisor)
      )
    }
  }

  // ceilMod

  val ceilModTestCases = Array(
    // from JVM 26 ceilMod(i, i) example, ordered as in example
    CeilTestCase(+4L, +3L, -2L),
    CeilTestCase(-4L, -3L, +2L),
    CeilTestCase(+4L, -3L, +1L),
    CeilTestCase(-4L, +3L, -1L),

    // Scala Native
    CeilTestCase(4L, 4L, 0L),
    CeilTestCase(4L, -4L, 0L),
    CeilTestCase(-4L, 4L, 0L),
    CeilTestCase(-4L, -4L, 0L),
    // Zero
    CeilTestCase(0L, 4L, 0L),
    CeilTestCase(0L, -4L, 0L),
    // check floating point quotient in (-1.0, 1.0) is rounded correctly.
    CeilTestCase(1L, 4L, -3L),
    CeilTestCase(1L, -4L, 1L),
    CeilTestCase(-1L, 4L, -1L),
    CeilTestCase(-1L, -4L, 3L)
  )

  @Test def ceilMod_Exceptions(): Unit = {
    assertThrows(
      "ceilMod(Int, Int)",
      classOf[ArithmeticException],
      Math.ceilMod(4, 0)
    )

    assertThrows(
      "ceilMod(Long, Int)",
      classOf[ArithmeticException],
      Math.ceilMod(4L, 0)
    )

    assertThrows(
      "ceilMod(Long, Long)",
      classOf[ArithmeticException],
      Math.ceilMod(4L, 0L)
    )
  }

  @Test def ceilMod_IntInt(): Unit = {
    for (j <- 0 until ceilModTestCases.length) {
      val tc = ceilModTestCases(j)
      assertEquals(
        s" ceilModLI(${tc.dividend}, ${tc.divisor})",
        tc.expected.toInt,
        Math.ceilMod(tc.dividend.toInt, tc.divisor.toInt)
      )
    }
  }

  @Test def ceilMod_LongInt(): Unit = {
    for (j <- 0 until ceilModTestCases.length) {
      val tc = ceilModTestCases(j)
      assertEquals(
        s" ceilModLI(${tc.dividend}, ${tc.divisor})",
        tc.expected.toInt,
        Math.ceilMod(tc.dividend, tc.divisor.toInt)
      )
    }
  }

  @Test def ceilMod_LongLong(): Unit = {
    for (j <- 0 until ceilModTestCases.length) {
      val tc = ceilModTestCases(j)
      assertEquals(
        s" ceilModLL(${tc.dividend}, ${tc.divisor})",
        tc.expected,
        Math.ceilMod(tc.dividend, tc.divisor)
      )
    }
  }
}
