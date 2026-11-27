package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class LongTest {
  val signedMaxValue = Long.MAX_VALUE
  val signedMaxValueText = "9223372036854775807"
  val signedMinValue = Long.MIN_VALUE
  val signedMinValueText = "-9223372036854775808"

  val signedMaxPlusOneText = "9223372036854775808"
  val signedMinMinusOneText = "-9223372036854775809"

  val unsignedMaxValue = -1L
  val unsignedMaxValueText = "18446744073709551615"
  val unsignedMaxPlusOneText = "18446744073709551616"

  def assertThrowsAndMessage[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U
  )(expectedMsg: String): Unit = {
    val e = assertThrows(expectedThrowable, code)
    if (!Platform.executingInJVMOnLowerThanJDK(17))
      assertEquals(expectedMsg, e.toString())
  }

  @Test def decodeTest(): Unit = {
    import Long.decode

    assertEquals(-1L, decode("-1"))
    assertEquals(1L, decode("+1"))
    assertEquals(1L, decode("1"))
    assertEquals(-123L, decode("-123"))
    assertEquals(123L, decode("+123"))
    assertEquals(123L, decode("123"))
    assertEquals(0L, decode("-0"))
    assertEquals(0L, decode("+0"))
    assertEquals(0L, decode("00"))
    assertEquals(-1L, decode("-0x1"))
    assertEquals(1L, decode("+0x1"))
    assertEquals(1L, decode("0x1"))
    assertEquals(-123L, decode("-0x7b"))
    assertEquals(123L, decode("+0x7b"))
    assertEquals(123L, decode("0x7b"))
    assertEquals(0L, decode("-0x0"))
    assertEquals(0L, decode("+0x0"))
    assertEquals(0L, decode("0x0"))
    assertEquals(-1L, decode("-0X1"))
    assertEquals(1L, decode("+0X1"))
    assertEquals(1L, decode("0X1"))
    assertEquals(-123L, decode("-0X7B"))
    assertEquals(123L, decode("+0X7B"))
    assertEquals(123L, decode("0X7b"))
    assertEquals(0L, decode("-0X0"))
    assertEquals(0L, decode("+0X0"))
    assertEquals(0L, decode("0X0"))
    assertEquals(-1L, decode("-#1"))
    assertEquals(1L, decode("+#1"))
    assertEquals(1L, decode("#1"))
    assertEquals(-123L, decode("-#7B"))
    assertEquals(123L, decode("+#7B"))
    assertEquals(123L, decode("#7b"))
    assertEquals(0L, decode("-#0"))
    assertEquals(0L, decode("+#0"))
    assertEquals(0L, decode("#0"))
    assertEquals(-1L, decode("-01"))
    assertEquals(1L, decode("+01"))
    assertEquals(1L, decode("01"))
    assertEquals(-123L, decode("-0173"))
    assertEquals(123L, decode("+0173"))
    assertEquals(123L, decode("0173"))
    assertEquals(0L, decode("-00"))
    assertEquals(0L, decode("+00"))
    assertEquals(signedMaxValue, decode(signedMaxValueText))
    assertEquals(signedMinValue, decode(signedMinValueText))

    assertThrows(classOf[NullPointerException], decode(null))
    assertThrowsAndMessage(classOf[NumberFormatException], decode("+"))(
      """java.lang.NumberFormatException: For input string: """""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("-"))(
      """java.lang.NumberFormatException: For input string: "-""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode(""))(
      """java.lang.NumberFormatException: Zero length string"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("0x"))(
      """java.lang.NumberFormatException: For input string: "" under radix 16"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("#"))(
      """java.lang.NumberFormatException: For input string: "" under radix 16"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("0xh"))(
      """java.lang.NumberFormatException: For input string: "h" under radix 16"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("0XH"))(
      """java.lang.NumberFormatException: For input string: "H" under radix 16"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], decode("09"))(
      """java.lang.NumberFormatException: For input string: "9" under radix 8"""
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
    assertThrowsAndMessage(classOf[NumberFormatException], decode("-0xg"))(
      """java.lang.NumberFormatException: For input string: "-g" under radix 16"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      decode("0x8000000000000000")
    )(
      """java.lang.NumberFormatException: For input string: "8000000000000000" under radix 16"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      decode("-#8000000000000001")
    )(
      """java.lang.NumberFormatException: For input string: "-8000000000000001" under radix 16"""
    )
    for (nm <- Seq("--1", "-+1", "+-1", "0x-1", "#+1", "0-1"))
      assertThrowsAndMessage(classOf[NumberFormatException], decode(nm))(
        """java.lang.NumberFormatException: Sign character in wrong position"""
      )
  }

  @Test def parseLong(): Unit = {
    import Long.{parseLong => parse}

    assertEquals(-1L, parse("-1"))
    assertEquals(1L, parse("+1"))
    assertEquals(1L, parse("1"))
    assertEquals(-123L, parse("-123"))
    assertEquals(123L, parse("+123"))
    assertEquals(123L, parse("123"))
    assertEquals(-4L, parse("-100", 2))
    assertEquals(4L, parse("+100", 2))
    assertEquals(4L, parse("100", 2))
    assertEquals(0L, parse("-0"))
    assertEquals(0L, parse("+0"))
    assertEquals(0L, parse("00"))
    assertEquals(signedMaxValue, parse(signedMaxValueText))
    assertEquals(signedMinValue, parse(signedMinValueText))

    assertThrowsAndMessage(classOf[NumberFormatException], parse(null))(
      "java.lang.NumberFormatException: Cannot parse null string"
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
    assertThrowsAndMessage(classOf[NumberFormatException], parse("12g", 16))(
      """java.lang.NumberFormatException: For input string: "12g" under radix 16"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse("-8000000000000001", 16)
    )(
      """java.lang.NumberFormatException: For input string: "-8000000000000001" under radix 16"""
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

  @Test def decimalParsingAcrossFastPrefix(): Unit = {
    def arabicDigits(s: String): String =
      s.map(ch =>
        if (ch >= '0' && ch <= '9') ('\u0660' + ch - '0').toChar else ch
      )

    val zeros = "0" * 24
    assertEquals(999999999999999999L, Long.parseLong("999999999999999999"))
    assertEquals(
      signedMaxValue,
      Long.parseLong(arabicDigits(signedMaxValueText))
    )
    assertEquals(
      signedMinValue,
      Long.parseLong(arabicDigits(signedMinValueText))
    )
    assertEquals(
      signedMaxValue,
      Long.parseLong("+" + zeros + signedMaxValueText, 10)
    )
    assertEquals(
      signedMinValue,
      Long.parseLong("-" + zeros + signedMinValueText.drop(1), 10)
    )
    assertEquals(123L, Long.parseLong("1\u06623"))
    assertEquals(0L, Long.parseLong("-" + zeros))

    assertEquals(
      unsignedMaxValue,
      Long.parseUnsignedLong(arabicDigits(unsignedMaxValueText))
    )
    assertEquals(
      unsignedMaxValue,
      Long.parseUnsignedLong("+" + zeros + unsignedMaxValueText, 10)
    )
    assertEquals(
      -6L,
      Long.parseUnsignedLong(unsignedMaxValueText.dropRight(1) + "0")
    )
    assertEquals(123L, Long.parseUnsignedLong("1\u06623"))
    assertEquals(0L, Long.parseUnsignedLong(zeros))

    val slice = "x+" + zeros + unsignedMaxValueText + "!"
    assertEquals(
      unsignedMaxValue,
      Long.parseUnsignedLong(slice, 1, slice.length() - 1, 10)
    )
    assertThrows(
      classOf[NumberFormatException],
      Long.parseLong(zeros + signedMaxPlusOneText)
    )
    assertThrows(
      classOf[NumberFormatException],
      Long.parseUnsignedLong(zeros + unsignedMaxPlusOneText)
    )
    assertThrows(
      classOf[NumberFormatException],
      Long.parseUnsignedLong("18446744073709551620")
    )
  }

  @Test def parseUnsignedLong(): Unit = {
    import Long.{parseUnsignedLong => parse}

    assertEquals(1L, parse("1"))
    assertEquals(1L, parse("+1"))
    assertEquals(0L, parse("0"))
    assertEquals(0L, parse("00"))
    assertEquals(4L, parse("+100", 2))
    assertEquals(4L, parse("100", 2))
    assertEquals(unsignedMaxValue, parse(unsignedMaxValueText))

    assertThrowsAndMessage(classOf[NumberFormatException], parse(null))(
      """java.lang.NumberFormatException: Cannot parse null string"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("+"))(
      """java.lang.NumberFormatException: For input string: "+""""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("-"))(
      """java.lang.NumberFormatException: Illegal leading minus sign on unsigned string -."""
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
      s"""java.lang.NumberFormatException: String value $unsignedMaxPlusOneText exceeds range of unsigned long."""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse("1ffffffffffffffff", 16)
    )(
      """java.lang.NumberFormatException: String value 1ffffffffffffffff exceeds range of unsigned long."""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("12g", 16))(
      """java.lang.NumberFormatException: For input string: "12g" under radix 16"""
    )
    assertThrowsAndMessage(classOf[NumberFormatException], parse("+", 16))(
      """java.lang.NumberFormatException: For input string: "+" under radix 16"""
    )

    val octalMulOverflow = "5777777777777777777770"
    // in binary:
    // octalMulOverflow:  0101111111111111111111111111111111111111111111111111111111111111000
    // max unsigned:      0001111111111111111111111111111111111111111111111111111111111111111
    assertThrows(classOf[NumberFormatException], parse(octalMulOverflow, 8))
  }

  @Test def testToString(): Unit = {
    import java.lang.Long.{toString => toStr}

    assertEquals("0", toStr(0L))
    assertEquals("1", toStr(1L))
    assertEquals("12", toStr(12L))
    assertEquals("123", toStr(123L))
    assertEquals("1234", toStr(1234L))
    assertEquals("12345", toStr(12345L))
    assertEquals("10", toStr(10L))
    assertEquals("100", toStr(100L))
    assertEquals("1000", toStr(1000L))
    assertEquals("10000", toStr(10000L))
    assertEquals("100000", toStr(100000L))
    assertEquals("101010", toStr(101010L))
    assertEquals("111111", toStr(111111L))
    assertEquals("-1", toStr(-1L))
    assertEquals("-12", toStr(-12L))
    assertEquals("-123", toStr(-123L))
    assertEquals("-1234", toStr(-1234L))
    assertEquals("-12345", toStr(-12345L))
    assertEquals(signedMaxValueText, toStr(signedMaxValue))
    assertEquals(signedMinValueText, toStr(signedMinValue))
  }

  @Test def toUnsignedString(): Unit = {
    import java.lang.Long.{toUnsignedString => toStr}

    assertEquals("0", toStr(0L))
    assertEquals("1", toStr(1L))
    assertEquals("12", toStr(12L))
    assertEquals("123", toStr(123L))
    assertEquals("1234", toStr(1234L))
    assertEquals("12345", toStr(12345L))
    assertEquals("18446744073709551615", toStr(-1L))
    assertEquals("18446744073709551604", toStr(-12L))
    assertEquals("18446744073709551493", toStr(-123L))
    assertEquals("18446744073709550382", toStr(-1234L))
    assertEquals("18446744073709539271", toStr(-12345L))
    assertEquals(unsignedMaxValueText, toStr(unsignedMaxValue))
  }

  @deprecated @Test def testEquals(): Unit = {
    assertEquals(new Long(0), new Long(0))
    assertEquals(new Long(1), new Long(1))
    assertEquals(new Long(-1), new Long(-1))
    assertEquals(new Long(123), new Long(123))
    assertEquals(new Long(Long.MAX_VALUE), new Long(Long.MAX_VALUE))
    assertEquals(new Long(Long.MIN_VALUE), new Long(Long.MIN_VALUE))
  }

  @Test def highestOneBit(): Unit = {
    assertEquals(1L, Long.highestOneBit(1))
    assertEquals(2L, Long.highestOneBit(2))
    assertEquals(2L, Long.highestOneBit(3))
    assertEquals(4L, Long.highestOneBit(4))
    assertEquals(4L, Long.highestOneBit(5))
    assertEquals(4L, Long.highestOneBit(6))
    assertEquals(4L, Long.highestOneBit(7))
    assertEquals(8L, Long.highestOneBit(8))
    assertEquals(8L, Long.highestOneBit(9))
    assertEquals(32L, Long.highestOneBit(63))
    assertEquals(64L, Long.highestOneBit(64))
    assertEquals(1073741824L, Long.highestOneBit(Int.MaxValue))
    assertEquals(2147483648L, Long.highestOneBit(Int.MaxValue + 1L))
  }
}
