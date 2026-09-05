package org.scalanative.testsuite.javalib.io

import java.io.{Reader, StringReader}
import java.util.{stream => jus}
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

class ReaderTestOnJDK25 {

  /* Java 26 documents ReadAllAsString() and ReadAllLines() as possibly
   * throwing only IOException and OutOfMemoryError.
   *
   * Rely upon Tests in the underlying methods used by Reader to exercise
   * those possibilities. StringBuilder in particular should throw
   * OutOfMemoryError on any attempt to grow the string beyond VM limits.
   */

  /* Selected verses from public domain:
   * Robert Louis Stephenson "Sing me a Song of a Lad that is Gone"
   *
   * URLs:
   *
   *   https://en.wikipedia.org/wiki/The_Skye_Boat_Song
   *
   *   https://www.poetryfoundation.org/poems/45949/
   *     sing-me-a-song-of-a-lad-that-is-gone
   */

  /* This list is crafted using some knowledge of Reader internals,
   * specifically the buffer sizes used by readAllAsString() and
   * readAllLines(). As of this writing, they both use 256 characters
   * for raw reads. The selected verses of the poem contain 369 so
   * that at least two buffered reads are necessary. This tests
   * the transition between buffers.
   *
   * No good way to ensure this balance, beyond a comment in Reader.scala
   * to change here if either of those sizes are changed.
   */

  val poemByLine = ju.List.of(
    "", // The first line is empty in an attempt to trip up read logic.
    "Sing me a song of a lad that is gone,",
    "Say, could that lad be I?",
    "Merry of soul he sailed on a day",
    "Over the sea to Skye.",
    "",
    "Mull was astern, Rum on the port,",
    "Eigg on the starboard bow;",
    "Glory of youth glowed in his soul;",
    "Where is that glory now?",
    "",
    "Give me again all that was there,",
    "Give me the sun that shone!",
    "Give me the eyes, give me the soul,",
    "Give me the lad that's gone!"
  )

  /* The final line does not get terminated to test non-terminated EOF.
   * A similar condition holds for both <CR> and <CR><LF> variants.
   */
  val poemNlTerminated = poemByLine
    .stream()
    .collect(jus.Collectors.joining("\n"))

  @Test def ReadAllAsString(): Unit = {
    val poem = poemNlTerminated

    val reader = new StringReader(poem)

    val asString = reader.readAllAsString()

    assertEquals("size", poem.size, asString.size)
    assertEquals("contents", poem, asString)
  }

  @Test def ReadAllLines_LfTerminated(): Unit = { // linefeed a.k.a '\n'

    val reader = new StringReader(poemNlTerminated)

    val asLines = reader.readAllLines()

    assertEquals("size", poemByLine.size(), asLines.size())
    assertEquals("contents", poemByLine, asLines)

    // Be paranoid, check that each of the lines do not contain a terminator.
    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) should not contain terminator '\\n'",
        asLines.get(j).indexOf('\n') == -1
      )
  }

  @Test def ReadAllLines_CrTerminated(): Unit = { // Carriage return '\r'
    val poemCrTerminated = poemByLine
      .stream()
      .collect(jus.Collectors.joining("\r"))

    assertEquals(
      s"poemCrTerminated() should not contain terminator '\\n'",
      -1,
      poemCrTerminated.indexOf('\n')
    )

    assertEquals(
      s"poemCrTerminated() should contain terminator '\\r'",
      0,
      poemCrTerminated.indexOf('\r')
    )

    val reader = new StringReader(poemCrTerminated)

    val asLines = reader.readAllLines()

    assertEquals("size", poemByLine.size(), asLines.size())
    assertEquals("contents", poemByLine, asLines)

    // Check that each of the lines do not contain a terminator.
    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) should not contain terminator '\\n'",
        asLines.get(j).indexOf('\n') == -1
      )

    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) |${asLines.get(j)}|should not contain terminator '\\r'",
        asLines.get(j).indexOf('\r') == -1
      )
  }

  @Test def ReadAllLines_CrLfTerminated(): Unit = { // '\r' '\n'
    val poemCrLfTerminated = poemByLine
      .stream()
      .collect(jus.Collectors.joining("\r\n"))

    val reader = new StringReader(poemCrLfTerminated)

    val asLines = reader.readAllLines()

    assertEquals("size", poemByLine.size(), asLines.size())
    assertEquals("contents", poemByLine, asLines)

    // Check that each of the lines do not contain a terminator.
    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) should not contain terminator '\\n'",
        asLines.get(j).indexOf('\n') == -1
      )

    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) should not contain terminator '\\r'",
        asLines.get(j).indexOf('\r') == -1
      )
  }

  /* Someday create a test where the lines have a mixture of lf, cr, and crlf
   * terminators. Should probably do the 9 combinations of one kind
   * of terminator being followed by different. A project for a
   * snowy eve.
   */

  @Test def ReadAllLines_CrAndLf_NotAdjacent(): Unit = {
    /* Exercise a corner case  where the CR and LF are not immediately
     * adjacent.
     * The LF should cause a line break and NOT be skipped for having come
     * after, but not immediately after, a CR.
     */

    val trickyText = "abc\rdef\nhij"

    val expected = ju.List.of("abc", "def", "hij")

    val reader = new StringReader(trickyText)

    val asLines = reader.readAllLines()

    assertEquals("size", expected.size(), asLines.size())
    assertEquals("contents", expected, asLines)

    // Check that each of the lines do not contain a terminator.
    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) should not contain terminator '\\n'",
        asLines.get(j).indexOf('\n') == -1
      )

    for (j <- 0 until asLines.size())
      assertTrue(
        s"output(${j}) should not contain terminator '\\r'",
        asLines.get(j).indexOf('\r') == -1
      )
  }
}
