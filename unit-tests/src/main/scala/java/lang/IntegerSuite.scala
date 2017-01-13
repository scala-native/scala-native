package java.lang

object IntegerSuite extends tests.Suite {
  val signedMaxValue     = Integer.MAX_VALUE
  val signedMaxValueText = "2147483647"
  val signedMinValue     = Integer.MIN_VALUE
  val signedMinValueText = "-2147483648"

  val signedMaxPlusOneText  = "2147483648"
  val signedMinMinusOneText = "-2147483649"

  val unsignedMaxValue       = -1
  val unsignedMaxValueText   = "4294967295"
  val unsignedMaxPlusOneText = "4294967296"

  test("parseInt") {
    import Integer.{parseInt => parse}

    assert(parse("-1") == -1)
    assert(parse("+1") == 1)
    assert(parse("1") == 1)
    assert(parse("-123") == -123)
    assert(parse("+123") == 123)
    assert(parse("123") == 123)
    assert(parse("-100", 2) == -4)
    assert(parse("+100", 2) == 4)
    assert(parse("100", 2) == 4)
    assert(parse("-0") == 0)
    assert(parse("+0") == 0)
    assert(parse("00") == 0)
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

  test("parseUnsignedInt") {
    import Integer.{parseUnsignedInt => parse}

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

    val octalMulOverflow = "137777777770"
    // in binary:
    // octalMulOverflow:  01011111111111111111111111111111000
    // max unsigned:      00011111111111111111111111111111111
    assertThrows[NumberFormatException](parse(octalMulOverflow, 8))
  }

  test("toString") {
    import java.lang.Integer.{toString => toStr}

    assert(toStr(0) == "0")
    assert(toStr(1) == "1")
    assert(toStr(12) == "12")
    assert(toStr(123) == "123")
    assert(toStr(1234) == "1234")
    assert(toStr(12345) == "12345")
    assert(toStr(10) == "10")
    assert(toStr(100) == "100")
    assert(toStr(1000) == "1000")
    assert(toStr(10000) == "10000")
    assert(toStr(100000) == "100000")
    assert(toStr(101010) == "101010")
    assert(toStr(111111) == "111111")
    assert(toStr(-1) == "-1")
    assert(toStr(-12) == "-12")
    assert(toStr(-123) == "-123")
    assert(toStr(-1234) == "-1234")
    assert(toStr(-12345) == "-12345")
    assert(toStr(signedMaxValue) == signedMaxValueText)
    assert(toStr(signedMinValue) == signedMinValueText)
  }

  test("toUnsignedString") {
    import java.lang.Integer.{toUnsignedString => toStr}

    assert(toStr(0) == "0")
    assert(toStr(1) == "1")
    assert(toStr(12) == "12")
    assert(toStr(123) == "123")
    assert(toStr(1234) == "1234")
    assert(toStr(12345) == "12345")
    assert(toStr(-1) == "4294967295")
    assert(toStr(-12) == "4294967284")
    assert(toStr(-123) == "4294967173")
    assert(toStr(-1234) == "4294966062")
    assert(toStr(-12345) == "4294954951")
    assert(toStr(unsignedMaxValue) == unsignedMaxValueText)
  }

  test("equals") {
    assert(new Integer(0) == new Integer(0))
    assert(new Integer(1) == new Integer(1))
    assert(new Integer(-1) == new Integer(-1))
    assert(new Integer(123) == new Integer(123))
    assert(new Integer(Integer.MAX_VALUE) == new Integer(Integer.MAX_VALUE))
    assert(new Integer(Integer.MIN_VALUE) == new Integer(Integer.MIN_VALUE))
  }

  test("highestOneBit") {
    assert(Integer.highestOneBit(1) == 1)
    assert(Integer.highestOneBit(2) == 2)
    assert(Integer.highestOneBit(3) == 2)
    assert(Integer.highestOneBit(4) == 4)
    assert(Integer.highestOneBit(5) == 4)
    assert(Integer.highestOneBit(6) == 4)
    assert(Integer.highestOneBit(7) == 4)
    assert(Integer.highestOneBit(8) == 8)
    assert(Integer.highestOneBit(9) == 8)
    assert(Integer.highestOneBit(63) == 32)
    assert(Integer.highestOneBit(64) == 64)
  }
}
