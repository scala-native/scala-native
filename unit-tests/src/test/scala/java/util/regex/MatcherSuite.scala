package java.util
package regex

// Tests are inspired by those projects under Apache2 License:
// j2objc: https://github.com/google/j2objc/blob/master/jre_emul/Tests/java/util/regex/MatcherTest.java#L1
// re2: https://github.com/google/re2/blob/master/re2/testing/re2_test.cc

object MatcherSuite extends tests.Suite {

  private def matcher(regex: String, text: String): Matcher =
    Pattern.compile(regex).matcher(text)

  private def checkGroupCount(m: Matcher, expected: Int): Boolean = {
    val result = m.groupCount()
    assert(result == expected,
           s"pattern: ${m.pattern} result: ${result} != expected: ${expected}")
    true
  }

  private def extractRegion(patternToString: String): String = {

    val needle = "region=(\\d+,\\d+)"
    val m      = matcher(needle, patternToString)

    assert(m.find(), s"should have found '${needle}' in '${patternToString}'")

    val expectedGroupCount = 1

    assert(m.groupCount() == expectedGroupCount,
           s"groupCount: ${m.groupCount} != expected: ${expectedGroupCount}")

    m.group(1)
  }

  test("region - invalid values") {
    val needle         = "needle"
    val haystack       = "haystack"
    val haystackLength = haystack.length

    val m = matcher(needle, haystack)

    assertThrows[IndexOutOfBoundsException] {
      m.region(-1, haystackLength)
    }

    assertThrows[IndexOutOfBoundsException] {
      m.region(haystackLength + 1, haystackLength)
    }

    assertThrows[IndexOutOfBoundsException] {
      m.region(0, -1)
    }

    assertThrows[IndexOutOfBoundsException] {
      m.region(0, haystackLength + 1)
    }

    assertThrows[IndexOutOfBoundsException] {
      m.region(haystackLength + 1, haystackLength - 1)
    }
  }

  test("find") {
    locally { // Expect success

      val needle   = "Cutty"
      val haystack = "Weel done, Cutty-sark!"

      val m = Pattern.compile(needle).matcher(haystack)
      assert(m.find(), s"should have found '${needle}' in '${haystack}'")
    }

    locally { // Expect failure
      val needle   = "vermin"
      val haystack = "haystack & needle"
      val m        = Pattern.compile(needle).matcher(haystack)
      assert(!m.find(), s"should not have found '${needle}' in '${haystack}'")
    }
  }

  test("find() - advanced") {
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

  test("find - group & start/end positions") {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assert(m.find(), s"should have found '${needle}' in '${haystack}'")

    val expectedGroup = needle
    val foundGroup    = m.group
    assert(foundGroup == expectedGroup,
           s"group: ${foundGroup} != expected: ${expectedGroup}")

    val expectedStart = prefix.length
    val foundStart    = m.start
    assert(foundStart == expectedStart,
           s"start index: ${foundStart} != expected: ${expectedStart}")

    val expectedEnd = expectedStart + needle.length
    val foundEnd    = m.end
    assert(foundEnd == expectedEnd,
           s"end index: ${foundEnd} != expected: ${expectedEnd}")
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

  test("find - second find uses remembered position") {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assert(m.find(),
           s"first find should have found '${needle}' in '${haystack}'")

    val firstEnd = m.end

    assert(m.find(),
           s"second find should have found '${needle}' in '${haystack}'")

    val expectedStart = firstEnd + 2
    val foundStart    = m.start
    assert(foundStart == expectedStart,
           s"start index: ${foundStart} != expected: ${expectedStart}")

    val expectedEnd = expectedStart + needle.length
    val foundEnd    = m.end
    assert(foundEnd == expectedEnd,
           s"end index: ${foundEnd} != expected: ${expectedEnd}")
  }

  test("find region - needle before region") {
    val needle   = "a"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    val regionBegin  = 2
    val regionEnd    = 5
    val regionString = haystack.slice(regionBegin, regionEnd)

    m.region(regionBegin, regionEnd)

    assert(!m.find(),
           s"should not have found '${needle}' in region '${regionString}'")
  }

  test("find region - needle in region") {
    val needle   = "s"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    val regionBegin  = 2
    val regionEnd    = 5
    val regionString = haystack.slice(regionBegin, regionEnd)

    m.region(regionBegin, regionEnd)

    assert(m.find(),
           s"should have found '${needle}' in region '${regionString}'")
  }

  test("find region - needle after region") {
    val needle   = "ck"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    val regionBegin  = 2
    val regionEnd    = 5
    val regionString = haystack.slice(regionBegin, regionEnd)

    m.region(regionBegin, regionEnd)

    assert(!m.find(),
           s"should not have found '${needle}' in region '${regionString}'")
  }

  // find(start) uses reset. reset uses find().
  // So reset test needs to be before find(start) and after find()

  test("reset - before use in find(start)") {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assert(find(), "Assert_1")
    assert(start == 1, s"Assert_2 start: ${start}")
    assert(end == 5, s"Assert_3, end: ${end}")

    reset()

    assert(find(), "Assert_4")
    assert(start == 1, s"Assert_5 start: ${start}")
    assert(end == 5, s"Assert_6, end: ${end}")

    assert(find(), "Assert_7")
    assert(start == 6, s"Assert_8 start: ${start}")
    assert(end == 10, s"Assert_9, end: ${end}")

    assert(!find(), "Assert_10")
  }

  test("find - after reset") {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assert(m.find(),
           s"first find should have found '${needle}' in '${haystack}'")

    val expectedStart = prefix.length
    val foundStart    = m.start
    assert(foundStart == expectedStart,
           s"first start index: ${foundStart} != expected: ${expectedStart}")

    m.reset()

    assert(m.find(),
           s"second find should have found '${needle}' in '${haystack}'")

    val resetStart = m.start

    assert(resetStart == expectedStart,
           s"reset start index: ${foundStart} != expected: ${expectedStart}")
  }

  test("find(start)") {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assert(m.find(),
           s"first find should have found '${needle}' in '${haystack}'")

    // startAt some arbitrary point in second "Twinkle" _after_ the
    // initial 'T'. Here that is the 'w'.

    val startAt = m.end + 4

    assert(m.find(startAt),
           s"find(${startAt}) should have found '${needle}' in '${haystack}'")

    val find2Start = m.start
    val find2End   = m.end

    // Should find third occurance of needle, not second.
    val expectedStart = 33
    val expectedEnd   = expectedStart + needle.length

    assert(find2Start == expectedStart,
           s"start index: ${find2Start} != expected: ${expectedStart}")

    assert(find2End == expectedEnd,
           s"start index: ${find2End} != expected: ${expectedEnd}")
  }

  test("find(start) - region") {

    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    locally {
      // region before first occurrence of needle.

      val regionStart = 2 // somewhere other than 0 and within prefix.
      val regionEnd   = prefix.length // somewhere in prefix.

      m.region(regionStart, regionEnd)

      val startAt = prefix.length

      assert(m.find(startAt),
             s"find(${startAt}) should not have found '${needle}' " +
               s"in region '${haystack.slice(regionStart, regionEnd)}'")
    }

    locally {
      // region contains needle.

      val regionStart = prefix.length + 3   // in 1st 'Twinkle' after 'T'.
      val regionEnd   = haystack.length - 4 // somewhere after 3rd needle.

      m.region(regionStart, regionEnd)

      val startAt = prefix.length

      assert(m.find(startAt),
             s"find(${startAt}) should have found '${needle}' " +
               s"in region '${haystack.slice(regionStart, regionEnd)}'")
    }

    locally {
      // region after last occurrence of needle.

      val regionStart = haystack.length - 11 // anywhere after 'T' of 3rd needle.
      val regionEnd   = haystack.length - 2  // somewhere before haystack end.

      m.region(regionStart, regionEnd)

      val startAt = prefix.length

      assert(m.find(startAt),
             s"find(${startAt}) should not have found '${needle}' " +
               s"in region '${haystack.slice(regionStart, regionEnd)}'")
    }

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

  test("hasAnchoringBounds") {
    val needle   = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assert(!m.hasAnchoringBounds()) // SN Bug: differs from JVM default
  }

  // we don't support lookahead
  testFails("hasTransparentBounds/useTransparentBounds (not supported)", 640) {

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

  test("hitEnd") {
    val needle   = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows[UnsupportedOperationException] {
      m.hitEnd()
    }
  }

  test("lookingAt - region") {
    val needle   = "Boston"
    val haystack = "Boston Boston"

    val m = matcher(needle, haystack)

    assert(m.lookingAt(), s"should be looking at '${needle}' in '${haystack}'")

    m.region(m.end, haystack.length - 2)

    assert(!m.lookingAt(),
           s"should not be looking at '${needle}' in '${haystack}'")
  }

  test("matches") {
    locally {
      val needle   = "foo"
      val haystack = "foobar"
      val m        = matcher(needle, haystack)
      assert(!m.matches(),
             s"should not have found '${needle}' in '${haystack}'")
    }

    locally {
      val needle   = "foobar"
      val haystack = needle
      val m        = matcher(needle, haystack)
      assert(m.matches(), s"should have found '${needle}' in '${haystack}'")
    }
  }

  test("matches - region") {
    locally {
      val needle   = "Peace"
      val haystack = "War & Peace by Leo Tolstoy"

      val m = matcher(needle, haystack)
      m.region(6, 11)

      assert(m.matches(),
             s"should have matched '${needle}' in " +
               s"region '${haystack.slice(6, 11)}'")
    }

  }

  test("named group (java syntax)") {
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find())
    assertEquals(group("S"), "Montreal, Canada")
    assertEquals(group("D"), "Lausanne, Switzerland")
  }

  // Do not expect support for re2 syntax in java.util.regex with re2s.
  // No Issue number necessary.
  testFails("named group (re2 syntax)", 0) {
    // re2s behavior change, so no Issue #
    // change pattern to java: "from (?<S>.*) to (?<D>.*)"
    val m = matcher(
      "from (?P<S>.*) to (?P<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )
    import m._

    assert(find(), "A1")
    assert(group("S") == "Montreal, Canada", "A2")
    assert(group("D") == "Lausanne, Switzerland", "A3")
    assertThrowsAnd[IllegalArgumentException](group("foo"))(
      _.getMessage == "No group with name <foo>"
    )
  }

  test("optional match - empty strings") {
    val OptionalA = "(a?)".r
    "a" match { case OptionalA("a") => assert(true) }
    "" match { case OptionalA("")   => assert(true) }
  }

  test("pattern") {
    val p = Pattern.compile("foo")
    assertEquals(
      p.matcher("foobar").pattern(),
      p
    )
  }

  test("quoteReplacement") {
    assertEquals(Matcher.quoteReplacement(""), "")
  }

  test("quoteReplacement should quote backslash and only it, issue #1070") {
    val replacement = "\\fin$\\du.$$monde\\"
    val expected    = "\\\\fin$\\\\du.$$monde\\\\"
    assertEquals(
      Matcher.quoteReplacement(replacement),
      expected
    )
  }

  test("regionEnd") {
    val needle   = "needle"
    val haystack = "haystack"
    val expected = 6

    val m = matcher(needle, haystack)

    m.region(2, expected)

    val result = m.regionEnd

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  test("regionStart") {
    val needle   = "needle"
    val haystack = "haystack"
    val expected = 3

    val m = matcher(needle, haystack)

    m.region(expected, haystack.length - 2)

    val result = m.regionStart

    assert(result == expected, s"result: ${result} != expected: ${expected}")
  }

  test("replaceAll") {
    assertEquals(matcher("abc", "abcabcabc").replaceAll("z"), "zzz")
  }

  test("replaceFirst") {
    assertEquals(
      matcher("abc", "abcabcabc").replaceFirst("z"),
      "zabcabc"
    )
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
      "from (?<S>.*) to (?<D>.*)", // java syntax
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

  test("requireEnd") {
    val needle   = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows[UnsupportedOperationException] {
      m.requireEnd()
    }
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

  test("start/end 0,0 on empty match") {
    val m = Pattern.compile(".*").matcher("")
    assert(m.find(), "Assert_1")
    assert(m.start == 0, s"Assert_2 m.start: ${m.start}")
    assert(m.end == 0, s"Assert_3, m.end: ${m.end}")
  }

  test("start(name)/end(name) java syntax") {
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

  // Do not support re2 syntax in java.util.regex with re2s. No Issue number.
  testFails("start(name)/end(name) re2 syntax", 0) {
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

  test("issue #852, StringIndexOutOfBoundsException") {
    val JsonNumberRegex =
      """(-)?((?:[1-9][0-9]*|0))(?:\.([0-9]+))?(?:[eE]([-+]?[0-9]+))?""".r
    val JsonNumberRegex(negative, intStr, decStr, expStr) = "0.000000"
    assert(negative == null, "Assert_1")
    assert(intStr == "0", "Assert_2")
    assert(decStr == "000000", "Assert_3")
    assert(expStr == null, "Assert_3")
  }

  test("toString - no prior match") {
    val m1 = matcher("needle", "sharp needle")
    val expected =
      "java.util.regex.Matcher[pattern=needle region=0,12 lastmatch=]"
    val result = m1.toString

    assert(result == expected,
           s"toString result: ${result} != expected: ${expected}")
  }

  test("toString - prior match") {
    val m1 = matcher("needle", "sharp needle")
    val expected =
      "java.util.regex.Matcher[pattern=needle region=0,12 lastmatch=needle]"

    m1.find
    val result = m1.toString

    assert(result == expected,
           s"toString result: ${result} != expected: ${expected}")
  }

  test("toMatchResult") {
    val needle   = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows[UnsupportedOperationException] {
      m.toMatchResult()
    }
  }

  test("useAnchoringBounds") {
    val needle   = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows[UnsupportedOperationException] {
      m.useAnchoringBounds(false)
    }
  }

  test("usePattern") {

    val oldNeedle = "(h)(.*)(y)"
    val newNeedle = "t.+(c+)k"
    val haystack  = "haystack"

    val m = matcher(oldNeedle, haystack)

    assertThrows[IllegalArgumentException] {
      m.usePattern(null)
    }

    checkGroupCount(m, 3)

    assert(m.find(), s"should have found '${oldNeedle}' in '${haystack}'")

    m.usePattern(Pattern.compile(newNeedle))

    checkGroupCount(m, 1)

    // all prior groups have been forgotten/cleared.
    for (i <- 0 until m.groupCount) {
      val grp = m.group(i)
      assert(grp == null, s"group(${i}): ${grp} != expected: null")
    }

    assert(m.find(), s"should have found '${newNeedle}' in '${haystack}'")
  }

  test("usePattern - append position unchanged") {

    val oldNeedle = "for "
    val newNeedle = "man"
    val original  = "That's one small step for man,"
    val expected  = "That's one small step for [a] man,"

    val m = matcher(oldNeedle, original)

    assert(m.find(), s"should have found '${oldNeedle}' in '${original}'")

    m.usePattern(Pattern.compile(newNeedle))

    val sb = new StringBuffer()
    m.appendReplacement(sb, s"${m.group}[a] ")
    val result = m.appendTail(sb).toString

    assert(result == expected,
           s"append position changed; result: ${result} != " +
             s"expected: ${expected}'")
  }

  test("usePattern - region unchanged") {

    val needle    = "leap"
    val newNeedle = "Mankind"
    val haystack  = "one giant leap for Mankind."

    val m = matcher(needle, haystack)

    // Establish a region shorter than full haystack to see if former changes.
    m.region(4, 8) // 4 & 8 are arbitrary valid values, nothing special here.

    val regionBefore = extractRegion(m.toString)

    m.usePattern(Pattern.compile(newNeedle))

    val regionAfter = extractRegion(m.toString)

    assert(regionAfter == regionBefore,
           s"region changed; after: ${regionAfter} != " +
             s"before: ${regionBefore}'")
  }

}
