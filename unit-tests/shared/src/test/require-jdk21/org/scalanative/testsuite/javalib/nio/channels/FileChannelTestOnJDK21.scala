package org.scalanative.testsuite.javalib.nio.channels

import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardOpenOption}

import org.junit.Assert._
import org.junit.Test

// Before JDK 21, transferFrom past the end of file transferred nothing.
class FileChannelTestOnJDK21 {

  private def withTemporaryDirectory(fn: java.nio.file.Path => Unit): Unit =
    fn(Files.createTempDirectory("FileChannelTestOnJDK21"))

  @Test def transferFromPastEndOfFileGrowsFile(): Unit = {
    withTemporaryDirectory { dir =>
      val path = dir.resolve("f")
      Files.write(path, "0123456789".getBytes("UTF-8"))
      val src = FileChannel.open(path, StandardOpenOption.READ)
      val channel = FileChannel.open(path, StandardOpenOption.WRITE)
      try {
        assertEquals("transferred", 5L, channel.transferFrom(src, 100L, 5L))
        assertEquals("size", 105L, channel.size())
        assertEquals("position", 0L, channel.position())
      } finally {
        channel.close()
        src.close()
      }
      val bytes = Files.readAllBytes(path)
      assertEquals("01234", new String(bytes, 100, 5, StandardCharsets.UTF_8))
    }
  }
}
