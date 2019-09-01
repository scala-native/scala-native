package scala.scalanative
package regex

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import scala.scalanative.junit.utils.ThrowsHelper._
import TestUtils._

// Tests are inspired by those projects under Apache2 License:
// j2objc: https://github.com/google/j2objc/blob/master/jre_emul/Tests/java/util/regex/MatcherTest.java#L1
// re2: https://github.com/google/re2/blob/master/re2/testing/re2_test.cc

class MatcherTest {

  @Test def quoteReplacement(): Unit = {
    assertTrue(Matcher.quoteReplacement("") == "")
  }

  @Test def testMatch(): Unit = {
    val m = matcher("foo", "foobar")
    assertFalse(m.matches())
  }

  @Test def replaceAll(): Unit = {
    assertTrue(matcher("abc", "abcabcabc").replaceAll("z") == "zzz")
  }

  @Test def replaceFirst(): Unit = {
    assertTrue(matcher("abc", "abcabcabc").replaceFirst("z") == "zabcabc")
  }

  @Test def testGroup(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assertTrue(groupCount == 2)

    assertThrowsAnd(classOf[IllegalStateException], group)(
      _.getMessage == "No match found"
    )

    assertTrue(find())
    assertTrue(group == "a12z")
    assertTrue(group(0) == "a12z")
    assertTrue(group(1) == "1")
    assertTrue(group(2) == "2")
    assertThrowsAnd(classOf[IndexOutOfBoundsException], group(42))(
      _.getMessage == "No group 42"
    )

    assertTrue(find())
    assertTrue(group == "a34z")
    assertTrue(group(0) == "a34z")
    assertTrue(group(1) == "3")
    assertTrue(group(2) == "4")

    assertFalse(find())
  }

  @Test def startIndexEndIndex(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "012345_a12z_012345")
    import m._

    assertThrowsAnd(classOf[IllegalStateException], start)(
      _.getMessage == "No match found"
    )

    assertThrowsAnd(classOf[IllegalStateException], end)(
      _.getMessage == "No match found"
    )

    assertTrue(find())

    assertTrue(start == 7)
    assertTrue(end == 11)

    assertTrue(start(0) == 7)
    assertTrue(end(0) == 11)

    assertTrue(start(1) == 8)
    assertTrue(end(1) == 9)

    assertTrue(start(2) == 9)
    assertTrue(end(2) == 10)

    assertThrowsAnd(classOf[IndexOutOfBoundsException], start(42))(
      _.getMessage == "No group 42"
    )

    assertThrowsAnd(classOf[IndexOutOfBoundsException], end(42))(
      _.getMessage == "No group 42"
    )

  }

  @Test def startEnd(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assertTrue(find())
    assertTrue(start == 1)
    assertTrue(end == 5)

    assertTrue(find())
    assertTrue(start == 6)
    assertTrue(end == 10)

    assertFalse(find())
  }

  @Test def appendReplacementAppendTail(): Unit = {
    val buf = new StringBuffer()

    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    while (find()) {
      appendReplacement(buf, "{" + group + "}")
    }
    appendTail(buf)
    assertTrue(buf.toString == "_{a12z}_{a34z}_")
  }

  @Test def appendReplacementAppendTailWithGroupReplacementByIndex(): Unit = {
    val buf = new StringBuffer()
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._
    while (find()) {
      appendReplacement(buf, "{$0}")
    }
    appendTail(buf)
    assertTrue(buf.toString == "_{a12z}_{a34z}_")
  }

  @Test def testReset(): Unit = {
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")
    import m._

    assertTrue(find())
    assertTrue(start == 1)
    assertTrue(end == 5)

    reset()

    assertTrue(find())
    assertTrue(start == 1)
    assertTrue(end == 5)

    assertTrue(find())
    assertTrue(start == 6)
    assertTrue(end == 10)

    assertFalse(find())
  }

  @Ignore("we don't support lookahead")
  @Test def hasTransparentBoundsUseTransparentBounds(): Unit = {

    // ?=  <==>	 zero-width positive look-ahead
    val m1 = Pattern.compile("foo(?=buzz)").matcher("foobuzz")
    m1.region(0, 3)
    m1.useTransparentBounds(false)
    assertFalse(m1.matches()) // opaque

    m1.useTransparentBounds(true)
    assertTrue(m1.matches()) // transparent

    // ?!  <==>	 zero-width negative look-ahead
    val m2 = Pattern.compile("foo(?!buzz)").matcher("foobuzz")
    m2.region(0, 3)
    m2.useTransparentBounds(false)
    assertFalse(m2.matches()) // opaque

    m2.useTransparentBounds(true)
    assertTrue(m2.matches()) // transparent
  }

  @Test def lookingAt(): Unit = {
    val m1 = matcher("foo", "foobar")
    assertTrue(m1.lookingAt())
  }

  @Test def pattern(): Unit = {
    val p = Pattern.compile("foo")
    assertTrue(
      p.matcher("foobar").pattern() ==
        p
    )
  }

  @Test def namedGroupsPerl() {

    val p = Pattern.compile(
      "(?P<baz>f(?P<foo>b*a(?P<another>r+)){0,10})" +
        "(?P<bag>bag)?(?P<nomatch>zzz)?"
    )

    val m = p.matcher("fbbarrrrrbag")

    assert(m.matches(), "match failed");
    assertEquals("fbbarrrrr", m.group("baz"));
    assertEquals("bbarrrrr", m.group("foo"));
    assertEquals("rrrrr", m.group("another"));
    assertEquals(0, m.start("baz"));
    assertEquals(1, m.start("foo"));
    assertEquals(4, m.start("another"));
    assertEquals(9, m.end("baz"));
    assertEquals(9, m.end("foo"));
    assertEquals("bag", m.group("bag"));
    assertEquals(9, m.start("bag"));
    assertEquals(12, m.end("bag"));
    assertEquals(null, m.group("nomatch"));
    assertEquals(-1, m.start("nomatch"));
    assertEquals(-1, m.end("nomatch"));

    assertThrowsAnd(classOf[IllegalStateException], m.group("nonexistent"))(
      _.getMessage == "No match found"
    )
  }

  @Test def namedGroupsJava() {

    val p = Pattern.compile(
      "(?<baz>f(?<foo>b*a(?<another>r+)){0,10})" +
        "(?<bag>bag)?(?<nomatch>zzz)?"
    )

    val m = p.matcher("fbbarrrrrbag")

    assert(m.matches(), "match failed");
    assertEquals("fbbarrrrr", m.group("baz"));
    assertEquals("bbarrrrr", m.group("foo"));
    assertEquals("rrrrr", m.group("another"));
    assertEquals(0, m.start("baz"));
    assertEquals(1, m.start("foo"));
    assertEquals(4, m.start("another"));
    assertEquals(9, m.end("baz"));
    assertEquals(9, m.end("foo"));
    assertEquals("bag", m.group("bag"));
    assertEquals(9, m.start("bag"));
    assertEquals(12, m.end("bag"));
    assertEquals(null, m.group("nomatch"));
    assertEquals(-1, m.start("nomatch"));
    assertEquals(-1, m.end("nomatch"));

    assertThrowsAnd(classOf[IllegalStateException], m.group("nonexistent"))(
      _.getMessage == "No match found"
    )
  }

  @Test def stringIndexOutOfBoundsExceptionIssue852(): Unit = {
    val JsonNumberRegex =
      """(-)?((?:[1-9][0-9]*|0))(?:\.([0-9]+))?(?:[eE]([-+]?[0-9]+))?""".r
    val JsonNumberRegex(negative, intStr, decStr, expStr) = "0.000000"
    assertTrue(negative == null)
    assertTrue(intStr == "0")
    assertTrue(decStr == "000000")
    assertTrue(expStr == null)
  }
}
