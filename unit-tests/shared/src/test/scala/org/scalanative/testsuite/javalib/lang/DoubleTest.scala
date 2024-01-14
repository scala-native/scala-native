package org.scalanative.testsuite.javalib.lang

import java.lang._

// Three tests ported from Scala.js javalib/lang/DoubleTest.scala
// commit: 0f25c8c dated: 2021-02-17
//   isFinite()
//   isInfinite()
//   isNanTest()

// Because this test is the java.lang package, an unqualified Double
// is a java.lang.Double. Prior art used unqualified Double freely,
// with that intent. Scala.js JDouble is introduced to minimize changes
// in ported Scala.js tests. Existing usages of unqualified Double
// are not changed. Joys of blending code bases.
import java.lang.{Double => JDouble}

import java.lang.Double.{
  doubleToLongBits,
  doubleToRawLongBits,
  longBitsToDouble,
  toHexString
}

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import org.scalanative.testsuite.utils.target.is32BitPlatform

class DoubleTest {
  @Test def testEquals(): Unit = {
    val pzero = +0.0
    val nzero = -0.0
    assertTrue(pzero.equals(pzero))
    assertTrue(nzero.equals(nzero))
    assertFalse(pzero.equals(nzero))
    val szero = 1.0 - 1.0
    assertTrue(pzero.equals(szero))

    val bpzero: java.lang.Double = pzero
    val bnzero: java.lang.Double = nzero
    assertFalse(bpzero.equals(bnzero))
    val bszero: java.lang.Double = szero
    assertTrue(bpzero.equals(bszero))

    val num1 = 123.45
    val num2 = 123.45
    assertTrue(num1.equals(num2))

    val bnum1: java.lang.Double = num1
    val bnum2: java.lang.Double = num2
    assertTrue(bnum1 == bnum2)
    val pmax1 = scala.Double.MaxValue
    val pmax2 = scala.Double.MaxValue
    assertTrue(pmax1.equals(pmax2))
    val pmax3 = scala.Double.MaxValue + 1
    assertTrue(pmax1.equals(pmax3))

    val bpmax1: java.lang.Double = scala.Double.MaxValue
    val bpmax2: java.lang.Double = scala.Double.MaxValue
    assertTrue(bpmax1.equals(bpmax2))
    val bpmax3: java.lang.Double = scala.Double.MaxValue + 1
    assertTrue(bpmax1.equals(bpmax3))

    val pmin1 = scala.Double.MinValue
    val pmin2 = scala.Double.MinValue
    assertTrue(pmin1.equals(pmin2))
    val pmin3 = scala.Double.MinValue + 1
    assertTrue(pmin1.equals(pmin3))

    val bpmin1: java.lang.Double = scala.Double.MinValue
    val bpmin2: java.lang.Double = scala.Double.MinValue
    assertTrue(bpmin1.equals(bpmin2))
    val bpmin3: java.lang.Double = scala.Double.MinValue + 1
    assertTrue(bpmin1.equals(bpmin3))

    val pinf1 = scala.Double.PositiveInfinity
    val pinf2 = scala.Double.MaxValue + scala.Double.MaxValue
    assertTrue(pinf1.equals(pinf2))

    val bpinf1: java.lang.Double = pinf1
    val bpinf2: java.lang.Double = pinf2
    assertTrue(bpinf1.equals(bpinf2))

    val ninf1 = scala.Double.NegativeInfinity
    val ninf2 = scala.Double.MinValue + scala.Double.MinValue
    assertTrue(ninf1.equals(ninf2))

    val bninf1: java.lang.Double = ninf1
    val bninf2: java.lang.Double = ninf2
    assertTrue(bninf1.equals(bninf2))

    assertTrue(Double.NaN.equals(Double.NaN))

    val x = Double.NaN
    val y = longBitsToDouble(doubleToRawLongBits(x) | 1)
    assertTrue(x.equals(y))

    val z = longBitsToDouble(doubleToLongBits(x) | 1)
    assertTrue(x.equals(z))
  }

  @Test def testEqualEqual(): Unit = {
    val pzero = +0.0
    val nzero = -0.0
    assertTrue(pzero == pzero)
    assertTrue(nzero == nzero)
    assertTrue(pzero == nzero)
    val szero = 1.0 - 1.0
    assertTrue(pzero == szero)

    val bpzero: Any = pzero
    val bnzero: Any = nzero
    assertTrue(bpzero == bnzero)
    val bszero: java.lang.Double = szero
    assertTrue(bpzero == bszero)

    val num1 = 123.45
    val num2 = 123.45
    assertTrue(num1 == num2)

    val bnum1: java.lang.Double = num1
    val bnum2: java.lang.Double = num2
    assertTrue(bnum1 == bnum2)

    val pmax1 = scala.Double.MaxValue
    val pmax2 = scala.Double.MaxValue
    assertTrue(pmax1 == pmax2)
    val pmax3 = scala.Double.MaxValue + 1
    assertTrue(pmax1 == pmax3)

    val bpmax1: java.lang.Double = scala.Double.MaxValue
    val bpmax2: java.lang.Double = scala.Double.MaxValue
    assertTrue(bpmax1 == bpmax2)
    val bpmax3: java.lang.Double = scala.Double.MaxValue + 1
    assertTrue(bpmax1 == bpmax3)

    val pmin1 = scala.Double.MinValue
    val pmin2 = scala.Double.MinValue
    assertTrue(pmin1 == pmin2)
    val pmin3 = scala.Double.MinValue + 1
    assertTrue(pmin1 == pmin3)

    val bpmin1: java.lang.Double = scala.Double.MinValue
    val bpmin2: java.lang.Double = scala.Double.MinValue
    assertTrue(bpmin1 == bpmin2)
    val bpmin3: java.lang.Double = scala.Double.MinValue + 1
    assertTrue(bpmin1 == bpmin3)

    val pinf1 = scala.Double.PositiveInfinity
    val pinf2 = scala.Double.MaxValue + scala.Double.MaxValue
    assertTrue(pinf1 == pinf2)

    val bpinf1: java.lang.Double = pinf1
    val bpinf2: java.lang.Double = pinf2
    assertTrue(bpinf1 == bpinf2)

    if (!is32BitPlatform) { // x86 has different float behavior
      val ninf1 = scala.Double.NegativeInfinity
      val ninf2 = scala.Double.MinValue + scala.Double.MinValue
      assertTrue(ninf1 == ninf2)

      val bninf1: java.lang.Double = ninf1
      val bninf2: java.lang.Double = ninf2
      assertTrue(bninf1 == bninf2)
    }

    assertFalse(Double.NaN == Double.NaN)

    val x = Double.NaN
    val y = longBitsToDouble(doubleToRawLongBits(x) | 1)
    assertFalse(x == y)

    val z = longBitsToDouble(doubleToLongBits(x) | 1)
    assertFalse(x == z)
  }

  @Test def testEq(): Unit = {
    val bpzero: java.lang.Double = +0.0
    val bnzero: java.lang.Double = -0.0
    assertTrue(bpzero eq bpzero)
    assertTrue(bnzero eq bnzero)
    assertFalse(bpzero eq bnzero)
    val bszero: java.lang.Double = 1.0 - 1.0
    assertFalse(bpzero eq bszero)

    val bnum1: java.lang.Double = 123.45
    val bnum2: java.lang.Double = 123.45
    assertFalse(bnum1 eq bnum2)

    val bpmax1: java.lang.Double = scala.Double.MaxValue
    val bpmax2: java.lang.Double = scala.Double.MaxValue
    assertFalse(bpmax1 eq bpmax2)
    val bpmax3: java.lang.Double = scala.Double.MaxValue + 1
    assertFalse(bpmax1 eq bpmax3)

    val bpmin1: java.lang.Double = scala.Double.MinValue
    val bpmin2: java.lang.Double = scala.Double.MinValue
    assertFalse(bpmin1 eq bpmin2)
    val bpmin3: java.lang.Double = scala.Double.MinValue + 1
    assertFalse(bpmin1 eq bpmin3)

    val bpinf1: java.lang.Double = scala.Double.PositiveInfinity
    val bpinf2: java.lang.Double = scala.Double.MaxValue + scala.Double.MaxValue
    assertFalse(bpinf1 eq bpinf2)

    val bninf1: java.lang.Double = scala.Double.NegativeInfinity
    val bninf2: java.lang.Double = scala.Double.MinValue + scala.Double.MinValue
    assertFalse(bninf1 eq bninf2)
  }

  @Test def parseDouble(): Unit = {
    assertTrue(Double.parseDouble("1.0") == 1.0)
    assertTrue(Double.parseDouble("-1.0") == -1.0)
    assertTrue(Double.parseDouble("0.0") == 0.0)
    assertTrue(Double.parseDouble("-0.0") == -0.0)
    assertTrue(Double.parseDouble("Infinity") == Double.POSITIVE_INFINITY)
    assertTrue(Double.parseDouble("-Infinity") == Double.NEGATIVE_INFINITY)
    assertTrue(Double.isNaN(Double.parseDouble("NaN")))

    assertTrue("a8", Double.parseDouble("6.66D") == 6.66)

    // Java allows trailing whitespace, including tabs & nulls.
    assertTrue("a9", Double.parseDouble("6.66D\t ") == 6.66)
    assertTrue("a9a", Double.parseDouble("6.66D\u0000") == 6.66)

    assertTrue("a10", Double.parseDouble("6.66d") == 6.66)

    assertTrue("a11", Double.parseDouble("7.77F") == 7.77)
    assertTrue("a12", Double.parseDouble("7.77f") == 7.77)

    // Does not parse characters beyond IEEE754 spec.
    assertTrue(
      "a13",
      Double.parseDouble("1.7976931348623157999999999")
        == 1.7976931348623157
    )

    assertThrows(classOf[NumberFormatException], Double.parseDouble(""))
    assertThrows(classOf[NumberFormatException], Double.parseDouble("D"))
    assertThrows(classOf[NumberFormatException], Double.parseDouble("potato"))
    assertThrows(
      classOf[NumberFormatException],
      Double.parseDouble("0.0potato")
    )
    assertThrows(classOf[NumberFormatException], Double.parseDouble("0.potato"))

    assertThrows(classOf[NumberFormatException], Double.parseDouble("6.66 D"))
    assertThrows(
      classOf[NumberFormatException],
      Double.parseDouble("6.66D  Bad  ")
    )
    assertThrows(
      classOf[NumberFormatException],
      Double.parseDouble("6.66D\u0000a")
    )
    assertThrows(
      classOf[NumberFormatException],
      Double.parseDouble("6.66D \u0100")
    )

    // Out of IEE754 range handling

    //   Too big - java.lang.Double.MAX_VALUE times 10
    assertTrue(
      "a20",
      Double.parseDouble("1.7976931348623157E309") ==
        Double.POSITIVE_INFINITY
    )

    //   Too big - Negative java.lang.Double.MAX_VALUE times 10
    assertTrue(
      "a21",
      Double.parseDouble("-1.7976931348623157E309") ==
        Double.NEGATIVE_INFINITY
    )

    //   Too close to 0 - java.lang.Double.MIN_VALUE divided by 10
    assertTrue("a22", Double.parseDouble("4.9E-325") == 0.0)

    // Scala Native Issue #1836, a string Too Big reported from the wild.
    val a = "-274672389457236457826542634627345697228374687236476867674746" +
      "2342342342342342342342323423423423423423426767456345745293762384756" +
      "2384756345634568456345689345683475863465786485764785684564576348756" +
      "7384567845678658734587364576745683475674576345786348576847567846578" +
      "3456702897830296720476846578634576384567845678346573465786457863"

    assertTrue("a23", Double.parseDouble(a) == Double.NEGATIVE_INFINITY)

    // Hexadecimal strings
    assertTrue("a30", Double.parseDouble("0x0p1") == 0.0f)
    assertTrue("a31", Double.parseDouble("0x1p0") == 1.0f)

    assertTrue("a32", Double.parseDouble("0x1p1F") == 2.0f)

    assertTrue("a33", Double.parseDouble("0x1.8eae14p6") == 99.67f)
    assertTrue("a34", Double.parseDouble("-0x1.8eae14p6") == -99.67f)
  }

  // scala.Double passes -0.0d without change. j.l.Double gets forced to +0.0.
  private def assertD2sEquals(expected: String, f: scala.Double): Unit = {
    val result = f.toString
    assertTrue(s"result: $result != expected: $expected", expected == result)
  }

  @Test def testToString(): Unit = {

    // Test non-finite values.
    assertD2sEquals("Infinity", Double.POSITIVE_INFINITY)
    assertD2sEquals("-Infinity", Double.NEGATIVE_INFINITY)
    assertD2sEquals("NaN", Double.NaN)

    // Test simple values around zero.
    assertD2sEquals("0.0", 0.0d)
    assertD2sEquals("-0.0", -0.0d)
    assertD2sEquals("1.0", 1.0d)
    assertD2sEquals("-1.0", -1.0d)
    assertD2sEquals("2.0", 2.0d)
    assertD2sEquals("-2.0", -2.0d)

    // Test maximum & minima.
    assertD2sEquals("1.7976931348623157E308", scala.Double.MaxValue)
    assertD2sEquals("-1.7976931348623157E308", scala.Double.MinValue)
    assertD2sEquals("4.9E-324", scala.Double.MinPositiveValue)

    // Test correctness least significant digits  & number of digits after the
    // decimal point of values with 'infinite' number of fraction digits.
    assertD2sEquals("3.141592653589793", (math.Pi * 1.0e+0))
    assertD2sEquals("31.41592653589793", (math.Pi * 1.0e+1))
    assertD2sEquals("0.3141592653589793", (math.Pi * 1.0e-1))

    // Test transitions to scientific notation.
    assertD2sEquals("3141592.653589793", (math.Pi * 1.0e+6))
    assertD2sEquals("3.1415926535897933E7", (math.Pi * 1.0e+7))
    assertD2sEquals("0.0031415926535897933", (math.Pi * 1.0e-3))
    assertD2sEquals("3.141592653589793E-4", (math.Pi * 1.0e-4))
  }

  @Test def toHexStringMinValueIssue1341(): Unit = {
    assertTrue(
      toHexString(java.lang.Double.MIN_VALUE).equals("0x0.0000000000001p-1022")
    )
  }

  @Test def toHexStringAssortedOtherValues(): Unit = {

    assertTrue(
      toHexString(java.lang.Double.MAX_VALUE).equals("0x1.fffffffffffffp1023")
    )

    // A value > 1.0 requiring lots of, but not all,  zeros.
    assertTrue(toHexString(1.00000000000003).equals("0x1.0000000000087p0"))

    // An arbitrary but negative value.
    assertTrue(java.lang.Double.toHexString(-31.0).equals("-0x1.fp4"))
  }

  @Test def isFinite(): Unit = {
    assertFalse(JDouble.isFinite(scala.Double.PositiveInfinity))
    assertFalse(JDouble.isFinite(scala.Double.NegativeInfinity))
    assertFalse(JDouble.isFinite(scala.Double.NaN))
    assertFalse(JDouble.isFinite(1d / 0))
    assertFalse(JDouble.isFinite(-1d / 0))

    assertTrue(JDouble.isFinite(0d))
    assertTrue(JDouble.isFinite(1d))
    assertTrue(JDouble.isFinite(123456d))
    assertTrue(JDouble.isFinite(scala.Double.MinValue))
    assertTrue(JDouble.isFinite(scala.Double.MaxValue))
    assertTrue(JDouble.isFinite(scala.Double.MinPositiveValue))
  }

  // Scala.js Issue 515
  @Test def isInfinite(): Unit = {
    assertTrue(scala.Double.PositiveInfinity.isInfinite)
    assertTrue(scala.Double.NegativeInfinity.isInfinite)
    assertTrue((1.0 / 0).isInfinite)
    assertTrue((-1.0 / 0).isInfinite)
    assertFalse((0.0).isInfinite)
  }

  @Test def isNaNTest(): Unit = {
    def f(v: Double): Boolean = {
      var v2 = v // do not inline
      v2.isNaN
    }

    assertTrue(f(Double.NaN))

    assertFalse(f(scala.Double.PositiveInfinity))
    assertFalse(f(scala.Double.NegativeInfinity))
    assertFalse(f(1.0 / 0))
    assertFalse(f(-1.0 / 0))
    assertFalse(f(0.0))
    assertFalse(f(3.0))
    assertFalse(f(-1.5))
  }

  @Test def negateTest(): Unit = {
    val delta = 0.000000001
    val one = 1.0
    assertEquals("negate one", -1.0, -one, delta)
    assertEquals("negate minus one", 1.0, -(-one), delta)

    val x = 0.0
    assertTrue("Negated value is equal to zero", 0.0 == -0.0)
    assertFalse("Can distinguish negated zero", 0.0.equals(-0.0))
    assertTrue("negate zero", -0.0.equals(-x))
    assertTrue("negate minus zero", 0.0.equals(-(-x)))

    assertEquals(
      "negate minValue",
      scala.Double.MaxValue,
      -scala.Double.MinValue,
      delta
    )
    assertEquals(
      "negate maxValue",
      scala.Double.MinValue,
      -scala.Double.MaxValue,
      delta
    )

    assertEquals(
      "negate infinity",
      scala.Double.NegativeInfinity,
      -scala.Double.PositiveInfinity,
      delta
    )
    assertEquals(
      "negate neg inf",
      scala.Double.PositiveInfinity,
      -scala.Double.NegativeInfinity,
      delta
    )
  }
}
