package scala.issues

import org.junit.Assert._
import org.junit.Test

class RegexIssuesTest:

  /* Issue 3631 describes a parse failue in a complicated regex.
   * To increase confidence, the Test should stay as close as feasible
   * to the original report.
   *
   * The complication is that Scala 2.12 regex class does not have
   * the "matches" method used in the Issue. That method was introduced
   * in Scala 2.13.
   *
   * To reduce duplication & confusion, this Test is run only on Scala 3.
   *
   * This test should be run on both Scala and JVM to ensure that the regex
   * from the Issue continues to parse on the latter.
   */

  @Test def test_Issue3631(): Unit = {
    // Use the full regex from the Issue, which is _not_ the minimal reproducer.
    val pattern = "^(\\-|\\+)?(0\\.[0-9]+|[1-9][0-9]*\\.[0-9]+|[1-9][0-9]*|0)$"
    val expected = "1"

    assertTrue(
      s"regex '${pattern}' does not match '${expected}'",
      pattern.r.matches(expected)
    )
  }

end RegexIssuesTest
