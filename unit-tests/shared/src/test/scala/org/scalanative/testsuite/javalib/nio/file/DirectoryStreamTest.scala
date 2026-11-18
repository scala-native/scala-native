package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import FilesTest.withTemporaryDirectory

class DirectoryStreamTest {

  // Issue #3898
  @Test def closeReleasesDirectory(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath().resolve("listed")
      for (_ <- 1 to 2) {
        Files.createDirectory(dir)
        for (j <- 1 to 3) Files.createFile(dir.resolve(s"file$j"))

        val stream = Files.newDirectoryStream(dir)
        val it = stream.iterator()
        assertTrue("hasNext", it.hasNext())
        assertNotNull("next", it.next())
        stream.close()
        stream.close()

        for (j <- 1 to 3) Files.delete(dir.resolve(s"file$j"))
        Files.delete(dir)
        assertFalse("dir deleted", Files.exists(dir))
      }
    }
  }

  // Issue #3898: close() from another thread must not race native reads.
  @Test def closeConcurrentWithIteration(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val nFiles = 200
      for (j <- 0 until nFiles) Files.createFile(dir.resolve(s"file$j"))

      for (_ <- 1 to 50) {
        val stream = Files.newDirectoryStream(dir)
        val it = stream.iterator()
        val started = new java.util.concurrent.CountDownLatch(1)
        @volatile var seen = 0
        @volatile var failure: Throwable = null

        val reader = new Thread(() =>
          try {
            started.countDown()
            while (it.hasNext()) {
              assertNotNull(it.next())
              seen += 1
            }
          } catch { case t: Throwable => failure = t }
        )
        reader.start()
        started.await()
        stream.close()
        reader.join()

        if (failure != null) throw failure
        assertTrue(s"seen $seen", seen <= nFiles)
        assertFalse("hasNext after close", it.hasNext())
      }
    }
  }

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
    val suffix = ".dirstreamtest"
    val passGlob = "*" + suffix
    val fixture = Files.createTempFile(Paths.get("."), "fixture", suffix)

    def hasMatch(dir: String): Boolean = {
      val stream = Files.newDirectoryStream(Paths.get(dir), passGlob)
      try stream.iterator().hasNext()
      finally stream.close()
    }

    try {
      assertTrue(
        s"current working directory stream has no match for '${passGlob}'",
        hasMatch("")
      )
      assertTrue(
        s"dot directory stream has no match for '${passGlob}'",
        hasMatch(".")
      )
    } finally Files.delete(fixture)
  }

  @Test def normalizesAcceptCandidatePathExpectNoMatch(): Unit = {

    val failGlob = "./*.sbt" // fails in JVM and should fail here

    // Path of current working directory, from empty string.
    val emptyPathStream = Files.newDirectoryStream(Paths.get(""), failGlob)
    val emptyPathPassed = emptyPathStream.iterator().hasNext() // count >= 1
    emptyPathStream.close()

    assertFalse(
      s"current working directory stream has a match for '${failGlob}'",
      emptyPathPassed
    )

    // Path of current working directory, from dot string.
    val dotPathStream = Files.newDirectoryStream(Paths.get("."), failGlob)
    val dotPathPassed = dotPathStream.iterator().hasNext() // count >= 1
    dotPathStream.close()

    assertFalse(
      s"dot directory stream has a match for '${failGlob}'",
      dotPathPassed
    )
  }
}
