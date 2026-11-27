package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.io.{FileOutputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.zip._

import org.junit.Assert._
import org.junit.Assume.assumeFalse
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower

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
    assertThrows(classOf[IllegalStateException], enumeration.nextElement())
    // JDK 8 also throws from hasMoreElements after close; JDK 17+ does not
    assumeFalse(executingInJVMOnJDK8OrLower)
    assertTrue(enumeration.hasMoreElements())
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

  @Test def getInputStreamNonAsciiNames(): Unit = {
    val data = "hello".getBytes(StandardCharsets.UTF_8)
    val file = Files.createTempFile("zip-non-ascii", ".zip").toFile()
    try {
      val out = new ZipOutputStream(new FileOutputStream(file))
      try {
        val stored = new ZipEntry("\u00e9.txt")
        val crc = new CRC32()
        crc.update(data)
        stored.setMethod(ZipEntry.STORED)
        stored.setSize(data.length)
        stored.setCrc(crc.getValue())
        out.putNextEntry(stored)
        out.write(data)
        out.closeEntry()
        out.putNextEntry(new ZipEntry("\u017c\u00f3\u0142w.txt"))
        out.write(data)
        out.closeEntry()
      } finally out.close()

      val zip = new ZipFile(file)
      try
        for (name <- Seq("\u00e9.txt", "\u017c\u00f3\u0142w.txt")) {
          val in = zip.getInputStream(zip.getEntry(name))
          try assertArrayEquals(name, data, in.readAllBytes())
          finally in.close()
        }
      finally zip.close()
    } finally file.delete()
  }
}
