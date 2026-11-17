package org.scalanative.testsuite.javalib.util.regex

import java.util.regex._

import org.junit.Assert._
import org.junit.Test

class PatternLineTerminatorsTest {

  private def findAll(regex: String, input: String, flags: Int = 0): String = {
    val m = Pattern.compile(regex, flags).matcher(input)
    val sb = new java.lang.StringBuilder
    while (m.find()) {
      if (sb.length() > 0) sb.append(' ')
      sb.append(m.start()).append('-').append(m.end())
    }
    sb.toString
  }

  private val terminators = Seq("\n", "\r", "\u0085", " ", " ")

  @Test def dotExcludesLineTerminators(): Unit = {
    for (t <- terminators) {
      val in = "a" + t + "b"
      assertEquals(s"'.' on ${in.map(_.toInt)}", "0-1 2-3", findAll(".", in))
      assertEquals("0-1 1-2 2-3", findAll("(?s).", in))
      assertEquals("0-1 1-2 2-3", findAll(".", in, Pattern.DOTALL))
    }
    assertEquals("0-1 3-4", findAll(".", "a\r\nb"))
    assertEquals("", findAll(".", "\r\n"))
    assertEquals("0-1 1-2", findAll(".", "\u000b\u000c"))
    assertFalse(Pattern.matches("a.b", "a\rb"))
    assertTrue(Pattern.matches("a.b", "a\u000bb"))
    assertEquals("1-3", findAll("b.+", "abé\r\nc"))
  }

  @Test def dotInAlternationAndClasses(): Unit = {
    assertEquals("0-1", findAll(".|\r", "\r"))
    assertEquals("0-1 1-2", findAll("[^a]", "\r\n"))
    assertEquals("0-1 1-2", findAll("(?:.|\n)", "x\n"))
    assertEquals("", findAll("(?i:.)", "\r"))
  }
}
