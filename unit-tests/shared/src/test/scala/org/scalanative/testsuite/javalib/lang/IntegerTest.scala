package org.scalanative.testsuite.javalib.lang

import java.lang.*

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class IntegerTest {
  val signedMaxValue = Integer.MAX_VALUE
  val signedMaxValueText = "2147483647"
  val signedMinValue = Integer.MIN_VALUE
  val signedMinValueText = "-2147483648"

  val signedMaxPlusOneText = "2147483648"
  val signedMinMinusOneText = "-2147483649"

  val unsignedMaxValue = -1
  val unsignedMaxValueText = "4294967295"
  val unsignedMaxPlusOneText = "4294967296"

  def assertThrowsAndMessage[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U
  )(expectedMsg: String): Unit = {
    assertThrows(expectedMsg, expectedThrowable, code)
  }

  @Test def decodeTest(): Unit = {
    import Integer.decode

    assertEquals(-1, decode("-1"))
    assertEquals(1, decode("+1"))
    assertEquals(1, decode("1"))
    assertEquals(-123, decode("-123"))
    assertEquals(123, decode("+123"))
    assertEquals(123, decode("123"))
    assertEquals(0, decode("-0"))
    assertEquals(0, decode("+0"))
    assertEquals(0, decode("00"))
    assertEquals(-1, decode("-0x1"))
    assertEquals(1, decode("+0x1"))
    assertEquals(1, decode("0x1"))
    assertEquals(-123, decode("-0x7b"))
    assertEquals(123, decode("+0x7b"))
    assertEquals(123, decode("0x7b"))
    assertEquals(0, decode("-0x0"))
    assertEquals(0, decode("+0x0"))
    assertEquals(0, decode("0x0"))
    assertEquals(-1, decode("-0X1"))
    assertEquals(1, decode("+0X1"))
    assertEquals(1, decode("0X1"))
    assertEquals(-123, decode("-0X7B"))
    assertEquals(123, decode("+0X7B"))
    assertEquals(123, decode("0X7b"))
    assertEquals(0, decode("-0X0"))
    assertEquals(0, decode("+0X0"))
    assertEquals(0, decode("0X0"))
    assertEquals(-1, decode("-#1"))
    assertEquals(1, decode("+#1"))
    assertEquals(1, decode("#1"))
    assertEquals(-123, decode("-#7B"))
    assertEquals(123, decode("+#7B"))
    assertEquals(123, decode("#7b"))
    assertEquals(0, decode("-#0"))
    assertEquals(0, decode("+#0"))
    assertEquals(0, decode("#0"))
    assertEquals(-1, decode("-01"))
    assertEquals(1, decode("+01"))
    assertEquals(1, decode("01"))
    assertEquals(-123, decode("-0173"))
    assertEquals(123, decode("+0173"))
    assertEquals(123, decode("0173"))
    assertEquals(0, decode("-00"))
    assertEquals(0, decode("+00"))
    assertEquals(signedMaxValue, decode(signedMaxValueText))
    assertEquals(signedMinValue, decode(signedMinValueText))

    assertThrowsAndMessage(classOf[NumberFormatException], decode(null))(
      "java.lang.NumberFormatException: null"
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("+"))(
      """java.lang.NumberFormatException: For input string: "+""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("-"))(
      """java.lang.NumberFormatException: For input string: "-""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode(""))(
      """java.lang.NumberFormatException: For input string: """""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("0x"))(
      """java.lang.NumberFormatException: For input string: "0x""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("#"))(
      """java.lang.NumberFormatException: For input string: "#""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("0xh"))(
      """java.lang.NumberFormatException: For input string: "0xh""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("0XH"))(
      """java.lang.NumberFormatException: For input string: "0XH""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("09"))(
      """java.lang.NumberFormatException: For input string: "09""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("123a"))(
      """java.lang.NumberFormatException: For input string: "123a""""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      decode(signedMinMinusOneText)
    )(
      s"""java.lang.NumberFormatException: For input string: "$signedMinMinusOneText""""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      decode(signedMaxPlusOneText)
    )(
      s"""java.lang.NumberFormatException: For input string: "$signedMaxPlusOneText""""
    )
  }

  @Test def parseInt(): Unit = {
    import Integer.parseInt as parse

    assertEquals(-1, parse("-1"))
    assertEquals(1, parse("+1"))
    assertEquals(1, parse("1"))
    assertEquals(-123, parse("-123"))
    assertEquals(123, parse("+123"))
    assertEquals(123, parse("123"))
    assertEquals(-4, parse("-100", 2))
    assertEquals(4, parse("+100", 2))
    assertEquals(4, parse("100", 2))
    assertEquals(0, parse("-0"))
    assertEquals(0, parse("+0"))
    assertEquals(0, parse("00"))
    assertEquals(signedMaxValue, parse(signedMaxValueText))
    assertEquals(signedMinValue, parse(signedMinValueText))

    assertThrowsAndMessage(classOf[NumberFormatException], parse(null))(
      "java.lang.NumberFormatException: null"
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("+"))(
      """java.lang.NumberFormatException: For input string: "+""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("-"))(
      """java.lang.NumberFormatException: For input string: "-""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse(""))(
      """java.lang.NumberFormatException: For input string: """""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse("123", Character.MIN_RADIX - 1)
    )(
      """java.lang.NumberFormatException: radix 1 less than Character.MIN_RADIX"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse("123", Character.MAX_RADIX + 1)
    )(
      """java.lang.NumberFormatException: radix 37 greater than Character.MAX_RADIX"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("123a", 10))(
      """java.lang.NumberFormatException: For input string: "123a""""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse(signedMinMinusOneText)
    )(
      s"""java.lang.NumberFormatException: For input string: "$signedMinMinusOneText""""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse(signedMaxPlusOneText)
    )(
      s"""java.lang.NumberFormatException: For input string: "$signedMaxPlusOneText""""
    )
  }

  @Test def parseUnsignedInt(): Unit = {
    import Integer.parseUnsignedInt as parse

    assertEquals(1, parse("1"))
    assertEquals(1, parse("+1"))
    assertEquals(0, parse("0"))
    assertEquals(0, parse("00"))
    assertEquals(4, parse("+100", 2))
    assertEquals(4, parse("100", 2))
    assertEquals(unsignedMaxValue, parse(unsignedMaxValueText))

    assertThrowsAndMessage(classOf[NumberFormatException], parse(null))(
      """java.lang.NumberFormatException: null"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("+"))(
      """java.lang.NumberFormatException: For input string: "+""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("-"))(
      """java.lang.NumberFormatException: For input string: "-""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse(""))(
      """java.lang.NumberFormatException: For input string: """""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("-1"))(
      """java.lang.NumberFormatException: Illegal leading minus sign on unsigned string -1."""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse("123", Character.MIN_RADIX - 1)
    )(
      """java.lang.NumberFormatException: radix 1 less than Character.MIN_RADIX"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse("123", Character.MAX_RADIX + 1)
    )(
      """java.lang.NumberFormatException: radix 37 greater than Character.MAX_RADIX"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("123a", 10))(
      """java.lang.NumberFormatException: For input string: "123a""""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse(unsignedMaxPlusOneText)
    )(
      s"""java.lang.NumberFormatException: String value $unsignedMaxPlusOneText exceeds range of unsigned int."""
    )

    val octalMulOverflow = "137777777770"
    // in binary:
    // octalMulOverflow:  01011111111111111111111111111111000
    // max unsigned:      00011111111111111111111111111111111
    assertThrows(classOf[NumberFormatException], parse(octalMulOverflow, 8))
  }

  @Test def testToString(): Unit = {
    import java.lang.Integer.toString as toStr

    assertEquals("0", toStr(0))
    assertEquals("1", toStr(1))
    assertEquals("12", toStr(12))
    assertEquals("123", toStr(123))
    assertEquals("1234", toStr(1234))
    assertEquals("12345", toStr(12345))
    assertEquals("10", toStr(10))
    assertEquals("100", toStr(100))
    assertEquals("1000", toStr(1000))
    assertEquals("10000", toStr(10000))
    assertEquals("100000", toStr(100000))
    assertEquals("101010", toStr(101010))
    assertEquals("111111", toStr(111111))
    assertEquals("-1", toStr(-1))
    assertEquals("-12", toStr(-12))
    assertEquals("-123", toStr(-123))
    assertEquals("-1234", toStr(-1234))
    assertEquals("-12345", toStr(-12345))
    assertEquals(signedMaxValueText, toStr(signedMaxValue))
    assertEquals(signedMinValueText, toStr(signedMinValue))
  }

  @Test def toUnsignedString(): Unit = {
    import java.lang.Integer.toUnsignedString as toStr

    assertEquals("0", toStr(0))
    assertEquals("1", toStr(1))
    assertEquals("12", toStr(12))
    assertEquals("123", toStr(123))
    assertEquals("1234", toStr(1234))
    assertEquals("12345", toStr(12345))
    assertEquals("4294967295", toStr(-1))
    assertEquals("4294967284", toStr(-12))
    assertEquals("4294967173", toStr(-123))
    assertEquals("4294966062", toStr(-1234))
    assertEquals("4294954951", toStr(-12345))
    assertEquals(unsignedMaxValueText, toStr(unsignedMaxValue))
  }

  @deprecated @Test def testEquals(): Unit = {
    assertEquals(new Integer(0), new Integer(0))
    assertEquals(new Integer(1), new Integer(1))
    assertEquals(new Integer(-1), new Integer(-1))
    assertEquals(new Integer(123), new Integer(123))
    assertEquals(new Integer(Integer.MAX_VALUE), new Integer(Integer.MAX_VALUE))
    assertEquals(new Integer(Integer.MIN_VALUE), new Integer(Integer.MIN_VALUE))
  }

  @Test def highestOneBit(): Unit = {
    assertEquals(1, Integer.highestOneBit(1))
    assertEquals(2, Integer.highestOneBit(2))
    assertEquals(2, Integer.highestOneBit(3))
    assertEquals(4, Integer.highestOneBit(4))
    assertEquals(4, Integer.highestOneBit(5))
    assertEquals(4, Integer.highestOneBit(6))
    assertEquals(4, Integer.highestOneBit(7))
    assertEquals(8, Integer.highestOneBit(8))
    assertEquals(8, Integer.highestOneBit(9))
    assertEquals(32, Integer.highestOneBit(63))
    assertEquals(64, Integer.highestOneBit(64))
  }
}
