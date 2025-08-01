package org.scalanative.testsuite.javalib.lang

import java.lang._

import java.nio.CharBuffer
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.charset.{Charset, StandardCharsets}

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.executingInJVM

class StringTest {

  @Test def stringArrayByteIntIntStringWithNullEncoding(): Unit = {
    assertThrows(
      classOf[java.lang.NullPointerException],
      new String("I don't like nulls".getBytes, 0, 3, null: String)
    )
  }

  @Test def stringArrayByteIntIntStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new String("Pacem in terris".getBytes, 0, 3, "unsupported encoding")
    )
  }

  @Test def stringArrayByteStringWithNullEncoding(): Unit = {
    assertThrows(
      classOf[java.lang.NullPointerException],
      new String("Nulls are just as bad".getBytes, null: String)
    )
  }

  @Test def stringArrayByteStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new String("to people of goodwill.".getBytes, "unsupported encoding")
    )
  }

  @Test def stringArrayByteStartLengthWithInvalidStartOrLength(): Unit = {
    val chars: Array[Char] = Array('a', 'b', 'c')

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      new String(chars, -1, chars.length) // invalid start
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      new String(chars, 0, chars.length + 1) // invalid length
    )
  }

  @Test def stringArrayIntOffsetCountWithInvalidOffsetOrCount(): Unit = {
    val codePoints = Array[Int](235, 872, 700, 298)

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      new String(codePoints, -1, codePoints.length) // invalid offset
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      new String(codePoints, 0, codePoints.length + 1) // invalid length
    )
  }

  @Test def plus(): Unit = {
    assertTrue("big 5" == "big " + 5.toByte)
    assertTrue("big 5" == "big " + 5.toShort)
    assertTrue("big 5" == "big " + 5)
    assertTrue("big 5" == "big " + 5L)
    assertTrue("5 big" == s"${5.toByte} big")
    assertTrue("5 big" == s"${5.toShort} big")
    assertTrue("5 big" == s"${5} big")
    assertTrue("5 big" == s"${5L} big")
    assertTrue("foo" == "foo" + "")
    assertTrue("foo" == "" + "foo")
    assertTrue("foobar" == "foo" + "bar")
    assertTrue("foobarbaz" == "foo" + "bar" + "baz")
  }

  @Test def codePointAtIndexWithInvalidIndex(): Unit = {
    val data = "When in the Course"

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      data.codePointAt(-1)
    )

    assertThrows(
      classOf[java.lang.StringIndexOutOfBoundsException],
      data.codePointAt(data.length + 1)
    )
  }

  @Test def codePointBeforeIndexWithInvalidIndex(): Unit = {
    val data = "of human events"

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      data.codePointBefore(-1)
    )

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      // Careful here, +1 is valid +2 is not
      data.codePointBefore(data.length + 2)
    )
  }

  @Test def codePointCountBeginIndexEndIndexWithInvalidBeginOrEndIndex()
      : Unit = {
    val data = "it becomes necessary"

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      data.codePointCount(-1, data.length)
    )

    assertThrows(
      classOf[java.lang.IndexOutOfBoundsException],
      data.codePointCount(0, data.length + 1)
    )
  }

  @Test def offsetByCodePoints(): Unit = {
    assertTrue("abc".offsetByCodePoints(0, 3) == 3)
    assertTrue("abc".offsetByCodePoints(1, 2) == 3)

    assertTrue("abc".offsetByCodePoints(3, -3) == 0)
    assertTrue("abc".offsetByCodePoints(3, -2) == 1)

    assertTrue("\uD800\uDC00".offsetByCodePoints(0, 1) == 2)
    assertTrue("\uD800\uDC00".offsetByCodePoints(1, -1) == 0)
  }

  @Test def offsetByCodePointsUnpairedSurrogates(): Unit = {
    assertTrue("\uD800".offsetByCodePoints(0, 1) == 1)
    assertTrue("\uDBFF".offsetByCodePoints(0, 1) == 1)
    assertTrue("\uDC00".offsetByCodePoints(0, 1) == 1)
    assertTrue("\uDFFF".offsetByCodePoints(0, 1) == 1)

    assertTrue("\uD800".offsetByCodePoints(1, -1) == 0)
    assertTrue("\uDBFF".offsetByCodePoints(1, -1) == 0)
    assertTrue("\uDC00".offsetByCodePoints(1, -1) == 0)
    assertTrue("\uDFFF".offsetByCodePoints(1, -1) == 0)

    assertTrue("\uD800x".offsetByCodePoints(0, 2) == 2)
    assertTrue("x\uD800".offsetByCodePoints(0, 2) == 2)
  }

  @Test def compareTo(): Unit = {
    assertTrue("test".compareTo("utest") < 0)
    assertTrue("test".compareTo("test") == 0)
    assertTrue("test".compareTo("stest") > 0)
    assertTrue("test".compareTo("tess") > 0)
  }

  @Test def compareToIgnoreCase(): Unit = {
    assertTrue("test".compareToIgnoreCase("Utest") < 0)
    assertTrue("test".compareToIgnoreCase("Test") == 0)
    assertTrue("Test".compareToIgnoreCase("stest") > 0)
    assertTrue("tesT".compareToIgnoreCase("teSs") > 0)

    // Scala Native substring based tests
    assertTrue("test".compareToIgnoreCase("NativeUtest".substring(6)) < 0)
    assertTrue("test".compareToIgnoreCase("NativeUtest".substring(6, 10)) < 0)
    assertTrue("NativeUTest".substring(6).compareToIgnoreCase("test") > 0)
    assertTrue("NativetesT".substring(6).compareToIgnoreCase("teSs") > 0)
    assertTrue("test".compareToIgnoreCase("NativeTest".substring(6)) == 0)
    assertTrue("NativeTest".substring(6).compareToIgnoreCase("test") == 0)

    // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
    assertEquals(0, "Scala.JS".compareToIgnoreCase("Scala.js"))
    assertEquals(3, "Scala.JS".compareToIgnoreCase("scala"))
    assertEquals(0, "åløb".compareToIgnoreCase("ÅLØB"))
    assertEquals(-9, "Java".compareToIgnoreCase("Scala"))

    // Case folding that changes the string length are not supported,
    // therefore ligatures are not equal to their expansion.
    // U+FB00 LATIN SMALL LIGATURE FF
    assertEquals(64154, "Eﬀet".compareToIgnoreCase("effEt"))
    assertEquals(64154, "Eﬀet".compareToIgnoreCase("eFFEt"))

    // "ı" and 'i' are considered equal, as well as their uppercase variants
    assertEquals(
      0,
      "ıiIİ ıiIİ ıiIİ ıiIİ".compareToIgnoreCase("ıııı iiii IIII İİİİ")
    )
  }

  @Test def equalsIgnoreCase(): Unit = {
    assertTrue("test".equalsIgnoreCase("TEST"))
    assertTrue("TEst".equalsIgnoreCase("teST"))
    assertFalse("SEst".equalsIgnoreCase("TEss"))

    // Scala Native substring based tests
    assertTrue("test".equalsIgnoreCase("NativeTEST".substring(6)))
    assertTrue("TEst".equalsIgnoreCase("NativeteST".substring(6)))
    assertFalse("SEst".equalsIgnoreCase("NativeTEss".substring(6)))
    assertTrue("NativeTEST".substring(6).equalsIgnoreCase("test"))
    assertTrue("NativeteST".substring(6).equalsIgnoreCase("TEst"))
    assertFalse("NativeTEss".substring(6).equalsIgnoreCase("SEst"))
    assertTrue("NativeTESTaaa".substring(6, 10).equalsIgnoreCase("test"))
    assertTrue("NativeteSTaaa".substring(6, 10).equalsIgnoreCase("TEst"))
    assertFalse("NativeTEssaaa".substring(6, 10).equalsIgnoreCase("SEst"))

    // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
    assertTrue("Scala.JS".equalsIgnoreCase("Scala.js"))
    assertTrue("åløb".equalsIgnoreCase("ÅLØb"))
    assertFalse("Scala.js".equalsIgnoreCase("Java"))
    assertFalse("Scala.js".equalsIgnoreCase(null))

    // Case folding that changes the string length are not supported,
    // therefore ligatures are not equal to their expansion.
    // U+FB00 LATIN SMALL LIGATURE FF
    assertFalse("Eﬀet".equalsIgnoreCase("effEt"))
    assertFalse("Eﬀet".equalsIgnoreCase("eFFEt"))

    // "ı" and 'i' are considered equal, as well as their uppercase variants
    assertTrue("ıiIİ ıiIİ ıiIİ ıiIİ".equalsIgnoreCase("ıııı iiii IIII İİİİ"))

    // null is a valid input
    assertFalse("foo".equalsIgnoreCase(null))
  }

  @Test def replaceChar(): Unit = {
    assertTrue("test".replace('t', 'p').equals("pesp"))
    assertTrue("Test".replace('t', 'p').equals("Tesp"))
    assertTrue("Test".replace('T', 'p').equals("pest"))
    assertTrue("Test".replace('0', '1').equals("Test"))
  }

  @Test def replaceCharSequence(): Unit = {
    // Runs assertion with and without prefix and suffix
    def check(input: String, replace: String => Boolean) = {
      assertTrue(replace(input))

      val inputWithPrefix = ("[" + input).substring(1)
      assertEquals(inputWithPrefix, input)
      assertTrue(replace(inputWithPrefix))

      val inputWithSuffix = (input + "]").substring(0, input.length)
      assertEquals(inputWithSuffix, input)
      assertTrue(replace(inputWithSuffix))

      val inputWithBoth = ("[" + input + "]").substring(1, input.length + 1)
      assertEquals(inputWithBoth, input)
      assertTrue(replace(inputWithBoth))
    }

    check("test", _.replace("t", "p").equals("pesp"))
    check("Test", _.replace("t", "p").equals("Tesp"))
    check("test", _.replace("e", "oa").equals("toast"))
    check("Test", _.replace("T", "p").equals("pest"))
    check("spantanplans", _.replace("an", ".").equals("sp.t.pl.s"))
    check("spantanplans", _.replace("an", "").equals("sptpls"))
    check("Test", _.replace("0", "1").equals("Test"))
    check("Test", _.replace("e", "").equals("Tst"))
    check("Test", _.replace("t", "").equals("Tes"))
    check("Test", _.replace("", "").equals("Test"))
    check("Test", _.replace("", "--").equals("--T--e--s--t--"))
  }

  @Test def replaceAllNonAscii(): Unit = {
    val greetings = "Gruesze"

    val greetingsWithUmlaut = greetings.replaceAll("ue", "ü")
    assertTrue(greetingsWithUmlaut == "Grüsze")

    val greetingsWithUmlautAndSharpS = greetingsWithUmlaut.replaceAll("sz", "ß")
    assertTrue(greetingsWithUmlautAndSharpS == "Grüße")

    assertTrue(
      "Grueszszszeszszszszsze".replaceAll("sz", "ß") == "Grueßßßeßßßßße"
    )
  }

  @Test def replaceWithDollarSignInReplacementIssue1070(): Unit = {
    val literal = "{.0}"
    val replacement = "\\$ipsum"
    val prefix = "Lorem "
    val suffix = " dolor"
    val text = prefix + literal + suffix
    val expected = prefix + replacement + suffix

    assertTrue(text.replace(literal, replacement) == expected)
  }

  private def splitVec(s: String, sep: String, limit: Int = 0) =
    s.split(sep, limit).toVector

  private def splitTest(sep: String, splitExpr: Option[String] = None) = {
    val splitSep = splitExpr getOrElse sep
    val n = 4
    val limit = 2

    assertTrue(splitVec("", splitSep) == Vector(""))
    assertTrue(splitVec("", splitSep, limit) == Vector(""))

    val noSep = "b"
    assertTrue(splitVec(noSep, splitSep) == Vector(noSep))
    assertTrue(splitVec(noSep, splitSep, limit) == Vector(noSep))

    (1 to n) foreach { i =>
      val allSep = sep * n
      assertTrue(splitVec(allSep, splitSep) == Vector.empty)
      assertTrue(
        splitVec(allSep, splitSep, n) == (0 until (n - 1))
          .map(_ => "")
          .toVector :+ sep
      )
      assertTrue(
        splitVec(allSep, splitSep, limit) == (0 until (limit - 1))
          .map(_ => "")
          .toVector :+ allSep.drop((limit - 1) * sep.length)
      )
    }

    val oneSep = noSep + sep
    assertTrue(splitVec(oneSep, splitSep) == Vector(noSep))
    assertTrue(splitVec(oneSep, splitSep, 1) == Vector(oneSep))
    assertTrue(splitVec(oneSep, splitSep, 2) == Vector(noSep, ""))

    val twoSep = oneSep * 2
    assertTrue(splitVec(twoSep, splitSep) == Vector(noSep, noSep))
    assertTrue(splitVec(twoSep, splitSep, 1) == Vector(twoSep))
    assertTrue(splitVec(twoSep, splitSep, 2) == Vector(noSep, oneSep))
    assertTrue(splitVec(twoSep, splitSep, 3) == Vector(noSep, noSep, ""))

    val leadingSep = sep + noSep
    assertTrue(splitVec(leadingSep, splitSep) == Vector("", noSep))
    assertTrue(splitVec(leadingSep, splitSep, 1) == Vector(leadingSep))
    assertTrue(splitVec(leadingSep, splitSep, 2) == Vector("", noSep))
    assertTrue(splitVec(leadingSep, splitSep, 3) == Vector("", noSep))

    val trailingSep = noSep + sep
    assertTrue(splitVec(trailingSep, splitSep) == Vector(noSep))
    assertTrue(splitVec(trailingSep, splitSep, 1) == Vector(trailingSep))
    assertTrue(splitVec(trailingSep, splitSep, 2) == Vector(noSep, ""))
    assertTrue(splitVec(trailingSep, splitSep, 3) == Vector(noSep, ""))

    val leadingPlusTrailing = sep + noSep + sep
    assertTrue(splitVec(leadingPlusTrailing, splitSep) == Vector("", noSep))
    assertTrue(
      splitVec(leadingPlusTrailing, splitSep, 1) == Vector(leadingPlusTrailing)
    )
    assertTrue(splitVec(leadingPlusTrailing, splitSep, 2) == Vector("", oneSep))
    assertTrue(
      splitVec(leadingPlusTrailing, splitSep, 3) == Vector("", noSep, "")
    )
    assertTrue(
      splitVec(leadingPlusTrailing, splitSep, 4) == Vector("", noSep, "")
    )
  }

  @Test def split(): Unit = {
    splitTest("a")
    splitTest(".", splitExpr = Some("\\."))
    splitTest("ab", splitExpr = Some("ab"))
    splitTest("ab", splitExpr = Some("(ab)"))
  }

  def testEncoding(charset: String, expectedInts: Seq[Int]): Unit = {
    testEncoding(Charset.forName(charset), expectedInts)
  }

  def testEncoding(charset: Charset, expectedInts: Seq[Int]): Unit = {
    // Try to break getBytes, test with difficult characters.
    // \u00DF Greek lowercase beta; expect 2 output bytes
    // \u4E66 Han Character 'book, letter, document; writings' ; 3 output bytes
    // \u1F50A emoji 'speaker with three sound waves'; 4 output bytes.
    //
    // Reference: http://stn.audible.com/abcs-of-unicode/
    //		       // replace 4E66 with hex string of interest
    //		  http://www.fileformat.info/info/unicode/char/4E66/index.htm

    val text = "\u0000\t\nAZaz09@~\u00DF\u4E66\u1F50A"

    // sanity check on character escapes, missing backslash or 'u', etc.
    assertEquals(text.length, 15)

    val bytes = text.getBytes(charset)
    val expectedBytes = expectedInts.map(i => java.lang.Byte.valueOf(i.toByte))
    val expected = Array[java.lang.Byte](expectedBytes: _*)
    assertTrue("result != expected}", bytes.sameElements(expected))
  }

  @Test def getBytesUTF8(): Unit = {

    val expectedInts =
      Seq(0, 9, 10, 65, 90, 97, 122, 48, 57, 64, 126, // one byte unicode
        -61, -97, // two byte unicode
        -28, -71, -90, // three byte unicode
        -31, -67, -112, 65 // four byte unicode
      )

    testEncoding(StandardCharsets.UTF_8, expectedInts)
    testEncoding("UTF-8", expectedInts)
  }

  @Test def getBytesUTF16(): Unit = {
    val expectedBE =
      Seq(
        0, 0, 0, 9, 0, 10, 0, 65, 0, 90, 0, 97, 0, 122, 0, 48, 0, 57, 0, 64, 0,
        126, 0, -33, 78, 102, 31, 80, 0, 65
      )

    val expectedLE = expectedBE
      .sliding(2, 2)
      .toSeq
      .flatMap(_.reverse)

    val expectedWithBOM = Seq(-2, -1) ++ expectedBE

    testEncoding(StandardCharsets.UTF_16BE, expectedBE)
    testEncoding("UTF-16BE", expectedBE)
    testEncoding(StandardCharsets.UTF_16LE, expectedLE)
    testEncoding("UTF-16LE", expectedLE)
    testEncoding(StandardCharsets.UTF_16, expectedWithBOM)
    testEncoding("UTF-16", expectedWithBOM)
  }

  @Test def getBytesUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      "This is a test".getBytes("unsupported encoding")
    )
  }

  @Test def literalsHaveConsistentHashCodeImplementation(): Unit = {
    assertTrue(
      "foobar".hashCode == new String(
        Array('f', 'o', 'o', 'b', 'a', 'r')
      ).hashCode
    )
  }

  @Ignore("#486")
  @Test def intern(): Unit = {
    val chars = Array('f', 'o', 'o', 'b', 'a', 'r')
    val s1 = new String(chars)
    val s2 = new String(chars)
    assertTrue(s1.intern eq s2.intern)
  }

  @Test def indexOf(): Unit = {
    assertTrue("afoobar".indexOf("a") == 0)
    assertTrue("afoobar".indexOf(97) == 0)
    assertTrue("afoobar".indexOf("a", 1) == 5)
    assertTrue("afoobar".indexOf(97, 1) == 5)
    assertTrue("".indexOf("a") == -1)
    assertTrue("".indexOf(97) == -1)
    assertTrue("".indexOf("a", 4) == -1)
    assertTrue("".indexOf(97, 4) == -1)
    assertTrue("fubår".indexOf("a") == -1)
    assertTrue("fubår".indexOf(97) == -1)
    assertTrue("fubår".indexOf("a", 4) == -1)
    assertTrue("fubår".indexOf(97, 4) == -1)
  }

  @Test def indexOfSubStringWithSurrogatePair(): Unit = {
    val helloInSurrogatePairs =
      "\ud835\udd59\ud835\udd56\ud835\udd5d\ud835\udd5d\ud835\udd60"

    val needle = "\ud835\udd5d\ud835\udd60" // outlined ell oh

    val index = helloInSurrogatePairs.indexOf(needle)
    assertEquals("indexOf surrogate outlined ell oh", 6, index)
  }

  @Test def lastIndexOf(): Unit = {
    assertTrue("afoobar".lastIndexOf("a") == 5)
    assertTrue("afoobar".lastIndexOf(97) == 5)
    assertTrue("afoobar".lastIndexOf("a", 4) == 0)
    assertTrue("afoobar".lastIndexOf(97, 4) == 0)
    assertTrue("".lastIndexOf("a") == -1)
    assertTrue("".lastIndexOf(97) == -1)
    assertTrue("".lastIndexOf("a", 4) == -1)
    assertTrue("".lastIndexOf(97, 4) == -1)
    assertTrue("fubår".lastIndexOf("a") == -1)
    assertTrue("fubår".lastIndexOf(97) == -1)
    assertTrue("fubår".lastIndexOf("a", 4) == -1)
    assertTrue("fubår".lastIndexOf(97, 4) == -1)
  }

  @Test def lastIndexOfSubStringWithSurrogatePair(): Unit = {
    val helloInSurrogatePairs =
      "\ud835\udd59\ud835\udd56\ud835\udd5d\ud835\udd5d\ud835\udd60"

    val needle = "\ud835\udd56\ud835\udd5d" // outlined e ell

    val index = helloInSurrogatePairs.lastIndexOf(needle)
    assertEquals("lastIndexOf surrorate outlined ell", 2, index)
  }

  @Test def toUpperCase(): Unit = {
    assertEquals("".toUpperCase(), "")
    // ascii
    assertEquals("Hello".toUpperCase(), "HELLO")
    // latin
    assertEquals("Perché".toUpperCase(), "PERCHÉ")
    // high (2 Char String) - 0x10400 or \ud801\udc00
    val iStr = new String(Character.toChars(0x10400))
    assertEquals(iStr.length, 2)
    assertEquals(iStr.toUpperCase, iStr)
    val bStr = "\ud801\udc00"
    assertEquals(bStr.length, 2)
    assertEquals(bStr.toUpperCase, "\ud801\udc00")
    assertEquals("𐐨aaaa".toUpperCase, "𐐀AAAA")
    assertEquals("aaaa𐐨".toUpperCase, "AAAA𐐀")
    assertEquals("aa𐐨aa".toUpperCase, "AA𐐀AA")
    // partial in surrogate range
    // case of poor slicing or construction of string
    assertEquals("\ud801aaaa".toUpperCase, "\ud801AAAA")
    assertEquals("aaaa\ud801".toUpperCase, "AAAA\ud801")
    assertEquals("\udc00aaaa".toUpperCase, "\udc00AAAA")
    assertEquals("aaaa\udc00".toUpperCase, "AAAA\udc00")
    // case of one high surrogate
    val hChar = '\ud801'
    val hStr = hChar.toString
    assertTrue(Character.isHighSurrogate(hChar))
    assertEquals(hStr.length, 1)
    assertEquals(hStr.toUpperCase, hStr)
    // toUpperCase should consider String's offset
    assertEquals(
      "SCALA NATIVE",
      "Hi, Scala Native!"
        .subSequence(4, 16)
        .toString
        .toUpperCase
    )
  }

  @Test def toUpperCaseSpecialCasing(): Unit = {
    // Generated based on Unconditional mappings in [SpecialCasing.txt](https://unicode.org/Public/UNIDATA/SpecialCasing.txt)
    assertEquals("\u0053\u0053", "\u00DF".toUpperCase) // ß to SS
    assertEquals("\u02BC\u004E", "\u0149".toUpperCase) // ŉ to ʼN
    assertEquals("\u004A\u030C", "\u01F0".toUpperCase) // ǰ to J̌
    assertEquals("\u0399\u0308\u0301", "\u0390".toUpperCase) // ΐ to Ϊ́
    assertEquals("\u03A5\u0308\u0301", "\u03B0".toUpperCase) // ΰ to Ϋ́
    assertEquals("\u0535\u0552", "\u0587".toUpperCase) // և to ԵՒ
    assertEquals("\u0048\u0331", "\u1E96".toUpperCase) // ẖ to H̱
    assertEquals("\u0054\u0308", "\u1E97".toUpperCase) // ẗ to T̈
    assertEquals("\u0057\u030A", "\u1E98".toUpperCase) // ẘ to W̊
    assertEquals("\u0059\u030A", "\u1E99".toUpperCase) // ẙ to Y̊
    assertEquals("\u0041\u02BE", "\u1E9A".toUpperCase) // ẚ to Aʾ
    assertEquals("\u03A5\u0313", "\u1F50".toUpperCase) // ὐ to Υ̓
    assertEquals("\u03A5\u0313\u0300", "\u1F52".toUpperCase) // ὒ to Υ̓̀
    assertEquals("\u03A5\u0313\u0301", "\u1F54".toUpperCase) // ὔ to Υ̓́
    assertEquals("\u03A5\u0313\u0342", "\u1F56".toUpperCase) // ὖ to Υ̓͂
    assertEquals("\u1F08\u0399", "\u1F80".toUpperCase) // ᾀ to ἈΙ
    assertEquals("\u1F09\u0399", "\u1F81".toUpperCase) // ᾁ to ἉΙ
    assertEquals("\u1F0A\u0399", "\u1F82".toUpperCase) // ᾂ to ἊΙ
    assertEquals("\u1F0B\u0399", "\u1F83".toUpperCase) // ᾃ to ἋΙ
    assertEquals("\u1F0C\u0399", "\u1F84".toUpperCase) // ᾄ to ἌΙ
    assertEquals("\u1F0D\u0399", "\u1F85".toUpperCase) // ᾅ to ἍΙ
    assertEquals("\u1F0E\u0399", "\u1F86".toUpperCase) // ᾆ to ἎΙ
    assertEquals("\u1F0F\u0399", "\u1F87".toUpperCase) // ᾇ to ἏΙ
    assertEquals("\u1F08\u0399", "\u1F88".toUpperCase) // ᾈ to ἈΙ
    assertEquals("\u1F09\u0399", "\u1F89".toUpperCase) // ᾉ to ἉΙ
    assertEquals("\u1F0A\u0399", "\u1F8A".toUpperCase) // ᾊ to ἊΙ
    assertEquals("\u1F0B\u0399", "\u1F8B".toUpperCase) // ᾋ to ἋΙ
    assertEquals("\u1F0C\u0399", "\u1F8C".toUpperCase) // ᾌ to ἌΙ
    assertEquals("\u1F0D\u0399", "\u1F8D".toUpperCase) // ᾍ to ἍΙ
    assertEquals("\u1F0E\u0399", "\u1F8E".toUpperCase) // ᾎ to ἎΙ
    assertEquals("\u1F0F\u0399", "\u1F8F".toUpperCase) // ᾏ to ἏΙ
    assertEquals("\u1F28\u0399", "\u1F90".toUpperCase) // ᾐ to ἨΙ
    assertEquals("\u1F29\u0399", "\u1F91".toUpperCase) // ᾑ to ἩΙ
    assertEquals("\u1F2A\u0399", "\u1F92".toUpperCase) // ᾒ to ἪΙ
    assertEquals("\u1F2B\u0399", "\u1F93".toUpperCase) // ᾓ to ἫΙ
    assertEquals("\u1F2C\u0399", "\u1F94".toUpperCase) // ᾔ to ἬΙ
    assertEquals("\u1F2D\u0399", "\u1F95".toUpperCase) // ᾕ to ἭΙ
    assertEquals("\u1F2E\u0399", "\u1F96".toUpperCase) // ᾖ to ἮΙ
    assertEquals("\u1F2F\u0399", "\u1F97".toUpperCase) // ᾗ to ἯΙ
    assertEquals("\u1F28\u0399", "\u1F98".toUpperCase) // ᾘ to ἨΙ
    assertEquals("\u1F29\u0399", "\u1F99".toUpperCase) // ᾙ to ἩΙ
    assertEquals("\u1F2A\u0399", "\u1F9A".toUpperCase) // ᾚ to ἪΙ
    assertEquals("\u1F2B\u0399", "\u1F9B".toUpperCase) // ᾛ to ἫΙ
    assertEquals("\u1F2C\u0399", "\u1F9C".toUpperCase) // ᾜ to ἬΙ
    assertEquals("\u1F2D\u0399", "\u1F9D".toUpperCase) // ᾝ to ἭΙ
    assertEquals("\u1F2E\u0399", "\u1F9E".toUpperCase) // ᾞ to ἮΙ
    assertEquals("\u1F2F\u0399", "\u1F9F".toUpperCase) // ᾟ to ἯΙ
    assertEquals("\u1F68\u0399", "\u1FA0".toUpperCase) // ᾠ to ὨΙ
    assertEquals("\u1F69\u0399", "\u1FA1".toUpperCase) // ᾡ to ὩΙ
    assertEquals("\u1F6A\u0399", "\u1FA2".toUpperCase) // ᾢ to ὪΙ
    assertEquals("\u1F6B\u0399", "\u1FA3".toUpperCase) // ᾣ to ὫΙ
    assertEquals("\u1F6C\u0399", "\u1FA4".toUpperCase) // ᾤ to ὬΙ
    assertEquals("\u1F6D\u0399", "\u1FA5".toUpperCase) // ᾥ to ὭΙ
    assertEquals("\u1F6E\u0399", "\u1FA6".toUpperCase) // ᾦ to ὮΙ
    assertEquals("\u1F6F\u0399", "\u1FA7".toUpperCase) // ᾧ to ὯΙ
    assertEquals("\u1F68\u0399", "\u1FA8".toUpperCase) // ᾨ to ὨΙ
    assertEquals("\u1F69\u0399", "\u1FA9".toUpperCase) // ᾩ to ὩΙ
    assertEquals("\u1F6A\u0399", "\u1FAA".toUpperCase) // ᾪ to ὪΙ
    assertEquals("\u1F6B\u0399", "\u1FAB".toUpperCase) // ᾫ to ὫΙ
    assertEquals("\u1F6C\u0399", "\u1FAC".toUpperCase) // ᾬ to ὬΙ
    assertEquals("\u1F6D\u0399", "\u1FAD".toUpperCase) // ᾭ to ὭΙ
    assertEquals("\u1F6E\u0399", "\u1FAE".toUpperCase) // ᾮ to ὮΙ
    assertEquals("\u1F6F\u0399", "\u1FAF".toUpperCase) // ᾯ to ὯΙ
    assertEquals("\u1FBA\u0399", "\u1FB2".toUpperCase) // ᾲ to ᾺΙ
    assertEquals("\u0391\u0399", "\u1FB3".toUpperCase) // ᾳ to ΑΙ
    assertEquals("\u0386\u0399", "\u1FB4".toUpperCase) // ᾴ to ΆΙ
    assertEquals("\u0391\u0342", "\u1FB6".toUpperCase) // ᾶ to Α͂
    assertEquals("\u0391\u0342\u0399", "\u1FB7".toUpperCase) // ᾷ to Α͂Ι
    assertEquals("\u0391\u0399", "\u1FBC".toUpperCase) // ᾼ to ΑΙ
    assertEquals("\u1FCA\u0399", "\u1FC2".toUpperCase) // ῂ to ῊΙ
    assertEquals("\u0397\u0399", "\u1FC3".toUpperCase) // ῃ to ΗΙ
    assertEquals("\u0389\u0399", "\u1FC4".toUpperCase) // ῄ to ΉΙ
    assertEquals("\u0397\u0342", "\u1FC6".toUpperCase) // ῆ to Η͂
    assertEquals("\u0397\u0342\u0399", "\u1FC7".toUpperCase) // ῇ to Η͂Ι
    assertEquals("\u0397\u0399", "\u1FCC".toUpperCase) // ῌ to ΗΙ
    assertEquals("\u0399\u0308\u0300", "\u1FD2".toUpperCase) // ῒ to Ϊ̀
    assertEquals("\u0399\u0308\u0301", "\u1FD3".toUpperCase) // ΐ to Ϊ́
    assertEquals("\u0399\u0342", "\u1FD6".toUpperCase) // ῖ to Ι͂
    assertEquals("\u0399\u0308\u0342", "\u1FD7".toUpperCase) // ῗ to Ϊ͂
    assertEquals("\u03A5\u0308\u0300", "\u1FE2".toUpperCase) // ῢ to Ϋ̀
    assertEquals("\u03A5\u0308\u0301", "\u1FE3".toUpperCase) // ΰ to Ϋ́
    assertEquals("\u03A1\u0313", "\u1FE4".toUpperCase) // ῤ to Ρ̓
    assertEquals("\u03A5\u0342", "\u1FE6".toUpperCase) // ῦ to Υ͂
    assertEquals("\u03A5\u0308\u0342", "\u1FE7".toUpperCase) // ῧ to Ϋ͂
    assertEquals("\u1FFA\u0399", "\u1FF2".toUpperCase) // ῲ to ῺΙ
    assertEquals("\u03A9\u0399", "\u1FF3".toUpperCase) // ῳ to ΩΙ
    assertEquals("\u038F\u0399", "\u1FF4".toUpperCase) // ῴ to ΏΙ
    assertEquals("\u03A9\u0342", "\u1FF6".toUpperCase) // ῶ to Ω͂
    assertEquals("\u03A9\u0342\u0399", "\u1FF7".toUpperCase) // ῷ to Ω͂Ι
    assertEquals("\u03A9\u0399", "\u1FFC".toUpperCase) // ῼ to ΩΙ
    assertEquals("\u0046\u0046", "\uFB00".toUpperCase) // ﬀ to FF
    assertEquals("\u0046\u0049", "\uFB01".toUpperCase) // ﬁ to FI
    assertEquals("\u0046\u004C", "\uFB02".toUpperCase) // ﬂ to FL
    assertEquals("\u0046\u0046\u0049", "\uFB03".toUpperCase) // ﬃ to FFI
    assertEquals("\u0046\u0046\u004C", "\uFB04".toUpperCase) // ﬄ to FFL
    assertEquals("\u0053\u0054", "\uFB05".toUpperCase) // ﬅ to ST
    assertEquals("\u0053\u0054", "\uFB06".toUpperCase) // ﬆ to ST
    assertEquals("\u0544\u0546", "\uFB13".toUpperCase) // ﬓ to ՄՆ
    assertEquals("\u0544\u0535", "\uFB14".toUpperCase) // ﬔ to ՄԵ
    assertEquals("\u0544\u053B", "\uFB15".toUpperCase) // ﬕ to ՄԻ
    assertEquals("\u054E\u0546", "\uFB16".toUpperCase) // ﬖ to ՎՆ
    assertEquals("\u0544\u053D", "\uFB17".toUpperCase) // ﬗ to ՄԽ
  }

  @Test def toLowerCase(): Unit = {
    assertEquals("".toLowerCase(), "")
    assertEquals("Hello".toLowerCase(), "hello")
    assertEquals("PERCHÉ".toLowerCase(), "perché")
    assertEquals("𐐀AAAA".toLowerCase, "𐐨aaaa")
    assertEquals("AAAA𐐀".toLowerCase, "aaaa𐐨")
    assertEquals("AA𐐀AA".toLowerCase, "aa𐐨aa")
    // toLowerCase should consider String's offset
    assertEquals(
      "scala native",
      "Hi, Scala Native!"
        .subSequence(4, 16)
        .toString
        .toLowerCase
    )
  }

  @Test def toLowerCaseSpecialCasing(): Unit = {
    assertEquals("\u0069\u0307", "\u0130".toLowerCase) // İ to i̇
    assertEquals("iíìĩi\u0307", "IÍÌĨİ".toLowerCase())

    /* Greek lower letter sigma exists in two forms:
     * \u03c3 'σ' - is standard lower case variant
     * \u03c2 'ς' - is used when it's final cased character in given word
     */
    assertEquals("σ", "Σ".toLowerCase())
    assertEquals("σς", "ΣΣ".toLowerCase())
    assertEquals("dς", "DΣ".toLowerCase())
    assertEquals("dσς aσς bσc", "DΣΣ AΣΣ BΣC".toLowerCase())
    assertEquals(
      "dσς a\uD804\uDC00σ\uD804\uDC00σ\uD804\uDC00 bσc",
      "DΣΣ A\uD804\uDC00Σ\uD804\uDC00Σ\uD804\uDC00 BΣC".toLowerCase()
    )
    assertEquals("dσσa", "DΣΣA".toLowerCase())
    assertEquals("dσς", "DΣΣA".substring(0, 3).toLowerCase())
    // \u02B9 is not cased character
    assertEquals(
      "dσ\u02B9\u02B9ς\u02B9\u02B9",
      "DΣ\u02B9\u02B9Σ\u02B9\u02B9".toLowerCase
    )
    assertEquals(
      "dσ\u02B9\u02B9σ\u02B9\u02B9z",
      "DΣ\u02B9\u02B9Σ\u02B9\u02B9Z".toLowerCase
    )
    assertEquals(
      "dσ\u02B9\u02B9ς\u02B9\u02B9",
      "DΣ\u02B9\u02B9Σ\u02B9\u02B9Z".substring(0, 7).toLowerCase
    )

    /* From Unicode 13.0.0 reference, chapter 13.3, description to table 3-17.
     * The sets of case-ignorable and cased characters are not disjoint: for example, they both contain U+0345 ypogegrammeni.
     * Thus, the Before condition is not satisfied if C is preceded by only U+0345,
     * but would be satisfied by the sequence <capital-alpha, ypogegrammeni>.
     * Similarly, the After condition is satisfied if C is only followed by ypogegrammeni,
     * but would not satisfied by the sequence <ypogegrammeni, capital-alpha>.
     */
    assertEquals("\u0345σ", "\u0345Σ".toLowerCase())
    assertEquals("\u03B1\u0345ς", "\u0391\u0345Σ".toLowerCase())
    if (!executingInJVM)
      assertEquals("\u03B1\u0345ς\u0345", "\u0391\u0345Σ\u0345".toLowerCase())

    assertEquals(
      "\u03B1\u0345σ\u0345\u03B1",
      "\u0391\u0345Σ\u0345\u0391".toLowerCase()
    )

  }

  /* --- UNIT TESTS VERIFYING STRING CONSTRUCTORS IMMUTABILITY INTEGRITY ---
   * Issue #2925
   *
   * These tests are in the order of declaration in the Java 8 specification.
   */


// format: off
    val testByteArray = Array(
      'f'.toByte, 0.toByte,
      'o'.toByte, 0.toByte,
      'o'.toByte, 0.toByte,
      'b'.toByte, 0.toByte,
      'a'.toByte, 0.toByte,
      'r'.toByte, 0.toByte
    )
// format: on

  /** String() - No Test, no characters to modify.
   */

  /** Checks that creating a String with an `Array[Byte]`, then replacing its
   *  first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromByteArray(): Unit = {
    val bytes = testByteArray.clone
    val offset = 0 // 'f'

    // Create str from bytes
    val str = new String(bytes)

    // Modify bytes
    bytes(offset) = 'm'.toByte

    assertEquals(
      s"bytes should start with ${'m'.toByte} instead of '${bytes(offset)}'",
      'm'.toByte,
      bytes(offset)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(offset)}'",
      'f',
      str.charAt(offset)
    )
  }

  /** Checks that creating a String with an `Array[Byte]` using a Charset, then
   *  replacing its first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromByteArrayCharset(): Unit = {
    val bytes = testByteArray.clone
    val offset = 0 // 'f'

    // Create str from bytes
    val str = new String(bytes, StandardCharsets.UTF_8)

    // Modify bytes
    bytes(offset) = 'm'.toByte

    assertEquals(
      s"bytes should start with ${'m'.toByte} instead of '${bytes(offset)}'",
      'm'.toByte,
      bytes(offset)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(offset)}'",
      'f',
      str.charAt(offset)
    )
  }

  /** String(byte[], int) - No test, Deprecated since Java 1.1.
   */

  /** Checks that creating a String with sub-Array of `Array[Byte]` then
   *  replacing its first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromByteArrayExtract(): Unit = {
    val bytes = testByteArray.clone
    val offset = 3 // 'b'

    // Create str from bytes
    val str = new String(bytes, offset, 6)

    // Modify bytes
    bytes(offset) = 'm'.toByte

    assertEquals(
      s"bytes should start with ${'m'.toByte} instead of '${bytes(offset)}'",
      'm'.toByte,
      bytes(offset)
    )

    assertEquals(
      s"str should start with 'b' instead of '${str.charAt(offset)}'",
      'b',
      str.charAt(offset)
    )
  }

  /** Checks that creating a String with sub-Array of `Array[Byte]` using a
   *  Charset, then replacing its first character, is not breaking String
   *  immutability.
   */
  @Test def checkImmutabilityNewStringFromByteArrayExtractCharset(): Unit = {
    val bytes = testByteArray.clone
    val offset = 3 // 'b'

    // Create str from bytes
    val str = new String(bytes, offset, 6, StandardCharsets.UTF_8)

    // Modify bytes
    bytes(offset) = 'm'.toByte

    assertEquals(
      s"bytes should start with ${'m'.toByte} instead of '${bytes(offset)}'",
      'm'.toByte,
      bytes(offset)
    )

    assertEquals(
      s"str should start with 'b' instead of '${str.charAt(offset)}'",
      'b',
      str.charAt(offset)
    )
  }

  /** String(byte[], int, int, int) - No test, Deprecated since Java 1.1.
   */

  /** Checks that creating a String with sub-Array of `Array[Byte]` using a
   *  CharsetName, then replacing its first character, is not breaking String
   *  immutability.
   */
  @Test def checkImmutabilityNewStringFromByteArrayExtractCharsetName()
      : Unit = {
    val bytes = testByteArray.clone
    val offset = 3 // 'b'

    // Create str from bytes
    val str = new String(bytes, offset, 6, "UTF-8")

    // Modify bytes
    bytes(offset) = 'm'.toByte

    assertEquals(
      s"bytes should start with ${'m'.toByte} instead of '${bytes(offset)}'",
      'm'.toByte,
      bytes(offset)
    )

    assertEquals(
      s"str should start with 'b' instead of '${str.charAt(offset)}'",
      'b',
      str.charAt(offset)
    )
  }

  /** Checks that creating a String with an `Array[Byte]` using a CharsetName,
   *  then replacing its first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromByteArrayCharsetName(): Unit = {
    val bytes = testByteArray.clone
    val offset = 0 // 'f'

    // Create str from bytes
    val str = new String(bytes, "UTF-8")

    // Modify bytes
    bytes(offset) = 'm'.toByte

    assertEquals(
      s"bytes should start with ${'m'.toByte} instead of '${bytes(offset)}'",
      'm'.toByte,
      bytes(offset)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(offset)}'",
      'f',
      str.charAt(offset)
    )
  }

  /** Checks that creating a String with an `Array[Char]`, then replacing its
   *  first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromCharArray(): Unit = {
    val chars = Array('f', 'o', 'o', 'b', 'a', 'r')
    val offset = 0 // 'f'

    // Create str from chars
    val str = new String(chars)
    // Modify chars
    chars(offset) = 'm'

    assertEquals(
      s"chars should start with 'm' instead of '${chars(offset)}'",
      'm',
      chars(offset)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(offset)}'",
      'f',
      str.charAt(offset)
    )
  }

  /** Checks that creating a String with an `Array[Char]`, then replacing its
   *  first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromCharArrayRange(): Unit = {
    val chars = Array('f', 'o', 'o', 'b', 'a', 'r')
    val offset = 0 // 'f'

    // Create str from a "range" of chars
    val str = new String(chars, offset, 1)

    // Modify chars
    chars(offset) = 'm'

    assertEquals(
      s"chars should start with 'm' instead of '${chars(offset)}'",
      'm',
      chars(offset)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(offset)}'",
      'f',
      str.charAt(offset)
    )
  }

  /** Checks that creating a String with an `Array[codePoints]`, then replacing
   *  its first character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromCodepointArrayRange(): Unit = {
    // Unicode code points are Integers.
    val chars = Array('f', 'o', 'o', 'b', 'a', 'r')
    val codepoints = chars.map(c => c.toInt)

    // Create str from a "range" of codepoints
    val str = new String(codepoints, 0, 5)

    val changedCp = 'm'.toInt
    // Modify codepoints
    codepoints(0) = changedCp

    assertEquals(
      s"codepoints should start with ${changedCp} " +
        s"instead of '${codepoints(0)}'",
      changedCp,
      codepoints(0)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(0)}'",
      'f',
      str.charAt(0)
    )
  }

  /** Checks that creating a String with a `String`, then replacing its first
   *  character, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromString(): Unit = {
    val s1 = "foobar"

    // Create String s2 from a String s1
    val s2 = new String(s1)

    // Modify String s1
    val s3 = s1.replace('f', 'm')

    assertEquals(
      s"s1 should start with 'f' instead of '${s1.charAt(0)}'",
      'f',
      s1.charAt(0)
    )

    assertEquals(
      s"s2 should start with 'f' instead of '${s2.charAt(0)}'",
      'f',
      s2.charAt(0)
    )

    assertEquals(
      s"s3 should start with 'm' instead of '${s3.charAt(0)}'",
      'm',
      s3.charAt(0)
    )
  }

  /** Checks that creating a String with a StringBuffer, whose backing Array is
   *  shared with the created String, is not breaking String immutability. See:
   *  https://github.com/scala-native/scala-native/issues/2925
   */
  @Test def checkImmutabilityNewStringFromStringBuffer(): Unit = {
    val strBuffer = new StringBuffer()
    strBuffer.append("foobar")

    // Create str from a StringBuffer
    val str = new String(strBuffer)

    // Modify the StringBuffer
    strBuffer.setCharAt(0, 'm')

    assertEquals(
      s"strBuffer should start with 'm' instead of '${strBuffer.charAt(0)}'",
      'm',
      strBuffer.charAt(0)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(0)}'",
      'f',
      str.charAt(0)
    )
  }

  /** Checks that creating a String with a StringBuilder, whose backing Array is
   *  shared with the created String, is not breaking String immutability.
   */
  @Test def checkImmutabilityNewStringFromStringBuilder(): Unit = {
    val strBuilder = new StringBuilder()
    strBuilder.append("foobar")

    // Create str from a StringBuilder
    val str = new String(strBuilder)

    // Modify the StringBuilder
    strBuilder.setCharAt(0, 'm')

    assertEquals(
      s"strBuilder should start with 'm' instead of '${strBuilder.charAt(0)}'",
      'm',
      strBuilder.charAt(0)
    )

    assertEquals(
      s"str should start with 'f' instead of '${str.charAt(0)}'",
      'f',
      str.charAt(0)
    )
  }

  // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
  @Test def charAt(): Unit = {
    @noinline def testNoInline(expected: Char, s: String, i: Int): Unit =
      assertEquals(expected, s.charAt(i))

    @inline def test(expected: Char, s: String, i: Int): Unit = {
      testNoInline(expected, s, i)
      assertEquals(expected, s.charAt(i))
    }

    test('S', "Scala.js", 0)
    test('.', "Scala.js", 5)
    test('s', "Scala.js", 7)
    test('o', "foo", 1)

    // Scala Native added
    test('a', "Scala.js".substring(4), 0)
    test('.', "Scala.js".substring(3), 2)
    test('s', "Scala.js".substring(5, 8), 2)
  }

  // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
  @Test def charAtIndexOutOfBounds(): Unit = {

    def test(s: String, i: Int): Unit = {
      val e =
        assertThrows(classOf[StringIndexOutOfBoundsException], s.charAt(i))
      assertTrue(e.getMessage(), e.getMessage().contains(i.toString()))
    }

    test("foo", -1)
    test("foo", -10000)
    test("foo", Int.MinValue)
    test("foo", 3)
    test("foo", 10000)
    test("foo", Int.MaxValue)

    test("", -1)
    test("", 0)
    test("", 1)

    // Test non-constant-folding
    assertThrows(classOf[StringIndexOutOfBoundsException], "foo".charAt(4))
  }

  /* selected Static methods
   */

  // "format" test ported from Scala.js, commit: e10803c, dated: 2024-09-16
  @Test def format(): Unit = {
    assertEquals("5", String.format("%d", new Integer(5)))
    assertEquals("00005", String.format("%05d", new Integer(5)))
    assertEquals("0x005", String.format("%0#5x", new Integer(5)))
    assertEquals("  0x5", String.format("%#5x", new Integer(5)))
    assertEquals("  0X5", String.format("%#5X", new Integer(5)))
    assertEquals("  -10", String.format("%5d", new Integer(-10)))
    assertEquals("-0010", String.format("%05d", new Integer(-10)))
    assertEquals("fffffffd", String.format("%x", new Integer(-3)))

    // SN matches JVM (8 to 23) "fc", Scala.js original expected "fffffffc"
    assertEquals("fc", String.format("%x", new java.lang.Byte(-4.toByte)))
  }

  @Test def joinVarargs(): Unit = {
    val strings = Array("one", "two", "three")
    val delimiter = "-%-"

    val expected = s"${strings(0)}${delimiter}" +
      s"${strings(1)}${delimiter}" +
      s"${strings(2)}"
    val joined = String.join(delimiter, strings(0), strings(1), strings(2))

    assertEquals(
      s"unexpected join",
      expected,
      joined
    )
  }

  @Test def joinIterable(): Unit = {
    val strings = new java.util.ArrayList[String](3)
    strings.add("zeta")
    strings.add("eta")
    strings.add("theta")

    val delimiter = "-*-"

    val expected = s"${strings.get(0)}${delimiter}" +
      s"${strings.get(1)}${delimiter}" +
      s"${strings.get(2)}"
    val joined = String.join(delimiter, strings)

    assertEquals(
      s"unexpected join",
      expected,
      joined
    )
  }

  // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
  @Test def startsWith(): Unit = {
    assertTrue("Scala.js".startsWith("Scala"))
    assertTrue("Scala.js".startsWith("Scala.js"))
    assertFalse("Scala.js".startsWith("scala"))
    assertTrue("ananas".startsWith("an"))

    assertThrows(classOf[NullPointerException], "ananas".startsWith(null))
  }

  // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
  @Test def startsWithOffset(): Unit = {
    assertTrue("Scala.js".startsWith("ala", 2))
    assertTrue("Scala.js".startsWith("Scal", 0))

    assertTrue("Scala.js".startsWith("", 3))
    assertTrue("Scala.js".startsWith("", 0))
    assertTrue("Scala.js".startsWith("", 8))

    assertFalse("Scala.js".startsWith("ala", 0))
    assertFalse("Scala.js".startsWith("Scal", 2))

    assertFalse("Scala.js".startsWith("Sc", -1))
    assertFalse("Scala.js".startsWith(".js", 10))
    assertFalse("Scala.js".startsWith("", -1))
    assertFalse("Scala.js".startsWith("", 9))

    assertThrows(classOf[NullPointerException], "Scala.js".startsWith(null, 2))
  }

  // Ported from Scala.js commit: 37df9c2ea dated: 2025-06-30
  // scalafmt: { maxColumn = 100 }
  @Test def regionMatches(): Unit = {
    // original Scala Native tests
    assertTrue("This is a test".regionMatches(10, "test", 0, 4))
    assertTrue(!("This is a test".regionMatches(10, "TEST", 0, 4)))
    assertTrue("This is a test".regionMatches(0, "This", 0, 4))

    /* Ported from
     * https://github.com/gwtproject/gwt/blob/master/user/test/com/google/gwt/emultest/java/lang/StringTest.java
     */
    val test = "abcdef"

    assertTrue(test.regionMatches(1, "bcd", 0, 3))
    assertTrue(test.regionMatches(1, "bcdx", 0, 3))
    assertFalse(test.regionMatches(1, "bcdx", 0, 4))
    assertFalse(test.regionMatches(1, "bcdx", 1, 3))
    assertTrue(test.regionMatches(true, 0, "XAbCd", 1, 4))
    assertTrue(test.regionMatches(true, 1, "BcD", 0, 3))
    assertTrue(test.regionMatches(true, 1, "bCdx", 0, 3))
    assertFalse(test.regionMatches(true, 1, "bCdx", 0, 4))
    assertFalse(test.regionMatches(true, 1, "bCdx", 1, 3))
    assertTrue(test.regionMatches(true, 0, "xaBcd", 1, 4))

    val testU = test.toUpperCase()
    assertTrue(testU.regionMatches(true, 0, "XAbCd", 1, 4))
    assertTrue(testU.regionMatches(true, 1, "BcD", 0, 3))
    assertTrue(testU.regionMatches(true, 1, "bCdx", 0, 3))
    assertFalse(testU.regionMatches(true, 1, "bCdx", 0, 4))
    assertFalse(testU.regionMatches(true, 1, "bCdx", 1, 3))
    assertTrue(testU.regionMatches(true, 0, "xaBcd", 1, 4))

    /* If len is negative, you must return true in some cases. See
     * http://docs.oracle.com/javase/8/docs/api/java/lang/String.html#regionMatches-boolean-int-java.lang.String-int-int-
     */

    // four cases that are false, irrelevant of sign of len nor the value of the other string
    assertFalse(test.regionMatches(-1, test, 0, -4))
    assertFalse(test.regionMatches(0, test, -1, -4))
    assertFalse(test.regionMatches(100, test, 0, -4))
    assertFalse(test.regionMatches(0, test, 100, -4))

    // offset + len > length
    assertFalse(test.regionMatches(3, "defg", 0, 4)) // on receiver string
    assertFalse(test.regionMatches(3, "abcde", 3, 3)) // on other string
    assertFalse(test.regionMatches(Int.MaxValue, "ab", 0, 1)) // #4878 overflow, large toffset
    assertFalse(test.regionMatches(0, "ab", Int.MaxValue, 1)) // #4878 overflow, large ooffset
    assertFalse(test.regionMatches(1, "ab", 1, Int.MaxValue)) // #4878 overflow, large len
    assertFalse(test.regionMatches(true, 3, "defg", 0, 4)) // on receiver string
    assertFalse(test.regionMatches(true, 3, "abcde", 3, 3)) // on other string
    assertFalse(test.regionMatches(true, Int.MaxValue, "ab", 0, 1)) // #4878 overflow, large toffset
    assertFalse(test.regionMatches(true, 0, "ab", Int.MaxValue, 1)) // #4878 overflow, large ooffset
    assertFalse(test.regionMatches(true, 1, "ab", 1, Int.MaxValue)) // #4878 overflow, large len

    // the strange cases that are true
    assertTrue(test.regionMatches(0, test, 0, -4))
    assertTrue(test.regionMatches(1, "bcdx", 0, -4))
    assertTrue(test.regionMatches(1, "bcdx", 1, -3))
    assertTrue(test.regionMatches(true, 1, "bCdx", 0, -4))
    assertTrue(test.regionMatches(true, 1, "bCdx", 1, -3))
    assertTrue(testU.regionMatches(true, 1, "bCdx", 0, -4))
    assertTrue(testU.regionMatches(true, 1, "bCdx", 1, -3))
  }

  // Appears to be no equivalent test in Scala.js as of this creation date.
  @Test def contentEqualsStringBuffer(): Unit = {
    val data = "Between the devil and the deep blue sea"

    val sbuf = new StringBuffer(data)
    assertTrue("sb.1", data.contentEquals(sbuf))

    sbuf.delete(12, 17) // delete "devil"
    assertFalse("sb.2", data.contentEquals(sbuf))
  }

  // Appears to be no equivalent test in Scala.js as of this creation date.
  @Test def contentEqualsCharSequence(): Unit = {
    val data = "and no tar ready"

    // CharSequence: CharBuffer
    val cb = CharBuffer.allocate(data.length + 1)
    cb.put(data)
    val endOfDataMark = cb.position()

    cb.flip()

    val cbString = cb.toString()

    assertEquals("cb.1", data, cbString)

    assertTrue("cb.2", data.contentEquals(cb))

    // It ought not to be this hard to append here, but that appears to be so.
    cb.position(endOfDataMark) // advance to original position
    cb.limit(endOfDataMark + 1) // make room for next put()

    cb.append('!')

    cb.position(0)

    assertFalse("cb.3", data.contentEquals(cb))

    // CharSequence: String
    assertTrue("str.1", data.contentEquals(data)) // self == self

    // CharSequence: StringBuilder
    val sbldr = new StringBuilder(data)
    assertTrue("sbldr.1", data.contentEquals(sbldr.toString()))

    sbldr.delete(4, 6)
    assertFalse("sbldr.2", data.contentEquals(sbldr.toString()))

    // CharSequence: String
    assertTrue("sbldr.3", data.contentEquals(data)) // self == self

    // but not substring "ready"
    assertFalse("sbldr.4", data.contentEquals(data.substring(11, data.length)))
  }

  // Ported from Scala.js commit: e10803c   dated: 2024-09-16
  @Test def contains(): Unit = {
    assertTrue("Scala.js".contains("Scala"))
    assertTrue("Scala.js".contains("Scala.js"))
    assertTrue("ananas".contains("na"))
    assertFalse("Scala.js".contains("scala"))

    // Scala Native additions

    // empty strings
    assertTrue("c.1", "".contains(""))
    assertTrue("c.2", "Scala Native".contains(""))
    assertFalse("c.3", "".contains("scala"))

    // needle > haystack
    assertFalse("c.4", "Scala Native".contains("Scala NativeN"))
  }
}
