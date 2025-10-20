package org.scalanative.testsuite.javalib.util.regex

import java.util.*
import java.util.regex.*

// Tests are inspired by those projects under Apache2 License:
// j2objc: https://github.com/google/j2objc/blob/master/jre_emul/Tests/java/util/regex/MatcherTest.java#L1
// re2: https://github.com/google/re2/blob/master/re2/testing/re2_test.cc

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.*

/* assumeFalse executingInJVM should either be fixed or moved to a Scala Native
 * re2 specific test
 */
class MatcherTest {

  private def matcher(regex: String, text: String): Matcher =
    Pattern.compile(regex).matcher(text)

  private def checkGroupCount(m: Matcher, expected: Int): Boolean = {
    val result = m.groupCount()
    assertTrue(
      s"pattern: ${m.pattern} result: ${result} != expected: ${expected}",
      result == expected
    )
    true
  }

  @Test def regionInvalidValues(): Unit = {
    val needle = "needle"
    val haystack = "haystack"
    val haystackLength = haystack.length

    val m = matcher(needle, haystack)

    assertThrows(
      classOf[IndexOutOfBoundsException],
      m.region(-1, haystackLength)
    )

    assertThrows(
      classOf[IndexOutOfBoundsException],
      m.region(haystackLength + 1, haystackLength)
    )

    assertThrows(classOf[IndexOutOfBoundsException], m.region(0, -1))

    assertThrows(
      classOf[IndexOutOfBoundsException],
      m.region(0, haystackLength + 1)
    )

    assertThrows(
      classOf[IndexOutOfBoundsException],
      m.region(haystackLength + 1, haystackLength - 1)
    )
  }

  @Test def testFind(): Unit = {
    locally { // Expect success
      val needle = "Cutty"
      val haystack = "Weel done, Cutty-sark!"

      val m = Pattern.compile(needle).matcher(haystack)
      assertTrue(s"should have found '${needle}' in '${haystack}'", m.find())
    }

    locally { // Expect failure
      val needle = "vermin"
      val haystack = "haystack & needle"
      val m = Pattern.compile(needle).matcher(haystack)
      assertFalse(
        s"should not have found '${needle}' in '${haystack}'",
        m.find()
      )
    }
  }

  @Test def findAdvanced(): Unit = {
    val prefix = "0123"
    val pattern = "abc"
    val noise = "def"
    val sample = prefix + pattern + noise + pattern + noise

    val m = Pattern.compile(pattern).matcher(sample)

    val expectedStart1 = prefix.length
    val expectedEnd1 = prefix.length + pattern.length
    assertTrue(s"initial find() failed.", m.find())

    assertTrue(
      s"first start: ${m.start()} != expected: $expectedStart1",
      m.start() == expectedStart1
    )
    assertTrue(
      s"first end: ${m.end} != expected: $expectedEnd1",
      m.end == expectedEnd1
    )

    val expectedStart2 = expectedEnd1 + noise.length
    val expectedEnd2 = expectedStart2 + pattern.length

    assertTrue(s"second find() failed.", m.find())
    assertTrue(
      s"second start: ${m.start()} != expected: $expectedStart2",
      m.start() == expectedStart2
    )
    assertTrue(
      s"second end: ${m.start()} != expected: $expectedEnd2",
      m.end == expectedEnd2
    )
  }

  @Test def findGroupStartEndPositions(): Unit = {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assertTrue(s"should have found '${needle}' in '${haystack}'", m.find())

    val expectedGroup = needle
    val foundGroup = m.group
    assertTrue(
      s"group: ${foundGroup} != expected: ${expectedGroup}",
      foundGroup == expectedGroup
    )

    val expectedStart = prefix.length
    val foundStart = m.start()
    assertTrue(
      s"start index: ${foundStart} != expected: ${expectedStart}",
      foundStart == expectedStart
    )

    val expectedEnd = expectedStart + needle.length
    val foundEnd = m.end
    assertTrue(
      s"end index: ${foundEnd} != expected: ${expectedEnd}",
      foundEnd == expectedEnd
    )
  }

  // find(start) uses reset. reset uses find().
  // So reset test needs to be before find(start) and after find()

  @Test def resetBeforeUseInFindStart(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    assertTrue("Assert_1", m.find())
    assertTrue(s"Assert_2 start: ${m.start()}", m.start() == 1)
    assertTrue(s"Assert_3, end: ${m.end}", m.end == 5)

    m.reset()

    assertTrue("Assert_4", m.find())
    assertTrue(s"Assert_5 start: ${m.start()}", m.start() == 1)
    assertTrue(s"Assert_6, end: ${m.end}", m.end == 5)

    assertTrue("Assert_7", m.find())
    assertTrue(s"Assert_8 start: ${m.start()}", m.start() == 6)
    assertTrue(s"Assert_9, end: ${m.end}", m.end == 10)

    assertFalse("Assert_10", m.find())
  }

  @Test def findAfterReset(): Unit = {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assertTrue(
      s"first find should have found '${needle}' in '${haystack}'",
      m.find()
    )

    val expectedStart = prefix.length
    val foundStart = m.start()
    assertTrue(
      s"first start index: ${foundStart} != expected: ${expectedStart}",
      foundStart == expectedStart
    )

    m.reset()

    assertTrue(
      s"second find should have found '${needle}' in '${haystack}'",
      m.find()
    )

    val resetStart = m.start()

    assertTrue(
      s"reset start index: ${foundStart} != expected: ${expectedStart}",
      resetStart == expectedStart
    )
  }

  // Issue 3431
  @Test def findAfterResetInput(): Unit = {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"
    val notHaystack = s"Repent"

    val m = Pattern.compile(needle).matcher(haystack)

    assertTrue(
      s"first find should have found '${needle}' in '${haystack}'",
      m.find()
    )

    val expectedStart = prefix.length
    val foundStart = m.start()
    assertTrue(
      s"first start index: ${foundStart} != expected: ${expectedStart}",
      foundStart == expectedStart
    )

    m.reset(notHaystack)

    assertFalse(
      s"find after reset(input) should not have found " +
        s"'${needle}' in '${haystack}'",
      m.find()
    )
  }

  @Test def findStartInvalidStartValues(): Unit = {
    val pattern = "Isaac"
    val sample = "Asimov"

    val m = Pattern.compile(pattern).matcher(sample)

    assertThrows(classOf[IndexOutOfBoundsException], m.find(-1))

    assertThrows(classOf[IndexOutOfBoundsException], m.find(sample.length + 1))
  }

  @Test def findStart(): Unit = {
    val prefix = "0"
    val pattern = "abc"
    val noise = "def"
    val sample1 = prefix + pattern + noise
    val sample2 = sample1 + pattern + pattern

    val index = 2 // start at leftmost 'b' in sample.

    val m1 = Pattern.compile(pattern).matcher(sample1)

    val m1f1Result = m1.find(index)

    // Evaluate m1.start and m1.end only in the unexpected case of a match
    // having being found. Calling either if no match was found throws
    // an exception.
    if (m1f1Result) {
      assertTrue(
        s"find(${index}) wrongly found start: ${m1.start} end: ${m1.end}",
        false
      )
    }

    val m2 = Pattern.compile(pattern).matcher(sample2)

    assertTrue(
      s"find(${index}) did not find ${pattern} in ${sample2}",
      m2.find(index)
    )

    val m2ExpectedStart1 = prefix.length + pattern.length + noise.length
    val m2ExpectedEnd1 = m2ExpectedStart1 + pattern.length

    assertTrue(
      s"first start: ${m2.start} != expected: $m2ExpectedStart1",
      m2.start == m2ExpectedStart1
    )
    assertTrue(
      s"first end: ${m2.end} != expected: $m2ExpectedEnd1",
      m2.end == m2ExpectedEnd1
    )

    // Simple find() after a find(index) should succeed.

    assertTrue(
      s"second find() did not find ${pattern} in ${sample2}",
      m2.find()
    )

    val m2ExpectedStart2 = m2ExpectedEnd1
    val m2ExpectedEnd2 = m2ExpectedStart2 + pattern.length

    assertTrue(
      s"first start: ${m2.start} != expected: $m2ExpectedStart2",
      m2.start == m2ExpectedStart2
    )
    assertTrue(
      s"first end: ${m2.end} != expected: $m2ExpectedEnd2",
      m2.end == m2ExpectedEnd2
    )
  }

  @Test def findStartGroup(): Unit = { // As reported in Issue #1506
    val needle = ".*[aeiou]"
    val haystack = "abcdefgh"
    val startAt = 1
    val expectedF0 = "abcde"
    val expectedF1 = "bcde"

    val m = Pattern.compile(needle).matcher(haystack)

    assertTrue(
      s"find() should have found '${needle}' in '${haystack}'",
      m.find()
    )

    val foundF0 = m.group
    assertTrue(
      s"group: ${foundF0} != expected: ${expectedF0}",
      foundF0 == expectedF0
    )

    assertTrue(
      s"find(1) should have found '${needle}' in '${haystack}'",
      m.find(startAt)
    )

    val foundF1 = m.group
    assertTrue(
      s"group: ${foundF1} != expected: ${expectedF1}",
      foundF1 == expectedF1
    )

    val expectedF1Start = startAt
    val foundF1Start = m.start
    assertTrue(
      s"start index: ${foundF1Start} != expected: ${expectedF1Start}",
      foundF1Start == expectedF1Start
    )

    val expectedF1End = expectedF1Start + expectedF1.length
    val foundF1End = m.end
    assertTrue(
      s"end index: ${foundF1End} != expected: ${expectedF1End}",
      foundF1End == expectedF1End
    )
  }

  @Test def testReplaceAll(): Unit = {
    assertEquals(
      matcher("abc", "abcabcabc").replaceAll("z"),
      "zzz"
    )
  }

  @Test def findSecondFindUsesRememberedPosition(): Unit = {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assertTrue(
      s"first find should have found '${needle}' in '${haystack}'",
      m.find()
    )

    val firstEnd = m.end

    assertTrue(
      s"second find should have found '${needle}' in '${haystack}'",
      m.find()
    )

    val expectedStart = firstEnd + 2
    val foundStart = m.start
    assertTrue(
      s"start index: ${foundStart} != expected: ${expectedStart}",
      foundStart == expectedStart
    )

    val expectedEnd = expectedStart + needle.length
    val foundEnd = m.end
    assertTrue(
      s"end index: ${foundEnd} != expected: ${expectedEnd}",
      foundEnd == expectedEnd
    )
  }

  @Test def findRegionNeedleBeforeRegion(): Unit = {
    val needle = "a"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    val regionBegin = 2
    val regionEnd = 5
    val regionString = haystack.slice(regionBegin, regionEnd)

    m.region(regionBegin, regionEnd)

    assertFalse(
      s"should not have found '${needle}' in region '${regionString}'",
      m.find()
    )
  }

  @Test def findRegionNeedleInRegion(): Unit = {
    val needle = "s"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    val regionBegin = 2
    val regionEnd = 5
    val regionString = haystack.slice(regionBegin, regionEnd)

    m.region(regionBegin, regionEnd)

    assertTrue(
      s"should have found '${needle}' in region '${regionString}'",
      m.find()
    )
  }

  @Test def findRegionNeedleAfterRegion(): Unit = {
    val needle = "ck"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    val regionBegin = 2
    val regionEnd = 5
    val regionString = haystack.slice(regionBegin, regionEnd)

    m.region(regionBegin, regionEnd)

    assertFalse(
      s"should not have found '${needle}' in region '${regionString}'",
      m.find()
    )
  }

  @Test def findStart2(): Unit = {
    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    assertTrue(
      s"first find should have found '${needle}' in '${haystack}'",
      m.find()
    )

    // startAt some arbitrary point in second "Twinkle" _after_ the
    // initial 'T'. Here that is the 'w'.

    val startAt = m.end + 4

    assertTrue(
      s"find(${startAt}) should have found '${needle}' in '${haystack}'",
      m.find(startAt)
    )

    val find2Start = m.start
    val find2End = m.end

    // Should find third occurance of needle, not second.
    val expectedStart = 33
    val expectedEnd = expectedStart + needle.length

    assertTrue(
      s"start index: ${find2Start} != expected: ${expectedStart}",
      find2Start == expectedStart
    )

    assertTrue(
      s"start index: ${find2End} != expected: ${expectedEnd}",
      find2End == expectedEnd
    )
  }

  @Test def findStartRegion(): Unit = {

    val needle = "Twinkle"
    val prefix = "Sing the song: "
    // "Sing the song: Twinkle, Twinkle, Twinkle, Little Star"
    val haystack = s"${prefix}${needle}, ${needle}, ${needle}, Little Star"

    val m = Pattern.compile(needle).matcher(haystack)

    locally {
      // region before first occurrence of needle.

      val regionStart = 2 // somewhere other than 0 and within prefix.
      val regionEnd = prefix.length // somewhere in prefix.

      m.region(regionStart, regionEnd)

      val startAt = prefix.length

      assertTrue(
        s"find(${startAt}) should not have found '${needle}' " +
          s"in region '${haystack.slice(regionStart, regionEnd)}'",
        m.find(startAt)
      )
    }

    locally {
      // region contains needle.

      val regionStart = prefix.length + 3 // in 1st 'Twinkle' after 'T'.
      val regionEnd = haystack.length - 4 // somewhere after 3rd needle.

      m.region(regionStart, regionEnd)

      val startAt = prefix.length

      assertTrue(
        s"find(${startAt}) should have found '${needle}' " +
          s"in region '${haystack.slice(regionStart, regionEnd)}'",
        m.find(startAt)
      )
    }

    locally {
      // region after last occurrence of needle.

      val regionStart =
        haystack.length - 11 // anywhere after 'T' of 3rd needle.
      val regionEnd = haystack.length - 2 // somewhere before haystack end.

      m.region(regionStart, regionEnd)

      val startAt = prefix.length

      assertTrue(
        s"find(${startAt}) should not have found '${needle}' " +
          s"in region '${haystack.slice(regionStart, regionEnd)}'",
        m.find(startAt)
      )
    }

  }

  @Test def testGroup(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    assertEquals(m.groupCount, 2)

    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.group
    )

    assertTrue(m.find())
    assertEquals(m.group(), "a12z")
    assertEquals(m.group(0), "a12z")
    assertEquals(m.group(1), "1")
    assertEquals(m.group(2), "2")
    assertThrows(
      "No group 42",
      classOf[IndexOutOfBoundsException],
      m.group(42)
    )

    assertTrue(m.find())
    assertEquals(m.group(), "a34z")
    assertEquals(m.group(0), "a34z")
    assertEquals(m.group(1), "3")
    assertEquals(m.group(2), "4")

    assertFalse(m.find())
  }

  @Test def hasAnchoringBounds(): Unit = {
    val needle = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertTrue(m.hasAnchoringBounds()) // Expect true, same as JVM default.
  }

  // we don't support lookahead
  @Ignore("#640")
  @Test def hasTransparentBoundsUseTransparentBoundsNotSupported(): Unit = {

    // ?=  <==>  zero-width positive look-ahead
    val m1 = Pattern.compile("foo(?=buzz)").matcher("foobuzz")
    m1.region(0, 3)
    m1.useTransparentBounds(false)
    assertFalse(m1.matches()) // opaque

    m1.useTransparentBounds(true)
    assertTrue(m1.matches()) // transparent

    // ?!  <==>  zero-width negative look-ahead
    val m2 = Pattern.compile("foo(?!buzz)").matcher("foobuzz")
    m2.region(0, 3)
    m2.useTransparentBounds(false)
    assertFalse(m2.matches()) // opaque

    m2.useTransparentBounds(true)
    assertTrue(m2.matches()) // transparent
  }

  @Test def testHitEnd(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    val needle = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows(classOf[UnsupportedOperationException], m.hitEnd())
  }

  @Test def lookingAtRegion(): Unit = {
    val needle = "Boston"
    val haystack = "Boston Boston"

    val m = matcher(needle, haystack)

    assertTrue(
      s"should be looking at '${needle}' in '${haystack}'",
      m.lookingAt()
    )

    m.region(m.end, haystack.length - 2)

    assertFalse(
      s"should not be looking at '${needle}' in '${haystack}'",
      m.lookingAt()
    )
  }

  // Scala Native issue 2608 provided this reproduction from:
  // softwaremill/sttp-model#188.
  //
  // The license on the original reproducer is Apache 2.0, the same
  // as Scala Native. To be compliant with that license this notice
  // is given that the original code has been modified to become a JUnit
  // test for Scala Native. It is adapted with appreciation & gratitude.

  @Test def issue2608LookingAtRegion(): Unit = {
    val t = "text/plain; charset=utf-8"
    val Token = "([a-z-!#$%&'*+.^_`{|}~]+)"
    val Quoted = "\"([^\"]*)\""
    val Parameter: Pattern =
      Pattern.compile(s";\\s*(?:$Token=(?:$Token|$Quoted))?")
    val parameter = Parameter.matcher(t)
    parameter.region(10, t.length)

    val slice = t.slice(parameter.regionStart(), parameter.regionEnd())

    assertTrue(
      s"pattern: '${Parameter}' not found in region '${slice}'",
      parameter.lookingAt()
    )
  }

  @Test def testMatches(): Unit = {
    locally {
      val needle = "foo"
      val haystack = "foobar"
      val m = matcher(needle, haystack)
      assertFalse(
        s"should not have found '${needle}' in '${haystack}'",
        m.matches()
      )
    }

    locally {
      val needle = "foobar"
      val haystack = needle
      val m = matcher(needle, haystack)
      assertTrue(s"should have found '${needle}' in '${haystack}'", m.matches())
    }
  }

  @Test def matchesRegion(): Unit = {
    locally {
      val needle = "Peace"
      val haystack = "War & Peace by Leo Tolstoy"

      val m = matcher(needle, haystack)
      m.region(6, 11)

      assertTrue(
        s"should have matched '${needle}' in " +
          s"region '${haystack.slice(6, 11)}'",
        m.matches()
      )
    }

  }

  @Test def loadsGroupsInRegion(): Unit = {
    // Ensure that the tricky _pattern.re2.match_ call with a region in private
    // method loadGroup() is exercised with a region containing at least two
    // groups.
    // This test exercises group loading only.  Other tests in this suite
    // check that the match is in the region, neither before nor after.

    val needle = "([A-Z])([a-z]{4})"
    val haystack = "War & Peace by Lev Tolstoy"

    val m = matcher(needle, haystack)
    m.region(6, 11)

    assertTrue(
      s"should have X2 matched '${needle}' in " +
        s"region '${haystack.slice(6, 11)}'",
      m.matches()
    )

    val expectedG0 = "Peace"
    assertEquals(
      s"group(0): {group(0)} should be '${expectedG0}'",
      m.group(0),
      expectedG0
    )
    val expectedG1 = "P"
    assertEquals(
      s"group(1): {group(1)} should be '${expectedG1}'",
      m.group(1),
      expectedG1
    )

    val expectedG2 = "eace"
    assertEquals(
      s"group(2): {group(2)} should be '${expectedG2}'",
      m.group(2),
      expectedG2
    )
  }

  @Test def namedGroupJavaSyntax(): Unit = {
    assumeFalse(
      "Fails in JVM, expected:<java.lang.IllegalStateException> but was:<java.lang.IllegalArgumentException>",
      executingInJVM
    )
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )

    assertTrue(m.find())
    assertEquals(m.group("S"), "Montreal, Canada")
    assertEquals(m.group("D"), "Lausanne, Switzerland")
    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.group("foo")
    )
  }

  // re2 syntax is not defined in Java, but it works with scalanative.regex
  @Test def namedGroupRe2Syntax(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    val m = matcher(
      "from (?P<S>.*) to (?P<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )

    assertTrue("A1", m.find())
    assertTrue("A2", m.group("S") == "Montreal, Canada")
    assertTrue("A3", m.group("D") == "Lausanne, Switzerland")
    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.group("foo")
    )
  }

  @Test def optionalMatchEmptyStrings(): Unit = {
    val OptionalA = "(a?)".r
    "a" match { case OptionalA("a") => assertTrue(true) }
    "" match { case OptionalA("") => assertTrue(true) }
  }

  @Test def testPattern(): Unit = {
    val p = Pattern.compile("foo")
    assertEquals(
      p.matcher("foobar").pattern(),
      p
    )
  }

  @Test def quoteReplacement(): Unit = {
    assertEquals(Matcher.quoteReplacement(""), "")
  }

  @Test def quoteReplacementShouldQuoteBackslashAndDollarSign(): Unit = {
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
    val expected = "\\\\fin\\$\\\\du.\\$\\$monde\\\\"

    assertEquals(
      Matcher.quoteReplacement(replacement),
      expected
    )
  }

  @Test def regionEnd(): Unit = {
    val needle = "needle"
    val haystack = "haystack"
    val expected = 6

    val m = matcher(needle, haystack)

    m.region(2, expected)

    val result = m.regionEnd

    assertTrue(
      s"result: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def regionStart(): Unit = {
    val needle = "needle"
    val haystack = "haystack"
    val expected = 3

    val m = matcher(needle, haystack)

    m.region(expected, haystack.length - 2)

    val result = m.regionStart

    assertTrue(
      s"result: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def replaceAll2(): Unit = {
    assertEquals(matcher("abc", "abcabcabc").replaceAll("z"), "zzz")
  }

  @Test def replaceFirst(): Unit = {
    assertEquals(
      matcher("abc", "abcabcabc").replaceFirst("z"),
      "zabcabc"
    )
  }

  @Test def appendReplacementAppendTail(): Unit = {
    val buf = new StringBuffer()

    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    while (m.find()) {
      m.appendReplacement(buf, "{" + m.group() + "}")
    }
    m.appendTail(buf)
    assertEquals(buf.toString, "_{a12z}_{a34z}_")
  }

  @Test def appendReplacementAppendTailWithGroupReplacementByIndex(): Unit = {
    val buf = new StringBuffer()
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    while (m.find()) {
      m.appendReplacement(buf, "{$0}")
    }
    m.appendTail(buf)
    assertEquals(buf.toString, "_{a12z}_{a34z}_")
  }

  @Test def appendReplacementAppendTailWithGroupReplacementByName(): Unit = {
    val buf = new StringBuffer()
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)", // java syntax
      "from Montreal, Canada to Lausanne, Switzerland"
    )

    while (m.find()) {
      m.appendReplacement(buf, "such ${S}, wow ${D}")
    }
    m.appendTail(buf)
    assertEquals(
      buf.toString,
      "such Montreal, Canada, wow Lausanne, Switzerland"
    )
  }

  @Test def requireEnd(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    val needle = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows(classOf[UnsupportedOperationException], m.requireEnd())
  }

  @Test def startEndIndices(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "012345_a12z_012345")

    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.start()
    )

    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.end()
    )

    assertTrue(m.find())

    assertEquals(m.start(), 7)
    assertEquals(m.end(), 11)

    assertEquals(m.start(0), 7)
    assertEquals(m.end(0), 11)

    assertEquals(m.start(1), 8)
    assertEquals(m.end(1), 9)

    assertEquals(m.start(2), 9)
    assertEquals(m.end(2), 10)

    assertThrows(
      "No group 42",
      classOf[IndexOutOfBoundsException],
      m.start(42)
    )

    assertThrows(
      "No group 42",
      classOf[IndexOutOfBoundsException],
      m.end(42)
    )
  }

  @Test def startEnd(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    assertTrue(m.find())
    assertEquals(m.start(), 1)
    assertEquals(m.end(), 5)

    assertTrue(m.find())
    assertEquals(m.start(), 6)
    assertEquals(m.end(), 10)

    assertFalse(m.find())
  }

  @Test def startEndZeroZeroOnEmptyMatch(): Unit = {
    val m = Pattern.compile(".*").matcher("")
    assertTrue("Assert_1", m.find())
    assertTrue(s"Assert_2 m.start: ${m.start}", m.start == 0)
    assertTrue(s"Assert_3, m.end: ${m.end}", m.end == 0)
  }

  @Test def startNameEndNameJavaSyntax(): Unit = {
    // change pattern to java: "from (?<S>.*) to (?<D>.*)"
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )

    assertTrue(m.find())
    assertEquals(m.start("S"), 5)
    assertEquals(m.end("S"), 21)

    assertEquals(m.start("D"), 25)
    assertEquals(m.end("D"), 46)
  }

  // re2 syntax is not defined in Java, but it works with scalanative.regex
  @Test def startNameEndNameRe2Syntax(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    val m = matcher(
      "from (?P<S>.*) to (?P<D>.*)",
      "from Montreal, Canada to Lausanne, Switzerland"
    )

    assertTrue(m.find())
    assertEquals(m.start("S"), 5)
    assertEquals(m.end("S"), 21)

    assertEquals(m.start("D"), 25)
    assertEquals(m.end("D"), 46)

    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.start("foo")
    )

    assertThrows(
      "No match found",
      classOf[IllegalStateException],
      m.end("foo")
    )
  }

  @Test def issue852StringIndexOutOfBoundsException(): Unit = {
    val JsonNumberRegex =
      """(-)?((?:[1-9][0-9]*|0))(?:\.([0-9]+))?(?:[eE]([-+]?[0-9]+))?""".r
    val JsonNumberRegex(negative, intStr, decStr, expStr) =
      "0.000000": @unchecked
    assertTrue("Assert_1", negative == null)
    assertTrue("Assert_2", intStr == "0")
    assertTrue("Assert_3", decStr == "000000")
    assertTrue("Assert_3", expStr == null)
  }

  @Test def toStringNoPriorMatch(): Unit = {
    val m1 = matcher("needle", "sharp needle")
    val expected =
      "java.util.regex.Matcher[pattern=needle region=0,12 lastmatch=]"
    val result = m1.toString

    assertTrue(
      s"toString result: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def toStringPriorMatch(): Unit = {
    val m1 = matcher("needle", "sharp needle")
    val expected =
      "java.util.regex.Matcher[pattern=needle region=0,12 lastmatch=needle]"

    m1.find
    val result = m1.toString

    assertTrue(
      s"toString result: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def useAnchoringBounds(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    val needle = "needle"
    val haystack = "haystack"

    val m = matcher(needle, haystack)

    assertThrows(
      classOf[UnsupportedOperationException],
      m.useAnchoringBounds(false)
    )
  }

  @Test def usePattern(): Unit = {

    val oldNeedle = "(h)(.*)(y)"
    val newNeedle = "t.+(c+)k" // group count decreases
    val haystack = "haystack"

    val m = matcher(oldNeedle, haystack)

    assertThrows(classOf[IllegalArgumentException], m.usePattern(null))

    checkGroupCount(m, 3)

    assertTrue(s"should have found '${oldNeedle}' in '${haystack}'", m.find())

    m.usePattern(Pattern.compile(newNeedle))

    checkGroupCount(m, 1)

    // all prior groups have been forgotten/cleared.
    for (i <- 0 until m.groupCount) {
      val grp = m.group(i)
      assertTrue(s"group(${i}): ${grp} != expected: null", grp == null)
    }

    assertTrue(s"should have found '${newNeedle}' in '${haystack}'", m.find())
  }

  @Test def usePatternAppendPositionUnchanged(): Unit = {

    val oldNeedle = "for "
    val newNeedle = "man"

    val original = "That's one small step for man,"
    val expected = "That's one small step for [a] man,"

    val m = matcher(oldNeedle, original)

    assertTrue(s"should have found '${oldNeedle}' in '${original}'", m.find())

    val found = m.group

    val sb = new StringBuffer()

    m.usePattern(Pattern.compile(newNeedle))

    val result = m
      .appendReplacement(sb, s"${found}[a] ")
      .appendTail(sb)
      .toString

    assertTrue(
      s"append position changed; result: ${result} != " +
        s"expected: ${expected}'",
      result == expected
    )
  }

  @Test def usePatternRegionUnchanged(): Unit = {

    val needle = "leap"
    val newNeedle = "Mankind"
    val haystack = "one giant leap for Mankind."

    val m = matcher(needle, haystack)

    // Establish a region shorter than full haystack to see if the
    // region changes.

    // 4 & 8 are arbitrary valid values, nothing special here.
    val startBefore = 4
    val endBefore = 8
    m.region(startBefore, endBefore)

    m.usePattern(Pattern.compile(newNeedle))

    val startAfter = m.regionStart
    val endAfter = m.regionEnd

    assertTrue(
      s"region start changed; after: ${startAfter} != " +
        s"before: ${startBefore}'",
      startAfter == startBefore
    )

    assertTrue(
      s"region end changed; after: ${endAfter} != " +
        s"before: ${endBefore}'",
      endAfter == endBefore
    )
  }

  @Test def toMatchResult(): Unit = {
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
      MatcherCyst(
        m.group,
        m.start,
        m.end,
        m.groupCount,
        m.group(1),
        m.start(1),
        m.end(1),
        m.group(2),
        m.start(2),
        m.end(2)
      )
    }

    def encystMatchResultState(m: MatchResult): MatcherCyst = {
      MatcherCyst(
        m.group,
        m.start,
        m.end,
        m.groupCount,
        m.group(1),
        m.start(1),
        m.end(1),
        m.group(2),
        m.start(2),
        m.end(2)
      )
    }

    // Did we find what we expect?
    def validateNewMatchState(m: Matcher): Unit = {

      val newGroupCount = m.groupCount
      val expectedNewGroupCount = 3
      assertTrue(
        s"groupCount: ${newGroupCount} != " +
          "expected: ${expectedNewGroupCount}",
        newGroupCount == expectedNewGroupCount
      )

      val newGroup1 = m.group(1)
      val expectedNewGroup1 = "hold"
      assertTrue(
        s"group(1): '${newGroup1} != " +
          "expected: '${expectedNewGroup1}'",
        newGroup1 == expectedNewGroup1
      )

      val newGroup1Start = m.start(1)
      val expectedNewGroup1Start = 3
      assertTrue(
        s"group(1).start: '${newGroup1Start} != " +
          "expected: '${expectedNewGroup1Start}'",
        newGroup1Start == expectedNewGroup1Start
      )

      val newGroup1End = m.end(1)
      val expectedNewGroup1End = 7
      assertTrue(
        s"group(1).end: '${newGroup1End} != " +
          "expected: '${expectedNewGroup1End}'",
        newGroup1End == expectedNewGroup1End
      )

      val newGroup2 = m.group(2)
      val expectedNewGroup2 = "truths"
      assertTrue(
        s"group(2): '${newGroup2}' != " +
          s"expected: '${expectedNewGroup2}'",
        newGroup2 == expectedNewGroup2
      )

      val newGroup2Start = m.start(2)
      val expectedNewGroup2Start = 14
      assertTrue(
        s"group(2).start: '${newGroup2Start} != " +
          "expected: '${expectedNewGroup2Start}'",
        newGroup2Start == expectedNewGroup2Start
      )

      val newGroup2End = m.end(2)
      val expectedNewGroup2End = 20
      assertTrue(
        s"group(2).end: '${newGroup2End} != " +
          "expected: '${expectedNewGroup2End}'",
        newGroup2End == expectedNewGroup2End
      )
    }

    // group count increases. Force scalanative.regex Matcher.scala to
    // allocate a new, larger data structure.
    val oldNeedle = "\\w+ (\\w+) \\w+ \\w+ \\w+ \\w+ (\\w+-\\w+)"
    val newNeedle = "\\w+ (\\w+) \\w+ (\\w+) (\\w+) \\w+ \\w+-\\w+"

    val haystack = "We hold these truths to be self-evident"

    val m = matcher(oldNeedle, haystack)

    assertTrue(s"should have found '${oldNeedle}' in '${haystack}'", m.find())

    val match1Cyst = encystMatcherState(m)

// format: off
    val expectedMatch1Cyst = MatcherCyst(
        "We hold these truths to be self-evident", 0, 39,
         2,
        "hold",3, 7,
        "self-evident",27,39)
// format: on

    assertTrue(
      s"initial matcher state: ${match1Cyst} != " +
        s"expected: ${expectedMatch1Cyst}",
      match1Cyst == expectedMatch1Cyst
    )

    val mr = m.toMatchResult
    val mrCyst = encystMatchResultState(mr)

    assertTrue(
      s"MatchResult state: ${mrCyst} != " +
        s"expected: ${match1Cyst}",
      mrCyst == match1Cyst
    )

    m.usePattern(Pattern.compile(newNeedle))

    // find(0) does a reset and starts searching again at the start of input.
    assertTrue(s"should have found '${newNeedle}' in '${haystack}'", m.find(0))

    val match2Cyst = encystMatcherState(m)

    // First a course grain examination.
    assertTrue(
      s"matchState did not change when should have.",
      match2Cyst != match1Cyst
    )

    // Now a fine grain examination.
    // group, start, & end should not have changed, but others should.
    validateNewMatchState(m)

    // Matcher state changed to expected but MatchResult's should not have.
    assertTrue(
      s"MatchResult state: ${mrCyst} != " +
        s"expected: ${match1Cyst}",
      mrCyst == match1Cyst
    )
  }

}
