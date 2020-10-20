package java.util.zip

// Ported from Apache Harmony

import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import scala.scalanative.junit.utils.AssertThrows._

import ZipBytes._

class ZipEntryTest {

  var zfile: ZipFile          = null
  var zentry: ZipEntry        = null
  var orgSize: Long           = 0L
  var orgCompressedSize: Long = 0L
  var orgCrc: Long            = 0L
  var orgTime: Long           = 0L
  var orgComment: String      = null

  @Test def constructorString(): Unit = {
    zentry = zfile.getEntry("File3.txt")
    assertTrue(zentry != null)

    assertThrows(classOf[NullPointerException], zfile.getEntry(null))
    val s = new StringBuffer()
    var i = 0
    while (i < 65535) {
      s.append('a')
      i += 1
    }

    new ZipEntry(s.toString)

    s.append('a')
    assertThrows(classOf[IllegalArgumentException], new ZipEntry(s.toString()))
  }

  @Test def constructorZipEntry(): Unit = {
    zentry.setSize(2)
    zentry.setCompressedSize(4)
    zentry.setComment("Testing")

    val zentry2 = new ZipEntry(zentry)
    assertTrue(zentry2.getSize() == 2)
    assertTrue(zentry2.getComment() == "Testing")
    assertTrue(zentry2.getCompressedSize() == 4)
    assertTrue(zentry2.getCrc() == orgCrc)
    assertTrue(zentry2.getTime() == orgTime)
  }

  @Test def getComment(): Unit = {
    val zipEntry = new ZipEntry("zippy.zip")
    assertTrue(zipEntry.getComment() == null)
    zipEntry.setComment("This Is A Comment")
    assertTrue(zipEntry.getComment() == "This Is A Comment")
  }

  @Test def getCompressedSize(): Unit = {
    assertTrue(zentry.getCompressedSize() == orgCompressedSize)
  }

  @Test def getCrc(): Unit = {
    assertTrue(zentry.getCrc() == orgCrc)
  }

  @Test def getExtra(): Unit = {
    assertTrue(zentry.getExtra() == null)
    val ba = Array[Byte]('T', 'E', 'S', 'T')
    zentry = new ZipEntry("test.tst")
    zentry.setExtra(ba)
    assertTrue(zentry.getExtra() == ba)
  }

  @Test def getMethod(): Unit = {
    zentry = zfile.getEntry("File1.txt")
    assertTrue(zentry.getMethod() == ZipEntry.STORED)

    zentry = zfile.getEntry("File3.txt")
    assertTrue(zentry.getMethod() == ZipEntry.DEFLATED)

    zentry = new ZipEntry("test.tst")
    assertTrue(zentry.getMethod() == -1)
  }

  @Test def getName(): Unit = {
    assertTrue(zentry.getName() == "File1.txt")
  }

  @Test def getSize(): Unit = {
    assertTrue(zentry.getSize() == orgSize)
  }

  @Test def getTime(): Unit = {
    assertTrue(zentry.getTime() == orgTime)
  }

  @Test def isDirectory(): Unit = {
    assertTrue(!zentry.isDirectory())
    zentry = new ZipEntry("Directory/")
    assertTrue(zentry.isDirectory())
  }

  @Test def setCommentString(): Unit = {
    zentry = zfile.getEntry("File1.txt")
    zentry.setComment("Set comment using api")
    assertTrue(zentry.getComment() == "Set comment using api")
    zentry.setComment(null)
    assertTrue(zentry.getComment() == null)
    val s = new StringBuffer()
    var i = 0
    while (i < 0xFFFF) {
      s.append('a')
      i += 1
    }
    zentry.setComment(s.toString)

    s.append('a')
    assertThrows(classOf[IllegalArgumentException],
                 zentry.setComment(s.toString))
  }

  @Test def setCompressedSizeLong(): Unit = {
    zentry.setCompressedSize(orgCompressedSize + 10)
    assertTrue(zentry.getCompressedSize() == orgCompressedSize + 10)

    zentry.setCompressedSize(0)
    assertTrue(zentry.getCompressedSize() == 0)

    zentry.setCompressedSize(-25)
    assertTrue(zentry.getCompressedSize() == -25)

    zentry.setCompressedSize(4294967296L)
    assertTrue(zentry.getCompressedSize() == 4294967296L)
  }

  @Test def setCrcLong(): Unit = {
    zentry.setCrc(orgCrc + 100)
    assertTrue(zentry.getCrc == orgCrc + 100)

    zentry.setCrc(0)
    assertTrue(zentry.getCrc == 0)

    assertThrows(classOf[IllegalArgumentException], zentry.setCrc(-25))

    zentry.setCrc(4294967295L)
    assertTrue(zentry.getCrc == 4294967295L)

    assertThrows(classOf[IllegalArgumentException], zentry.setCrc(4294967296L))
  }

  @Test def setExtraArrayByte(): Unit = {
    zentry = zfile.getEntry("File1.txt")
    zentry.setExtra("Test setting extra information".getBytes())
    assertTrue(
      new String(zentry.getExtra(), 0, zentry.getExtra().length) == "Test setting extra information")

    zentry = new ZipEntry("test.tst")
    var ba = new Array[Byte](0xFFFF)
    zentry.setExtra(ba)
    assertTrue(zentry.getExtra() == ba)

    assertThrows(classOf[IllegalArgumentException], {
      ba = new Array[Byte](0xFFFF + 1)
      zentry.setExtra(ba)
    })

    val zeInput = new ZipEntry("InputZip")
    val extraB  = Array[Byte]('a', 'b', 'd', 'e')
    zeInput.setExtra(extraB)
    assertTrue(extraB == zeInput.getExtra())
    assertTrue(extraB(3) == zeInput.getExtra()(3))
    assertTrue(extraB.length == zeInput.getExtra().length)

    val zeOutput = new ZipEntry(zeInput)
    assertTrue(zeInput.getExtra()(3) == zeOutput.getExtra()(3))
    assertTrue(zeInput.getExtra().length == zeOutput.getExtra().length)
    assertTrue(extraB(3) == zeOutput.getExtra()(3))
    assertTrue(extraB.length == zeOutput.getExtra().length)
  }

  @Test def setMethodInt(): Unit = {
    zentry = zfile.getEntry("File3.txt")
    zentry.setMethod(ZipEntry.STORED)
    assertTrue(zentry.getMethod() == ZipEntry.STORED)

    zentry.setMethod(ZipEntry.DEFLATED)
    assertTrue(zentry.getMethod() == ZipEntry.DEFLATED)

    val error = 1
    assertThrows(classOf[IllegalArgumentException], {
      zentry = new ZipEntry("test.tst")
      zentry.setMethod(error)
    })
  }

  @Test def setSizeLong(): Unit = {
    zentry.setSize(orgSize + 10)
    assertTrue(zentry.getSize() == orgSize + 10)

    zentry.setSize(0)
    assertTrue(zentry.getSize() == 0)

    assertThrows(classOf[IllegalArgumentException], zentry.setSize(-25))

    zentry.setSize(4294967295L)

    assertThrows(classOf[IllegalArgumentException], zentry.setSize(4294967296L))
  }

  @Before
  def setUp(): Unit = {
    zfile = getZipFile(zipFile)
    zentry = zfile.getEntry("File1.txt")
    orgSize = zentry.getSize()
    orgCompressedSize = zentry.getCompressedSize()
    orgCrc = zentry.getCrc()
    orgTime = zentry.getTime()
    orgComment = zentry.getComment()
  }

}
