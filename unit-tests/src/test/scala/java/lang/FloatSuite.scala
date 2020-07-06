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

    assert(Float.parseFloat("6.66D") == 6.66f, "a8")

    // Java allows trailing whitespace, including tabs & nulls.
    assert(Float.parseFloat("6.66D\t ") == 6.66f, "a9")
    assert(Float.parseFloat("6.66D\u0000") == 6.66f, "a9a")

    assert(Float.parseFloat("6.66d") == 6.66f, "a10")

    assert(Float.parseFloat("7.77F") == 7.77f, "a11")
    assert(Float.parseFloat("7.77f") == 7.77f, "a12")

    // Does not parse characters beyond IEEE754 spec.
    assert(
      Float.parseFloat("1.7976931348623157999999999") == 1.7976931348623157f,
      "a13")

    assertThrows[NumberFormatException](Float.parseFloat(""))
    assertThrows[NumberFormatException](Float.parseFloat("F"))
    assertThrows[NumberFormatException](Float.parseFloat("potato"))
    assertThrows[NumberFormatException](Float.parseFloat("0.0potato"))
    assertThrows[NumberFormatException](Float.parseFloat("0.potato"))

    assertThrows[NumberFormatException](Float.parseFloat("6.66 F"))
    assertThrows[NumberFormatException](Float.parseFloat("6.66F  Bad  "))
    assertThrows[NumberFormatException](Float.parseFloat("6.66F\u0000a"))
    assertThrows[NumberFormatException](Float.parseFloat("6.66F \u0100"))

    // Out of IEE754 range handling

    //   Too big - java.lang.Float.MAX_VALUE times 10
    assert(Float.parseFloat("3.4028235E39") ==
             Float.POSITIVE_INFINITY,
           "a20")

    //   Too big - Negative java.lang.Float.MAX_VALUE times 10
    assert(Float.parseFloat("-3.4028235E39") ==
             Float.NEGATIVE_INFINITY,
           "a21")

    //   Too close to 0 - java.lang.Float.MIN_VALUE divided by 10
    assert(Float.parseFloat("1.4E-46") == 0.0f, "a22")

    // Scala Native Issue #1836, a string Too Big reported from the wild.
    val a = "274672389457236457826542634627345697228374687236476867674746" +
      "2342342342342342342342323423423423423423426767456345745293762384756" +
      "2384756345634568456345689345683475863465786485764785684564576348756" +
      "7384567845678658734587364576745683475674576345786348576847567846578" +
      "3456702897830296720476846578634576384567845678346573465786457863"

    assert(Float.parseFloat(a) == Float.POSITIVE_INFINITY, "a23")

    // Hexadecimal strings
    assert(Float.parseFloat("0x0p1") == 0.0f, "a30")
    assert(Float.parseFloat("0x1p0") == 1.0f, "a31")
    assert(Float.parseFloat("0x1p1D") == 2.0f, "a32")

    assert(Float.parseFloat("0x1.8eae14p6") == 99.67f, "a33")
    assert(Float.parseFloat("-0x1.8eae14p6") == -99.67f, "a34")
  }

  // scala.Float passes -0.0F without change. j.l.Double forced to +0.0.
  private def assertF2sEquals(expected: String, f: scala.Float): Unit = {
    val result = f.toString
    assert(expected == result, s"result: $result != expected: $expected")
  }

  test("toString") {

    // Test non-finite values.
    assertF2sEquals("Infinity", Float.POSITIVE_INFINITY)
    assertF2sEquals("-Infinity", Float.NEGATIVE_INFINITY)
    assertF2sEquals("NaN", Float.NaN)

    // Test simple values around zero.
    assertF2sEquals("0.0", 0.0F)
    assertF2sEquals("-0.0", -0.0F)
    assertF2sEquals("1.0", 1.0F)
    assertF2sEquals("-1.0", -1.0F)
    assertF2sEquals("2.0", 2.0F)
    assertF2sEquals("-2.0", -2.0F)

    // Test maximum & minima.
    assertF2sEquals("3.4028235E38", scala.Float.MaxValue)
    assertF2sEquals("-3.4028235E38", scala.Float.MinValue)
    assertF2sEquals("1.4E-45", scala.Float.MinPositiveValue)

    // Test correctness least significant digits  & number of digits after the
    // decimal point of values with 'infinite' number of fraction digits.
    assertF2sEquals("3.1415927", (math.Pi * 1.0E+0).toFloat)
    assertF2sEquals("31.415926", (math.Pi * 1.0E+1).toFloat)
    assertF2sEquals("0.31415927", (math.Pi * 1.0E-1).toFloat)

    // Test transitions to scientific notation.
    assertF2sEquals("3141592.8", (math.Pi * 1.0E+6).toFloat)
    assertF2sEquals("3.1415926E7", (math.Pi * 1.0E+7).toFloat)
    assertF2sEquals("0.0031415927", (math.Pi * 1.0E-3).toFloat)
    assertF2sEquals("3.1415926E-4", (math.Pi * 1.0E-4).toFloat)
  }
}
