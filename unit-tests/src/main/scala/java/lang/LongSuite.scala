package java.lang

object LongSuite extends tests.Suite {
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
  }
}
