package java.lang

import java.lang.Double.{
  doubleToLongBits,
  doubleToRawLongBits,
  longBitsToDouble,
  toHexString
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
    val bpinf2: java.lang.Double = scala.Double.MaxValue + scala.Double.MaxValue
    assertNot(bpinf1 eq bpinf2)

    val bninf1: java.lang.Double = scala.Double.NegativeInfinity
    val bninf2: java.lang.Double = scala.Double.MinValue + scala.Double.MinValue
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

    assert(Double.parseDouble("6.66D") == 6.66, "a8")

    // Java allows trailing whitespace, including tabs & nulls.
    assert(Double.parseDouble("6.66D\t ") == 6.66, "a9")
    assert(Double.parseDouble("6.66D\u0000") == 6.66, "a9a")

    assert(Double.parseDouble("6.66d") == 6.66, "a10")

    assert(Double.parseDouble("7.77F") == 7.77, "a11")
    assert(Double.parseDouble("7.77f") == 7.77, "a12")

    // Does not parse characters beyond IEEE754 spec.
    assert(Double.parseDouble("1.7976931348623157999999999")
             == 1.7976931348623157,
           "a13")

    assertThrows[NumberFormatException](Double.parseDouble(""))
    assertThrows[NumberFormatException](Double.parseDouble("D"))
    assertThrows[NumberFormatException](Double.parseDouble("potato"))
    assertThrows[NumberFormatException](Double.parseDouble("0.0potato"))
    assertThrows[NumberFormatException](Double.parseDouble("0.potato"))

    assertThrows[NumberFormatException](Double.parseDouble("6.66 D"))
    assertThrows[NumberFormatException](Double.parseDouble("6.66D  Bad  "))
    assertThrows[NumberFormatException](Double.parseDouble("6.66D\u0000a"))
    assertThrows[NumberFormatException](Double.parseDouble("6.66D \u0100"))

    // Out of IEE754 range handling

    //   Too big - java.lang.Double.MAX_VALUE times 10
    assert(Double.parseDouble("1.7976931348623157E309") ==
             Double.POSITIVE_INFINITY,
           "a20")

    //   Too big - Negative java.lang.Double.MAX_VALUE times 10
    assert(Double.parseDouble("-1.7976931348623157E309") ==
             Double.NEGATIVE_INFINITY,
           "a21")

    //   Too close to 0 - java.lang.Double.MIN_VALUE divided by 10
    assert(Double.parseDouble("4.9E-325") == 0.0, "a22")

    // Scala Native Issue #1836, a string Too Big reported from the wild.
    val a = "-274672389457236457826542634627345697228374687236476867674746" +
      "2342342342342342342342323423423423423423426767456345745293762384756" +
      "2384756345634568456345689345683475863465786485764785684564576348756" +
      "7384567845678658734587364576745683475674576345786348576847567846578" +
      "3456702897830296720476846578634576384567845678346573465786457863"

    assert(Double.parseDouble(a) == Double.NEGATIVE_INFINITY, "a23")

    // Hexadecimal strings
    assert(Double.parseDouble("0x0p1") == 0.0f, "a30")
    assert(Double.parseDouble("0x1p0") == 1.0f, "a31")

    assert(Double.parseDouble("0x1p1F") == 2.0f, "a32")

    assert(Double.parseDouble("0x1.8eae14p6") == 99.67f, "a33")
    assert(Double.parseDouble("-0x1.8eae14p6") == -99.67f, "a34")
  }

  // scala.Double passes -0.0d without change. j.l.Double gets forced to +0.0.
  private def assertD2sEquals(expected: String, f: scala.Double): Unit = {
    val result = f.toString
    assert(expected == result, s"result: $result != expected: $expected")
  }

  test("toString") {

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
    assertD2sEquals("3.141592653589793", (math.Pi * 1.0E+0))
    assertD2sEquals("31.41592653589793", (math.Pi * 1.0E+1))
    assertD2sEquals("0.3141592653589793", (math.Pi * 1.0E-1))

    // Test transitions to scientific notation.
    assertD2sEquals("3141592.653589793", (math.Pi * 1.0E+6))
    assertD2sEquals("3.1415926535897933E7", (math.Pi * 1.0E+7))
    assertD2sEquals("0.0031415926535897933", (math.Pi * 1.0E-3))
    assertD2sEquals("3.141592653589793E-4", (math.Pi * 1.0E-4))
  }

  test("toHexString - MIN_VALUE, Issue #1341") {
    assert(
      toHexString(java.lang.Double.MIN_VALUE).equals("0x0.0000000000001p-1022"))
  }

  test("toHexString - assorted other values") {

    assert(
      toHexString(java.lang.Double.MAX_VALUE).equals("0x1.fffffffffffffp1023"))

    // A value > 1.0 requiring lots of, but not all,  zeros.
    assert(toHexString(1.00000000000003).equals("0x1.0000000000087p0"))

    // An arbitrary but negative value.
    assert(java.lang.Double.toHexString(-31.0).equals("-0x1.fp4"))
  }

}
