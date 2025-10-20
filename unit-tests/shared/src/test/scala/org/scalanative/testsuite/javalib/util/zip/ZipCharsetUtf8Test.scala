package org.scalanative.testsuite.javalib.util.zip

import org.junit.Test
import org.junit.Assert.*
import org.junit.BeforeClass

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.io.{BufferedInputStream, FileInputStream}
import java.io.{BufferedOutputStream, FileOutputStream}

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Paths}

import java.util.stream.Stream

import java.util.zip.{ZipEntry, ZipFile}
import java.util.zip.{ZipInputStream, ZipOutputStream}

/* Selected Tests of the use of UTF-8 characters in Scala Native
 * for java.util.zip.
 *
 * Passing these tests provide a necessary but not sufficient condition
 * for having confidence in Scala Native's Zip UTF-8 support.
 *
 * The reference .zip files were written on Unix.  Someday, Test cases
 * for .zip files written on Windows ought to be added.
 */

/* Debugging Notes:
 *   Zip support is complicated and has abundant quirks,
 *   given its wide usage, the passage of time, its slow development rate,
 *   and the demands of interoperability.
 *
 *   Some information for posterity:
 *
 *    - To increase confidence, the output file can be manually inspected
 *      and hand parsed using combinations of:
 *      # https://en.wikipedia.org/wiki/ZIP_(file_format)#History
 *      # "unzip -l file.zip" (that is an ell character) & kin are useful
 *        in displaying .zip contents at application level.
 *      # "hexdump -C file.zip" makes binary contents visible.
 *
 *   - Use "unzip -v" to check if unzip has been compiled with UTF-8 support.
 *     Ubuntu reports yes, macOS (Sonoma) reports no.
 *     If unzip has UTF-8 support, the "O" (capital letter oh) option
 *     may show names in archive as UTF-8 even if basic unzip does not.
 *     "unzip -l -O UTF-8 file.zip".
 *
 *     # Lack of clarity in "unzip -h" (help) description of -O
 *       suggests that if -O does not work -I might. (or -O might be
 *       for "output", not OS specific handling.)
 *
 *    - macOS is reported to use UTF-8 names in the archive, but not
 *      set zip general purpose flags bit 11 (mask value decimal 2048)
 *      to announce that practice.
 */

object ZipCharsetUtf8Test {

  private var workDirString: String = _

  private val zipTestUtf8DataFileName = "zipCharsetUtf8TestData.zip"

  private def makeTestDirs(): String = {
    val orgDir = Files.createTempDirectory("scala-native-testsuite")
    val javalibDir = orgDir.resolve("javalib")
    val testDirRootPath = javalibDir
      .resolve("java")
      .resolve("util")
      .resolve("zip")
      .resolve("ZipCharsetsTest")

    val testDirSrcPath = testDirRootPath.resolve("src")
    val testDirDstPath = testDirRootPath.resolve("dst")

    Files.createDirectories(testDirRootPath)
    Files.createDirectory(testDirSrcPath)
    Files.createDirectory(testDirDstPath)

    testDirRootPath.toString()
  }

  private def provisionZipFileCharsetsTestData(zeTestDir: String): Unit = {
    // In JVM, cwd is set to unit-tests/jvm/[scala-version]
    val inputRootDir =
      if (Platform.executingInJVM) "../.."
      else "unit-tests"

    val inputSubDirs =
      s"shared/src/test/resources/testsuite/javalib/java/util/zip/"

    val inputDir = s"${inputRootDir}/${inputSubDirs}"

    val inputFileName = s"${inputDir}/${zipTestUtf8DataFileName}"
    val outputFileName = s"${zeTestDir}/src/${zipTestUtf8DataFileName}"

    Files.copy(Paths.get(inputFileName), Paths.get(outputFileName))
  }

  @BeforeClass
  def beforeClass(): Unit = {
    workDirString = makeTestDirs()
    provisionZipFileCharsetsTestData(workDirString)
  }
}

class ZipCharsetUtf8Test {
  import ZipCharsetUtf8Test.*

  @Test def readZfArchiveComment_utf8(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestUtf8DataFileName}"

    val zf = new ZipFile(srcName)
    try {
      val expectedArchiveComment =
        "Written on Linux: Ελπίζω - hope 🌈 \"Rainbow\" U+1F308"

      val archiveComment = zf.getComment()
      assertNotNull(s"zipFile comment '${srcName}' not found", archiveComment)

      assertEquals(
        "Archive comment nBytes",
        60,
        archiveComment.getBytes(StandardCharsets.UTF_8).length
      )

      assertEquals(
        "Archive comment nCodepoints",
        51,
        archiveComment.codePointCount(0, archiveComment.length)
      )

      assertEquals("zipfile comment", expectedArchiveComment, archiveComment)
    } finally {
      zf.close()
    }
  }

  @Test def readZfEntryAndItsComment_utf8(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestUtf8DataFileName}"

    val zf = new ZipFile(srcName)
    try {
      val entryName = "Δίκη"
      val ze = zf.getEntry(entryName)
      assertNotNull(s"zipEntry '${entryName}' not found", ze)

      val expectedEntryComment =
        "Δίκη - Dike ⏳ - \"Hourglass - not done\" U+23f3"

      val entryComment = ze.getComment()
      assertNotNull(
        s"zipEntry comment for '${entryName}' not found",
        entryComment
      )

      assertEquals(
        "Entry comment nBytes",
        51,
        entryComment.getBytes(StandardCharsets.UTF_8).length
      )

      assertEquals(
        "entry comment nCodepoints",
        45,
        entryComment.codePointCount(0, entryComment.length)
      )

      assertEquals("Entry comment", expectedEntryComment, entryComment)
    } finally {
      zf.close()
    }
  }

  @Test def readZisEntryNameButNotComment_utf8(): Unit = {
    /* ZipInputStream API provides no way to Test archive comment.
     *
     * There is also no way to fetch the Entry comment for testing.
     * ZipInputStream reads the "local file header" (LOCHDR) not
     * the "C entral Directory file header" (CENHDR). Only the latter
     * has Entry comment data.
     */

    val srcName =
      s"${workDirString}/src/${zipTestUtf8DataFileName}"

    // Use "unexpected default" ISO_8859_1 to try to trip things up.
    val bis = new BufferedInputStream(new FileInputStream(srcName))
    val zis = new ZipInputStream(bis, StandardCharsets.ISO_8859_1)

    try {
      val ze = zis.getNextEntry()
      assertNotNull(s"zipEntry not found", ze)

      val expectedEntryName = "Δίκη"

      val entryName = ze.getName()
      assertNotNull(s"zipEntry no entry found", entryName)

      assertEquals(s"zipEntry name", expectedEntryName, entryName)

      // Spot check a "known" value; is current entry minimally believable.
      val entrySize = ze.getSize()
      assertEquals(s"zipEntry size for '${entryName}'", 68, entrySize)
    } finally {
      zis.close()
      bis.close() // zis should have closed this, but be sure.
    }
  }

  @Test def writeZosEntryNameAndTwoComments_utf8(): Unit = {
    val dstName =
      s"${workDirString}/dst/zipCharsetUtf8OutputStreamTest.zip"

    val expectedEntryName = "Δίκη"

    // Any Which Way but Loose
    val clydeInUtf8 = "🦧 - \"Orangutan\" U+1F9A7 -"

    val expectedArchiveComment =
      s"Written on Linux: ${clydeInUtf8} archive comment"

    val expectedEntryComment =
      s"${clydeInUtf8} file comment"

    val zipOut = new ZipOutputStream(
      new BufferedOutputStream(new FileOutputStream(dstName)),
      StandardCharsets.UTF_8
    )

    try {
      zipOut.setComment(expectedArchiveComment)

      val ze = new ZipEntry(expectedEntryName)
      ze.setComment(expectedEntryComment)

      zipOut.putNextEntry(ze)
      zipOut.closeEntry()
      zipOut.finish()
    } finally {
      zipOut.close()
    }
    // No Exception happened up to here, archive may still be junk.

    // Read the archive just created and check its UTF-8 comments & entry name.
    val zf = new ZipFile(dstName)
    try {
      val archiveComment = zf.getComment()
      assertNotNull(s"archive comment not found", archiveComment)
      assertEquals("archive comment", expectedArchiveComment, archiveComment)

      val ze = zf.getEntry(expectedEntryName)
      assertNotNull(s"zipEntry '${expectedEntryName}' not found", ze)

      val entryComment = ze.getComment()

      assertNotNull(
        s"zipEntry comment for '${expectedEntryName}' not found",
        expectedEntryComment
      )
      assertEquals("entry comment", expectedEntryComment, entryComment)

      // See "Debugging Notes" at top of file
    } finally {
      zf.close()
    }
  }
}
