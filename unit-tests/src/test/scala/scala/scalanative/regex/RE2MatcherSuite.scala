package scala.scalanative
package regex

import ApiTestUtils._

import ScalaTestCompat._

object RE2MatcherSuite extends tests.Suite {

  test("LookingAt") {
    ApiTestUtils.verifyLookingAt("abcdef", "abc", true)
    ApiTestUtils.verifyLookingAt("ab", "abc", false)
  }

  test("Matches") {
    ApiTestUtils.testMatcherMatches("ab+c", "abbbc", "cbbba")
    ApiTestUtils.testMatcherMatches("ab.*c", "abxyzc", "ab\nxyzc")
    ApiTestUtils.testMatcherMatches("^ab.*c$", "abc", "xyz\nabc\ndef")
    ApiTestUtils.testMatcherMatches("ab+c", "abbbc", "abbcabc")
  }

  test("ReplaceAll") {
    ApiTestUtils.testReplaceAll(
      "What the Frog's Eye Tells the Frog's Brain",
      "Frog",
      "Lizard",
      "What the Lizard's Eye Tells the Lizard's Brain")
    ApiTestUtils.testReplaceAll(
      "What the Frog's Eye Tells the Frog's Brain",
      "F(rog)",
      "\\$Liza\\rd$1",
      "What the $Lizardrog's Eye Tells the $Lizardrog's Brain")
    ApiTestUtils.testReplaceAll("abcdefghijklmnopqrstuvwxyz123",
                                "(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)",
                                "$10$20",
                                "jb0wo0123")
    ApiTestUtils.testReplaceAll("\u00e1\u0062\u00e7\u2655",
                                "(.)",
                                "<$1>",
                                "<\u00e1><\u0062><\u00e7><\u2655>")
    ApiTestUtils.testReplaceAll("\u00e1\u0062\u00e7\u2655",
                                "[\u00e0-\u00e9]",
                                "<$0>",
                                "<\u00e1>\u0062<\u00e7>\u2655")
    ApiTestUtils.testReplaceAll("hello world",
                                "z*",
                                "x",
                                "xhxexlxlxox xwxoxrxlxdx")
    // test replaceAll with alternation
    ApiTestUtils.testReplaceAll("123:foo", "(?:\\w+|\\d+:foo)", "x", "x:x")
    ApiTestUtils.testReplaceAll("123:foo", "(?:\\d+:foo|\\w+)", "x", "x")
    ApiTestUtils.testReplaceAll("aab", "a*", "<$0>", "<aa><>b<>")
    ApiTestUtils.testReplaceAll("aab", "a*?", "<$0>", "<>a<>a<>b<>")
  }

  test("ReplaceFirst") {
    ApiTestUtils.testReplaceFirst(
      "What the Frog's Eye Tells the Frog's Brain",
      "Frog",
      "Lizard",
      "What the Lizard's Eye Tells the Frog's Brain")
    ApiTestUtils.testReplaceFirst(
      "What the Frog's Eye Tells the Frog's Brain",
      "F(rog)",
      "\\$Liza\\rd$1",
      "What the $Lizardrog's Eye Tells the Frog's Brain")
    ApiTestUtils.testReplaceFirst("abcdefghijklmnopqrstuvwxyz123",
                                  "(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)",
                                  "$10$20",
                                  "jb0nopqrstuvwxyz123")
    ApiTestUtils.testReplaceFirst("\u00e1\u0062\u00e7\u2655",
                                  "(.)",
                                  "<$1>",
                                  "<\u00e1>\u0062\u00e7\u2655")
    ApiTestUtils.testReplaceFirst("\u00e1\u0062\u00e7\u2655",
                                  "[\u00e0-\u00e9]",
                                  "<$0>",
                                  "<\u00e1>\u0062\u00e7\u2655")
    ApiTestUtils.testReplaceFirst("hello world", "z*", "x", "xhello world")
    ApiTestUtils.testReplaceFirst("aab", "a*", "<$0>", "<aa>b")
    ApiTestUtils.testReplaceFirst("aab", "a*?", "<$0>", "<>aab")
  }

  test("GroupCount") {
    ApiTestUtils.testGroupCount("(a)(b(c))d?(e)", 4)
  }

  test("Group") {
    ApiTestUtils.testGroup("xabdez",
                           "(a)(b(c)?)d?(e)",
                           Array[String]("abde", "a", "b", null, "e"))
    ApiTestUtils.testGroup("abc",
                           "(a)(b$)?(b)?",
                           Array[String]("ab", "a", null, "b"))
    ApiTestUtils.testGroup("abc", "(^b)?(b)?c", Array[String]("bc", null, "b"))
    ApiTestUtils.testGroup(" a b", "\\b(.).\\b", Array[String]("a ", "a"))

    // Not allowed to use UTF-8 except in comments, per Java style guide.
    // ("αβξδεφγ", "(.)(..)(...)", new String[] {"αβξδεφ", "α", "βξ", "δεφ"});
    ApiTestUtils.testGroup(
      "\u03b1\u03b2\u03be\u03b4\u03b5\u03c6\u03b3",
      "(.)(..)(...)",
      Array[String]("\u03b1\u03b2\u03be\u03b4\u03b5\u03c6",
                    "\u03b1",
                    "\u03b2\u03be",
                    "\u03b4\u03b5\u03c6")
    )
  }

  test("Find") {
    ApiTestUtils.testFind("abcdefgh", ".*[aeiou]", 0, "abcde")
    ApiTestUtils.testFind("abcdefgh", ".*[aeiou]", 1, "bcde")
    ApiTestUtils.testFind("abcdefgh", ".*[aeiou]", 2, "cde")
    ApiTestUtils.testFind("abcdefgh", ".*[aeiou]", 3, "de")
    ApiTestUtils.testFind("abcdefgh", ".*[aeiou]", 4, "e")
    ApiTestUtils.testFindNoMatch("abcdefgh", ".*[aeiou]", 5)
    ApiTestUtils.testFindNoMatch("abcdefgh", ".*[aeiou]", 6)
    ApiTestUtils.testFindNoMatch("abcdefgh", ".*[aeiou]", 7)
  }

  test("InvalidFind") {
    try {
      ApiTestUtils.testFind("abcdef", ".*", 10, "xxx")
      fail()
    } catch {
      case e: IndexOutOfBoundsException =>
      /* ok */
    }
  }

  test("InvalidReplacement") {
    try {
      ApiTestUtils.testReplaceFirst("abc", "abc", "$4", "xxx")
      fail()
    } catch {
      case e: IndexOutOfBoundsException =>
        assert(true)
    }
  }

  test("InvalidGroupNoMatch") {
    try {
      ApiTestUtils.testInvalidGroup("abc", "xxx", 0)
      fail()
    } catch {
      case e: IllegalStateException =>
        // Linter complains on empty catch block.
        assert(true)
    }
  }

  ignore("InvalidGroupOutOfRange") { // TODO: fails because of incorrect exception
    try {
      ApiTestUtils.testInvalidGroup("abc", "abc", 1)
      fail()
    } catch {
      case e: IndexOutOfBoundsException => ()
    }
  }

  /**
   * Test the NullPointerException is thrown on null input.
   */
  test("ThrowsOnNullInputReset") { // null in constructor.
    try {
      new Matcher(Pattern.compile("pattern"), null.asInstanceOf[String])
      fail()
    } catch {
      case n: NullPointerException =>
        assert(true)
    }
  }

  test("ThrowsOnNullInputCtor") {
    try {
      new Matcher(null, "input")
      fail()
    } catch {
      case n: NullPointerException =>
        assert(true)
    }
  }

  /**
   * Test that IllegalStateException is thrown if start/end are called
   * before calling find
   */
  test("StartEndBeforeFind") {
    try {
      val m = Pattern.compile("a").matcher("abaca")
      m.start
      fail()
    } catch {
      case ise: IllegalStateException =>
        assert(true)
    }
  }

  /**
   * Test for b/6891357. Basically matches should behave like find when
   * it comes to updating the information of the match.
   */
  test("MatchesUpdatesMatchInformation") {
    val m = Pattern.compile("a+").matcher("aaa")
    if (m.matches) assert("aaa" == m.group(0))
  }

  /**
   * Test for b/6891133. Test matches in case of alternation.
   */
  test("AlternationMatches") {
    val s = "123:foo"
    assert(Pattern.compile("(?:\\w+|\\d+:foo)").matcher(s).matches)
    assert(Pattern.compile("(?:\\d+:foo|\\w+)").matcher(s).matches)
  }

  private def helperTestMatchEndUTF16(string: String,
                                      num: Int,
                                      end: Int): Unit = {
    val pattern = "[" + string + "]"
    val re = new RE2(pattern) {
      override def match_(input: CharSequence,
                          start: Int,
                          e: Int,
                          anchor: Int,
                          group: Array[Int],
                          ngroup: Int): Boolean = {
        assert(end == e)
        super.match_(input, start, e, anchor, group, ngroup)
      }
    }
    val pat   = new Pattern(pattern, 0, re)
    val m     = pat.matcher(string)
    var found = 0
    while (m.find) found += 1
    assert(
      num == found,
      "Matches Expected " + num + " but found " + found + ", for input " + string)
  }

  /**
   * Test for variable length encoding, test whether RE2's match function gets
   * the required parameter based on UTF16 codes and not chars and Runes.
   */
  test("MatchEndUTF16") { // Latin alphabetic chars such as these 5 lower-case, acute vowels have multi-byte UTF-8
    // encodings but fit in a single UTF-16 code, so the final match is at UTF16 offset 5.
    val vowels = "\u0095\u009b\u009f\u00a3\u00a8"
    helperTestMatchEndUTF16(vowels, 5, 5)
    // But surrogates are encoded as two UTF16 codes, so we should expect match
    // to get 6 rather than 3.
    val utf16 = new java.lang.StringBuilder()
      .appendCodePoint(0x10000)
      .appendCodePoint(0x10001)
      .appendCodePoint(0x10002)
      .toString
    assert(utf16 == "\uD800\uDC00\uD800\uDC01\uD800\uDC02")
    helperTestMatchEndUTF16(utf16, 3, 6)
  }

  test("AppendTail_StringBuffer") {
    val p  = Pattern.compile("cat")
    val m  = p.matcher("one cat two cats in the yard")
    val sb = new StringBuffer
    while (m.find) m.appendReplacement(sb, "dog")
    m.appendTail(sb)
    m.appendTail(sb)
    assert("one dog two dogs in the yards in the yard" == sb.toString)
  }

  test("AppendTail_StringBuilder") {
    val p  = Pattern.compile("cat")
    val m  = p.matcher("one cat two cats in the yard")
    val sb = new StringBuffer()
    while (m.find) m.appendReplacement(sb, "dog")
    m.appendTail(sb)
    m.appendTail(sb)
    assert("one dog two dogs in the yards in the yard" == sb.toString)
  }

  test("ResetOnFindInt_StringBuffer") {
    var buffer  = new StringBuffer
    val matcher = Pattern.compile("a").matcher("zza")
    assert(matcher.find)
    buffer = new StringBuffer
    matcher.appendReplacement(buffer, "foo")
    assert("zzfoo" == buffer.toString, "1st time")
    assert(matcher.find(0))
    buffer = new StringBuffer
    matcher.appendReplacement(buffer, "foo")
    assert("zzfoo" == buffer.toString, "2nd time")
  }

  test("ResetOnFindInt_StringBuilder") {
    var buffer  = new StringBuffer
    val matcher = Pattern.compile("a").matcher("zza")
    assert(matcher.find)
    buffer = new StringBuffer
    matcher.appendReplacement(buffer, "foo")
    assert("zzfoo" == buffer.toString, "1st time")
    assert(matcher.find(0))
    buffer = new StringBuffer
    matcher.appendReplacement(buffer, "foo")
    assert("zzfoo" == buffer.toString, "2nd time")
  }

  test("EmptyReplacementGroups_StringBuffer") {
    var buffer  = new StringBuffer
    var matcher = Pattern.compile("(a)(b$)?(b)?").matcher("abc")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1-$2-$3")
    assert("a--b" == buffer.toString)
    matcher.appendTail(buffer)
    assert("a--bc" == buffer.toString)
    buffer = new StringBuffer
    matcher = Pattern.compile("(a)(b$)?(b)?").matcher("ab")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1-$2-$3")
    matcher.appendTail(buffer)
    assert("a-b-" == buffer.toString)
    buffer = new StringBuffer
    matcher = Pattern.compile("(^b)?(b)?c").matcher("abc")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1-$2")
    matcher.appendTail(buffer)
    assert("a-b" == buffer.toString)
    buffer = new StringBuffer
    matcher = Pattern.compile("^(.)[^-]+(-.)?(.*)").matcher("Name")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1$2")
    matcher.appendTail(buffer)
    assert("N" == buffer.toString)
  }

  test("EmptyReplacementGroups_StringBuilder") {
    var buffer  = new StringBuffer
    var matcher = Pattern.compile("(a)(b$)?(b)?").matcher("abc")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1-$2-$3")
    assert("a--b" == buffer.toString)
    matcher.appendTail(buffer)
    assert("a--bc" == buffer.toString)
    buffer = new StringBuffer
    matcher = Pattern.compile("(a)(b$)?(b)?").matcher("ab")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1-$2-$3")
    matcher.appendTail(buffer)
    assert("a-b-" == buffer.toString)
    buffer = new StringBuffer
    matcher = Pattern.compile("(^b)?(b)?c").matcher("abc")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1-$2")
    matcher.appendTail(buffer)
    assert("a-b" == buffer.toString)
    buffer = new StringBuffer
    matcher = Pattern.compile("^(.)[^-]+(-.)?(.*)").matcher("Name")
    assert(matcher.find)
    matcher.appendReplacement(buffer, "$1$2")
    matcher.appendTail(buffer)
    assert("N" == buffer.toString)
  }

  // This example is documented in the com.google.re2j package.html.
  test("DocumentedExample") {
    val p = Pattern.compile("b(an)*(.)")
    val m = p.matcher("by, band, banana")
    assert(m.lookingAt)
    m.reset
    assert(m.find)
    assert("by" == m.group(0))
    assert(null == m.group(1))
    assert("y" == m.group(2))
    assert(m.find)
    assert("band" == m.group(0))
    assert("an" == m.group(1))
    assert("d" == m.group(2))
    assert(m.find)
    assert("banana" == m.group(0))
    assert("an" == m.group(1))
    assert("a" == m.group(2))
    assert(!m.find)
  }
}
