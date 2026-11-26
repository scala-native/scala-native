package org.scalanative.testsuite.javalib.nio.channels

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, Reader}
import java.nio.channels.{
  Channels, ClosedChannelException, ReadableByteChannel, WritableByteChannel
}
import java.nio.charset.{
  CodingErrorAction, MalformedInputException, StandardCharsets
}
import java.nio.{ByteBuffer, CharBuffer}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class ChannelsTest {
  @Test def newChannelInputStreamReads(): Unit = {
    val expected = Array[Byte](1, 2, 3)
    val in = new ByteArrayInputStream(expected, 0, 3)
    val channel = Channels.newChannel(in)

    val byteBuffer = ByteBuffer.allocate(3)

    channel.read(byteBuffer)
    assertArrayEquals(expected, byteBuffer.array())
  }

  // Issue 3477
  @Test def newChannelInputStreamReportsEOF(): Unit = {
    val expected = Array[Byte](1, 2, 3)
    val in = new ByteArrayInputStream(expected, 0, 3)
    val channel = Channels.newChannel(in)

    val byteBuffer = ByteBuffer.allocate(3)

    // Read, check, and then discard expected in order to get to EOF
    channel.read(byteBuffer)
    assertArrayEquals(expected, byteBuffer.array())
    byteBuffer.rewind()

    val nRead = channel.read(byteBuffer)
    assertEquals("Read of channel at EOF)", -1, nRead)
  }

  @Test def newChannelInputStreamThrows(): Unit = {
    assumeFalse(
      "Bug in the JVM, works for later versions than java 8",
      Platform.executingInJVMOnJDK8OrLower
    )

    val in = new ByteArrayInputStream(Array(0), 0, 1)
    val channel = Channels.newChannel(in)
    val byteBuffer = ByteBuffer.wrap(Array(1))

    channel.close()
    assertThrows(classOf[ClosedChannelException], channel.read(byteBuffer))
  }

  @Test def newChannelOutputStreamWrites(): Unit = {
    val expected = Array[Byte](1, 2, 3)
    val out = new ByteArrayOutputStream(3)
    val channel = Channels.newChannel(out)

    val byteBuffer = ByteBuffer.wrap(expected)

    channel.write(byteBuffer)
    assertArrayEquals(expected, out.toByteArray())
  }

  @Test def newChannelOutputStreamThrows(): Unit = {
    assumeFalse(
      "Bug in the JVM, works for later versions than java 8",
      Platform.executingInJVMOnJDK8OrLower
    )

    val out = new ByteArrayOutputStream(2)
    val channel = Channels.newChannel(out)
    val byteBuffer = ByteBuffer.wrap(Array(0, 0))

    channel.close()
    assertThrows(classOf[ClosedChannelException], channel.write(byteBuffer))
  }

  // Example from javalib.lang.StringTest
  val utf8Array = "\u0000\t\nAZaz09@~\u00DF\u4E66\u1F50A".toCharArray
  val utf8Decoded =
    Seq[Byte](0, 9, 10, 65, 90, 97, 122, 48, 57, 64, 126, // one byte unicode
      -61, -97, // two byte unicode
      -28, -71, -90, // three byte unicode
      -31, -67, -112, 65 // four byte unicode
    )

  @Test def newReaderReadMultiple(): Unit = {
    val in = new ByteArrayInputStream(utf8Decoded.toArray)
    val channel = Channels.newChannel(in)
    val reader =
      Channels.newReader(channel, StandardCharsets.UTF_8.newDecoder, -1)

    val obtained = Array.ofDim[Char](utf8Array.length)
    reader.read(obtained)

    assertArrayEquals(utf8Array, obtained)
  }

  @Test def newReaderReadSingle(): Unit = {
    val in = new ByteArrayInputStream(utf8Decoded.toArray)
    val channel = Channels.newChannel(in)
    val reader =
      Channels.newReader(channel, StandardCharsets.UTF_8.newDecoder, -1)

    for (i <- 0 to 10) { // only the first 11 characters should be equal
      assertEquals(s"Char #$i", utf8Decoded(i), reader.read())
    }
    assertNotEquals("Char #11", utf8Decoded(10), reader.read())
  }

  @Test def newWriterWriteMultiple(): Unit = {
    val out = new ByteArrayOutputStream(10)
    val channel = Channels.newChannel(out)

    val writer = Channels.newWriter(
      channel,
      StandardCharsets.UTF_8.newEncoder,
      utf8Decoded.length
    )

    writer.write(utf8Array)
    writer.flush()

    assertArrayEquals(utf8Decoded.toArray, out.toByteArray())
  }

  @Test def newWriterWriteSingle(): Unit = {
    val out = new ByteArrayOutputStream(10)
    val channel = Channels.newChannel(out)
    val writer = Channels.newWriter(
      channel,
      StandardCharsets.UTF_8.newEncoder,
      utf8Decoded.length
    )

    for (i <- 0 until utf8Array.length) {
      writer.write(utf8Array(i))
    }
    writer.flush()

    assertArrayEquals(utf8Decoded.toArray, out.toByteArray())
  }

  /** Returns at most `chunk` bytes per read. */
  private def chunkedChannel(
      bytes: Array[Byte],
      chunk: Int
  ): ReadableByteChannel = new ReadableByteChannel {
    private var pos = 0
    private var open = true
    def read(dst: ByteBuffer): Int =
      if (pos >= bytes.length) -1
      else {
        val n = chunk.min(dst.remaining()).min(bytes.length - pos)
        dst.put(bytes, pos, n)
        pos += n
        n
      }
    def isOpen(): Boolean = open
    def close(): Unit = open = false
  }

  /** Accepts at most `chunk` bytes per write. */
  private class ChunkedSink(chunk: Int) extends WritableByteChannel {
    val out = new ByteArrayOutputStream()
    var open = true
    def write(src: ByteBuffer): Int = {
      val n = chunk.min(src.remaining())
      for (_ <- 0 until n) out.write(src.get())
      n
    }
    def isOpen(): Boolean = open
    def close(): Unit = open = false
  }

  private def readAll(reader: Reader, bufSize: Int): String = {
    val sb = new java.lang.StringBuilder
    val buf = new Array[Char](bufSize)
    var n = reader.read(buf, 0, bufSize)
    while (n != -1) {
      assertTrue(s"read returned $n", n > 0)
      sb.append(buf, 0, n)
      n = reader.read(buf, 0, bufSize)
    }
    sb.toString
  }

  private def utf8Reader(ch: ReadableByteChannel): Reader =
    Channels.newReader(ch, StandardCharsets.UTF_8.newDecoder(), -1)

  // Issue 5355
  @Test def newReaderReturnsEOFOnEmptyChannel(): Unit = {
    val reader = utf8Reader(chunkedChannel(Array.empty, 16))
    val buf = new Array[Char](16)
    assertEquals(-1, reader.read(buf, 0, 16))
    assertEquals(-1, reader.read(buf, 0, 16))
    assertEquals(-1, reader.read())
    assertEquals(0, reader.read(buf, 0, 0))
  }

  // Issue 5355
  @Test def newReaderReturnsPartialReadThenEOF(): Unit = {
    val in = new ByteArrayInputStream("ab".getBytes(StandardCharsets.UTF_8))
    val reader = Channels.newReader(Channels.newChannel(in), "UTF-8")
    val buf = new Array[Char](16)
    assertEquals(2, reader.read(buf, 0, 16))
    assertEquals("ab", new String(buf, 0, 2))
    assertEquals(-1, reader.read(buf, 0, 16))
    assertEquals(-1, reader.read(buf, 0, 16))
    assertEquals(-1, reader.read())
  }

  @Test def newReaderReadSingleUntilEOF(): Unit = {
    val reader = utf8Reader(
      chunkedChannel("xy".getBytes(StandardCharsets.UTF_8), 16)
    )
    assertEquals('x'.toInt, reader.read())
    assertEquals('y'.toInt, reader.read())
    assertEquals(-1, reader.read())
  }

  @Test def newReaderDecodesCharsSplitAcrossReads(): Unit = {
    val text = "aß书😊zé"
    val bytes = text.getBytes(StandardCharsets.UTF_8)
    for (chunk <- 1 to 4; bufSize <- Seq(1, 2, 3, 64)) {
      val reader = utf8Reader(chunkedChannel(bytes, chunk))
      assertEquals(s"chunk=$chunk buf=$bufSize", text, readAll(reader, bufSize))
    }
  }

  @Test def newReaderDecodesCharsStraddlingInternalBuffer(): Unit = {
    for (prefix <- (1022 to 1025) ++ (4094 to 4097)) {
      val text = ("a" * prefix) + "书😊" + ("b" * 2000)
      val bytes = text.getBytes(StandardCharsets.UTF_8)
      val in = new ByteArrayInputStream(bytes)
      val reader = utf8Reader(Channels.newChannel(in))
      assertEquals(s"prefix=$prefix", text, readAll(reader, 700))
    }
  }

  @Test def newReaderTruncatedInputReportsMalformed(): Unit = {
    val bytes = Array[Byte]('a', 0xe4.toByte, 0xb9.toByte)
    val reader = utf8Reader(chunkedChannel(bytes, 16))
    val buf = new Array[Char](16)
    assertEquals(1, reader.read(buf, 0, 16))
    assertEquals('a', buf(0))
    assertThrows(classOf[MalformedInputException], reader.read(buf, 0, 16))
  }

  @Test def newReaderTruncatedInputIsReplaced(): Unit = {
    val bytes = Array[Byte]('a', 0xe4.toByte, 0xb9.toByte)
    val decoder = StandardCharsets.UTF_8
      .newDecoder()
      .onMalformedInput(CodingErrorAction.REPLACE)
    val reader = Channels.newReader(chunkedChannel(bytes, 1), decoder, -1)
    assertEquals("a�", readAll(reader, 16))
  }

  @Test def newReaderChecksBounds(): Unit = {
    val reader = utf8Reader(
      chunkedChannel("abc".getBytes(StandardCharsets.UTF_8), 16)
    )
    val buf = new Array[Char](4)
    assertThrows(classOf[IndexOutOfBoundsException], reader.read(buf, -1, 2))
    assertThrows(classOf[IndexOutOfBoundsException], reader.read(buf, 0, -1))
    assertThrows(classOf[IndexOutOfBoundsException], reader.read(buf, 3, 2))
  }

  @Test def newInputStreamReturnsEOF(): Unit = {
    val in = Channels.newInputStream(chunkedChannel(Array[Byte](1, -1), 1))
    val buf = new Array[Byte](8)
    assertEquals(1, in.read())
    assertEquals(0xff, in.read())
    assertEquals(-1, in.read())
    assertEquals(-1, in.read(buf, 2, 4))
    assertEquals(0, in.read(buf, 0, 0))
  }

  @Test def newChannelInputStreamRespectsBufferPosition(): Unit = {
    val in = new ByteArrayInputStream(Array[Byte](1, 2, 3, 4, 5))
    val channel = Channels.newChannel(in)
    val dst = ByteBuffer.allocate(4)
    dst.position(2)
    assertTrue(channel.read(dst) > 0)
    assertEquals(4, dst.position())
    assertEquals(0, channel.read(dst))
    dst.flip()
    assertEquals(0, dst.get(0))
    assertEquals(1, dst.get(2))
    assertEquals(2, dst.get(3))
  }

  @Test def newOutputStreamWritesFully(): Unit = {
    val sink = new ChunkedSink(1)
    val out = Channels.newOutputStream(sink)
    out.write(Array[Byte](1, 2, 3, 4), 1, 3)
    out.write(5)
    assertArrayEquals(Array[Byte](2, 3, 4, 5), sink.out.toByteArray())
  }

  @Test def newWriterWritesMoreThanBufferCapacity(): Unit = {
    assumeFalse(
      "JDK < 25 drops bytes on partial channel writes",
      Platform.executingInJVMOnLowerThanJDK(25)
    )
    val text = ("a" * 1000) + "书" + ("b" * 3000) + "😊"
    val sink = new ChunkedSink(7)
    val writer = Channels.newWriter(sink, "UTF-8")
    writer.write(text.substring(0, 1500))
    writer.write(text.substring(1500))
    writer.flush()
    assertArrayEquals(
      text.getBytes(StandardCharsets.UTF_8),
      sink.out.toByteArray()
    )
  }

  @Test def newWriterSurrogatePairSplitAcrossFlush(): Unit = {
    val sink = new ChunkedSink(16)
    val writer = Channels.newWriter(sink, "UTF-8")
    writer.write('\uD83D')
    writer.flush()
    writer.write('\uDE0A')
    writer.flush()
    assertArrayEquals(
      "😊".getBytes(StandardCharsets.UTF_8),
      sink.out.toByteArray()
    )
  }

  @Test def newWriterCloseFlushes(): Unit = {
    val sink = new ChunkedSink(16)
    val writer = Channels.newWriter(sink, "UTF-8")
    writer.write("hello")
    writer.close()
    assertFalse(sink.isOpen())
    assertArrayEquals(
      "hello".getBytes(StandardCharsets.UTF_8),
      sink.out.toByteArray()
    )
  }

  @Test def newReaderAndWriterAcceptUsedCoders(): Unit = {
    val enc = StandardCharsets.UTF_8.newEncoder()
    enc.encode(CharBuffer.wrap("x"))
    val sink = new ChunkedSink(16)
    val writer = Channels.newWriter(sink, enc, -1)
    writer.write("ok")
    writer.close()
    assertArrayEquals(
      "ok".getBytes(StandardCharsets.UTF_8),
      sink.out.toByteArray()
    )

    val dec = StandardCharsets.UTF_8.newDecoder()
    dec.decode(ByteBuffer.wrap("x".getBytes(StandardCharsets.UTF_8)))
    val reader =
      Channels.newReader(
        chunkedChannel("ok".getBytes(StandardCharsets.UTF_8), 16),
        dec,
        -1
      )
    assertEquals("ok", readAll(reader, 16))
  }

  @Test def newReaderHonoursMinBufferCap(): Unit = {
    var maxRequested = 0
    val ch = new ReadableByteChannel {
      def read(dst: ByteBuffer): Int = {
        maxRequested = maxRequested.max(dst.remaining())
        -1
      }
      def isOpen(): Boolean = true
      def close(): Unit = ()
    }
    val reader =
      Channels.newReader(ch, StandardCharsets.UTF_8.newDecoder(), 10000)
    assertEquals(-1, reader.read())
    assertTrue(s"requested $maxRequested", maxRequested >= 10000)
  }

  @Test def newWriterClosesChannelWhenFinalFlushFails(): Unit = {
    val sink = new ChunkedSink(16) {
      override def write(src: ByteBuffer): Int =
        throw new java.io.IOException("write failed")
    }
    val writer = Channels.newWriter(sink, "UTF-8")
    writer.write("hello")
    assertThrows(classOf[java.io.IOException], writer.close())
    // Older JDKs leave the channel open (JDK-8136895)
    if (!Platform.executingInJVM) assertFalse(sink.isOpen())
    writer.close()
  }
}
