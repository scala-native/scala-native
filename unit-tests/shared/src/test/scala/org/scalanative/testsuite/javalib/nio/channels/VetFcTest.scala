package org.scalanative.testsuite.javalib.nio.channels

import java.nio.channels._

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, StandardOpenOption}
import java.nio.file.AccessDeniedException
import java.io.File

import org.junit.Test
import org.junit.Assert._
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.io.{FileInputStream, FileOutputStream}
import java.io.RandomAccessFile

class VetFileChannelTest {
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

        val mappedChan0 = channel.map(FileChannel.MapMode.READ_WRITE, 0, 0)

        assertThrows(
          classOf[java.lang.IndexOutOfBoundsException],
          mappedChan0.get(0)
        )

      } finally {
        channel.close()
      }
    }
  }

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
        // FIXME: Succeeds on JVM, fails on unix, ??? on Windows
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

}
