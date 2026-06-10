package org.scalanative.testsuite.javalib.util.zip

import java.io.{BufferedOutputStream, FileOutputStream, IOException}
import java.nio.file.{Files, Paths}
import java.util.Arrays
import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

/* Do not disturb the peace of Tests written when the  Harmony code
 * was ported to Scala Native.
 *
 * Consolidate Test(s) written well after that time in this separate file.
 */

object ZipEntryIssuesTest {

  private var workDirString: String = _

  private val zipTestDataFileName = "zipEntryReadCommentTestData.zip"
  private val zipTestSetDosTimeFileName = "zipEntrySetDosTimeTestData.zip"

  private def makeTestDirs(): String = {
    val orgDir = Files.createTempDirectory("scala-native-testsuite")
    val javalibDir = orgDir.resolve("javalib")
    val testDirRootPath = javalibDir
      .resolve("java")
      .resolve("util")
      .resolve("zip")
      .resolve("ZipEntriesIssuesTest")

    val testDirSrcPath = testDirRootPath.resolve("src")
    val testDirDstPath = testDirRootPath.resolve("dst")

    Files.createDirectories(testDirRootPath)
    Files.createDirectory(testDirSrcPath)
    Files.createDirectory(testDirDstPath)

    testDirRootPath.toString()
  }

  private def createZipFile(
      location: String,
      entryNames: Array[String]
  ): Unit = {
    val zipOut = new ZipOutputStream(
      new BufferedOutputStream(new FileOutputStream(location))
    )
    try {
      zipOut.setComment("Some interesting moons of Saturn.")

      Arrays
        .stream(entryNames)
        .forEach(e => zipOut.putNextEntry(new ZipEntry(e)))

    } finally {
      zipOut.close()
    }
  }

  private def provisionZipEntrySetDosTimeTestData(zosTestDir: String): Unit = {
    // In JVM, cwd is set to unit-tests/jvm/[scala-version]
    val inputRootDir =
      if (Platform.executingInJVM) "../.."
      else "unit-tests"

    val outputFileQualifiedName =
      s"${zosTestDir}/src/${zipTestSetDosTimeFileName}"

    val entryNames = Array(
      "Rhea_1",
      "Prometheus_2",
      "Phoebe_3",
      "Tethys_4",
      "Iapetus_5"
    )

    createZipFile(outputFileQualifiedName, entryNames)
  }

  private def provisionZipEntryIssuesTestData(zeTestDir: String): Unit = {
    // In JVM, cwd is set to unit-tests/jvm/[scala-version]
    val inputRootDir =
      if (Platform.executingInJVM) "../.."
      else "unit-tests"

    val inputSubDirs =
      s"shared/src/test/resources/testsuite/javalib/java/util/zip/"

    val inputDir = s"${inputRootDir}/${inputSubDirs}"

    val inputFileName = s"${inputDir}/${zipTestDataFileName}"

    val outputFileName = s"${zeTestDir}/src/${zipTestDataFileName}"

    Files.copy(Paths.get(inputFileName), Paths.get(outputFileName))
  }

  @BeforeClass
  def beforeClass(): Unit = {
    workDirString = makeTestDirs()
    provisionZipEntryIssuesTestData(workDirString)
    provisionZipEntrySetDosTimeTestData(workDirString)
  }
}

class ZipEntryIssuesTest {
  import ZipEntryIssuesTest._

  // Issue 3755
  @Test def readEntryComment(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestDataFileName}"

    val zf = new ZipFile(srcName)
    try {
      val entryName = "LoremIpsum.utf-8"
      val ze = zf.getEntry(entryName)
      assertNotNull("zipEntry '${entryName}' not found", ze)

      // How do we know? Manual "zip -l" exam of src .zip told us. Who told it?
      val expected = "Better days are coming"

      val comment = ze.getComment()

      assertNotNull("zipEntry comment '${entryName}' not found", comment)

      assertEquals("Entry comment", expected, comment)
    } finally {
      zf.close()
    }
  }

  // Issue 3816 - stress the setTime/getTime path that previously
  // segfaulted on macOS due to uninitialised stackalloc'd `tm`
  // (mktime reads tm_gmtoff/tm_zone/tm_wday past the fields we set).
  // Run a tight loop in case the segfault is intermittent.
  @Test def setTimeGetTimeStressLoop(): Unit = {
    // Y2K UTC; deterministic round-trip independent of local tz, since
    // setTime(localtime) and getTime(mktime) cancel local offset.
    val expectedMillis = 946684800000L
    val ze = new ZipEntry("stress")
    var i = 0
    while (i < 100) {
      ze.setTime(expectedMillis)
      assertEquals("getTime round-trip", expectedMillis, ze.getTime())
      i += 1
    }
  }

  // Regression: msDosYears == 0 (year 1980, the MS-DOS epoch) is a
  // valid encoding; the previous guard `<= 0` collapsed every 1980
  // date to 1980-01-01 00:00.
  @Test def setTimeMidYear1980RoundTrip(): Unit = {
    // 1980-06-15 00:00:00 UTC — seconds-mod-2 == 0 so MS-DOS's 2-second
    // granularity is lossless; mid-year so local-tz offset can't push
    // the date out of 1980 in any sane zone.
    val mid1980Millis = 329875200000L
    val ze = new ZipEntry("y1980")
    ze.setTime(mid1980Millis)
    assertEquals("getTime round-trip", mid1980Millis, ze.getTime())
  }

  // Regression: timestamps past 2038-01-19 are within the DOS date
  // range (1980..2107) but exceeded `__time32_t` on Windows. Linux/macOS
  // already use 64-bit time_t; this guards against a future regression
  // and exercises the same code path that the Windows 64-bit binding
  // depends on.
  @Test def setTimePost2038RoundTrip(): Unit = {
    // 3_000_000_000 seconds since epoch ≈ 2065-01-24 UTC
    val post2038Millis = 3000000000000L
    val ze = new ZipEntry("post2038")
    ze.setTime(post2038Millis)
    assertEquals("getTime round-trip", post2038Millis, ze.getTime())
  }

  // Issue 3787
  @Test def setEntryDosTime(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestSetDosTimeFileName}"

    val dstName =
      s"${workDirString}/dst/CopyOf_${zipTestSetDosTimeFileName}"

    /*  expectedMillis generated using JVM:
     *  val y2k = Instant.parse("2000-01-01T00:00:00.00Z").toEpochMilli
     *  val y2k: Long = 946684800000
     */

    val changeEntry = "Tethys_4"

    val expectedMillis = 946684800000L

    val zf = new ZipFile(srcName)
    try {
      val zipOut = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(dstName))
      )

      try {
        zf.stream()
          .limit(99)
          .forEach(e => {
            zipOut.putNextEntry(e)

            if (!e.isDirectory()) {
              val fis = zf.getInputStream(e)
              val buf = new Array[Byte](2 * 1024)

              try {
                var nRead = 0
                // Poor but useful idioms creep in: porting from Java style
                while ({ nRead = fis.read(buf); nRead } > 0) {
                  zipOut.write(buf, 0, nRead)
                  assertEquals("fis nRead", e.getSize(), nRead)
                }
              } finally {
                fis.close()
              }
            }
            // make a change to modification time which should be noticable.
            if (e.getName() == changeEntry) {
              e.setTime(expectedMillis)
              e.setComment(
                "ms-dos modtime should be Year 2000 UTC, " +
                  s"local to where file was written."
              )
            }
            zipOut.closeEntry()
          })

      } finally {
        zipOut.close()
      }

    } finally {
      zf.close()
    }

    /* Re-read to see if getTime() returns the expected value.
     * If not, manual visual inspection of the output file will distinguish
     * if the change was durable or if getTime() mangled reading it.
     */

    val zfDst = new ZipFile(dstName)
    try {
      val ze = zfDst.getEntry(changeEntry)
      assertNotNull("zipEntry '${changeEntry}' not found", ze)
      assertEquals("getTime()", expectedMillis, ze.getTime())
    } finally {
      zfDst.close()
    }
  }
}
