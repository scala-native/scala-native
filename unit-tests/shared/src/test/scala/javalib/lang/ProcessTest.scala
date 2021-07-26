package javalib.lang

import java.util.concurrent.TimeUnit
import java.io._
import java.nio.file.Files

import scala.io.Source

import org.junit.Test
import org.junit.Assert._

class ProcessTest {
  import javalib.lang.ProcessUtils._

  @Test def ls(): Unit = {
    val proc = new ProcessBuilder("ls", resourceDir).start()
    val out = readInputStream(proc.getInputStream)

    assertProcessExitOrTimeout(proc)

    assertEquals(scripts, out.split("\n").toSet)
  }

  private def checkPathOverride(pb: ProcessBuilder) = {
    val proc = pb.start()
    val out = readInputStream(proc.getInputStream) // must read before exit

    assertProcessExitOrTimeout(proc)

    assertEquals("1", out)
  }

  @Test def pathOverride(): Unit = {
    val pb = new ProcessBuilder("ls", resourceDir)
    pb.environment.put("PATH", resourceDir)
    checkPathOverride(pb)
  }

  @Test def pathPrefixOverride(): Unit = {
    val pb = new ProcessBuilder("ls", resourceDir)
    pb.environment.put("PATH", s"$resourceDir:${pb.environment.get("PATH")}")
    checkPathOverride(pb)
  }

  @Test def inputAndErrorStream(): Unit = {
    val pb = new ProcessBuilder("err.sh")
    val cwd = System.getProperty("user.dir")
    pb.environment.put(
      "PATH",
      s"$cwd/unit-tests/shared/src/test/resources/process"
    )
    val proc = pb.start()

    assertProcessExitOrTimeout(proc)

    assertEquals("foo", readInputStream(proc.getErrorStream))
    assertEquals("bar", readInputStream(proc.getInputStream))
  }

  @Test def inputStreamWritesToFile(): Unit = {
    val pb = new ProcessBuilder("echo.sh")
    pb.environment.put("PATH", resourceDir)
    val file = File.createTempFile("istest", ".tmp", new File("/tmp"))
    pb.redirectOutput(file)

    try {
      val proc = pb.start()
      proc.getOutputStream.write("hello\n".getBytes)
      proc.getOutputStream.write("quit\n".getBytes)
      proc.getOutputStream.flush()

      assertProcessExitOrTimeout(proc)

      val out = Source.fromFile(file.toString).getLines mkString "\n"
      assertEquals("hello", out)
    } finally {
      file.delete()
    }
  }

  @Test def outputStreamReadsFromFile(): Unit = {
    val pb = new ProcessBuilder("echo.sh")
    pb.environment.put("PATH", resourceDir)
    val file = File.createTempFile("istest", ".tmp", new File("/tmp"))
    pb.redirectInput(file)

    try {
      val os = new FileOutputStream(file)
      os.write("hello\n".getBytes)
      os.write("quit\n".getBytes)

      val proc = pb.start()
      assertProcessExitOrTimeout(proc)

      assertEquals("hello", readInputStream(proc.getInputStream))
    } finally {
      file.delete()
    }
  }

  @Test def redirectErrorStream(): Unit = {
    val pb = new ProcessBuilder("err.sh")
    val cwd = System.getProperty("user.dir")
    pb.environment.put(
      "PATH",
      s"$cwd/unit-tests/shared/src/test/resources/process"
    )
    pb.redirectErrorStream(true)
    val proc = pb.start()

    assertProcessExitOrTimeout(proc)

    assertEquals("", readInputStream(proc.getErrorStream))
    assertEquals("foobar", readInputStream(proc.getInputStream))
  }

  @Test def waitForWithTimeoutCompletes(): Unit = {
    val proc = new ProcessBuilder("sleep", "0.1").start()

    assertTrue(
      "process should have exited but timed out",
      proc.waitFor(1, TimeUnit.SECONDS)
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
    val proc = new ProcessBuilder("sleep", "2.0").start()

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
    val proc = new ProcessBuilder("sleep", "2.0").start()

    assertTrue("process should be alive", proc.isAlive)
    proc.destroy()
    assertTrue(
      "process should have exited but timed out",
      proc.waitFor(500, TimeUnit.MILLISECONDS)
    )
    assertEquals(0x80 + 9, proc.exitValue) // SIGKILL, excess 128
  }

  @Test def destroyForcibly(): Unit = {
    val proc = new ProcessBuilder("sleep", "2.0").start()

    assertTrue("process should be alive", proc.isAlive)
    val p = proc.destroyForcibly()
    assertTrue(
      "process should have exited but timed out",
      p.waitFor(500, TimeUnit.MILLISECONDS)
    )
    assertEquals(0x80 + 9, p.exitValue) // SIGKILL, excess 128
  }

  @Test def shellFallback(): Unit = {
    val pb = new ProcessBuilder("hello.sh")
    pb.environment.put("PATH", resourceDir)
    val proc = pb.start()

    assertProcessExitOrTimeout(proc)

    assertEquals("hello\n", readInputStream(proc.getInputStream))
  }
}
