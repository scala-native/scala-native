package org.scalanative.testsuite.javalib.lang

import java.lang._

// Three tests ported from Scala.js javalib/lang/FloatTest.scala
// commit: 217f3a3 dated: 2021-02-19
//   isFinite()
//   isInfinite()
//   isNanTest()

// Because this test is the java.lang package, an unqualified Float
// is a java.lang.Float. Prior art used unqualified Float freely,
// with that intent.  Scala.js JFloat is introduced to minimize changes
// in ported Scala.js tests. Existing usages of unqualified Double
// are not changed. Joys of blending code bases.
import java.lang.{Float => JFloat}

import java.lang.Float.{floatToIntBits, floatToRawIntBits, intBitsToFloat}

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import org.scalanative.testsuite.utils.target.is32BitPlatform

class FloatTest {
  @Test def testEquals(): Unit = {
    val pzero = +0.0f
    val nzero = -0.0f
    assertTrue(pzero.equals(pzero))
    assertTrue(nzero.equals(nzero))
    assertFalse(pzero.equals(nzero))
    val szero = 1.0f - 1.0f
    assertTrue(pzero.equals(szero))

    val bpzero: java.lang.Float = pzero
    val bnzero: java.lang.Float = nzero
    assertFalse(bpzero.equals(bnzero))
    val bszero: java.lang.Float = szero
    assertTrue(bpzero.equals(bszero))

    val num1 = 123.45f
    val num2 = 123.45f
    assertTrue(num1.equals(num2))

    val bnum1: java.lang.Float = num1
    val bnum2: java.lang.Float = num2
    assertTrue(bnum1 == bnum2)
    val pmax1 = scala.Float.MaxValue
    val pmax2 = scala.Float.MaxValue
    assertTrue(pmax1.equals(pmax2))
    val pmax3 = scala.Float.MaxValue + 1
    assertTrue(pmax1.equals(pmax3))

    val bpmax1: java.lang.Float = scala.Float.MaxValue
    val bpmax2: java.lang.Float = scala.Float.MaxValue
    assertTrue(bpmax1.equals(bpmax2))
    val bpmax3: java.lang.Float = scala.Float.MaxValue + 1
    assertTrue(bpmax1.equals(bpmax3))

    val pmin1 = scala.Float.MinValue
    val pmin2 = scala.Float.MinValue
    assertTrue(pmin1.equals(pmin2))
    val pmin3 = scala.Float.MinValue + 1
    assertTrue(pmin1.equals(pmin3))

    val bpmin1: java.lang.Float = scala.Float.MinValue
    val bpmin2: java.lang.Float = scala.Float.MinValue
    assertTrue(bpmin1.equals(bpmin2))
    val bpmin3: java.lang.Float = scala.Float.MinValue + 1
    assertTrue(bpmin1.equals(bpmin3))

    val pinf1 = scala.Float.PositiveInfinity
    val pinf2 = scala.Float.MaxValue + scala.Float.MaxValue
    assertTrue(pinf1.equals(pinf2))

    val bpinf1: java.lang.Float = pinf1
    val bpinf2: java.lang.Float = pinf2
    assertTrue(bpinf1.equals(bpinf2))

    val ninf1 = scala.Float.NegativeInfinity
    val ninf2 = scala.Float.MinValue + scala.Float.MinValue
    assertTrue(ninf1.equals(ninf2))

    val bninf1: java.lang.Float = ninf1
    val bninf2: java.lang.Float = ninf2
    assertTrue(bninf1.equals(bninf2))

    assertTrue(Float.NaN.equals(Float.NaN))

    val x = Float.NaN
    val y = intBitsToFloat(floatToRawIntBits(x) | 1)
    assertTrue(x.equals(y))

    val z = intBitsToFloat(floatToIntBits(x) | 1)
    assertTrue(x.equals(z))
  }

  @Test def testEqualEqual(): Unit = {
    val pzero = +0.0f
    val nzero = -0.0f
    assertTrue(pzero == pzero)
    assertTrue(nzero == nzero)
    assertTrue(pzero == nzero)
    val szero = 1.0f - 1.0f
    assertTrue(pzero == szero)

    val bpzero: Any = pzero
    val bnzero: Any = nzero
    assertTrue(bpzero == bnzero)
    val bszero: java.lang.Float = szero
    assertTrue(bpzero == bszero)

    val num1 = 123.45f
    val num2 = 123.45f
    assertTrue(num1 == num2)

    val bnum1: java.lang.Float = num1
    val bnum2: java.lang.Float = num2
    assertTrue(bnum1 == bnum2)

    val pmax1 = scala.Float.MaxValue
    val pmax2 = scala.Float.MaxValue
    assertTrue(pmax1 == pmax2)
    val pmax3 = scala.Float.MaxValue + 1
    assertTrue(pmax1 == pmax3)

    val bpmax1: java.lang.Float = scala.Float.MaxValue
    val bpmax2: java.lang.Float = scala.Float.MaxValue
    assertTrue(bpmax1 == bpmax2)
    val bpmax3: java.lang.Float = scala.Float.MaxValue + 1
    assertTrue(bpmax1 == bpmax3)

    val pmin1 = scala.Float.MinValue
    val pmin2 = scala.Float.MinValue
    assertTrue(pmin1 == pmin2)
    val pmin3 = scala.Float.MinValue + 1
    assertTrue(pmin1 == pmin3)

    val bpmin1: java.lang.Float = scala.Float.MinValue
    val bpmin2: java.lang.Float = scala.Float.MinValue
    assertTrue(bpmin1 == bpmin2)
    val bpmin3: java.lang.Float = scala.Float.MinValue + 1
    assertTrue(bpmin1 == bpmin3)

    val pinf1 = scala.Float.PositiveInfinity
    val pinf2 = scala.Float.MaxValue + scala.Float.MaxValue
    assertTrue(pinf1 == pinf2)

    val bpinf1: java.lang.Float = pinf1
    val bpinf2: java.lang.Float = pinf2
    assertTrue(bpinf1 == bpinf2)

    if (!is32BitPlatform) { // x86 has different float behavior
      val ninf1 = scala.Float.NegativeInfinity
      val ninf2 = scala.Float.MinValue + scala.Float.MinValue
      assertTrue(ninf1 == ninf2)

      val bninf1: java.lang.Float = ninf1
      val bninf2: java.lang.Float = ninf2
      assertTrue(bninf1 == bninf2)
    }

    assertFalse(Float.NaN == Float.NaN)

    val x = Float.NaN
    val y = intBitsToFloat(floatToRawIntBits(x) | 1)
    assertFalse(x == y)

    val z = intBitsToFloat(floatToIntBits(x) | 1)
    assertFalse(x == z)
  }

  @Test def testEq(): Unit = {
    val bpzero: java.lang.Float = +0.0f
    val bnzero: java.lang.Float = -0.0f
    assertTrue(bpzero eq bpzero)
    assertTrue(bnzero eq bnzero)
    assertFalse(bpzero eq bnzero)
    val bszero: java.lang.Float = 1.0f - 1.0f
    assertFalse(bpzero eq bszero)

    val bnum1: java.lang.Float = 123.45f
    val bnum2: java.lang.Float = 123.45f
    assertFalse(bnum1 eq bnum2)

    val bpmax1: java.lang.Float = scala.Float.MaxValue
    val bpmax2: java.lang.Float = scala.Float.MaxValue
    assertFalse(bpmax1 eq bpmax2)
    val bpmax3: java.lang.Float = scala.Float.MaxValue + 1
    assertFalse(bpmax1 eq bpmax3)

    val bpmin1: java.lang.Float = scala.Float.MinValue
    val bpmin2: java.lang.Float = scala.Float.MinValue
    assertFalse(bpmin1 eq bpmin2)
    val bpmin3: java.lang.Float = scala.Float.MinValue + 1
    assertFalse(bpmin1 eq bpmin3)

    val bpinf1: java.lang.Float = scala.Float.PositiveInfinity
    val bpinf2: java.lang.Float = scala.Float.MaxValue + scala.Float.MaxValue
    assertFalse(bpinf1 eq bpinf2)

    val bninf1: java.lang.Float = scala.Float.NegativeInfinity
    val bninf2: java.lang.Float = scala.Float.MinValue + scala.Float.MinValue
    assertFalse(bninf1 eq bninf2)
  }

  @Test def parseFloat(): Unit = {
    assertTrue(Float.parseFloat("1.0") == 1.0f)
    assertTrue(Float.parseFloat("-1.0") == -1.0f)
    assertTrue(Float.parseFloat("0.0") == 0.0f)
    assertTrue(Float.parseFloat("-0.0") == -0.0f)
    assertTrue(Float.parseFloat("Infinity") == Float.POSITIVE_INFINITY)
    assertTrue(Float.parseFloat("-Infinity") == Float.NEGATIVE_INFINITY)
    assertTrue(Float.isNaN(Float.parseFloat("NaN")))

    assertTrue("a8", Float.parseFloat("6.66D") == 6.66f)

    // Java allows trailing whitespace, including tabs & nulls.
    assertTrue("a9", Float.parseFloat("6.66D\t ") == 6.66f)
    assertTrue("a9a", Float.parseFloat("6.66D\u0000") == 6.66f)

    assertTrue("a10", Float.parseFloat("6.66d") == 6.66f)

    assertTrue("a11", Float.parseFloat("7.77F") == 7.77f)
    assertTrue("a12", Float.parseFloat("7.77f") == 7.77f)

    // Does not parse characters beyond IEEE754 spec.
    assertTrue(
      "a13",
      Float.parseFloat("1.7976931348623157999999999") == 1.7976931348623157f
    )

    assertThrows(classOf[NumberFormatException], Float.parseFloat(""))
    assertThrows(classOf[NumberFormatException], Float.parseFloat("F"))
    assertThrows(classOf[NumberFormatException], Float.parseFloat("potato"))
    assertThrows(classOf[NumberFormatException], Float.parseFloat("0.0potato"))
    assertThrows(classOf[NumberFormatException], Float.parseFloat("0.potato"))

    assertThrows(classOf[NumberFormatException], Float.parseFloat("6.66 F"))
    assertThrows(
      classOf[NumberFormatException],
      Float.parseFloat("6.66F  Bad  ")
    )
    assertThrows(
      classOf[NumberFormatException],
      Float.parseFloat("6.66F\u0000a")
    )
    assertThrows(
      classOf[NumberFormatException],
      Float.parseFloat("6.66F \u0100")
    )

    // Out of IEE754 range handling

    //   Too big - java.lang.Float.MAX_VALUE times 10
    assertTrue(
      "a20",
      Float.parseFloat("3.4028235E39") ==
        Float.POSITIVE_INFINITY
    )

    //   Too big - Negative java.lang.Float.MAX_VALUE times 10
    assertTrue(
      "a21",
      Float.parseFloat("-3.4028235E39") ==
        Float.NEGATIVE_INFINITY
    )

    //   Too close to 0 - java.lang.Float.MIN_VALUE divided by 10
    assertTrue("a22", Float.parseFloat("1.4E-46") == 0.0f)

    // Scala Native Issue #1836, a string Too Big reported from the wild.
    val a = "274672389457236457826542634627345697228374687236476867674746" +
      "2342342342342342342342323423423423423423426767456345745293762384756" +
      "2384756345634568456345689345683475863465786485764785684564576348756" +
      "7384567845678658734587364576745683475674576345786348576847567846578" +
      "3456702897830296720476846578634576384567845678346573465786457863"

    assertTrue("a23", Float.parseFloat(a) == Float.POSITIVE_INFINITY)

    // Hexadecimal strings
    assertTrue("a30", Float.parseFloat("0x0p1") == 0.0f)
    assertTrue("a31", Float.parseFloat("0x1p0") == 1.0f)
    assertTrue("a32", Float.parseFloat("0x1p1D") == 2.0f)

    assertTrue("a33", Float.parseFloat("0x1.8eae14p6") == 99.67f)
    assertTrue("a34", Float.parseFloat("-0x1.8eae14p6") == -99.67f)
  }

  // scala.Float passes -0.0F without change. j.l.Double forced to +0.0.
  private def assertF2sEquals(expected: String, f: scala.Float): Unit = {
    val result = f.toString
    assertTrue(s"result: $result != expected: $expected", expected == result)
  }

  @Test def testToString(): Unit = {

    // Test non-finite values.
    assertF2sEquals("Infinity", Float.POSITIVE_INFINITY)
    assertF2sEquals("-Infinity", Float.NEGATIVE_INFINITY)
    assertF2sEquals("NaN", Float.NaN)

    // Test simple values around zero.
    assertF2sEquals("0.0", 0.0f)
    assertF2sEquals("-0.0", -0.0f)
    assertF2sEquals("1.0", 1.0f)
    assertF2sEquals("-1.0", -1.0f)
    assertF2sEquals("2.0", 2.0f)
    assertF2sEquals("-2.0", -2.0f)

    // Test maximum & minima.
    assertF2sEquals("3.4028235E38", scala.Float.MaxValue)
    assertF2sEquals("-3.4028235E38", scala.Float.MinValue)
    assertF2sEquals("1.4E-45", scala.Float.MinPositiveValue)

    // Test correctness least significant digits  & number of digits after the
    // decimal point of values with 'infinite' number of fraction digits.
    assertF2sEquals("3.1415927", (math.Pi * 1.0e+0).toFloat)
    assertF2sEquals("31.415926", (math.Pi * 1.0e+1).toFloat)
    assertF2sEquals("0.31415927", (math.Pi * 1.0e-1).toFloat)

    // Test transitions to scientific notation.
    assertF2sEquals("3141592.8", (math.Pi * 1.0e+6).toFloat)
    assertF2sEquals("3.1415926E7", (math.Pi * 1.0e+7).toFloat)
    assertF2sEquals("0.0031415927", (math.Pi * 1.0e-3).toFloat)
    assertF2sEquals("3.1415926E-4", (math.Pi * 1.0e-4).toFloat)
  }

  @Test def isFinite(): Unit = {
    assertFalse(JFloat.isFinite(scala.Float.PositiveInfinity))
    assertFalse(JFloat.isFinite(scala.Float.NegativeInfinity))
    assertFalse(JFloat.isFinite(scala.Float.NaN))
    assertFalse(JFloat.isFinite(1f / 0))
    assertFalse(JFloat.isFinite(-1f / 0))

    assertTrue(JFloat.isFinite(0f))
    assertTrue(JFloat.isFinite(1f))
    assertTrue(JFloat.isFinite(123456f))
    assertTrue(JFloat.isFinite(scala.Float.MinValue))
    assertTrue(JFloat.isFinite(scala.Float.MaxValue))
    assertTrue(JFloat.isFinite(scala.Float.MinPositiveValue))
  }

  @Test def isInfinite_Issue515(): Unit = {
    assertTrue(scala.Float.PositiveInfinity.isInfinite)
    assertTrue(scala.Float.NegativeInfinity.isInfinite)
    assertTrue((1f / 0).isInfinite)
    assertTrue((-1f / 0).isInfinite)
    assertFalse(0f.isInfinite)
  }

  @Test def isNaNTest(): Unit = {
    def f(v: Float): Boolean = {
      var v2 = v // do not inline
      v2.isNaN
    }

    assertTrue(f(Float.NaN))

    assertFalse(f(scala.Float.PositiveInfinity))
    assertFalse(f(scala.Float.NegativeInfinity))
    assertFalse(f(1f / 0))
    assertFalse(f(-1f / 0))
    assertFalse(f(0f))
    assertFalse(f(3f))
    assertFalse(f(-1.5f))
  }

  @Test def negateTest(): Unit = {
    val delta = 0.000000001f
    val one = 1.0f
    assertEquals("negate one", -1.0f, -one, delta)
    assertEquals("negate minus one", 1.0f, -(-one), delta)

    val x = 0.0f
    assertTrue("Negated value is equal to zero", 0.0f == -0.0f)
    assertFalse("Can distinguish negated zero", 0.0f.equals(-0.0f))
    assertTrue("negate zero", -0.0f.equals(-x))
    assertTrue("negate minus zero", 0.0f.equals(-(-x)))

    assertEquals(
      "negate minValue",
      scala.Float.MaxValue,
      -scala.Float.MinValue,
      delta
    )
    assertEquals(
      "negate maxValue",
      scala.Float.MinValue,
      -scala.Float.MaxValue,
      delta
    )

    assertEquals(
      "negate infinity",
      scala.Float.NegativeInfinity,
      -scala.Float.PositiveInfinity,
      delta
    )
    assertEquals(
      "negate neg inf",
      scala.Float.PositiveInfinity,
      -scala.Float.NegativeInfinity,
      delta
    )
  }
}
