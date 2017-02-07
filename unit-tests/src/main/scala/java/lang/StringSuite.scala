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

  test("replace") {
    assert("test".replace('t', 'p') equals "pesp")
    assert("Test".replace('t', 'p') equals "Tesp")
    assert("Test".replace('T', 'p') equals "pest")
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
}
