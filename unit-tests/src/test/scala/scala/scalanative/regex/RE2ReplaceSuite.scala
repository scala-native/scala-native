package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

import ScalaTestCompat.fail

object RE2ReplaceSuite extends tests.Suite {

  private val REPLACE_TESTS = Array( // Test empty input and/or replacement,
    // with pattern that matches the empty string.
    Array("", "", "", "", "false"),
    Array("", "x", "", "x", "false"),
    Array("", "", "abc", "abc", "false"),
    Array("", "x", "abc", "xaxbxcx", "false"), // with pattern that does not match the empty string.
    Array("b", "", "", "", "false"),
    Array("b", "x", "", "", "false"),
    Array("b", "", "abc", "ac", "false"),
    Array("b", "x", "abc", "axc", "false"),
    Array("y", "", "", "", "false"),
    Array("y", "x", "", "", "false"),
    Array("y", "", "abc", "abc", "false"),
    Array("y", "x", "abc", "abc", "false"), // Multibyte characters -- verify that we don't try to match in the middle
    // of a character.
    Array("[a-c]*", "x", "\u65e5", "x\u65e5x", "false"),
    Array("[^\u65e5]", "x", "abc\u65e5def", "xxx\u65e5xxx", "false"), // Start and end of a string.
    Array("^[a-c]*", "x", "abcdabc", "xdabc", "false"),
    Array("[a-c]*$", "x", "abcdabc", "abcdx", "false"),
    Array("^[a-c]*$", "x", "abcdabc", "abcdabc", "false"),
    Array("^[a-c]*", "x", "abc", "x", "false"),
    Array("[a-c]*$", "x", "abc", "x", "false"),
    Array("^[a-c]*$", "x", "abc", "x", "false"),
    Array("^[a-c]*", "x", "dabce", "xdabce", "false"),
    Array("[a-c]*$", "x", "dabce", "dabcex", "false"),
    Array("^[a-c]*$", "x", "dabce", "dabce", "false"),
    Array("^[a-c]*", "x", "", "x", "false"),
    Array("[a-c]*$", "x", "", "x", "false"),
    Array("^[a-c]*$", "x", "", "x", "false"),
    Array("^[a-c]+", "x", "abcdabc", "xdabc", "false"),
    Array("[a-c]+$", "x", "abcdabc", "abcdx", "false"),
    Array("^[a-c]+$", "x", "abcdabc", "abcdabc", "false"),
    Array("^[a-c]+", "x", "abc", "x", "false"),
    Array("[a-c]+$", "x", "abc", "x", "false"),
    Array("^[a-c]+$", "x", "abc", "x", "false"),
    Array("^[a-c]+", "x", "dabce", "dabce", "false"),
    Array("[a-c]+$", "x", "dabce", "dabce", "false"),
    Array("^[a-c]+$", "x", "dabce", "dabce", "false"),
    Array("^[a-c]+", "x", "", "", "false"),
    Array("[a-c]+$", "x", "", "", "false"),
    Array("^[a-c]+$", "x", "", "", "false"), // Other cases.
    Array("abc", "def", "abcdefg", "defdefg", "false"),
    Array("bc", "BC", "abcbcdcdedef", "aBCBCdcdedef", "false"),
    Array("abc", "", "abcdabc", "d", "false"),
    Array("x", "xXx", "xxxXxxx", "xXxxXxxXxXxXxxXxxXx", "false"),
    Array("abc", "d", "", "", "false"),
    Array("abc", "d", "abc", "d", "false"),
    Array(".+", "x", "abc", "x", "false"),
    Array("[a-c]*", "x", "def", "xdxexfx", "false"),
    Array("[a-c]+", "x", "abcbcdcdedef", "xdxdedef", "false"),
    Array("[a-c]*", "x", "abcbcdcdedef", "xdxdxexdxexfx", "false"),
    Array("", "", "", "", "true"),
    Array("", "x", "", "x", "true"),
    Array("", "", "abc", "abc", "true"),
    Array("", "x", "abc", "xabc", "true"),
    Array("b", "", "", "", "true"),
    Array("b", "x", "", "", "true"),
    Array("b", "", "abc", "ac", "true"),
    Array("b", "x", "abc", "axc", "true"),
    Array("y", "", "", "", "true"),
    Array("y", "x", "", "", "true"),
    Array("y", "", "abc", "abc", "true"),
    Array("y", "x", "abc", "abc", "true"),
    Array("[a-c]*", "x", "\u65e5", "x\u65e5", "true"),
    Array("[^\u65e5]", "x", "abc\u65e5def", "xbc\u65e5def", "true"),
    Array("^[a-c]*", "x", "abcdabc", "xdabc", "true"),
    Array("[a-c]*$", "x", "abcdabc", "abcdx", "true"),
    Array("^[a-c]*$", "x", "abcdabc", "abcdabc", "true"),
    Array("^[a-c]*", "x", "abc", "x", "true"),
    Array("[a-c]*$", "x", "abc", "x", "true"),
    Array("^[a-c]*$", "x", "abc", "x", "true"),
    Array("^[a-c]*", "x", "dabce", "xdabce", "true"),
    Array("[a-c]*$", "x", "dabce", "dabcex", "true"),
    Array("^[a-c]*$", "x", "dabce", "dabce", "true"),
    Array("^[a-c]*", "x", "", "x", "true"),
    Array("[a-c]*$", "x", "", "x", "true"),
    Array("^[a-c]*$", "x", "", "x", "true"),
    Array("^[a-c]+", "x", "abcdabc", "xdabc", "true"),
    Array("[a-c]+$", "x", "abcdabc", "abcdx", "true"),
    Array("^[a-c]+$", "x", "abcdabc", "abcdabc", "true"),
    Array("^[a-c]+", "x", "abc", "x", "true"),
    Array("[a-c]+$", "x", "abc", "x", "true"),
    Array("^[a-c]+$", "x", "abc", "x", "true"),
    Array("^[a-c]+", "x", "dabce", "dabce", "true"),
    Array("[a-c]+$", "x", "dabce", "dabce", "true"),
    Array("^[a-c]+$", "x", "dabce", "dabce", "true"),
    Array("^[a-c]+", "x", "", "", "true"),
    Array("[a-c]+$", "x", "", "", "true"),
    Array("^[a-c]+$", "x", "", "", "true"),
    Array("abc", "def", "abcdefg", "defdefg", "true"),
    Array("bc", "BC", "abcbcdcdedef", "aBCbcdcdedef", "true"),
    Array("abc", "", "abcdabc", "dabc", "true"),
    Array("x", "xXx", "xxxXxxx", "xXxxxXxxx", "true"),
    Array("abc", "d", "", "", "true"),
    Array("abc", "d", "abc", "d", "true"),
    Array(".+", "x", "abc", "x", "true"),
    Array("[a-c]*", "x", "def", "xdef", "true"),
    Array("[a-c]+", "x", "abcbcdcdedef", "xdcdedef", "true"),
    Array("[a-c]*", "x", "abcbcdcdedef", "xdcdedef", "true")
  )

  test("replaceTestHelper") {
    for (Array(pattern, replacement, source, expected, _replaceFirst) <- REPLACE_TESTS) {
      val replaceFirst = java.lang.Boolean.parseBoolean(_replaceFirst)
      var re: RE2      = null
      try re = RE2.compile(pattern)
      catch {
        case e: PatternSyntaxException =>
          fail(
            "Unexpected error compiling %s: %s".format(pattern, e.getMessage))
      }
      val actual =
        if (replaceFirst) re.replaceFirst(source, replacement)
        else re.replaceAll(source, replacement)
      if (!(actual == expected))
        fail(
          "%s.replaceAll(%s,%s) = %s; want %s"
            .format(pattern, source, replacement, actual, expected))
    }
  }
}
