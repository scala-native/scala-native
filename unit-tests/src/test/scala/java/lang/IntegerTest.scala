package java.lang

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class IntegerTest {
  val signedMaxValue     = Integer.MAX_VALUE
  val signedMaxValueText = "2147483647"
  val signedMinValue     = Integer.MIN_VALUE
  val signedMinValueText = "-2147483648"

  val signedMaxPlusOneText  = "2147483648"
  val signedMinMinusOneText = "-2147483649"

  val unsignedMaxValue       = -1
  val unsignedMaxValueText   = "4294967295"
  val unsignedMaxPlusOneText = "4294967296"

  @Test def parseInt(): Unit = {
    import Integer.{parseInt => parse}

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

    assertThrows(classOf[NumberFormatException], parse(null))
    assertThrows(classOf[NumberFormatException], parse("+"))
    assertThrows(classOf[NumberFormatException], parse("-"))
    assertThrows(classOf[NumberFormatException], parse(""))
    assertThrows(classOf[NumberFormatException],
                 parse("123", Character.MIN_RADIX - 1))
    assertThrows(classOf[NumberFormatException],
                 parse("123", Character.MAX_RADIX + 1))
    assertThrows(classOf[NumberFormatException], parse("123a", 10))
    assertThrows(classOf[NumberFormatException], parse(signedMinMinusOneText))
    assertThrows(classOf[NumberFormatException], parse(signedMaxPlusOneText))
  }

  @Test def parseUnsignedInt(): Unit = {
    import Integer.{parseUnsignedInt => parse}

    assertTrue(parse("1") == 1)
    assertTrue(parse("+1") == 1)
    assertTrue(parse("0") == 0)
    assertTrue(parse("00") == 0)
    assertTrue(parse("+100", 2) == 4)
    assertTrue(parse("100", 2) == 4)
    assertTrue(parse(unsignedMaxValueText) == unsignedMaxValue)

    assertThrows(classOf[NumberFormatException], parse(null))
    assertThrows(classOf[NumberFormatException], parse("+"))
    assertThrows(classOf[NumberFormatException], parse("-"))
    assertThrows(classOf[NumberFormatException], parse(""))
    assertThrows(classOf[NumberFormatException], parse("-1"))
    assertThrows(classOf[NumberFormatException],
                 parse("123", Character.MIN_RADIX - 1))
    assertThrows(classOf[NumberFormatException],
                 parse("123", Character.MAX_RADIX + 1))
    assertThrows(classOf[NumberFormatException], parse("123a", 10))
    assertThrows(classOf[NumberFormatException], parse(unsignedMaxPlusOneText))

    val octalMulOverflow = "137777777770"
    // in binary:
    // octalMulOverflow:  01011111111111111111111111111111000
    // max unsigned:      00011111111111111111111111111111111
    assertThrows(classOf[NumberFormatException], parse(octalMulOverflow, 8))
  }

  @Test def testToString(): Unit = {
    import java.lang.Integer.{toString => toStr}

    assertTrue(toStr(0) == "0")
    assertTrue(toStr(1) == "1")
    assertTrue(toStr(12) == "12")
    assertTrue(toStr(123) == "123")
    assertTrue(toStr(1234) == "1234")
    assertTrue(toStr(12345) == "12345")
    assertTrue(toStr(10) == "10")
    assertTrue(toStr(100) == "100")
    assertTrue(toStr(1000) == "1000")
    assertTrue(toStr(10000) == "10000")
    assertTrue(toStr(100000) == "100000")
    assertTrue(toStr(101010) == "101010")
    assertTrue(toStr(111111) == "111111")
    assertTrue(toStr(-1) == "-1")
    assertTrue(toStr(-12) == "-12")
    assertTrue(toStr(-123) == "-123")
    assertTrue(toStr(-1234) == "-1234")
    assertTrue(toStr(-12345) == "-12345")
    assertTrue(toStr(signedMaxValue) == signedMaxValueText)
    assertTrue(toStr(signedMinValue) == signedMinValueText)
  }

  @Test def toUnsignedString(): Unit = {
    import java.lang.Integer.{toUnsignedString => toStr}

    assertTrue(toStr(0) == "0")
    assertTrue(toStr(1) == "1")
    assertTrue(toStr(12) == "12")
    assertTrue(toStr(123) == "123")
    assertTrue(toStr(1234) == "1234")
    assertTrue(toStr(12345) == "12345")
    assertTrue(toStr(-1) == "4294967295")
    assertTrue(toStr(-12) == "4294967284")
    assertTrue(toStr(-123) == "4294967173")
    assertTrue(toStr(-1234) == "4294966062")
    assertTrue(toStr(-12345) == "4294954951")
    assertTrue(toStr(unsignedMaxValue) == unsignedMaxValueText)
  }

  @Test def testEquals(): Unit = {
    assertTrue(new Integer(0) == new Integer(0))
    assertTrue(new Integer(1) == new Integer(1))
    assertTrue(new Integer(-1) == new Integer(-1))
    assertTrue(new Integer(123) == new Integer(123))
    assertTrue(new Integer(Integer.MAX_VALUE) == new Integer(Integer.MAX_VALUE))
    assertTrue(new Integer(Integer.MIN_VALUE) == new Integer(Integer.MIN_VALUE))
  }

  @Test def highestOneBit(): Unit = {
    assertTrue(Integer.highestOneBit(1) == 1)
    assertTrue(Integer.highestOneBit(2) == 2)
    assertTrue(Integer.highestOneBit(3) == 2)
    assertTrue(Integer.highestOneBit(4) == 4)
    assertTrue(Integer.highestOneBit(5) == 4)
    assertTrue(Integer.highestOneBit(6) == 4)
    assertTrue(Integer.highestOneBit(7) == 4)
    assertTrue(Integer.highestOneBit(8) == 8)
    assertTrue(Integer.highestOneBit(9) == 8)
    assertTrue(Integer.highestOneBit(63) == 32)
    assertTrue(Integer.highestOneBit(64) == 64)
  }
}
