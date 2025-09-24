package org.scalanative.testsuite.javalib.lang

/* This is a DEBUGGING DRAFT Test for  SN Issue #4357.
 * That issue discusses Scala sys.process.Process hangs on Windows.
 *
 * I suspect that I will end up debugging javalib Process, so I am
 * creating this test in the testsuite.javalib.lang. If it becomes
 * useful in SN mainling Git, it can be moved to a better home.
 *
 * For now, it is a good bootstrap.
 */

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.Platform

import java.io.File

import scala.sys.process // specify the process class & its methods we want.
import scala.sys.process._
import scala.sys.process.ProcessLogger._

class DraftScalaProcessTest {

  @Test def testScalaSimplifiedReproducer(): Unit = {

    val response = "git init -b main".!!

    /* If execution gets to assertion, then child process has exited, not
     * hung.
     *
     * If execution gets past assertion, then child process has done expected
     * work, before exiting.
     */

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

  @Test def testScalaCloseToOriginaReproducer(): Unit = {
    /* Close to the Issue original reproducer, no logger
     */

    val argv = Seq("git", "init", "-b", "main")
    val cwd: Option[File] = None

    val response = sys.process.Process(argv, None).!!

    /* If execution gets to assertion, then child process has exited, not
     * hung.
     *
     * If execution gets past assertion, then process has done expected
     * work, before exiting.
     */

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

  @Test def testScalaOriginaReproducer(): Unit = {
    /* The Issue original reproducer
     */

    /* 2025-09-24 12:16 -0400 Enable this Test on Windows, simpler Tests work.
    // Basic Process(argv, cmd) hangs on Windows,
    // do not test there until basic Process .!! works.
    // assumeFalse("Not tested in Windows", Platform.isWindows)
     */

    val argv = Seq("git", "init", "-b", "main")
    val cwd: Option[File] = None

    val logger =
      ProcessLogger(_ => (), x => System.err.append("\n> ").append(s"$x\n\n"))

    val response = sys.process.Process(argv, None).!!(logger)

    /* If execution gets to assertion, then child process has exited, not
     * hung.
     *
     * If execution gets past assertion, then child process has done expected
     * work, before exiting.
     */

    assertTrue(
      s"process logger response): <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }
}
