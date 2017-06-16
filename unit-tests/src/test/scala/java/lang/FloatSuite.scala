package java.lang

import java.lang.Float.{floatToIntBits, floatToRawIntBits, intBitsToFloat}

object FloatSuite extends tests.Suite {
  test("equals") {
    val pzero = +0.0f
    val nzero = -0.0f
    assert(pzero equals pzero)
    assert(nzero equals nzero)
    assertNot(pzero equals nzero)
    val szero = 1.0f - 1.0f
    assert(pzero equals szero)

    val bpzero: java.lang.Float = pzero
    val bnzero: java.lang.Float = nzero
    assertNot(bpzero equals bnzero)
    val bszero: java.lang.Float = szero
    assert(bpzero equals bszero)

    val num1 = 123.45f
    val num2 = 123.45f
    assert(num1 equals num2)

    val bnum1: java.lang.Float = num1
    val bnum2: java.lang.Float = num2
    assert(bnum1 == bnum2)
    val pmax1 = scala.Float.MaxValue
    val pmax2 = scala.Float.MaxValue
    assert(pmax1 equals pmax2)
    val pmax3 = scala.Float.MaxValue + 1
    assert(pmax1 equals pmax3)

    val bpmax1: java.lang.Float = scala.Float.MaxValue
    val bpmax2: java.lang.Float = scala.Float.MaxValue
    assert(bpmax1 equals bpmax2)
    val bpmax3: java.lang.Float = scala.Float.MaxValue + 1
    assert(bpmax1 equals bpmax3)

    val pmin1 = scala.Float.MinValue
    val pmin2 = scala.Float.MinValue
    assert(pmin1 equals pmin2)
    val pmin3 = scala.Float.MinValue + 1
    assert(pmin1 equals pmin3)

    val bpmin1: java.lang.Float = scala.Float.MinValue
    val bpmin2: java.lang.Float = scala.Float.MinValue
    assert(bpmin1 equals bpmin2)
    val bpmin3: java.lang.Float = scala.Float.MinValue + 1
    assert(bpmin1 equals bpmin3)

    val pinf1 = scala.Float.PositiveInfinity
    val pinf2 = scala.Float.MaxValue + scala.Float.MaxValue
    assert(pinf1 equals pinf2)

    val bpinf1: java.lang.Float = pinf1
    val bpinf2: java.lang.Float = pinf2
    assert(bpinf1 equals bpinf2)

    val ninf1 = scala.Float.NegativeInfinity
    val ninf2 = scala.Float.MinValue + scala.Float.MinValue
    assert(ninf1 equals ninf2)

    val bninf1: java.lang.Float = ninf1
    val bninf2: java.lang.Float = ninf2
    assert(bninf1 equals bninf2)

    assert(Float.NaN equals Float.NaN)

    val x = Float.NaN
    val y = intBitsToFloat(floatToRawIntBits(x) | 1)
    assert(x equals y)

    val z = intBitsToFloat(floatToIntBits(x) | 1)
    assert(x equals z)
  }

  test("==") {
    val pzero = +0.0f
    val nzero = -0.0f
    assert(pzero == pzero)
    assert(nzero == nzero)
    assert(pzero == nzero)
    val szero = 1.0f - 1.0f
    assert(pzero == szero)

    val bpzero: Any = pzero
    val bnzero: Any = nzero
    assert(bpzero == bnzero)
    val bszero: java.lang.Float = szero
    assert(bpzero == bszero)

    val num1 = 123.45f
    val num2 = 123.45f
    assert(num1 == num2)

    val bnum1: java.lang.Float = num1
    val bnum2: java.lang.Float = num2
    assert(bnum1 == bnum2)

    val pmax1 = scala.Float.MaxValue
    val pmax2 = scala.Float.MaxValue
    assert(pmax1 == pmax2)
    val pmax3 = scala.Float.MaxValue + 1
    assert(pmax1 == pmax3)

    val bpmax1: java.lang.Float = scala.Float.MaxValue
    val bpmax2: java.lang.Float = scala.Float.MaxValue
    assert(bpmax1 == bpmax2)
    val bpmax3: java.lang.Float = scala.Float.MaxValue + 1
    assert(bpmax1 == bpmax3)

    val pmin1 = scala.Float.MinValue
    val pmin2 = scala.Float.MinValue
    assert(pmin1 == pmin2)
    val pmin3 = scala.Float.MinValue + 1
    assert(pmin1 == pmin3)

    val bpmin1: java.lang.Float = scala.Float.MinValue
    val bpmin2: java.lang.Float = scala.Float.MinValue
    assert(bpmin1 == bpmin2)
    val bpmin3: java.lang.Float = scala.Float.MinValue + 1
    assert(bpmin1 == bpmin3)

    val pinf1 = scala.Float.PositiveInfinity
    val pinf2 = scala.Float.MaxValue + scala.Float.MaxValue
    assert(pinf1 == pinf2)

    val bpinf1: java.lang.Float = pinf1
    val bpinf2: java.lang.Float = pinf2
    assert(bpinf1 == bpinf2)

    val ninf1 = scala.Float.NegativeInfinity
    val ninf2 = scala.Float.MinValue + scala.Float.MinValue
    assert(ninf1 == ninf2)

    val bninf1: java.lang.Float = ninf1
    val bninf2: java.lang.Float = ninf2
    assert(bninf1 == bninf2)

    assertNot(Float.NaN == Float.NaN)

    val x = Float.NaN
    val y = intBitsToFloat(floatToRawIntBits(x) | 1)
    assertNot(x == y)

    val z = intBitsToFloat(floatToIntBits(x) | 1)
    assertNot(x == z)
  }

  test("eq") {
    val bpzero: java.lang.Float = +0.0f
    val bnzero: java.lang.Float = -0.0f
    assert(bpzero eq bpzero)
    assert(bnzero eq bnzero)
    assertNot(bpzero eq bnzero)
    val bszero: java.lang.Float = 1.0f - 1.0f
    assertNot(bpzero eq bszero)

    val bnum1: java.lang.Float = 123.45f
    val bnum2: java.lang.Float = 123.45f
    assertNot(bnum1 eq bnum2)

    val bpmax1: java.lang.Float = scala.Float.MaxValue
    val bpmax2: java.lang.Float = scala.Float.MaxValue
    assertNot(bpmax1 eq bpmax2)
    val bpmax3: java.lang.Float = scala.Float.MaxValue + 1
    assertNot(bpmax1 eq bpmax3)

    val bpmin1: java.lang.Float = scala.Float.MinValue
    val bpmin2: java.lang.Float = scala.Float.MinValue
    assertNot(bpmin1 eq bpmin2)
    val bpmin3: java.lang.Float = scala.Float.MinValue + 1
    assertNot(bpmin1 eq bpmin3)

    val bpinf1: java.lang.Float = scala.Float.PositiveInfinity
    val bpinf2: java.lang.Float = scala.Float.MaxValue + scala.Float.MaxValue
    assertNot(bpinf1 eq bpinf2)

    val bninf1: java.lang.Float = scala.Float.NegativeInfinity
    val bninf2: java.lang.Float = scala.Float.MinValue + scala.Float.MinValue
    assertNot(bninf1 eq bninf2)
  }

  test("parseFloat") {
    assert(Float.parseFloat("1.0") == 1.0f)
    assert(Float.parseFloat("-1.0") == -1.0f)
    assert(Float.parseFloat("0.0") == 0.0f)
    assert(Float.parseFloat("-0.0") == -0.0f)
    assert(Float.parseFloat("Infinity") == Float.POSITIVE_INFINITY)
    assert(Float.parseFloat("-Infinity") == Float.NEGATIVE_INFINITY)
    assert(Float.isNaN(Float.parseFloat("NaN")))
  }

  test("toString") {
    assert(Float.toString(1.0f).equals("1.000000"))
    assert(Float.toString(-1.0f).equals("-1.000000"))
    assert(Float.toString(0.0f).equals("0.000000"))
    assert(Float.toString(-0.0f).equals("-0.000000"))
    assert(Float.toString(Float.POSITIVE_INFINITY).equals("Infinity"))
    assert(Float.toString(Float.NEGATIVE_INFINITY).equals("-Infinity"))
    assert(Float.toString(Float.NaN).equals("NaN"))
  }
}
