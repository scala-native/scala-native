package org.scalanative.testsuite.javalib.io

import java.io.{StringWriter, IOException, Reader}
import java.nio.CharBuffer

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ReaderTestOnJDK11 {

  @Test def nullReaderWhenOpen(): Unit = {
    val nullReader = Reader.nullReader()

    val buffer = new Array[Char](8)

    assertThrows(
      "mark()",
      classOf[IOException],
      nullReader.mark(1)
    )

    assertEquals("markSupported()", false, nullReader.markSupported())

    assertEquals("read()", -1, nullReader.read())

    assertEquals("read(charArray)", -1, nullReader.read(buffer))

    /* Check the args of the abstract read(buffer, off, len) method more
     * rigorously. Other methods eventually rely upon those args being valid.
     *
     * JVM checks its arguments before checking if the Reader is closed,
     * so no need to repeat these checks in Test  nullReaderWhenClosed().
     */

    assertThrows(
      "read(buffer, off, len) null buffer",
      classOf[NullPointerException],
      nullReader.read(null, 1, 0)
    )

    assertThrows(
      "read(buffer, off, len) offset < 0",
      classOf[IndexOutOfBoundsException],
      nullReader.read(buffer, -1, 0)
    )

    assertThrows(
      "read(buffer, off, len) length < 0",
      classOf[IndexOutOfBoundsException],
      nullReader.read(buffer, 0, -2)
    )

    assertThrows(
      "read(buffer, off, len) off > buffer.length - off",
      classOf[IndexOutOfBoundsException],
      nullReader.read(buffer, 6, 5)
    )

    // zero length read returns 0, a corner case
    assertEquals(
      "read(buffer, off, len) len == 0",
      0,
      nullReader.read(buffer, 1, 0)
    )

    // Normal read
    assertEquals(
      "read(buffer, off, len)",
      -1,
      nullReader.read(buffer, 1, buffer.length - 2)
    )

    assertEquals(
      "read(CharBuffer)",
      -1,
      nullReader.read(CharBuffer.wrap(buffer, 0, buffer.length))
    )

    assertEquals(
      "ready()",
      false,
      nullReader.ready()
    )

    assertThrows(
      "reset()",
      classOf[IOException],
      nullReader.reset()
    )

    assertEquals("skip(len)", 0L, nullReader.skip(buffer.length - 3))

    assertEquals("transferTo()", 0, nullReader.transferTo(new StringWriter))

    nullReader.close() // close of open reader should succeed
  }

  @Test def nullReaderWhenClosed(): Unit = {
    val nullReader = Reader.nullReader()

    val buffer = new Array[Char](8)

    nullReader.close() // close of open reader should succeed

    assertThrows(
      "mark()",
      classOf[IOException],
      nullReader.mark(1)
    )

    assertEquals("markSupported()", false, nullReader.markSupported())

    /* JVM checks arguments before checking closed. The Test nullReaderWhenOpen
     * exercises those paths, so no need to check here.
     */

    assertThrows("read()", classOf[IOException], nullReader.read())

    assertThrows(
      "read(charArray)",
      classOf[IOException],
      nullReader.read(buffer)
    )

    assertThrows(
      "read(buffer, off, len)",
      classOf[IOException],
      nullReader.read(buffer, 1, buffer.length - 2)
    )

    assertThrows(
      "read(CharBuffer)",
      classOf[IOException],
      nullReader.read(CharBuffer.wrap(buffer, 0, buffer.length))
    )

    assertThrows(
      "ready()",
      classOf[IOException],
      nullReader.ready()
    )

    assertThrows(
      "ready()",
      classOf[IOException],
      nullReader.ready()
    )

    assertThrows(
      "reset()",
      classOf[IOException],
      nullReader.reset()
    )

    assertThrows(
      "skip(len)",
      classOf[IOException],
      nullReader.skip(buffer.length - 3)
    )

    assertThrows(
      "transferTo()",
      classOf[IOException],
      nullReader.transferTo(new StringWriter)
    )

    nullReader.close() // close of closed reader should succeed
  }
}
