package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

import org.junit.Assert._
import org.junit.Test

class RE2CompileTest {

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
    // 2020-03-16 The next block is commented out because
    // Parser.scala for ScalaNative was modified to use JVM descriptions.

//    Array("*", "Bad repetition operator near index 0\n*\n^"),
//    Array("+", "Bad repetition operator near index 0\n+\n^"),
//    Array("?", "Bad repetition operator near index 0\n?\n^"),
//    Array("(abc", "Missing parenthesis near index 4\n(abc\n    ^"),
//    Array("abc)", "Unclosed character class near index 2\nabc)\n  ^"),
//    Array("[a-z",
//          "Illegal/unsupported character class near index 3\n[a-z\n   ^"),
    Array("[z-a]", "Illegal character range near index 3\n[z-a]\n   ^"),
    Array("abc\\", "Trailing Backslash near index 4\nabc\\\n    ^"),
    Array("a**", "Invalid nested repetition operator near index 0\n**\n^"),
    Array("a*+", "Invalid nested repetition operator near index 0\n*+\n^"),
    Array("\\x", "Illegal/unsupported escape sequence near index 1\n\\x\n ^"),
    Array("\\p", "Unknown character property name near index 2\n\\p\n  ^"),
    Array("\\p{", "Unknown character property name near index 3\n\\p{\n   ^")
  )

  @Test def compile(): Unit = {
    for (Array(input, expectedError) <- testData) {
      try {
        RE2.compile(input)
        if (expectedError != null)
          fail(
            "RE2.compile(" + input + ") was successful, expected " + expectedError
          )
      } catch {
        case e: PatternSyntaxException =>
          // Adapt error message on Windows to match Unix strings
          val errorMsg = e.getMessage().replaceAll(System.lineSeparator(), "\n")
          if (expectedError == null || errorMsg != expectedError)
            fail("compiling " + input + "; unexpected error: " + e.getMessage)
      }
    }
  }
}
