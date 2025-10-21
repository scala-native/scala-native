package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ShortTest {
  val signedMaxValue = Short.MAX_VALUE
  val signedMaxValueText = "32767"
  val signedMinValue = Short.MIN_VALUE
  val signedMinValueText = "-32768"

  val signedMaxPlusOneText = "32768"
  val signedMinMinusOneText = "-32769"

  def assertThrowsAndMessage[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U
  )(expectedMsg: String): Unit = {
    assertThrows(expectedMsg, expectedThrowable, code)
  }

  @Test def decodeTest(): Unit = {
    import Short.decode

    assertEquals(-1.toShort, decode("-1"))
    assertEquals(1.toShort, decode("+1"))
    assertEquals(1.toShort, decode("1"))
    assertEquals(-123.toShort, decode("-123"))
    assertEquals(123.toShort, decode("+123"))
    assertEquals(123.toShort, decode("123"))
    assertEquals(0.toShort, decode("-0"))
    assertEquals(0.toShort, decode("+0"))
    assertEquals(0.toShort, decode("00"))
    assertEquals(-1.toShort, decode("-0x1"))
    assertEquals(1.toShort, decode("+0x1"))
    assertEquals(1.toShort, decode("0x1"))
    assertEquals(-123.toShort, decode("-0x7b"))
    assertEquals(123.toShort, decode("+0x7b"))
    assertEquals(123.toShort, decode("0x7b"))
    assertEquals(0.toShort, decode("-0x0"))
    assertEquals(0.toShort, decode("+0x0"))
    assertEquals(0.toShort, decode("0x0"))
    assertEquals(-1.toShort, decode("-0X1"))
    assertEquals(1.toShort, decode("+0X1"))
    assertEquals(1.toShort, decode("0X1"))
    assertEquals(-123.toShort, decode("-0X7B"))
    assertEquals(123.toShort, decode("+0X7B"))
    assertEquals(123.toShort, decode("0X7b"))
    assertEquals(0.toShort, decode("-0X0"))
    assertEquals(0.toShort, decode("+0X0"))
    assertEquals(0.toShort, decode("0X0"))
    assertEquals(-1.toShort, decode("-#1"))
    assertEquals(1.toShort, decode("+#1"))
    assertEquals(1.toShort, decode("#1"))
    assertEquals(-123.toShort, decode("-#7B"))
    assertEquals(123.toShort, decode("+#7B"))
    assertEquals(123.toShort, decode("#7b"))
    assertEquals(0.toShort, decode("-#0"))
    assertEquals(0.toShort, decode("+#0"))
    assertEquals(0.toShort, decode("#0"))
    assertEquals(-1.toShort, decode("-01"))
    assertEquals(1.toShort, decode("+01"))
    assertEquals(1.toShort, decode("01"))
    assertEquals(-123.toShort, decode("-0173"))
    assertEquals(123.toShort, decode("+0173"))
    assertEquals(123.toShort, decode("0173"))
    assertEquals(0.toShort, decode("-00"))
    assertEquals(0.toShort, decode("+00"))
    assertEquals(signedMaxValue.toShort, decode(signedMaxValueText))
    assertEquals(signedMinValue.toShort, decode(signedMinValueText))

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
      decode(signedMaxPlusOneText)
    )(
      s"""java.lang.NumberFormatException: Value $signedMaxPlusOneText out of range from input $signedMaxPlusOneText"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      decode(signedMinMinusOneText)
    )(
      s"""java.lang.NumberFormatException: Value $signedMinMinusOneText out of range from input $signedMinMinusOneText"""
    )
  }

  @Test def parseShort(): Unit = {
    import Short.{parseShort => parse}

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
      parse(signedMaxPlusOneText)
    )(
      s"""java.lang.NumberFormatException: Value out of range. Value:"$signedMaxPlusOneText" Radix:10"""
    )
    assertThrowsAndMessage(
      classOf[NumberFormatException],
      parse(signedMinMinusOneText)
    )(
      s"""java.lang.NumberFormatException: Value out of range. Value:"$signedMinMinusOneText" Radix:10"""
    )
  }

  @Test def testToString(): Unit = {
    import java.lang.Short.{toString => toStr}

    assertEquals("0", toStr(0.toShort))
    assertEquals("1", toStr(1.toShort))
    assertEquals("12", toStr(12.toShort))
    assertEquals("123", toStr(123.toShort))
    assertEquals("1234", toStr(1234.toShort))
    assertEquals("12345", toStr(12345.toShort))
    assertEquals("10", toStr(10.toShort))
    assertEquals("100", toStr(100.toShort))
    assertEquals("1000", toStr(1000.toShort))
    assertEquals("10000", toStr(10000.toShort))
    assertEquals("-1", toStr(-1.toShort))
    assertEquals("-12", toStr(-12.toShort))
    assertEquals("-123", toStr(-123.toShort))
    assertEquals("-1234", toStr(-1234.toShort))
    assertEquals("-12345", toStr(-12345.toShort))
    assertEquals(signedMaxValueText, toStr(signedMaxValue))
    assertEquals(signedMinValueText, toStr(signedMinValue))
  }

  @deprecated @Test def testEquals(): Unit = {
    assertEquals(new Short(0.toShort), new Short(0.toShort))
    assertEquals(new Short(1.toShort), new Short(1.toShort))
    assertEquals(new Short(-1.toShort), new Short(-1.toShort))
    assertEquals(new Short(123.toShort), new Short(123.toShort))
    assertEquals(new Short(Short.MAX_VALUE), new Short(Short.MAX_VALUE))
    assertEquals(new Short(Short.MIN_VALUE), new Short(Short.MIN_VALUE))
  }
}
