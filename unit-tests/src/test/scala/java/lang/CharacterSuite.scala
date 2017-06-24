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
