package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringTestOnJDK12 {

  @Test def indent(): Unit = {
    assertEquals("", "".indent(1))
    assertEquals("", "".indent(0))
    assertEquals("", "".indent(-1))
    assertEquals(" \n", "\n".indent(1))
    assertEquals("\n", "\n".indent(0))
    assertEquals("\n", "\n".indent(-1))

    // indent adds the extra new line due to JDK normalization requirements
    assertEquals("  abc\n", "abc".indent(2))
    assertEquals(" abc\n", "abc".indent(1))
    assertEquals("abc\n", "abc".indent(0))
    assertEquals("abc\n", "abc".indent(-1))
    assertEquals("abc\n", "abc".indent(-2))
    assertEquals("     a\n       b\n", "a\n  b\n".indent(5))
    assertEquals("a\n  b\n", "a\n  b\n".indent(0))
    assertEquals("a\nb\n", "a\n  b\n".indent(-5))
    assertEquals("      \n", "      ".indent(0))
    assertEquals("            \n", "      ".indent(6))
    assertEquals("\n", "      ".indent(-6))
    assertEquals(" \n", "   ".indent(-2))
    assertEquals("  \n", "        ".indent(-6))

    assertEquals("  a\n  \n  c\n", "a\n\nc".indent(2))
    assertEquals("  abc\n  def\n", "abc\ndef".indent(2))
    assertEquals(
      "  abc\n  def\n  \n  \n  \n  a\n",
      "abc\ndef\n\n\n\na".indent(2)
    )

    assertEquals(" \n  \n", "\n \n".indent(1))
    assertEquals("  \n  \n  \n", " \n \n ".indent(1))
    assertEquals(" \n \n \n \n", "\n\n\n\n".indent(1))
    assertEquals(" 0\n A\n B\n C\n D\n", "0\r\nA\r\nB\r\nC\r\nD".indent(1))
    assertEquals(" 0\n A\n B\n C\n D\n", "0\rA\rB\rC\rD".indent(1))

    assertEquals("  \n  \n  \n", "\r\r\n\n".indent(2))
    assertEquals("  \n  \n  \n  \n", "\r\r\r\r".indent(2))
    assertEquals("  \n  \n", "\r\n\r\n".indent(2))
    assertEquals("\n\n\n", "\r\n\n\n".indent(-1))
    assertEquals("\n\n\n", "\r\n\n\n".indent(0))

    // non-U+0020 WS
    assertEquals(
      "  \u2028 \u2029 \u2004 \u200a \u3000 \n",
      "\u2028 \u2029 \u2004 \u200A \u3000 ".indent(2)
    )
    assertEquals(
      "\u2029 \u2004 \u200A \u3000 \n",
      "\u2028 \u2029 \u2004 \u200A \u3000 ".indent(-2)
    )
    assertEquals(
      "\u2028 \u2029 \u2004 \u200A \u3000 \n",
      "\u2028 \u2029 \u2004 \u200A \u3000 ".indent(0)
    )
  }

  @Test def transform(): Unit = {
    assertEquals("", "".transform(x => x))
    assertEquals("abcabc", "abc".transform(_ * 2))
    assertEquals("bar", "foo".transform(_ => "bar"))
  }

}
