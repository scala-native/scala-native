package org.scalanative.testsuite.javalib.lang

import java.io._
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source
import scala.sys.{process => sp}
import scala.util.{Failure, Success, Try}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper._

import Platform._

class ProcessTest {
  import ProcessUtils._

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

  // Exercise the fork() path in UnixProcessGen2
  @Test def dirOverride(): Unit = {
    assumeNotJVMCompliant()
    assumeFalse("Not tested in Windows", isWindows)

    val pb = new ProcessBuilder("./ls")
    pb.directory(new File(resourceDir))
    checkPathOverride(pb) // off-label use of checkPathOverride() here.
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
      // both streams are empty; stdout to file, and stderr is empty
      assertEquals(-1, proc.getInputStream.read()) // null input
      assertEquals(-1, proc.getErrorStream.read()) // pipe input
      val out = Source.fromFile(file.toString).getLines().mkString

      assertEquals("inputStreamWritesToFile()", "hello", out)
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
      assertEquals(
        "outputStreamReadsFromFile()",
        "hello",
        readInputStream(proc.getInputStream).trim
      )
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

  // PR #3950, aka Issue 3944, part 1
  @Ignore
  @Test def isAliveIsReliable(): Unit = {
    /* Exercise both expected Boolean conditions.
     *
     * This is an advanced test for javalib developers.
     * It is @Ignore'd in normal CI because it introduces a perceptible
     * pause. This is needed to ensure that the process is still alive
     * when the first isAlive() is executed.
     *
     * Cutting this pause down to milliseconds risks false failures on
     * heavily loaded systems. Pick your druthers, Life is short!
     */

    val proc = processSleep(5.0).start()

    assertTrue("process should be alive", proc.isAlive)

    try {
      val timeout = 30
      assertTrue(
        s"process should have exited but timed out (limit: ${timeout} seconds)",
        proc.waitFor(timeout, TimeUnit.SECONDS)
      )

      assertFalse("process should have exited", proc.isAlive)

      assertEquals(0, proc.exitValue)

      assertFalse(
        "isAlive should be robust to second call after process exit",
        proc.isAlive
      )

    } finally {
      proc.destroyForcibly() // Let OS eliminate any child process still alive.
    }
  }

  // Issue 3452
  @Test def waitForReturnsExitCode(): Unit = {
    /* This test is neither robust nor CI friendly.
     * A buggy implementation of waitFor(pid) and/or of processSleep()
     * could cause it to hang forever.
     *
     * waitfor(pid, timeout) can not be used here because it returns a Boolean,
     * not the exit code of the child.
     *
     * Scala Native does not implement junit "@Test(timeout)". That was
     * designed for situations just like this.
     *
     * Let's see how this fares in CI. Does it hang intermittently?
     * Should it be a manual development & maintains only test, ignored
     * in CI?
     */

    val proc = processSleep(0.1).start()

    val expected = 0 // Successful completion

    assertEquals(
      s"waitFor return value",
      expected,
      proc.waitFor()
    )
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

  // Defensive code for Issue 3944
  @Test def waitForWithTimeoutIsRobustAfterProcessExit(): Unit = {
    /* Check that waitFor(timeout) can safely be called more than once after a
     * process exits. Both waitFor(timout) calls will probably return
     * immediatly.
     *
     * The idea is to exercise the internals of the second call. How does it
     * respond to a now invalid process id.
     */

    val proc = processSleep(0.1).start()

    try {
      val timeout = 30
      assertTrue(
        s"process should have exited but timed out (limit: ${timeout} seconds)",
        proc.waitFor(timeout, TimeUnit.SECONDS)
      )

      /* DO NOT do the usual & expected 'assertEquals(0, proc.exitValue)' here.
       * That would set the internal cached exitValue. Not doing that
       * call here allows checking if waitFor() is caching that value
       * to protect itself against a second call.
       */

      assertTrue(
        s"Second waitFor after process exit should be robust.)",
        proc.waitFor(timeout, TimeUnit.SECONDS)
      )

    } finally {
      proc.destroyForcibly() // Let OS eliminate any child process still alive.
    }
  }

  // Issue 3944, part 2
  @Test def waitForWithTimeoutAcceptsLargeMilliseconds(): Unit = {
    val proc = processSleep(2.0).start()

    try {
      val timeout = 10 * 1000 // Value from Issue 3944

      /*  Exception before fix, where nnnn is a pid number:
       *
       *  Linux & non-BSD kin:
       *   java.io.IOException: wait pid=nnnn, ppoll failed: 22
       *
       *  macOS, FreeBSD:
       *   java.io.IOException: wait pid=nnnn, kevent failed: Invalid argument
       *
       *  After the corresponding PR, the messages should never be seen.
       *  If one is, the Linux case should look like the macOS case.
       *
       *  A TimeUnit larger than MILLISECONDS will not trigger the I3944
       *  defect.
       *
       *  The number of that unit needs to be more than those in a second.
       *  say 1001 for MILLISECONDS. That gives a large number of
       *  nanoseconds after full seconds (in nanos) have been subtracted off.
       *  Be generous here, use the value from the Issue,  and not try to
       *  find too fine an edge of failure.
       */

      assertTrue(
        s"process should have exited but timed out (limit: ${timeout} millis)",
        proc.waitFor(timeout, TimeUnit.MILLISECONDS)
      )

      assertFalse("process should have exited", proc.isAlive)
      assertEquals(0, proc.exitValue)
    } finally {
      proc.destroyForcibly() // Let OS eliminate any child process still alive.
    }
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
    assumeNotCrossCompiling()
    val proc = processForScript(Scripts.hello).start()

    assertProcessExitOrTimeout(proc)
    assertEquals(0, proc.exitValue())
    assertEquals("", readInputStream(proc.getErrorStream()))
    assertEquals(
      "shellFallback()",
      s"hello$EOL",
      readInputStream(proc.getInputStream())
    )
  }

  /* concurrentPipe() Design Note: Issue 4164
   *
   *   This evolution of concurrentPipe() is motivated by both experience
   *   and analysis.
   *
   *   The presenting, experiential concern is that the this Test fails
   *   intermittently when run by Continuous Integration on Windows.
   *
   *   Analysis:
   *     In a concurrent world, there is no guarantee that the read Thread
   *   will execute after the child process has written all its bytes and
   *   those bytes are ready at the parent end of the pipe.
   *
   *     Many Tests in this file establish an order by waiting for the child
   *   to exit before attempting to read. That is not realistic for
   *   The Real World.
   *
   *     Here, the method "readNBytes()" is used to read its end of the pipe
   *   in a more representative manner. If the child executes before the Thread,
   *   the first read should succeed.
   *
   *     If the Thread reads before the child has written, End-of-File (EOF)will
   *   be immediately returned.    We know, but the InputStream class does not,
   *   that the underlying I/O stream is a pipe. The usual implementation
   *   of pipe reads is that they do not block when the pipe is empty.
   *   InputStream uses EOF/-1 to indicate both true EOF and zero byte
   *   reads because of an empty pipe.
   *
   *     There is also no guarantee that a single write of N bytes by the child
   *   will traverse the pipe and become an N byte read at the parent end of
   *   the pipe. Here a count of the expected bytes is used to both consolidate
   *   fragmented reads and avoid determining EOF.
   */

  @Test def concurrentPipe(): Unit = {
    /* Ensure that reading from process stdout does not lead to exceptions
     * when thread terminates (was failing with Bad file descriptor in
     * FileChannel.read)
     */
    assumeNotCrossCompiling()
    assumeMultithreadingIsEnabled()
    assumeNot32Bit() // Flaky on x86

    val iterations = 16

    // See Design Note just before this Test.
    val perIterationTimeout = 20 // seconds

    /* Give a smidge more time to the worst case where each iteration succeeds
     * just before timing out. Test might be executing on slow uniprocessor or
     * low core multiprocessor. Be generous to avoid intermittent
     * false failures. The full timeout should be infrequent and Heisenbugs
     * are costly.
     */
    val totalTimeout = (iterations + 1) + perIterationTimeout // seconds

    val tasks = for (n <- 0 until iterations) yield Future {
      val proc = processForScript(Scripts.hello).start()
      val expectedResponse = "hello"

      var done = false

      def readNBytes(
          src: InputStream,
          nToRead: Int,
          timeoutSeconds: Int
      ): Array[Byte] = {
        val buffer = new Array[Byte](nToRead)

        var totalRead = 0
        val maxSleepCount = timeoutSeconds
        var sleepCount = 0

        while ((totalRead < nToRead) && (sleepCount < maxSleepCount)) {
          val nRead = src.read(buffer, totalRead, nToRead - totalRead)

          if (nRead > 0) {
            totalRead += nRead
          } else {
            sleepCount += 1
            Thread.`yield`() // in case sleep() itself does not yield.
            Thread.sleep(1 * 1000)
          }
        }

        buffer
      }

      val t = new Thread(() => {
        val src = proc.getInputStream()

        // All bytes in expectedResponse bytes are 1 byte in UTF-8.
        val bytes =
          readNBytes(src, expectedResponse.length(), perIterationTimeout)
        assertEquals(
          "concurrentPipe() thread failed",
          expectedResponse,
          new String(bytes, StandardCharsets.UTF_8)
        )
        done = true
      })

      /* Increase the chance of detecting concurrency issues.
       * Start the thread _before_ waiting for process exit to increase the
       * chance of reading from pipe while child is still active. The
       * pipe code paths before and after child exit differ.
       */
      t.start()

      val pwfTimeout = perIterationTimeout
      val procWaitForStatus = proc.waitFor(pwfTimeout, TimeUnit.SECONDS)

      /* ??? If these assertions fail, will the message ever make it all the
       * way back to the Test runner and be reported? Perhaps.
       */

      assertTrue(
        s"child.waitFor exceeded timeout seconds: ${pwfTimeout}",
        procWaitForStatus
      )

      assertEquals("child exit value", 0, proc.exitValue())

      t.join()
      done
    }

    var threadFailed = false

    val awaitSuccess = Await
      .result(Future.sequence(tasks), totalTimeout.seconds)
      .forall((r: Boolean) => {
        if (r) true else { threadFailed = true; false }
      })

    if (!awaitSuccess) { // Disambiguate the error cases
      assertFalse("executed thread failed", threadFailed)
      fail(s"Await failed with totalTimeout seconds: ${totalTimeout}")
    }
  }

  @Test def redirectOutputAccess() = {
    // Regression test for com-lihaoyi/os-lib
    val dir = Files.createTempDirectory("test-")
    val out = dir.resolve("all.txt")
    Files.write(dir.resolve("empty.txt"), "".getBytes())
    Files.write(dir.resolve("foo.txt"), "foo".getBytes())
    val files = Files
      .list(dir)
      .filter(_.getFileName().toString().endsWith(".txt"))
      .toArray()
      .map(_.toString())
    val cmd = if (scala.util.Properties.isWin) "type" else "cat"
    val proc = processForCommand((cmd +: files): _*)
      .redirectOutput(out.toFile())
      .start()

    assertEquals(
      s"stderr='${readInputStream(proc.getErrorStream())}'",
      0,
      proc.waitFor()
    )
    assertEquals("foo", Files.readAllLines(out).toArray().mkString)
  }

  private val gitTestIterations = 1
  private val githubWorkspace =
    Paths.get(sys.env.get("GITHUB_WORKSPACE").getOrElse("."))

  @Test def testGitLsFilesUsingJavaProcessStart(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix =
      s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] `git ls-files`"

    // run git ls-files --full-names on source
    val proc = processForCommand("git", "ls-files", "--full-name")
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)
      .directory(githubWorkspace.toFile)
      .start()

    val stdout = Source.fromInputStream(proc.getInputStream).mkString
    val stderr = Source.fromInputStream(proc.getErrorStream).mkString

    assertTrue(
      s"$prefix should exit quickly",
      proc.waitFor(10, TimeUnit.SECONDS)
    )

    assertTrue(s"$prefix stdout: <${trunc(stdout)}>", stdout.length > 100)
    assertEquals(s"$prefix stderr: <${trunc(stderr)}>", "", stderr)

    assertTrue(s"$prefix exited", !proc.isAlive)
    assertEquals(
      s"$prefix exit code; stderr: <${trunc(stderr)}",
      0,
      proc.exitValue()
    )
  }

  @Test def testGitInitUsingJavaProcessStart(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix = s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] "
    val dir = Files.createTempDirectory("test-")

    val pbInit = processForCommand("git", "init", "-b", "main", dir.toString)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)

    // run init the first time
    locally {
      val proc = pbInit.start()
      val stdout = Source.fromInputStream(proc.getInputStream).mkString
      val stderr = Source.fromInputStream(proc.getErrorStream).mkString

      assertTrue(
        "`git init` should exit quickly",
        proc.waitFor(10, TimeUnit.SECONDS)
      )

      assertTrue(
        s"`git init` stdout: <$stdout>",
        stdout.startsWith("Initialized empty Git repository in ")
      )

      assertEquals(s"$prefix`git init` stderr: <$stderr>", "", stderr)

      assertTrue(prefix + "`git init` exited", !proc.isAlive)
      assertEquals(prefix + "`git init` exit code", 0, proc.exitValue())
    }

    // run init the second time
    locally {
      val proc = pbInit.start()
      val stdout = Source.fromInputStream(proc.getInputStream).mkString
      val stderr = Source.fromInputStream(proc.getErrorStream).mkString

      assertTrue(
        "`git init` should exit quickly",
        proc.waitFor(10, TimeUnit.SECONDS)
      )

      assertTrue(
        s"`git init` stdout: <$stdout>",
        stdout.startsWith("Reinitialized existing Git repository in ")
      )

      assertEquals(
        s"`git init` stderr: <$stderr>",
        "warning: re-init: ignored --initial-branch=main\n",
        stderr
      )

      assertTrue(prefix + "`git init` exited", !proc.isAlive)
      assertEquals(prefix + "`git init` exit code", 0, proc.exitValue())
    }

    // run log, hoping it fails
    locally {
      val pbLog = processForCommand("git", "-C", dir.toString, "log")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)

      val proc = pbLog.start()
      val stdout = Source.fromInputStream(proc.getInputStream).mkString
      val stderr = Source.fromInputStream(proc.getErrorStream).mkString

      assertTrue(
        "`git log` should exit quickly",
        proc.waitFor(10, TimeUnit.SECONDS)
      )

      assertEquals(s"$prefix`git log` stdout: <$stdout>", "", stdout)

      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )

      assertTrue(prefix + "`git log` exited", !proc.isAlive)
      assertEquals(prefix + "`git log` exit code", 128, proc.exitValue())
    }

    // run log, hoping it fails, this time specifying directory in ProcessBuilder
    locally {
      val pbLog = processForCommand("git", "log")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .directory(dir.toFile)

      val proc = pbLog.start()
      val stdout = Source.fromInputStream(proc.getInputStream).mkString
      val stderr = Source.fromInputStream(proc.getErrorStream).mkString

      assertTrue(
        "`git log` should exit quickly",
        proc.waitFor(10, TimeUnit.SECONDS)
      )

      assertEquals(s"$prefix`git log` stdout: <$stdout>", "", stdout)

      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )

      assertTrue(prefix + "`git log` exited", !proc.isAlive)
      assertEquals(prefix + "`git log` exit code", 128, proc.exitValue())
    }

  }

  @Test def testGitLsFilesUsingScalaProcessRun(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix =
      s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] `git ls-files`"

    // run git ls-files --full-names on source
    val argv = Seq("git", "ls-files", "--full-name")
    val (res, stdout, stderr) =
      ProcessTest.runArgv(10.seconds, githubWorkspace)(argv: _*)
    res match {
      case Success(x) =>
        assertEquals(s"$prefix exit; stderr: ${trunc(stderr)}", 0, x)
        assertTrue(s"$prefix stdout: <${trunc(stdout)}>", stdout.length > 100)
        assertEquals(s"$prefix stderr: <${trunc(stderr)}>", "", stderr)
      case Failure(x) => fail(s"$prefix failed: $x")
    }
  }

  @Test def testGitInitUsingScalaProcessRun(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix = s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] "
    val dir = Files.createTempDirectory("test-")

    val argvInit = Seq("git", "init", "-b", "main", dir.toString)

    // run init the first time
    locally {
      val (res, stdout, stderr) = ProcessTest.runArgv()(argvInit: _*)
      res match {
        case Success(x) => assertEquals(prefix + "`git init` exit code", 0, x)
        case Failure(x) => fail(prefix + "`git init` failed: " + x)
      }

      assertTrue(
        s"$prefix`git init` stdout: <$stdout>",
        stdout.startsWith("Initialized empty Git repository in ")
      )

      assertEquals(s"$prefix`git init` stderr: <$stderr>", "", stderr)
    }

    // run init the second time
    locally {
      val (res, stdout, stderr) = ProcessTest.runArgv()(argvInit: _*)
      res match {
        case Success(x) => assertEquals(prefix + "`git init` exit code", 0, x)
        case Failure(x) => fail(prefix + "`git init` failed: " + x)
      }

      assertTrue(
        s"`git init` stdout: <$stdout>",
        stdout.startsWith("Reinitialized existing Git repository in ")
      )

      assertEquals(
        s"`git init` stderr: <$stderr>",
        "warning: re-init: ignored --initial-branch=main\n",
        stderr
      )
    }

    // run log, hoping it fails
    locally {
      val (res, stdout, stderr) =
        ProcessTest.runArgv()("git", "-C", dir.toString, "log")
      res match {
        case Success(x) => assertEquals(prefix + "`git log` exit code", 128, x)
        case Failure(x) => fail(prefix + "`git log` failed: " + x)
      }

      assertEquals(s"$prefix`git log` stdout: <$stdout>", "", stdout)
      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )
    }

    // run log, hoping it fails, this time specifying directory in ProcessBuilder
    locally {
      val (res, stdout, stderr) = ProcessTest.runArgv(cwd = dir)("git", "log")
      res match {
        case Success(x) => assertEquals(prefix + "`git log` exit code", 128, x)
        case Failure(x) => fail(prefix + "`git log` failed: " + x)
      }

      assertEquals(s"$prefix`git log` stdout: <$stdout>", "", stdout)
      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )
    }

  }

  @Test def testGitLsFilesUsingScalaProcessBang(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix =
      s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] `git ls-files`"

    // run git ls-files --full-names on source
    val argv = Seq("git", "ls-files", "--full-name")
    val (res, stdout, stderr) =
      ProcessTest.runArgvWithBang(60.seconds, githubWorkspace)(argv: _*)
    res match {
      case Success(x) =>
        assertEquals(s"$prefix exit; stderr: ${trunc(stderr)}", 0, x)
        assertTrue(s"$prefix stdout: <${trunc(stdout)}>", stdout.length > 100)
        assertEquals(s"$prefix stderr: <${trunc(stderr)}>", "", stderr)
      case Failure(x) => fail(s"$prefix failed: $x")
    }
  }

  @Test def testGitInitUsingScalaProcessBang(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix = s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] "
    val dir = Files.createTempDirectory("test-")

    val argvInit = Seq("git", "init", "-b", "main", dir.toString)

    // run init the first time
    locally {
      val (res, stdout, stderr) =
        ProcessTest.runArgvWithBang()(argvInit: _*)
      res match {
        case Success(x) => assertEquals(prefix + "`git init` exit code", 0, x)
        case Failure(x) => fail(prefix + "`git init` failed: " + x)
      }

      assertTrue(
        s"$prefix`git init` stdout: <$stdout>",
        stdout.startsWith("Initialized empty Git repository in ")
      )

      assertEquals(s"$prefix`git init` stderr: <$stderr>", "", stderr)
    }

    // run init the second time
    locally {
      val (res, stdout, stderr) =
        ProcessTest.runArgvWithBang()(argvInit: _*)
      res match {
        case Success(x) => assertEquals(prefix + "`git init` exit code", 0, x)
        case Failure(x) => fail(prefix + "`git init` failed: " + x)
      }

      assertTrue(
        s"`git init` stdout: <$stdout>",
        stdout.startsWith("Reinitialized existing Git repository in ")
      )

      assertEquals(
        s"`git init` stderr: <$stderr>",
        "warning: re-init: ignored --initial-branch=main\n",
        stderr
      )
    }

    // run log, hoping it fails
    locally {
      val (res, stdout, stderr) =
        ProcessTest.runArgvWithBang()("git", "-C", dir.toString, "log")
      res match {
        case Success(x) => assertEquals(prefix + "`git log` exit code", 128, x)
        case Failure(x) => fail(prefix + "`git log` failed: " + x)
      }

      assertEquals(s"$prefix`git log` stdout: <$stdout>", "", stdout)
      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )
    }

    // run log, hoping it fails, this time specifying directory in ProcessBuilder
    locally {
      val (res, stdout, stderr) =
        ProcessTest.runArgvWithBang(cwd = dir)("git", "log")
      res match {
        case Success(x) => assertEquals(prefix + "`git log` exit code", 128, x)
        case Failure(x) => fail(prefix + "`git log` failed: " + x)
      }

      assertEquals(s"$prefix`git log` stdout: <$stdout>", "", stdout)
      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )
    }

  }

  @Test def testGitLsFilesUsingScalaProcessBangBang(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix =
      s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] `git ls-files`"

    // run git ls-files --full-names on source
    val argv = Seq("git", "ls-files", "--full-name")
    val (res, stderr) =
      ProcessTest.runArgvWithBangBang(60.seconds, githubWorkspace)(argv: _*)
    res match {
      case Success(x) =>
        assertTrue(s"$prefix stdout: <${trunc(x)}>", x.length > 100)
        assertEquals(s"$prefix stderr: <${trunc(stderr)}>", "", stderr)
      case Failure(x) => fail(s"$prefix failed: $x")
    }
  }

  @Test def testGitInitUsingScalaProcessBangBang(
  ): Unit = (0 until gitTestIterations).foreach { iter =>

    val prefix = s"[iter=$iter ${new java.io.File(".").getAbsolutePath}] "
    val dir = Files.createTempDirectory("test-")

    val argvInit = Seq("git", "init", "-b", "main", dir.toString)

    // run init the first time
    locally {
      val (res, stderr) = ProcessTest.runArgvWithBangBang()(argvInit: _*)
      res match {
        case Success(x) =>
          assertTrue(
            s"`git init` stdout: <$x>",
            x.startsWith("Initialized empty Git repository in ")
          )
        case Failure(x) => fail(prefix + "`git init` failed: " + x)
      }

      assertEquals(s"$prefix`git init` stderr: <$stderr>", "", stderr)
    }

    // run init the second time
    locally {
      val (res, stderr) = ProcessTest.runArgvWithBangBang()(argvInit: _*)
      res match {
        case Success(x) =>
          assertTrue(
            s"`git init` stdout: <$x>",
            x.startsWith("Reinitialized existing Git repository in ")
          )
        case Failure(x) => fail(prefix + "`git init` failed: " + x)
      }

      assertEquals(
        s"`git init` stderr: <$stderr>",
        "warning: re-init: ignored --initial-branch=main\n",
        stderr
      )
    }

    // run log, hoping it fails
    locally {
      val (res, stderr) =
        ProcessTest.runArgvWithBangBang()("git", "-C", dir.toString, "log")
      res match {
        case Success(x) => fail(prefix + "`git log` succeeded: " + x)
        case Failure(x) =>
          assertEquals(
            s"`git log` failed: <$x>",
            "Nonzero exit value: 128",
            x.getMessage()
          )
      }

      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )
    }

    // run log, hoping it fails, this time specifying directory in ProcessBuilder
    locally {
      val (res, stderr) =
        ProcessTest.runArgvWithBangBang(cwd = dir)("git", "log")
      res match {
        case Success(x) => fail(prefix + "`git log` succeeded: " + x)
        case Failure(x) =>
          assertEquals(
            s"`git log` failed: <$x>",
            "Nonzero exit value: 128",
            x.getMessage()
          )
      }

      assertEquals(
        s"`git log` stderr: <$stderr>",
        "fatal: your current branch 'main' does not have any commits yet\n",
        stderr
      )
    }

  }

}

object ProcessTest {

  def runArgv(to: Duration = 10.seconds, cwd: Path = null)(
      cmd: String*
  ): (Try[Int], String, String) = {
    val out = new StringBuilder()
    val err = new StringBuilder()
    val logger = sp.ProcessLogger(
      x => out.append(x).append('\n'),
      x => err.append(x).append('\n')
    )

    val proc = sp
      .Process(cmd, Option(cwd).map(_.toFile))
      .run(logger, connectInput = false)
    val res = Try { Await.result(Future(proc.exitValue()), to) }
    res.failed.foreach { _ => proc.destroy() }

    (res, out.result(), err.toString())
  }

  def runArgvWithBang(to: Duration = 10.seconds, cwd: Path = null)(
      cmd: String*
  ): (Try[Int], String, String) = {
    val out = new StringBuilder()
    val err = new StringBuilder()
    val logger = sp.ProcessLogger(
      x => out.append(x).append('\n'),
      x => err.append(x).append('\n')
    )

    val pb = sp.Process(cmd, Option(cwd).map(_.toFile))
    val res = Try { Await.result(Future(pb.!(logger)), to) }

    (res, out.result(), err.toString())
  }

  def runArgvWithBangBang(to: Duration = 10.seconds, cwd: Path = null)(
      cmd: String*
  ): (Try[String], String) = {
    val err = new StringBuilder()
    val logger = sp.ProcessLogger(
      _ => {},
      x => err.append(x).append('\n')
    )

    val pb = sp.Process(cmd, Option(cwd).map(_.toFile))
    val res = Try { Await.result(Future(pb.!!(logger)), to) }

    (res, err.toString())
  }

}
