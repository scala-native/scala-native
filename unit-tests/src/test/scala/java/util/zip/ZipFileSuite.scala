package java.util.zip

// Ported from Apache Harmony

import java.io.InputStream

import ZipBytes._

object ZipFileSuite extends tests.Suite {

  test("Constructor(File)") {
    val file = getFile(zipFile)
    val zip  = new ZipFile(file)
    assert(file.exists())
    zip.close()
  }

  test("Constructor(File, Int)") {
    val file = getFile(zipFile)
    val zip  = new ZipFile(file, ZipFile.OPEN_DELETE | ZipFile.OPEN_READ)
    zip.close()
    assert(!file.exists())

    assertThrows[IllegalArgumentException] {
      val error = 3
      new ZipFile(file, error)
    }
  }

  test("close()") {
    val zip = getZipFile(zipFile)
    zip.close()
    assertThrows[IllegalStateException] {
      zip.getInputStream(zip.getEntry("ztest/file1.txt"))
    }
  }

  test("entries()") {
    val zip    = getZipFile(zipFile)
    val enumer = zip.entries()
    var c      = 0
    while (enumer.hasMoreElements()) {
      c += 1
      enumer.nextElement()
    }
    assert(c == 6)

    val enumeration = zip.entries()
    zip.close()
    assertThrows[IllegalStateException] {
      enumeration.hasMoreElements()
    }
  }

  test("getEntry(String)") {
    val zip    = getZipFile(zipFile)
    var zentry = zip.getEntry("File1.txt")
    assert(zentry != null)

    zentry = zip.getEntry("testdir1/File1.txt")
    assert(zentry != null)

    var r               = 0
    var in: InputStream = null
    zentry = zip.getEntry("testdir1/")
    assert(zentry != null)
    in = zip.getInputStream(zentry)
    assert(in != null)
    r = in.read()
    in.close()
    assert(r == -1)

    zentry = zip.getEntry("testdir1")
    assert(zentry != null)
    in = zip.getInputStream(zentry)
    r = in.read()
    in.close()
    assert(r == -1)

    zentry = zip.getEntry("testdir1/testdir1")
    assert(zentry != null)
    in = zip.getInputStream(zentry)
    val buf = new Array[Byte](256)
    r = in.read(buf)
    assert(new String(buf, 0, r, "UTF-8") == "This is also text")
  }

  test("getEntry(String) throws an exception when the zip is closed") {
    val zip    = getZipFile(zipFile)
    val zentry = zip.getEntry("File1.txt")
    assert(zentry != null)

    zip.close()
    assertThrows[IllegalStateException] {
      zip.getEntry("File2.txt")
    }
  }

  test("getInputStream(ZipEntry)") {
    val zip             = getZipFile(zipFile)
    var is: InputStream = null

    try {
      val zentry = zip.getEntry("File1.txt")
      is = zip.getInputStream(zentry)
      val rbuf = new Array[Byte](1000)
      var r    = zentry.getSize().toInt
      is.read(rbuf, 0, r)
      assert(new String(rbuf, 0, r, "UTF-8") == "This is text")
    } finally {
      is.close()
    }
  }

  test("size()") {
    val zip = getZipFile(zipFile)
    assert(zip.size() == 6)
  }
}
