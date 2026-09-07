package org.scalanative.testsuite.javalib.lang

import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class IntegerTestOnJDK9 {

  @Test def parseInteger_Exceptions(): Unit = {
    val payload = "xx012349yy"
    val cs = new StringBuilder(payload)

    assertThrows(
      "parseLong(null, n, n, n)",
      classOf[NullPointerException],
      jl.Integer.parseInt(null.asInstanceOf[CharSequence], 0, 10, 2)
    )

    assertThrows(
      "parseLong(cs, -1, n, n)",
      classOf[IndexOutOfBoundsException],
      jl.Integer.parseInt(cs, -1, 1, 2)
    )

    assertThrows(
      "parseLong(cs, 2, 0, n)",
      classOf[IndexOutOfBoundsException],
      jl.Integer.parseInt(cs, 2, 0, 3)
    )

    assertThrows(
      "parseLong(cs, 0, cs.length + 1, n)",
      classOf[IndexOutOfBoundsException],
      jl.Integer.parseInt(cs, 0, cs.length + 1, 3)
    )
  }

  @Test def parseInteger_Exceptions_Radix(): Unit = {
    val payload = "xx012349yy"
    val cs = new StringBuilder(payload)

    val startParse = 3
    val endParse = 7

    locally {
      val radix = 3

      assertThrows(
        "digit 3 in radix 3 parse should throw",
        classOf[NumberFormatException],
        jl.Integer.parseInt(cs, startParse, endParse, radix)
      )
    }

    locally {
      val radix = Character.MIN_RADIX - 1

      assertThrows(
        "radix less than MIN_RADIX should throw",
        classOf[NumberFormatException],
        jl.Integer.parseInt(cs, startParse, endParse, radix)
      )
    }

    locally {
      val radix = Character.MAX_RADIX + 1

      assertThrows(
        "radix greater than MAX_RADIX should throw",
        classOf[NumberFormatException],
        jl.Integer.parseInt(cs, startParse, endParse, radix)
      )
    }
  }

  @Test def parseInteger_radix10(): Unit = {
    val payload = "xx012349yy" // digits 0 and 9 should not be in result Int.
    val cs = new StringBuilder(payload)

    val startParse = 3
    val endParse = 7
    val radix = 10

    val expected = 1234L

    assertEquals(
      s"parse radix ${radix}",
      expected,
      jl.Integer.parseInt(cs, startParse, endParse, radix)
    )
  }

  @Test def parseInteger_radix2(): Unit = {
    val binaryString = jl.Integer.toBinaryString(jl.Integer.MAX_VALUE)
    val payload = s"xx1${binaryString}0yy"
    val cs = new StringBuilder(payload)

    val startParse = 3
    val endParse = payload.length - 3
    val radix = 2

    val expected = jl.Integer.MAX_VALUE

    assertEquals(
      s"parse radix ${radix}",
      expected,
      jl.Integer.parseInt(cs, startParse, endParse, radix)
    )
  }

  @Test def parseInteger_radix16(): Unit = {
    val binaryString = jl.Integer.toHexString(jl.Integer.MIN_VALUE)

    // Signed, notice leading minus sign required/used here.
    val payload = s"0x-${binaryString}Ayy"
    val cs = new StringBuilder(payload)

    val startParse = 2
    val endParse = payload.length - 3
    val radix = 16

    val expected = jl.Integer.MIN_VALUE

    assertEquals(
      s"parse radix ${radix}",
      expected,
      jl.Integer.parseInt(cs, startParse, endParse, radix)
    )
  }

  @Test def parseUnsignedLong_Exceptions(): Unit = {
    val payload = "xx012349yy"
    val cs = new StringBuilder(payload)

    assertThrows(
      "parseUnsignedLong(null, n, n, n)",
      classOf[NullPointerException],
      jl.Integer.parseUnsignedInt(null.asInstanceOf[CharSequence], 0, 10, 2)
    )

    assertThrows(
      "parseUnsignedLong(cs, -1, n, n)",
      classOf[IndexOutOfBoundsException],
      jl.Integer.parseUnsignedInt(cs, -1, 1, 2)
    )

    assertThrows(
      "parseUnsignedLong(cs, 2, 0, n)",
      classOf[IndexOutOfBoundsException],
      jl.Integer.parseUnsignedInt(cs, 2, 0, 3)
    )

    assertThrows(
      "parseUnsignedLong(cs, 0, cs.length + 1, n)",
      classOf[IndexOutOfBoundsException],
      jl.Integer.parseUnsignedInt(cs, 0, cs.length + 1, 3)
    )
  }

  @Test def parseUnsignedLong_Exceptions_Radix(): Unit = {
    val payload = "xx012349yy"
    val cs = new StringBuilder(payload)

    val startParse = 3
    val endParse = 7

    locally {
      val radix = 3

      assertThrows(
        "digit 3 in radix 3 parse should throw",
        classOf[NumberFormatException],
        jl.Integer.parseUnsignedInt(cs, startParse, endParse, radix)
      )
    }

    locally {
      val radix = Character.MIN_RADIX - 1

      assertThrows(
        "radix less than MIN_RADIX should throw",
        classOf[NumberFormatException],
        jl.Integer.parseUnsignedInt(cs, startParse, endParse, radix)
      )
    }

    locally {
      val radix = Character.MAX_RADIX + 1

      assertThrows(
        "radix greater than MAX_RADIX should throw",
        classOf[NumberFormatException],
        jl.Integer.parseUnsignedInt(cs, startParse, endParse, radix)
      )
    }
  }

  @Test def parseUnsignedLong_radix10(): Unit = {
    val payload = "xx012349yy" // digits 0 and 9 should not be in result Int.
    val cs = new StringBuilder(payload)

    val startParse = 3
    val endParse = 7
    val radix = 10

    val expected = 1234L

    assertEquals(
      s"parse radix ${radix}",
      expected,
      jl.Integer.parseUnsignedInt(cs, startParse, endParse, radix)
    )
  }

  @Test def parseUnsignedLong_radix2(): Unit = {
    val binaryString = jl.Integer.toBinaryString(jl.Integer.MAX_VALUE)
    val payload = s"xx1${binaryString}0yy"
    val cs = new StringBuilder(payload)

    val startParse = 3
    val endParse = payload.length - 3
    val radix = 2

    val expected = jl.Integer.MAX_VALUE

    assertEquals(
      s"parse radix ${radix}",
      expected,
      jl.Integer.parseUnsignedInt(cs, startParse, endParse, radix)
    )
  }

  @Test def parseUnsignedLong_radix16(): Unit = {
    val binaryString = jl.Integer.toHexString(jl.Integer.MIN_VALUE)

    // Unsigned, notice no leading minus sign (not allowed).
    val payload = s"0x${binaryString}Ayy"
    val cs = new StringBuilder(payload)

    val startParse = 2
    val endParse = payload.length - 3
    val radix = 16

    val expected = jl.Integer.MIN_VALUE

    assertEquals(
      s"parse radix ${radix}",
      expected,
      jl.Integer.parseUnsignedInt(cs, startParse, endParse, radix)
    )
  }
}
