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

  @Test def canWriteToChannelWithAppend(): Unit = {
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
}
