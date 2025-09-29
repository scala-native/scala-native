package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import TestUtils._

class PatternTest {

  @Test def splitSplitLimit(): Unit = {
    val input = "boo:and:foo"

    val p1 = Pattern.compile(":")
    assertTrue(p1.split(input).toList == List("boo", "and", "foo"))
    assertTrue(p1.split(input, 2).toList == List("boo", "and:foo"))
    assertTrue(p1.split(input, 5).toList == List("boo", "and", "foo"))
    assertTrue(p1.split(input, -2).toList == List("boo", "and", "foo"))

    val p2 = Pattern.compile("o")
    assertTrue(p2.split(input).toList == List("b", "", ":and:f"))
    assertTrue(p2.split(input, 5).toList == List("b", "", ":and:f", "", ""))
    assertTrue(p2.split(input, -2).toList == List("b", "", ":and:f", "", ""))
    assertTrue(p2.split(input, 0).toList == List("b", "", ":and:f"))
  }

  @Test def quote(): Unit = {
    // TODO: taken from re2j, behaviour might differ jdk
    assertTrue(Pattern.quote("1.5-2.0?") == "1\\.5-2\\.0\\?")
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

  @Ignore("not supported")
  @Test def characterClasses2(): Unit = {
    pass("\\0100", "\u0040") // 100 octal = 40 hex
    pass("\\xFF", "\u00FF")
    pass("\\uBEEF", "\uBEEF")
    pass("\\e", "\u001B") // escape
    // control char \cx
  }

  @Test def characterClasses(): Unit = {
    pass("[abc]", "a")
    fail("[^abc]", "c")
    pass("[a-zA-Z]", "T")
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

  @Test def testPOSIXCharacterClasses(): Unit = {
    pass("\\p{Alnum}", "a")
    pass("\\p{Alpha}", "a")
    pass("\\p{ASCII}", "a")
    pass("\\p{Blank}", " ")
    pass("\\p{Digit}", "1")
    pass("\\p{Graph}", "a")
    pass("\\p{Lower}", "a")
    pass("\\p{Print}", "a")
    pass("\\p{Punct}", ".")
    pass("\\p{Space}", " ")
    pass("\\p{Upper}", "A")
    pass("\\p{XDigit}", "a")
    pass("\\p{Cntrl}", new String(Array[Byte](0x7f)))
  }

  @Test def unicodeClasses(): Unit = {
    // Categories
    pass("\\p{Lu}", "A")
    pass("\\p{Sc}", "$")

    // Blocks
    pass("\\p{InGreek}", "α")

    // Scripts
    pass("\\p{IsLatin}", "a")
    fail("\\p{IsLatin}", "α")
  }

  @Ignore
  @Test def pending(): Unit = {
    // The prefix In should only allow blocks like Mongolian
    assertThrows(
      "Unknown character block name {Latin} near index 10",
      classOf[PatternSyntaxException],
      Pattern.compile("\\p{InLatin}")
    )

    // Binary Properties
    pass("\\p{IsAlphabetic}", "a")
    fail("\\p{IsAlphabetic}", "-")
  }

  @Test def boundaryMatchers(): Unit = {
    pass("^a", "a")
    pass("$", "")
    pass("foo\\b", "foo")
    fail("foo\\B", "foo")
    pass("\\AAbcdef", "Abcdef")
    pass("foo\\z", "foo")
  }

  @Ignore("not supported")
  @Test def boundaryMatchers2(): Unit = {
    // \G = at the end of the previous match
    val m1 = Pattern.compile("\\Gfoo").matcher("foofoo foo")
    assertTrue(m1.find())
    assertTrue(m1.find())
    assertFalse(m1.find())

    // \Z = end of input of the final terminator
    find("foo\\Z", "foo")
    find("foo\\Z", "foo\n")

    // \z = end of input
    find("foo\\z", "foo")
    find("foo\\z", "foo\n", pass = false)

    // \R Unicode linebreak
    pass("\\R", "\u000D\u000A")
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

  @Test def namedGroups(): Unit = {
    pass("(?<foo>a)", "a")
  }

  @Test def nonCapturingGroups(): Unit = {
    pass("(?:a)", "a")
  }

  @Test def flags(): Unit = {
    pass("(?i)iNsEnSiTiVe", "insensitive")
    pass("(?i:iNsEnSiTiVe)", "insensitive")
  }

  @Test def testToString(): Unit = {
    val in = "foo|bar"
    assertTrue(Pattern.compile(in).toString == in)
  }

  @Ignore("not supported")
  @Test def characterClassesUnionAndIntersection(): Unit = {
    pass("[a-d[m-p]]", "acn")
    pass("[[a-z] && [b-y] && [c-x]]", "g")
    pass("[a-z&&[def]]", "e")
    pass("[a-z&&[^aeiou]]", "c")
    fail("[a-z&&[^aeiou]]", "o")
    pass("[a-z&&[^m-p]]", "c")
    fail("[a-z&&[^m-p]]", "n")
  }

  @Ignore("not supported")
  @Test def predefinedCharacterClassesHorizontalAndVertical(): Unit = {
    pass("\\h", " ")
    pass("\\H", "a")
    pass("\\v", "\n")
    pass("\\V", "a")
  }

  @Ignore("not supported")
  @Test def javaCharacterFunctionClasses(): Unit = {
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
  @Ignore("not supported")
  @Test def backReferences(): Unit = {
    pass("(a)\\1", "aa")
    pass("(?<foo>a)\\k<foo>", "aa")
  }

  @Ignore("not supported")
  @Test def lookaheads(): Unit = {
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

    // quantifiers over look-ahead
    passAndFail(".*(?<=abc)*\\.log$", "cde.log", "cde.log")
  }

  @Ignore("not supported")
  @Test def possessiveQuantifiers(): Unit = {
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
    assertTrue(m.matches())
    assertTrue(m.group(0) == input)
    assertTrue(m.group(1) == "ghi")
    assertTrue(m.group() == input)
  }

  @Test def groupContainingMultibyteCharacters(): Unit = {
    val pat = "abcあいう(えお)def"
    val input = "abcあいうえおdef"
    val m = Pattern.compile(pat).matcher(input)
    assertTrue(m.matches())
    assertTrue(m.group(0) == input)
    assertTrue(m.group(1) == "えお")
    assertTrue(m.group() == input)
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
    /// Helps ensure that each scalanative/regex Parser description
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

    assertTrue(s""""$pattern" $mid "$input"""", ret0)
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
}
