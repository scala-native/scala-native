package scala.scalanative
package regex

import java.util

import org.junit.Assert.*

object ApiTestUtils {

  def assertArrayEquals[A](arr1: Array[A], arr2: Array[A]) =
    assertTrue(arr1.sameElements(arr2))

  /** Asserts that IllegalArgumentException is thrown from compile with flags.
   */
  def assertCompileFails(regex: String, flag: Int): Unit = {
    try {
      Pattern.compile(regex, flag)
      fail(
        "Compiling Pattern with regex: " + regex + " and flag: " + flag + " passed, when it should have failed."
      )
    } catch {
      case e: IllegalArgumentException =>
        if (!("Flags UNIX_LINES and COMMENTS unsupported" == e.getMessage))
          throw e
    }
  }

  /** Tests that both RE2's and JDK's pattern class act as we expect them. The
   *  regular expression {@code regexp} matches the string {@code match} and
   *  doesn't match {@code nonMatch}
   *
   *  @param regexp
   *  @param match
   *  @param nonMatch
   */
  def testMatches(regexp: String, `match`: String, nonMatch: String): Unit = {
    val errorString = "Pattern with regexp: " + regexp
    assertTrue(
      "JDK " + errorString + " doesn't match: " + `match`,
      java.util.regex.Pattern.matches(regexp, `match`)
    )
    assertFalse(
      "JDK " + errorString + " matches: " + nonMatch,
      java.util.regex.Pattern.matches(regexp, nonMatch)
    )
    assertTrue(
      errorString + " doesn't match: " + `match`,
      Pattern.matches(regexp, `match`)
    )
    assertFalse(
      errorString + " matches: " + nonMatch,
      Pattern.matches(regexp, nonMatch)
    )
  }

  // Test matches via a matcher.
  def testMatcherMatches(
      regexp: String,
      `match`: String,
      nonMatch: String
  ): Unit = {
    testMatcherMatches(regexp, `match`)
    testMatcherNotMatches(regexp, nonMatch)
  }

  def testMatcherMatches(regexp: String, `match`: String): Unit = {
    val p = java.util.regex.Pattern.compile(regexp)
    assertTrue(
      "JDK Pattern with regexp: " + regexp + " doesn't match: " + `match`,
      p.matcher(`match`).matches
    )
    val pr = Pattern.compile(regexp)
    assertTrue(
      "Pattern with regexp: " + regexp + " doesn't match: " + `match`,
      pr.matcher(`match`).matches()
    )
  }

  def testMatcherNotMatches(regexp: String, nonMatch: String): Unit = {
    val p = java.util.regex.Pattern.compile(regexp)
    assertFalse(
      "JDK Pattern with regexp: " + regexp + " matches: " + nonMatch,
      p.matcher(nonMatch).matches
    )
    val pr = Pattern.compile(regexp)
    assertFalse(
      "Pattern with regexp: " + regexp + " matches: " + nonMatch,
      pr.matcher(nonMatch).matches()
    )
  }

  /** This takes a regex and its compile time flags, a string that is expected
   *  to match the regex and a string that is not expected to match the regex.
   *
   *  We don't check for JDK compatibility here, since the flags are not in a
   *  1-1 correspondence.
   */
  def testMatchesRE2(
      regexp: String,
      flags: Int,
      `match`: String,
      nonMatch: String
  ): Unit = {
    val p = Pattern.compile(regexp, flags)
    val errorString = "Pattern with regexp: " + regexp + " and flags: " + flags
    assertTrue(errorString + " doesn't match: " + `match`, p.matches(`match`))
    assertFalse(errorString + " matches: " + nonMatch, p.matches(nonMatch))
  }

  def testMatchesRE2(
      regexp: String,
      flags: Int,
      matches: Array[String],
      nonMatches: Array[String]
  ): Unit = {
    val p = Pattern.compile(regexp, flags)
    for (s <- matches) {
      assertTrue(p.matches(s))
    }
    for (s <- nonMatches) {
      assertFalse(p.matches(s))
    }
  }

  /** Tests that both RE2 and JDK split the string on the regex in the same way,
   *  and that that way matches our expectations.
   */
  def testSplit(regexp: String, text: String, expected: Array[String]): Unit = {
    testSplit(regexp, text, 0, expected)
  }

  def testSplit(
      regexp: String,
      text: String,
      limit: Int,
      expected: Array[String]
  ): Unit = {
    assertArrayEquals(
      expected,
      java.util.regex.Pattern.compile(regexp).split(text, limit)
    )
    assertArrayEquals(expected, Pattern.compile(regexp).split(text, limit))
  }

  // Tests that both RE2 and JDK's Matchers do the same replaceFist.
  def testReplaceAll(
      orig: String,
      regex: String,
      repl: String,
      actual: String
  ): Unit = {
    val p = Pattern.compile(regex)
    val m = p.matcher(orig)
    var replaced = m.replaceAll(repl)
    assertTrue(actual == replaced)
//    // JDK's
//    val pj = java.util.regex.Pattern.compile(regex)
//    val mj = pj.matcher(orig)
//    replaced = mj.replaceAll(repl)
//    assertTrue(actual == replaced)
  }

  def testReplaceFirst(
      orig: String,
      regex: String,
      repl: String,
      actual: String
  ): Unit = {
    val p = Pattern.compile(regex)
    val m = p.matcher(orig)
    var replaced = m.replaceFirst(repl)
    assertTrue(actual == replaced)
//    val pj = java.util.regex.Pattern.compile(regex)
//    val mj = pj.matcher(orig)
//    replaced = mj.replaceFirst(repl)
//    assertTrue(actual == replaced)
  }

  // Tests that both RE2 and JDK's Patterns/Matchers give the same groupCount.
  def testGroupCount(pattern: String, count: Int): Unit = { // RE2
    val p = Pattern.compile(pattern)
    val m = p.matcher("x")
    assertTrue(
      s"pattern: ${pattern} p.groupCount: ${} != expected: ${count}",
      count == p.groupCount()
    )
    assertTrue(
      s"pattern: ${pattern} m.groupCount: ${} != expected: ${count}",
      count == m.groupCount()
    )

    // JDK -- SN j.u.regex calls into scalanative.regex, so something
    // rotten on false.
    val pj = java.util.regex.Pattern.compile(pattern)
    val mj = pj.matcher("x")
    assertTrue(
      s"pattern: ${pattern} mj.groupCount: ${} != expected: ${count}",
      count == mj.groupCount
    )
  }

  def testGroup(text: String, regexp: String, output: Array[String]): Unit = {

    val p = Pattern.compile(regexp)
    val matchString = p.matcher(text)
    assertTrue(s"scalanative.regex find failed", matchString.find())

    // This tests ms.group code path, for loop tests the ms.group(0) path.
    assertTrue(
      s"output(0): ${output(0)} != expected: ${matchString.group()}",
      output(0) == matchString.group()
    )

    for (i <- 0 until output.length) {
      assertTrue(
        s"output(${i}): ${output(i)} != expected:" +
          s" ${matchString.group(i)}",
        output(i) == matchString.group(i)
      )
    }

    assertTrue(
      s"length - 1: ${output.length - 1} != expected: " +
        s"${matchString.groupCount()}",
      output.length - 1 == matchString.groupCount()
    )

    val pj = java.util.regex.Pattern.compile(regexp)
    val matchStringj = pj.matcher(text)
    assertTrue(s"j.u.regex find failed", matchStringj.find)

    assertTrue(
      s"j.u.regex output(0): ${output(0)} != " +
        s"expected: ${matchStringj.group}",
      output(0) == matchStringj.group
    )

    for (i <- 0 until output.length) {
      assertTrue(
        s"matchString(${i}): ${matchString.group(i)} != " +
          s"java: ${matchStringj.group(i)}",
        matchString.group(i) == matchStringj.group(i)
      )
    }
  }

  def testFind(
      text: String,
      regexp: String,
      start: Int,
      output: String
  ): Unit = {
    val p = Pattern.compile(regexp)
    val matchString = p.matcher(text)
    // RE2Matcher matchBytes = p.matcher(text.getBytes(Charsets.UTF_8));
    assertTrue(matchString.find(start))
    // assertTrue(matchBytes.find(start));
    assertTrue(output == matchString.group())
    // assertTrue(output == matchBytes.group());
    val pj = java.util.regex.Pattern.compile(regexp)
    val matchStringj = pj.matcher(text)
    assertTrue(matchStringj.find(start))
    assertTrue(output == matchStringj.group)
  }

  def testFindNoMatch(text: String, regexp: String, start: Int): Unit = {
    val p = Pattern.compile(regexp)
    val matchString = p.matcher(text)
    assertFalse(matchString.find(start))
    // assertFalse(matchBytes.find(start));
    val pj = java.util.regex.Pattern.compile(regexp)
    val matchStringj = pj.matcher(text)
    assertFalse(matchStringj.find(start))
  }

  def testInvalidGroup(text: String, regexp: String, group: Int): Unit = {
    val p = Pattern.compile(regexp)
    val m = p.matcher(text)
    m.find()
    m.group(group)
    fail("") // supposed to have exception by now
  }

  def verifyLookingAt(text: String, regexp: String, output: Boolean): Unit = {
    assertTrue(output == Pattern.compile(regexp).matcher(text).lookingAt())
    assertTrue(
      output == java.util.regex.Pattern.compile(regexp).matcher(text).lookingAt
    )
  }
}
