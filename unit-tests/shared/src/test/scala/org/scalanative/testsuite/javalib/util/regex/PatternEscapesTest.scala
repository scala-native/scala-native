package org.scalanative.testsuite.javalib.util.regex

import java.util.regex._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class PatternEscapesTest {

  private def matches(pattern: String, input: String): Unit =
    assertTrue(
      s"'$pattern' should match '$input'",
      Pattern.matches(pattern, input)
    )

  private def notMatches(pattern: String, input: String): Unit =
    assertFalse(
      s"'$pattern' should not match '$input'",
      Pattern.matches(pattern, input)
    )

  private def invalid(pattern: String): Unit =
    assertThrows(classOf[PatternSyntaxException], Pattern.compile(pattern))

  private def matchesInAndOutOfClass(escape: String, input: String): Unit = {
    matches(escape, input)
    matches(s"[$escape]", input)
    matches(s"[^$escape]", "~")
    notMatches(s"[^$escape]", input)
  }

  @Test def unicodeEscape(): Unit = {
    matchesInAndOutOfClass("\\u2070", "\u2070")
    matchesInAndOutOfClass("\\u0041", "A")
    matchesInAndOutOfClass("\\uBEEF", "\uBEEF")
    matchesInAndOutOfClass("\\uffFF", "\uFFFF")
    matches("\\u0041{3}", "AAA")
    matches("[\\u0041-\\u0043]+", "ABC")
    notMatches("[\\u0041-\\u0043]+", "ABCD")
    matches("x\\u002A", "x*")
    notMatches("x\\u002A", "xxx")
  }

  @Test def unicodeEscapeSurrogatePair(): Unit = {
    val smiley = "\uD83D\uDE00"
    matchesInAndOutOfClass("\\uD83D\\uDE00", smiley)
    matches("\\uD83D\\uDE00+", smiley + smiley)
    matches("[\\uD83D\\uDE00-\\uD83D\\uDE4F]", "\uD83D\uDE4F")
    notMatches("\\uD83D\\uDE00", "\uD83D")
    notMatches("[\\uD83D\\uDE00]", "\uD83D")
    notMatches("[\\uD83D\\uDE00]", "\uDE00")
  }

  @Test def unicodeEscapeLoneSurrogate(): Unit = {
    matches("\\uD83D", "\uD83D")
    matches("a\\uDE00", "a\uDE00")
    matches("\\uD83D\\u0041", "\uD83DA")
    assertTrue(Pattern.compile("\\uD83D").matcher("x\uD83Dy").find())
  }

  @Test def unicodeEscapeQuoted(): Unit = {
    matches("\\\\u0041", "\\u0041")
    matches("\\Q\\u0041\\E", "\\u0041")
    notMatches("\\Q\\u0041\\E", "A")
  }

  @Test def unicodeEscapeInvalid(): Unit = {
    invalid("\\u")
    invalid("\\u00")
    invalid("\\u004")
    invalid("\\u00G1")
    invalid("[\\u00]")
    invalid("\\U0041")
  }

  @Test def hexEscape(): Unit = {
    matchesInAndOutOfClass("\\x41", "A")
    matchesInAndOutOfClass("\\xFF", "\u00FF")
    matchesInAndOutOfClass("\\x{41}", "A")
    matchesInAndOutOfClass("\\x{2070}", "\u2070")
    matchesInAndOutOfClass("\\x{1F600}", "\uD83D\uDE00")
    matchesInAndOutOfClass("\\x{10FFFF}", "\uDBFF\uDFFF")
    matches("[\\x41-\\x43]+", "ABC")
    matches("[\\x{1F600}-\\x{1F64F}]", "\uD83D\uDE4F")
  }

  @Test def hexEscapeInvalid(): Unit = {
    invalid("\\x")
    invalid("\\x4")
    invalid("[\\x4")
    invalid("\\xG1")
    invalid("\\x{}")
    invalid("\\x{110000}")
    invalid("\\x{41")
  }

  @Test def octalEscape(): Unit = {
    matchesInAndOutOfClass("\\01", "\u0001")
    matchesInAndOutOfClass("\\07", "\u0007")
    matchesInAndOutOfClass("\\012", "\n")
    matchesInAndOutOfClass("\\0101", "A")
    matchesInAndOutOfClass("\\0100", "@")
    matchesInAndOutOfClass("\\0377", "\u00FF")
    // A three digit octal escape must start with 0-3
    matches("\\0477", "'7")
    matches("\\0400", " 0")
    matches("\\01010", "A0")
    matches("[\\0101-\\0103]+", "ABC")
  }

  @Test def octalEscapeInvalid(): Unit = {
    invalid("\\0")
    invalid("\\08")
    invalid("[\\0]")
    invalid("[\\08]")
  }

  @Test def controlEscape(): Unit = {
    matchesInAndOutOfClass("\\cA", "\u0001")
    matchesInAndOutOfClass("\\cZ", "\u001A")
    matchesInAndOutOfClass("\\c@", "\u0000")
    matchesInAndOutOfClass("\\c[", "\u001B")
    matchesInAndOutOfClass("\\c?", "\u007F")
    matches("\\ca", "!")
    matches("\\cA+", "\u0001\u0001")
    matches("[\\cA-\\cC]+", "\u0001\u0002\u0003")
  }

  @Test def controlEscapeInvalid(): Unit = {
    invalid("\\c")
    invalid("[\\c")
  }

  @Test def otherCharacterEscapes(): Unit = {
    matchesInAndOutOfClass("\\e", "\u001B")
    matchesInAndOutOfClass("\\a", "\u0007")
    matchesInAndOutOfClass("\\t", "\t")
    matchesInAndOutOfClass("\\n", "\n")
    matchesInAndOutOfClass("\\r", "\r")
    matchesInAndOutOfClass("\\f", "\f")
  }
}
