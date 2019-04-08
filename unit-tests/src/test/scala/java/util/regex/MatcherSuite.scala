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

  test("issue #1070, quoteReplacement should quote backslash and only it") {
    val replacement = "\\fin$\\du.$$monde\\"
    val expected    = "\\\\fin$\\\\du.$$monde\\\\"
    assertEquals(
      Matcher.quoteReplacement(replacement),
      expected
    )
  }

  test("match") {
    val m = matcher("foo", "foobar")
    assert(!m.matches())
  }

  test("find()") {
    val prefix  = "0123"
    val pattern = "abc"
    val noise   = "def"
    val sample  = prefix + pattern + noise + pattern + noise

    val m = Pattern.compile(pattern).matcher(sample)

    val expectedStart1 = prefix.length
    val expectedEnd1   = prefix.length + pattern.length
    assert(m.find(), s"initial find() failed.")
    assert(m.start == expectedStart1,
           s"first start: ${m.start} != expected: $expectedStart1")
    assert(m.end == expectedEnd1,
           s"first end: ${m.end} != expected: $expectedEnd1")

    val expectedStart2 = expectedEnd1 + noise.length
    val expectedEnd2   = expectedStart2 + pattern.length

    assert(m.find(), s"second find() failed.")
    assert(m.start == expectedStart2,
           s"second start: ${m.start} != expected: $expectedStart2")
    assert(m.end == expectedEnd2,
           s"second end: ${m.start} != expected: $expectedEnd2")
  }

  test("find(start) - invalid start values") {
    val pattern = "Isaac"
    val sample  = "Asimov"

    val m = Pattern.compile(pattern).matcher(sample)

    assertThrows[IndexOutOfBoundsException] {
      m.find(-1)
    }

    assertThrows[IndexOutOfBoundsException] {
      m.find(sample.length + 1)
    }
  }

  test("find(start)") {
    val prefix  = "0"
    val pattern = "abc"
    val noise   = "def"
    val sample1 = prefix + pattern + noise
    val sample2 = sample1 + pattern + pattern

    val index = 2 // start at leftmost 'b' in sample.

    val m1 = Pattern.compile(pattern).matcher(sample1)

    val m1f1Result = m1.find(index)

    // Evaluate m1.start and m1.end only in the unexpected case of a match
    // having being found. Calling either if no match was found throws
    // an exception.
    if (m1f1Result) {
      assert(false,
             s"find(${index}) wrongly found start: ${m1.start} end: ${m1.end}")
    }

    val m2 = Pattern.compile(pattern).matcher(sample2)

    assert(m2.find(index),
           s"find(${index}) did not find ${pattern} in ${sample2}")

    val m2ExpectedStart1 = prefix.length + pattern.length + noise.length
    val m2ExpectedEnd1   = m2ExpectedStart1 + pattern.length

    assert(m2.start == m2ExpectedStart1,
           s"first start: ${m2.start} != expected: $m2ExpectedStart1")
    assert(m2.end == m2ExpectedEnd1,
           s"first end: ${m2.end} != expected: $m2ExpectedEnd1")

    // Simple find() after a find(index) should succeed.

    assert(m2.find(), s"second find() did not find ${pattern} in ${sample2}")

    val m2ExpectedStart2 = m2ExpectedEnd1
    val m2ExpectedEnd2   = m2ExpectedStart2 + pattern.length

    assert(m2.start == m2ExpectedStart2,
           s"first start: ${m2.start} != expected: $m2ExpectedStart2")
    assert(m2.end == m2ExpectedEnd2,
           s"first end: ${m2.end} != expected: $m2ExpectedEnd2")
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

    // ?=  <==>	 zero-width positive look-ahead
    val m1 = Pattern.compile("foo(?=buzz)").matcher("foobuzz")
    m1.region(0, 3)
    m1.useTransparentBounds(false)
    assert(!m1.matches()) // opaque

    m1.useTransparentBounds(true)
    assert(m1.matches()) // transparent

    // ?!  <==>	 zero-width negative look-ahead
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

  test("pattern") {
    val p = Pattern.compile("foo")
    assertEquals(
      p.matcher("foobar").pattern(),
      p
    )
  }

  test("issue #852, StringIndexOutOfBoundsException") {
    val JsonNumberRegex =
      """(-)?((?:[1-9][0-9]*|0))(?:\.([0-9]+))?(?:[eE]([-+]?[0-9]+))?""".r
    val JsonNumberRegex(negative, intStr, decStr, expStr) = "0.000000"
    assert(negative == null)
    assert(intStr == "0")
    assert(decStr == "000000")
    assert(expStr == null)
  }

  test("start/end 0,0 on empty match") {
    val m = Pattern.compile(".*").matcher("")
    assert(m.find())
    assertEquals(m.start, 0)
    assertEquals(m.end, 0)
  }

  test("empty strings for optional match") {
    val OptionalA = "(a?)".r
    "a" match { case OptionalA("a") => assert(true) }
    "" match { case OptionalA("")   => assert(true) }
  }

  private def matcher(regex: String, text: String): Matcher =
    Pattern.compile(regex).matcher(text)
}
