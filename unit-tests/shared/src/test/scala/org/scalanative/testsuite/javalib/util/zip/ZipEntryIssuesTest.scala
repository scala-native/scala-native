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

/* Do not disturb the peace of the Harmony code ported to Scala Native.
 * Consolidate Test(s) written well after that port in this separate file.
 */

object ZipEntryIssuesTest {

  private var workDirString: String = _

  private val zipTestDataFileName = "zipEntryReadCommentTestData.zip"

  private def makeTestDirs(): String = {
    val orgDir = Files.createTempDirectory("scala-native-testsuite")
    val javalibDir = orgDir.resolve("javalib")
    val testDirRootPath = javalibDir
      .resolve("java")
      .resolve("util")
      .resolve("zip")
      .resolve("ZipEntriesIssuesTest")

    val testDirSrcPath = testDirRootPath.resolve("src")

    Files.createDirectories(testDirRootPath)
    Files.createDirectory(testDirSrcPath)

    testDirRootPath.toString()
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
}
