package java.util.zip

// Ported from Apache Harmony

import ZipBytes._

object ZipEntrySuite extends tests.Suite {

  var zfile: ZipFile          = null
  var zentry: ZipEntry        = null
  var orgSize: Long           = 0L
  var orgCompressedSize: Long = 0L
  var orgCrc: Long            = 0L
  var orgTime: Long           = 0L
  var orgComment: String      = null

  test("Constructor(String)") {
    setUp()
    zentry = zfile.getEntry("File3.txt")
    assert(zentry != null)

    assertThrows[NullPointerException] {
      zfile.getEntry(null)
    }
    val s = new StringBuffer()
    var i = 0
    while (i < 65535) {
      s.append('a')
      i += 1
    }

    new ZipEntry(s.toString)

    s.append('a')
    assertThrows[IllegalArgumentException] {
      new ZipEntry(s.toString())
    }
  }

  test("Constructor(ZipEntry)") {
    setUp()
    zentry.setSize(2)
    zentry.setCompressedSize(4)
    zentry.setComment("Testing")

    val zentry2 = new ZipEntry(zentry)
    assert(zentry2.getSize() == 2)
    assert(zentry2.getComment() == "Testing")
    assert(zentry2.getCompressedSize() == 4)
    assert(zentry2.getCrc() == orgCrc)
    assert(zentry2.getTime() == orgTime)
  }

  test("getComment()") {
    val zipEntry = new ZipEntry("zippy.zip")
    assert(zipEntry.getComment() == null)
    zipEntry.setComment("This Is A Comment")
    assert(zipEntry.getComment() == "This Is A Comment")
  }

  test("getCompressedSize()") {
    setUp()
    assert(zentry.getCompressedSize() == orgCompressedSize)
  }

  test("getCrc()") {
    setUp()
    assert(zentry.getCrc() == orgCrc)
  }

  test("getExtra()") {
    setUp()
    assert(zentry.getExtra() == null)
    val ba = Array[Byte]('T', 'E', 'S', 'T')
    zentry = new ZipEntry("test.tst")
    zentry.setExtra(ba)
    assert(zentry.getExtra() == ba)
  }

  test("getMethod()") {
    setUp()

    zentry = zfile.getEntry("File1.txt")
    assert(zentry.getMethod() == ZipEntry.STORED)

    zentry = zfile.getEntry("File3.txt")
    assert(zentry.getMethod() == ZipEntry.DEFLATED)

    zentry = new ZipEntry("test.tst")
    assert(zentry.getMethod() == -1)
  }

  test("getName()") {
    setUp()
    assert(zentry.getName() == "File1.txt")
  }

  test("getSize()") {
    setUp()
    assert(zentry.getSize() == orgSize)
  }

  test("getTime()") {
    setUp()
    assert(zentry.getTime() == orgTime)
  }

  test("isDirectory()") {
    setUp()
    assert(!zentry.isDirectory())
    zentry = new ZipEntry("Directory/")
    assert(zentry.isDirectory())
  }

  test("setComment(String)") {
    setUp()
    zentry = zfile.getEntry("File1.txt")
    zentry.setComment("Set comment using api")
    assert(zentry.getComment() == "Set comment using api")
    zentry.setComment(null)
    assert(zentry.getComment() == null)
    val s = new StringBuffer()
    var i = 0
    while (i < 0xFFFF) {
      s.append('a')
      i += 1
    }
    zentry.setComment(s.toString)

    s.append('a')
    assertThrows[IllegalArgumentException] {
      zentry.setComment(s.toString)
    }
  }

  test("setCompressedSize(Long)") {
    setUp()
    zentry.setCompressedSize(orgCompressedSize + 10)
    assert(zentry.getCompressedSize() == orgCompressedSize + 10)

    zentry.setCompressedSize(0)
    assert(zentry.getCompressedSize() == 0)

    zentry.setCompressedSize(-25)
    assert(zentry.getCompressedSize() == -25)

    zentry.setCompressedSize(4294967296L)
    assert(zentry.getCompressedSize() == 4294967296L)
  }

  test("setCrc(Long)") {
    setUp()
    zentry.setCrc(orgCrc + 100)
    assert(zentry.getCrc == orgCrc + 100)

    zentry.setCrc(0)
    assert(zentry.getCrc == 0)

    assertThrows[IllegalArgumentException] {
      zentry.setCrc(-25)
    }

    zentry.setCrc(4294967295L)
    assert(zentry.getCrc == 4294967295L)

    assertThrows[IllegalArgumentException] {
      zentry.setCrc(4294967296L)
    }
  }

  test("setExtra(Array[Byte])") {
    setUp()
    zentry = zfile.getEntry("File1.txt")
    zentry.setExtra("Test setting extra information".getBytes())
    assert(
      new String(zentry.getExtra(), 0, zentry.getExtra().length) == "Test setting extra information")

    zentry = new ZipEntry("test.tst")
    var ba = new Array[Byte](0xFFFF)
    zentry.setExtra(ba)
    assert(zentry.getExtra() == ba)

    assertThrows[IllegalArgumentException] {
      ba = new Array[Byte](0xFFFF + 1)
      zentry.setExtra(ba)
    }

    val zeInput = new ZipEntry("InputZip")
    val extraB  = Array[Byte]('a', 'b', 'd', 'e')
    zeInput.setExtra(extraB)
    assert(extraB == zeInput.getExtra())
    assert(extraB(3) == zeInput.getExtra()(3))
    assert(extraB.length == zeInput.getExtra().length)

    val zeOutput = new ZipEntry(zeInput)
    assert(zeInput.getExtra()(3) == zeOutput.getExtra()(3))
    assert(zeInput.getExtra().length == zeOutput.getExtra().length)
    assert(extraB(3) == zeOutput.getExtra()(3))
    assert(extraB.length == zeOutput.getExtra().length)
  }

  test("setMethod(Int)") {
    setUp()
    zentry = zfile.getEntry("File3.txt")
    zentry.setMethod(ZipEntry.STORED)
    assert(zentry.getMethod() == ZipEntry.STORED)

    zentry.setMethod(ZipEntry.DEFLATED)
    assert(zentry.getMethod() == ZipEntry.DEFLATED)

    val error = 1
    assertThrows[IllegalArgumentException] {
      zentry = new ZipEntry("test.tst")
      zentry.setMethod(error)
    }
  }

  test("setSize(Long)") {
    setUp()
    zentry.setSize(orgSize + 10)
    assert(zentry.getSize() == orgSize + 10)

    zentry.setSize(0)
    assert(zentry.getSize() == 0)

    assertThrows[IllegalArgumentException] {
      zentry.setSize(-25)
    }

    zentry.setSize(4294967295L)

    assertThrows[IllegalArgumentException] {
      zentry.setSize(4294967296L)
    }
  }

  private def setUp(): Unit = {
    zfile = getZipFile(zipFile)
    zentry = zfile.getEntry("File1.txt")
    orgSize = zentry.getSize()
    orgCompressedSize = zentry.getCompressedSize()
    orgCrc = zentry.getCrc()
    orgTime = zentry.getTime()
    orgComment = zentry.getComment()
  }

}
