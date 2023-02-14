package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import FilesTest.withTemporaryDirectory

class DirectoryStreamTest {

  @Test def filesNewDirectoryStreamPath(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0 = dir.resolve("f0")
      val f1 = dir.resolve("f1")
      val d0 = dir.resolve("d0")
      val f2 = d0.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createFile(f2)
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val stream = Files.newDirectoryStream(dir)
      val expected = Set(f0, f1, d0)
      val result = scala.collection.mutable.Set.empty[Path]

      val it = stream.iterator()
      while (it.hasNext()) {
        result += it.next()
      }
      assertTrue(result == expected)
    }
  }

  @Test def filesNewDirectoryStreamPathDirectoryStreamFilterPath(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0 = dir.resolve("f0")
      val f1 = dir.resolve("f1")
      val d0 = dir.resolve("d0")
      val f2 = d0.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createFile(f2)
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val filter = new DirectoryStream.Filter[Path] {
        override def accept(p: Path): Boolean = !p.toString.endsWith("f1")
      }
      val stream = Files.newDirectoryStream(dir, filter)
      val expected = Set(f0, d0)
      val result = scala.collection.mutable.Set.empty[Path]

      val it = stream.iterator()
      while (it.hasNext()) {
        result += it.next()
      }
      assertTrue(result == expected)
    }
  }

  @Test def cannotGetIteratorMoreThanOnce(): Unit = {
    val stream = Files.newDirectoryStream(Paths.get("."))
    stream.iterator()
    assertThrows(classOf[IllegalStateException], stream.iterator())
  }

  @Test def cannotGetAnIteratorAfterClose(): Unit = {
    val stream = Files.newDirectoryStream(Paths.get("."))
    stream.close()
    assertThrows(classOf[IllegalStateException], stream.iterator())
  }

  @Test def hasNextReturnsFalseAfterStreamIsClosed(): Unit = {
    val stream = Files.newDirectoryStream(Paths.get("."))
    val it = stream.iterator()
    stream.close()
    assertFalse(it.hasNext())
    assertThrows(classOf[NoSuchElementException], it.next())
  }

  /* Issue #2937
   *
   * Note Well - This Test is fragile/sensitiveToChange/lessThanRobust.
   *
   * This Test is fragile in the sense that it assumes/requires that
   * the current working directory contains at least one .sbt file.
   * Such is true at the time this test is created. The current working
   * directory when TestMain is started is the project directory. That
   * directory contains a .sbt file because that file was used to start
   * the execution of TestMain; Worm Ouroboros.
   *
   * An alternative approach of saving and restoring the current working
   * directory was considered but judged to be more fragile.
   */
  @Test def normalizesAcceptCandidatePathExpectMatch(): Unit = {

    val passGlob = "*.sbt" // passes in JVM

    /*
    // Path of current working directory, from empty string.
    val emptyPathStream = Files.newDirectoryStream(Paths.get(""), passGlob)
    val emptyPathIt = emptyPathStream.iterator()
    emptyPathStream.close()

    assertTrue(
      s"current working directory stream has no match for '${passGlob}'",
      emptyPathIt.hasNext()
    )
     */

    // Path of current working directory, from dot string.
    val dotPathStream = Files.newDirectoryStream(Paths.get("."), passGlob)
    val dotPathIt = dotPathStream.iterator()

    try {
      assertTrue(
        s"dot directory stream has no match for '${passGlob}'",
        dotPathIt.hasNext()
      )
    } finally {
      dotPathStream.close()
    }
  }

  @Test def normalizesAcceptCandidatePathExpectNoMatch(): Unit = {

    val failGlob = "./*.sbt" // fails in JVM and should fail here

    /*
    // Path of current working directory, from empty string.
    val emptyPathStream = Files.newDirectoryStream(Paths.get(""), failGlob)
    val emptyPathIt = emptyPathStream.iterator()
    emptyPathStream.close()

    assertTrue(
      s"current working directory stream has a match for '${failGlob}'",
      emptyPathIt.hasNext()
    )
     */

    // Path of current working directory, from dot string.
    val dotPathStream = Files.newDirectoryStream(Paths.get("."), failGlob)
    val dotPathIt = dotPathStream.iterator()

    try {
      assertFalse(
        s"dot directory stream has a match for '${failGlob}'",
        dotPathIt.hasNext()
      )
    } finally {
      dotPathStream.close()
    }
  }
}
