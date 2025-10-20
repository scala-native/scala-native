package org.scalanative.testsuite.javalib.lang

import java.nio.CharBuffer

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharSequenceTestOnJDK11 {

  @Test def compareStringString(): Unit = {

    val string_1 = "abc"
    val string_1Clone = "abc"
    val string_1CloneClone = "abc"

    val string_2 = "axc"

    // Reflexive
    assertTrue("s1 == s1", CharSequence.compare(string_1, string_1) == 0)

    assertTrue(
      "s1 == s1Clone",
      CharSequence.compare(string_1, string_1Clone) == 0
    )

    // Symmetric

    assertTrue("s1 < s2", CharSequence.compare(string_1, string_2) < 0)
    assertTrue("s2 > s1", CharSequence.compare(string_2, string_1) > 0)

    // Transitive

    // s1 == s1Clone checked above.
    assertTrue(
      "s1Clone == s1CloneClone",
      CharSequence.compare(string_1Clone, string_1CloneClone) == 0
    )

    assertTrue(
      "s1 == s1CloneClone",
      CharSequence.compare(string_1, string_1CloneClone) == 0
    )
  }

  @Test def compareStringCharBuffer(): Unit = {

    val string_1 = "ghijk"

    val charBuf_1 = CharBuffer.allocate(20).append("ghijk").flip()
    val charBuf_2 = CharBuffer.allocate(20).append("ghijk").flip()
    val charBuf_3 = CharBuffer.allocate(20).append("ghijklmnopq").flip()

    assertTrue("s1 == s1", CharSequence.compare(string_1, string_1) == 0)

    assertTrue("cb1 == cb1", CharSequence.compare(charBuf_1, charBuf_1) == 0)
    assertTrue("cb1 == cb2", CharSequence.compare(charBuf_1, charBuf_2) == 0)
    assertTrue("cb1 != cb3", CharSequence.compare(charBuf_1, charBuf_3) != 0)

    // Symmetric, across classes
    assertTrue("s1 == cb1", CharSequence.compare(string_1, charBuf_1) == 0)
    assertTrue("cb1 == s1", CharSequence.compare(charBuf_1, string_1) == 0)

    assertTrue("s1 < cb3", CharSequence.compare(string_1, charBuf_3) < 0)
    assertTrue("cb3 > s1", CharSequence.compare(charBuf_3, string_1) > 0)

    // Transitive, across classes
    assertTrue("s1 == cb2", CharSequence.compare(string_1, charBuf_2) == 0)
  }

}
