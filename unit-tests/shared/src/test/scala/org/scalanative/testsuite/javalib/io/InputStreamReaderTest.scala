package org.scalanative.testsuite.javalib.io

import java.io._
import java.nio.charset._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamReaderTest {

  /* Scala.js tests - Begin
   * Ported from Scala.js commit: cbf86bb dated: 2020-10-23
   */

  /* Scala.js tests - End
   */
  import scala.annotation.tailrec

  @Test def readUTF8(): Unit = {

    val buf = Array[Byte](72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100,
      46, -29, -127, -109, -29, -126, -109, -29, -127, -85, -29, -127, -95, -29,
      -127, -81, -26, -105, -91, -26, -100, -84, -24, -86, -98, -29, -126, -110,
      -24, -86, -83, -29, -126, -127, -29, -127, -66, -29, -127, -103, -29,
      -127, -117, -29, -128, -126)

    val r = new InputStreamReader(new ByteArrayInputStream(buf))

    def expectRead(str: String): Unit = {
      val buf = new Array[Char](str.length)
      @tailrec
      def readAll(readSoFar: Int): Int = {
        if (readSoFar == buf.length) readSoFar
        else {
          val newlyRead = r.read(buf, readSoFar, buf.length - readSoFar)
          if (newlyRead == -1) readSoFar
          else readAll(readSoFar + newlyRead)
        }
      }
      assertEquals(str.length, readAll(0))
      assertEquals(str, new String(buf))
    }

    expectRead("Hello World.")
    expectRead("こんにちは")
    expectRead("日本語を読めますか。")
    assertEquals(-1, r.read())
  }

  @Test def readEOFThrows(): Unit = {
    val data = "Lorem ipsum".getBytes()
    val streamReader = new InputStreamReader(new ByteArrayInputStream(data))
    val bytes = new Array[Char](11)

    assertEquals(11, streamReader.read(bytes))
    // Do it twice to check for a regression where this used to throw
    assertEquals(-1, streamReader.read(bytes))
    assertEquals(-1, streamReader.read(bytes))
    assertThrows(
      classOf[IndexOutOfBoundsException],
      streamReader.read(bytes, 10, 3)
    )
    assertEquals(0, streamReader.read(new Array[Char](0)))
  }

  @Test def skipReturns0AfterReachingEnd(): Unit = {
    val data = "Lorem ipsum".getBytes()
    val r = new InputStreamReader(new ByteArrayInputStream(data))
    assertTrue(r.skip(100) > 0)
    assertEquals(-1, r.read())

    assertEquals(0, r.skip(100))
    assertEquals(-1, r.read())
  }

  @Test def markThrowsNotSupported(): Unit = {
    val data = "Lorem ipsum".getBytes()
    val r = new InputStreamReader(new ByteArrayInputStream(data))
    assertThrows(classOf[IOException], r.mark(0))
  }

  /* Scala Native authored tests
   */

  class MockInputStream extends InputStream {
    private var _closed: Boolean = false

    def isClosed: Boolean = _closed

    override def close(): Unit = _closed = true

    def read(): Int = -1
  }

  @Test def shouldThrowNpeIfNullIsPassedToConstructor(): Unit = {
    assertThrows(classOf[NullPointerException], new InputStreamReader(null))
    assertThrows(
      classOf[NullPointerException],
      new InputStreamReader(new MockInputStream, null: CharsetDecoder)
    )
    assertThrows(
      classOf[NullPointerException],
      new InputStreamReader(new MockInputStream, null: Charset)
    )
    assertThrows(
      classOf[NullPointerException],
      new InputStreamReader(new MockInputStream, null: String)
    )
  }

  @Test def inputStreamReaderInputStreamStringWithUnsupportedEnc(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new InputStreamReader(new MockInputStream, "unsupported encoding")
    )
  }

  @Test def closeClosesTheInnerStream(): Unit = {
    val in = new MockInputStream
    val reader = new InputStreamReader(in)
    reader.close()
    assertTrue(in.isClosed)
  }

  @Test def closingTwiceIsHarmless(): Unit = {
    val in = new MockInputStream
    val reader = new InputStreamReader(in)
    reader.close()
    reader.close()
  }
}
