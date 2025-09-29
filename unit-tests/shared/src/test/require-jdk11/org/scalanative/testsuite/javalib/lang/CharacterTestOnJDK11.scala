package org.scalanative.testsuite.javalib.lang

// Ported from Scala.js commit: 9683b0c dated: 2021-12-07

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharacterTestOnJDK11 {

  @Test def toStringCodePoint(): Unit = {
    assertEquals("\u0000", Character.toString(0))
    assertEquals("\u04D2", Character.toString(1234))
    assertEquals("\uD845", Character.toString(0xd845))
    assertEquals("\uDC54", Character.toString(0xdc54))
    assertEquals("\uFFFF", Character.toString(0xffff))

    assertEquals("\uD800\uDC00", Character.toString(0x10000))
    assertEquals("\uD808\uDF45", Character.toString(0x12345))
    assertEquals("\uDBFF\uDFFF", Character.toString(0x10ffff))

    assertThrows(
      classOf[IllegalArgumentException],
      Character.toString(0x110000)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      Character.toString(0x234567)
    )
    assertThrows(classOf[IllegalArgumentException], Character.toString(-1))
    assertThrows(
      classOf[IllegalArgumentException],
      Character.toString(Int.MinValue)
    )
  }

}
