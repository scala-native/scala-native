package org.scalanative.testsuite.javalib.util.zip

import org.junit.Test
import org.junit.Assert._
import org.junit.BeforeClass

import java.io.FileOutputStream
import java.nio.file.Files
import java.util.Arrays

import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

object ZipOutputStreamTest {

  private var workDirString: String = _

  private val zipFileName = "ZipOutputStreamTestData.zip"

  private def makeTestDirs(): String = {
    val orgDir = Files.createTempDirectory("scala-native-testsuite")
    val javalibDir = orgDir.resolve("javalib")
    val testDirRootPath = javalibDir
      .resolve("java")
      .resolve("util")
      .resolve("zip")
      .resolve("ZipOutputStreamTest")

    val testDirSrcPath = testDirRootPath.resolve("src")
    val testDirDstPath = testDirRootPath.resolve("dst")
    Files.createDirectories(testDirRootPath)
    Files.createDirectory(testDirSrcPath)
    Files.createDirectory(testDirDstPath)

    testDirRootPath.toString()
  }

  @BeforeClass
  def beforeClass(): Unit = {
    workDirString = makeTestDirs()
  }
}

class ZipOutputStreamTest {
  import ZipOutputStreamTest._

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

  // Issue 3754
  @Test def zipOutputStream(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipFileName}"

    val dstName =
      s"${workDirString}/dst/copyOf_${zipFileName}"

    val entryNames = Array(
      "Rhea_1",
      "Prometheus_2",
      "Phoebe_3",
      "Tethys_4",
      "Iapetus_5"
    )

    createZipFile(srcName, entryNames)

    val zf = new ZipFile(srcName)
    try {
      val zipOut = new ZipOutputStream(new FileOutputStream(dstName))
      var outCount = 0

      try {
        zipOut.setComment(
          "Archive written by Scala Native java.util.zip.ZipOutputStreamTest"
        )

        zf.stream()
          .limit(99)
          .forEach(e => {
            outCount += 1
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
            zipOut.closeEntry()
          })

        assertEquals("number of entries written", entryNames.size, outCount)
      } finally {
        /* Down to the point of this Test: verifying a robust
         * "finish(); close()" sequence, without someone throwing an NPE.
         */

        zipOut.finish() // Throws no Null Pointer Exception
        zipOut.finish() // and can be done more than once without error.

        zipOut.close() // internally calls finish() again; for 3rd time series
      }
    } finally {
      zf.close()
    }
  }
}
