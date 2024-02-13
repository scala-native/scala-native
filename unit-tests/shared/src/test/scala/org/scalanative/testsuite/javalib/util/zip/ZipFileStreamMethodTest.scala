package org.scalanative.testsuite.javalib.util.zip

import org.junit.Test
import org.junit.Assert._
import org.junit.BeforeClass

import java.io.FileOutputStream
import java.nio.file.Files
import java.util.Arrays

import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

object ZipFileStreamMethodTest {

  private var workDirString: String = _

  private val zipFileName = "ZipFileStreamMethodsTestData.zip"

  private def makeTestDirs(): String = {
    val orgDir = Files.createTempDirectory("scala-native-testsuite")
    val javalibDir = orgDir.resolve("javalib")
    val testDirRootPath = javalibDir
      .resolve("java")
      .resolve("util")
      .resolve("zip")
      .resolve("ZipFileStreamMethodTest")

    val testDirDstPath = testDirRootPath.resolve("dst")
    Files.createDirectories(testDirRootPath)
    Files.createDirectory(testDirDstPath)

    testDirRootPath.toString()
  }

  @BeforeClass
  def beforeClass(): Unit = {
    workDirString = makeTestDirs()
  }
}

class ZipFileStreamMethodTest {
  import ZipFileStreamMethodTest._

  private def createZipFile(
      location: String,
      entryNames: Array[String]
  ): Unit = {
    val zipOut = new ZipOutputStream(new FileOutputStream(location))
    try {
      zipOut.setComment("Some interesting moons of Saturn.")

      Arrays
        .stream(entryNames)
        .forEach(e => zipOut.putNextEntry(new ZipEntry(e)))

    } finally {
      zipOut.close()
    }
  }

  @Test def streamMethod(): Unit = {
    val zfileName =
      s"${workDirString}/dst/${zipFileName}"

    /* To help failure message give better clues,  names should _not_ be
     * in alphabetical order. Suffix gives expected encounter order.
     */
    val entryNames = Array(
      "Rhea_1",
      "Prometheus_2",
      "Phoebe_3",
      "Tethys_4",
      "Iapetus_5"
    )

    createZipFile(zfileName, entryNames)

    // Now check that that the stream()'ed entries are in encounter order.

    var index = 0

    val zf = new ZipFile(zfileName)
    try {
      zf.stream()
        .forEach(e => {
          assertEquals(
            "unexpected stream order",
            entryNames(index),
            e.getName()
          )
          index += 1
        })

      assertEquals("unexpected stream size", entryNames.length, index)

    } finally {
      zf.close()
    }
  }
}
