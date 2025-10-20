package org.scalanative.testsuite.javalib.util.zip

import org.junit.Test
import org.junit.Assert.*
import org.junit.BeforeClass

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.io.{BufferedOutputStream, IOException, FileOutputStream}
import java.nio.file.Files
import java.util.Arrays

import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

object ZipOutputStreamTest {

  private var workDirString: String = _

  private val zipTestDataFileName = "ZipOutputStreamTestData.zip"

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

  private def provisionZipOutputStreamTestData(zosTestDir: String): Unit = {
    // In JVM, cwd is set to unit-tests/jvm/[scala-version]
    val inputRootDir =
      if (Platform.executingInJVM) "../.."
      else "unit-tests"

    val outputFileQualifiedName = s"${zosTestDir}/src/${zipTestDataFileName}"

    val entryNames = Array(
      "Rhea_1",
      "Prometheus_2",
      "Phoebe_3",
      "Tethys_4",
      "Iapetus_5"
    )

    createZipFile(outputFileQualifiedName, entryNames)
  }

  @BeforeClass
  def beforeClass(): Unit = {
    workDirString = makeTestDirs()
    provisionZipOutputStreamTestData(workDirString)
  }
}

class ZipOutputStreamTest {
  import ZipOutputStreamTest.*

  // Issue 3754
  @Test def zipOutputStreamFinishThenClose(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestDataFileName}"

    val dstName =
      s"${workDirString}/dst/FinishThenClose_CopyOf_${zipTestDataFileName}"

    val zf = new ZipFile(srcName)
    try {
      val zipOut = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(dstName))
      )

      try {
        zipOut.setComment(
          "Archive written by Scala Native java.util.zip.ZipOutputStreamTest"
        )

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
            zipOut.closeEntry()
          })
      } finally {
        /* Down to the point of this Test: verifying a robust
         * "finish(); close()" sequence, without someone throwing an NPE.
         */

        zipOut.finish() // Throws no Null Pointer Exception
        zipOut.finish() // and can be done more than once without error.

        /* "put" after "finish" makes no sense, but someday someone
         * is going to do it. JVM silently skips such absurdity and does not
         * corrupt the output file after its Central Directory End Record
         * has been written by first finish().
         */

        zipOut.putNextEntry(new ZipEntry("IllAdvised"))
        zipOut.closeEntry()

        // close() internally calls finish() again; for 3rd time. Bug be gone!
        zipOut.close()

        /* One can now manually examine the output zip using, say Linux/Mark's
         * "unzip -l" (ell). The archive ought to be readable and it ought
         * to contain exactly the entries of the src file: no more, no less.
         * Difficult to automate that for CI but a time-saver to know for
         * debugging.
         */
      }
    } finally {
      zf.close()
    }
  }

  @Test def zipOutputStreamCloseThenFinish(): Unit = {
    val srcName =
      s"${workDirString}/src/${zipTestDataFileName}"

    val dstName =
      s"${workDirString}/dst/CloseThenFinish_CopyOf_${zipTestDataFileName}"

    val zf = new ZipFile(srcName)
    try {
      val zipOut = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(dstName))
      )

      try {
        zipOut.setComment(
          "Archive written by Scala Native java.util.zip.ZipOutputStreamTest"
        )

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
            zipOut.closeEntry()
          })
      } finally {
        /* Down to the point of this Test: verifying a robust
         * "close(); finish()" sequence. Bookend of "finish(); close()" test.
         */

        zipOut.close()
        assertThrows(classOf[IOException], zipOut.finish())
      }
    } finally {
      zf.close()
    }
  }
}
