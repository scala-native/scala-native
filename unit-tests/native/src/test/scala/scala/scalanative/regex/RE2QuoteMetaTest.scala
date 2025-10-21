package scala.scalanative
package regex

import java.util.regex.PatternSyntaxException

import org.junit.Assert._
import org.junit.Test

class RE2QuoteMetaTest {

  // (pattern, output, literal, isLiteral)
  private val META_TESTS = Array(
    Array("", "", "", "true"),
    Array("foo", "foo", "foo", "true"),
    // has meta but no operator:
    Array("foo\\.\\$", "foo\\\\\\.\\\\\\$", "foo.$", "true"),
    // has escaped operators and real operators:
    Array("foo.\\$", "foo\\.\\\\\\$", "foo", "false"),
    Array(
      "!@#$%^&*()_+-=[{]}\\|,<.>/?~",
      "!@#\\$%\\^&\\*\\(\\)_\\+-=\\[\\{\\]\\}\\\\\\|,<\\.>/\\?~",
      "!@#",
      "false"
    )
  )

  @Test def quoteMeta(): Unit = {
    // Verify that quoteMeta returns the expected string.
    for (Array(pattern, output, _, _) <- META_TESTS) {
      val quoted = RE2.quoteMeta(pattern)
      assertTrue(
        "RE2.quoteMeta(\"%s\") = \"%s\"; want \"%s\""
          .format(pattern, quoted, output),
        quoted == output
      )

      // Verify that the quoted string is in fact treated as expected
      // by compile -- i.e. that it matches the original, unquoted string.
      if (!pattern.isEmpty) {
        var re: RE2 = null
        try re = RE2.compile(quoted)
        catch {
          case e: PatternSyntaxException =>
            fail(
              "Unexpected error compiling quoteMeta(\"%s\"): %s"
                .format(pattern, e.getMessage)
            )
        }
        val src = "abc" + pattern + "def"
        val repl = "xyz"
        val replaced = re.replaceAll(src, repl)
        val expected = "abcxyzdef"
        assertTrue(
          "quoteMeta(`%s`).replace(`%s`,`%s`) = `%s`; want `%s`"
            .format(pattern, src, repl, replaced, expected),
          replaced == expected
        )
      }
    }
  }

  @Test def literalPrefix(): Unit = {
    // Literal method needs to scan the pattern.
    for (Array(pattern, output, literal, _isLiteral) <- META_TESTS) {
      val isLiteral = java.lang.Boolean.parseBoolean(_isLiteral)
      val re = RE2.compile(pattern)
      val msg = "literalPrefix(\"%s\") = \"%s\"; want \"%s\""
      assertFalse(
        msg.format(pattern, re.prefixComplete, isLiteral),
        re.prefixComplete != isLiteral
      )
      assertTrue(msg.format(pattern, re.prefix, literal), re.prefix == literal)
    }
  }
}
