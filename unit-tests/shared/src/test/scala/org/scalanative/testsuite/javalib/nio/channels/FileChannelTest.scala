package org.scalanative.testsuite.javalib.nio.channels

import java.nio.channels._

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}
import java.nio.file.AccessDeniedException
import java.io.File

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.io.{FileInputStream, FileOutputStream}
import java.io.RandomAccessFile

class FileChannelTest {
  @Test def fileChannelCanReadBufferFromFile(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      Files.write(f, bytes)
      assertTrue(Files.getAttribute(f, "size") == 5)

      val channel = FileChannel.open(f)
      val buffer = ByteBuffer.allocate(5)

      val bread = channel.read(buffer)
      buffer.flip()

      assertTrue(buffer.limit() == 5)
      assertTrue(buffer.position() == 0)
      assertTrue(bread == 5L)
      assertTrue(buffer.array() sameElements bytes)

      channel.close()
    }
  }

  @Test def fileChannelCanReadBuffersFromFile(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      Files.write(f, bytes)
      assertTrue(Files.getAttribute(f, "size") == 5)

      val channel = FileChannel.open(f)
      val bufferA = ByteBuffer.allocate(2)
      val bufferB = ByteBuffer.allocate(3)
      val buffers = Array[ByteBuffer](bufferA, bufferB)

      val bread = channel.read(buffers)
      bufferA.flip()
      bufferB.flip()

      assertTrue(bufferA.limit() == 2)
      assertTrue(bufferB.limit() == 3)
      assertTrue(bufferA.position() == 0)
      assertTrue(bufferB.position() == 0)

      assertTrue(bread == 5L)
      assertTrue(bufferA.array() sameElements Array[Byte](1, 2))
      assertTrue(bufferB.array() sameElements Array[Byte](3, 4, 5))

      channel.close()
    }
  }

  @Test def fileChannelCanWriteToFile(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      val src = ByteBuffer.wrap(bytes)
      val channel =
        FileChannel.open(f, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
      while (src.remaining() > 0) channel.write(src)

      val in = Files.newInputStream(f)
      var i = 0
      while (i < bytes.length) {
        assertTrue(in.read() == bytes(i))
        i += 1
      }

    }
  }

  @Test def fileChannelCanWriteReadOnlyByteBufferToFile(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      val src = ByteBuffer.wrap(bytes).asReadOnlyBuffer()
      val channel =
        FileChannel.open(f, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
      while (src.remaining() > 0) channel.write(src)

      val in = Files.newInputStream(f)
      var i = 0
      while (i < bytes.length) {
        assertTrue(in.read() == bytes(i))
        i += 1
      }

    }
  }

  @Test def fileChannelCanOverwriteFile(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("file")
      Files.write(f, "hello, world".getBytes("UTF-8"))

      val bytes = "goodbye".getBytes("UTF-8")
      val src = ByteBuffer.wrap(bytes)
      val channel =
        FileChannel.open(f, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
      while (src.remaining() > 0) channel.write(src)

      val in = Files.newInputStream(f)
      var i = 0
      while (i < bytes.length) {
        assertTrue(in.read() == bytes(i))
        i += 1
      }
    }
  }

  @Test def fileChannelWritesAtTheBeginningUnlessOtherwiseSpecified(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "abcdefgh".getBytes("UTF-8"))
      val lines = Files.readAllLines(f)
      assertTrue(lines.size() == 1)
      assertTrue(lines.get(0) == "abcdefgh")

      val c = FileChannel.open(f, StandardOpenOption.WRITE)
      val src = ByteBuffer.wrap("xyz".getBytes("UTF-8"))
      while (src.remaining() > 0) c.write(src)

      val newLines = Files.readAllLines(f)
      assertTrue(newLines.size() == 1)
      assertTrue(newLines.get(0) == "xyzdefgh")
    }
  }

  @Test def cannotCombineAppendAndTruncateExisting(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      assertThrows(
        classOf[IllegalArgumentException],
        FileChannel.open(
          f,
          StandardOpenOption.APPEND,
          StandardOpenOption.TRUNCATE_EXISTING
        )
      )
    }
  }

  @Test def cannotCombineAppendAndRead(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      assertThrows(
        classOf[IllegalArgumentException],
        FileChannel.open(f, StandardOpenOption.APPEND, StandardOpenOption.READ)
      )
    }
  }

  @Test def canRelativeWriteToChannelWithAppend(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "hello, ".getBytes("UTF-8"))

      val lines = Files.readAllLines(f)
      assertTrue(lines.size() == 1)
      assertTrue(lines.get(0) == "hello, ")

      val bytes = "world".getBytes("UTF-8")
      val src = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f, StandardOpenOption.APPEND)
      while (src.remaining() > 0) channel.write(src)

      val newLines = Files.readAllLines(f)
      assertTrue(newLines.size() == 1)
      assertTrue(newLines.get(0) == "hello, world")
    }
  }

  // Issue #3316
  @Test def canRepositionChannelThenRelativeWriteAppend(): Unit = {
    withTemporaryDirectory { dir =>
      val prefix = "Γειά "
      val suffix = "σου Κόσμε"
      val message = s"${prefix}${suffix}"

      val prefixBytes = prefix.getBytes("UTF-8") // Greek uses 2 bytes per char
      val suffixBytes = suffix.getBytes("UTF-8")

      val f = dir.resolve("rePositionThenAppend.txt")
      Files.write(f, prefixBytes)

      val lines = Files.readAllLines(f)
      assertEquals("lines size", 1, lines.size())
      assertEquals("lines content", prefix, lines.get(0))

      val channel = Files.newByteChannel(
        f,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND
      )

      try {
        // channel must start off positioned at EOF.
        val positionAtOpen = channel.position()
        assertEquals("position at open", channel.size(), positionAtOpen)

        /* Java 8 SeekableByteChannel description says:
         *   Setting the channel's position is not recommended when connected
         *   to an entity, typically a file, that is opened with the APPEND
         *   option.
         *
         * JVM re-inforces this caution by "position(pos)" on a channel
         * opened for APPEND silently not actually move the position; it is
         * a no-op.
         */
        channel.position(0L)

        assertEquals("reposition", positionAtOpen, channel.position())

        val src = ByteBuffer.wrap(suffixBytes)

        while (src.remaining() > 0)
          channel.write(src)

        val newLines = Files.readAllLines(f)
        assertEquals("Second lines size", 1, newLines.size())

        // Verify append happened at expected place; end of line, not beginning
        assertEquals("Second lines content", message, newLines.get(0))

      } finally {
        channel.close()
      }
    }
  }

  @Test def canAbsoluteWriteToChannel(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "hello, ".getBytes("UTF-8"))

      val lines = Files.readAllLines(f)
      assertTrue(lines.size() == 1)
      assertTrue(lines.get(0) == "hello, ")

      val bytes = "world".getBytes("UTF-8")
      val src = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f, StandardOpenOption.WRITE)

      try {
        val preWritePos = channel.position()
        assertEquals("pre-write position", 0, preWritePos)

        channel.write(src, 3)

        // Absolute write without APPEND should not move current position.
        assertEquals("post-write position", preWritePos, channel.position())

        val bytes2 = "%".getBytes("UTF-8")
        val src2 = ByteBuffer.wrap(bytes2)

        channel.write(src2)
      } finally channel.close()

      val newLines = Files.readAllLines(f)
      assertEquals("size", 1, newLines.size())
      assertEquals("content", "%elworld", newLines.get(0))
    }
  }

  @Test def canAbsoluteWriteToChannelWithAppend(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "hello, ".getBytes("UTF-8"))

      val lines = Files.readAllLines(f)
      assertTrue(lines.size() == 1)
      assertTrue(lines.get(0) == "hello, ")

      val bytes = "world".getBytes("UTF-8")
      val src = ByteBuffer.wrap(bytes)
      val channel = FileChannel.open(f, StandardOpenOption.APPEND)

      try {
        val preWritePos = channel.position()
        assertEquals("pre-write position", preWritePos, channel.size()) // EOF

        val nWritten = channel.write(src, 2) // write at absolute position
        assertEquals("bytes written", bytes.size, nWritten)

        /* Absolute write with APPEND uses a logical "current position" of EOF
         * not an absolute number qua position, such as 42.
         *
         * Using this understanding, the "current position" has not moved
         * from EOF, even though the absolute position has been updated
         * to the new EOF.
         */

        assertEquals("post-write position", channel.size(), channel.position())

        val bytes2 = "!".getBytes("UTF-8")
        val src2 = ByteBuffer.wrap(bytes2)

        channel.write(src2) // APPEND relative write should be at EOF.
      } finally channel.close()

      val newLines = Files.readAllLines(f)
      assertEquals("size", 1, newLines.size())

      /* Welcome to the realm of Ὀϊζύς (Oizys), goddess of misery,
       * anxiety, grief, depression, and misfortune.
       *
       * Skipping lightly over _lots_ of complexity, operating systems
       * and their file systems differ in allowing the absolute write or not.
       * Branching on all supported operating systems and each of _their_
       * file systems is simply not feasible.
       *
       * The important part is that the relative write happened at EOF
       * and the absolute write happened at a believable place, even
       * if the re-position of that write was a no-op.
       */

      val content = newLines.get(0)

      assertTrue(
        s"unexpected content '${content}'",
        (content == "heworld!") // write at absolute position happened
          || (content == "hello, world!") // write happed at EOF.
      )
    }
  }

  @Test def canMoveFilePointer(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, "hello".getBytes("UTF-8"))
      val channel = new RandomAccessFile(f.toFile(), "rw").getChannel()
      assertEquals(0, channel.position())
      channel.position(3)
      assertEquals(3, channel.position())
      channel.write(ByteBuffer.wrap("a".getBytes()))

      channel.close()

      val newLines = Files.readAllLines(f)
      assertTrue(newLines.size() == 1)
      assertTrue(newLines.get(0) == "helao")
    }
  }

  @Test def getChannelFromFileInputStreamCoherency(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)
      Files.write(f, bytes)
      val in = new FileInputStream(f.toString())
      val channel = in.getChannel()
      val read345 = ByteBuffer.allocate(3)

      in.read()
      in.read()
      channel.read(read345)

      var i = 2
      while (i < bytes.length) {
        assertEquals(f"Byte#$i", bytes(i), read345.get(i - 2))
        i += 1
      }
    }
  }

  @Test def getChannelFromFileOutputStreamCoherency(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val out = new FileOutputStream(f.toString())
      val channel = out.getChannel()

      val bytes = Array.apply[Byte](1, 2, 3, 4, 5)

      var i = 0
      while (i < 3) {
        out.write(bytes(i))
        i += 1
      }
      while (i < bytes.length) {
        channel.write(ByteBuffer.wrap(Array[Byte](bytes(i))))
        i += 1
      }
      channel.close()
      val readb = Files.readAllBytes(f)
      assertTrue(bytes sameElements readb)
    }
  }

  @Test def fileChannelThrowsAccessDeniedForReadOnly(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("file")
      Files.write(f, "hello, world".getBytes("UTF-8"))

      val sroStatus = f.toFile().setReadOnly()
      assertTrue("setReadOnly failed", sroStatus)

      assertThrows(
        f.toString(),
        classOf[AccessDeniedException],
        FileChannel.open(f, StandardOpenOption.WRITE)
      )
    }
  }

  def withTemporaryDirectory(fn: Path => Unit): Unit = {
    val file = File.createTempFile("test", ".tmp")
    assertTrue(file.delete())
    assertTrue(file.mkdir())
    fn(file.toPath)
  }

  @Test def mapMethodIsTidy(): Unit = {
    withTemporaryDirectory { dir =>
      val data = s"abcdef"
      val dataBytes = data.getBytes("UTF-8")

      val f = dir.resolve("mapArguments.txt")
      Files.write(f, dataBytes)

      val lines = Files.readAllLines(f)
      assertEquals("lines size", 1, lines.size())
      assertEquals("lines content", data, lines.get(0))

      val channel = FileChannel.open(
        f,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE
      )

      try {
        // Fails where it should
        assertThrows(
          classOf[IllegalArgumentException],
          channel.map(FileChannel.MapMode.READ_WRITE, -1, 0)
        )

        assertThrows(
          classOf[IllegalArgumentException],
          channel.map(FileChannel.MapMode.READ_WRITE, 0, -2)
        )

        assertThrows(
          classOf[IllegalArgumentException],
          channel.map(FileChannel.MapMode.READ_WRITE, 0, Integer.MAX_VALUE + 1)
        )

        // succeeds where it should
        val mappedChan = channel.map(
          FileChannel.MapMode.READ_WRITE,
          0,
          dataBytes.size
        ) // for this test, must be > 0.
        val offset = 2 // two is an arbitrary non-zero position in range.
        assertEquals("mappedChan", dataBytes(offset), mappedChan.get(offset))
      } finally {
        channel.close()
      }
    }
  }

  // Issue #3340
  @Test def mapMethodMapZeroBytes(): Unit = {
    withTemporaryDirectory { dir =>
      val data = s"ABCDEF"
      val dataBytes = data.getBytes("UTF-8")

      val f = dir.resolve("mapZeroBytes.txt")
      Files.write(f, dataBytes)

      val lines = Files.readAllLines(f)
      assertEquals("lines size", 1, lines.size())
      assertEquals("lines content", data, lines.get(0))

      val channel = FileChannel.open(
        f,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE
      )

      try {
        val mappedChan = channel.map(FileChannel.MapMode.READ_WRITE, 0, 0)

        assertThrows(
          classOf[java.lang.IndexOutOfBoundsException],
          mappedChan.get(0)
        )

      } finally {
        channel.close()
      }
    }
  }

  @Test def cannotTruncateChannelUsingNegativeSize(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("negativeSize.txt")

      val channel = Files.newByteChannel(
        f,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
      )

      try {
        assertThrows(
          classOf[IllegalArgumentException],
          channel.truncate(-1)
        )
      } finally {
        channel.close()
      }
    }
  }

  @Test def cannotTruncateChannelOpenedReadOnly(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.resolve("truncateReadOnly.txt")
      Files.write(f, "".getBytes("UTF-8")) // "touch" file so it gets created

      val channel = Files.newByteChannel(
        f,
        StandardOpenOption.CREATE,
        StandardOpenOption.READ
      )

      try {
        assertThrows(
          classOf[NonWritableChannelException],
          channel.truncate(0)
        )
      } finally
        channel.close()
    }
  }

  @Test def canTruncateChannelOpenForWrite(): Unit = {
    withTemporaryDirectory { dir =>
      val prefix = "Γειά "
      val suffix = "σου Κόσμε"
      val message = s"${prefix}${suffix}"

      val f = dir.resolve("truncateChannelOpenForWrite.txt")
      Files.write(f, message.getBytes("UTF-8"))

      val lines = Files.readAllLines(f)
      assertEquals("lines size", 1, lines.size())
      assertEquals("lines content", message, lines.get(0))

      val channel = Files.newByteChannel(
        f,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
      )

      try {
        val startingSize = channel.size()

        assertEquals(
          "starting size UTF-8",
          startingSize,
          message.getBytes("UTF-8").size
        )

        // channel must start off positioned at beginning of file.
        assertEquals("position at open", 0, channel.position())

        val workingPos = 9L // arbitrary mid-range pos; gives room to move
        channel.position(workingPos)
        assertEquals("first re-position", workingPos, channel.position())

        // Truncate to size greater than current position
        val gtTruncateSize = workingPos + 20
        channel.truncate(gtTruncateSize)
        assertEquals("gtTruncate size", startingSize, channel.size())
        assertEquals("gtTruncate position", workingPos, channel.position())

        // Truncate to size equal to current position
        val eqTruncateSize = workingPos
        channel.truncate(eqTruncateSize)
        assertEquals("eqTruncate size", eqTruncateSize, channel.size())
        assertEquals("eqTruncate position", workingPos, channel.position())

        // Truncate to size less than current position
        val ltTruncateSize = workingPos - 2
        channel.truncate(ltTruncateSize)
        assertEquals("ltTruncate size", ltTruncateSize, channel.size())
        assertEquals("ltTruncate position", ltTruncateSize, channel.position())

      } finally {
        channel.close()
      }
    }
  }

  @Test def canTruncateChannelOpenForAppend(): Unit = {
    withTemporaryDirectory { dir =>
      val prefix = "Γειά "
      val suffix = "σου Κόσμε"
      val message = s"${prefix}${suffix}"

      val f = dir.resolve("truncateChannelOpenForAppend.txt")
      Files.write(f, message.getBytes("UTF-8"))

      val lines = Files.readAllLines(f)
      assertEquals("lines size", 1, lines.size())
      assertEquals("lines content", message, lines.get(0))

      val channel = Files.newByteChannel(
        f,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND
      )

      try {
        val startingSize = channel.size()

        // channel must start off positioned at EOF.
        val positionAtOpen = channel.position()
        assertEquals("position at open", startingSize, positionAtOpen)

        // Truncate to size greater than current position
        val gtTruncateSize = startingSize + 20
        channel.truncate(gtTruncateSize)
        assertEquals("gtTruncate size", startingSize, channel.size())
        assertEquals("gtTruncate position", positionAtOpen, channel.position())

        // Truncate to size equal to current position
        val eqTruncateSize = startingSize
        channel.truncate(eqTruncateSize)
        assertEquals("eqTruncate size", eqTruncateSize, channel.size())
        assertEquals("eqTruncate position", positionAtOpen, channel.position())

        val ltTruncateSize = startingSize - 3
        channel.truncate(ltTruncateSize)
        assertEquals("ltTruncate size", ltTruncateSize, channel.size())
        assertEquals("ltTruncate position", ltTruncateSize, channel.position())

      } finally {
        channel.close()
      }
    }
  }

}
