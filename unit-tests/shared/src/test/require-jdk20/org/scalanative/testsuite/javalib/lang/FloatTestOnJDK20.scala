package org.scalanative.testsuite.javalib.lang

import java.lang._
// Because this test is in the java.lang package, an unqualified Float
// is a java.lang.Float. Prior art used unqualified Float freely,
// with that intent.  Scala.js JFloat is introduced to minimize changes
// in ported Scala.js tests. Existing usages of unqualified Double
// are not changed. Joys of blending code bases.
import java.lang.{Float => JFloat, Short => JShort}

import org.junit.Assert._
import org.junit.Test

class FloatTestOnJDK20 {

  /* Scala Native additions for features added after Java 8.
   */

  /** Since: Java 12 */
  @Test def testDescribeConstable(): Unit = {
    val expected = JFloat.valueOf(1.01f)
    val result = expected
      .describeConstable()
      .orElseGet(() => { fail("describeConstable is empty"); null })

    assertTrue(result.eq(expected)) // Require reference equality
  }

  /** Since: Java 12 */
  /** Requires reflection so can not presently (SN 0.5.12) be implemented on
   *  Scala Native.
   */
  //  @Test def testresolveConstantDesc (): Unit

  /** Since: Java 19 */
  @Test def testPrecision(): Unit = {
    assertEquals("PRECISION", 24, JFloat.PRECISION)
  }

  /** Since: Java 20 */
  @Test def testFloat16ToFloat_SpecialCases(): Unit = {
    /* Use explicit, known Float16 (binary16) values to avoid the
     * possibility of compensating to/from errors in code under test.
     */

    val epsilon = 0.0f

    assertEquals(
      "NaN",
      JFloat.NaN,
      JFloat.float16ToFloat(32256.toShort), // Float16 NaN
      epsilon
    )

    locally {
      assertEquals(
        "positive infinity",
        JFloat.POSITIVE_INFINITY,
        JFloat.float16ToFloat(31744.toShort), // Float16 POSITIVE_INFINITY
        epsilon
      )

      assertEquals(
        "negative infinity",
        JFloat.NEGATIVE_INFINITY,
        JFloat.float16ToFloat(-1024), // Float16 NEGATIVE_INFINITY
        epsilon
      )
    }

    locally {
      val pzResult = JFloat.float16ToFloat(0.toShort) // +0.0f
      assertEquals("positive zero", 0.0f, pzResult, epsilon)

      val nzResult = JFloat.float16ToFloat(-32768.toShort) // -0.0f
      assertEquals("negative zero", -0.0f, nzResult, epsilon)

      /* Scala Native tends to have problems/defects with IEEE754 negative
       * zeros. Test explicitly even though assertEquals should have caught
       * such.
       */
      assertEquals(
        "nzResult should be negative",
        0,
        JFloat.compare(nzResult, -0.0f)
      )
    }
  }

  /** Since: Java 20 */
  @Test def testFloat16ToFloat_Normals(): Unit = {
    val epsilon = 0.0f

    locally {
      val expected = 1.7753906f
      val f16Uut = 16154.toShort

      assertEquals("positive", expected, JFloat.float16ToFloat(f16Uut), epsilon)
    }

    locally {
      val expected = -1.7783203f
      val f16Uut = -16611.toShort

      assertEquals("negative", expected, JFloat.float16ToFloat(f16Uut), epsilon)
    }
  }

  /** Since: Java 20 */
  @Test def testFloat16ToFloat_Subnormals(): Unit = {
    val epsilon = 0.0f

    locally {
      val expected = 0.000000059604645f
      val f16Uut = 1.toShort // positive subnornal closest to 0.0f

      assertEquals(
        "positive closest",
        expected,
        JFloat.float16ToFloat(f16Uut),
        epsilon
      )
    }

    locally {
      val expected = -5.960464477539063e-8
      val f16Uut = 0x8001.toShort // negative subnormal closest to -0.0f

      assertEquals(
        "negative closest",
        expected,
        JFloat.float16ToFloat(f16Uut),
        epsilon
      )
    }
  }

  /** Since: Java 20 */
  @Test def testFloatToFloat16_SpecialCases(): Unit = {
    /* Use explicit, known Float16 (binary16) values to avoid the
     * possibility of compensating to/from errors in code under test.
     */

    val epsilon = 0.0f

    assertEquals(
      "NaN",
      32256.toShort,
      JFloat.floatToFloat16(JFloat.NaN)
    )

    locally {
      assertEquals(
        "positive infinity",
        31744.toShort,
        JFloat.floatToFloat16(JFloat.POSITIVE_INFINITY)
      )

      assertEquals(
        "negative infinity",
        -1024.toShort,
        JFloat.floatToFloat16(JFloat.NEGATIVE_INFINITY)
      )
    }

    locally {
      val pzResult = JFloat.floatToFloat16(0.0f)
      assertEquals("positive zero", 0.toShort, pzResult)

      val nzResult = JFloat.floatToFloat16(-0.0f)
      assertEquals("negative zero", -32768.toShort, nzResult)
    }
  }

  /** Since: Java 20 */
  @Test def testFloatToFloat16_Normals(): Unit = {

    locally {
      val expected = 16162.toShort
      val f32Uut = 1.783f // Will exercise HALF_EVEN rounding.

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }

    locally {
      val expected = -15955.toShort
      val f32Uut = -2.837f

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }
  }

  @Test def testFloatToFloat16_Subnormals(): Unit = {
    /* This is a minimal smoke test. Future developers may will want
     * to add additional carefully chosen test points.
     *
     * Rounding is always tricky and prone to harbor bugs. half_even rounding
     * in particular, might benefit from additional cases.
     */

    locally {
      val expected = 0.toShort
      val f32Uut = JFloat.MIN_VALUE // Exercise/force underflow

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }

    locally {
      val expected = 1.toShort
      val f32Uut = 0.000000059604645f // smallest positive F16 subnormal

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }

    locally {
      val expected = -32767.toShort
      val f32Uut = -0.000000059604645f // closest to 0.0 negative F16 subnormal

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }

    locally {
      // Ensure that rounding does not cause change to a normal bit pattern.
      val expected = 1023.toShort
      val f32Uut = 0.000060975552f // largest F16 subnormal

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }

    locally {
      val expected = 64.toShort
      val f32Uut = Math.pow(2, -18).toFloat

      assertEquals(expected, JFloat.floatToFloat16(f32Uut))
    }
  }
}
