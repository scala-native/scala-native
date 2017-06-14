package java.lang

import java.lang.Double.{
  doubleToLongBits,
  doubleToRawLongBits,
  longBitsToDouble
}

object DoubleSuite extends tests.Suite {
  test("equals") {
    val pzero = +0.0
    val nzero = -0.0
    assert(pzero equals pzero)
    assert(nzero equals nzero)
    assertNot(pzero equals nzero)
    val szero = 1.0 - 1.0
    assert(pzero equals szero)

    val bpzero: java.lang.Double = pzero
    val bnzero: java.lang.Double = nzero
    assertNot(bpzero equals bnzero)
    val bszero: java.lang.Double = szero
    assert(bpzero equals bszero)

    val num1 = 123.45
    val num2 = 123.45
    assert(num1 equals num2)

    val bnum1: java.lang.Double = num1
    val bnum2: java.lang.Double = num2
    assert(bnum1 == bnum2)
    val pmax1 = scala.Double.MaxValue
    val pmax2 = scala.Double.MaxValue
    assert(pmax1 equals pmax2)
    val pmax3 = scala.Double.MaxValue + 1
    assert(pmax1 equals pmax3)

    val bpmax1: java.lang.Double = scala.Double.MaxValue
    val bpmax2: java.lang.Double = scala.Double.MaxValue
    assert(bpmax1 equals bpmax2)
    val bpmax3: java.lang.Double = scala.Double.MaxValue + 1
    assert(bpmax1 equals bpmax3)

    val pmin1 = scala.Double.MinValue
    val pmin2 = scala.Double.MinValue
    assert(pmin1 equals pmin2)
    val pmin3 = scala.Double.MinValue + 1
    assert(pmin1 equals pmin3)

    val bpmin1: java.lang.Double = scala.Double.MinValue
    val bpmin2: java.lang.Double = scala.Double.MinValue
    assert(bpmin1 equals bpmin2)
    val bpmin3: java.lang.Double = scala.Double.MinValue + 1
    assert(bpmin1 equals bpmin3)

    val pinf1 = scala.Double.PositiveInfinity
    val pinf2 = scala.Double.MaxValue + scala.Double.MaxValue
    assert(pinf1 equals pinf2)

    val bpinf1: java.lang.Double = pinf1
    val bpinf2: java.lang.Double = pinf2
    assert(bpinf1 equals bpinf2)

    val ninf1 = scala.Double.NegativeInfinity
    val ninf2 = scala.Double.MinValue + scala.Double.MinValue
    assert(ninf1 equals ninf2)

    val bninf1: java.lang.Double = ninf1
    val bninf2: java.lang.Double = ninf2
    assert(bninf1 equals bninf2)

    assert(Double.NaN equals Double.NaN)

    val x = Double.NaN
    val y = longBitsToDouble(doubleToRawLongBits(x) | 1)
    assert(x equals y)

    val z = longBitsToDouble(doubleToLongBits(x) | 1)
    assert(x equals z)
  }

  test("==") {
    val pzero = +0.0
    val nzero = -0.0
    assert(pzero == pzero)
    assert(nzero == nzero)
    assert(pzero == nzero)
    val szero = 1.0 - 1.0
    assert(pzero == szero)

    val bpzero: Any = pzero
    val bnzero: Any = nzero
    assert(bpzero == bnzero)
    val bszero: java.lang.Double = szero
    assert(bpzero == bszero)

    val num1 = 123.45
    val num2 = 123.45
    assert(num1 == num2)

    val bnum1: java.lang.Double = num1
    val bnum2: java.lang.Double = num2
    assert(bnum1 == bnum2)

    val pmax1 = scala.Double.MaxValue
    val pmax2 = scala.Double.MaxValue
    assert(pmax1 == pmax2)
    val pmax3 = scala.Double.MaxValue + 1
    assert(pmax1 == pmax3)

    val bpmax1: java.lang.Double = scala.Double.MaxValue
    val bpmax2: java.lang.Double = scala.Double.MaxValue
    assert(bpmax1 == bpmax2)
    val bpmax3: java.lang.Double = scala.Double.MaxValue + 1
    assert(bpmax1 == bpmax3)

    val pmin1 = scala.Double.MinValue
    val pmin2 = scala.Double.MinValue
    assert(pmin1 == pmin2)
    val pmin3 = scala.Double.MinValue + 1
    assert(pmin1 == pmin3)

    val bpmin1: java.lang.Double = scala.Double.MinValue
    val bpmin2: java.lang.Double = scala.Double.MinValue
    assert(bpmin1 == bpmin2)
    val bpmin3: java.lang.Double = scala.Double.MinValue + 1
    assert(bpmin1 == bpmin3)

    val pinf1 = scala.Double.PositiveInfinity
    val pinf2 = scala.Double.MaxValue + scala.Double.MaxValue
    assert(pinf1 == pinf2)

    val bpinf1: java.lang.Double = pinf1
    val bpinf2: java.lang.Double = pinf2
    assert(bpinf1 == bpinf2)

    val ninf1 = scala.Double.NegativeInfinity
    val ninf2 = scala.Double.MinValue + scala.Double.MinValue
    assert(ninf1 == ninf2)

    val bninf1: java.lang.Double = ninf1
    val bninf2: java.lang.Double = ninf2
    assert(bninf1 == bninf2)

    assertNot(Double.NaN == Double.NaN)

    val x = Double.NaN
    val y = longBitsToDouble(doubleToRawLongBits(x) | 1)
    assertNot(x == y)

    val z = longBitsToDouble(doubleToLongBits(x) | 1)
    assertNot(x == z)
  }

  test("eq") {
    val bpzero: java.lang.Double = +0.0
    val bnzero: java.lang.Double = -0.0
    assert(bpzero eq bpzero)
    assert(bnzero eq bnzero)
    assertNot(bpzero eq bnzero)
    val bszero: java.lang.Double = 1.0 - 1.0
    assertNot(bpzero eq bszero)

    val bnum1: java.lang.Double = 123.45
    val bnum2: java.lang.Double = 123.45
    assertNot(bnum1 eq bnum2)

    val bpmax1: java.lang.Double = scala.Double.MaxValue
    val bpmax2: java.lang.Double = scala.Double.MaxValue
    assertNot(bpmax1 eq bpmax2)
    val bpmax3: java.lang.Double = scala.Double.MaxValue + 1
    assertNot(bpmax1 eq bpmax3)

    val bpmin1: java.lang.Double = scala.Double.MinValue
    val bpmin2: java.lang.Double = scala.Double.MinValue
    assertNot(bpmin1 eq bpmin2)
    val bpmin3: java.lang.Double = scala.Double.MinValue + 1
    assertNot(bpmin1 eq bpmin3)

    val bpinf1: java.lang.Double = scala.Double.PositiveInfinity
    val bpinf2
      : java.lang.Double = scala.Double.MaxValue + scala.Double.MaxValue
    assertNot(bpinf1 eq bpinf2)

    val bninf1: java.lang.Double = scala.Double.NegativeInfinity
    val bninf2
      : java.lang.Double = scala.Double.MinValue + scala.Double.MinValue
    assertNot(bninf1 eq bninf2)
  }

  test("parseDouble") {
    assert(Double.parseDouble("1.0") == 1.0)
    assert(Double.parseDouble("-1.0") == -1.0)
    assert(Double.parseDouble("0.0") == 0.0)
    assert(Double.parseDouble("-0.0") == -0.0)
    assert(Double.parseDouble("Infinity") == Double.POSITIVE_INFINITY)
    assert(Double.parseDouble("-Infinity") == Double.NEGATIVE_INFINITY)
    assert(Double.isNaN(Double.parseDouble("NaN")))
  }

  // Not fully JVM compliant yet
  test("toString") {
    assert(Double.toString(1.0).equals("1.000000"))
    assert(Double.toString(-1.0).equals("-1.000000"))
    assert(Double.toString(0.0).equals("0.000000"))
    assert(Double.toString(-0.0).equals("-0.000000"))
    assert(Double.toString(Double.POSITIVE_INFINITY).equals("Infinity"))
    assert(Double.toString(Double.NEGATIVE_INFINITY).equals("-Infinity"))
    assert(Double.toString(Double.NaN).equals("NaN"))
  }
}
