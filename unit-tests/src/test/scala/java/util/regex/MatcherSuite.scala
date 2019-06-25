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

  test("find(start) - group") { // As reported in Issue #1506
    val needle     = ".*[aeiou]"
    val haystack   = "abcdefgh"
    val startAt    = 1
    val expectedF0 = "abcde"
    val expectedF1 = "bcde"

    val m = Pattern.compile(needle).matcher(haystack)

    assert(m.find(), s"find() should have found '${needle}' in '${haystack}'")

    val foundF0 = m.group
    assert(foundF0 == expectedF0,
           s"group: ${foundF0} != expected: ${expectedF0}")

    assert(m.find(startAt),
           s"find(1) should have found '${needle}' in '${haystack}'")

    val foundF1 = m.group
    assert(foundF1 == expectedF1,
           s"group: ${foundF1} != expected: ${expectedF1}")

    val expectedF1Start = startAt
    val foundF1Start    = m.start
    assert(foundF1Start == expectedF1Start,
           s"start index: ${foundF1Start} != expected: ${expectedF1Start}")

    val expectedF1End = expectedF1Start + expectedF1.length
    val foundF1End    = m.end
    assert(foundF1End == expectedF1End,
           s"end index: ${foundF1End} != expected: ${expectedF1End}")
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

  testFails("find region - needle before region", 0) {
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

  testFails("find region - needle after region", 0) {
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

    assert(m.hasAnchoringBounds()) // Expect true, same as JVM default.
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

  testFails("lookingAt - region", 0) {
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

  testFails("matches - region", 0) {
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

  // Do not expect support for re2 syntax in java.util.regex with
  // scalanative.regex.
  // No Issue number necessary.
  testFails("named group (re2 syntax)", 0) {
    // scalanative.regex behavior change, so no Issue #
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

  test("quoteReplacement should quote backslash and $") {
    // SN Issue #1070 described a condition where String.replaceAllLiterally()
    // would fail with the cre2 based j.u.regex. The resolution
    // of that issue led to an SN idiosyncratic implementation of
    // quoteReplacement.
    //
    // That implementation has now been replaced. The scalanative.regex
    // based implementation of quoteReplacement now returns the same result
    // as the JVM.
    //
    // The test case for String.replaceAllLiterally() in the StringSuite
    // shows that things changed for scalanative.regex in parallel so that
    // Issue #1070 did not regress. Check & cross check.

    val replacement = "\\fin$\\du.$$monde\\"
    val expected    = "\\\\fin\\$\\\\du.\\$\\$monde\\\\"

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

  // Do not support re2 syntax in java.util.regex with scalanative.regex.
  // No Issue number.
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
    val newNeedle = "t.+(c+)k" // group count decreases
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

    val original = "That's one small step for man,"
    val expected = "That's one small step for [a] man,"

    val m = matcher(oldNeedle, original)

    assert(m.find(), s"should have found '${oldNeedle}' in '${original}'")

    val found = m.group

    val sb = new StringBuffer()

    m.usePattern(Pattern.compile(newNeedle))

    val result = m
      .appendReplacement(sb, s"${found}[a] ")
      .appendTail(sb)
      .toString

    assert(result == expected,
           s"append position changed; result: ${result} != " +
             s"expected: ${expected}'")
  }

  test("usePattern - region unchanged") {

    val needle    = "leap"
    val newNeedle = "Mankind"
    val haystack  = "one giant leap for Mankind."

    val m = matcher(needle, haystack)

    // Establish a region shorter than full haystack to see if the
    // region changes.

    // 4 & 8 are arbitrary valid values, nothing special here.
    val startBefore = 4
    val endBefore   = 8
    m.region(startBefore, endBefore)

    m.usePattern(Pattern.compile(newNeedle))

    val startAfter = m.regionStart
    val endAfter   = m.regionEnd

    assert(startAfter == startBefore,
           s"region start changed; after: ${startAfter} != " +
             s"before: ${startBefore}'")

    assert(endAfter == endBefore,
           s"region end changed; after: ${endAfter} != " +
             s"before: ${endBefore}'")
  }

  test("toMatchResult") {
    // Out of alphabetical order because of critical dependence on usePattern.
    // Test usePattern before using it here.

    case class MatcherCyst(
        group: String,
        start: Int,
        end: Int,
        groupCount: Int,
        groupG1: String,
        startG1: Int,
        endG1: Int,
        groupG2: String,
        startG2: Int,
        endG2: Int
    )

    def encystMatcherState(m: Matcher): MatcherCyst = {
      MatcherCyst(m.group,
                  m.start,
                  m.end,
                  m.groupCount,
                  m.group(1),
                  m.start(1),
                  m.end(1),
                  m.group(2),
                  m.start(2),
                  m.end(2))
    }

    def encystMatchResultState(m: MatchResult): MatcherCyst = {
      MatcherCyst(m.group,
                  m.start,
                  m.end,
                  m.groupCount,
                  m.group(1),
                  m.start(1),
                  m.end(1),
                  m.group(2),
                  m.start(2),
                  m.end(2))
    }

    // Did we find what we expect?
    def validateNewMatchState(m: Matcher): Unit = {

      val newGroupCount         = m.groupCount
      val expectedNewGroupCount = 3
      assert(newGroupCount == expectedNewGroupCount,
             s"groupCount: ${newGroupCount} != " +
               "expected: ${expectedNewGroupCount}")

      val newGroup1         = m.group(1)
      val expectedNewGroup1 = "hold"
      assert(newGroup1 == expectedNewGroup1,
             s"group(1): '${newGroup1} != " +
               "expected: '${expectedNewGroup1}'")

      val newGroup1Start         = m.start(1)
      val expectedNewGroup1Start = 3
      assert(newGroup1Start == expectedNewGroup1Start,
             s"group(1).start: '${newGroup1Start} != " +
               "expected: '${expectedNewGroup1Start}'")

      val newGroup1End         = m.end(1)
      val expectedNewGroup1End = 7
      assert(newGroup1End == expectedNewGroup1End,
             s"group(1).end: '${newGroup1End} != " +
               "expected: '${expectedNewGroup1End}'")

      val newGroup2         = m.group(2)
      val expectedNewGroup2 = "truths"
      assert(newGroup2 == expectedNewGroup2,
             s"group(2): '${newGroup2}' != " +
               s"expected: '${expectedNewGroup2}'")

      val newGroup2Start         = m.start(2)
      val expectedNewGroup2Start = 14
      assert(newGroup2Start == expectedNewGroup2Start,
             s"group(2).start: '${newGroup2Start} != " +
               "expected: '${expectedNewGroup2Start}'")

      val newGroup2End         = m.end(2)
      val expectedNewGroup2End = 20
      assert(newGroup2End == expectedNewGroup2End,
             s"group(2).end: '${newGroup2End} != " +
               "expected: '${expectedNewGroup2End}'")
    }

    // group count increases. Force scalanative.regex Matcher.scala to
    // allocate a new, larger data structure.
    val oldNeedle = "\\w+ (\\w+) \\w+ \\w+ \\w+ \\w+ (\\w+-\\w+)"
    val newNeedle = "\\w+ (\\w+) \\w+ (\\w+) (\\w+) \\w+ \\w+-\\w+"

    val haystack = "We hold these truths to be self-evident"

    val m = matcher(oldNeedle, haystack)

    assert(m.find(), s"should have found '${oldNeedle}' in '${haystack}'")

    val match1Cyst = encystMatcherState(m)

// format: off
    val expectedMatch1Cyst = MatcherCyst(
        "We hold these truths to be self-evident", 0, 39,
         2,
        "hold",3, 7,
        "self-evident",27,39)
// format: on

    assert(match1Cyst == expectedMatch1Cyst,
           s"initial matcher state: ${match1Cyst} != " +
             s"expected: ${expectedMatch1Cyst}")

    val mr     = m.toMatchResult
    val mrCyst = encystMatchResultState(mr)

    assert(mrCyst == match1Cyst,
           s"MatchResult state: ${mrCyst} != " +
             s"expected: ${match1Cyst}")

    m.usePattern(Pattern.compile(newNeedle))

    // find(0) does a reset and starts searching again at the start of input.
    assert(m.find(0), s"should have found '${newNeedle}' in '${haystack}'")

    val match2Cyst = encystMatcherState(m)

    // First a course grain examination.
    assert(match2Cyst != match1Cyst,
           s"matchState did not change when should have.")

    // Now a fine grain examination.
    // group, start, & end should not have changed, but others should.
    validateNewMatchState(m)

    // Matcher state changed to expected but MatchResult's should not have.
    assert(mrCyst == match1Cyst,
           s"MatchResult state: ${mrCyst} != " +
             s"expected: ${match1Cyst}")
  }

}
