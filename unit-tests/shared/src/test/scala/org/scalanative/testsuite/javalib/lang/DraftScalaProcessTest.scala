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

// import org.scalanative.testsuite.utils.Platform

import scala.sys.process // specify the process class & its methods we want.
import scala.sys.process._

class DraftScalaProcessTest {

  @Test def testScalaString(): Unit = {
    /* A simplified version of the reproducer in the base Issue.
     *
     * Does basic Process creation & execution of a non-cmd work on
     * Windows?
     * 
     * Factor out:
     *   - complicated process & argument creation
     *   - ProcessLogger
     */

    /* Straight forward string works on macOS
     *  Does Scala do any magic to handle Windows? prepend "cmd /c"
     */

    val response = "git --version".!!

    /* If execution gets to assertion, then process has exited, not
     * hung. An apparent failure here is really a success.
     */
    assertEquals("process exited", "Lorem ipsum", response)
  }
}
