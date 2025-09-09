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

import org.scalanative.testsuite.utils.Platform

import java.io.File
import java.{lang => jl}
import java.{util => ju}

import scala.sys.process // specify the process class & its methods we want.
import scala.sys.process._
import scala.sys.process.ProcessLogger._

class DraftScalaProcessTest {

  lazy val rng = new ju.SplittableRandom()

  def makeRandomDirName(): String = {
    // Jitter the name to ease running the Test manually more than once.
    // Hack around lack of usable java.util.UUID on Scala Native,

    val suffix = rng.nextLong(0L, jl.Long.MAX_VALUE).toString

    s"WindowsProcessDebug_${suffix}"
  }

  @Ignore // Passes, JVM & SN
  @Test def testScalaString_0(): Unit = {
    /* Avoid doing IO; see if process exits cleanly.
     * 
     * Try to factor out parent/child pipe I/O handling
     * I/O goes to shared stdout, not write end of pipe.
     */

    val dirName = makeRandomDirName()

    // 'mkdir' takes no input and gives no output.
    val commandString = s"mkdir ${dirName}"

    val proc = commandString.run(connectInput = false)

    Thread.sleep(1000 * 20) // seconds, be generous to avoid flakey failures

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  @Ignore
  @Test def testScalaString_0_A(): Unit = {
    /* Avoid doing IO; see if process exits cleanly.
     * 
     * Try to factor out parent/child pipe I/O handling
     * I/O goes to shared stdout, not write end of pipe.
     */

    val dirName = makeRandomDirName()

    // 'mkdir' takes no input and gives no output.
    val commandString = s"mkdir ${dirName}"

    // 'mkdir' takes no input and gives no output.
    //  Use .!! to connect parent/child I/Os but never read or write on them

    val proc = sys.process.Process(commandString)

    // Lee: Careful lazyLines is Scala 2.13 & 3 only.
    //      Will fail to compile in CI for Scala 2.12, but the other
    //      cases are may tell me something.

    // Will throw Exception if process exits with error code.
    val response = proc.lazyLines

    response.foreach(x => {
      assertEquals("foreach", "nevermore", x) // Fail if any substantial I/O
    })

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case: Make it evident that Windows process exited")
      }
  }

  @Ignore
  @Test def testScalaString_0_B(): Unit = {
    /* Do one line of I/O using lazyList; see if process exits cleanly.
     */

    val commandString = "git init -b main"

    val proc = sys.process.Process(commandString)

    // Lee: Careful lazyLines is Scala 2.13 & 3 only.
    //      Will fail to compile in CI for Scala 2.12, but the other
    //      cases are may tell me something.

    // Will throw Exception if process exits with error code.
    val response = proc.lazyLines

    response
      .take(1)
      .foreach(x => {
        assertTrue(
          s"foreach: '${x}'",
          (x.startsWith("Initialized empty Git repository in") ||
            x.startsWith("Reinitialized existing Git repository in"))
        )
      })

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case 0_B: Make it evident that Windows process exited")
      }
  }

  @Ignore // Appears to pass on Windows Scala 3 & 2.13
  @Test def testScalaString_0_C(): Unit = {
    /* This is testScalaString_2() with a stronger test for process exit.
     * Do results match?
     */

    val commandString = "git init -b main"

    val proc = sys.process.Process(commandString)

    val response = proc.!!

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case 0_C: Make it evident that Windows process exited")
      }
  }

  @Ignore // Appears to pass on Windows Scala 3 & 2.13
  @Test def testScalaString_0_D(): Unit = {
    /* This is testScalaString_0_B modified to use
     * a Process class overload which is closer to the Issue reproducer.
     * The method of starting & reading from the resultant ProcessBuilder
     * is modified to be what worked in testScalaString_0_A.
     * Do things continue to work?
     */

    val argv = Seq("git", "init", "-b", "main")
    val cwd: Option[File] = None

    val proc = sys.process.Process(argv, None)

    // Lee: Careful lazyLines is Scala 2.13 & 3 only.
    //      Will fail to compile in CI for Scala 2.12, but the other
    //      cases are may tell me something.

    // Will throw Exception if process exits with error code.
    val response = proc.lazyLines

    response
      .take(1)
      .foreach(x => {
        assertTrue(
          s"foreach: '${x}'",
          (x.startsWith("Initialized empty Git repository in") ||
            x.startsWith("Reinitialized existing Git repository in"))
        )
      })

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case 0_D: Make it evident that Windows process exited")
      }
  }

  // @Ignore  // Coal face
  @Test def testScalaString_0_E(): Unit = {
    /* This is testScalaString_0_E modified to use
     * a deprecated lineStream() rather than lazyLines().
     * The !! method appears to use a lineStream.
     * Do things still work?
     */

    val argv = Seq("git", "init", "-b", "main")
    val cwd: Option[File] = None

    val proc = sys.process.Process(argv, None)

    // Lee: Careful lazyLines is Scala 2.13 & 3 only.
    //      Will fail to compile in CI for Scala 2.12, but the other
    //      cases are may tell me something.

    // Will throw Exception if process exits with error code.
    val response = proc.lineStream

    response
      .take(1)
      .foreach(x => {
        assertTrue(
          s"foreach: '${x}'",
          (x.startsWith("Initialized empty Git repository in") ||
            x.startsWith("Reinitialized existing Git repository in"))
        )
      })

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case 0_E: Make it evident that Windows process exited")
      }
  }

  @Ignore // Fails on Windows
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

    val response = "git --version".!!

    /* If execution gets past assertion, then process has done expected
     * work, then exited.
     */
    assertTrue(
      s"process response: <${response}>",
      response.startsWith("git version ")
    )
  }

  @Ignore
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
