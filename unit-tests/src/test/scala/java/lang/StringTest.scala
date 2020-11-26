package java.lang

import java.nio.charset.{Charset, StandardCharsets}

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class StringTest {

  @Test def stringArrayByteIntIntStringWithNullEncoding(): Unit = {
    assertThrows(classOf[java.lang.NullPointerException],
                 new String("I don't like nulls".getBytes, 0, 3, null: String))
  }

  @Test def stringArrayByteIntIntStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new String("Pacem in terris".getBytes, 0, 3, "unsupported encoding"))
  }

  @Test def stringArrayByteStringWithNullEncoding(): Unit = {
    assertThrows(classOf[java.lang.NullPointerException],
                 new String("Nulls are just as bad".getBytes, null: String))
  }

  @Test def stringArrayByteStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new String("to people of goodwill.".getBytes, "unsupported encoding"))
  }

  @Test def stringArrayByteStartLengthWithInvalidStartOrLength(): Unit = {
    val chars: Array[Char] = Array('a', 'b', 'c')

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 new String(chars, -1, chars.length) // invalid start
    )

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 new String(chars, 0, chars.length + 1) // invalid length
    )
  }

  @Test def stringArrayIntOffsetCountWithInvalidOffsetOrCount(): Unit = {
    val codePoints = Array[Int](235, 872, 700, 298)

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
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
    assertTrue("5 big" == 5.toByte + " big")
    assertTrue("5 big" == 5.toShort + " big")
    assertTrue("5 big" == 5 + " big")
    assertTrue("5 big" == 5L + " big")
    assertTrue("foo" == "foo" + "")
    assertTrue("foo" == "" + "foo")
    assertTrue("foobar" == "foo" + "bar")
    assertTrue("foobarbaz" == "foo" + "bar" + "baz")
  }

  @Test def codePointAtIndexWithInvalidIndex(): Unit = {
    val data = "When in the Course"

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 data.codePointAt(-1))

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 data.codePointAt(data.length + 1))
  }

  @Test def codePointBeforeIndexWithInvalidIndex(): Unit = {
    val data = "of human events"

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 data.codePointBefore(-1))

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 // Careful here, +1 is valid +2 is not
                 data.codePointBefore(data.length + 2))
  }

  @Test def codePointCountBeginIndexEndIndexWithInvalidBeginOrEndIndex()
      : Unit = {
    val data = "it becomes necessary"

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 data.codePointCount(-1, data.length))

    assertThrows(classOf[java.lang.StringIndexOutOfBoundsException],
                 data.codePointCount(0, data.length + 1))
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
  }

  @Test def equalsIgnoreCase(): Unit = {
    assertTrue("test".equalsIgnoreCase("TEST"))
    assertTrue("TEst".equalsIgnoreCase("teST"))
    assertTrue(!("SEst".equalsIgnoreCase("TEss")))
  }

  @Test def regionMatches(): Unit = {
    assertTrue("This is a test".regionMatches(10, "test", 0, 4))
    assertTrue(!("This is a test".regionMatches(10, "TEST", 0, 4)))
    assertTrue("This is a test".regionMatches(0, "This", 0, 4))
  }

  @Test def replaceChar(): Unit = {
    assertTrue("test".replace('t', 'p') equals "pesp")
    assertTrue("Test".replace('t', 'p') equals "Tesp")
    assertTrue("Test".replace('T', 'p') equals "pest")
    assertTrue("Test".replace('0', '1') equals "Test")
  }

  @Test def replaceCharSequence(): Unit = {
    // Runs assertion with and without prefix and suffix
    def check(input: String, replace: String => Boolean) = {
      assertTrue(replace(input))

      val inputWithPrefix = ("[" + input).substring(1)
      assertTrue(inputWithPrefix equals input)
      assertTrue(replace(inputWithPrefix))

      val inputWithSuffix = (input + "]").substring(0, input.length)
      assertTrue(inputWithSuffix equals input)
      assertTrue(replace(inputWithSuffix))

      val inputWithBoth = ("[" + input + "]").substring(1, input.length + 1)
      assertTrue(inputWithBoth equals input)
      assertTrue(replace(inputWithBoth))
    }

    check("test", _.replace("t", "p") equals "pesp")
    check("Test", _.replace("t", "p") equals "Tesp")
    check("test", _.replace("e", "oa") equals "toast")
    check("Test", _.replace("T", "p") equals "pest")
    check("spantanplans", _.replace("an", ".") equals "sp.t.pl.s")
    check("spantanplans", _.replace("an", "") equals "sptpls")
    check("Test", _.replace("0", "1") equals "Test")
    check("Test", _.replace("e", "") equals "Tst")
    check("Test", _.replace("t", "") equals "Tes")
    check("Test", _.replace("", "") equals "Test")
    check("Test", _.replace("", "--") equals "--T--e--s--t--")
  }

  @Test def replaceAllNonAscii(): Unit = {
    val greetings = "Gruesze"

    val greetingsWithUmlaut = greetings.replaceAll("ue", "ü")
    assertTrue(greetingsWithUmlaut == "Grüsze")

    val greetingsWithUmlautAndSharpS = greetingsWithUmlaut.replaceAll("sz", "ß")
    assertTrue(greetingsWithUmlautAndSharpS == "Grüße")

    assertTrue(
      "Grueszszszeszszszszsze".replaceAll("sz", "ß") == "Grueßßßeßßßßße")
  }

  @Test def replaceAllLiterallyWithDollarSignInReplacementIssue1070(): Unit = {
    val literal     = "{.0}"
    val replacement = "\\$ipsum"
    val prefix      = "Lorem "
    val suffix      = " dolor"
    val text        = prefix + literal + suffix
    val expected    = prefix + replacement + suffix

    assertTrue(text.replaceAllLiterally(literal, replacement) == expected)
  }

  private def splitVec(s: String, sep: String, limit: Int = 0) =
    s.split(sep, limit).toVector

  private def splitTest(sep: String, splitExpr: Option[String] = None) = {
    val splitSep = splitExpr getOrElse sep
    val n        = 4
    val limit    = 2

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
          .toVector :+ sep)
      assertTrue(
        splitVec(allSep, splitSep, limit) == (0 until (limit - 1))
          .map(_ => "")
          .toVector :+ allSep.drop((limit - 1) * sep.length))
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
      splitVec(leadingPlusTrailing, splitSep, 1) == Vector(leadingPlusTrailing))
    assertTrue(splitVec(leadingPlusTrailing, splitSep, 2) == Vector("", oneSep))
    assertTrue(
      splitVec(leadingPlusTrailing, splitSep, 3) == Vector("", noSep, ""))
    assertTrue(
      splitVec(leadingPlusTrailing, splitSep, 4) == Vector("", noSep, ""))
  }
  @Test def split(): Unit = {
    splitTest("a")
    splitTest(".", splitExpr = Some("\\."))
    splitTest("ab", splitExpr = Some("ab"))
    splitTest("ab", splitExpr = Some("(ab)"))
  }

  @Test def getBytes(): Unit = {
    val b = new Array[scala.Byte](4)
    // This form of getBytes() has been depricated since JDK 1.1
    "This is a test".getBytes(10, 14, b, 0)
    assertTrue(new String(b) equals "test")
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

    val bytes         = text.getBytes(charset)
    val expectedBytes = expectedInts.map(i => java.lang.Byte.valueOf(i.toByte))
    val expected      = Array[java.lang.Byte](expectedBytes: _*)
    assertTrue("result != expected}", bytes.sameElements(expected))
  }

  @Test def getBytesUTF8(): Unit = {

    val expectedInts =
      Seq(0, 9, 10, 65, 90, 97, 122, 48, 57, 64, 126, // one byte unicode
        -61, -97,                                     // two byte unicode
        -28, -71, -90,                                // three byte unicode
        -31, -67, -112, 65                            // four byte unicode
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
    assertThrows(classOf[java.io.UnsupportedEncodingException],
                 "This is a test".getBytes("unsupported encoding"))
  }

  @Test def literalsHaveConsistentHashCodeImplementation(): Unit = {
    assertTrue("foobar".hashCode == new String(
      Array('f', 'o', 'o', 'b', 'a', 'r')).hashCode)
  }

  @Ignore("#486")
  @Test def intern(): Unit = {
    val chars = Array('f', 'o', 'o', 'b', 'a', 'r')
    val s1    = new String(chars)
    val s2    = new String(chars)
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

  @Test def toUpperCase(): Unit = {
    assertTrue("".toUpperCase() equals "")
    // ascii
    assertTrue("Hello".toUpperCase() equals "HELLO")
    // latin
    assertTrue("Perché".toUpperCase() equals "PERCHÉ")
    // high (2 Char String) - 0x10400 or \ud801\udc00
    val iStr = new String(Character.toChars(0x10400))
    assertTrue(iStr.length equals 2)
    assertTrue(iStr.toUpperCase equals iStr)
    val bStr = "\ud801\udc00"
    assertTrue(bStr.length equals 2)
    assertTrue(bStr.toUpperCase equals "\ud801\udc00")
    assertTrue("𐐨aaaa".toUpperCase equals "𐐀AAAA")
    assertTrue("aaaa𐐨".toUpperCase equals "AAAA𐐀")
    assertTrue("aa𐐨aa".toUpperCase equals "AA𐐀AA")
    // partial in surrogate range
    // case of poor slicing or construction of string
    assertTrue("\ud801aaaa".toUpperCase equals "\ud801AAAA")
    assertTrue("aaaa\ud801".toUpperCase equals "AAAA\ud801")
    assertTrue("\udc00aaaa".toUpperCase equals "\udc00AAAA")
    assertTrue("aaaa\udc00".toUpperCase equals "AAAA\udc00")
    // case of one high surrogate
    val hChar = '\ud801'
    val hStr  = hChar.toString
    assertTrue(Character.isHighSurrogate(hChar))
    assertTrue(hStr.length equals 1)
    assertTrue(hStr.toUpperCase equals hStr)
    // toUpperCase should consider String's offset
    assertTrue(
      "Hi, Scala Native!"
        .subSequence(4, 16)
        .toString
        .toUpperCase equals "SCALA NATIVE")
  }

  @Test def toLowerCase(): Unit = {
    assertTrue("".toLowerCase() equals "")
    assertTrue("Hello".toLowerCase() equals "hello")
    assertTrue("PERCHÉ".toLowerCase() equals "perché")
    assertTrue("𐐀AAAA".toLowerCase equals "𐐨aaaa")
    assertTrue("AAAA𐐀".toLowerCase equals "aaaa𐐨")
    assertTrue("AA𐐀AA".toLowerCase equals "aa𐐨aa")
    // toLowerCase should consider String's offset
    assertTrue(
      "Hi, Scala Native!"
        .subSequence(4, 16)
        .toString
        .toLowerCase equals "scala native")
  }
}
