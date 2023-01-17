package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip._
import java.io.InputStream

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper._

import ZipBytes._

class ZipFileTest {

  @Test def constructorFile(): Unit = {
    val file = getFile(zipFile)
    val zip = new ZipFile(file)
    assertTrue(file.exists())
    zip.close()
  }

  @Test def constructorFileInt(): Unit = {
    val file = getFile(zipFile)
    val zip = new ZipFile(file, ZipFile.OPEN_DELETE | ZipFile.OPEN_READ)
    zip.close()
    assertTrue(!file.exists())

    assertThrows(
      classOf[IllegalArgumentException], {
        val error = 3
        new ZipFile(file, error)
      }
    )
  }

  @Test def close(): Unit = {
    val zip = getZipFile(zipFile)
    zip.close()
    assertThrows(
      classOf[IllegalStateException],
      zip.getInputStream(zip.getEntry("ztest/file1.txt"))
    )
  }

  @Test def entries(): Unit = {
    val zip = getZipFile(zipFile)
    val enumer = zip.entries()
    var c = 0
    while (enumer.hasMoreElements()) {
      c += 1
      enumer.nextElement()
    }
    assertTrue(c == 6)

    val enumeration = zip.entries()
    zip.close()
    assumeNotJVMCompliant()
    // Behaviour of hasMoreElements might differ across version.
    // In Java 8 it would throw IllegalStateException,
    // In Java 15 it would not throw any exception
    assertThrows(classOf[IllegalStateException], enumeration.hasMoreElements())
  }

  @Test def getEntryString(): Unit = {
    val zip = getZipFile(zipFile)
    var zentry = zip.getEntry("File1.txt")
    assertTrue(zentry != null)

    zentry = zip.getEntry("testdir1/File1.txt")
    assertTrue(zentry != null)

    var r = 0
    var in: InputStream = null
    zentry = zip.getEntry("testdir1/")
    assertTrue(zentry != null)
    in = zip.getInputStream(zentry)
    assertTrue(in != null)
    r = in.read()
    in.close()
    assertTrue(r == -1)

    zentry = zip.getEntry("testdir1")
    assertTrue(zentry != null)
    in = zip.getInputStream(zentry)
    r = in.read()
    in.close()
    assertTrue(r == -1)

    zentry = zip.getEntry("testdir1/testdir1")
    assertTrue(zentry != null)
    in = zip.getInputStream(zentry)
    val buf = new Array[Byte](256)
    r = in.read(buf)
    assertTrue(new String(buf, 0, r, "UTF-8") == "This is also text")
  }

  @Test def getEntryStringThrowsAnExceptionWhenTheZipIsClosed(): Unit = {
    val zip = getZipFile(zipFile)
    val zentry = zip.getEntry("File1.txt")
    assertTrue(zentry != null)

    zip.close()
    assertThrows(classOf[IllegalStateException], zip.getEntry("File2.txt"))
  }

  @Test def getInputStreamZipEntry(): Unit = {
    val zip = getZipFile(zipFile)
    var is: InputStream = null

    try {
      val zentry = zip.getEntry("File1.txt")
      is = zip.getInputStream(zentry)
      val rbuf = new Array[Byte](1000)
      var r = zentry.getSize().toInt
      is.read(rbuf, 0, r)
      assertTrue(new String(rbuf, 0, r, "UTF-8") == "This is text")
    } finally {
      is.close()
    }
  }

  @Test def size(): Unit = {
    val zip = getZipFile(zipFile)
    assertTrue(zip.size() == 6)
  }
}
