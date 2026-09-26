package org.scalanative.testsuite.javalib.util.regex

import java.util._
import java.util.regex._
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform._

/* assumeFalse executingInJVM should either be fixed or moved to a Scala Native
 * re2 specific test
 */
class MatcherTestOnJDK9 {

  private def matcher(regex: String, text: String): Matcher =
    Pattern.compile(regex).matcher(text)

  @Test def appendReplacementAppendTail_StringBuilder(): Unit = {
    val buf = new jl.StringBuilder()

    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    while (m.find()) {
      m.appendReplacement(buf, "{" + m.group() + "}")
    }
    m.appendTail(buf)
    assertEquals(buf.toString, "_{a12z}_{a34z}_")
  }

  @Test def appendReplacementAppendTailGroupReplacementByIndex_StringBuilder()
      : Unit = {
    val buf = new jl.StringBuilder()
    val m = matcher("a(\\d)(\\d)z", "_a12z_a34z_")

    while (m.find()) {
      m.appendReplacement(buf, "{$0}")
    }
    m.appendTail(buf)
    assertEquals(buf.toString, "_{a12z}_{a34z}_")
  }

  @Test def appendReplacementAppendTailGroupReplacementByName_StringBuilder()
      : Unit = {
    val buf = new jl.StringBuilder()
    val m = matcher(
      "from (?<S>.*) to (?<D>.*)", // java syntax
      "from Montreal, Canada to Lausanne, Switzerland"
    )

    while (m.find()) {
      m.appendReplacement(buf, "such ${S}, wow ${D}")
    }
    m.appendTail(buf)
    assertEquals(
      buf.toString,
      "such Montreal, Canada, wow Lausanne, Switzerland"
    )
  }

  @Test def usePatternAppendPositionUnchanged_StringBuilder(): Unit = {

    val oldNeedle = "for "
    val newNeedle = "man"

    val original = "That's one small step for man,"
    val expected = "That's one small step for [a] man,"

    val m = matcher(oldNeedle, original)

    assertTrue(s"should have found '${oldNeedle}' in '${original}'", m.find())

    val found = m.group

    val sb = new jl.StringBuilder()

    m.usePattern(Pattern.compile(newNeedle))

    val result = m
      .appendReplacement(sb, s"${found}[a] ")
      .appendTail(sb)
      .toString

    assertTrue(
      s"append position changed; result: ${result} != " +
        s"expected: ${expected}'",
      result == expected
    )
  }
}
