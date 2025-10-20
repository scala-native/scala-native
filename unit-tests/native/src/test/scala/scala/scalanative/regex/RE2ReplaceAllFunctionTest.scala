package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

import org.junit.Test
import org.junit.Assert.*

class RE2ReplaceAllFunctionTest {

  def REPLACE_XSY = new RE2.ReplaceFunc() {
    override def replace(s: String): String = "x" + s + "y"
    override def toString = "REPLACE_XSY"
  }

  // Each row is (String pattern, input, output, ReplaceFunc replacement).
  // Conceptually the replacement func is a table column---but for now
  // it's always REPLACE_XSY.
  private val REPLACE_FUNC_TESTS = Array(
    Array("[a-c]", "defabcdef", "defxayxbyxcydef"),
    Array("[a-c]+", "defabcdef", "defxabcydef"),
    Array("[a-c]*", "defabcdef", "xydxyexyfxabcydxyexyfxy")
  )

  @Test def replaceAllFunc(): Unit = {
    for (Array(pattern, input, expected) <- REPLACE_FUNC_TESTS) {
      var re: RE2 = null
      try re = RE2.compile(pattern)
      catch {
        case e: PatternSyntaxException =>
          fail(
            "Unexpected error compiling %s: %s".format(pattern, e.getMessage)
          )
      }

      val actual = re.replaceAllFunc(input, input.length)(REPLACE_XSY.replace)

      assertTrue(
        "%s.replaceAllFunc(%s,%s) = %s; want %s"
          .format(pattern, input, REPLACE_XSY, actual, expected),
        actual == expected
      )
    }
  }
}
