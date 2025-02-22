package org.scalanative.testsuite.javalib.io

import java.io.{IOException, Writer}

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class WriterTestOnJDK11 {

  @Test def nullWriterWhenOpen(): Unit = {
    val nullWriter = Writer.nullWriter()

    val buffer = new Array[Char](8)

    assertEquals("append(char)", nullWriter, nullWriter.append('Y'))

    assertEquals("append(CharSequence)", nullWriter, nullWriter.append("XYZ"))

    assertEquals(
      "append(CharSequence, int, int)",
      nullWriter,
      nullWriter.append("LMNOPQ", 1, 3)
    )

    nullWriter.flush() // Should not throw

    nullWriter.write(42) // Should not throw

    nullWriter.write(buffer) // Should not throw

    /* Check the args of the abstract write(buffer, off, len) method more
     * rigorously. Other methods eventually rely upon those args being valid.
     *
     * JVM checks its arguments before checking if the Writer is closed,
     * so no need to repeat these checks in Test nullWritererWhenClosed().
     */

    assertThrows(
      "write(buffer, off, len) null buffer",
      classOf[NullPointerException],
      nullWriter.write(null.asInstanceOf[Array[Char]], 1, 0)
    )

    assertThrows(
      "write(buffer, off, len) offset < 0",
      classOf[IndexOutOfBoundsException],
      nullWriter.write(buffer, -1, 0)
    )

    assertThrows(
      "write(buffer, off, len) length < 0",
      classOf[IndexOutOfBoundsException],
      nullWriter.write(buffer, 0, -2)
    )

    assertThrows(
      "write(buffer, off, len) off > buffer.length - off",
      classOf[IndexOutOfBoundsException],
      nullWriter.write(buffer, 6, 5)
    )

    // Normal write
    nullWriter.write(buffer, 2, 5) // Should not throw

    val poe = "while I pondered, weak and weary"
    nullWriter.write(poe) // Should not throw
    nullWriter.write(poe, 3, 6) // Should not throw

    nullWriter.close() // close of open writer should succeed
  }

  @Test def nullWriterWhenClosed(): Unit = {
    val nullWriter = Writer.nullWriter()

    val buffer = new Array[Char](8)

    nullWriter.close() // close of open writer should succeed

    assertThrows("append(char)", classOf[IOException], nullWriter.append('Y'))

    assertThrows(
      "append(CharSequence)",
      classOf[IOException],
      nullWriter.append("XYZ")
    )

    assertThrows(
      "append(CharSequence, int, int)",
      classOf[IOException],
      nullWriter.append("LMNOPQ", 1, 3)
    )

    assertThrows(
      classOf[IOException],
      nullWriter.flush()
    )

    assertThrows(
      classOf[IOException],
      nullWriter.write(42)
    )

    assertThrows(
      classOf[IOException],
      nullWriter.write(buffer)
    )

    assertThrows(
      classOf[IOException],
      nullWriter.write(buffer, 2, 5)
    )

    val poe = "while I pondered, weak and weary"
    assertThrows(
      classOf[IOException],
      nullWriter.write(poe)
    )

    /* JVM checks arguments before checking closed. The Test nullWriterWhenOpen
     * exercises those paths, so no need to check here.
     */

    assertThrows(
      classOf[IOException],
      nullWriter.write(poe, 3, 6)
    )

    nullWriter.close() // close of closed writer should succeed
  }
}
