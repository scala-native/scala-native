package java.lang

// Ported from Scala.js

object StringBuilderSuite extends tests.Suite {

  def newBuilder: java.lang.StringBuilder =
    new java.lang.StringBuilder

  def initBuilder(str: String): java.lang.StringBuilder =
    new java.lang.StringBuilder(str)

  test("append") {
    assertEquals("asdf", newBuilder.append("asdf").toString)
    assertEquals("null", newBuilder.append(null: AnyRef).toString)
    assertEquals("null", newBuilder.append(null: String).toString)
    assertEquals("nu", newBuilder.append(null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuilder.append(true).toString)
    assertEquals("a", newBuilder.append('a').toString)
    assertEquals("abcd", newBuilder.append(Array('a', 'b', 'c', 'd')).toString)
    assertEquals("bc",
                 newBuilder.append(Array('a', 'b', 'c', 'd'), 1, 2).toString)
    assertEquals("4", newBuilder.append(4.toByte).toString)
    assertEquals("304", newBuilder.append(304.toShort).toString)
    assertEquals("100000", newBuilder.append(100000).toString)
  }

  testFails("append float", issue = 481) {
    assertEquals("2.5", newBuilder.append(2.5f).toString)
    assertEquals("3.5", newBuilder.append(3.5).toString)
  }

  test("insert") {
    assertEquals("asdf", newBuilder.insert(0, "asdf").toString)
    assertEquals("null", newBuilder.insert(0, null: AnyRef).toString)
    assertEquals("null", newBuilder.insert(0, null: String).toString)
    assertEquals("nu", newBuilder.insert(0, null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuilder.insert(0, true).toString)
    assertEquals("a", newBuilder.insert(0, 'a').toString)
    assertEquals("abcd",
                 newBuilder.insert(0, Array('a', 'b', 'c', 'd')).toString)
    assertEquals(
      "bc",
      newBuilder.insert(0, Array('a', 'b', 'c', 'd'), 1, 2).toString)
    assertEquals("4", newBuilder.insert(0, 4.toByte).toString)
    assertEquals("304", newBuilder.insert(0, 304.toShort).toString)
    assertEquals("100000", newBuilder.insert(0, 100000).toString)

    assertEquals("abcdef", initBuilder("adef").insert(1, "bc").toString)
    assertEquals("abcdef", initBuilder("abcd").insert(4, "ef").toString)
    assertEquals("abcdef",
                 initBuilder("adef").insert(1, Array('b', 'c')).toString)
    assertEquals("abcdef",
                 initBuilder("adef").insert(1, initBuilder("bc")).toString)
    assertEquals("abcdef",
                 initBuilder("abef")
                   .insert(2, Array('a', 'b', 'c', 'd', 'e'), 2, 2)
                   .toString)

    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuilder("abcd").insert(-1, "whatever"))
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuilder("abcd").insert(5, "whatever"))
  }

  testFails("insert float", issue = 481) {
    assertEquals("2.5", newBuilder.insert(0, 2.5f).toString)
    assertEquals("3.5", newBuilder.insert(0, 3.5).toString)
  }

  // TODO: segfaults with EXC_BAD_ACCESS (code=1, address=0x0)
  // testFails("insert string builder", issue = -1) {
  //   assertEquals("abcdef", initBuilder("abef").insert(2, initBuilder("abcde"), 2, 4).toString)
  // }

  test("should_allow_string_interpolation_to_survive_null_and_undefined") {
    assertEquals("null", s"${null}")
  }

  test("deleteCharAt") {
    assertEquals("023", initBuilder("0123").deleteCharAt(1).toString)
    assertEquals("123", initBuilder("0123").deleteCharAt(0).toString)
    assertEquals("012", initBuilder("0123").deleteCharAt(3).toString)
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuilder("0123").deleteCharAt(-1))
    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuilder("0123").deleteCharAt(4))
  }

  test("replace") {
    assertEquals("0bc3", initBuilder("0123").replace(1, 3, "bc").toString)
    assertEquals("abcd", initBuilder("0123").replace(0, 4, "abcd").toString)
    assertEquals("abcd", initBuilder("0123").replace(0, 10, "abcd").toString)
    assertEquals("012defg",
                 initBuilder("0123").replace(3, 10, "defg").toString)
    assertEquals("xxxx123", initBuilder("0123").replace(0, 1, "xxxx").toString)
    assertEquals("0xxxx123",
                 initBuilder("0123").replace(1, 1, "xxxx").toString)
    assertEquals("0123x", initBuilder("0123").replace(4, 5, "x").toString)

    expectThrows(classOf[StringIndexOutOfBoundsException],
                 initBuilder("0123").replace(-1, 3, "x"))
  }

  test("setCharAt") {
    val b = newBuilder
    b.append("foobar")

    b.setCharAt(2, 'x')
    assertEquals("foxbar", b.toString)

    b.setCharAt(5, 'h')
    assertEquals("foxbah", b.toString)

    expectThrows(classOf[StringIndexOutOfBoundsException],
                 b.setCharAt(-1, 'h'))
    expectThrows(classOf[StringIndexOutOfBoundsException], b.setCharAt(6, 'h'))
  }

  test("ensureCapacity") {
    // test that ensureCapacity is linking
    newBuilder.ensureCapacity(10)
  }

  test("should_properly_setLength") {
    val b = newBuilder
    b.append("foobar")

    expectThrows(classOf[StringIndexOutOfBoundsException], b.setLength(-3))

    assertEquals("foo", { b.setLength(3); b.toString })
    assertEquals("foo\u0000\u0000\u0000", { b.setLength(6); b.toString })
  }

  test("appendCodePoint") {
    val b = newBuilder
    b.appendCodePoint(0x61)
    assertEquals("a", b.toString)
    b.appendCodePoint(0x10000)
    assertEquals("a\uD800\uDC00", b.toString)
    b.append("fixture")
    b.appendCodePoint(0x00010FFFF)
    assertEquals("a\uD800\uDC00fixture\uDBFF\uDFFF", b.toString)
  }
}
