package org.scalanative.testsuite.javalib.io

import java.io.{Reader, StringWriter}
import java.nio.CharBuffer
import java.util.{stream => jus}
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ReaderTestOnJDK24 {

  /* Selected verses from public domain "We Shall Overcome".
   *
   * URLs:
   *
   *  - Rich author history & January 26, 2018 dedication to the public domain
   *      https://en.wikipedia.org/wiki/We_Shall_Overcome
   *
   *  - Lyrics from:
   *     https://www.african-american-civil-rights.org/
   *       we-shall-overcome/#google_vignette
   */

  val songByLine = ju.List.of(
    "We shall overcome,",
    "We shall overcome,",
    "We shall overcome, some day.",
    "",
    "Oh, deep in my heart,",
    "I do believe",
    "We shall overcome, some day.",
    "",
    "We’ll walk hand in hand,",
    "We’ll walk hand in hand,",
    "We’ll walk hand in hand, some day.",
    "",
    "We are not afraid,",
    "We are not afraid,",
    "We are not afraid, TODAY",
    "",
    "Oh, deep in my heart,",
    "I do believe",
    "We shall overcome, some day.      "
  )

  val song = songByLine
    .stream()
    .collect(jus.Collectors.joining("\n"))

  class CustomCharSequence(underlying: String) extends CharSequence {
    def charAt(x0: Int): Char = underlying.charAt(x0)
    def length(): Int = underlying.length()
    def subSequence(x0: Int, x1: Int): CharSequence =
      underlying.subSequence(x0, x1)
  }

  case class CharSequenceSource(name: String, cs: CharSequence)

  val sources = ju.List.of(
    CharSequenceSource("String", song),
    CharSequenceSource("StringBuilder", new StringBuilder(song)),
    CharSequenceSource("StringBuffer", new StringBuffer(song)),
    CharSequenceSource("CharBuffer", CharBuffer.wrap(song)),
    CharSequenceSource("CustomCharSequence", new CustomCharSequence(song))
  )

  @Test def readerOf_Exceptions(): Unit = {
    assertThrows(
      "null argument",
      classOf[NullPointerException],
      Reader.of(null)
    )
  }

  @Test def readerOf(): Unit = {
    sources.forEach { src =>
      val reader = Reader.of(src.cs)
      val writer = new StringWriter()

      val nWritten = reader.transferTo(writer)

      assertEquals(s"reader.of(${src.name}) size", song.size, nWritten)

      assertEquals(
        s"reader.of(${src.name}) contents",
        song,
        writer.toString()
      )
    }
  }
}
