package scala.scalanative
package regex

import org.junit.Test
import org.junit.Assert.*

object RegexHashcodeEqualsSuite {

  @Test def equalsHashcode: Unit = {

    case class TestPoint(
        first: String,
        second: String,
        expectEqual: Boolean,
        mode: Int
    )

    val testPoints = Seq(
      TestPoint("abc", "abc", true, RE2.POSIX),
      TestPoint("abc", "def", false, RE2.POSIX),
      TestPoint("(abc)", "(a)(b)(c)", false, RE2.POSIX),
      TestPoint("a|$", "a|$", true, RE2.POSIX),
      TestPoint("abc|def", "def|abc", false, RE2.POSIX),
      TestPoint("a?", "b?", false, RE2.POSIX),
      TestPoint("a?", "a?", true, RE2.POSIX),
      TestPoint("a{1,3}", "a{1,3}", true, RE2.POSIX),
      TestPoint("a{2,3}", "a{1,3}", false, RE2.POSIX),
      TestPoint("^((?P<foo>what)a)$", "^((?P<foo>what)a)$", true, RE2.PERL),
      TestPoint("^((?<foo>what)a)$", "^((?<foo>what)a)$", true, RE2.PERL),
      TestPoint("^((?P<foo>what)a)$", "^((?P<bar>what)a)$", false, RE2.PERL),
      TestPoint("^((?<foo>what)a)$", "^((?<bar>what)a)$", false, RE2.PERL)
    )

    for (TestPoint(s1, s2, expectEqual, mode) <- testPoints) {
      val r1 = Parser.parse(s1, mode)
      val r2 = Parser.parse(s2, mode)

      if (!expectEqual) {
        assert(
          !r1.equals(r2),
          s"regexes for '${s1}' and '${s2}' should not be equal()"
        )

        assert(!(r1 == r2), s"regexes for '${s1}' and '${s2}' should not be ==")

      } else {

        assert(
          r1.equals(r2),
          s"regexes for '${s1}' and '${s2}' should be equal()"
        )

        assert(r1 == r2, s"regexes for '${s1}' and '${s2}' should be ==")

        val r1Hash = r1.hashCode
        val r2Hash = r2.hashCode

        assert(
          r1Hash == r2Hash,
          s"hashcode for '${s1}': ${r1Hash} != '${s2}': ${r2Hash}"
        )
      }
    }
  }

}
