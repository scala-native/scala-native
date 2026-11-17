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

  private val anchorInputs = Seq(
    "a\r\nb",
    "a\nb",
    "a\rb",
    "a\u0085b",
    "a\u2028b",
    "a\u2029b",
    "a\n",
    "a\r\n",
    "a\r",
    "a\n\n",
    "a\r\n\r\n",
    "a\u2028",
    "\r\n",
    "\n\r",
    "a\n\r",
    "",
    "a"
  )

  private def checkAll(regex: String, expected: Seq[String]): Unit =
    anchorInputs.zip(expected).foreach {
      case (in, exp) =>
        assertEquals(s"$regex on ${in.map(_.toInt)}", exp, findAll(regex, in))
    }

  @Test def dollarAndUpperZ(): Unit = {
    val expected = Seq(
      "4-4",
      "3-3",
      "3-3",
      "3-3",
      "3-3",
      "3-3",
      "1-1 2-2",
      "1-1 3-3",
      "1-1 2-2",
      "2-2 3-3",
      "3-3 5-5",
      "1-1 2-2",
      "0-0 2-2",
      "1-1 2-2",
      "2-2 3-3",
      "0-0",
      "1-1"
    )
    checkAll("$", expected)
    checkAll("\\Z", expected)
  }

  @Test def lowerZ(): Unit = checkAll(
    "\\z",
    Seq(
      "4-4",
      "3-3",
      "3-3",
      "3-3",
      "3-3",
      "3-3",
      "2-2",
      "3-3",
      "2-2",
      "3-3",
      "5-5",
      "2-2",
      "2-2",
      "2-2",
      "3-3",
      "0-0",
      "1-1"
    )
  )

  @Test def caret(): Unit = checkAll("^", anchorInputs.map(_ => "0-0"))

  @Test def multilineDollar(): Unit = checkAll(
    "(?m)$",
    Seq(
      "1-1 4-4",
      "1-1 3-3",
      "1-1 3-3",
      "1-1 3-3",
      "1-1 3-3",
      "1-1 3-3",
      "1-1 2-2",
      "1-1 3-3",
      "1-1 2-2",
      "1-1 2-2 3-3",
      "1-1 3-3 5-5",
      "1-1 2-2",
      "0-0 2-2",
      "0-0 1-1 2-2",
      "1-1 2-2 3-3",
      "0-0",
      "1-1"
    )
  )

  @Test def multilineCaret(): Unit = checkAll(
    "(?m)^",
    Seq(
      "0-0 3-3",
      "0-0 2-2",
      "0-0 2-2",
      "0-0 2-2",
      "0-0 2-2",
      "0-0 2-2",
      "0-0",
      "0-0",
      "0-0",
      "0-0 2-2",
      "0-0 3-3",
      "0-0",
      "0-0",
      "0-0 1-1",
      "0-0 2-2",
      "",
      "0-0"
    )
  )

  @Test def literalBeforeDollar(): Unit = {
    checkAll(
      "a$",
      Seq(
        "",
        "",
        "",
        "",
        "",
        "",
        "0-1",
        "0-1",
        "0-1",
        "",
        "",
        "0-1",
        "",
        "",
        "",
        "",
        "0-1"
      )
    )
    checkAll(
      "(?m)a$",
      Seq(
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "0-1",
        "",
        "",
        "0-1",
        "",
        "0-1"
      )
    )
  }

  @Test def crlfIsOneTerminator(): Unit = {
    assertEquals("1-3", findAll("(?m)$\\r\\n", "a\r\nb"))
    assertEquals("", findAll("(?m)$\\n", "a\r\nb"))
    assertEquals("", findAll("(?m)\\r$", "a\r\nb"))
    assertEquals("", findAll("(?m)\\r^", "a\r\nb"))
    assertEquals("", findAll("(?m)^\\n", "a\r\nb"))
    assertEquals("1-2", findAll("(?m)$\\s", "a\r\nb"))
    assertEquals("0-2", findAll("a$\\n", "a\n"))
    assertEquals("", findAll("$\\n", "a\r\n"))
    assertEquals("", findAll("\\r$\\n", "a\r\n"))
  }

  @Test def trailingHorizontalWhitespaceOnCrlfInput(): Unit = {
    assertEquals(
      "1-3 6-7",
      findAll("\\h+$", "x  \r\ny \r\n", Pattern.MULTILINE)
    )
    assertEquals(
      "x\r\ny\r\n",
      Pattern
        .compile("[ \\t]+$", Pattern.MULTILINE)
        .matcher("x  \r\ny \t\r\n")
        .replaceAll("")
    )
  }

  @Test def nonTerminatorsAreNotLineEnds(): Unit = {
    assertEquals("", findAll("x$", "x\u000b"))
    assertEquals("", findAll("x$", "x\u000c"))
    assertEquals("", findAll("(?m)x$", "x\u000by"))
  }

  @Test def matchesAndLookingAt(): Unit = {
    assertTrue(Pattern.compile("a$").matcher("a\r\n").lookingAt())
    assertFalse(Pattern.matches("a$", "a\r\n"))
    assertTrue(Pattern.matches("a$\\r\\n", "a\r\n"))
    assertTrue(Pattern.matches("(?m)a$\\u2028^b", "a\u2028b"))
  }

  @Test def splitOnLineEnds(): Unit = {
    val parts = Pattern.compile("(?m)$(?:\\r\\n|\\n)^").split("a\r\nb\nc")
    assertEquals(List("a", "b", "c"), parts.toList)
  }
}
