package java.lang

object StringSuite extends tests.Suite {
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

  test("getBytes") {
    val b = new Array[scala.Byte](4)
    "This is a test".getBytes(10, 14, b, 0)
    assert(new String(b) equals "test")
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
  }

  test("toLowerCase") {
    assert("".toLowerCase() equals "")
    assert("Hello".toLowerCase() equals "hello")
    assert("PERCHÉ".toLowerCase() equals "perché")
    assert("𐐀AAAA".toLowerCase equals "𐐨aaaa")
    assert("AAAA𐐀".toLowerCase equals "aaaa𐐨")
    assert("AA𐐀AA".toLowerCase equals "aa𐐨aa")
  }
}
