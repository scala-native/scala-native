package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.{BeforeClass, AfterClass}

import scala.scalanative.meta.LinktimeInfo.{isWindows, isNetBSD}

import java.nio.file.{Path, Paths}
import java.nio.file.Files

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.glob._
import scala.scalanative.posix.globOps._

object GlobTest {
  private var orgDir: Path = _
  private var posixlibDir: Path = _
  private var workDir: Path = _

  private var createdFilePaths: List[Path] = _

  private def createTestData(dir: Path): List[Path] = {
    List("a.no", "b.yes", "c.no", "d.yes", "e.no").map(fname =>
      Files.createFile(dir.resolve(fname))
    )
  }

  @BeforeClass
  def beforeClass(): Unit = {
    if (!isWindows) {
      orgDir = Files.createTempDirectory("org.scalanative.testsuite")
      posixlibDir = orgDir.resolve("posixlib")
      workDir = Files.createDirectories(posixlibDir.resolve("GlobTest"))

      createdFilePaths = createTestData(workDir)
    }
  }

  @AfterClass
  def afterClass(): Unit = {
    if (!isWindows) {
      /* Delete items created by this test.
       * Delete files within "GlobTest" directory and then the directory itself,
       * its parent & grandparent.
       */
      val deleteList = createdFilePaths :+ workDir :+ posixlibDir :+ orgDir
      deleteList.foreach(p => Files.delete(p))
    }
  }
}

class GlobTest {
  import GlobTest._

  private def checkGlobStatus(status: Int, pattern: String): Unit = {
    if (status != 0) {
      val msg =
        if (status == GLOB_ABORTED) "GLOB_ABORTED"
        else if (status == GLOB_NOMATCH) "GLOB_NOMATCH"
        else if (status == GLOB_NOSPACE) "GLOB_NOSPACE"
        else s"Unknown code: ${status}"

      fail(s"glob(${pattern})failed: ${msg}")
    }
  }

  @Test def globExpectNotFound(): Unit = {
    assumeTrue(
      "glob.scala is not implemented on Windows",
      !isWindows
    )

    if (!isWindows) Zone.acquire { implicit z =>
      val globP = stackalloc[glob_t]()

      val wdAbsP = workDir.toAbsolutePath()
      val pattern = s"${wdAbsP}/*.NONEXISTENT"

      val status = glob(toCString(pattern), 0, null, globP)

      if (status != GLOB_NOMATCH) {
        if (status != 0) checkGlobStatus(status, pattern)
        else {
          val found = fromCString(globP.gl_pathv(0))
          fail(s"Unexpected match, pattern: '${pattern}' found: '${found}'")
        }
      }

      globfree(globP) // should never get here, but if here, do not leak memory

    } // !isWindows
  }

  @Test def globExpectFound(): Unit = {
    assumeTrue(
      "glob.scala is not implemented on Windows",
      !isWindows
    )

    assumeTrue(
      "glob seems doesn't work on NetBSD",
      !isNetBSD
    )

    if (!isWindows && !isNetBSD) Zone.acquire { implicit z =>
      val globP = stackalloc[glob_t]()

      val wdAbsP = workDir.toAbsolutePath()
      val pattern = s"${wdAbsP}/*.yes"

      val status = glob(toCString(pattern), 0, null, globP)

      try {
        checkGlobStatus(status, pattern)

        assertEquals("Unexpected gl_pathc", 2, globP.gl_pathc.toInt)

        // by default glob() vector is sorted, sort expected to match.
        val expected = Array("b.yes", "d.yes")

        for (j <- 0 until globP.gl_pathc.toInt)
          assertEquals(
            "Unexpected match found",
            s"${wdAbsP}/${expected(j)}",
            fromCString(globP.gl_pathv(j))
          )
      } finally {
        globfree(globP)
      }
    } // !isWindows
  }
}
