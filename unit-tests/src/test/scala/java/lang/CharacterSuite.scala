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

  def toInt(hex: String): Int = Integer.parseInt(hex, 16)

  /** toUpperCase/toLowerCase based on Unicode 7 case folding.
   * @see [[http://www.unicode.org/Public/7.0.0/ucd/CaseFolding.txt]]
   * The implementation with the Char argument forwards
   * to the implementation with the Int argument.
   * Most sequence ranges that step by two have alternating
   * upper and lowercase code points.
   */
  test("toLowerCase") {
    // low chars
    assert(toLowerCase('\n') equals '\n')
    // ascii chars
    assert(toLowerCase('A') equals 'a')
    assert(toLowerCase('a') equals 'a')
    assertNot(toLowerCase('a') equals 'A')
    assert(toLowerCase('F') equals 'f')
    assert(toLowerCase('Z') equals 'z')
    // compat characters don't agree with JDK
    assert(toLowerCase('µ') equals 'μ')

    /** The Int tests are below. */
    // alternating upper and lower case
    //(256,257,1,0)(302,303,1,2)
    assert(toLowerCase(256) equals 257)
    assert(toLowerCase(257) equals 257)
    assert(toLowerCase(258) equals 259)
    assert(toLowerCase(302) equals 303)
    // high points
    assert(toLowerCase(65313) equals 65345)
    assert(toLowerCase(65338) equals 65370)
    assert(toLowerCase(65339) equals 65339)
    // top and above range
    assert(toLowerCase(toInt("10FFFF")) equals toInt("10FFFF"))
    assert(toLowerCase(toInt("110000")) equals toInt("110000"))
  }

  test("toUpperCase") {
    // low chars
    assert(toUpperCase('\n') equals '\n')
    // ascii chars
    assert(toUpperCase('a') equals 'A')
    assert(toUpperCase('A') equals 'A')
    assertNot(toUpperCase('A') equals 'a')
    assert(toUpperCase('f') equals 'F')
    assert(toUpperCase('z') equals 'Z')
    // compat characters don't agree with JDK
    assert(toUpperCase('ß') equals 'ẞ')

    /** The Int tests are below. */
    // alternating upper and lower case
    //(256,257,1,0)(302,303,1,2)
    assert(toUpperCase(257) equals 256)
    assert(toUpperCase(258) equals 258)
    assert(toUpperCase(259) equals 258)
    assert(toUpperCase(303) equals 302)
    // high points
    assert(toUpperCase(65345) equals 65313)
    assert(toUpperCase(65370) equals 65338)
    assert(toUpperCase(65371) equals 65371)
    // top and above range
    assert(toUpperCase(toInt("10FFFF")) equals toInt("10FFFF"))
    assert(toUpperCase(toInt("110000")) equals toInt("110000"))
  }

}
