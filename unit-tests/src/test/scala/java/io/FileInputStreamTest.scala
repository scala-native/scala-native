package java.io

import scala.util.Try

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class FileInputStreamTest {
  @Test def readNull(): Unit = {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    assertThrows(classOf[NullPointerException], fis.read(null))
    assertThrows(classOf[NullPointerException], fis.read(null, 0, 0))
  }

  @Test def readOutOfBoundsNegativeCount(): Unit = {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, 0, -1))
  }

  @Test def readOutOfBoundsNegativeOffset(): Unit = {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, -1, 0))
  }

  @Test def readOutOfBoundsArrayTooSmall(): Unit = {
    val file = new File(".")
    val fis  = new FileInputStream(file)
    val arr  = new Array[Byte](8)
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, 0, 16))
    assertThrows(classOf[IndexOutOfBoundsException], fis.read(arr, 4, 8))
  }

  @Test def validFileDescriptorAndSyncSuccess(): Unit = {
    val file = File.createTempFile("fisfdtest", "")
    val fis  = new FileInputStream(file)
    val fd   = fis.getFD
    assertTrue(fd.valid())
    assertTrue(Try(fd.sync()).isSuccess)
    fis.close()
  }

  @Test def canRead0xffCorrectly(): Unit = {
    val file = File.createTempFile("file", ".tmp")
    val fos  = new FileOutputStream(file)
    fos.write(0xFF)
    fos.close()

    val fis = new FileInputStream(file)
    assertTrue(fis.read() == 0xFF)
    assertTrue(fis.read() == -1)
    fis.close()
  }

  @Test def throwsWhenCreatingFileInputStreamWithNonExistentFilePath(): Unit = {
    assertThrows(classOf[FileNotFoundException],
                 new FileInputStream("/the/path/does/not/exist/for/sure"))
  }
}
