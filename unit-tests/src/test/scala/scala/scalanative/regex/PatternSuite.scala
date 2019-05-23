package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

import ScalaTestCompat._
import TestUtils._

object PatternSuite extends tests.Suite {

  test("split/split(n)") {
    val input = "boo:and:foo"

    val p1 = Pattern.compile(":")
    assert(p1.split(input).toList == List("boo", "and", "foo"))
    assert(p1.split(input, 2).toList == List("boo", "and:foo"))
    assert(p1.split(input, 5).toList == List("boo", "and", "foo"))
    assert(p1.split(input, -2).toList == List("boo", "and", "foo"))

    val p2 = Pattern.compile("o")
    assert(p2.split(input).toList == List("b", "", ":and:f"))
    assert(p2.split(input, 5).toList == List("b", "", ":and:f", "", ""))
    assert(p2.split(input, -2).toList == List("b", "", ":and:f", "", ""))
    assert(p2.split(input, 0).toList == List("b", "", ":and:f"))
  }

  test("quote") {
    assert(Pattern.quote("1.5-2.0?") == "1\\.5-2\\.0\\?") // TODO: taken from re2j, behaviour might differ jdk
  }

  test("characters") {
    pass("a", "a")

    // backslash
    pass("\\\\", "\\")

    // octal
    pass("\\01", "\u0001")
    pass("\\012", "\u000A") // 12 octal = A hex

    // hex
    pass("\\x10", "\u0010")

    pass("\\t", "\t")     // tab
    pass("\\n", "\n")     // newline
    pass("\\r", "\r")     // carriage-return
    pass("\\f", "\f")     // form-feed
    pass("\\a", "\u0007") // alert (bell)
  }

  ignore("(not supported) character classes") {
    pass("\\0100", "\u0040") // 100 octal = 40 hex
    pass("\\xFF", "\u00FF")
    pass("\\uBEEF", "\uBEEF")
    pass("\\e", "\u001B") // escape
    // control char \cx
  }

  test("character classes") {
    pass("[abc]", "a")
    fail("[^abc]", "c")
    pass("[a-zA-Z]", "T")
  }

  test("predefined character classes") {
    pass(".", "a")
    pass("\\d", "0")
    pass("\\D", "a")
    pass("\\s", " ")
    pass("\\S", "a")
    pass("\\w", "a")
    pass("\\W", "-")
  }

  test("POSIX character classes") {
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
    pass("\\p{Cntrl}", new String(Array[Byte](0x7F)))
  }

  test("Unicode classes") {
    // Categories
    pass("\\p{Lu}", "A")
    pass("\\p{Sc}", "$")

    // Blocks
    pass("\\p{InGreek}", "α")

    // Scripts
    pass("\\p{IsLatin}", "a")
    fail("\\p{IsLatin}", "α")
  }

  ignore("pending") {
    // The prefix In should only allow blokcs like Mongolian
    assertThrowsAnd[PatternSyntaxException](Pattern.compile("\\p{InLatin}"))(
      _.getMessage == "Unknown character block name {Latin} near index 10"
    )

    // Binary Properties
    pass("\\p{IsAlphabetic}", "a")
    fail("\\p{IsAlphabetic}", "-")
  }

  test("boundary matchers") {
    pass("^a", "a")
    pass("$", "")
    pass("foo\\b", "foo")
    fail("foo\\B", "foo")
    pass("\\AAbcdef", "Abcdef")
    pass("foo\\z", "foo")
  }

  ignore("(Not supported) boundary matchers") {
    // \G = at the end of the previous match
    val m1 = Pattern.compile("\\Gfoo").matcher("foofoo foo")
    assert(m1.find())
    assert(m1.find())
    assert(!m1.find())

    // \Z = end of input of the final terminator
    find("foo\\Z", "foo")
    find("foo\\Z", "foo\n")

    // \z = end of input
    find("foo\\z", "foo")
    find("foo\\z", "foo\n", pass = false)

    // \R Unicode linebreak
    pass("\\R", "\u000D\u000A")

    pending // 620
  }

  test("greedy quantifiers") {
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

  test("lazy quantifiers") {
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

  test("composite operators") {
    pass("abc", "abc")

    // x or y, prefer x
    pass("a|b", "a")
    pass("a|b", "b")
    fail("a|b", "c")
  }

  test("quotation") {
    pass("\\Qa|b|c\\E", "a|b|c")
  }

  test("named groups") {
    pass("(?<foo>a)", "a")
  }

  test("non-capturing groups") {
    pass("(?:a)", "a")
  }

  test("flags") {
    pass("(?i)iNsEnSiTiVe", "insensitive")
    pass("(?i:iNsEnSiTiVe)", "insensitive")
  }

  test("toString") {
    val in = "foo|bar"
    assert(Pattern.compile(in).toString == in)
  }

  ignore("(Not supported) character classes (union and intersection)") {
    pass("[a-d[m-p]]", "acn")
    pass("[[a-z] && [b-y] && [c-x]]", "g")
    pass("[a-z&&[def]]", "e")
    pass("[a-z&&[^aeiou]]", "c")
    fail("[a-z&&[^aeiou]]", "o")
    pass("[a-z&&[^m-p]]", "c")
    fail("[a-z&&[^m-p]]", "n")
    pending // 620
  }

  ignore(
    "(Not Supported) predefined character classes (horizontal and vertical)") {
    pass("\\h", " ")
    pass("\\H", "a")
    pass("\\v", "\n")
    pass("\\V", "a")
    pending // 620
  }

  ignore("(Not supported)java character function classes") {
    pass("\\p{javaLowerCase}", "a")
    pass("\\p{javaUpperCase}", "A")
    pass("\\p{javaWhitespace}", " ")
    pass("\\p{javaMirrored}", "{") // mirrored with }
    fail("\\p{javaMirrored}", "c")
    pending // 620
  }

  /*
    Google's RE2 does not support back references because they can be infinite
    https://github.com/google/re2/wiki/WhyRE2
    https://github.com/google/re2/blob/2017-03-01/doc/syntax.txt
   */
  ignore("(Not supported) back references") {
    pass("(a)\\1", "aa")
    pass("(?<foo>a)\\k<foo>", "aa")
    pending // 620
  }

  ignore("(Not supported) lookaheads") {
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

    pending // 620
  }

  ignore("(Not supported) possessive quantifiers") {
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

    pending // 620
  }

  test("multibyte characters") {
    find("こんにちは", "こんにちはみなさま")
  }

  test("character class consisting of multibyte characters") {
    pass(
      "^[\u0000-\u00a0\u1680\u2000-\u200a\u202f\u205f\u3000\u2028\u2029]$",
      "\u200a"
    )
  }

  test("group not containing multibyte characters") {
    val pat   = "abcdef(ghi)jkl"
    val input = "abcdefghijkl"
    val m     = Pattern.compile(pat).matcher(input)
    assert(m.matches())
    assert(m.group(0) == input)
    assert(m.group(1) == "ghi")
    assert(m.group() == input)
  }

  test("group containing multibyte characters") {
    val pat   = "abcあいう(えお)def"
    val input = "abcあいうえおdef"
    val m     = Pattern.compile(pat).matcher(input)
    assert(m.matches())
    assert(m.group(0) == input)
    assert(m.group(1) == "えお")
    assert(m.group() == input)
  }

  test("compiling a lot of patterns") {
    val pats = (0 until 200).map(i => Pattern.compile(i.toString))
    // pick a newer pattern (likely in cache).
    locally {
      val pat = pats(198)
      val m   = pat.matcher("198")
      assert(m.matches())
    }
    // pick an older pattern (likely out of cache).
    locally {
      val pat = pats(1)
      val m   = pat.matcher("1")
      assert(m.matches())
    }
  }

  test("syntax exceptions") {

    assertThrowsAnd[PatternSyntaxException](Pattern.compile("foo\\L"))(
      e => {
        e.getDescription == "Illegal/unsupported escape sequence" &&
        e.getIndex == 4 &&
        e.getPattern == "foo\\L" &&
        e.getMessage ==
          """|Illegal/unsupported escape sequence near index 4
	     |foo\L
	     |    ^""".stripMargin
      }
    )

    syntax("foo\\Lbar", "Illegal/unsupported escape sequence", 4)
    syntax("foo[bar", "Unclosed character class", 6)
    syntax("foo\\", "Trailing Backslash", 4)
    syntax("[a-0]", "Illegal character range", 3)
    syntax("*", "Dangling meta character '*'", 0)
    syntax("foo(bar(foo)baz", "Missing parenthesis", 15)
    syntax("foo(foo)bar)baz", "Missing parenthesis", 10)
  }

  private def syntax(pattern: String, description: String, index: Int): Unit = {
    assertThrowsAnd[PatternSyntaxException](Pattern.compile(pattern))(
      e => {
        e.getDescription == description &&
        e.getPattern == pattern
        e.getIndex == index
      }
    )
  }

  private def pass(pattern: String, input: String): Unit =
    matches(pattern, input, pass = true)

  private def fail(pattern: String, input: String): Unit =
    matches(pattern, input, pass = false)

  private def passAndFail(pattern: String,
                          passInput: String,
                          failInput: String): Unit = {
    pass(pattern, passInput)
    fail(pattern, failInput)
  }

  private def assertRegex(pass: Boolean,
                          ret: Boolean,
                          mid: String,
                          pattern: String,
                          input: String): Unit = {
    val ret0 =
      if (pass) ret
      else !ret

    assert(ret0, s""""$pattern" $mid "$input"""")
  }

  private def matches(pattern: String, input: String, pass: Boolean): Unit = {

    val ret = Pattern.matches(pattern, input)

    val mid =
      if (pass) "does not match"
      else "should not match"

    assertRegex(pass, ret, mid, pattern, input)
  }

  private def find(pattern: String,
                   input: String,
                   pass: Boolean = true): Unit = {
    val ret = Pattern.compile(pattern).matcher(input).find()

    val mid =
      if (pass) "does not match"
      else "should not match"

    assertRegex(pass, ret, mid, pattern, input)
  }
}
