package java.lang

object LongSuite extends tests.Suite {
  val unsignedMaxValueText = "18446744073709551615"
  val unsignedMaxPlusOneText = "18446744073709551616"
  val unsignedMaxValue = -1L

  val signedMaxPlusOneText = "9223372036854775808"
  val signedMinMinusOneText = "-9223372036854775809"

  test("parseLong") {
    assert(Long.parseLong("-1").equals(-1L))
    assert(Long.parseLong("+1").equals(1L))
    assert(Long.parseLong("1").equals(1L))

    assert(Long.parseLong("-123").equals(-123L))
    assert(Long.parseLong("+123").equals(123L))
    assert(Long.parseLong("123").equals(123L))

    assert(Long.parseLong("-100", 2).equals(-4L))
    assert(Long.parseLong("+100", 2).equals(4L))
    assert(Long.parseLong("100", 2).equals(4L))

    assert(Long.parseLong("-0").equals(0L))
    assert(Long.parseLong("+0").equals(0L))
    assert(Long.parseLong("00").equals(0L))

    assert(Long.parseLong(Long.MAX_VALUE.toString).equals(Long.MAX_VALUE))
    assert(Long.parseLong(Long.MIN_VALUE.toString).equals(Long.MIN_VALUE))

    assertThrows[NumberFormatException](Long.parseLong(null))
    assertThrows[NumberFormatException](Long.parseLong("+"))
    assertThrows[NumberFormatException](Long.parseLong("-"))
    assertThrows[NumberFormatException](Long.parseLong(""))
    assertThrows[NumberFormatException](
        Long.parseLong("123", Character.MIN_RADIX - 1))
    assertThrows[NumberFormatException](
        Long.parseLong("123", Character.MAX_RADIX + 1))
    assertThrows[NumberFormatException](Long.parseLong("123a", 10))
    assertThrows[NumberFormatException](Long.parseLong(signedMinMinusOneText))
    assertThrows[NumberFormatException](Long.parseLong(signedMaxPlusOneText))
  }

  test("parseUnsignedLong") {
    assert(Long.parseUnsignedLong("1").equals(1L))
    assert(Long.parseUnsignedLong("+1").equals(1L))

    assert(Long.parseUnsignedLong("0").equals(0L))
    assert(Long.parseUnsignedLong("00").equals(0L))

    assert(Long.parseUnsignedLong("+100", 2).equals(4L))
    assert(Long.parseUnsignedLong("100", 2).equals(4L))

    assert(
        Long.parseUnsignedLong(unsignedMaxValueText).equals(unsignedMaxValue))

    assertThrows[NumberFormatException](Long.parseUnsignedLong(null))
    assertThrows[NumberFormatException](Long.parseUnsignedLong("+"))
    assertThrows[NumberFormatException](Long.parseUnsignedLong("-"))
    assertThrows[NumberFormatException](Long.parseUnsignedLong(""))
    assertThrows[NumberFormatException](Long.parseUnsignedLong("-1"))
    assertThrows[NumberFormatException](
        Long.parseUnsignedLong("123", Character.MIN_RADIX - 1))
    assertThrows[NumberFormatException](
        Long.parseUnsignedLong("123", Character.MAX_RADIX + 1))
    assertThrows[NumberFormatException](Long.parseUnsignedLong("123a", 10))
    assertThrows[NumberFormatException](
        Long.parseUnsignedLong(unsignedMaxPlusOneText))

    val octalMulOverflow = "5777777777777777777770"
    // in binary:
    // octalMulOverflow:  0101111111111111111111111111111111111111111111111111111111111111000
    // max unsigned:      0001111111111111111111111111111111111111111111111111111111111111111
    assertThrows[NumberFormatException](
        Long.parseUnsignedLong(octalMulOverflow, 8))
  }

  test("toString") {
    assert(Long.toString(0L).equals("0"))
    assert(Long.toString(1L).equals("1"))
    assert(Long.toString(-1L).equals("-1"))
    assert(Long.toString(123L).equals("123"))
    assert(Long.toString(-123L).equals("-123"))
    assert(Long.toString(1234L).equals("1234"))
    assert(Long.toString(-1234L).equals("-1234"))
  }

  test("toUnsignedString") {
    assert(Long.toUnsignedString(0L).equals("0"))
    assert(Long.toUnsignedString(1L).equals("1"))
    assert(
        Long.toUnsignedString(unsignedMaxValue).equals(unsignedMaxValueText))
    assert(Long.toUnsignedString(123L).equals("123"))
    assert(Long.toUnsignedString(-123L).equals("18446744073709551493"))
    assert(Long.toUnsignedString(1234L).equals("1234"))
    assert(Long.toUnsignedString(-1234L).equals("18446744073709550382"))
  }

}
