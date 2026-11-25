package org.scalanative.testsuite.javalib.nio.charset

import java.nio.charset._
import java.nio.{ByteBuffer, CharBuffer}

import org.junit.Assert._
import org.junit.Test

class UTF8DecoderTest {

  /* (input, malformed lengths reported with endOfInput = true,
   *  result of the first decode call with endOfInput = false,
   *  decoded string with REPLACE)
   */
  private val cases: Seq[(Seq[Int], Seq[Int], String, String)] = Seq(
    (Seq(0xc0), Seq(1), "M1", "�"),
    (Seq(0xc1, 0xbf), Seq(1, 1), "M1", "��"),
    (Seq(0xc2), Seq(1), "U0", "�"),
    (Seq(0xc2, 0x41), Seq(1), "M1", "�A"),
    (Seq(0xdf, 0xc0), Seq(1, 1), "M1", "��"),
    (Seq(0xe0), Seq(1), "U0", "�"),
    (Seq(0xe0, 0x80), Seq(1, 1), "M1", "��"),
    (Seq(0xe0, 0x9f), Seq(1, 1), "M1", "��"),
    (Seq(0xe0, 0xa0), Seq(2), "U0", "�"),
    (Seq(0xe0, 0x80, 0x80), Seq(1, 1, 1), "M1", "���"),
    (Seq(0xe0, 0xa0, 0x41), Seq(2), "M2", "�A"),
    (Seq(0xe1, 0x80), Seq(2), "U0", "�"),
    (Seq(0xe1, 0x41), Seq(1), "M1", "�A"),
    (Seq(0xe1, 0x80, 0x41), Seq(2), "M2", "�A"),
    (Seq(0xed, 0xa0), Seq(2), "U0", "�"),
    (Seq(0xed, 0xa0, 0x80), Seq(3), "M3", "�"),
    (Seq(0xed, 0x9f, 0xbf), Seq(), "U3", "퟿"),
    (Seq(0xef, 0xc0), Seq(1, 1), "M1", "��"),
    (Seq(0xf0), Seq(1), "U0", "�"),
    (Seq(0xf0, 0x80), Seq(1, 1), "M1", "��"),
    (Seq(0xf0, 0x8f), Seq(1, 1), "M1", "��"),
    (Seq(0xf0, 0x90), Seq(2), "U0", "�"),
    (Seq(0xf0, 0x8f, 0xbf, 0xbf), Seq(1, 1, 1, 1), "M1", "�" * 4),
    (Seq(0xf0, 0x90, 0x80), Seq(3), "U0", "�"),
    (Seq(0xf0, 0x90, 0x41), Seq(2), "M2", "�A"),
    (Seq(0xf0, 0x90, 0x80, 0x41), Seq(3), "M3", "�A"),
    (Seq(0xf3, 0xbf, 0xbf), Seq(3), "U0", "�"),
    (Seq(0xf4), Seq(1), "U0", "�"),
    (Seq(0xf4, 0x8f), Seq(2), "U0", "�"),
    (Seq(0xf4, 0x90), Seq(1, 1), "M1", "��"),
    (Seq(0xf4, 0x90, 0x80, 0x80), Seq(1, 1, 1, 1), "M1", "�" * 4),
    (Seq(0xf4, 0x8f, 0xbf), Seq(3), "U0", "�"),
    (Seq(0xf4, 0x8f, 0xbf, 0xbf), Seq(), "U4", "􏿿"),
    (Seq(0xf5), Seq(1), "M1", "�"),
    (Seq(0xf5, 0x80), Seq(1, 1), "M1", "��"),
    (Seq(0xf7, 0xbf, 0xbf, 0xbf), Seq(1, 1, 1, 1), "M1", "�" * 4),
    (Seq(0xf8, 0x80), Seq(1, 1), "M1", "��"),
    (Seq(0xff), Seq(1), "M1", "�"),
    (Seq(0xbf, 0x41), Seq(1), "M1", "�A"),
    // Issue #5329
    (Seq(0xe0, 0x80, 0x41), Seq(1, 1), "M1", "��A"),
    (Seq(0xe0, 0x9f, 0x41), Seq(1, 1), "M1", "��A"),
    (Seq(0xf0, 0x80, 0x41), Seq(1, 1), "M1", "��A"),
    (Seq(0xf0, 0x8f, 0x41), Seq(1, 1), "M1", "��A"),
    (Seq(0xf4, 0x90, 0x41), Seq(1, 1), "M1", "��A"),
    (Seq(0xf5, 0x80, 0x41), Seq(1, 1), "M1", "��A"),
    (Seq(0xf4, 0x8f, 0x41), Seq(2), "M2", "�A"),
    (Seq(0xed, 0xa0, 0x41), Seq(2), "M2", "�A"),
    (Seq(0xc0, 0xaf, 0x41), Seq(1, 1), "M1", "��A")
  )

  private def bytes(in: Seq[Int]): Array[Byte] = in.map(_.toByte).toArray

  private def hex(in: Seq[Int]): String =
    in.map(b => f"$b%02X").mkString(" ")

  private def reportDecoder(): CharsetDecoder =
    StandardCharsets.UTF_8
      .newDecoder()
      .onMalformedInput(CodingErrorAction.REPORT)
      .onUnmappableCharacter(CodingErrorAction.REPORT)

  @Test def malformedLengthsAtEndOfInput(): Unit = {
    for ((input, expected, _, _) <- cases) {
      val in = ByteBuffer.wrap(bytes(input))
      val out = CharBuffer.allocate(16)
      val decoder = reportDecoder()
      val lengths = Seq.newBuilder[Int]
      var done = false
      while (!done) {
        val result = decoder.decode(in, out, true)
        if (result.isMalformed()) {
          lengths += result.length()
          in.position(in.position() + result.length())
        } else {
          assertTrue(hex(input), result.isUnderflow())
          done = true
        }
      }
      assertEquals(hex(input), expected, lengths.result())
    }
  }

  @Test def malformedLengthsBeforeEndOfInput(): Unit = {
    for ((input, _, expected, _) <- cases) {
      val in = ByteBuffer.wrap(bytes(input))
      val result = reportDecoder().decode(in, CharBuffer.allocate(16), false)
      val actual =
        if (result.isMalformed()) s"M${result.length()}"
        else if (result.isUnderflow()) s"U${in.position()}"
        else result.toString()
      assertEquals(hex(input), expected, actual)
    }
  }

  @Test def replacementCount(): Unit = {
    for ((input, _, _, expected) <- cases) {
      assertEquals(
        hex(input),
        expected,
        new String(bytes(input), StandardCharsets.UTF_8)
      )
      val decoded = StandardCharsets.UTF_8
        .newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .decode(ByteBuffer.wrap(bytes(input)))
        .toString()
      assertEquals(hex(input), expected, decoded)
    }
  }
}
