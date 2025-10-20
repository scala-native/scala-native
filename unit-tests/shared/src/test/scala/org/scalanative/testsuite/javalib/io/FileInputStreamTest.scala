package org.scalanative.testsuite.javalib.io

import java.io.*
import java.nio.file.{Files, Path}

import scala.util.Try

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.javalib.io.IoTestHelpers.withTemporaryDirectory

import org.scalanative.testsuite.utils.Platform.isWindows
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FileInputStreamTest {
  // On JVM new File(".") is not valid input file
  val file =
    if (isWindows) new File("NUL")
    else new File("/dev/null")

  @Test def readNull(): Unit = {
    val fis = new FileInputStream(file)
    assertThrows(classOf[NullPointerException], fis.read(null))
    assertThrows(classOf[NullPointerException], fis.read(null, 0, 0))
  }

  @Test def readOutOfBoundsNegativeCount(): Unit = {
    val fis = new FileInputStream(file)
    val arr = new Array[Byte](8)
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, 0, -1))
  }

  @Test def readOutOfBoundsNegativeOffset(): Unit = {
    val fis = new FileInputStream(file)
    val arr = new Array[Byte](8)
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, -1, 0))
  }

  @Test def readOutOfBoundsArrayTooSmall(): Unit = {
    val fis = new FileInputStream(file)
    val arr = new Array[Byte](8)
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, 0, 16))
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, 4, 8))
  }

  @Test def validFileDescriptorAndSyncSuccess(): Unit = {
    val file = File.createTempFile("fisfdtest", "")
    val fis = new FileInputStream(file)
    val fd = fis.getFD
    assertTrue(fd.valid())
    assertTrue(Try(fd.sync()).isSuccess)
    fis.close()
  }

  @Test def canRead0xffCorrectly(): Unit = {
    val file = File.createTempFile("file", ".tmp")
    val fos = new FileOutputStream(file)
    fos.write(0xff)
    fos.close()

    val fis = new FileInputStream(file)
    assertTrue(fis.read() == 0xff)
    assertTrue(fis.read() == -1)
    fis.close()
  }

  @Test def throwsWhenCreatingFileInputStreamWithNonExistentFilePath(): Unit = {
    assertThrows(
      classOf[FileNotFoundException],
      new FileInputStream("/the/path/does/not/exist/for/sure")
    )
  }

  @Test def available(): Unit = {
    withTemporaryDirectory { dir =>
      val f = dir.toPath().resolve("FisTestDataForMethodAvailable.utf-8")
      val str = "They were the best of us!"
      Files.write(f, str.getBytes("UTF-8"))

      val fis = new FileInputStream(f.toFile())
      try {
        // current position less than file size.
        assertEquals("available pos < size", str.length(), fis.available())
        // 2023-06-15 11:21 -0400 FIXME

        // move current position to > than file size.
        val channel = fis.getChannel()
        channel.position(str.length * 2) // two is an arbitrary value > 1
        assertEquals("available pos > size", 0, fis.available())
      } finally {
        fis.close()
      }
    }
  }

}
