package java.lang

object StringSuite extends tests.Suite {

  test("String(Array[Byte], Int, Int, String) with null encoding") {
    assertThrows[java.lang.NullPointerException] {
      new String("I don't like nulls".getBytes, 0, 3, null: String)
    }
  }

  test("String(Array[Byte], Int, Int, String) with unsupported encoding") {
    assertThrows[java.io.UnsupportedEncodingException] {
      new String("Pacem in terris".getBytes, 0, 3, "unsupported encoding")
    }
  }

  test("String(Array[Byte], String) with null encoding") {
    assertThrows[java.lang.NullPointerException] {
      new String("Nulls are just as bad".getBytes, null: String)
    }
  }

  test("String(Array[Byte], String) with unsupported encoding") {
    assertThrows[java.io.UnsupportedEncodingException] {
      new String("to people of goodwill.".getBytes, "unsupported encoding")
    }
  }

  test("String(Array[Byte], start, length) with invalid start or length") {
    val chars: Array[Char] = Array('a', 'b', 'c')

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      new String(chars, -1, chars.length) // invalid start
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      new String(chars, 0, chars.length + 1) // invalid length
    }
  }

  test("String(Array[Int], offset, count) with invalid offset or count") {
    val codePoints = Array[Int](235, 872, 700, 298)

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      new String(codePoints, -1, codePoints.length) // invalid offset
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      new String(codePoints, 0, codePoints.length + 1) // invalid length
    }
  }

  test("+") {
    assert("big 5" == "big " + 5.toByte)
    assert("big 5" == "big " + 5.toShort)
    assert("big 5" == "big " + 5)
    assert("big 5" == "big " + 5L)
    assert("5 big" == 5.toByte + " big")
    assert("5 big" == 5.toShort + " big")
    assert("5 big" == 5 + " big")
    assert("5 big" == 5L + " big")
    assert("foo" == "foo" + "")
    assert("foo" == "" + "foo")
    assert("foobar" == "foo" + "bar")
    assert("foobarbaz" == "foo" + "bar" + "baz")
  }

  test("codePointAt(index) with invalid index") {
    val data = "When in the Course"

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      data.codePointAt(-1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      data.codePointAt(data.length + 1)
    }
  }

  test("codePointBefore(index) with invalid index") {
    val data = "of human events"

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      data.codePointBefore(-1)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      // Careful here, +1 is valid +2 is not
      data.codePointBefore(data.length + 2)
    }
  }

  test("codePointCount(beginIndex, endIndex) with invalid begin | end index") {
    val data = "it becomes necessary"

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      data.codePointCount(-1, data.length)
    }

    assertThrows[java.lang.StringIndexOutOfBoundsException] {
      data.codePointCount(0, data.length + 1)
    }
  }

  test("compareTo") {
    assert("test".compareTo("utest") < 0)
    assert("test".compareTo("test") == 0)
    assert("test".compareTo("stest") > 0)
    assert("test".compareTo("tess") > 0)
  }

  test("compareToIgnoreCase") {
    assert("test".compareToIgnoreCase("Utest") < 0)
    assert("test".compareToIgnoreCase("Test") == 0)
    assert("Test".compareToIgnoreCase("stest") > 0)
    assert("tesT".compareToIgnoreCase("teSs") > 0)
  }

  test("equalsIgnoreCase") {
    assert("test".equalsIgnoreCase("TEST"))
    assert("TEst".equalsIgnoreCase("teST"))
    assert(!("SEst".equalsIgnoreCase("TEss")))
  }

  test("regionMatches") {
    assert("This is a test".regionMatches(10, "test", 0, 4))
    assert(!("This is a test".regionMatches(10, "TEST", 0, 4)))
    assert("This is a test".regionMatches(0, "This", 0, 4))
  }

  test("replace Char") {
    assert("test".replace('t', 'p') equals "pesp")
    assert("Test".replace('t', 'p') equals "Tesp")
    assert("Test".replace('T', 'p') equals "pest")
    assert("Test".replace('0', '1') equals "Test")
  }

  test("replace CharSequence") {
    // Runs assertion with and without prefix and suffix
    def check(input: String, replace: String => Boolean) = {
      assert(replace(input))

      val inputWithPrefix = ("[" + input).substring(1)
      assert(inputWithPrefix equals input)
      assert(replace(inputWithPrefix))

      val inputWithSuffix = (input + "]").substring(0, input.length)
      assert(inputWithSuffix equals input)
      assert(replace(inputWithSuffix))

      val inputWithBoth = ("[" + input + "]").substring(1, input.length + 1)
      assert(inputWithBoth equals input)
      assert(replace(inputWithBoth))
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

  test("replaceAll non-ascii") {
    val greetings = "Gruesze"

    val greetingsWithUmlaut = greetings.replaceAll("ue", "ü")
    assert(greetingsWithUmlaut == "Grüsze")

    val greetingsWithUmlautAndSharpS = greetingsWithUmlaut.replaceAll("sz", "ß")
    assert(greetingsWithUmlautAndSharpS == "Grüße")

    assert("Grueszszszeszszszszsze".replaceAll("sz", "ß") == "Grueßßßeßßßßße")
  }

  test("replaceAllLiterally with $ in replacement, issue #1070") {
    val literal     = "{.0}"
    val replacement = "\\$ipsum"
    val prefix      = "Lorem "
    val suffix      = " dolor"
    val text        = prefix + literal + suffix
    val expected    = prefix + replacement + suffix

    assert(text.replaceAllLiterally(literal, replacement) == expected)
  }

  private implicit class StringOps(val s: String) extends AnyVal {
    def splitVec(sep: String, limit: Int = 0) = s.split(sep, limit).toVector
  }
  private def splitTest(sep: String, splitExpr: Option[String] = None) = {
    val splitSep = splitExpr getOrElse sep
    val n        = 4
    val limit    = 2

    assert("".splitVec(splitSep) == Vector(""))
    assert("".splitVec(splitSep, limit) == Vector(""))

    val noSep = "b"
    assert(noSep.splitVec(splitSep) == Vector(noSep))
    assert(noSep.splitVec(splitSep, limit) == Vector(noSep))

    (1 to n) foreach { i =>
      val allSep = sep * n
      assert(allSep.splitVec(splitSep) == Vector.empty)
      assert(
        allSep.splitVec(splitSep, n) == (0 until (n - 1))
          .map(_ => "")
          .toVector :+ sep)
      assert(
        allSep.splitVec(splitSep, limit) == (0 until (limit - 1))
          .map(_ => "")
          .toVector :+ allSep.drop((limit - 1) * sep.length))
    }

    val oneSep = noSep + sep
    assert(oneSep.splitVec(splitSep) == Vector(noSep))
    assert(oneSep.splitVec(splitSep, 1) == Vector(oneSep))
    assert(oneSep.splitVec(splitSep, 2) == Vector(noSep, ""))

    val twoSep = oneSep * 2
    assert(twoSep.splitVec(splitSep) == Vector(noSep, noSep))
    assert(twoSep.splitVec(splitSep, 1) == Vector(twoSep))
    assert(twoSep.splitVec(splitSep, 2) == Vector(noSep, oneSep))
    assert(twoSep.splitVec(splitSep, 3) == Vector(noSep, noSep, ""))

    val leadingSep = sep + noSep
    assert(leadingSep.splitVec(splitSep) == Vector("", noSep))
    assert(leadingSep.splitVec(splitSep, 1) == Vector(leadingSep))
    assert(leadingSep.splitVec(splitSep, 2) == Vector("", noSep))
    assert(leadingSep.splitVec(splitSep, 3) == Vector("", noSep))

    val trailingSep = noSep + sep
    assert(trailingSep.splitVec(splitSep) == Vector(noSep))
    assert(trailingSep.splitVec(splitSep, 1) == Vector(trailingSep))
    assert(trailingSep.splitVec(splitSep, 2) == Vector(noSep, ""))
    assert(trailingSep.splitVec(splitSep, 3) == Vector(noSep, ""))

    val leadingPlusTrailing = sep + noSep + sep
    assert(leadingPlusTrailing.splitVec(splitSep) == Vector("", noSep))
    assert(
      leadingPlusTrailing.splitVec(splitSep, 1) == Vector(leadingPlusTrailing))
    assert(leadingPlusTrailing.splitVec(splitSep, 2) == Vector("", oneSep))
    assert(leadingPlusTrailing.splitVec(splitSep, 3) == Vector("", noSep, ""))
    assert(leadingPlusTrailing.splitVec(splitSep, 4) == Vector("", noSep, ""))
  }
  test("split") {
    splitTest("a")
    splitTest(".", splitExpr = Some("\\."))
    splitTest("ab", splitExpr = Some("ab"))
    splitTest("ab", splitExpr = Some("(ab)"))
  }

  test("getBytes") {
    val b = new Array[scala.Byte](4)
    "This is a test".getBytes(10, 14, b, 0)
    assert(new String(b) equals "test")
  }

  test("getBytes unsupported encoding") {
    assertThrows[java.io.UnsupportedEncodingException] {
      "This is a test".getBytes("unsupported encoding")
    }
  }

  test("literals have consistent hash code implementation") {
    assert(
      "foobar".hashCode == new String(Array('f', 'o', 'o', 'b', 'a', 'r')).hashCode)
  }

  testFails("intern", issue = 486) {
    val chars = Array('f', 'o', 'o', 'b', 'a', 'r')
    val s1    = new String(chars)
    val s2    = new String(chars)
    assert(s1.intern eq s2.intern)
  }

  test("indexOf") {
    assert("afoobar".indexOf("a") == 0)
    assert("afoobar".indexOf(97) == 0)
    assert("afoobar".indexOf("a", 1) == 5)
    assert("afoobar".indexOf(97, 1) == 5)
    assert("".indexOf("a") == -1)
    assert("".indexOf(97) == -1)
    assert("".indexOf("a", 4) == -1)
    assert("".indexOf(97, 4) == -1)
    assert("fubår".indexOf("a") == -1)
    assert("fubår".indexOf(97) == -1)
    assert("fubår".indexOf("a", 4) == -1)
    assert("fubår".indexOf(97, 4) == -1)
  }

  test("lastIndexOf") {
    assert("afoobar".lastIndexOf("a") == 5)
    assert("afoobar".lastIndexOf(97) == 5)
    assert("afoobar".lastIndexOf("a", 4) == 0)
    assert("afoobar".lastIndexOf(97, 4) == 0)
    assert("".lastIndexOf("a") == -1)
    assert("".lastIndexOf(97) == -1)
    assert("".lastIndexOf("a", 4) == -1)
    assert("".lastIndexOf(97, 4) == -1)
    assert("fubår".lastIndexOf("a") == -1)
    assert("fubår".lastIndexOf(97) == -1)
    assert("fubår".lastIndexOf("a", 4) == -1)
    assert("fubår".lastIndexOf(97, 4) == -1)
  }

  test("toUpperCase") {
    assert("".toUpperCase() equals "")
    // ascii
    assert("Hello".toUpperCase() equals "HELLO")
    // latin
    assert("Perché".toUpperCase() equals "PERCHÉ")
    // high (2 Char String) - 0x10400 or \ud801\udc00
    val iStr = new String(Character.toChars(0x10400))
    assert(iStr.length equals 2)
    assert(iStr.toUpperCase equals iStr)
    val bStr = "\ud801\udc00"
    assert(bStr.length equals 2)
    assert(bStr.toUpperCase equals "\ud801\udc00")
    assert("𐐨aaaa".toUpperCase equals "𐐀AAAA")
    assert("aaaa𐐨".toUpperCase equals "AAAA𐐀")
    assert("aa𐐨aa".toUpperCase equals "AA𐐀AA")
    // partial in surrogate range
    // case of poor slicing or construction of string
    assert("\ud801aaaa".toUpperCase equals "\ud801AAAA")
    assert("aaaa\ud801".toUpperCase equals "AAAA\ud801")
    assert("\udc00aaaa".toUpperCase equals "\udc00AAAA")
    assert("aaaa\udc00".toUpperCase equals "AAAA\udc00")
    // case of one high surrogate
    val hChar = '\ud801'
    val hStr  = hChar.toString
    assert(Character.isHighSurrogate(hChar) equals true)
    assert(hStr.length equals 1)
    assert(hStr.toUpperCase equals hStr)
    // toUpperCase should consider String's offset
    assert(
      "Hi, Scala Native!"
        .subSequence(4, 16)
        .toString
        .toUpperCase equals "SCALA NATIVE")
  }

  test("toLowerCase") {
    assert("".toLowerCase() equals "")
    assert("Hello".toLowerCase() equals "hello")
    assert("PERCHÉ".toLowerCase() equals "perché")
    assert("𐐀AAAA".toLowerCase equals "𐐨aaaa")
    assert("AAAA𐐀".toLowerCase equals "aaaa𐐨")
    assert("AA𐐀AA".toLowerCase equals "aa𐐨aa")
    // toLowerCase should consider String's offset
    assert(
      "Hi, Scala Native!"
        .subSequence(4, 16)
        .toString
        .toLowerCase equals "scala native")
  }
}
