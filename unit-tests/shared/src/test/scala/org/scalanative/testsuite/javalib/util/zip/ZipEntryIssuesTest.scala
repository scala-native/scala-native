package org.scalanative.testsuite.javalib.util.zip

import org.junit.Test
import org.junit.Assert._
import org.junit.BeforeClass

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.io.{BufferedOutputStream, IOException, FileOutputStream}
import java.nio.file.{Files, Paths}
import java.util.Arrays

import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

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
