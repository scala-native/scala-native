package java.lang

object LongSuite extends tests.Suite {
  val signedMaxValue     = Long.MAX_VALUE
  val signedMaxValueText = "9223372036854775807"
  val signedMinValue     = Long.MIN_VALUE
  val signedMinValueText = "-9223372036854775808"

  val signedMaxPlusOneText  = "9223372036854775808"
  val signedMinMinusOneText = "-9223372036854775809"

  val unsignedMaxValue       = -1L
  val unsignedMaxValueText   = "18446744073709551615"
  val unsignedMaxPlusOneText = "18446744073709551616"

  test("parseLong") {
    import Long.{parseLong => parse}

    assert(parse("-1") == -1L)
    assert(parse("+1") == 1L)
    assert(parse("1") == 1L)
    assert(parse("-123") == -123L)
    assert(parse("+123") == 123L)
    assert(parse("123") == 123L)
    assert(parse("-100", 2) == -4L)
    assert(parse("+100", 2) == 4L)
    assert(parse("100", 2) == 4L)
    assert(parse("-0") == 0L)
    assert(parse("+0") == 0L)
    assert(parse("00") == 0L)
    assert(parse(signedMaxValueText) == signedMaxValue)
    assert(parse(signedMinValueText) == signedMinValue)

    assertThrows[NumberFormatException](parse(null))
    assertThrows[NumberFormatException](parse("+"))
    assertThrows[NumberFormatException](parse("-"))
    assertThrows[NumberFormatException](parse(""))
    assertThrows[NumberFormatException](parse("123", Character.MIN_RADIX - 1))
    assertThrows[NumberFormatException](parse("123", Character.MAX_RADIX + 1))
    assertThrows[NumberFormatException](parse("123a", 10))
    assertThrows[NumberFormatException](parse(signedMinMinusOneText))
    assertThrows[NumberFormatException](parse(signedMaxPlusOneText))
  }

  test("parseUnsignedLong") {
    import Long.{parseUnsignedLong => parse}

    assert(parse("1") == 1)
    assert(parse("+1") == 1)
    assert(parse("0") == 0)
    assert(parse("00") == 0)
    assert(parse("+100", 2) == 4)
    assert(parse("100", 2) == 4)
    assert(parse(unsignedMaxValueText) == unsignedMaxValue)

    assertThrows[NumberFormatException](parse(null))
    assertThrows[NumberFormatException](parse("+"))
    assertThrows[NumberFormatException](parse("-"))
    assertThrows[NumberFormatException](parse(""))
    assertThrows[NumberFormatException](parse("-1"))
    assertThrows[NumberFormatException](parse("123", Character.MIN_RADIX - 1))
    assertThrows[NumberFormatException](parse("123", Character.MAX_RADIX + 1))
    assertThrows[NumberFormatException](parse("123a", 10))
    assertThrows[NumberFormatException](parse(unsignedMaxPlusOneText))

    val octalMulOverflow = "5777777777777777777770"
    // in binary:
    // octalMulOverflow:  0101111111111111111111111111111111111111111111111111111111111111000
    // max unsigned:      0001111111111111111111111111111111111111111111111111111111111111111
    assertThrows[NumberFormatException](parse(octalMulOverflow, 8))
  }

  test("toString") {
    import java.lang.Long.{toString => toStr}

    assert(toStr(0L) == "0")
    assert(toStr(1L) == "1")
    assert(toStr(12L) == "12")
    assert(toStr(123L) == "123")
    assert(toStr(1234L) == "1234")
    assert(toStr(12345L) == "12345")
    assert(toStr(10L) == "10")
    assert(toStr(100L) == "100")
    assert(toStr(1000L) == "1000")
    assert(toStr(10000L) == "10000")
    assert(toStr(100000L) == "100000")
    assert(toStr(101010L) == "101010")
    assert(toStr(111111L) == "111111")
    assert(toStr(-1L) == "-1")
    assert(toStr(-12L) == "-12")
    assert(toStr(-123L) == "-123")
    assert(toStr(-1234L) == "-1234")
    assert(toStr(-12345L) == "-12345")
    assert(toStr(signedMaxValue) == signedMaxValueText)
    assert(toStr(signedMinValue) == signedMinValueText)
  }

  test("toUnsignedString") {
    import java.lang.Long.{toUnsignedString => toStr}

    assert(toStr(0L) == "0")
    assert(toStr(1L) == "1")
    assert(toStr(12L) == "12")
    assert(toStr(123L) == "123")
    assert(toStr(1234L) == "1234")
    assert(toStr(12345L) == "12345")
    assert(toStr(-1L) == "18446744073709551615")
    assert(toStr(-12L) == "18446744073709551604")
    assert(toStr(-123L) == "18446744073709551493")
    assert(toStr(-1234L) == "18446744073709550382")
    assert(toStr(-12345L) == "18446744073709539271")
    assert(toStr(unsignedMaxValue) == unsignedMaxValueText)
  }

  test("equals") {
    assert(new Long(0) == new Long(0))
    assert(new Long(1) == new Long(1))
    assert(new Long(-1) == new Long(-1))
    assert(new Long(123) == new Long(123))
    assert(new Long(Long.MAX_VALUE) == new Long(Long.MAX_VALUE))
    assert(new Long(Long.MIN_VALUE) == new Long(Long.MIN_VALUE))
  }

  test("highestOneBit") {
    assert(Long.highestOneBit(1) == 1L)
    assert(Long.highestOneBit(2) == 2L)
    assert(Long.highestOneBit(3) == 2L)
    assert(Long.highestOneBit(4) == 4L)
    assert(Long.highestOneBit(5) == 4L)
    assert(Long.highestOneBit(6) == 4L)
    assert(Long.highestOneBit(7) == 4L)
    assert(Long.highestOneBit(8) == 8L)
    assert(Long.highestOneBit(9) == 8L)
    assert(Long.highestOneBit(63) == 32L)
    assert(Long.highestOneBit(64) == 64L)
    assert(Long.highestOneBit(Int.MaxValue) == 1073741824)
    assert(Long.highestOneBit(Int.MaxValue + 1L) == 2147483648L)
  }
}
