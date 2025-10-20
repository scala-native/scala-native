// this should be shared - some failures on JVM
package org.scalanative.testsuite.javalib.util.regex

import java.util.*
import java.util.regex.*

import java.util.stream.Stream as jStream

import scala.collection.immutable.List
import org.junit.Ignore
import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*

import scala.scalanative.junit.utils.CollectionConverters.*
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.*

/* assumeFalse executingInJVM should either be fixed or moved to a Scala Native
 * re2 specific test
 */
class PatternTest {

  @Test def compileRegex(): Unit = {
    val p1 = Pattern.compile("xyz")
  }

  @Test def compileRegexFlagsInvalidFlag(): Unit = {
    // fails in CI on Java 8 - works locally with Java 11 without the assumeFalse
    assumeFalse(
      "Fails in JVM, expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown",
      executingInJVM
    )
    assertThrows(
      classOf[IllegalArgumentException],
      Pattern.compile(":", 0xa0000000)
    )
  }

  @Test def compileRegexFlagsUnsupportedFlags(): Unit = {
    assumeFalse(
      "Fails in JVM, expected java.lang.UnsupportedOperationException to be thrown, but nothing was thrown",
      executingInJVM
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      Pattern.compile("m", Pattern.CANON_EQ)
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      Pattern.compile("n", Pattern.COMMENTS)
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      Pattern.compile("o", Pattern.UNICODE_CASE)
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      Pattern.compile("p", Pattern.UNICODE_CHARACTER_CLASS)
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      Pattern.compile("q", Pattern.UNIX_LINES)
    )
  }

  @Test def exerciseMatcherMethodsBeforeUsingThem(): Unit = {
    // Establish a baseline for correctness.
    // Many tests below assume that Matcher methods matcher() and find() are
    // available and correct.
    // Check that at least the simple case of using them with no
    // Pattern#compile flags works.  If that does not work, every test
    // using these methods is suspect.
    val needle = "needle"
    val haystack = "haystack & needle"
    val m = Pattern.compile(needle).matcher(haystack)
    assertTrue(s"should have found ${needle} in ${haystack}", m.find())
  }

  @Test def compileRegexFlagsPatternCaseInsensitive(): Unit = {
    val needle = "hubble telescope"
    val haystack = "Hubble Telescope"
    val m = Pattern
      .compile(needle, Pattern.CASE_INSENSITIVE)
      .matcher(haystack)
    assertTrue(s"should have found '${needle}' in '${haystack}'", m.find())
  }

  @Test def compileRegexFlagsPatternCaseDotall(): Unit = {
    val needle = "Four score.*Units"
    val haystack = "Four score and seven years ago\nOur Parental Units"

    val m = Pattern.compile(needle).matcher(haystack)
    assertFalse(s"should not have found '${needle}' in '${haystack}'", m.find())

    val m2 = Pattern.compile(needle, Pattern.DOTALL).matcher(haystack)
    assertTrue(s"should have found '${needle}' in '${haystack}'", m2.find())
  }

  @Test def compileRegexFlagsPatternLiteral(): Unit = {
    val needle = "(a)(b$)?(b)?"
    val haystack = "(a)(b$)?(b)?"
    val m = Pattern
      .compile(needle, Pattern.LITERAL)
      .matcher(haystack)
    assertTrue(s"should have found '${needle}' in '${haystack}'", m.find())
  }

  @Test def compileRegexFlagsCaseInsensitiveLiteral(): Unit = {
    val needle = "(a)(b$)?(b)?"
    val haystack = "(a)(B$)?(b)?"
    val m = Pattern
      .compile(needle, Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
      .matcher(haystack)
    assertTrue(s"should have found '${needle}' in '${haystack}'", m.find())
  }

  @Test def testAsPredicate(): Unit = {

    val needle = "needle"
    val haystack = needle
    val p = Pattern.compile(needle)

    val pred = p.asPredicate()

    assertTrue(
      s"should have found '${needle}' in '${haystack}'",
      pred.test(haystack)
    )

    // Let's get complicated to show that asPredicate() uses
    // full 'matches' not partial 'find'.
    // That seems to be the JVM behavior, if you can tease it out.

    // Tokens start with 'L', have at least one additional word
    // character and contain only word characters until the end.
    // '\Z' is currently unsupported in Scala Native.

    val needle2 = "\\AL\\w\\w+\\z"
    val p2 = Pattern.compile(needle2)
    val pred2 = p2.asPredicate()

    val shouldNotFind = Array("L9", "Life ", "LovePotion#9", "funny")

    assertTrue(
      s"should not have found '${needle2}' in" +
        s" '${shouldNotFind.toString}'",
      shouldNotFind.filter(pred2.test).length == 0
    )

    val shouldFind = Array("Life", "Liberty", "Love")

    val expectedLength = shouldFind.length
    val resultLength = shouldFind.filter(pred2.test).length

    assertTrue(
      s"number found: ${resultLength} != expected: ${expectedLength}",
      resultLength == expectedLength
    )
  }

  @Test def splitSplitN(): Unit = {
    val input = "boo:and:foo"

    val p1 = Pattern.compile(":")
    assertTrue(s"A1", p1.split(input).toList == List("boo", "and", "foo"))
    assertTrue(s"A2", p1.split(input, 2).toList == List("boo", "and:foo"))
    assertTrue(s"A3", p1.split(input, 5).toList == List("boo", "and", "foo"))
    assertTrue(s"A4", p1.split(input, -2).toList == List("boo", "and", "foo"))

    val p2 = Pattern.compile("o")
    assertTrue(s"A5", p2.split(input).toList == List("b", "", ":and:f"))
    assertTrue(
      s"A6",
      p2.split(input, 5).toList == List("b", "", ":and:f", "", "")
    )
    assertTrue(
      s"A7",
      p2.split(input, -2).toList == List("b", "", ":and:f", "", "")
    )
    assertTrue(s"A8", p2.split(input, 0).toList == List("b", "", ":and:f"))
  }

  @Test def testSplitAsStream(): Unit = {
    val input = "boo:and:foo"

    val p1 = Pattern.compile(":")

    essaySplitAsStream(
      p1.splitAsStream(input),
      Array("boo", "and", "foo"),
      "A_1"
    )

    val p2 = Pattern.compile("o")

    essaySplitAsStream(p2.splitAsStream(input), Array("b", "", ":and:f"), "A5")
  }

  @Test def testQuote(): Unit = {
    assertEquals(Pattern.quote("1.5-2.0?"), "\\Q1.5-2.0?\\E")
  }

  @Test def characters(): Unit = {
    pass("a", "a")

    // backslash
    pass("\\\\", "\\")

    // octal
    pass("\\01", "\u0001")
    pass("\\012", "\u000A") // 12 octal = A hex

    // hex
    pass("\\x10", "\u0010")

    pass("\\t", "\t") // tab
    pass("\\n", "\n") // newline
    pass("\\r", "\r") // carriage-return
    pass("\\f", "\f") // form-feed
    pass("\\a", "\u0007") // alert (bell)
  }

  @Test def unicodeBlock(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    pass("\\p{InGreek}", "α")
    pass("\\p{Greek}", "Ω")
    fail("\\p{InGreek}", "a")
    pass("\\P{InGreek}", "a") // not in block

    pass("\\p{InLatin}", "a")
    pass("\\p{Latin}", "a")
    fail("\\p{InLatin}", "α")
    pass("\\P{InLatin}", "α") // not in block
  }

  @Ignore("#620")
  @Test def notSupportedCharacterClasses(): Unit = {
    pass("\\0100", "\u0040") // 100 octal = 40 hex
    pass("\\uBEEF", "\uBEEF")
    pass("\\e", "\u001B") // escape
    pass("\\cZ", s"\\x1A") // Control-Z
  }

  @Test def characterClasses(): Unit = {
    pass("[abc]", "a")
    fail("[^abc]", "c")
    pass("[a-zA-Z]", "T")
    pass("\\xFF", "\u00FF")
  }

  @Test def predefinedCharacterClasses(): Unit = {
    pass(".", "a")
    pass("\\d", "0")
    pass("\\D", "a")
    pass("\\s", " ")
    pass("\\S", "a")
    pass("\\w", "a")
    pass("\\W", "-")
  }

  @Test def posixCharacterClasses(): Unit = {
    pass("\\p{Alnum}", "a")
    pass("\\p{Alpha}", "a")
    pass("\\p{ASCII}", "a")
    pass("\\p{Blank}", " ")
    pass("\\p{Cntrl}", "\u0003")
    pass("\\p{Digit}", "1")
    pass("\\p{Graph}", "a")
    pass("\\p{Lower}", "a")
    pass("\\p{Print}", "a")
    pass("\\p{Punct}", ".")
    pass("\\p{Space}", " ")
    pass("\\p{Upper}", "A")
    pass("\\p{XDigit}", "a")
  }

  @Test def unicodeClasses(): Unit = {
    pass("\\p{Lu}", "A")
    pass("\\p{Sc}", "$")

    fail("\\p{Lu}", "@") // should not be in Uppercase class
    pass("\\P{Lu}", "@") // but should be in negated class. Thanks, Aristotle!
  }
  @Ignore("#620")
  @Test def notSupportedUnicodeClasses(): Unit = {
    // not supported: IsAlphabetic binary property.
    pass("\\p{IsAlphabetic}", "a")
    fail("\\p{IsAlphabetic}", "-")
  }

  @Test def unicodeScript(): Unit = {
    // "\u03b1" is Greek alpha character, "\u0061" is Latin lowercase 'a'
    pass("\\p{IsGreek}", "\u03b1") // behavior changes in Java 9
    fail("\\p{IsGreek}", "\u0061")
    pass("\\P{IsGreek}", "\u0061") // not in block

    pass("\\p{IsLatin}", "\u0061") // behavior changes in Java 9
    fail("\\p{IsLatin}", "\u03b1")
    pass("\\P{IsLatin}", "\u03b1") // not in block
  }

  @Test def boundaryMatchers(): Unit = {
    pass("^a", "a")
    pass("$", "")
    pass("foo\\b", "foo")
    fail("foo\\B", "foo")
    pass("\\AAbcdef", "Abcdef")

    // \z = end of input
    pass("foo\\z", "foo")
    find("foo\\z", "foo\n", pass = false)
  }

  @Ignore("#620")
  @Test def boundaryMatchersPrevious(): Unit = {
    // \G = at the end of the previous match
    val m1 = Pattern.compile("\\Gfoo").matcher("foofoo foo")
    assertTrue(m1.find())
    assertTrue(m1.find())
    assertTrue(!m1.find())

    // \Z = end of input of the final terminator
    find("foo\\Z", "foo")
    find("foo\\Z", "foo\n")

    // \R Unicode linebreak
    pass("\\R", "\u000D\u000A")
  }

  @Ignore("no issue")
  @Test def boundaryMatchersRegion(): Unit = {
    locally {
      val needle = "^a"
      val haystack = "01234abcabc"

      val m = Pattern.compile(needle).matcher(haystack)

      val regionBegin = 5
      val regionEnd = 9
      val regionString = haystack.slice(regionBegin, regionEnd)

      m.region(5, 9)

      assertTrue(s"should have found ${needle} in ${regionString}", m.find())

      val foundPos = m.start
      val expectedPos = 5

      assertTrue(
        s"found position: ${foundPos} != expected: ${expectedPos}",
        foundPos == expectedPos
      )
    }

    locally {
      val needle = "^a"
      val haystack = "01234abcabc"
      val m = Pattern.compile(needle).matcher(haystack)

      m.region(4, 9)

      assertFalse(
        s"should not have found ${needle} at " +
          s"position: ${m.start} in ${haystack}",
        m.find()
      )
    }
  }

  @Test def greedyQuantifiers(): Unit = {
    // once or zero
    pass("X?", "")
    pass("X?", "X")
    fail("^X?$", "Y")

    // zero or more
    pass("X*", "X")
    pass("X*", "")
    pass("X*", "XX")

    // one or more
    pass("X+", "X")
    fail("X+", "")
    pass("X+", "XX")

    // exactly n
    pass("X{3}", "XXX")
    fail("^X{3}$", "XXXX")

    // n or more
    pass("^X{3,}$", "XXXX")
    fail("^X{3,}$", "XX")

    // n but not more than m
    fail("^X{3,5}$", "XX")
    pass("^X{3,5}$", "XXX")
    pass("^X{3,5}$", "XXXX")
    pass("^X{3,5}$", "XXXXX")
    fail("^X{3,5}$", "XXXXXX")
  }

  @Test def lazyQuantifiers(): Unit = {
    // zero or one, prefer zero
    pass("X??", "")
    pass("X??", "X")

    // zero or more, prefer fewer
    pass("X*?", "")
    pass("X*?", "XXX")

    // one or more, prefer fewer
    fail("X+?", "")
    pass("X+?", "XXX")

    // exactly n
    pass("X{3}?", "XXX")
    fail("^X{3}?$", "XXXX")

    // n or more, prefer fewer
    pass("^X{3,}?$", "XXXX")
    fail("^X{3,}?$", "XX")

    // [n, m], prefer fewer
    fail("^X{3,5}?$", "XX")
    pass("^X{3,5}?$", "XXX")
    pass("^X{3,5}?$", "XXXX")
    pass("^X{3,5}?$", "XXXXX")
    fail("^X{3,5}?$", "XXXXXX")
  }

  @Test def compositeOperators(): Unit = {
    pass("abc", "abc")

    // x or y, prefer x
    pass("a|b", "a")
    pass("a|b", "b")
    fail("a|b", "c")
  }

  @Test def quotation(): Unit = {
    pass("\\Qa|b|c\\E", "a|b|c")
  }

  @Test def javaNamedGroups(): Unit = {
    pass("(?<foo>a)", "a")
  }

  // re2 syntax is not defined in Java, but it works with scalanative.regex
  @Test def re2NamedGroupsNotInJava8(): Unit = {
    assumeFalse("Fails in JVM", executingInJVM)
    pass("(?P<foo>a)", "a")
  }

  @Test def nonCapturingGroups(): Unit = {
    pass("(?:a)", "a")
  }

  @Test def flagsInRegex(): Unit = {
    pass("(?i)iNsEnSiTiVe", "insensitive")
    pass("(?i:iNsEnSiTiVe)", "insensitive")
  }

  @Test def testToString(): Unit = {
    val in = "foo|bar"
    assertEquals(Pattern.compile(in).toString, in)
  }

  @Ignore("#620")
  @Test def notSupportedCharacterClassesUnionAndIntersection(): Unit = {
    pass("[a-d[m-p]]", "acn")
    pass("[[a-z] && [b-y] && [c-x]]", "g")
    pass("[a-z&&[def]]", "e")
    pass("[a-z&&[^aeiou]]", "c")
    fail("[a-z&&[^aeiou]]", "o")
    pass("[a-z&&[^m-p]]", "c")
    fail("[a-z&&[^m-p]]", "n")
  }

  @Ignore("#620")
  @Test def notSupportedPredefinedCharClassesHorizontalAndVertical(): Unit = {
    pass("\\h", " ")
    pass("\\H", "a")
    pass("\\v", "\n")
    pass("\\V", "a")
  }

  @Ignore("#620")
  @Test def notSupportedJavaCharacterFunctionClasses(): Unit = {
    pass("\\p{javaLowerCase}", "a")
    pass("\\p{javaUpperCase}", "A")
    pass("\\p{javaWhitespace}", " ")
    pass("\\p{javaMirrored}", "{") // mirrored with }
    fail("\\p{javaMirrored}", "c")
  }

  /*
    Google's RE2 does not support back references because they can be infinite
    https://github.com/google/re2/wiki/WhyRE2
    https://github.com/google/re2/blob/2017-03-01/doc/syntax.txt
   */
  @Ignore("#620")
  @Test def notSupportedBackReferences(): Unit = {
    pass("(a)\\1", "aa")
    pass("(?<foo>a)\\k<foo>", "aa")
  }

  @Ignore("#620")
  @Test def notSupportedLookaheads(): Unit = {
    // positive lookahead
    passAndFail(".*\\.(?=log$).*$", "a.b.c.log", "a.b.c.log.")

    // negative lookahead
    passAndFail(".*\\.(?!log$).*$", "abc.logg", "abc.log")

    // positive lookbehind
    passAndFail(".*(?<=abc)\\.log$", "abc.log", "cde.log")

    // negative lookbehind
    passAndFail(".*(?<!abc)\\.log$", "cde.log", "abc.log")

    // atomic group
    pass("(?>a*)abb", "aaabb")
    pass("(?>a*)bb", "aaabb")
    pass("(?>a|aa)aabb", "aaabb")
    pass("(?>aa|a)aabb", "aaabb")

    // quantifiers over look ahead
    passAndFail(".*(?<=abc)*\\.log$", "cde.log", "cde.log")
  }

  @Ignore("#620")
  @Test def notSupportedPossessiveQuantifiers(): Unit = {
    // zero or one, prefer more
    pass("X?+", "")
    pass("X?+", "X")
    find(".?+X", "X", pass = false)

    // zero or more, prefer more
    pass("X*+", "")
    pass("X*+", "XXX")
    find(".?+X", "X", pass = false)

    // one or more, prefer more
    fail("X++", "")
    pass("X++", "XXX")

    // exactly n
    pass("X{3}+", "XXX")
    fail("^X{3}+$", "XXXX")

    // n or more, prefer more
    pass("^X{3,}+$", "XXXX")
    fail("^X{3,}+$", "XX")

    // [n, m], prefer more
    fail("^X{3,5}+$", "XX")
    pass("^X{3,5}+$", "XXX")
    pass("^X{3,5}+$", "XXXX")
    pass("^X{3,5}+$", "XXXXX")
    fail("^X{3,5}+$", "XXXXXX")
  }

  @Test def multibyteCharacters(): Unit = {
    find("こんにちは", "こんにちはみなさま")
  }

  @Test def characterClassConsistingOfMultibyteCharacters(): Unit = {
    pass(
      "^[\u0000-\u00a0\u1680\u2000-\u200a\u202f\u205f\u3000\u2028\u2029]$",
      "\u200a"
    )
  }

  @Test def groupNotContainingMultibyteCharacters(): Unit = {
    val pat = "abcdef(ghi)jkl"
    val input = "abcdefghijkl"

    val m = Pattern.compile(pat).matcher(input)

    assertTrue(s"A_1", m.matches())
    assertTrue(s"A_2", m.group(0) == input)
    assertTrue(s"A_3", m.group(1) == "ghi")
    assertTrue(s"A_4", m.group() == input)
  }

  @Test def groupContainingMultibyteCharacters(): Unit = {
    val pat = "abcあいう(えお)def"
    val input = "abcあいうえおdef"
    val m = Pattern.compile(pat).matcher(input)
    assertTrue(m.matches())
    assertEquals(input, m.group(0))
    assertEquals("えお", m.group(1))
    assertEquals(input, m.group())
  }

  @Test def compilingLotsOfPatterns(): Unit = {
    val pats = (0 until 200).map(i => Pattern.compile(i.toString))
    // pick a newer pattern (likely in cache).
    locally {
      val pat = pats(198)
      val m = pat.matcher("198")
      assertTrue(m.matches())
    }
    // pick an older pattern (likely out of cache).
    locally {
      val pat = pats(1)
      val m = pat.matcher("1")
      assertTrue(m.matches())
    }
  }

  @Test def syntaxExceptions(): Unit = {
    assumeFalse(
      "Fails in JVM, expected:<[Trailing Backslash]> but was:<[Unexpected internal error]>",
      executingInJVM
    )

    try {
      Pattern.compile("foo\\L")
    } catch {
      case e: PatternSyntaxException =>
        assertEquals(
          "Illegal/unsupported escape sequence",
          e.getDescription
        )

        assertEquals(4, e.getIndex)
        assertEquals("foo\\L", e.getPattern)

        assertEquals(
          """|Illegal/unsupported escape sequence near index 4
             |foo\L
             |    ^""".stripMargin,
          e.getMessage
        )
    }

    /// Ordered alphabetical by description (second arg).
    /// Helps ensuring that each scalanative/regex Parser description
    /// matches its JVM equivalent.
    ///
    /// These are _not_ all the JVM parser PatternSyntaxExceptions available.
    /// They are only the cases reported by scalanative.regex.Parser.scala
    ///
    /// Some tests are marked MISSING because I (LeeT.) have not yet
    /// figured out a pattern which will trigger the error.

    // MISSING: Test scalanative.regex ERR_INVALID_NAMED_CAPTURE
    //          "Bad named capture group"

    // There are two conditions which currently yield the same description.
    // ERR_MISSING_REPEAT_ARGUMENT matches the JVM.
    // Trigger for ERR_MISSING_REPEAT_ARGUMENT  not yet found.

    // Test scalanative.regex ERR_MISSING_REPEAT_ARGUMENT
    syntax("*", "Dangling meta character '*'", 0)

    // MISSING: Test scalanative.regex ERR_MISSING_REPEAT_ARGUMENT
    //          "Dangling meta character '*'"

    syntax("[a-0]", "Illegal character range", 3)
    syntax("foo\\Lbar", "Illegal/unsupported escape sequence", 4)

    // MISSING: Test scalanative.regex ERR_INVALID_REPEAT_OP
    //          "invalid nested repetition operator"

    syntax("foo\\", "Trailing Backslash", 4)
    syntax("foo[bar", "Unclosed character class", 6)
    syntax("foo(bar(foo)baz", "Unclosed group", 15)
    syntax("(?q)", "Unknown inline modifier", 2) // bad perl operator
    syntax("foo(foo)bar)baz", "Unmatched closing ')'", 10)
  }

  private def syntax(pattern: String, description: String, index: Int): Unit = {
    try {
      Pattern.compile(pattern)
    } catch {
      case e: PatternSyntaxException =>
        assertEquals(description, e.getDescription)
        assertEquals(pattern, e.getPattern)
        assertEquals(index, e.getIndex)
    }
  }

  private def pass(pattern: String, input: String): Unit =
    matches(pattern, input, pass = true)

  private def fail(pattern: String, input: String): Unit =
    matches(pattern, input, pass = false)

  private def passAndFail(
      pattern: String,
      passInput: String,
      failInput: String
  ): Unit = {
    pass(pattern, passInput)
    fail(pattern, failInput)
  }

  private def assertRegex(
      pass: Boolean,
      ret: Boolean,
      mid: String,
      pattern: String,
      input: String
  ): Unit = {
    val ret0 =
      if (pass) ret
      else !ret

    assertTrue(ret0)
  }

  private def matches(pattern: String, input: String, pass: Boolean): Unit = {

    val ret = Pattern.matches(pattern, input)

    val mid =
      if (pass) "does not match"
      else "should not match"

    assertRegex(pass, ret, mid, pattern, input)
  }

  private def find(
      pattern: String,
      input: String,
      pass: Boolean = true
  ): Unit = {
    val ret = Pattern.compile(pattern).matcher(input).find()

    val mid =
      if (pass) "does not match"
      else "should not match"

    assertRegex(pass, ret, mid, pattern, input)
  }

  private def essaySplitAsStream(
      st: jStream[String],
      expected: Array[String],
      marker: String
  ): Unit = {

    val result = st.iterator.toScalaSeq.toArray

    assertTrue(
      s"${marker} result.size: ${result.size} != ${expected.size}",
      result.size == expected.size
    )

    for (i <- 0 until result.size) {
      assertTrue(
        s"${marker} result(${i}): ${result(i)} != " +
          s"expected: ${expected(i)}",
        result(i) == expected(i)
      )
    }
  }

}
