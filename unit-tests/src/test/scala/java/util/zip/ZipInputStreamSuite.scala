package java.util.zip

// Ported from Apache Harmony

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream,
  FilterInputStream,
  IOException
}

import ZipBytes.{brokenManifestBytes, zipFile}

object ZipInputStreamSuite extends tests.Suite {

  private var zis: ZipInputStream    = null
  private var zipBytes: Array[Byte]  = null
  private val dataBytes: Array[Byte] = "Some data in my file".getBytes()
  private var zip: ZipInputStream    = null
  private var zentry: ZipEntry       = null

  test("Constructor(InputStream)") {
    setUp()
    zentry = zis.getNextEntry()
    zis.closeEntry()
  }

  test("close()") {
    setUp()
    zis.close()
    val rbuf = new Array[Byte](10)

    assertThrows[IOException] {
      zis.read(rbuf, 0, 1)
    }
  }

  test("close() can be called several times") {
    setUp()
    zis.close()
    zis.close()
  }

  test("closeEntry()") {
    setUp()
    zentry = zis.getNextEntry()
    zis.closeEntry()
  }

  test("close() after exception") {
    val bis  = new ByteArrayInputStream(brokenManifestBytes)
    val zis1 = new ZipInputStream(bis)

    assertThrows[ZipException] {
      var i = 0
      while (i < 6) {
        zis1.getNextEntry()
        i += 1
      }
    }

    zis1.close()
    assertThrows[IOException] {
      zis1.getNextEntry()
    }
  }

  test("getNextEntry()") {
    setUp()
    assert(zis.getNextEntry() != null)
  }

  test("read(Array[Byte], Int, Int)") {
    setUp()
    zentry = zis.getNextEntry()
    val rbuf = new Array[Byte](zentry.getSize().toInt)
    val r    = zis.read(rbuf, 0, rbuf.length)
    new String(rbuf, 0, r)
    assert(r == 12)
  }

  test("Read only byte at a time") {
    setUp()
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

  test("skip(Long)") {
    setUp()
    zentry = zis.getNextEntry()
    val rbuf = new Array[Byte](zentry.getSize().toInt)
    zis.skip(2)
    val r = zis.read(rbuf, 0, rbuf.length)
    assert(r == 10)

    zentry = zis.getNextEntry()
    zentry = zis.getNextEntry()
    val s = zis.skip(1025)
    assert(s == 1025)

    val zis2 = new ZipInputStream(new ByteArrayInputStream(zipBytes))
    zis2.getNextEntry()
    val skipLen = dataBytes.length / 2
    assert(skipLen == zis2.skip(skipLen))
    zis2.skip(dataBytes.length)
    assert(0 == zis2.skip(1))
    assert(0 == zis2.skip(0))
    assertThrows[IllegalArgumentException] {
      zis2.skip(-1)
    }
  }

  test("available()") {
    setUp()
    val zis1  = new ZipInputStream(new ByteArrayInputStream(zipFile))
    val entry = zis1.getNextEntry()
    assert(entry != null)
    val entrySize = entry.getSize()
    assert(entrySize > 0)

    var i = 0
    while (zis1.available() > 0) {
      zis1.skip(1)
      i += 1
    }
    assert(i == entrySize)
    assert(zis1.skip(1) == 0)
    assert(zis1.available() == 0)
    zis1.closeEntry()
    assert(zis.available() == 1)
    zis1.close()

    assertThrows[IOException] {
      zis1.available()
    }
  }

  private def setUp() {
    zis = new ZipInputStream(new ByteArrayInputStream(zipFile))

    val bos   = new ByteArrayOutputStream()
    val zos   = new ZipOutputStream(bos)
    val entry = new ZipEntry("myFile")
    zos.putNextEntry(entry)
    zos.write(dataBytes)
    zos.closeEntry()
    zos.close()
    zipBytes = bos.toByteArray()
  }

}
