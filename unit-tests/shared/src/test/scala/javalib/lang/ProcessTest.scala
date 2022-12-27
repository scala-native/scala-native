package javalib.lang

import java.util.concurrent.TimeUnit
import java.io._
import java.nio.file.Files

import scala.io.Source

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.scalanative.testsuite.utils.Platform, Platform._
import scala.scalanative.junit.utils.AssumesHelper._

class ProcessTest {
  import javalib.lang.ProcessUtils._

  @Test def ls(): Unit = {
    val proc =
      if (isWindows) {
        processForCommand(Scripts.ls, "/b", resourceDir).start()
      } else {
        processForCommand(Scripts.ls, resourceDir).start()
      }
    assertProcessExitOrTimeout(proc)
    assertEquals("", readInputStream(proc.getErrorStream()))
    val out = readInputStream(proc.getInputStream())

    assertEquals(scripts, out.split(EOL).toSet)
  }

  private def checkPathOverride(pb: ProcessBuilder) = {
    val proc = pb.start()
    val out = readInputStream(proc.getInputStream) // must read before exit

    assertProcessExitOrTimeout(proc)

    assertEquals("1", out)
  }

  @Test def pathOverride(): Unit = {
    assumeNotJVMCompliant()
    assumeFalse(
      "Not possible in Windows, would use dir keyword anyway",
      isWindows
    )

    val pb = new ProcessBuilder("ls", resourceDir)
    pb.environment.put("PATH", resourceDir)
    checkPathOverride(pb)
  }

  @Test def pathPrefixOverride(): Unit = {
    assumeNotJVMCompliant()
    assumeFalse(
      "Not possible in Windows, would use dir keyword anyway",
      isWindows
    )

    val pb = new ProcessBuilder("ls", resourceDir)
    pb.environment.put("PATH", s"$resourceDir:${pb.environment.get("PATH")}")
    checkPathOverride(pb)
  }

  @Test def inputAndErrorStream(): Unit = {
    val proc = processForScript(Scripts.err).start()

    assertProcessExitOrTimeout(proc)

    assertEquals("foo", readInputStream(proc.getErrorStream))
    assertEquals("bar", readInputStream(proc.getInputStream))
  }

  @Test def inputStreamWritesToFile(): Unit = {
    val file = File.createTempFile(
      "istest",
      ".tmp",
      new File(System.getProperty("java.io.tmpdir"))
    )

    val proc = processForScript(Scripts.echo)
      .redirectOutput(file)
      .start()

    try {
      proc.getOutputStream.write(s"hello$EOL".getBytes)
      proc.getOutputStream.write(s"quit$EOL".getBytes)
      proc.getOutputStream.flush()
      if (isWindows) {
        // Currently used batch script needs output stream to be closed
        proc.getOutputStream.close()
      }
      assertProcessExitOrTimeout(proc)
      assertEquals("", readInputStream(proc.getErrorStream()))
      val out = Source.fromFile(file.toString).getLines().mkString

      assertEquals("hello", out)
    } finally {
      file.delete()
    }
  }

  @Test def outputStreamReadsFromFile(): Unit = {
    val file = File.createTempFile(
      "istest",
      ".tmp",
      new File(System.getProperty("java.io.tmpdir"))
    )
    val pb = processForScript(Scripts.echo)
      .redirectInput(file)

    try {
      val os = new FileOutputStream(file)
      os.write(s"hello$EOL".getBytes)
      os.write(s"quit$EOL".getBytes)
      os.flush()

      val proc = pb.start()
      assertProcessExitOrTimeout(proc)
      assertEquals("", readInputStream(proc.getErrorStream()))
      assertEquals("hello", readInputStream(proc.getInputStream).trim)
    } finally {
      file.delete()
    }
  }

  @Test def redirectErrorStream(): Unit = {
    val proc = processForScript(Scripts.err)
      .redirectErrorStream(true)
      .start()

    assertProcessExitOrTimeout(proc)

    assertEquals("", readInputStream(proc.getErrorStream))
    assertEquals("foobar", readInputStream(proc.getInputStream))
  }

  @Test def waitForWithTimeoutCompletes(): Unit = {
    val proc = processSleep(0.1).start()

    /* This is another Receiver Operating Characteristic (ROC) curve
     * decision, where one tries to balance the rates of true failure
     * and false failure detection.
     *
     * On contemporary machines, even virtual machines, a process should
     * take only a few seconds to exit. Then there is Windows. Many CI
     * failures having nothing to do with the PR under test have been seen,
     * mostly on Windows, to have failed here with the previous
     * "reasonable & conservative" value of 4. No best guess long survives
     * first contact with the facts on the ground (actually, I think that
     * was a 10th, or more, best guess).
     */

    val timeout = 30
    assertTrue(
      s"process should have exited but timed out (limit: ${timeout} seconds)",
      proc.waitFor(timeout, TimeUnit.SECONDS)
    )
    assertEquals(0, proc.exitValue)
  }

  // Design Notes:
  //   1) The timing on the next few tests is pretty tight and subject
  //      to race conditions.
  //
  //      The waitFor(100, TimeUnit.MILLISECONDS) assumes that the
  //      process has not lived its lifetime by the time it
  //      executes, a race condition.  Just because two instructions are
  //      right next to each other, does not mean they execute without
  //      intervening interruption or significant elapsed time.
  //
  //      This section has been hand tweaked for the __slow__ conditions
  //      of Travis CI. It may still show intermittent failures, requiring
  //      re-tweaking.
  //
  //   2) The code below has zombie process mitigation code.  That is,
  //      It assumes a competent destroyForcibly() and attempts to force
  //      processes which _should_have_ exited on their own to do so.
  //
  //      A number of other tests in this file have the potential to
  //      strand zombie processes and are candidates for a similar fix.

  @Test def waitForWithTimeoutTimesOut(): Unit = {
    val proc = processSleep(2.0).start()

    val timeout = 500 // Make message distinguished.
    assertTrue(
      "process should have timed out but exited" +
        s" (limit: ${timeout} milliseconds)",
      !proc.waitFor(timeout, TimeUnit.MILLISECONDS)
    )
    assertTrue("process should be alive", proc.isAlive)

    // await exit code to release resources. Attempt to force
    // hanging processes to exit.
    if (!proc.waitFor(10, TimeUnit.SECONDS))
      proc.destroyForcibly()
  }

  private def processForDestruction(): Process = {
    /* Return a Process that is suitable to receive a SIGTERM or SIGKILL
     * signal and return that signal as its exit code.
     *
     * The underlying operating system (OS) process must be in the prime of
     * its life; not too young, not too old.
     *
     * Specifically,the signal must be delivered after OS process calls one
     * of the 'exec' family and before it completes on its own and exits
     * with an "unexpected" exit code.
     *
     * See Issue #2759 for an extended discussion.
     */

    /* "ping" is used here as a timing ~~hack~~ felicity, not
     * to do anything actually sensible with a network.
     *
     * Send two packets, one immediately sends I/O to parent.
     * Then the process expects to live long enough to send a second
     * in 10 seconds. When either SIGTERM or SIGKILL arrives, only the
     * necessary minimum time will have actually been taken.
     */
    val proc = processForCommand("ping", "-c", "2", "-i", "10", "127.0.0.1")
      .start()

    // When process has produced a byte of output, it should be past 'exec'.
    proc.getInputStream().read()

    proc
  }

  @Test def destroy(): Unit = {
    val proc = processForDestruction()

    proc.destroy()

    val timeout = 501 // Make message distinguished.
    assertTrue(
      "process should have exited but timed out" +
        s" (limit: ${timeout} milliseconds)",
      proc.waitFor(timeout, TimeUnit.MILLISECONDS)
    )
    assertEquals(
      // SIGTERM, use unix signal 'excess 128' convention on non-Windows.
      if (isWindows) 1 else 0x80 + 15,
      proc.exitValue
    )
  }

  @Test def destroyForcibly(): Unit = {
    val proc = processForDestruction()

    proc.destroyForcibly()

    val timeout = 502 // Make message distinguished.
    assertTrue(
      "process should have exited but timed out" +
        s" (limit: ${timeout} milliseconds)",
      proc.waitFor(timeout, TimeUnit.MILLISECONDS)
    )
    assertEquals(
      // SIGKILL, use unix signal 'excess 128' convention on non-Windows.
      if (isWindows) 1 else 0x80 + 9,
      proc.exitValue
    )
  }

  @Test def shellFallback(): Unit = {
    val proc = processForScript(Scripts.hello).start()

    assertProcessExitOrTimeout(proc)
    assertEquals("", readInputStream(proc.getErrorStream()))
    assertEquals(s"hello$EOL", readInputStream(proc.getInputStream()))
  }
}
