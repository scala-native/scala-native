package org.scalanative.testsuite
package javalib.nio.file

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Path}
import java.util.function.Consumer
import java.util.{stream => jus}
import java.{util => ju}

import org.junit.Assert._
import org.junit.{AfterClass, BeforeClass, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FilesTestOnJDK12 {
  import FilesTestOnJDK12._

  @Test def mismatch_EqualPaths: Unit = {
    val commonFileName = "JohnSmith.txt"
    val path1 = getCleanIoPath(commonFileName)
    val path2 = getCleanIoPath(commonFileName)

    // Call should show "no mismatch" even when no actual file exists at path.
    val loc = Files.mismatch(path1, path2)
    assertEquals("equal paths to not existing file", -1, loc)
  }

  @Test def mismatch: Unit = {
    val pathA = getCleanIoPath("FileA")
    val pathB = getCleanIoPath("FileB")
    val pathC = getCleanIoPath("FileC")
    val pathD = getCleanIoPath("FileD")

    val rng = new ju.Random(232244L) // an arbitrary amusing value

    val commonLength = 64

    val dataA = new Array[Byte](commonLength)
    rng.nextBytes(dataA)

    val dataB = ju.Arrays.copyOf(dataA, commonLength)

    // Introduce a difference at known offset
    val dataC = ju.Arrays.copyOf(dataA, commonLength)
    val dataCMismatchAt = dataC.length - 3
    dataC(dataCMismatchAt) = 255.toByte

    val dataD = ju.Arrays.copyOf(dataA, commonLength + 5)

    /* Scala 2.12 & Scala 2.13 require Consumer[]. A lambda is idiomatic
     * on Scala 3 but Consumer[] is accepted. Using Consumer[] allows to
     * support all three version.
     */

    class CheckedCopier() extends Consumer[(Array[Byte], Path)] {
      def accept(packet: (Array[Byte], Path)): Unit = {
        val data = packet._1
        val path = packet._2
        val bais = new ByteArrayInputStream(data)
        val n = Files.copy(bais, path)
        assertEquals("copy ${path.toString}", data.length, n)
      }
    }

    val checkedCopier = new CheckedCopier

    jus.Stream
      .of(
        (dataA, pathA),
        (dataB, pathB),
        (dataC, pathC),
        (dataD, pathD)
      )
      .forEach(checkedCopier)

    assertEquals("A == B", -1, Files.mismatch(pathA, pathB))
    assertEquals("A != C", dataCMismatchAt, Files.mismatch(pathA, pathC))

    assertEquals("A < D ", dataA.length, Files.mismatch(pathA, pathD))
    assertEquals("D > A ", dataA.length, Files.mismatch(pathD, pathA))

    // Show comparisons with D do not always return fixed common prefix length
    assertEquals("D == D ", -1, Files.mismatch(pathD, pathD))
  }

}

object FilesTestOnJDK12 {
  private var orgPath: Path = _
  private var workPath: Path = _

  final val testsuitePackagePrefix = "org.scalanative."

  private def getCleanIoPath(fileName: String): Path = {
    val ioPath = workPath.resolve(fileName)
    Files.deleteIfExists(ioPath)
    ioPath
  }

  @BeforeClass
  def beforeClass(): Unit = {
    /* Scala package statement does not allow "-", so the testsuite
     * packages are all "scalanative", not the "scala-native" used
     * in distribution artifacts or the name of the GitHub repository.
     */
    orgPath = Files.createTempDirectory(s"${testsuitePackagePrefix}testsuite")

    val tmpPath =
      orgPath.resolve(s"javalib/nio/file/${this.getClass().getSimpleName()}")
    workPath = Files.createDirectories(tmpPath)
  }

  @AfterClass
  def afterClass(): Unit = {
    // Delete items created by this test.

    // Avoid blind "rm -r /" and other oops! catastrophes.
    if (!orgPath.toString().contains(s"${testsuitePackagePrefix}"))
      fail(s"Refusing recursive delete of unknown path: ${orgPath}")

    // Avoid resize overhead; 64 is a high guess. deque will grow if needed.
    val stack = new ju.ArrayDeque[Path](64)
    val stream = Files.walk(orgPath)

    try {
      // Delete Files; start with deepest & work upwards to beginning of walk.
      stream.forEach(stack.addFirst(_)) // push() Path
      stack.forEach(Files.delete(_)) // pop() a Path then delete its File.
    } finally {
      stream.close()
    }
  }
}
