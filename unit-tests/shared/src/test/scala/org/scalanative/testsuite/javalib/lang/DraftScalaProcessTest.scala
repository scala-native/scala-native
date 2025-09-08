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
import org.junit.Ignore

// import org.scalanative.testsuite.utils.Platform

import java.io.File

import scala.sys.process // specify the process class & its methods we want.
import scala.sys.process._
import scala.sys.process.ProcessLogger._

class DraftScalaProcessTest {

  @Ignore
  @Test def testScalaString_1(): Unit = {
    /* A simplified version of the reproducer in the base Issue.
     *
     * Does basic Process creation & execution of a non-cmd work on
     * Windows?
     * 
     * Factor out:
     *   - complicated process & argument creation
     *   - ProcessLogger
     */

    /* First pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + the same string works for Linux, macOS, & Windows on
     *     both JDK & Scala Native
     * 
     *   + The assert is reached on {Linux, macOS, & Windows} x {JDK, SN}
     *     No apparent hang in this basic step.
     */

    val response = "git --version".!!

    /* If execution gets to assertion, then process has exited, not
     * hung. An apparent failure here is really a success.
     */
    assertEquals("process exited", "Lorem ipsum", response)
  }

  // @Ignore // Coal face
  @Test def testScalaString_2(): Unit = {
    /* Get a small change closer to the Issue reproducer.
     */

    /* Nth pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + ???
     */

    val response = "git init -b main".!!

    /* If execution gets to assertion, then process has exited, not
     * hung.
     */

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

  @Ignore
  @Test def testScalaString_3(): Unit = {
    /* Get a small change closer to the Issue reproducer.
     */

    /* Nth pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + ???
     */

    val response = Seq("git", "init", "-b", "main").!!

    /* If execution gets to assertion, then process has exited, not
     * hung.
     */

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

  @Ignore // Appears to fail on Windows
  @Test def testScalaString_4(): Unit = {
    /* Start using Scala Process() overload, first with String
     */

    /* Nth pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + ???
     */

    val response = sys.process.Process("git init -b main").!!

    /* If execution gets to assertion, then process has exited, not
     * hung.
     */

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

  @Ignore // Appears to hang on Windows SN (only)
  @Test def testScalaString_5(): Unit = {
    /* Get a small change closer to the Issue reproducer.
     */

    /* Nth pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + Appears to hang on Windows SN (only)
     *      ???

     */

    val argv = Seq("git", "init", "-b", "main")
    val cwd: Option[File] = None

    val response = sys.process.Process(argv, None).!!

    /* If execution gets to assertion, then process has exited, not
     * hung.
     *
     * If execution gets past assertion, then process has done expected
     * work, then exited.
     */

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

  @Ignore // Un- tested, basic Process(argv, cmd) hangs, do not advance
  @Test def testScalaString_6(): Unit = {
    /* Add in a logger
     */

    /* Nth pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + Appears to work,
     * 
     *     Runs after the first may see a non-fatal warning from Git:
     *       warning: re-init: ignored --initial-branch=main
     *     That is OK.
     */

    val argv = Seq("git", "init", "-b", "main")
    val cwd: Option[File] = None

    val logger =
      ProcessLogger(_ => (), x => System.err.append("\n> ").append(s"$x\n\n"))

    val response = sys.process.Process(argv, None).!!(logger)

    /* If execution gets to assertion, then process has exited, not
     * hung.
     *
     * If execution gets past assertion, then process has done expected
     * work, then exited.
     */

    assertTrue(
      s"process logger response): <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )
  }

}
