package java.lang

object IntegerSuite extends tests.Suite {
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

  test("toString") {
    assert(Integer.toString(1).equals("1"))
    assert(Integer.toString(-1).equals("-1"))
    assert(Integer.toString(123).equals("123"))
    assert(Integer.toString(-123).equals("-123"))
    assert(Integer.toString(1234).equals("1234"))
    assert(Integer.toString(-1234).equals("-1234"))
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
