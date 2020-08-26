package java.lang

/** Test suite for [[java.lang.Character]]
 *
 * To be consistent the implementations should be based on
 * Unicode 7.0.
 * @see [[http://www.unicode.org/Public/7.0.0 Unicode 7.0]]
 *
 * Overall code point range U+0000 - U+D7FF and U+E000 - U+10FFF.
 * Surrogate code points are in the gap and U+FFFF
 * is the max value for [[scala.Char]].
 */
object CharacterSuite extends tests.Suite {
  import java.lang.Character._

  // codePointAt tests
  test("codePointAt - invalid values") {
    val str1 = "<invalid values>"
    val arr1 = str1.toArray

    assertThrows[java.lang.NullPointerException] {
      Character.codePointAt(null.asInstanceOf[Array[Char]], 1, arr1.length)
    }

    assertThrows[java.lang.NullPointerException] {
      Character.codePointAt(null.asInstanceOf[Array[Char]], 1, arr1.length)
    }

    assertThrows[java.lang.NullPointerException] {
      Character.codePointAt(null.asInstanceOf[Array[Char]], 2)
    }

    assertThrows[java.lang.NullPointerException] {
      Character.codePointAt(null.asInstanceOf[CharSequence], 2)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointAt(arr1, -1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointAt(str1, -1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointAt(arr1, 2, arr1.length + 1)
    }
  }

  test("codePointAt - Array[Char]") {
    val arr1 = "abcdEfghIjklMnoPqrsTuvwXyz".toArray

    val result   = Character.codePointAt(arr1, 3, arr1.length)
    val expected = 100 // 'd'
    assert(result == expected, s"result: $result != expected: $expected")
  }

  test("codePointAt - CharSeq") {
    val charSeq1: CharSequence = "abcdEfghIjklMnoPqrsTuvwXyz"

    val result   = Character.codePointAt(charSeq1, 8)
    val expected = 73 // 'I'
    assert(result == expected, s"result: $result != expected: $expected")
  }

  test("codePointAt - Array[Char],CharSeq return same non-ASCII value") {
    val str1  = "30\u20ac" // 'euro-character'
    val index = str1.length - 1

    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0x20AC // 'euro-character'

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointAt - low surrogate at beginning of line") {
    val str1     = "\uDC00eol" // Character.MIN_LOW_SURROGATE
    val index    = 0
    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0xDC00 // Character.MIN_LOW_SURROGATE, 56320 decimal

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointAt - high surrogate at end of line") {
    val str1  = "eol\uDBFF" // Character.MAX_HIGH_SURROGATE
    val index = str1.length - 1

    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0xDBFF // Character.MAX_HIGH_SURROGATE, 56319 decimal

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointAt - surrogate pair") {
    // Character.MIN_HIGH_SURROGATE followed by Character.MAX_LOW_SURROGATE
    val str1  = "before \uD800\uDFFF after"
    val index = 7

    val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
    val resultCS = Character.codePointAt(str1, index)
    val expected = 0x103FF // surrogate pair, decimal 66559

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointAt - high-non-high surrogate") {
    val str1    = "a\uDBFFb\uDBFFc"
    val indexes = Seq(1, 3)
    indexes.foreach { index =>
      val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
      val resultCS = Character.codePointAt(str1, index)
      val expected = 0xDBFF

      assert(resultCA == resultCS,
             s"resultCA: $resultCA != resultCS: $resultCS")
      assert(resultCA == expected,
             s"resultCA: $resultCA != expected: $expected")
    }
  }

  test("codePointAt - low-non-low surrogate") {
    val str1    = "a\uDC00b\uDC00c"
    val indexes = Seq(1, 3)
    indexes.foreach { index =>
      val resultCA = Character.codePointAt(str1.toArray, index, str1.length)
      val resultCS = Character.codePointAt(str1, index)
      val expected = 0xDC00

      assert(resultCA == resultCS,
             s"resultCA: $resultCA != resultCS: $resultCS")
      assert(resultCA == expected,
             s"resultCA: $resultCA != expected: $expected")
    }
  }

  // codePointBefore tests

  test("codePointBefore - invalid values") {
    val str1 = "<invalid values>"
    val arr1 = str1.toArray

    assertThrows[java.lang.NullPointerException] {
      Character.codePointBefore(null.asInstanceOf[Array[Char]], 1)
    }

    assertThrows[java.lang.NullPointerException] {
      Character.codePointBefore(null.asInstanceOf[CharSequence], 1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointBefore(arr1, -1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointBefore(str1, -1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointBefore(arr1, 0)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.codePointBefore(str1, 0)
    }

  }

  test("codePointBefore - Array[Char]") {
    val arr1  = "abcdEfghIjklMnopQrstUvwxYz".toArray
    val index = 10

    val result   = Character.codePointBefore(arr1, index)
    val expected = 106 // 'j'

    assert(result == expected, s"result: $result != expected: $expected")
  }

  test("codePointBefore - CharSeq") {
    val str1  = "abcdEfghIjklMnoPqrsTuvwXyz"
    val index = str1.length - 1

    val result   = Character.codePointBefore(str1, index)
    val expected = 121 // 'y'

    assert(result == expected, s"result: $result != expected: $expected")
  }

  test("codePointBefore - Array[Char], CharSeq return same non-ASCII value") {
    val str1  = "bugsabound\u03bb" // Greek small letter lambda
    val index = str1.length

    val resultCA = Character.codePointBefore(str1.toArray, index)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 955 // Greek snall letter lambda

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointBefore - low surrogate at beginning of line") {
    val str1     = "\uDC00eol" // Character.MIN_LOW_SURROGATE
    val index    = 1
    val resultCA = Character.codePointBefore(str1.toArray, index)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 0xDC00 // Character.MIN_LOW_SURROGATE, 56320 decimal

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointBefore - high surrogate at end of line") {
    val str1  = "eol\uDBFF" // Character.MAX_HIGH_SURROGATE
    val index = str1.length

    val resultCA = Character.codePointBefore(str1.toArray, index)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 0xDBFF // Character.MAX_HIGH_SURROGATE, 56319 decimal

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointBefore - surrogate pair") {
    // Character.MIN_HIGH_SURROGATE followed by Character.MAX_LOW_SURROGATE
    val str1  = "Denali\uD800\uDFFF"
    val index = str1.length

    val resultCA = Character.codePointBefore(str1.toArray, index, str1.length)
    val resultCS = Character.codePointBefore(str1, index)
    val expected = 0x103FF // surrogate pair, decimal 66559

    assert(resultCA == resultCS, s"resultCA: $resultCA != resultCS: $resultCS")
    assert(resultCA == expected, s"resultCA: $resultCA != expected: $expected")
  }

  test("codePointBefore - high-non-high surrogate") {
    val str1    = "a\uDBFFb\uDBFFc"
    val indexes = Seq(2, 4)
    indexes.foreach { index =>
      val resultCA = Character.codePointBefore(str1.toArray, index)
      val resultCS = Character.codePointBefore(str1, index)
      val expected = 0xDBFF

      assert(resultCA == resultCS,
             s"resultCA: $resultCA != resultCS: $resultCS")
      assert(resultCA == expected,
             s"resultCA: $resultCA != expected: $expected")
    }
  }

  test("codePointBefore - low-non-low surrogate") {
    val str1    = "a\uDC00b\uDC00c"
    val indexes = Seq(2, 4)
    indexes.foreach { index =>
      val resultCA = Character.codePointBefore(str1.toArray, index)
      val resultCS = Character.codePointBefore(str1, index)
      val expected = 0xDC00

      assert(resultCA == resultCS,
             s"resultCA: $resultCA != resultCS: $resultCS")
      assert(resultCA == expected,
             s"resultCA: $resultCA != expected: $expected")
    }
  }

  test("codePointCount") {
    val data     = "Mt. Whitney".toArray[scala.Char]
    val offset   = 1
    val expected = data.size - offset
    val result   = Character.codePointCount(data, offset, expected)

    assert(result == expected, s"result: $result != expected: $expected")
  }

  // Ported, with gratitude & possibly modifications
  // from ScalaJs CharacterTest.scala
  // https://github.com/scala-js/scala-js/blob/master/
  //         test-suite/shared/src/test/scala/org/scalajs/testsuite/
  //         javalib/lang/CharacterTest.scala

  test("digit") {

    // method test() in ScalaJS, but that name already in use in test Suite.
    // To assay is, roughly "to judge the worth of" or "try, attempt".

    def assay(expected: Int, codePoint: Int): Unit = {
      assertEquals(expected, Character.digit(codePoint, MAX_RADIX))
      if (codePoint <= Char.MaxValue)
        assertEquals(expected, Character.digit(codePoint.toChar, MAX_RADIX))

      if (expected != -1) {
        assertEquals(
          expected,
          Character.digit(codePoint, Math.max(expected + 1, MIN_RADIX)))

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
    assay(-1, '}')
    assay(-1, -4)
    assay(-1, 0xffffff)
    assay(-1, '0' - 1)
    assay(-1, '9' + 1)
    assay(-1, 'A' - 1)
    assay(-1, 'Z' + 1)
    assay(-1, 'a' - 1)
    assay(-1, 'z' + 1)
    assay(-1, 0xff20)
    assay(-1, 0xff3b)
    assay(-1, 0xff40)
    assay(-1, 0xff5b)
    assay(-1, 0xbe5)
    assay(-1, 0xbf0)
    assay(-1, 0x11065)
    assay(-1, 0x11070)
    assay(-1, Int.MinValue)
    assay(-1, Int.MaxValue)

    // Every single valid digit

// format: off
    val All0s = Array[Int]('0', 0x660, 0x6f0, 0x7c0, 0x966, 0x9e6, 0xa66,
        0xae6, 0xb66, 0xbe6, 0xc66, 0xce6, 0xd66, 0xe50, 0xed0, 0xf20, 0x1040,
        0x1090, 0x17e0, 0x1810, 0x1946, 0x19d0, 0x1a80, 0x1a90, 0x1b50, 0x1bb0,
        0x1c40, 0x1c50, 0xa620, 0xa8d0, 0xa900, 0xa9d0, 0xaa50, 0xabf0, 0xff10,
        0x104a0, 0x11066, 0x110f0, 0x11136, 0x111d0, 0x116c0, 0x1d7ce, 0x1d7d8,
        0x1d7e2, 0x1d7ec, 0x1d7f6)
// format: on

    for {
      zero   <- All0s
      offset <- 0 to 9
    } {
      assay(offset, zero + offset)
    }

    val AllAs = Array[Int]('A', 'a', 0xff21, 0xff41)

    for {
      a      <- AllAs
      offset <- 0 to 25
    } {
      assay(10 + offset, a + offset)
    }
  }

  test("offsetByCodePoints - invalid values") {
    val str1 = "<bad args>"
    val arr1 = str1.toArray

    assertThrows[java.lang.NullPointerException] {
      Character.offsetByCodePoints(null.asInstanceOf[Array[Char]],
                                   1,
                                   arr1.length,
                                   0,
                                   0)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.offsetByCodePoints(arr1, -1, arr1.length, 0, 0)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.offsetByCodePoints(arr1, 0, -1, 0, 0)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.offsetByCodePoints(arr1, 1, arr1.length, 2, 0)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.offsetByCodePoints(arr1, 2, arr1.length, 1, 0)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      Character.offsetByCodePoints(arr1, 2, arr1.length, arr1.length + 1, 0)
    }

  }

  test("toLowerCase-low") {
    // low chars
    assert(toLowerCase('\n') equals '\n')
  }

  test("toLowerCase-ascii") {
    // ascii chars
    assert(toLowerCase('A') equals 'a')
    assert(toLowerCase('a') equals 'a')
    assertNot(toLowerCase('a') equals 'A')
    assert(toLowerCase('F') equals 'f')
    assert(toLowerCase('Z') equals 'z')
  }

  test("toLowerCase-compat") {
    // compat characters are directly from the DB
    // (03F4,GREEK CAPITAL THETA SYMBOL,Lu,0,L,<compat> 0398,N,,03B8,)
    assert(toLowerCase(0x03F4) equals 0x03B8)
    assert(toLowerCase('Θ') equals 'θ')
    // (2161,ROMAN NUMERAL TWO,Nl,0,L,<compat> 0049 0049,N,,2171,)
    assert(toLowerCase(0x2161) equals 0x2171)
    // check lower to lower
    assert(toLowerCase('µ') equals 'µ')
  }

  test("toLowerCase-alt") {
    // alternating upper and lower case
    // (256,257,-1,0)(302,303,-1,2)
    assert(toLowerCase(256) equals 257)
    assert(toLowerCase(257) equals 257)
    assert(toLowerCase(258) equals 259)
    assert(toLowerCase(302) equals 303)
  }

  test("toLowerCase-high") {
    // high points
    assert(toLowerCase(65313) equals 65345)
    assert(toLowerCase(65338) equals 65370)
    assert(toLowerCase(65339) equals 65339)
  }

  test("toLowerCase-above") {
    // top and above range
    assert(toLowerCase(0x10FFFF) equals 0x10FFFF)
    assert(toLowerCase(0x110000) equals 0x110000)
  }

  test("toUpperCase-low") {
    // low chars
    assert(toUpperCase('\n') equals '\n')
  }

  test("toUpperCase-ascii") {
    // ascii chars
    assert(toUpperCase('a') equals 'A')
    assert(toUpperCase('A') equals 'A')
    assertNot(toUpperCase('A') equals 'a')
    assert(toUpperCase('f') equals 'F')
    assert(toUpperCase('z') equals 'Z')

  }

  test("toUpperCase-compat") {
    // compat characters are directly from the DB
    // (03D0,GREEK BETA SYMBOL,Ll,0,L,<compat> 03B2,N,0392,,0392)
    assert(toUpperCase(0x03D0) equals 0x0392)
    assert(toUpperCase('β') equals 'Β')
    // (00B5,MICRO SIGN,Ll,0,L,<compat> 03BC,N,039C,,039C)
    assert(toUpperCase(0x00B5) equals 0x039C)
    assert(toUpperCase('μ') equals 'Μ')
  }

  test("toUpperCase-alt") {
    // alternating upper and lower case
    // (257,256,1,0)(303,302,1,2)
    assert(toUpperCase(257) equals 256)
    assert(toUpperCase(258) equals 258)
    assert(toUpperCase(259) equals 258)
    assert(toUpperCase(303) equals 302)
  }

  test("toUpperCase-high") {
    // high points
    // (65345,65313,32,0)(65370,65338,32,1)
    // (66600,66560,40,0)(66639,66599,40,1)
    // (71872,71840,32,0)(71903,71871,32,1)
    assert(toUpperCase(65345) equals 65313)
    assert(toUpperCase(65370) equals 65338)
    assert(toUpperCase(66600) equals 66560)
  }

  test("toUpperCase-above") {
    // top and above range
    assert(toUpperCase(0x10FFFF) equals 0x10FFFF)
    assert(toUpperCase(0x110000) equals 0x110000)
  }

  test("UnicodeBlock.of") {
    assert(UnicodeBlock.of('a') equals UnicodeBlock.BASIC_LATIN)
    assert(UnicodeBlock.of('א') equals UnicodeBlock.HEBREW)
  }
}
