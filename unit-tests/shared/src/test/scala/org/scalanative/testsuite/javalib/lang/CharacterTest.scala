package org.scalanative.testsuite.javalib.lang

import java.lang._

/** Test suite for [[java.lang.Character]]
 *
 *  To be consistent the implementations should be based on Unicode 7.0.
 *  @see
 *    [[http://www.unicode.org/Public/7.0.0 Unicode 7.0]]
 *
 *  Overall code point range U+0000 - U+D7FF and U+E000 - U+10FFF. Surrogate
 *  code points are in the gap and U+FFFF is the max value for [[scala.Char]].
 */
import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharacterTest {
  import java.lang.Character._

  // codePointAt tests
  @Test def codePointAtInvalidValues(): Unit = {
    val str1 = "<invalid values>"
    val arr1 = str1.toArray

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.codePointAt(null.asInstanceOf[Array[Char]], 1, arr1.length)
    )

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.codePointAt(null.asInstanceOf[Array[Char]], 1, arr1.length)
    )

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.codePointAt(null.asInstanceOf[Array[Char]], 2)
    )

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.codePointAt(null.asInstanceOf[CharSequence], 2)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointAt(arr1, -1)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointAt(str1, -1)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointAt(arr1, 2, arr1.length + 1)
    )
  }

  @Test def codePointAtArrayChar(): Unit = {
    val arr1 = "abcdEfghIjklMnoPqrsTuvwXyz".toArray

    val result = Character.codePointAt(arr1, 3, arr1.length)
    val expected = 100 // 'd'
    assertTrue(s"result: $result != expected: $expected", result == expected)
  }

  @Test def codePointAtCharSeq(): Unit = {
    val charSeq1: CharSequence = "abcdEfghIjklMnoPqrsTuvwXyz"

    val result = Character.codePointAt(charSeq1, 8)
    val expected = 73 // 'I'
    assertTrue(s"result: $result != expected: $expected", result == expected)
  }

  @Test def codePointAtArrayCharCharSeqReturnSameNonAsciiValue(): Unit = {
    val str1 = "30\u20ac" // 'euro-character'
    val index = str1.length - 1

    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0x20ac // 'euro-character'

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointAtLowSurrogateAtBeginningOfLine(): Unit = {
    val str1 = "\uDC00eol" // Character.MIN_LOW_SURROGATE
    val index = 0
    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0xdc00 // Character.MIN_LOW_SURROGATE, 56320 decimal

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointAtHighSurrogateAtEndOfLine(): Unit = {
    val str1 = "eol\uDBFF" // Character.MAX_HIGH_SURROGATE
    val index = str1.length - 1

    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0xdbff // Character.MAX_HIGH_SURROGATE, 56319 decimal

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointAtSurrogatePair(): Unit = {
    // Character.MIN_HIGH_SURROGATE followed by Character.MAX_LOW_SURROGATE
    val str1 = "before \uD800\uDFFF after"
    val index = 7

    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0x103ff // surrogate pair, decimal 66559

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointAtHighNonHighSurrogate(): Unit = {
    val str1 = "a\uDBFFb\uDBFFc"
    val indexes = Seq(1, 3)
    indexes.foreach { index =>
      val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
      val resultCS = Character.codePointAt(str1, index)
      val expected = 0xdbff

      assertTrue(
        s"resultCA: $resultCA != resultCS: $resultCS",
        resultCA == resultCS
      )
      assertTrue(
        s"resultCA: $resultCA != expected: $expected",
        resultCA == expected
      )
    }
  }

  @Test def codePointAtLowNonLowSurrogate(): Unit = {
    val str1 = "a\uDC00b\uDC00c"
    val indexes = Seq(1, 3)
    indexes.foreach { index =>
      val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
      val resultCS = Character.codePointAt(str1, index)
      val expected = 0xdc00

      assertTrue(
        s"resultCA: $resultCA != resultCS: $resultCS",
        resultCA == resultCS
      )
      assertTrue(
        s"resultCA: $resultCA != expected: $expected",
        resultCA == expected
      )
    }
  }

  // codePointBefore tests

  @Test def codePointBeforeInvalidValues(): Unit = {
    val str1 = "<invalid values>"
    val arr1 = str1.toArray

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.codePointBefore(null.asInstanceOf[Array[Char]], 1)
    )

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.codePointBefore(null.asInstanceOf[CharSequence], 1)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointBefore(arr1, -1)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointBefore(str1, -1)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointBefore(arr1, 0)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.codePointBefore(str1, 0)
    )

  }

  @Test def codePointBeforeArrayChar(): Unit = {
    val arr1 = "abcdEfghIjklMnopQrstUvwxYz".toArray
    val index = 10

    val result = Character.codePointBefore(arr1, index)
    val expected = 106 // 'j'

    assertTrue(s"result: $result != expected: $expected", result == expected)
  }

  @Test def codePointBeforeCharSeq(): Unit = {
    val str1 = "abcdEfghIjklMnoPqrsTuvwXyz"
    val index = str1.length - 1

    val result = Character.codePointBefore(str1, index)
    val expected = 121 // 'y'

    assertTrue(s"result: $result != expected: $expected", result == expected)
  }

  @Test def codePointBeforeArrayCharCharSeqReturnSamNonAsciiValue(): Unit = {
    val str1 = "bugsabound\u03bb" // Greek small letter lambda
    val index = str1.length

    val resultCA = Character.codePointBefore(str1.toArray, index)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 955 // Greek snall letter lambda

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointBeforeLowSurrogateAtBeginningOfLine(): Unit = {
    val str1 = "\uDC00eol" // Character.MIN_LOW_SURROGATE
    val index = 1
    val resultCA = Character.codePointBefore(str1.toArray, index)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 0xdc00 // Character.MIN_LOW_SURROGATE, 56320 decimal

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointBeforeHighSurrogateAtEndOfLine(): Unit = {
    val str1 = "eol\uDBFF" // Character.MAX_HIGH_SURROGATE
    val index = str1.length

    val resultCA = Character.codePointBefore(str1.toArray, index)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 0xdbff // Character.MAX_HIGH_SURROGATE, 56319 decimal

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointBeforeSurrogatePair(): Unit = {
    // Character.MIN_HIGH_SURROGATE followed by Character.MAX_LOW_SURROGATE
    val str1 = "Denali\uD800\uDFFF"
    val index = str1.length

    val resultCA = Character.codePointBefore(str1.toArray, index, str1.length)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 0x103ff // surrogate pair, decimal 66559

    assertTrue(
      s"resultCA: $resultCA != resultCS: $resultCS",
      resultCA == resultCS
    )
    assertTrue(
      s"resultCA: $resultCA != expected: $expected",
      resultCA == expected
    )
  }

  @Test def codePointBeforeHighNonHighSurrogate(): Unit = {
    val str1 = "a\uDBFFb\uDBFFc"
    val indexes = Seq(2, 4)
    indexes.foreach { index =>
      val resultCA = Character.codePointBefore(str1.toArray, index)
      val resultCS = Character.codePointBefore(str1, index)
      val expected = 0xdbff

      assertTrue(
        s"resultCA: $resultCA != resultCS: $resultCS",
        resultCA == resultCS
      )
      assertTrue(
        s"resultCA: $resultCA != expected: $expected",
        resultCA == expected
      )
    }
  }

  @Test def codePointBeforeLowNonLowSurrogate(): Unit = {
    val str1 = "a\uDC00b\uDC00c"
    val indexes = Seq(2, 4)
    indexes.foreach { index =>
      val resultCA = Character.codePointBefore(str1.toArray, index)
      val resultCS = Character.codePointBefore(str1, index)
      val expected = 0xdc00

      assertTrue(
        s"resultCA: $resultCA != resultCS: $resultCS",
        resultCA == resultCS
      )
      assertTrue(
        s"resultCA: $resultCA != expected: $expected",
        resultCA == expected
      )
    }
  }

  @Test def codePointCount(): Unit = {
    val data = "Mt. Whitney".toArray[scala.Char]
    val offset = 1
    val expected = data.size - offset
    val result = Character.codePointCount(data, offset, expected)

    assertTrue(s"result: $result != expected: $expected", result == expected)
  }

  // Ported, with gratitude & possibly modifications
  // from ScalaJs CharacterTest.scala
  // https://github.com/scala-js/scala-js/blob/master/
  //         test-suite/shared/src/test/scala/org/scalajs/testsuite/
  //         javalib/lang/CharacterTest.scala

  @Test def digit(): Unit = {

    def test(expected: Int, codePoint: Int): Unit = {
      assertEquals(expected, Character.digit(codePoint, MAX_RADIX))
      if (codePoint <= Char.MaxValue)
        assertEquals(expected, Character.digit(codePoint.toChar, MAX_RADIX))

      if (expected != -1) {
        assertEquals(
          expected,
          Character.digit(codePoint, Math.max(expected + 1, MIN_RADIX))
        )

        if (expected >= MIN_RADIX)
          assertEquals(-1, Character.digit(codePoint, expected))
      }
    }

    // Invalid radix

    assertEquals(-1, Character.digit('0', MIN_RADIX - 1))
    assertEquals(-1, Character.digit('0', MAX_RADIX + 1))
    assertEquals(-1, Character.digit('0', -1))

    assertEquals(-1, Character.digit('0'.toInt, MIN_RADIX - 1))
    assertEquals(-1, Character.digit('0'.toInt, MAX_RADIX + 1))
    assertEquals(-1, Character.digit('0'.toInt, -1))

    // A few invalid digits
    test(-1, '}')
    test(-1, -4)
    test(-1, 0xffffff)
    test(-1, '0' - 1)
    test(-1, '9' + 1)
    test(-1, 'A' - 1)
    test(-1, 'Z' + 1)
    test(-1, 'a' - 1)
    test(-1, 'z' + 1)
    test(-1, 0xff20)
    test(-1, 0xff3b)
    test(-1, 0xff40)
    test(-1, 0xff5b)
    test(-1, 0xbe5)
    test(-1, 0xbf0)
    test(-1, 0x11065)
    test(-1, 0x11070)
    test(-1, Int.MinValue)
    test(-1, Int.MaxValue)

    // Every single valid digit

    val All0s =
      Array[Int]('0', 0x660, 0x6f0, 0x7c0, 0x966, 0x9e6, 0xa66, 0xae6, 0xb66,
        0xbe6, 0xc66, 0xce6, 0xd66, 0xe50, 0xed0, 0xf20, 0x1040, 0x1090, 0x17e0,
        0x1810, 0x1946, 0x19d0, 0x1a80, 0x1a90, 0x1b50, 0x1bb0, 0x1c40, 0x1c50,
        0xa620, 0xa8d0, 0xa900, 0xa9d0, 0xaa50, 0xabf0, 0xff10, 0x104a0,
        0x11066, 0x110f0, 0x11136, 0x111d0, 0x116c0, 0x1d7ce, 0x1d7d8, 0x1d7e2,
        0x1d7ec, 0x1d7f6)

    for {
      zero <- All0s
      offset <- 0 to 9
    } {
      test(offset, zero + offset)
    }

    val AllAs = Array[Int]('A', 'a', 0xff21, 0xff41)

    for {
      a <- AllAs
      offset <- 0 to 25
    } {
      test(10 + offset, a + offset)
    }
  }

  @Test def offsetByCodePointsInvalidValues(): Unit = {
    val str1 = "<bad args>"
    val arr1 = str1.toArray

    assertThrows(
      classOf[java.lang.NullPointerException],
      Character.offsetByCodePoints(
        null.asInstanceOf[Array[Char]],
        1,
        arr1.length,
        0,
        0
      )
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.offsetByCodePoints(arr1, -1, arr1.length, 0, 0)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.offsetByCodePoints(arr1, 0, -1, 0, 0)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.offsetByCodePoints(arr1, 1, arr1.length, 2, 0)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.offsetByCodePoints(arr1, 2, arr1.length, 1, 0)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      Character.offsetByCodePoints(arr1, 2, arr1.length, arr1.length + 1, 0)
    )

  }

  @Test def isLowerCase(): Unit = {
    assertTrue(Character.isLowerCase('a'))
    assertTrue(Character.isLowerCase('z'))
    assertFalse(Character.isLowerCase('A'))
    assertFalse(Character.isLowerCase(-1))
  }

  @Test def toLowerCaseLow(): Unit = {
    // low chars
    assertTrue(toLowerCase('\n') equals '\n')
  }

  @Test def toLowerCaseAscii(): Unit = {
    // ascii chars
    assertTrue(toLowerCase('A') equals 'a')
    assertTrue(toLowerCase('a') equals 'a')
    assertFalse(toLowerCase('a') equals 'A')
    assertTrue(toLowerCase('F') equals 'f')
    assertTrue(toLowerCase('Z') equals 'z')
  }

  @Test def toLowerCaseCompat(): Unit = {
    // compat characters are directly from the DB
    // (03F4,GREEK CAPITAL THETA SYMBOL,Lu,0,L,<compat> 0398,N,,03B8,)
    assertTrue(toLowerCase(0x03f4) equals 0x03b8)
    assertTrue(toLowerCase('Θ') equals 'θ')
    // (2161,ROMAN NUMERAL TWO,Nl,0,L,<compat> 0049 0049,N,,2171,)
    assertTrue(toLowerCase(0x2161) equals 0x2171)
    // check lower to lower
    assertTrue(toLowerCase('µ') equals 'µ')
  }

  @Test def toLowerCaseAlt(): Unit = {
    // alternating upper and lower case
    // (256,257,-1,0)(302,303,-1,2)
    assertTrue(toLowerCase(256) equals 257)
    assertTrue(toLowerCase(257) equals 257)
    assertTrue(toLowerCase(258) equals 259)
    assertTrue(toLowerCase(302) equals 303)
  }

  @Test def toLowerCaseHigh(): Unit = {
    // high points
    assertTrue(toLowerCase(65313) equals 65345)
    assertTrue(toLowerCase(65338) equals 65370)
    assertTrue(toLowerCase(65339) equals 65339)
  }

  @Test def toLowerCaseAbove(): Unit = {
    // top and above range
    assertTrue(toLowerCase(0x10ffff) equals 0x10ffff)
    assertTrue(toLowerCase(0x110000) equals 0x110000)
  }

  @Test def toUpperCaseLow(): Unit = {
    // low chars
    assertTrue(toUpperCase('\n') equals '\n')
  }

  @Test def toUpperCaseAscii(): Unit = {
    // ascii chars
    assertTrue(toUpperCase('a') equals 'A')
    assertTrue(toUpperCase('A') equals 'A')
    assertFalse(toUpperCase('A') equals 'a')
    assertTrue(toUpperCase('f') equals 'F')
    assertTrue(toUpperCase('z') equals 'Z')

  }

  @Test def toUpperCaseCompat(): Unit = {
    // compat characters are directly from the DB
    // (03D0,GREEK BETA SYMBOL,Ll,0,L,<compat> 03B2,N,0392,,0392)
    assertTrue(toUpperCase(0x03d0) equals 0x0392)
    assertTrue(toUpperCase('β') equals 'Β')
    // (00B5,MICRO SIGN,Ll,0,L,<compat> 03BC,N,039C,,039C)
    assertTrue(toUpperCase(0x00b5) equals 0x039c)
    assertTrue(toUpperCase('μ') equals 'Μ')
  }

  @Test def toUpperCaseAlt(): Unit = {
    // alternating upper and lower case
    // (257,256,1,0)(303,302,1,2)
    assertTrue(toUpperCase(257) equals 256)
    assertTrue(toUpperCase(258) equals 258)
    assertTrue(toUpperCase(259) equals 258)
    assertTrue(toUpperCase(303) equals 302)
  }

  @Test def toUpperCaseHigh(): Unit = {
    // high points
    // (65345,65313,32,0)(65370,65338,32,1)
    // (66600,66560,40,0)(66639,66599,40,1)
    // (71872,71840,32,0)(71903,71871,32,1)
    assertTrue(toUpperCase(65345) equals 65313)
    assertTrue(toUpperCase(65370) equals 65338)
    assertTrue(toUpperCase(66600) equals 66560)
  }

  @Test def toUpperCaseAbove(): Unit = {
    // top and above range
    assertTrue(toUpperCase(0x10ffff) equals 0x10ffff)
    assertTrue(toUpperCase(0x110000) equals 0x110000)
  }

  @Test def unicodeBlockOf(): Unit = {
    assertTrue(UnicodeBlock.of('a') equals UnicodeBlock.BASIC_LATIN)
    assertTrue(UnicodeBlock.of('א') equals UnicodeBlock.HEBREW)
  }

  // from scala-js tests
  @Test def highSurrogate(): Unit = {
    assertEquals(0xd800, Character.highSurrogate(0x10000))
    assertEquals(0xd808, Character.highSurrogate(0x12345))
    assertEquals(0xdbff, Character.highSurrogate(0x10ffff))

    // unspecified for non-supplementary code points
  }

  @Test def lowSurrogate(): Unit = {
    assertEquals(0xdc00, Character.lowSurrogate(0x10000))
    assertEquals(0xdf45, Character.lowSurrogate(0x12345))
    assertEquals(0xdfff, Character.lowSurrogate(0x10ffff))

    // unspecified for non-supplementary code points
  }

  @Test def isWhitespace(): Unit = {
    assertTrue(Character.isWhitespace(' '))
    assertTrue(Character.isWhitespace('\t'))
    assertTrue(Character.isWhitespace('\n'))
    assertTrue(Character.isWhitespace('\f'))
    assertTrue(Character.isWhitespace('\r'))
    assertTrue(Character.isWhitespace('\u001C')) // file separator
    assertTrue(Character.isWhitespace('\u001D')) // group separator
    assertTrue(Character.isWhitespace('\u001E')) // record separator
    assertTrue(Character.isWhitespace('\u001F')) // unit separator

    assertFalse(Character.isWhitespace('\b'))
    assertFalse(Character.isWhitespace('a'))
    // https://github.com/scala-native/scala-native/issues/3154
    assertFalse(Character.isWhitespace(-1))
  }
}
