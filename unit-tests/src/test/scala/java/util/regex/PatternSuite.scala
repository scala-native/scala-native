package java.util
package regex

import scala.collection.immutable.List
import scala.collection.JavaConverters._

import java.util.stream.{Stream => jStream}

object PatternSuite extends tests.Suite {

  test(s"compile(regex)") {
    val p1 = Pattern.compile("xyz")
  }

  test(s"compile(regex, flags) - invalid flag") {
    assertThrows[IllegalArgumentException] {
      val p1 = Pattern.compile(":", 0xA0000000)
    }
  }

  test("compile(regex, flags) - unsupported flags") {

    assertThrows[UnsupportedOperationException] {
      val p1 = Pattern.compile("m", Pattern.CANON_EQ)
    }

    assertThrows[UnsupportedOperationException] {
      val p1 = Pattern.compile("n", Pattern.COMMENTS)
    }

    assertThrows[UnsupportedOperationException] {
      val p1 = Pattern.compile("o", Pattern.UNICODE_CASE)
    }

    assertThrows[UnsupportedOperationException] {
      val p1 = Pattern.compile("p", Pattern.UNICODE_CHARACTER_CLASS)
    }

    assertThrows[UnsupportedOperationException] {
      val p1 = Pattern.compile("q", Pattern.UNIX_LINES)
    }
  }

  test("Exercise Matcher methods before using them.") {
    // Establish a baseline for correctness.
    // Many tests below assume that Matcher methods matcher() and find() are
    // available and correct.
    // Check that at least the simple case of using them with no
    // Pattern#compile flags works.  If that does not work, every test
    // using these methods is suspect.
    val needle   = "needle"
    val haystack = "haystack & needle"
    val m        = Pattern.compile(needle).matcher(haystack)
    assert(m.find(), s"should have found ${needle} in ${haystack}")
  }

  test("compile(regex, flags) - Pattern.CASE_INSENSITIVE") {
    val needle   = "hubble telescope"
    val haystack = "Hubble Telescope"
    val m = Pattern
      .compile(needle, Pattern.CASE_INSENSITIVE)
      .matcher(haystack)
    assert(m.find(), s"should have found '${needle}' in '${haystack}'")
  }

  test("compile(regex, flags) - Pattern.CASE_DOTALL") {
    val needle   = "Four score.*Units"
    val haystack = "Four score and seven years ago\nOur Parental Units"

    val m = Pattern.compile(needle).matcher(haystack)
    assert(!m.find(), s"should not have found '${needle}' in '${haystack}'")

    val m2 = Pattern.compile(needle, Pattern.DOTALL).matcher(haystack)
    assert(m2.find(), s"should have found '${needle}' in '${haystack}'")
  }

  test("compile(regex, flags) - Pattern.LITERAL") {
    val needle   = "(a)(b$)?(b)?"
    val haystack = "(a)(b$)?(b)?"
    val m = Pattern
      .compile(needle, Pattern.LITERAL)
      .matcher(haystack)
    assert(m.find(), s"should have found '${needle}' in '${haystack}'")
  }

  test("compile(regex, flags) - (CASE_INSENSITIVE | LITERAL)") {
    val needle   = "(a)(b$)?(b)?"
    val haystack = "(a)(B$)?(b)?"
    val m = Pattern
      .compile(needle, Pattern.CASE_INSENSITIVE | Pattern.LITERAL)
      .matcher(haystack)
    assert(m.find(), s"should have found '${needle}' in '${haystack}'")
  }

  test("asPredicate") {

    val needle   = "needle"
    val haystack = needle
    val p        = Pattern.compile(needle)

    val pred = p.asPredicate()

    assert(pred.test(haystack),
           s"should have found '${needle}' in '${haystack}'")

    // Let's get complicated to show that asPredicate() uses
    // full 'matches' not partial 'find'.
    // That seems to be the JVM behavior, if you can tease it out.

    // Tokens start with 'L', have at least one additional word
    // character and contain only word characters until the end.
    // '\Z' is currently unsupported in Scala Native.

    val needle2 = "\\AL\\w\\w+\\z"
    val p2      = Pattern.compile(needle2)
    val pred2   = p2.asPredicate()

    val shouldNotFind = Array("L9", "Life ", "LovePotion#9", "funny")

    assert(shouldNotFind.filter(pred2.test).length == 0,
           s"should not have found '${needle2}' in" +
             s" '${shouldNotFind.toString}'")

    val shouldFind = Array("Life", "Liberty", "Love")

    val expectedLength = shouldFind.length
    val resultLength   = shouldFind.filter(pred2.test).length

    assert(resultLength == expectedLength,
           s"number found: ${resultLength} != expected: ${expectedLength}")
  }

  test("split/split(n)") {
    val input = "boo:and:foo"

    val p1 = Pattern.compile(":")
    assert(p1.split(input).toList == List("boo", "and", "foo"), s"A1")
    assert(p1.split(input, 2).toList == List("boo", "and:foo"), s"A2")
    assert(p1.split(input, 5).toList == List("boo", "and", "foo"), s"A3")
    assert(p1.split(input, -2).toList == List("boo", "and", "foo"), s"A4")

    val p2 = Pattern.compile("o")
    assert(p2.split(input).toList == List("b", "", ":and:f"), s"A5")
    assert(p2.split(input, 5).toList == List("b", "", ":and:f", "", ""), s"A6")
    assert(p2.split(input, -2).toList == List("b", "", ":and:f", "", ""), s"A7")
    assert(p2.split(input, 0).toList == List("b", "", ":and:f"), s"A8")
  }

  test("splitAsStream") {
    val input = "boo:and:foo"

    val p1 = Pattern.compile(":")

    essaySplitAsStream(p1.splitAsStream(input),
                       Array("boo", "and", "foo"),
                       "A_1")

    val p2 = Pattern.compile("o")

    essaySplitAsStream(p2.splitAsStream(input), Array("b", "", ":and:f"), "A5")
  }

  test("quote") {
    assertEquals(Pattern.quote("1.5-2.0?"), "\\Q1.5-2.0?\\E")
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

  test("Unicode block") {
    pass("\\p{InGreek}", "α")
    pass("\\p{Greek}", "Ω")
    fail("\\p{InGreek}", "a")
    pass("\\P{InGreek}", "a") // not in block

    pass("\\p{InLatin}", "a")
    pass("\\p{Latin}", "a")
    fail("\\p{InLatin}", "α")
    pass("\\P{InLatin}", "α") // not in block
  }

  testFails("(not supported) character classes", 620) {
    pass("\\0100", "\u0040") // 100 octal = 40 hex
    pass("\\uBEEF", "\uBEEF")
    pass("\\e", "\u001B")  // escape
    pass("\\cZ", s"\\x1A") // Control-Z
  }

  test("character classes") {
    pass("[abc]", "a")
    fail("[^abc]", "c")
    pass("[a-zA-Z]", "T")
    pass("\\xFF", "\u00FF")
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

  test("Unicode classes") {
    pass("\\p{Lu}", "A")
    pass("\\p{Sc}", "$")

    fail("\\p{Lu}", "@") // should not be in Uppercase class
    pass("\\P{Lu}", "@") // but should be in negated class. Thanks, Aristotle!
  }

  testFails("(not supported) Unicode classes", 620) {
    // not supported: IsAlphabetic binary property.
    pass("\\p{IsAlphabetic}", "a")
    fail("\\p{IsAlphabetic}", "-")
  }

  test("Unicode script") {
    // "\u03b1" is Greek alpha character, "\u0061" is Latin lowercase 'a'
    pass("\\p{IsGreek}", "\u03b1") // behavior changes in Java 9
    fail("\\p{IsGreek}", "\u0061")
    pass("\\P{IsGreek}", "\u0061") // not in block

    pass("\\p{IsLatin}", "\u0061") // behavior changes in Java 9
    fail("\\p{IsLatin}", "\u03b1")
    pass("\\P{IsLatin}", "\u03b1") // not in block
  }

  test("boundary matchers") {
    pass("^a", "a")
    pass("$", "")
    pass("foo\\b", "foo")
    fail("foo\\B", "foo")
    pass("\\AAbcdef", "Abcdef")

    // \z = end of input
    pass("foo\\z", "foo")
    find("foo\\z", "foo\n", pass = false)
  }

  testFails("boundary matchers", 620) {
    // \G = at the end of the previous match
    val m1 = Pattern.compile("\\Gfoo").matcher("foofoo foo")
    assert(m1.find())
    assert(m1.find())
    assert(!m1.find())

    // \Z = end of input of the final terminator
    find("foo\\Z", "foo")
    find("foo\\Z", "foo\n")

    // \R Unicode linebreak
    pass("\\R", "\u000D\u000A")
  }

  testFails("boundary matchers - region", 0) {

    locally {
      val needle   = "^a"
      val haystack = "01234abcabc"

      val m = Pattern.compile(needle).matcher(haystack)

      val regionBegin  = 5
      val regionEnd    = 9
      val regionString = haystack.slice(regionBegin, regionEnd)

      m.region(5, 9)

      assert(m.find(), s"should have found ${needle} in ${regionString}")

      val foundPos    = m.start
      val expectedPos = 5

      assert(foundPos == expectedPos,
             s"found position: ${foundPos} != expected: ${expectedPos}")
    }

    locally {
      val needle   = "^a"
      val haystack = "01234abcabc"
      val m        = Pattern.compile(needle).matcher(haystack)

      m.region(4, 9)

      assert(!m.find(),
             s"should not have found ${needle} at " +
               s"position: ${m.start} in ${haystack}")
    }
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

  test("java named groups") {
    pass("(?<foo>a)", "a")
  }

  // Do not support re2 syntax in java.util.regex.
  testFails("(Not supported) re2 named groups", 0) { // Intended, No Issue
    pass("(?P<foo>a)", "a")
  }

  test("non-capturing groups") {
    pass("(?:a)", "a")
  }

  test("flags in regex") {
    pass("(?i)iNsEnSiTiVe", "insensitive")
    pass("(?i:iNsEnSiTiVe)", "insensitive")
  }

  test("toString") {
    val in = "foo|bar"
    assertEquals(Pattern.compile(in).toString, in)
  }

  testFails("(Not supported) character classes (union and intersection)", 620) {
    pass("[a-d[m-p]]", "acn")
    pass("[[a-z] && [b-y] && [c-x]]", "g")
    pass("[a-z&&[def]]", "e")
    pass("[a-z&&[^aeiou]]", "c")
    fail("[a-z&&[^aeiou]]", "o")
    pass("[a-z&&[^m-p]]", "c")
    fail("[a-z&&[^m-p]]", "n")
  }

  testFails(
    "(Not Supported) predefined character classes (horizontal and vertical)",
    620) {

    pass("\\h", " ")
    pass("\\H", "a")
    pass("\\v", "\n")
    pass("\\V", "a")
  }

  testFails("(not supported)java character function classes", 620) {
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
  testFails("(not supported) back references", 620) {
    pass("(a)\\1", "aa")
    pass("(?<foo>a)\\k<foo>", "aa")
  }

  testFails("(not supported) lookaheads", 620) {
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

  testFails("(not supported) possessive quantifiers", 620) {
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

    val m = Pattern.compile(pat).matcher(input)

    assert(m.matches(), s"A_1")
    assert(m.group(0) == input, s"A_2")
    assert(m.group(1) == "ghi", s"A_3")
    assert(m.group() == input, s"A_4")
  }

  test("group containing multibyte characters") {
    val pat   = "abcあいう(えお)def"
    val input = "abcあいうえおdef"
    val m     = Pattern.compile(pat).matcher(input)
    assert(m.matches())
    assertEquals(m.group(0), input)
    assertEquals(m.group(1), "えお")
    assertEquals(m.group(), input)
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

    assert(ret0)
  }

  private def matches(pattern: String, input: String, pass: Boolean): Unit = {

    val ret = Pattern.matches(pattern, input)

    val mid =
      if (pass) "does not matches"
      else "should not match"

    assertRegex(pass, ret, mid, pattern, input)
  }

  private def find(pattern: String,
                   input: String,
                   pass: Boolean = true): Unit = {
    val ret = Pattern.compile(pattern).matcher(input).find()

    val mid =
      if (pass) "does not matches"
      else "should not match"

    assertRegex(pass, ret, mid, pattern, input)
  }

  private def essaySplitAsStream(st: jStream[String],
                                 expected: Array[String],
                                 marker: String): Unit = {

    val result = st.iterator.asScala.toArray

    assert(result.size == expected.size,
           s"${marker} result.size: ${result.size} != ${expected.size}")

    for (i <- 0 until result.size) {
      assert(result(i) == expected(i),
             s"${marker} result(${i}): ${result(i)} != " +
               s"expected: ${expected(i)}")
    }
  }

}
