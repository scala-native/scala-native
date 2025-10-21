package org.scalanative.testsuite.javalib.lang

import java.lang.StringBuilder

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringBuilderTestOnJDK21 {

  @Test def repeatCharSequenceCheckArgs(): Unit = {
    val bldr = new StringBuilder()

    assertThrows(
      "negative count",
      classOf[IllegalArgumentException],
      bldr.repeat("Y", -3)
    )
  }

  @Test def repeatCharSequence(): Unit = {
    val prefix = "0123"
    val stem = "Díkē"
    val suffix = "89AB"

    val expected = s"${prefix}${stem}${stem}${stem}${suffix}"

    val bldr = new StringBuilder(prefix)
    bldr.repeat(stem, 3).append(suffix)

    assertEquals("repeat", expected, bldr.toString())
  }

  @Test def repeatCodePointCheckArgs(): Unit = {
    val bldr = new StringBuilder()

    assertThrows(
      "non-Unicode codePoint",
      classOf[IllegalArgumentException],
      bldr.repeat(-1, 3)
    )

    assertThrows(
      "negative count",
      classOf[IllegalArgumentException],
      bldr.repeat("Y", -3)
    )
  }

  @Test def repeatCodePoint(): Unit = {
    val prefix = "abcd"
    val cp = '*'
    val stem = s"${cp}${cp}${cp}${cp}${cp}"
    val suffix = "wxyz"

    val expected = s"${prefix}${stem}${suffix}"

    val bldr = new StringBuilder(prefix)
    bldr.repeat(cp, stem.length).append(suffix)

    assertEquals("repeat", expected, bldr.toString())
  }
}
