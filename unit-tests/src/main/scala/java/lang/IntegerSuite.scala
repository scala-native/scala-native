package java.lang

object IntegerSuite extends tests.Suite {

  val unsignedMaxValue       = -1
  val unsignedMaxValueText   = "4294967295"
  val unsignedMaxPlusOneText = "4294967296"

  test("parseInt") {
    assert(Integer.parseInt("-1").equals(-1))
    assert(Integer.parseInt("+1").equals(1))
    assert(Integer.parseInt("1").equals(1))

    assert(Integer.parseInt("-123").equals(-123))
    assert(Integer.parseInt("+123").equals(123))
    assert(Integer.parseInt("123").equals(123))

    assert(Integer.parseInt("-100", 2).equals(-4))
    assert(Integer.parseInt("+100", 2).equals(4))
    assert(Integer.parseInt("100", 2).equals(4))

    assert(Integer.parseInt("-0").equals(0))
    assert(Integer.parseInt("+0").equals(0))
    assert(Integer.parseInt("00").equals(0))

    assert(
      Integer.parseInt(Integer.MAX_VALUE.toString).equals(Integer.MAX_VALUE))
    assert(
      Integer.parseInt(Integer.MIN_VALUE.toString).equals(Integer.MIN_VALUE))

    assertThrows[NumberFormatException](Integer.parseInt(null))
    assertThrows[NumberFormatException](Integer.parseInt(""))
    assertThrows[NumberFormatException](
      Integer.parseInt("123", Character.MIN_RADIX - 1))
    assertThrows[NumberFormatException](
      Integer.parseInt("123", Character.MAX_RADIX + 1))
    assertThrows[NumberFormatException](Integer.parseInt("123a", 10))
    assertThrows[NumberFormatException](
      Integer.parseInt((Integer.MAX_VALUE.toLong + 1).toString))
    assertThrows[NumberFormatException](
      Integer.parseInt((Integer.MIN_VALUE.toLong - 1).toString))

  }

  test("parseUnsignedInt") {
    assert(Integer.parseUnsignedInt("1").equals(1))
    assert(Integer.parseUnsignedInt("+1").equals(1))

    assert(Integer.parseUnsignedInt("0").equals(0))
    assert(Integer.parseUnsignedInt("00").equals(0))

    assert(Integer.parseUnsignedInt("+100", 2).equals(4))
    assert(Integer.parseUnsignedInt("100", 2).equals(4))

    assert(
      Integer.parseUnsignedInt(unsignedMaxValueText).equals(unsignedMaxValue))

    assertThrows[NumberFormatException](Integer.parseUnsignedInt(null))
    assertThrows[NumberFormatException](Integer.parseUnsignedInt("+"))
    assertThrows[NumberFormatException](Integer.parseUnsignedInt("-"))
    assertThrows[NumberFormatException](Integer.parseUnsignedInt(""))
    assertThrows[NumberFormatException](Integer.parseUnsignedInt("-1"))
    assertThrows[NumberFormatException](
      Integer.parseUnsignedInt("123", Character.MIN_RADIX - 1))
    assertThrows[NumberFormatException](
      Integer.parseUnsignedInt("123", Character.MAX_RADIX + 1))
    assertThrows[NumberFormatException](Integer.parseUnsignedInt("123a", 10))
    assertThrows[NumberFormatException](
      Integer.parseUnsignedInt(unsignedMaxPlusOneText))

    val octalMulOverflow = "137777777770"
    // in binary:
    // octalMulOverflow:  01011111111111111111111111111111000
    // max unsigned:      00011111111111111111111111111111111
    assertThrows[NumberFormatException](
      Integer.parseUnsignedInt(octalMulOverflow, 8))
  }

  test("toString") {
    assert(Integer.toUnsignedString(0).equals("0"))
    assert(Integer.toString(1).equals("1"))
    assert(Integer.toString(-1).equals("-1"))
    assert(Integer.toString(123).equals("123"))
    assert(Integer.toString(-123).equals("-123"))
    assert(Integer.toString(1234).equals("1234"))
    assert(Integer.toString(-1234).equals("-1234"))
  }

  test("toUnsignedString") {
    assert(Integer.toUnsignedString(0).equals("0"))
    assert(Integer.toUnsignedString(1).equals("1"))
    assert(
      Integer.toUnsignedString(unsignedMaxValue).equals(unsignedMaxValueText))
    assert(Integer.toUnsignedString(123).equals("123"))
    assert(Integer.toUnsignedString(-123).equals("4294967173"))
    assert(Integer.toUnsignedString(1234).equals("1234"))
    assert(Integer.toUnsignedString(-1234).equals("4294966062"))
  }

  test("equals") {
    assert(new Integer(0).equals(new Integer(0)))
    assert(new Integer(1).equals(new Integer(1)))
    assert(new Integer(-1).equals(new Integer(-1)))
    assert(new Integer(123).equals(new Integer(123)))
    assert(
      new Integer(Integer.MAX_VALUE) equals new Integer(Integer.MAX_VALUE))
    assert(
      new Integer(Integer.MIN_VALUE) equals new Integer(Integer.MIN_VALUE))
  }
}
