package java.lang

object StringSuite extends tests.Suite {
  test("concat primitive") {
    assert("big 5" == "big " + 5.toByte)
    assert("big 5" == "big " + 5.toShort)
    assert("big 5" == "big " + 5)
    assert("big 5" == "big " + 5L)
    assert("5 big" == 5.toByte + " big")
    assert("5 big" == 5.toShort + " big")
    assert("5 big" == 5 + " big")
    assert("5 big" == 5L + " big")
  }

  test("concat string") {
    assert("foo" == "foo" + "")
    assert("foo" == "" + "foo")
    assert("foobar" == "foo" + "bar")
    assert("foobarbaz" == "foo" + "bar" + "baz")
  }

  test("String.indexOf") {
    assert("test".indexOf("e", 100) == -1) // indexOf outside the range
    assert("".indexOf("e", 0) == -1)       // empty
    assert("test".indexOf("e", 0) == 1)    // pos1
  }

  test("String.compareTo") {
    assert("test".compareTo("utest") < 0)
    assert("test".compareTo("test") == 0)
    assert("test".compareTo("stest") > 0)
    assert("test".compareTo("tess") > 0)
  }

  //need toUpperCase and toLowerCase from Charset to be implemented for those tests to work.
  /*test("String.compareToIgnoreCase") {
    assert("test".compareToIgnoreCase("Utest") < 0)
    assert("test".compareToIgnoreCase("Test") == 0)
    assert("Test".compareToIgnoreCase("stest") > 0)
    assert("tesT".compareToIgnoreCase("teSs") > 0)
  }*/

  //need toUpperCase and toLowerCase from Charset to be implemented for those tests to work.
  /*test("String.equalsIgnoreCase") {
    assert("test".equalsIgnoreCase("TEST"))
    assert("TEst".equalsIgnoreCase("teST"))
    assert(!("SEst".equalsIgnoreCase("TEss")))
  }*/

  test("String.regionMatches") {
    assert("This is a test".regionMatches(10, "test", 0, 4))
    assert(!("This is a test".regionMatches(10, "TEST", 0, 4)))
    assert("This is a test".regionMatches(0, "This", 0, 4))
  }

  test("String.replace") {
    assert("test".replace('t', 'p') equals "pesp")
    assert("Test".replace('t', 'p') equals "Tesp")
    assert("Test".replace('T', 'p') equals "pest")
  }

  test("String.getBytes") {
    val b = new Array[scala.Byte](4)
    "This is a test".getBytes(10, 14, b, 0)
    assert(new String(b) equals "test")
  }
}
