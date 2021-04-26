package java.lang

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._
import scalanative.junit.utils.ThrowsHelper._

class ShortTest {
  val signedMaxValue     = Short.MAX_VALUE
  val signedMaxValueText = "32767"
  val signedMinValue     = Short.MIN_VALUE
  val signedMinValueText = "-32768"

  val signedMaxPlusOneText  = "32768"
  val signedMinMinusOneText = "-32769"

  @Test def decodeTest(): Unit = {
    import Short.decode

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
                    decode(signedMaxPlusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: Value $signedMaxPlusOneText out of range from input $signedMaxPlusOneText"""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    decode(signedMinMinusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: Value $signedMinMinusOneText out of range from input $signedMinMinusOneText"""
    }
  }

  @Test def parseShort(): Unit = {
    import Short.{parseShort => parse}

    assertTrue(parse("-1") == -1)
    assertTrue(parse("+1") == 1)
    assertTrue(parse("1") == 1)
    assertTrue(parse("-123") == -123)
    assertTrue(parse("+123") == 123)
    assertTrue(parse("123") == 123)
    assertTrue(parse("-100", 2) == -4)
    assertTrue(parse("+100", 2) == 4)
    assertTrue(parse("100", 2) == 4)
    assertTrue(parse("-0") == 0)
    assertTrue(parse("+0") == 0)
    assertTrue(parse("00") == 0)
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
    assertThrowsAnd(classOf[NumberFormatException], parse(signedMaxPlusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: Value out of range. Value:"$signedMaxPlusOneText" Radix:10"""
    }
    assertThrowsAnd(classOf[NumberFormatException],
                    parse(signedMinMinusOneText)) {
      _.toString == s"""java.lang.NumberFormatException: Value out of range. Value:"$signedMinMinusOneText" Radix:10"""
    }
  }

  @Test def testToString(): Unit = {
    import java.lang.Short.{toString => toStr}

    assertTrue(toStr(0.toShort) == "0")
    assertTrue(toStr(1.toShort) == "1")
    assertTrue(toStr(12.toShort) == "12")
    assertTrue(toStr(123.toShort) == "123")
    assertTrue(toStr(1234.toShort) == "1234")
    assertTrue(toStr(12345.toShort) == "12345")
    assertTrue(toStr(10.toShort) == "10")
    assertTrue(toStr(100.toShort) == "100")
    assertTrue(toStr(1000.toShort) == "1000")
    assertTrue(toStr(10000.toShort) == "10000")
    assertTrue(toStr(-1.toShort) == "-1")
    assertTrue(toStr(-12.toShort) == "-12")
    assertTrue(toStr(-123.toShort) == "-123")
    assertTrue(toStr(-1234.toShort) == "-1234")
    assertTrue(toStr(-12345.toShort) == "-12345")
    assertTrue(toStr(signedMaxValue) == signedMaxValueText)
    assertTrue(toStr(signedMinValue) == signedMinValueText)
  }

  @Test def testEquals(): Unit = {
    assertTrue(new Short(0.toShort) == new Short(0.toShort))
    assertTrue(new Short(1.toShort) == new Short(1.toShort))
    assertTrue(new Short(-1.toShort) == new Short(-1.toShort))
    assertTrue(new Short(123.toShort) == new Short(123.toShort))
    assertTrue(new Short(Short.MAX_VALUE) == new Short(Short.MAX_VALUE))
    assertTrue(new Short(Short.MIN_VALUE) == new Short(Short.MIN_VALUE))
  }
}
