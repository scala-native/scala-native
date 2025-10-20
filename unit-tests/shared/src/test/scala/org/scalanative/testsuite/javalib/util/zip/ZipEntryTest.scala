package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony. Contains extensive changes for Scala Native.

import org.junit.Test
import org.junit.Assert.*
import org.junit.AfterClass
import org.junit.Ignore

import scala.scalanative.junit.utils.AssumesHelper.*
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.lang as jl

import java.util.Arrays
import java.util.zip.*

/* Note: JDK 23 non-compliance
 *
 * JDK 23 introduced stricter argument checking for ZipEntry(name: String),
 * setComment(comment: String), & setExtra(extra: Array[Byte]). It makes a
 * best effort attempt to ensure, from the JDK 24 ZipEntry doc for setExtra():
 *   "IllegalArgumentException - if the combined length of the specified
 *    extra field data, the entry name, the entry comment, and the
 *    CEN Header size exceeds 65,535 bytes."
 *
 * Currently, Scala Native javalib is hard coded to run as JDK 8 so the
 * implementation follows the JDK 8 rules and do not comply with JDK >= 23.
 * Scala Native will silently truncate sizes as it is writing to ZIP outputs
 * so as to write ZIP files which comply ZIP APPNOTE.txt.
 *
 * The non-compliance here is only in not reporting various combinations
 * of unusually large sizes.
 *
 * 1) When time comes to look at this non-compliance, there are some
 *    subtleties, a.k.a pain points, about determining byte counts for the
 *    various UTF-16 fields at points in the code where the output charset
 *    is not known. A precisely perverse UTF-16 String can convert to
 *    approximately 1.5 times its size as UTF-8 bytes.
 *
 * 2) It appears that the ZipEntry(name: String) constructor reserves
 *    space for the size of the CEN Header. The size of a CEN Header
 *    varies by ZIP file. The JDK 24 doc makes no mention of how the
 *    CEN Header size is made known to the ZIPEntry constructor.
 *    Experimentation appears to indicate that space is reserved for the
 *    46 bytes of the fixed portion of the CEN Header.
 */

object ZipEntryTest {
  import ZipBytes.{getZipFile, zipFile}

  val zfile = getZipFile(zipFile)
  val zentry = zfile.getEntry("File1.txt")

  val orgSize = zentry.getSize()
  val orgCompressedSize = zentry.getCompressedSize()
  val orgCrc = zentry.getCrc()

// Revert PR #3794 so I can chase intermittent bad values & Segfault
//  lazy val orgTime = zentry.getTime()
  val orgTime = -1

  val orgComment = zentry.getComment()

  @AfterClass
  def cleanup(): Unit =
    zfile.close()

}

class ZipEntryTest {
  import ZipEntryTest.*

  private final val maxZipNameLen = 0xffff // decimal 65535.

  private val jumboZipNameSB: jl.StringBuilder = {
    // Must run on Java 8, which has no j.l.StringBuilder.repeat()

    // 0xFFFF has 4 prime factors, decimal 3, 5, 17, 257
    val maxChunk = 3 * 5 * 257 // 3855, approx 4K, yielding 17 loops iterations
    val chunk = new Array[Char](maxChunk)
    Arrays.fill(chunk, 'a')

    // Allocate +1 to allow testing going over the max, without reallocation.
    val sb = new jl.StringBuilder(maxZipNameLen + 1)

    for (j <- 1 to maxZipNameLen / maxChunk)
      sb.append(chunk)

    assertEquals("jumboZipName length", maxZipNameLen, sb.length())
    sb.append("Z")

    sb
  }

  private final val atMax = jumboZipNameSB.substring(0, maxZipNameLen)
  private final val overMax = jumboZipNameSB.toString()

  @Test def constructorString(): Unit = {
    assertThrows(classOf[NullPointerException], zfile.getEntry(null))

    // See JDK 23+ strict argument checking note at top of file.
    if (Platform.executingInJVMWithJDKIn(23 to 9999)) {
      assumeNotJVMCompliant()
    } else {
      val ze = new ZipEntry(atMax)
      assertNotNull("string == 0xFFFF", ze)

      assertThrows(
        classOf[IllegalArgumentException],
        new ZipEntry(overMax)
      )
    }
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

// Revert PR #3794 so I can chase intermittent bad values & Segfault
  @Ignore
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
    // See JDK 23+ strict argument checking note at top of file.
    if (Platform.executingInJVMWithJDKIn(23 to 9999)) {
      assumeNotJVMCompliant()
    } else {
      val ze = zfile.getEntry("File1.txt")
      ze.setComment("Set comment using api")
      assertTrue(ze.getComment() == "Set comment using api")
      assertEquals("getComment", "Set comment using api", ze.getComment())

      ze.setComment(null)
      assertNull("setComment(null)", ze.getComment())

      ze.setComment(atMax)

      // From Java API docs:
      // ZIP entry comments have maximum length of 0xffff. If the length of the
      // specified comment string is greater than 0xFFFF bytes after encoding,
      // only the first 0xFFFF bytes are output to the ZIP file entry.

      ze.setComment(overMax) // Should silently truncate, not throw().
    }
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
    // See JDK 23+ strict argument checking note at top of file.
    if (Platform.executingInJVMWithJDKIn(23 to 9999)) {
      assumeNotJVMCompliant()
    } else {
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

    if (!Platform.executingInJVM) {
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
