package org.scalanative.testsuite.javalib.lang

import java.lang.*

import org.junit.Test
import org.junit.Assert.*

class MathTest {

  // Max()

  @Test def maxWithNanArguments(): Unit = {
    val a = 123.123d
    val b = 456.456d

    assertTrue("Double.NaN as first argument", Math.max(Double.NaN, b).isNaN)
    assertTrue("Double.NaN as second argument", Math.max(a, Double.NaN).isNaN)

    assertTrue(
      "Float.NaN as first argument",
      Math.max(Float.NaN, b.toFloat).isNaN
    )
    assertTrue("Float.NaN as second argument", Math.max(a, Float.NaN).isNaN)
  }

  @Test def maxAlphaGreaterThanBeta(): Unit = {
    val a = 578.910d
    val b = 456.456d
    assertTrue("Double", Math.max(a, b) == a)
    assertTrue("Float", Math.max(a.toFloat, b.toFloat) == a.toFloat)
    assertTrue("Int", Math.max(a.toInt, b.toInt) == a.toInt)
    assertTrue("Long", Math.max(a.toLong, b.toLong) == a.toLong)
  }

  @Test def maxAlphaEqualEqualBeta(): Unit = {
    val a = 576528
    val b = a
    assertTrue("Double", Math.max(a, b) == a)
    assertTrue("Float", Math.max(a.toFloat, b.toFloat) == a.toFloat)
    assertTrue("Int", Math.max(a.toInt, b.toInt) == a.toInt)
    assertTrue("Long", Math.max(a.toLong, b.toLong) == a.toLong)
  }

  @Test def maxAlphaLessThanBeta(): Unit = {
    val a = 123.123d
    val b = 456.456d
    assertTrue("Double", Math.max(a, b) == b)
    assertTrue("Float", Math.max(a.toFloat, b.toFloat) == b.toFloat)
    assertTrue("Int", Math.max(a.toInt, b.toInt) == b.toInt)
    assertTrue("Long", Math.max(a.toLong, b.toLong) == b.toLong)
  }

  // Min()

  @Test def minWithNanArguments(): Unit = {
    val a = 773.211d
    val b = 843.531d

    assertTrue("Double.NaN as first argument", Math.max(Double.NaN, b).isNaN)
    assertTrue("Double.NaN as second argument", Math.max(a, Double.NaN).isNaN)

    assertTrue(
      "Float.NaN as first argument",
      Math.max(Float.NaN, b.toFloat).isNaN
    )
    assertTrue("Float.NaN as second argument", Math.max(a, Float.NaN).isNaN)
  }

  @Test def minAlphaGreaterThanBeta(): Unit = {
    val a = 949.538d
    val b = 233.411d
    assertTrue("Double", Math.min(a, b) == b)
    assertTrue("Float", Math.min(a.toFloat, b.toFloat) == b.toFloat)
    assertTrue("Int", Math.min(a.toInt, b.toInt) == b.toInt)
    assertTrue("Long", Math.min(a.toLong, b.toLong) == b.toLong)
  }

  @Test def minAlphaEqualEqualBeta(): Unit = {
    val a = 553.838d
    val b = a
    assertTrue("Double", Math.min(a, b) == b)
    assertTrue("Float", Math.min(a.toFloat, b.toFloat) == b.toFloat)
    assertTrue("Int", Math.min(a.toInt, b.toInt) == b.toInt)
    assertTrue("Long", Math.min(a.toLong, b.toLong) == b.toLong)
  }

  @Test def minAlphaLessThanBeta(): Unit = {
    val a = 312.966d
    val b = 645.521d
    assertTrue("Double", Math.min(a, b) == a)
    assertTrue("Float", Math.min(a.toFloat, b.toFloat) == a.toFloat)
    assertTrue("Int", Math.min(a.toInt, b.toInt) == a.toInt)
    assertTrue("Long", Math.min(a.toLong, b.toLong) == a.toLong)
  }

  // round()

  @Test def roundDoubleSpecialValues(): Unit = {

    assertTrue("round(NaN) != 0L", Math.round(Double.NaN) == 0L)

    // value d as reported in issue #1071
    val dTooLarge: Double = 4228438087383875356545203991520.000000d
    val roundedTooLarge: Long = Math.round(dTooLarge)
    val expectedTooLarge: Long = scala.Long.MaxValue

    assertTrue(
      s"${roundedTooLarge} != ${expectedTooLarge}" +
        " when Double > Long.MaxValue",
      roundedTooLarge == expectedTooLarge
    )

    val roundedTooNegative: Long = Math.round(-1.0 * dTooLarge)
    val expectedTooNegative: Long = scala.Long.MinValue

    assertTrue(
      s"${roundedTooNegative} != ${expectedTooNegative}" +
        " when Double < Long.MinValue",
      roundedTooNegative == expectedTooNegative
    )
  }

  @Test def roundDoubleTiesRoundingTowardsPlusInfinity(): Unit = {

    case class TestPoint(value: Double, expected: Long)

    // Check that implementation addition of 0.5 does not cause
    // overflow into negative numbers. Values near MinValue do not
    // have this potential flaw.

    val testPointsOverflow = Seq(
      TestPoint((scala.Double.MaxValue - 0.4d), scala.Long.MaxValue)
    )

    // Useful/Definitive cases from URL:
    // https://docs.oracle.com/javase/10/docs/api/java/math/RoundingMode.html
    //
    // The expected values are from the Scala REPL/JVM.
    // The "ties towards +Infinity" rule best explains the observed results.
    // Note well that _none_ of the rounding modes at that URL describe the
    // the Scala REPL results, not even HALF_UP.

    val testPointsJavaApi = Seq(
      TestPoint(+5.5d, +6L),
      TestPoint(+2.5d, +3L),
      TestPoint(+1.6d, +2L),
      TestPoint(+1.1d, +1L),
      TestPoint(+1.0d, +1L),
      TestPoint(-1.0d, -1L),
      TestPoint(-1.1d, -1L),
      TestPoint(-1.6d, -2L),
      TestPoint(-2.5d, -2L),
      TestPoint(-5.5d, -5L)
    )

    // +2.5 and -2.5 are the distinguishing cases. They show that
    // math.round() is correctly rounding towards positive Infinity,
    //
    // The other cases are sanity cases to establish context.

    val testPoints = Seq(
      TestPoint(-2.6d, -3L),
      TestPoint(-2.5d, -2L),
      TestPoint(-2.4d, -2L),
      TestPoint(+2.4d, +2L),
      TestPoint(+2.5d, +3L),
      TestPoint(+2.6d, +3L)
    )

    val TestPointGroup = Seq(
      testPointsOverflow,
      testPointsJavaApi,
      testPoints
    )

    for (testPoints <- TestPointGroup) {
      for (testPoint <- testPoints) {
        val v: Double = testPoint.value
        val result: Long = math.round(v)
        val expected: Long = testPoint.expected

        assertTrue(
          s"round(${v}) result: ${result} != expected: ${expected}",
          result == testPoint.expected
        )
      }
    }
  }

  @Test def roundFloatSpecialValues(): Unit = {

    assertTrue("round(NaN) != 0", Math.round(Float.NaN) == 0)

    val fTooLarge: Float = scala.Float.MaxValue
    val roundedTooLarge: Int = Math.round(fTooLarge)
    val expectedTooLarge: Int = scala.Int.MaxValue

    assertTrue(
      s"${roundedTooLarge} != ${expectedTooLarge}" +
        " when Float > Int.MaxValue",
      roundedTooLarge == expectedTooLarge
    )

    val roundedTooNegative: Int = Math.round(scala.Float.MinValue)
    val expectedTooNegative: Int = scala.Int.MinValue

    assertTrue(
      s"${roundedTooNegative} != ${expectedTooNegative}" +
        " when Float < Int.MinValue",
      roundedTooNegative == expectedTooNegative
    )
  }

  @Test def roundFloatTiesRoundingTowardsPlusInfinity(): Unit = {

    case class TestPoint(value: Float, expected: Int)

    // See extensive comments in test for round(Double) above.

    // Check that implementation addition of 0.5 does not cause
    // overflow into negative numbers. Values near MinValue do not
    // have this potential flaw.

    val testPointsOverflow = Seq(
      TestPoint((scala.Float.MaxValue - 0.4f), scala.Int.MaxValue)
    )

    // Useful/Definitive cases from URL:
    // https://docs.oracle.com/javase/10/docs/api/java/math/RoundingMode.html
    //
    // The expected values are from the Scala REPL/JVM.
    // The "ties towards +Infinity" rule best explains the observed results.
    // Note well that _none_ of the rounding modes at that URL describe the
    // the Scala REPL results, not even HALF_UP.

    val testPointsJavaApi = Seq(
      TestPoint(+5.5f, +6),
      TestPoint(+2.5f, +3),
      TestPoint(+1.6f, +2),
      TestPoint(+1.1f, +1),
      TestPoint(+1.0f, +1),
      TestPoint(-1.0f, -1),
      TestPoint(-1.1f, -1),
      TestPoint(-1.6f, -2),
      TestPoint(-2.5f, -2),
      TestPoint(-5.5f, -5)
    )

    val testPoints = Seq(
      TestPoint(-97.6f, -98),
      TestPoint(-97.5f, -97),
      TestPoint(-97.4f, -97),
      TestPoint(+97.4f, +97),
      TestPoint(+97.5f, +98),
      TestPoint(+97.6f, +98)
    )

    val TestPointGroup = Seq(
      testPointsOverflow,
      testPointsJavaApi,
      testPoints
    )

    for (testPoints <- TestPointGroup) {
      for (testPoint <- testPoints) {
        val v: Float = testPoint.value
        val result: Int = math.round(v)
        val expected: Int = testPoint.expected

        assertTrue(
          s"round(${v}) result: ${result} != expected: ${expected}",
          result == testPoint.expected
        )
      }
    }
  }

}
