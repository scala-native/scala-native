package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo._
import scala.scalanative.posix.stdlib
import scala.scalanative.posix.wordexp._
import scala.scalanative.posix.wordexpOps._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

class WordexpTest {

  private def checkWordexpStatus(status: Int, pattern: String): Unit = {
    if (status != 0) {
      val msg =
        if (status == WRDE_BADCHAR) "WRDE_BADCHAR"
        else if (status == WRDE_BADVAL) "WRDE_BADVAL"
        else if (status == WRDE_CMDSUB) "WRDE_CMDSUB"
        else if (status == WRDE_NOSPACE) "WRDE_NOSPACE"
        else if (status == WRDE_SYNTAX) "WRDE_SYNTAX"
        else s"Unknown code: ${status}"

      fail(s"wordexp(${pattern})failed: ${msg}")
    }
  }

  @Test def wordexpExpectBadcharError(): Unit = {
    assumeTrue(
      "wordexp.scala is not implemented on Windows or OpenBSD",
      !isWindows && !isOpenBSD
    )
    if (!isWindows && !isOpenBSD) Zone.acquire { implicit z =>
      val wrdeP = stackalloc[wordexp_t]()

      /* wordexp is defined as using the sh shell. That shell does not
       * allow an out-of-place semicolon on the command line. Show that we
       * are indeed using sh.
       */
      val pattern = "prefix ; suffix"
      val status = wordexp(toCString(pattern), wrdeP, 0)

      try {
        assertEquals("Expected WRDE_BADCHAR error", WRDE_BADCHAR, status)
      } finally {
        wordfree(wrdeP)
      }
    }
  }

  @Test def wordexpTildeExpansion: Unit = {
    assumeTrue(
      "wordexp.scala is not implemented on Windows or OpenBSD",
      !isWindows && !isOpenBSD
    )

    if (!isWindows && !isOpenBSD) Zone.acquire { implicit z =>
      val wrdeP = stackalloc[wordexp_t]()

      val pattern = "~"
      val status = wordexp(toCString(pattern), wrdeP, 0)

      try {
        checkWordexpStatus(status, pattern)

        assertEquals("Unexpected we_wordc", 1, wrdeP.we_wordc.toInt)

        val expected = System.getProperty("user.home", "Alabama")

        assertEquals(
          s"Unexpected expansion of '${pattern}'",
          expected,
          fromCString(wrdeP.we_wordv(0))
        )
      } finally {
        wordfree(wrdeP)
      }
    } // !isWindows && !isOpenBSD
  }

  @Test def wordexpVariableSubstitution: Unit = {
    assumeTrue(
      "wordexp.scala is not implemented on Windows or OpenBSD",
      !isWindows && !isOpenBSD
    )

    /* The environment variable $HOME may not exist on all non-CI systems.
     * Do a 'soft fail' on such systems. This allows running this test
     * on systems where the variable does exist without hard failing in
     * the wild.
     */

    val hasHomeEnvvar = stdlib.getenv(c"HOME")

    assumeTrue(
      "Could not find environment variable named 'HOME'",
      hasHomeEnvvar != null
    )

    if (!isWindows && !isOpenBSD) Zone.acquire { implicit z =>
      val wrdeP = stackalloc[wordexp_t]()

      val pattern = "Phil $HOME Ochs"
      val status = wordexp(toCString(pattern), wrdeP, 0)

      try {
        checkWordexpStatus(status, pattern)

        assertEquals("Unexpected we_wordc", 3, wrdeP.we_wordc.toInt)

        val expected = System.getProperty("user.home", "Mississippi")

        assertEquals(
          s"Unexpected expansion of '${pattern}'",
          expected,
          fromCString(wrdeP.we_wordv(1))
        )
      } finally {
        wordfree(wrdeP)
      }
    } // !isWindows && !isOpenBSD
  }
}
