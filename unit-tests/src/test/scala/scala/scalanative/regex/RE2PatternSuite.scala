package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

object RE2PatternSuite extends tests.Suite {
  test("compile") {
    val p = Pattern.compile("abc")
    assert("abc" == p.pattern)
    assert(0 == p.flags)
  }

  test("toString") {
    val p = Pattern.compile("abc")
    assert("abc" == p.toString)
  }

  test("CompileFlags") {
    val p = Pattern.compile("abc", 5)
    assert("abc" == p.pattern)
    assert(5 == p.flags)
  }

  test("SyntaxError") {
    var caught = false
    try Pattern.compile("abc(")
    catch {
      case e: PatternSyntaxException =>
        assert(4 == e.getIndex)
        assert("" != e.getDescription)
        assert("" != e.getMessage)
        assert("abc(" == e.getPattern)
        caught = true
    }
    assert(caught)
  }

  test("MatchesNoFlags") {
    ApiTestUtils.testMatches("ab+c", "abbbc", "cbbba")
    ApiTestUtils.testMatches("ab.*c", "abxyzc", "ab\nxyzc")
    ApiTestUtils.testMatches("^ab.*c$", "abc", "xyz\nabc\ndef")
  }

  test("MatchesWithFlags") {
    ApiTestUtils.testMatchesRE2("ab+c", 0, "abbbc", "cbba")
    ApiTestUtils.testMatchesRE2("ab+c",
                                Pattern.CASE_INSENSITIVE,
                                "abBBc",
                                "cbbba")
    ApiTestUtils.testMatchesRE2("ab.*c", 0, "abxyzc", "ab\nxyzc")
    ApiTestUtils.testMatchesRE2("ab.*c", Pattern.DOTALL, "ab\nxyzc", "aB\nxyzC")
    ApiTestUtils.testMatchesRE2("ab.*c",
                                Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
                                "aB\nxyzC",
                                "z")
    ApiTestUtils.testMatchesRE2("^ab.*c$", 0, "abc", "xyz\nabc\ndef")
    ApiTestUtils.testMatchesRE2("^ab.*c$",
                                Pattern.MULTILINE,
                                "abc",
                                "xyz\nabc\ndef")
    ApiTestUtils.testMatchesRE2("^ab.*c$", Pattern.MULTILINE, "abc", "")
    ApiTestUtils.testMatchesRE2("^ab.*c$",
                                Pattern.DOTALL | Pattern.MULTILINE,
                                "ab\nc",
                                "AB\nc")
    ApiTestUtils.testMatchesRE2(
      "^ab.*c$",
      Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
      "AB\nc",
      "z")
  }

  private def testFind(regexp: String,
                       flag: Int,
                       `match`: String,
                       nonMatch: String): Unit = {
    assert(Pattern.compile(regexp, flag).matcher(`match`).find)
    assert(!Pattern.compile(regexp, flag).matcher(nonMatch).find)
  }

  test("Find") {
    testFind("ab+c", 0, "xxabbbc", "cbbba")
    testFind("ab+c", Pattern.CASE_INSENSITIVE, "abBBc", "cbbba")
    testFind("ab.*c", 0, "xxabxyzc", "ab\nxyzc")
    testFind("ab.*c", Pattern.DOTALL, "ab\nxyzc", "aB\nxyzC")
    testFind("ab.*c",
             Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
             "xaB\nxyzCz",
             "z")
    testFind("^ab.*c$", 0, "abc", "xyz\nabc\ndef")
    testFind("^ab.*c$", Pattern.MULTILINE, "xyz\nabc\ndef", "xyz\nab\nc\ndef")
    testFind("^ab.*c$",
             Pattern.DOTALL | Pattern.MULTILINE,
             "xyz\nab\nc\ndef",
             "xyz\nAB\nc\ndef")
    testFind("^ab.*c$",
             Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE,
             "xyz\nAB\nc\ndef",
             "z")
  }

  test("Split") {
    ApiTestUtils.testSplit("/", "abcde", Array[String]("abcde"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           Array[String]("a", "b", "cc", "", "d", "e"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           3,
                           Array[String]("a", "b", "cc//d/e//"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           4,
                           Array[String]("a", "b", "cc", "/d/e//"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           5,
                           Array[String]("a", "b", "cc", "", "d/e//"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           6,
                           Array[String]("a", "b", "cc", "", "d", "e//"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           7,
                           Array[String]("a", "b", "cc", "", "d", "e", "/"))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           8,
                           Array[String]("a", "b", "cc", "", "d", "e", "", ""))
    ApiTestUtils.testSplit("/",
                           "a/b/cc//d/e//",
                           9,
                           Array[String]("a", "b", "cc", "", "d", "e", "", ""))
    // The tests below are listed at
    // http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html#split(java.lang.CharSequence, int)
    val s       = "boo:and:foo"
    val regexp1 = ":"
    val regexp2 = "o"
    ApiTestUtils.testSplit(regexp1, s, 2, Array[String]("boo", "and:foo"))
    ApiTestUtils.testSplit(regexp1, s, 5, Array[String]("boo", "and", "foo"))
    ApiTestUtils.testSplit(regexp1, s, -2, Array[String]("boo", "and", "foo"))
    ApiTestUtils.testSplit(regexp2,
                           s,
                           5,
                           Array[String]("b", "", ":and:f", "", ""))
    ApiTestUtils.testSplit(regexp2,
                           s,
                           -2,
                           Array[String]("b", "", ":and:f", "", ""))
    ApiTestUtils.testSplit(regexp2, s, 0, Array[String]("b", "", ":and:f"))
    ApiTestUtils.testSplit(regexp2, s, Array[String]("b", "", ":and:f"))
  }

  test("GroupCount") { // It is a simple delegation, but still test it.
    ApiTestUtils.testGroupCount("(.*)ab(.*)a", 2)
    ApiTestUtils.testGroupCount("(.*)(ab)(.*)a", 3)
    ApiTestUtils.testGroupCount("(.*)((a)b)(.*)a", 4)
    ApiTestUtils.testGroupCount("(.*)(\\(ab)(.*)a", 3)
    ApiTestUtils.testGroupCount("(.*)(\\(a\\)b)(.*)a", 3)
  }

  test("Quote") {
    ApiTestUtils.testMatchesRE2(Pattern.quote("ab+c"), 0, "ab+c", "abc")
  }

}
