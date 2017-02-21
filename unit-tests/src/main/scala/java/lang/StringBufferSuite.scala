package java.lang

// Ported from Scala.js

object StringBufferSuite extends tests.Suite {

  def newBuf: java.lang.StringBuffer =
    new java.lang.StringBuffer

  def initBuf(str: String): java.lang.StringBuffer =
    new java.lang.StringBuffer(str)

  test("append") {
    assertEquals("asdf", newBuf.append("asdf").toString)
    assertEquals("null", newBuf.append(null: AnyRef).toString)
    assertEquals("null", newBuf.append(null: String).toString)
    assertEquals("nu", newBuf.append(null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuf.append(true).toString)
    assertEquals("a", newBuf.append('a').toString)
    assertEquals("abcd", newBuf.append(Array('a', 'b', 'c', 'd')).toString)
    assertEquals("bc", newBuf.append(Array('a', 'b', 'c', 'd'), 1, 2).toString)
    assertEquals("4", newBuf.append(4.toByte).toString)
    assertEquals("304", newBuf.append(304.toShort).toString)
    assertEquals("100000", newBuf.append(100000).toString)
  }

  testFails("append float", issue = 481) {
    assertEquals("2.5", newBuf.append(2.5f).toString)
    assertEquals("3.5", newBuf.append(3.5).toString)
  }

  test("insert") {
    assertEquals("asdf", newBuf.insert(0, "asdf").toString)
    assertEquals("null", newBuf.insert(0, null: AnyRef).toString)
    assertEquals("null", newBuf.insert(0, null: String).toString)
    assertEquals("nu", newBuf.insert(0, null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuf.insert(0, true).toString)
    assertEquals("a", newBuf.insert(0, 'a').toString)
    assertEquals("abcd", newBuf.insert(0, Array('a', 'b', 'c', 'd')).toString)
    assertEquals("bc",
                 newBuf.insert(0, Array('a', 'b', 'c', 'd'), 1, 2).toString)
    assertEquals("4", newBuf.insert(0, 4.toByte).toString)
    assertEquals("304", newBuf.insert(0, 304.toShort).toString)
    assertEquals("100000", newBuf.insert(0, 100000).toString)

    assertEquals("abcdef", initBuf("adef").insert(1, "bc").toString)
    assertEquals("abcdef", initBuf("abcd").insert(4, "ef").toString)
    assertEquals("abcdef", initBuf("adef").insert(1, Array('b', 'c')).toString)
    assertEquals("abcdef", initBuf("adef").insert(1, initBuf("bc")).toString)
    assertEquals(
      "abcdef",
      initBuf("abef").insert(2, Array('a', 'b', 'c', 'd', 'e'), 2, 2).toString)

    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuf("abcd").insert(-1, "whatever"))
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuf("abcd").insert(5, "whatever"))
  }

  testFails("insert float", issue = 481) {
    assertEquals("2.5", newBuf.insert(0, 2.5f).toString)
    assertEquals("3.5", newBuf.insert(0, 3.5).toString)
  }

  // TODO: segfaults with EXC_BAD_ACCESS (code=1, address=0x0)
  // testFails("insert string buffer", issue = -1) {
  //   assertEquals("abcdef", initBuf("abef").insert(2, initBuf("abcde"), 2, 4).toString)
  // }

  test("deleteCharAt") {
    assertEquals("023", initBuf("0123").deleteCharAt(1).toString)
    assertEquals("123", initBuf("0123").deleteCharAt(0).toString)
    assertEquals("012", initBuf("0123").deleteCharAt(3).toString)
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuf("0123").deleteCharAt(-1))
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuf("0123").deleteCharAt(4))
  }

  test("replace") {
    assertEquals("0bc3", initBuf("0123").replace(1, 3, "bc").toString)
    assertEquals("abcd", initBuf("0123").replace(0, 4, "abcd").toString)
    assertEquals("abcd", initBuf("0123").replace(0, 10, "abcd").toString)
    assertEquals("012defg", initBuf("0123").replace(3, 10, "defg").toString)
    assertEquals("xxxx123", initBuf("0123").replace(0, 1, "xxxx").toString)
    assertEquals("0xxxx123", initBuf("0123").replace(1, 1, "xxxx").toString)
    assertEquals("0123x", initBuf("0123").replace(4, 5, "x").toString)

    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuf("0123").replace(-1, 3, "x"))
  }

  test("setCharAt") {
    val buf = newBuf
    buf.append("foobar")

    buf.setCharAt(2, 'x')
    assertEquals("foxbar", buf.toString)

    buf.setCharAt(5, 'h')
    assertEquals("foxbah", buf.toString)

    expectThrows(classOf[StringIndexOutOfBoundsException],
                 buf.setCharAt(-1, 'h'))
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 buf.setCharAt(6, 'h'))
  }

  test("ensureCapacity") {
    // test that ensureCapacity is linking
    newBuf.ensureCapacity(10)
  }

  test("should_properly_setLength") {
    val buf = newBuf
    buf.append("foobar")

    expectThrows(classOf[StringIndexOutOfBoundsException], buf.setLength(-3))

    assertEquals("foo", { buf.setLength(3); buf.toString })
    assertEquals("foo\u0000\u0000\u0000", { buf.setLength(6); buf.toString })
  }

  test("appendCodePoint") {
    val buf = newBuf
    buf.appendCodePoint(0x61)
    assertEquals("a", buf.toString)
    buf.appendCodePoint(0x10000)
    assertEquals("a\uD800\uDC00", buf.toString)
    buf.append("fixture")
    buf.appendCodePoint(0x00010FFFF)
    assertEquals("a\uD800\uDC00fixture\uDBFF\uDFFF", buf.toString)
  }
}
