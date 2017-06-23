package java.util
package regex

// Tests are inspired by those projects under Apache2 License:
// j2objc: https://github.com/google/j2objc/blob/master/jre_emul/Tests/java/util/regex/MatcherTest.java#L1
// re2: https://github.com/google/re2/blob/master/re2/testing/re2_test.cc

object MatcherSuite extends tests.Suite {
  test("quoteReplacement") {
    assertEquals(
      Matcher.quoteReplacement(""),
      ""
    )
  }

  test("match") {
    val m = matcher("foo", "foobar")
    assert(!m.matches())
  }

  test("replaceAll") {
    assertEquals(
      matcher("abc", "abcabcabc").replaceAll("z"),
      "zzz"
    )
  }

  test("replaceFirst") {
    assertEquals(
      matcher("abc", "abcabcabc").replaceFirst("z"),
      "zabcabc"
    )
  }

  test("group") {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assertEquals(groupCount, 2)

    assertThrowsAnd[IllegalStateException](group)(
      _.getMessage == "No match found"
    )

    assert(find())
    assertEquals(group, "a12z")
    assertEquals(group(0), "a12z")
    assertEquals(group(1), "1")
    assertEquals(group(2), "2")
    assertThrowsAnd[IndexOutOfBoundsException](group(42))(
      _.getMessage == "No group 42"
    )

    assert(find())
    assertEquals(group, "a34z")
    assertEquals(group(0), "a34z")
    assertEquals(group(1), "3")
    assertEquals(group(2), "4")

    assert(!find())
  }

  test("named group (re2 syntax)") {
    // change pattern to java: "from (?<S>.*) to (?<D>.*)"
    val m = matcher(
      "from (?P<S>.*) to (?P<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assertEquals(group("S"), "Montreal, Canada")
    assertEquals(group("D"), "Lausanne, Switzerland")
    assertThrowsAnd[IllegalArgumentException](group("foo"))(
      _.getMessage == "No group with name <foo>"
    )
  }

  testFails("named group (java syntax)", 620) {
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assertEquals(group("S"), "Montreal, Canada")
    assertEquals(group("D"), "Lausanne, Switzerland")
  }

  test("start(i)/end(i)") {
    val m = matcher("a(\\d)(\\d)z", "012345_a12z_012345")
    import m._

    assertThrowsAnd[IllegalStateException](start)(
      _.getMessage == "No match found"
    )

    assertThrowsAnd[IllegalStateException](end)(
      _.getMessage == "No match found"
    )

    assert(find())

    assertEquals(start, 7)
    assertEquals(end, 11)

    assertEquals(start(0), 7)
    assertEquals(end(0), 11)

    assertEquals(start(1), 8)
    assertEquals(end(1), 9)

    assertEquals(start(2), 9)
    assertEquals(end(2), 10)

    assertThrowsAnd[IndexOutOfBoundsException](start(42))(
      _.getMessage == "No group 42"
    )

    assertThrowsAnd[IndexOutOfBoundsException](end(42))(
      _.getMessage == "No group 42"
    )
  }

  test("start/end") {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assert(find())
    assertEquals(start, 1)
    assertEquals(end, 5)

    assert(find())
    assertEquals(start, 6)
    assertEquals(end, 10)

    assert(!find())
  }

  test("start(name)/end(name) re2 syntax") {
    val m = matcher(
      "from (?P<S>.*) to (?P<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assertEquals(start("S"), 5)
    assertEquals(end("S"), 21)

    assertEquals(start("D"), 25)
    assertEquals(end("D"), 46)

    assertThrowsAnd[IllegalArgumentException](start("foo"))(
      _.getMessage == "No group with name <foo>"
    )

    assertThrowsAnd[IllegalArgumentException](end("foo"))(
      _.getMessage == "No group with name <foo>"
    )
  }

  testFails("start(name)/end(name) java syntax", 620) {
    // change pattern to java: "from (?<S>.*) to (?<D>.*)"
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assertEquals(start("S"), 5)
    assertEquals(end("S"), 21)

    assertEquals(start("D"), 25)
    assertEquals(end("D"), 46)
  }

  test("appendReplacement/appendTail") {
    val buf = new StringBuffer()

    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    while (find()) {
      appendReplacement(buf, "{" + group + "}")
    }
    appendTail(buf)
    assertEquals(buf.toString, "_{a12z}_{a34z}_")
  }

  test("appendReplacement/appendTail with group replacement by index") {
    val buf = new StringBuffer()
    val m   = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._
    while (find()) {
      appendReplacement(buf, "{$0}")
    }
    appendTail(buf)
    assertEquals(buf.toString, "_{a12z}_{a34z}_")
  }

  test("appendReplacement/appendTail with group replacement by name") {
    val buf = new StringBuffer()
    val m = matcher(
      "from (?P<S>.*) to (?P<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._
    while (find()) {
      appendReplacement(buf, "such ${S}, wow ${D}")
    }
    appendTail(buf)
    assertEquals(buf.toString,
                 "such Montreal, Canada, wow Lausanne, Switzerland")
  }

  test("reset") {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assert(find())
    assertEquals(start, 1)
    assertEquals(end, 5)

    reset()

    assert(find())
    assertEquals(start, 1)
    assertEquals(end, 5)

    assert(find())
    assertEquals(start, 6)
    assertEquals(end, 10)

    assert(!find())
  }

  // we don't support lookahead
  testFails("(not supported) hasTransparentBounds/useTransparentBounds", 640) {

    // ?=  <==>  zero-width positive look-ahead
    val m1 = Pattern.compile("foo(?=buzz)").matcher("foobuzz")
    m1.region(0, 3)
    m1.useTransparentBounds(false)
    assert(!m1.matches()) // opaque

    m1.useTransparentBounds(true)
    assert(m1.matches()) // transparent

    // ?!  <==>  zero-width negative look-ahead
    val m2 = Pattern.compile("foo(?!buzz)").matcher("foobuzz")
    m2.region(0, 3)
    m2.useTransparentBounds(false)
    assert(!m2.matches()) // opaque

    m2.useTransparentBounds(true)
    assert(m2.matches()) // transparent
  }

  test("lookingAt") {
    val m1 = matcher("foo", "foobar")
    assert(m1.lookingAt())
  }

  test("issue #852, StringIndexOutOfBoundsException") {
    val JsonNumberRegex =
      """(-)?((?:[1-9][0-9]*|0))(?:\.([0-9]+))?(?:[eE]([-+]?[0-9]+))?""".r
    val JsonNumberRegex(negative, intStr, decStr, expStr) = "0.000000"
    assert(negative.isEmpty)
    assert(intStr == "0")
    assert(decStr == "000000")
    assert(expStr.isEmpty)
  }

  private def matcher(regex: String, text: String): Matcher =
    Pattern.compile(regex).matcher(text)
}
