package org.scalanative.testsuite.javalib.nio.channels

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.channels.{Channels, ClosedChannelException}
import java.nio.charset.StandardCharsets
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

}
