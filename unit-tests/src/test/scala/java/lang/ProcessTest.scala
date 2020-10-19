package java.lang

import java.util.concurrent.TimeUnit
import java.io._
import java.nio.file.Files

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.posix.{fcntl, unistd}
import scala.io.Source

import org.junit.Test
import org.junit.Assert._

class ProcessTest {
  import ProcessUtils._

  private def assertProcessExitOrTimeout(process: Process): Unit = {
    // Suspend execution of the test until either the specified
    // process has exited or a reasonable wait period has timed out.
    //
    // A waitFor() prevents zombie processes and makes the exit value
    // available. A timed waitfor means the test will eventually complete,
    // even if there is a problem with the underlying process.
    //
    // In the normal case, the process will exit within milliseconds or less.
    // The timeout will not increase the expected execution time of the test.
    //
    // Five seconds is an order of magnitude guess for a "reasonable"
    // completion time.  If a process expected to exit in milliseconds
    // takes that three orders of magnitude longer, it must be reported.

    val tmo    = 5
    val tmUnit = TimeUnit.SECONDS

    assertTrue(s"Process took more than $tmo ${tmUnit.name} to exit.",
               process.waitFor(tmo, tmUnit))
  }

  val scripts = Set("echo.sh", "err.sh", "ls", "hello.sh")

  @Test def ls(): Unit = {
    val proc = new ProcessBuilder("ls", resourceDir).start()
    val out  = readInputStream(proc.getInputStream)

    assertProcessExitOrTimeout(proc)

    assertTrue(out.split("\n").toSet == scripts)
  }

  @Test def inherit(): Unit = {
    val f       = Files.createTempFile("/tmp", "out")
    val savedFD = unistd.dup(unistd.STDOUT_FILENO)
    val flags   = fcntl.O_RDWR | fcntl.O_TRUNC | fcntl.O_CREAT
    val fd = Zone { implicit z =>
      fcntl.open(toCString(f.toAbsolutePath.toString), flags, 0.toUInt)
    }
    val out =
      try {
        unistd.dup2(fd, unistd.STDOUT_FILENO)
        fcntl.close(fd)
        val proc = new ProcessBuilder("ls", resourceDir).inheritIO().start()
        proc.waitFor(5, TimeUnit.SECONDS)
        readInputStream(new FileInputStream(f.toFile))
      } finally {
        unistd.dup2(savedFD, unistd.STDOUT_FILENO)
        fcntl.close(savedFD)
      }
    assertTrue(out.split("\n").toSet == scripts)
  }

  private def checkPathOverride(pb: ProcessBuilder) = {
    val proc = pb.start()
    val out  = readInputStream(proc.getInputStream)

    assertProcessExitOrTimeout(proc)

    assertTrue(out == "1")
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
    val pb  = new ProcessBuilder("err.sh")
    val cwd = System.getProperty("user.dir")
    pb.environment.put("PATH", s"$cwd/unit-tests/src/test/resources/process")
    val proc = pb.start()

    assertProcessExitOrTimeout(proc)

    assertTrue(readInputStream(proc.getErrorStream) == "foo")
    assertTrue(readInputStream(proc.getInputStream) == "bar")
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
      assertTrue(out == "hello")
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
      val proc = pb.start()
      val os   = new FileOutputStream(file)
      os.write("hello\n".getBytes)
      os.write("quit\n".getBytes)

      assertProcessExitOrTimeout(proc)

      val out = readInputStream(proc.getInputStream)
      assertTrue(out == "hello")
    } finally {
      file.delete()
    }
  }

  @Test def redirectErrorStream(): Unit = {
    val pb  = new ProcessBuilder("err.sh")
    val cwd = System.getProperty("user.dir")
    pb.environment.put("PATH", s"$cwd/unit-tests/src/test/resources/process")
    pb.redirectErrorStream(true)
    val proc = pb.start()

    assertProcessExitOrTimeout(proc)

    val out = readInputStream(proc.getInputStream)
    val err = readInputStream(proc.getErrorStream)
    assertTrue(out == "foobar")
    assertTrue(err == "")
  }

  @Test def waitForWithTimeoutCompletes(): Unit = {
    val proc = new ProcessBuilder("sleep", "0.001").start()
    assertTrue(proc.waitFor(1, TimeUnit.SECONDS))
    assertTrue(proc.exitValue == 0)
  }

  // Design Note:
  //  The timing on the next few tests is pretty tight and subject
  //  to race conditions. The process is intended to take only 5 milliseconds
  //  overall. The waitFor(1. TimeUnit.MILLISECONDS) assumes that the
  //  process has not lived its lifetime by the time the assertTrue()
  //  executes, a race condition.  Just because two instructions are
  //  right next to each other, does not mean they execute without
  //  intervening interruption or significant elapsed time.
  //
  //  The normal solution would be to increase the expected lifetime
  //  of process. The short 5 millisecond delay has already caught
  //  at least one Clang bug, so let it be until the race trips up
  //  someone else.

  @Test def waitForWithTimeoutTimesOut(): Unit = {
    val proc = new ProcessBuilder("sleep", "0.005").start()
    assertTrue(!proc.waitFor(1, TimeUnit.MILLISECONDS))
    assertTrue(proc.isAlive)
    proc.waitFor(1, TimeUnit.SECONDS)
  }

  @Test def destroy(): Unit = {
    val proc = new ProcessBuilder("sleep", "0.005").start()
    assertTrue(proc.isAlive)
    proc.destroy()
    assertTrue(proc.waitFor(100, TimeUnit.MILLISECONDS))
    assertTrue(proc.exitValue == 0x80 + 9)
  }

  @Test def destroyForcibly(): Unit = {
    val proc = new ProcessBuilder("sleep", "0.005").start()
    assertTrue(proc.isAlive)
    val p = proc.destroyForcibly()
    assertTrue(p.waitFor(100, TimeUnit.MILLISECONDS))
    assertTrue(p.exitValue == 0x80 + 9)
  }

  @Test def shellFallback(): Unit = {
    val pb = new ProcessBuilder("hello.sh")
    pb.environment.put("PATH", resourceDir)
    val proc = pb.start()

    assertProcessExitOrTimeout(proc)

    val out = readInputStream(proc.getInputStream)
    assertTrue(out == "hello\n")
  }
}
