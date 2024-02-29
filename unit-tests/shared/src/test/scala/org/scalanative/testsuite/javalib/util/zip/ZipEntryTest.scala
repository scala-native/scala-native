package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony. Contains extensive changes for Scala Native.

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.executingInJVM

import java.{lang => jl}

import java.util.Arrays
import java.util.zip._

object ZipEntryTest {
  import ZipBytes.{getZipFile, zipFile}

  val zfile: ZipFile = getZipFile(zipFile)
  val zentry = zfile.getEntry("File1.txt")

  val orgSize = zentry.getSize()
  val orgCompressedSize = zentry.getCompressedSize()
  val orgCrc = zentry.getCrc()
  lazy val orgTime = zentry.getTime()
  val orgComment = zentry.getComment()
}

class ZipEntryTest {
  import ZipEntryTest._

  /* Use a 'def' rather than a 'val' because two tests each append a char
   * to the StringBuilder returned.
   *
   * It is unclear if these tests are ever run in parallel.  If they were
   * un-synchronized access to a 'val' might show intermittent errors.
   * A person with some time could probably develop a synchronized 'val'
   * and save some allocation and setting of memory.  An optimization for
   * a future devo. java.util.zip has bigger problems today.
   */
  private def jumboZipNameSB(): jl.StringBuilder = {
    //  Also the maximum comment length.
    val maxZipNameLen = 0xffff // decimal 65535.

    // 0xFFFF has 4 prime factors, decimal 3, 5, 17, 257
    val maxChunk = 3 * 5 * 257 // 3855, approx 4K, yielding 17 loops iterations
    val chunk = new Array[Char](maxChunk)
    Arrays.fill(chunk, 'a')

    // Allocate +1 to allow testing going over the max, without reallocation.
    val s = new jl.StringBuilder(maxZipNameLen + 1)

    for (j <- 1 to maxZipNameLen / maxChunk)
      s.append(chunk)

    assertEquals("jumboZipName length", maxZipNameLen, s.length())

    s
  }

  @Test def constructorString(): Unit = {
    assertThrows(classOf[NullPointerException], zfile.getEntry(null))

    val atMax = jumboZipNameSB()

    val ze = new ZipEntry(atMax.toString())
    assertNotNull("string == 0xFFFF", ze)

    val overMax = atMax.append('a')
    assertThrows(
      classOf[IllegalArgumentException],
      new ZipEntry(overMax.toString())
    )
  }

  @Test def constructorZipEntry(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    ze.setSize(2L)
    ze.setCompressedSize(4L)
    ze.setComment("Testing")

    val ze2 = new ZipEntry(ze)

    assertNotEquals("Need clone, not identity", ze, ze2)

    assertEquals("getSize", 2L, ze2.getSize())
    assertEquals("getComment", "Testing", ze2.getComment())
    assertEquals("getCompressedSize", 4L, ze2.getCompressedSize())
    assertEquals("getCrc", orgCrc, ze2.getCrc())
    assertEquals("getTime", ze.getTime(), ze2.getTime())
  }

  @Test def getComment(): Unit = {
    val ze = new ZipEntry("zippy.zip")
    assertNull("null comment", ze.getComment())

    val expected = "This Is A Comment"
    ze.setComment(expected)
    assertEquals("comment", expected, ze.getComment())
  }

  @Test def getCompressedSize(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertEquals("compressed size", orgCompressedSize, ze.getCompressedSize())
  }

  @Test def getCrc(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertTrue(ze.getCrc() == orgCrc)
  }

  @Test def getExtra(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertTrue(ze.getExtra() == null)
    val ba = Array[Byte]('T', 'E', 'S', 'T')
    val ze2 = new ZipEntry("test.tst")
    ze2.setExtra(ba)
    assertTrue(ze2.getExtra() == ba)
  }

  @Test def getMethod(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertTrue(ze.getMethod() == ZipEntry.STORED)
    assertEquals("File1.txt", ZipEntry.STORED, ze.getMethod())

    val ze2 = zfile.getEntry("File3.txt")
    assertEquals("File2.txt", ZipEntry.DEFLATED, ze2.getMethod())

    val ze3 = new ZipEntry("test.tst")
    assertTrue(ze3.getMethod() == -1)
    assertEquals("test.tst", -1, ze3.getMethod())
  }

  @Test def getName(): Unit = {
    val expected = "File1.txt"
    val ze = zfile.getEntry(expected)
    assertEquals(expected, ze.getName())
  }

  @Test def getSize(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertTrue(ze.getSize() == orgSize)
  }

  @Test def getTime(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertEquals("getTime", orgTime, ze.getTime())
  }

  @Test def isDirectory(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    assertTrue("Expected non-directory", !ze.isDirectory())

    val ze2 = new ZipEntry("Directory/")
    assertTrue("Expected non-directory", ze2.isDirectory())
  }

  @Test def setCommentString(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    ze.setComment("Set comment using api")
    assertTrue(ze.getComment() == "Set comment using api")
    assertEquals("getComment", "Set comment using api", ze.getComment())

    ze.setComment(null)
    assertNull("setComment(null)", ze.getComment())

    val atMax = jumboZipNameSB()
    ze.setComment(atMax.toString())

    // From Java API docs:
    // ZIP entry comments have maximum length of 0xffff. If the length of the
    // specified comment string is greater than 0xFFFF bytes after encoding,
    // only the first 0xFFFF bytes are output to the ZIP file entry.

    val overMax = atMax.append('a')
    ze.setComment(overMax.toString()) // Should silently truncate, not throw().
  }

  @Test def setCompressedSizeLong(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    ze.setCompressedSize(orgCompressedSize + 10)
    assertTrue(ze.getCompressedSize() == orgCompressedSize + 10)

    ze.setCompressedSize(0)
    assertTrue(ze.getCompressedSize() == 0)

    ze.setCompressedSize(-25)
    assertTrue(ze.getCompressedSize() == -25)

    ze.setCompressedSize(4294967296L)
    assertTrue(ze.getCompressedSize() == 4294967296L)
  }

  @Test def setCrcLong(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    ze.setCrc(orgCrc + 100)
    assertTrue(ze.getCrc == orgCrc + 100)

    ze.setCrc(0)
    assertTrue(ze.getCrc == 0)

    assertThrows(classOf[IllegalArgumentException], ze.setCrc(-25))

    ze.setCrc(4294967295L)
    assertTrue(ze.getCrc == 4294967295L)

    assertThrows(classOf[IllegalArgumentException], ze.setCrc(4294967296L))
  }

  @Test def setExtraArrayByte(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    ze.setExtra("Test setting extra information".getBytes())
    assertTrue(
      new String(
        ze.getExtra(),
        0,
        ze.getExtra().length
      ) == "Test setting extra information"
    )

    val ze2 = new ZipEntry("test.tst")
    var ba = new Array[Byte](0xffff)
    ze2.setExtra(ba)
    assertTrue(ze2.getExtra() == ba)

    assertThrows(
      classOf[IllegalArgumentException], {
        ba = new Array[Byte](0xffff + 1)
        ze2.setExtra(ba)
      }
    )

    val zeInput = new ZipEntry("InputZip")
    val extraB = Array[Byte]('a', 'b', 'd', 'e')
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
    val ze = zfile.getEntry("File3.txt")
    ze.setMethod(ZipEntry.STORED)
    assertTrue(ze.getMethod() == ZipEntry.STORED)

    ze.setMethod(ZipEntry.DEFLATED)
    assertTrue(ze.getMethod() == ZipEntry.DEFLATED)

    val error = 1
    assertThrows(
      classOf[IllegalArgumentException],
      (new ZipEntry("test.tst")).setMethod(error)
    )
  }

  @Test def setSizeLong(): Unit = {
    val ze = zfile.getEntry("File1.txt")
    ze.setSize(orgSize + 10)
    assertTrue(ze.getSize() == orgSize + 10)

    ze.setSize(0)
    assertTrue(ze.getSize() == 0)

    assertThrows(classOf[IllegalArgumentException], ze.setSize(-25))

    if (!executingInJVM) {
      // Cannot determine whether ZIP64 support is supported on Windows
      // From Java API: throws IllegalArgumentException if:
      // * the specified size is less than 0
      // * is greater than 0xFFFFFFFF when ZIP64 format is not supported
      // * or is less than 0 when ZIP64 is supported
      // ScalaNative supports ZIP64
      ze.setSize(4294967295L)

      assertThrows(
        classOf[IllegalArgumentException],
        ze.setSize(4294967296L)
      )
    }
  }
}
