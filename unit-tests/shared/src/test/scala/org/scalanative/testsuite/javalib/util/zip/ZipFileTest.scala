package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.io.{FileOutputStream, InputStream}
import java.nio.file.Files
import java.util.zip._

import org.junit.Assert._
import org.junit.Test

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

  @Test def opensEmptyArchive(): Unit = {
    // EOCD-only archive: zero entries, zero comment, zero disks.
    val emptyArchive: Array[Byte] = Array(
      0x50.toByte,
      0x4b.toByte,
      0x05.toByte,
      0x06.toByte, // EOCD signature
      0,
      0, // disk number
      0,
      0, // disk with start of CD
      0,
      0, // entries on this disk
      0,
      0, // total entries
      0,
      0,
      0,
      0, // CD size
      0,
      0,
      0,
      0, // CD offset
      0,
      0 // comment length
    )
    val file = Files.createTempFile("empty", ".zip").toFile
    try {
      val fos = new FileOutputStream(file)
      try fos.write(emptyArchive)
      finally fos.close()

      val zip = new ZipFile(file)
      try {
        assertEquals(0, zip.size())
        assertFalse(zip.entries().hasMoreElements)
      } finally zip.close()
    } finally file.delete()
  }

  // getInputStream used to skip the LFH file-name field using
  // `String.length` (UTF-16 code units), so any entry whose name contained
  // multi-byte UTF-8 characters mis-aligned the inflater and read garbage
  // or threw ZipException.
  @Test def getInputStreamWithNonAsciiEntryName(): Unit = {
    val zipPath = Files.createTempFile("scala-native-utf8-entry", ".zip")
    try {
      // 4 Cyrillic chars (2 bytes each in UTF-8) + 4 ASCII = 12 UTF-8 bytes
      // vs 8 UTF-16 code units — a 4-byte skew that breaks the inflater.
      val entryName = "Тест.nir"
      val payload =
        "the quick brown fox jumps over the lazy dog".getBytes("UTF-8")

      val zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile))
      try {
        zos.putNextEntry(new ZipEntry(entryName))
        zos.write(payload)
        zos.closeEntry()
      } finally {
        zos.close()
      }

      val zf = new ZipFile(zipPath.toFile)
      try {
        val entry = zf.getEntry(entryName)
        assertNotNull("entry not found by non-ASCII name", entry)

        val in = zf.getInputStream(entry)
        try {
          val out = new java.io.ByteArrayOutputStream()
          val buf = new Array[Byte](256)
          var n = in.read(buf)
          while (n > 0) {
            out.write(buf, 0, n)
            n = in.read(buf)
          }
          assertArrayEquals(payload, out.toByteArray)
        } finally {
          in.close()
        }
      } finally {
        zf.close()
      }
    } finally {
      Files.deleteIfExists(zipPath)
    }
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
