package scala.scalanative
package regex

import java.util

import ScalaTestCompat.fail

object ApiTestUtils extends tests.Suite {

  def assertArrayEquals[A](arr1: Array[A], arr2: Array[A]) =
    assert(arr1.deep == arr2.deep)

  /**
   * Asserts that IllegalArgumentException is thrown from compile with flags.
   */
  def assertCompileFails(regex: String, flag: Int): Unit = {
    try {
      Pattern.compile(regex, flag)
      fail(
        "Compiling Pattern with regex: " + regex + " and flag: " + flag + " passed, when it should have failed.")
    } catch {
      case e: IllegalArgumentException =>
        if (!("Flags UNIX_LINES and COMMENTS unsupported" == e.getMessage))
          throw e
    }
  }

  /**
   * Asserts all strings in array equal.
   */
  def assertArrayEquals(expected: Array[AnyRef],
                        actual: Array[AnyRef]): Unit = {
    assert(
      expected.length == actual.length,
      "Arrays have unequal length, therefore can't be equal to " + "each other. Expected: " + util.Arrays
        .toString(expected) + " Actual: " + util.Arrays.toString(actual)
    )
    var idx = 0
    while (idx < expected.length) {
      assert(expected(idx) == actual(idx),
             "Index: " + idx + " is unequal in the arrays")
      idx += 1
    }
  }

  /**
   * Tests that both RE2's and JDK's pattern class act as we expect them.
   * The regular expression {@code regexp} matches the string {@code match} and
   * doesn't match {@code nonMatch}
   *
   * @param regexp
   * @param match
   * @param nonMatch
   */
  def testMatches(regexp: String, `match`: String, nonMatch: String): Unit = {
    val errorString = "Pattern with regexp: " + regexp
    assert(java.util.regex.Pattern.matches(regexp, `match`),
           "JDK " + errorString + " doesn't match: " + `match`)
    assert(!java.util.regex.Pattern.matches(regexp, nonMatch),
           "JDK " + errorString + " matches: " + nonMatch)
    assert(Pattern.matches(regexp, `match`),
           errorString + " doesn't match: " + `match`)
    assert(!Pattern.matches(regexp, nonMatch),
           errorString + " matches: " + nonMatch)
  }

  // Test matches via a matcher.
  def testMatcherMatches(regexp: String,
                         `match`: String,
                         nonMatch: String): Unit = {
    testMatcherMatches(regexp, `match`)
    testMatcherNotMatches(regexp, nonMatch)
  }

  def testMatcherMatches(regexp: String, `match`: String): Unit = {
    val p = java.util.regex.Pattern.compile(regexp)
    assert(p.matcher(`match`).matches,
           "JDK Pattern with regexp: " + regexp + " doesn't match: " + `match`)
    val pr = Pattern.compile(regexp)
    assert(pr.matcher(`match`).matches,
           "Pattern with regexp: " + regexp + " doesn't match: " + `match`)
  }

  def testMatcherNotMatches(regexp: String, nonMatch: String): Unit = {
    val p = java.util.regex.Pattern.compile(regexp)
    assert(!p.matcher(nonMatch).matches,
           "JDK Pattern with regexp: " + regexp + " matches: " + nonMatch)
    val pr = Pattern.compile(regexp)
    assert(!pr.matcher(nonMatch).matches,
           "Pattern with regexp: " + regexp + " matches: " + nonMatch)
  }

  /**
   * This takes a regex and it's compile time flags, a string that is expected
   * to match the regex and a string that is not expected to match the regex.
   *
   * We don't check for JDK compatibility here, since the flags are not in a 1-1
   * correspondence.
   *
   */
  def testMatchesRE2(regexp: String,
                     flags: Int,
                     `match`: String,
                     nonMatch: String): Unit = {
    val p           = Pattern.compile(regexp, flags)
    val errorString = "Pattern with regexp: " + regexp + " and flags: " + flags
    assert(p.matches(`match`), errorString + " doesn't match: " + `match`)
    assert(!p.matches(nonMatch), errorString + " matches: " + nonMatch)
  }

  def testMatchesRE2(regexp: String,
                     flags: Int,
                     matches: Array[String],
                     nonMatches: Array[String]): Unit = {
    val p = Pattern.compile(regexp, flags)
    for (s <- matches) {
      assert(p.matches(s))
    }
    for (s <- nonMatches) {
      assert(!p.matches(s))
    }
  }

  /**
   * Tests that both RE2 and JDK split the string on the regex in the same way,
   * and that that way matches our expectations.
   */
  def testSplit(regexp: String, text: String, expected: Array[String]): Unit = {
    testSplit(regexp, text, 0, expected)
  }

  def testSplit(regexp: String,
                text: String,
                limit: Int,
                expected: Array[String]): Unit = {
    assertArrayEquals(
      expected,
      java.util.regex.Pattern.compile(regexp).split(text, limit))
    assertArrayEquals(expected, Pattern.compile(regexp).split(text, limit))
  }

  // Tests that both RE2 and JDK's Matchers do the same replaceFist.
  def testReplaceAll(orig: String,
                     regex: String,
                     repl: String,
                     actual: String): Unit = {
    val p        = Pattern.compile(regex)
    val m        = p.matcher(orig)
    var replaced = m.replaceAll(repl)
    assert(actual == replaced)
//    // JDK's
//    val pj = java.util.regex.Pattern.compile(regex)
//    val mj = pj.matcher(orig)
//    replaced = mj.replaceAll(repl)
//    assert(actual == replaced)
  }

  def testReplaceFirst(orig: String,
                       regex: String,
                       repl: String,
                       actual: String): Unit = {
    val p        = Pattern.compile(regex)
    val m        = p.matcher(orig)
    var replaced = m.replaceFirst(repl)
    assert(actual == replaced)
//    val pj = java.util.regex.Pattern.compile(regex)
//    val mj = pj.matcher(orig)
//    replaced = mj.replaceFirst(repl)
//    assert(actual == replaced)
  }

  // Tests that both RE2 and JDK's Patterns/Matchers give the same groupCount.
  def testGroupCount(pattern: String, count: Int): Unit = { // RE2
    val p = Pattern.compile(pattern)
    val m = p.matcher("x")
    assert(count == p.groupCount,
           s"pattern: ${pattern} p.groupCount: ${} != expected: ${count}")
    assert(count == m.groupCount,
           s"pattern: ${pattern} m.groupCount: ${} != expected: ${count}")

    // JDK -- SN j.u.regex calls into scalanative.regex, so somethin
    // rotten on false.
    val pj = java.util.regex.Pattern.compile(pattern)
    val mj = pj.matcher("x")
    assert(count == mj.groupCount,
           s"pattern: ${pattern} mj.groupCount: ${} != expected: ${count}")
  }

  def testGroup(text: String, regexp: String, output: Array[String]): Unit = {

    val p           = Pattern.compile(regexp)
    val matchString = p.matcher(text)
    assert(matchString.find, s"scalanative.regex find failed")

    // This tests ms.group code path, for loop tests the ms.group(0) path.
    assert(output(0) == matchString.group,
           s"output(0): ${output(0)} != expected: ${matchString.group}")

    for (i <- 0 until output.length) {
      assert(output(i) == matchString.group(i),
             s"output(${i}): ${output(i)} != expected:" +
               s" ${matchString.group(i)}")
    }

    assert(output.length - 1 == matchString.groupCount,
           s"length - 1: ${output.length - 1} != expected: " +
             s"${matchString.groupCount}")

    val pj           = java.util.regex.Pattern.compile(regexp)
    val matchStringj = pj.matcher(text)
    assert(matchStringj.find, s"j.u.regex find failed")

    assert(output(0) == matchStringj.group,
           s"j.u.regex output(0): ${output(0)} != " +
             s"expected: ${matchStringj.group}")

    for (i <- 0 until output.length) {
      assert(matchString.group(i) == matchStringj.group(i),
             s"matchString(${i}): ${matchString.group(i)} != " +
               s"java: ${matchStringj.group(i)}")
    }
  }

  def testFind(text: String,
               regexp: String,
               start: Int,
               output: String): Unit = {
    val p           = Pattern.compile(regexp)
    val matchString = p.matcher(text)
    // RE2Matcher matchBytes = p.matcher(text.getBytes(Charsets.UTF_8));
    assert(matchString.find(start))
    // assert(matchBytes.find(start));
    assert(output == matchString.group)
    // assert(output == matchBytes.group());
    val pj           = java.util.regex.Pattern.compile(regexp)
    val matchStringj = pj.matcher(text)
    assert(matchStringj.find(start))
    assert(output == matchStringj.group)
  }

  def testFindNoMatch(text: String, regexp: String, start: Int): Unit = {
    val p           = Pattern.compile(regexp)
    val matchString = p.matcher(text)
    assert(!matchString.find(start))
    // assertFalse(matchBytes.find(start));
    val pj           = java.util.regex.Pattern.compile(regexp)
    val matchStringj = pj.matcher(text)
    assert(!matchStringj.find(start))
  }

  def testInvalidGroup(text: String, regexp: String, group: Int): Unit = {
    val p = Pattern.compile(regexp)
    val m = p.matcher(text)
    m.find
    m.group(group)
    fail("") // supposed to have exception by now
  }

  def verifyLookingAt(text: String, regexp: String, output: Boolean): Unit = {
    assert(output == Pattern.compile(regexp).matcher(text).lookingAt)
    assert(
      output == java.util.regex.Pattern.compile(regexp).matcher(text).lookingAt)
  }
}
