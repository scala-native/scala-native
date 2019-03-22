package scala.re2s

object RE2CompileSuite extends tests.Suite {

  // LeeT FIX ME -- Rough & rude
  private def fail(msg: String) = assert(false, msg)

  // A list of regexp and expected error when calling RE2.compile. null implies that compile should
  // succeed.
  def testData: Array[Array[String]] = Array[Array[String]](
    Array("", null),
    Array(".", null),
    Array("^.$", null),
    Array("a", null),
    Array("a*", null),
    Array("a+", null),
    Array("a?", null),
    Array("a|b", null),
    Array("a*|b*", null),
    Array("(a*|b)(c*|d)", null),
    Array("[a-z]", null),
    Array("[a-abc-c\\-\\]\\[]", null),
    Array("[a-z]+", null),
    Array("[abc]", null),
    Array("[^1234]", null),
    Array("[^\n]", null),
    Array("\\!\\\\", null),
    Array("abc]", null), // Matches the closing bracket literally.
    Array("a??", null),
    Array("*", "missing argument to repetition operator near index 0\n*\n^"),
    Array("+", "missing argument to repetition operator near index 0\n+\n^"),
    Array("?", "missing argument to repetition operator near index 0\n?\n^"),
    Array("(abc", "Unclosed group near index 4\n(abc\n    ^"),
    Array("abc)", "Unmatched closing ')' near index 2\nabc)\n  ^"),
    Array("Unclosed character class near index 4\nx[a-z\n    ^"),
    Array("[z-a]", "Illegal character range near index 3\n[z-a]\n   ^"),
    Array("abc\\", "Unexpected internal error near index 4\nabc\\\n    ^"),
    Array("a**", "invalid nested repetition operator near index 0\n**\n^"),
    Array("a*+", "invalid nested repetition operator near index 0\n*+\n^"),
    Array("\\x", "Illegal/unsupported escape sequence near index 1\n\\x\n ^")
  )

  test("Compile") {
    for (Array(input, expectedError) <- testData) {
      try {
        RE2.compile(input)
        if (expectedError != null)
          fail(
            "RE2.compile(" + input + ") was successful, expected " + expectedError)
      } catch {
        case e: PatternSyntaxException =>
          if (expectedError == null || !(e.getMessage == expectedError))
            fail("compiling " + input + "; unexpected error: " + e.getMessage)
      }
    }
  }
}
