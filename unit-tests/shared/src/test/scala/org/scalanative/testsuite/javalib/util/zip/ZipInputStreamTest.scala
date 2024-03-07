package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper._

import java.io._
import java.util.zip._

import ZipBytes.{brokenManifestBytes, zipFile}

class ZipInputStreamTest {

  private var zis: ZipInputStream = null
  private var zipBytes: Array[Byte] = null
  private val dataBytes: Array[Byte] = "Some data in my file".getBytes()
  private var zip: ZipInputStream = null
  private var zentry: ZipEntry = null

  @Test def constructorInputStream(): Unit = {
    zentry = zis.getNextEntry()
    zis.closeEntry()
  }

  @Test def close(): Unit = {
    zis.close()
    val rbuf = new Array[Byte](10)

    assertThrows(classOf[IOException], zis.read(rbuf, 0, 1))
  }

  @Test def closeCanBeCalledSeveralTimes(): Unit = {
    zis.close()
    zis.close()
  }

  @Test def closeEntry(): Unit = {
    zentry = zis.getNextEntry()
    zis.closeEntry()
  }

  @Test def closeAfterException(): Unit = {
    val bis = new ByteArrayInputStream(brokenManifestBytes)
    val zis1 = new ZipInputStream(bis)

    assertThrows(
      classOf[ZipException], {
        var i = 0
        while (i < 6) {
          zis1.getNextEntry()
          i += 1
        }
      }
    )

    zis1.close()
    assertThrows(classOf[IOException], zis1.getNextEntry())
  }

  @Test def getNextEntry(): Unit = {
    assertTrue(zis.getNextEntry() != null)
  }

  @Test def readArrayByteIntInt(): Unit = {
    zentry = zis.getNextEntry()
    val rbuf = new Array[Byte](zentry.getSize().toInt)
    val r = zis.read(rbuf, 0, rbuf.length)
    new String(rbuf, 0, r)
    assertTrue(r == 12)
  }

  @Test def readOnlyByteEachTime(): Unit = {
    val in = new FilterInputStream(new ByteArrayInputStream(zipFile)) {
      override def read(buffer: Array[Byte], offset: Int, count: Int): Int =
        super.read(buffer, offset, 1) // one byte at a time

      override def read(buffer: Array[Byte]): Int =
        super.read(buffer, 0, 1) // one byte at a time
    }

    zis = new ZipInputStream(in)
    while ({ zentry = zis.getNextEntry(); zentry != null }) {
      zentry.getName()
    }
    zis.close()
  }

  @Test def skipLong(): Unit = {
    zentry = zis.getNextEntry()
    val rbuf = new Array[Byte](zentry.getSize().toInt)
    zis.skip(2)
    val r = zis.read(rbuf, 0, rbuf.length)
    assertTrue(r == 10)

    zentry = zis.getNextEntry()
    zentry = zis.getNextEntry()
    val s = zis.skip(1025)
    assertTrue(s == 1025)

    val zis2 = new ZipInputStream(new ByteArrayInputStream(zipBytes))
    zis2.getNextEntry()
    val skipLen = dataBytes.length / 2
    assertTrue(skipLen == zis2.skip(skipLen))
    zis2.skip(dataBytes.length)
    assertTrue(0 == zis2.skip(1))
    assertTrue(0 == zis2.skip(0))
    assertThrows(classOf[IllegalArgumentException], zis2.skip(-1))
  }

  @Test def available(): Unit = {
    assumeNotJVMCompliant()
    val zis1 = new ZipInputStream(new ByteArrayInputStream(zipFile))
    val entry = zis1.getNextEntry()
    assertTrue(entry != null)
    val entrySize = entry.getSize()
    assertTrue(entrySize > 0)

    var i = 0
    while (zis1.available() > 0) {
      zis1.skip(1)
      i += 1
    }
    assertEquals(entrySize, i)
    assertEquals(0, zis1.skip(1))
    assertEquals(0, zis1.available())
    zis1.closeEntry()
    assertTrue(zis.available() == 1)
    zis1.close()

    assertThrows(classOf[IOException], zis1.available())
  }

  @Before
  def setUp(): Unit = {
    zis = new ZipInputStream(new ByteArrayInputStream(zipFile))
    val bos = new ByteArrayOutputStream()
    val zos = new ZipOutputStream(bos)
    val entry = new ZipEntry("myFile")
    zos.putNextEntry(entry)
    zos.write(dataBytes)
    zos.closeEntry()
    zos.close()
    zipBytes = bos.toByteArray()
  }
}
