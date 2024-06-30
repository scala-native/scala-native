package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.lang._

class MathTestOnJDK21 {

  // tolerances for IEEE 754 floating point number comparisons.
  final val epsilonD = 0.00000000d
  final val epsilonF = 0.0f

  // clamp() Double

  @Test def clampDoubleWithIllegalArguments(): Unit = {

    assertThrows(
      "min is a NaN",
      classOf[IllegalArgumentException],
      Math.clamp(Math.TAU, Double.NaN, 1.1)
    )

    assertThrows(
      "max is a NaN",
      classOf[IllegalArgumentException],
      Math.clamp(Math.TAU, 2.2, Double.NaN)
    )

    val lb = 4.4 // By intent, lowerBound is greater than highBound.
    val hb = 3.3

    assertThrows(
      s"bounds: ${lb} > ${hb}",
      classOf[IllegalArgumentException],
      Math.clamp(Math.TAU, lb, hb)
    )

    assertThrows(
      "min is +0.0 and max is -0.0",
      classOf[IllegalArgumentException],
      Math.clamp(Math.TAU, +0.0, -0.0)
    )
  }

  @Test def clampDoubleValueIsNaN(): Unit = {
    val clamped = Math.clamp(Double.NaN, -1.0, 1.0)

    assertTrue("value is not a NaN", clamped.isNaN())
  }

  @Test def clampDouble(): Unit = {

    // Closed interval [min, max], both endpoints are included.
    case class TestPoint(
        value: Double,
        min: Double,
        max: Double,
        expected: Double
    )

    /* The first TestPoint below, from the JVM doc example, only detects
     * behavior-under-test if IEEE 754 negative zero is less than positive
     * zero. Detect implementation flaws early.
     *
     * Case in point, see Issue #3977.
     * The next assertion looks strange because it works around that issue.
     */

    assertTrue(
      "-0.0D does not compare less than +0.0D",
      Double.compare(Double.valueOf(-0.0d), Double.valueOf(+0.0d)) == -1
    )

    val testPoints = Seq(
      TestPoint(Double.valueOf(-0.0), +0.0, 1.0, 0.0), // JVM 21 doc test case
      TestPoint(+0.0, +0.0, 1.1, 0.0),
      TestPoint(0.5, 0.0, 1.2, 0.5),
      TestPoint(1.3, 0.0, 1.3, 1.3),
      TestPoint(1.5, 0.0, 1.4, 1.4),
      TestPoint(-0.0, -2.0, -0.0, -0.0),
      TestPoint(-1.1, -2.1, -0.0, -1.1),
      TestPoint(-2.2, -2.2, -0.0, -2.2),
      TestPoint(-2.5, -2.3, -0.0, -2.3)
    )

    val TestPointGroup = Seq(
      testPoints
    )

    for (testPoint <- testPoints) {
      val v = testPoint.value
      val min = testPoint.min
      val max = testPoint.max
      val expected = testPoint.expected

      val result = Math.clamp(v, min, max)

      assertEquals(
        s"unexpected clamp(${v}, ${min}, ${max}) result",
        expected,
        result,
        epsilonD
      )
    }
  }

  // clamp() Float

  @Test def clampFloatWithIllegalArguments(): Unit = {

    assertThrows(
      "min is a NaN",
      classOf[IllegalArgumentException],
      Math.clamp(Float.MIN_VALUE, Float.NaN, 1.1f)
    )

    assertThrows(
      "max is a NaN",
      classOf[IllegalArgumentException],
      Math.clamp(Float.MIN_VALUE, 2.2f, Float.NaN)
    )

    val lb = 14.4f // By intent, lowerBound is greater than highBound.
    val hb = 13.3f

    assertThrows(
      s"bounds: ${lb} > ${hb}",
      classOf[IllegalArgumentException],
      Math.clamp(Float.MIN_VALUE, lb, hb)
    )

    assertThrows(
      "min is +0.0 and max is -0.0",
      classOf[IllegalArgumentException],
      Math.clamp(Float.MIN_VALUE, +0.0f, -0.0f)
    )
  }

  @Test def clampFloatValueIsNaN(): Unit = {
    val clamped = Math.clamp(Float.NaN, -1.0f, 1.0f)

    assertTrue("value is not a NaN", clamped.isNaN())
  }

  @Test def clampFloat(): Unit = {

    // Closed interval [min, max], both endpoints are included.
    case class TestPoint(
        value: Float,
        min: Float,
        max: Float,
        expected: Float
    )

    /* The first TestPoint below, from the JVM doc example, only detects
     *  behavior-under-test if IEEE 754 negative zero is less than positive
     *  zero. Detect implementation flaws early.
     *
     * The next assertion looks strange because it works around that issue.
     */

    assertTrue(
      "-0.0F does not compare less than +0.0F",
      Float.compare(Float.valueOf(-0.0f), Float.valueOf(+0.0f)) == -1
    )

    val testPoints = Seq(
      TestPoint(-0.0f, +0.0f, 1.0f, +0.0f), // from JVM 21 clamp(float) doc.
      TestPoint(+0.0f, +0.0f, 1.1f, +0.0f),
      TestPoint(0.5f, 0.0f, 1.2f, 0.5f),
      TestPoint(1.3f, 0.0f, 1.3f, 1.3f),
      TestPoint(1.5f, 0.0f, 1.4f, 1.4f),
      TestPoint(-0.0f, -2.0f, -0.0f, -0.0f),
      TestPoint(-1.1f, -2.1f, -0.0f, -1.1f),
      TestPoint(-2.2f, -2.2f, -0.0f, -2.2f),
      TestPoint(-2.5f, -2.3f, -0.0f, -2.3f)
    )

    val TestPointGroup = Seq(
      testPoints
    )

    for (testPoint <- testPoints) {
      val v = testPoint.value
      val min = testPoint.min
      val max = testPoint.max
      val expected = testPoint.expected

      val result = Math.clamp(v, min, max)

      assertEquals(
        s"unexpected clamp(${v}, ${min}, ${max}) result",
        expected,
        result,
        epsilonF
      )
    }
  }

  // clamp() LongToInt

  @Test def clampLongToIntWithIllegalArguments(): Unit = {
    val lb = 628 // By intent, lowerBound is greater than highBound.
    val hb = 6

    assertThrows(
      s"bounds: ${lb} > ${hb}",
      classOf[IllegalArgumentException],
      Math.clamp(Long.MAX_VALUE, lb, hb)
    )
  }

  @Test def clampLongToInt(): Unit = {

    // Closed interval [min, max], both endpoints are included.
    case class TestPoint(
        value: Long,
        min: Int,
        max: Int,
        expected: Int
    )

    val testPoints = Seq(
      TestPoint(Long.MIN_VALUE, Integer.MIN_VALUE, 0, Integer.MIN_VALUE),
      TestPoint(
        Integer.MIN_VALUE.toLong,
        Integer.MIN_VALUE,
        0,
        Integer.MIN_VALUE
      ),
      TestPoint(6L, 5, 12, 6),
      TestPoint(-8L, -13, -7, -8),
      TestPoint(
        Integer.MAX_VALUE.toLong,
        0,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE
      ),
      TestPoint(Long.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE)
    )

    val TestPointGroup = Seq(
      testPoints
    )

    for (testPoint <- testPoints) {
      val v = testPoint.value
      val min = testPoint.min
      val max = testPoint.max
      val expected = testPoint.expected

      val result = Math.clamp(v, min, max)

      assertEquals(
        s"unexpected clamp(${v}, ${min}, ${max}) result",
        expected,
        result
      )
    }
  }

  // clamp() Long

  @Test def clampLongWithIllegalArguments(): Unit = {
    val lb = 41L // By intent, lowerBound is greater than highBound.
    val hb = 20L

    assertThrows(
      s"bounds: ${lb} > ${hb}",
      classOf[IllegalArgumentException],
      Math.clamp(Long.MAX_VALUE, lb, hb)
    )
  }

  @Test def clampLong(): Unit = {

    // Closed interval [min, max], both endpoints are included.
    case class TestPoint(
        value: Long,
        min: Long,
        max: Long,
        expected: Long
    )

    val testPoints = Seq(
      TestPoint(2L, 5L, 11L, 5L),
      TestPoint(5L, 5L, 12L, 5L),
      TestPoint(6L, 5L, 13L, 6L),
      TestPoint(14L, 5L, 14L, 14L),
      TestPoint(16L, 5L, 15L, 15L),
      TestPoint(-17L, -16L, -1L, -16L),
      TestPoint(-16L, -16L, -2L, -16L),
      TestPoint(-12L, -16L, -3L, -12L),
      TestPoint(-4L, -17L, -4L, -4L),
      TestPoint(-2L, -17L, -5L, -5L)
    )

    val TestPointGroup = Seq(
      testPoints
    )

    for (testPoint <- testPoints) {
      val v = testPoint.value
      val min = testPoint.min
      val max = testPoint.max
      val expected = testPoint.expected

      val result = Math.clamp(v, min, max)

      assertEquals(
        s"unexpected clamp(${v}, ${min}, ${max}) result",
        expected,
        result
      )
    }
  }
}
