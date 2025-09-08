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

import scala.sys.process // specify the process class & its methods we want.
import scala.sys.process._

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

  @Test def testScalaString_2(): Unit = {
    /* Get a small change closer to the Issue reproducer.
     */

    /* Second pass results, using SN 0.5.9-SNAPSHOT:
     * 
     *   + ???
     */

    val response = "git init -b main".!!

    /* If execution gets to assertion, then process has exited, not
     * hung.
     */

    /* Be careful when running manually more than once.  Before second
     * and subsequent manual runs, one needs to manually delete prior
     * created .git directories such as, say, ./unit-tests/jvm/.3/.git
     * Otherwise one gets: warning: re-init: ignored --initial-branch=main
     * Those fail the assertion and leave one hunting for where the
     * prior .git was created.
     * 
     * This could be handled in a production test. For now I want to
     * stay as close as I can to the original Issue.
     */

    assertTrue(
      "process response",
      response.startsWith("Initialized empty Git repository in")
    )
  }
}
