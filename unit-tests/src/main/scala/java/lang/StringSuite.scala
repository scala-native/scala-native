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
}
