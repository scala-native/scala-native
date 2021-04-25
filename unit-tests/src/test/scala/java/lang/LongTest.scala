package java.lang

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._
import scalanative.junit.utils.ThrowsHelper._

class LongTest {
  val signedMaxValue     = Long.MAX_VALUE
  val signedMaxValueText = "9223372036854775807"
  val signedMinValue     = Long.MIN_VALUE
  val signedMinValueText = "-9223372036854775808"

  val signedMaxPlusOneText  = "9223372036854775808"
  val signedMinMinusOneText = "-9223372036854775809"

  val unsignedMaxValue       = -1L
  val unsignedMaxValueText   = "18446744073709551615"
  val unsignedMaxPlusOneText = "18446744073709551616"

  @Test def decodeTest(): Unit = {
    import Long.decode

    assertTrue(decode("-1") == -1)
    assertTrue(decode("+1") == 1)
    assertTrue(decode("1") == 1)
    assertTrue(decode("-123") == -123)
    assertTrue(decode("+123") == 123)
    assertTrue(decode("123") == 123)
    assertTrue(decode("-0") == 0)
    assertTrue(decode("+0") == 0)
    assertTrue(decode("00") == 0)
    assertTrue(decode("-0x1") == -1)
    assertTrue(decode("+0x1") == 1)
    assertTrue(decode("0x1") == 1)
    assertTrue(decode("-0x7b") == -123)
    assertTrue(decode("+0x7b") == 123)
    assertTrue(decode("0x7b") == 123)
    assertTrue(decode("-0x0") == 0)
    assertTrue(decode("+0x0") == 0)
    assertTrue(decode("0x0") == 0)
    assertTrue(decode("-0X1") == -1)
    assertTrue(decode("+0X1") == 1)
    assertTrue(decode("0X1") == 1)
    assertTrue(decode("-0X7B") == -123)
    assertTrue(decode("+0X7B") == 123)
    assertTrue(decode("0X7b") == 123)
    assertTrue(decode("-0X0") == 0)
    assertTrue(decode("+0X0") == 0)
    assertTrue(decode("0X0") == 0)
    assertTrue(decode("-#1") == -1)
    assertTrue(decode("+#1") == 1)
    assertTrue(decode("#1") == 1)
    assertTrue(decode("-#7B") == -123)
    assertTrue(decode("+#7B") == 123)
    assertTrue(decode("#7b") == 123)
    assertTrue(decode("-#0") == 0)
    assertTrue(decode("+#0") == 0)
    assertTrue(decode("#0") == 0)
    assertTrue(decode("-01") == -1)
    assertTrue(decode("+01") == 1)
    assertTrue(decode("01") == 1)
    assertTrue(decode("-0173") == -123)
    assertTrue(decode("+0173") == 123)
    assertTrue(decode("0173") == 123)
    assertTrue(decode("-00") == 0)
    assertTrue(decode("+00") == 0)
    assertTrue(decode(signedMaxValueText) == signedMaxValue)
    assertTrue(decode(signedMinValueText) == signedMinValue)

    assertThrowsAnd(classOf[NumberFormatException], decode(null)) {
      _.toString == "java.lang.NumberFormatException: null"
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("+")) {
      _.toString == """java.lang.NumberFormatException: For input string: "+""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("-")) {
      _.toString == """java.lang.NumberFormatException: For input string: "-""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("")) {
      _.toString == """java.lang.NumberFormatException: For input string: """""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("0x")) {
      _.toString == """java.lang.NumberFormatException: For input string: "0x""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("#")) {
      _.toString == """java.lang.NumberFormatException: For input string: "#""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("0xh")) {
      _.toString == """java.lang.NumberFormatException: For input string: "0xh""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("0XH")) {
      _.toString == """java.lang.NumberFormatException: For input string: "0XH""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("09")) {
      _.toString == """java.lang.NumberFormatException: For input string: "09""""
    }
    assertThrowsAnd(classOf[NumberFormatException], decode("123a")) {
      _.toString == """java.lang.NumberFormatException: For input string: "123a""""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    decode(signedMinMinusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: For input string: "$signedMinMinusOneText""""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    decode(signedMaxPlusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: For input string: "$signedMaxPlusOneText""""
    }
  }

  @Test def parseLong(): Unit = {
    import Long.{parseLong => parse}

    assertTrue(parse("-1") == -1L)
    assertTrue(parse("+1") == 1L)
    assertTrue(parse("1") == 1L)
    assertTrue(parse("-123") == -123L)
    assertTrue(parse("+123") == 123L)
    assertTrue(parse("123") == 123L)
    assertTrue(parse("-100", 2) == -4L)
    assertTrue(parse("+100", 2) == 4L)
    assertTrue(parse("100", 2) == 4L)
    assertTrue(parse("-0") == 0L)
    assertTrue(parse("+0") == 0L)
    assertTrue(parse("00") == 0L)
    assertTrue(parse(signedMaxValueText) == signedMaxValue)
    assertTrue(parse(signedMinValueText) == signedMinValue)

    assertThrowsAnd(classOf[NumberFormatException], parse(null)) {
      _.toString == "java.lang.NumberFormatException: null"
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("+")) {
      _.toString == """java.lang.NumberFormatException: For input string: "+""""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("-")) {
      _.toString == """java.lang.NumberFormatException: For input string: "-""""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("")) {
      _.toString == """java.lang.NumberFormatException: For input string: """""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse("123", Character.MIN_RADIX - 1)) {
      _.toString == """java.lang.NumberFormatException: radix 1 less than Character.MIN_RADIX"""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse("123", Character.MAX_RADIX + 1)) {
      _.toString == """java.lang.NumberFormatException: radix 37 greater than Character.MAX_RADIX"""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("123a", 10)) {
      _.toString == """java.lang.NumberFormatException: For input string: "123a""""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse(signedMinMinusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: For input string: "$signedMinMinusOneText""""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse(signedMaxPlusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: For input string: "$signedMaxPlusOneText""""
    }
  }

  @Test def parseUnsignedLong(): Unit = {
    import Long.{parseUnsignedLong => parse}

    assertTrue(parse("1") == 1)
    assertTrue(parse("+1") == 1)
    assertTrue(parse("0") == 0)
    assertTrue(parse("00") == 0)
    assertTrue(parse("+100", 2) == 4)
    assertTrue(parse("100", 2) == 4)
    assertTrue(parse(unsignedMaxValueText) == unsignedMaxValue)

    assertThrowsAnd(classOf[NumberFormatException], parse(null)) {
      _.toString == """java.lang.NumberFormatException: null"""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("+")) {
      _.toString == """java.lang.NumberFormatException: For input string: "+""""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("-")) {
      _.toString == """java.lang.NumberFormatException: For input string: "-""""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("")) {
      _.toString == """java.lang.NumberFormatException: For input string: """""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("-1")) {
      _.toString == """java.lang.NumberFormatException: Illegal leading minus sign on unsigned string -1."""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse("123", Character.MIN_RADIX - 1)) {
      _.toString == """java.lang.NumberFormatException: radix 1 less than Character.MIN_RADIX"""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse("123", Character.MAX_RADIX + 1)) {
      _.toString == """java.lang.NumberFormatException: radix 37 greater than Character.MAX_RADIX"""
    }
    assertThrowsAnd(classOf[NumberFormatException], parse("123a", 10)) {
      _.toString == """java.lang.NumberFormatException: For input string: "123a""""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse(unsignedMaxPlusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: String value $unsignedMaxPlusOneText exceeds range of unsigned long."""
    }

    val octalMulOverflow = "5777777777777777777770"
    // in binary:
    // octalMulOverflow:  0101111111111111111111111111111111111111111111111111111111111111000
    // max unsigned:      0001111111111111111111111111111111111111111111111111111111111111111
    assertThrows(classOf[NumberFormatException], parse(octalMulOverflow, 8))
  }

  @Test def testToString(): Unit = {
    import java.lang.Long.{toString => toStr}

    assertTrue(toStr(0L) == "0")
    assertTrue(toStr(1L) == "1")
    assertTrue(toStr(12L) == "12")
    assertTrue(toStr(123L) == "123")
    assertTrue(toStr(1234L) == "1234")
    assertTrue(toStr(12345L) == "12345")
    assertTrue(toStr(10L) == "10")
    assertTrue(toStr(100L) == "100")
    assertTrue(toStr(1000L) == "1000")
    assertTrue(toStr(10000L) == "10000")
    assertTrue(toStr(100000L) == "100000")
    assertTrue(toStr(101010L) == "101010")
    assertTrue(toStr(111111L) == "111111")
    assertTrue(toStr(-1L) == "-1")
    assertTrue(toStr(-12L) == "-12")
    assertTrue(toStr(-123L) == "-123")
    assertTrue(toStr(-1234L) == "-1234")
    assertTrue(toStr(-12345L) == "-12345")
    assertTrue(toStr(signedMaxValue) == signedMaxValueText)
    assertTrue(toStr(signedMinValue) == signedMinValueText)
  }

  @Test def toUnsignedString(): Unit = {
    import java.lang.Long.{toUnsignedString => toStr}

    assertTrue(toStr(0L) == "0")
    assertTrue(toStr(1L) == "1")
    assertTrue(toStr(12L) == "12")
    assertTrue(toStr(123L) == "123")
    assertTrue(toStr(1234L) == "1234")
    assertTrue(toStr(12345L) == "12345")
    assertTrue(toStr(-1L) == "18446744073709551615")
    assertTrue(toStr(-12L) == "18446744073709551604")
    assertTrue(toStr(-123L) == "18446744073709551493")
    assertTrue(toStr(-1234L) == "18446744073709550382")
    assertTrue(toStr(-12345L) == "18446744073709539271")
    assertTrue(toStr(unsignedMaxValue) == unsignedMaxValueText)
  }

  @Test def testEquals(): Unit = {
    assertTrue(new Long(0) == new Long(0))
    assertTrue(new Long(1) == new Long(1))
    assertTrue(new Long(-1) == new Long(-1))
    assertTrue(new Long(123) == new Long(123))
    assertTrue(new Long(Long.MAX_VALUE) == new Long(Long.MAX_VALUE))
    assertTrue(new Long(Long.MIN_VALUE) == new Long(Long.MIN_VALUE))
  }

  @Test def highestOneBit(): Unit = {
    assertTrue(Long.highestOneBit(1) == 1L)
    assertTrue(Long.highestOneBit(2) == 2L)
    assertTrue(Long.highestOneBit(3) == 2L)
    assertTrue(Long.highestOneBit(4) == 4L)
    assertTrue(Long.highestOneBit(5) == 4L)
    assertTrue(Long.highestOneBit(6) == 4L)
    assertTrue(Long.highestOneBit(7) == 4L)
    assertTrue(Long.highestOneBit(8) == 8L)
    assertTrue(Long.highestOneBit(9) == 8L)
    assertTrue(Long.highestOneBit(63) == 32L)
    assertTrue(Long.highestOneBit(64) == 64L)
    assertTrue(Long.highestOneBit(Int.MaxValue) == 1073741824)
    assertTrue(Long.highestOneBit(Int.MaxValue + 1L) == 2147483648L)
  }
}
