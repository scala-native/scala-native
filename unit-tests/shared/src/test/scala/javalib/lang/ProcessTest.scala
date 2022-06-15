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

    assertTrue(
      "process should have exited but timed out",
      proc.waitFor(4, TimeUnit.SECONDS)
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

    assertTrue(
      "process should have timed out but exited",
      !proc.waitFor(500, TimeUnit.MILLISECONDS)
    )
    assertTrue("process should be alive", proc.isAlive)

    // await exit code to release resources. Attempt to force
    // hanging processes to exit.
    if (!proc.waitFor(10, TimeUnit.SECONDS))
      proc.destroyForcibly()
  }

  @Test def destroy(): Unit = {
    assumeFalse(
      // Fails with traceback on mac arm64 and maybe others.
      // See Issue #2648
      "Test is available on arm64 hardware only when using JVM",
      Platform.hasArm64SignalQuirk
    )
    val proc = processSleep(2.0).start()

    assertTrue("process should be alive", proc.isAlive)
    proc.destroy()
    assertTrue(
      "process should have exited but timed out",
      proc.waitFor(500, TimeUnit.MILLISECONDS)
    )
    assertEquals(
      // SIGTERM, use unix signal 'excess 128' convention on non-Windows.
      if (isWindows) 1 else 0x80 + 15,
      proc.exitValue
    )
  }

  @Test def destroyForcibly(): Unit = {
    assumeFalse(
      // Fails with traceback on mac arm64 and maybe others.
      // See Issue #2648
      "Test is available on arm64 hardware only when using JVM",
      Platform.hasArm64SignalQuirk
    )
    val proc = processSleep(2.0).start()

    assertTrue("process should be alive", proc.isAlive)
    val p = proc.destroyForcibly()
    assertTrue(
      "process should have exited but timed out",
      p.waitFor(500, TimeUnit.MILLISECONDS)
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
