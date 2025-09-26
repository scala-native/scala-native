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

// import java.io.File
import java.{lang => jl}
import java.{io => ji}
import java.{util => ju}

// import scala.sys.process // specify the process class & its methods we want.
import scala.sys.process._
// import scala.sys.process.ProcessLogger._

class DraftJavaProcessTest {

  lazy val rng = new ju.SplittableRandom()

  def makeRandomDirName(): String = {
    // Jitter the name to ease running the Test manually more than once.
    // Hack around lack of usable java.util.UUID on Scala Native,

    val suffix = rng.nextLong(0L, jl.Long.MAX_VALUE).toString

    s"WindowsProcessDebug_SNjavalib_${suffix}"
  }

  object MockScalaBasicIO {
    import scala.annotation.tailrec
    def processFully(processLine: String => Unit): ji.InputStream => Unit =
      in => {
        val reader = new ji.BufferedReader(new ji.InputStreamReader(in))
        try processLinesFully(processLine)(() => reader.readLine())
        finally reader.close()
      }

    def processLinesFully(
        processLine: String => Unit
    )(readLine: () => String): Unit = {
      def working = !Thread.currentThread.isInterrupted
      def halting = { Thread.currentThread.interrupt(); null }

      @tailrec
      def readFully(): Unit =
        if (working) {
          val line =
            try readLine()
            catch {
              case _: InterruptedException       => halting
              case _: ji.IOException if !working => halting
            }
          if (line != null) {
            processLine(line)
            readFully()
          }
        }
      readFully()
    }
  }

  @Ignore // Passes JVM & SN
  @Test def testJavaString_A_1(): Unit = {
    /* Avoid doing IO; see if process exits cleanly.
     * 
     * Try to factor out parent/child pipe I/O handling
     * I/O goes to shared stdout, not write end of pipe.
     */

    val dirName = makeRandomDirName()

    // 'mkdir' takes no input and gives no output.

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("mkdir")
    cmd.add(s"${dirName}")

    val proc = new jl.ProcessBuilder(cmd).start()

    Thread.sleep(1000 * 10) // seconds, be generous to avoid flakey failures

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  @Ignore // Appears to work JVM & SN, Windows included
  @Test def testJavaString_B_1(): Unit = {
    /* ???
     */

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("git")
    cmd.add("init")
    cmd.add("-b")
    cmd.add("main")

    val proc = new jl.ProcessBuilder(cmd).start()

    Thread.sleep(1000 * 10) // seconds, be generous to avoid flakey failures

    val is = proc.getInputStream()
    val isr = new ji.InputStreamReader(is)

    val cbufLen = 256
    val cbuf = new Array[Char](cbufLen)

    val nIsrRead_1 = isr.read(cbuf, 0, cbufLen)

    assertTrue("nIsrRead: ${nIsrRead_1}", nIsrRead_1 > 0)

    val response = String.valueOf(ju.Arrays.copyOfRange(cbuf, 0, nIsrRead_1))

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )

    val nIsrRead_2 = isr.read(cbuf, 0, cbufLen)

    assertEquals("nIsrReadEof:", -1, nIsrRead_2)

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case B_1: Make it evident that Windows process exited")
      }

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  @Ignore // Seems to be working, Windows, etc.
  @Test def testJavaString_B_2(): Unit = {
    /* ???
     */

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("git")
    cmd.add("init")
    cmd.add("-b")
    cmd.add("main")

    val proc = new jl.ProcessBuilder(cmd).start()

    Thread.sleep(1000 * 10) // seconds, be generous to avoid flakey failures

    // ToDo - make sure these eventually get closed,
    //   probably with a Try/finally which closes br
    //   Leave hanging for now to see if underling layers, especially
    //   process pipe InputStream report EOF.
    val is = proc.getInputStream()
    val isr = new ji.InputStreamReader(is)
    val br = new ji.BufferedReader(isr)

    val cbufLen = 256
    val cbuf = new Array[Char](cbufLen)

    val nBrRead_1 = br.read(cbuf, 0, cbufLen)

    assertTrue("nIsrRead: ${nBrRead_1}", nBrRead_1 > 0)

    val response = String.valueOf(ju.Arrays.copyOfRange(cbuf, 0, nBrRead_1))

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )

    val nBrRead_2 = br.read(cbuf, 0, cbufLen)

    assertEquals("nBrReadEof:", -1, nBrRead_2)

    /* Contorted DEBUG logic ahead. Focus attention on Windows SN case.
     * If the process is exiting correctly, it should always get to the
     * fail(), that is overall Success. If process hangs, then a
     * successful Windows CI run is really failure.
     */
    if (!Platform.executingInJVM)
      if (Platform.isWindows) {
        // Sometimes Success is best revealed by Failure.
        fail("Expected case B_2: Make it evident that Windows process exited")
      }

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  @Ignore
  @Test def testJavaString_C_1(): Unit = {
    /* ???
     */

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("git")
    cmd.add("init")
    cmd.add("-b")
    cmd.add("main")

    val proc = new jl.ProcessBuilder(cmd).start()

    Thread.sleep(1000 * 10) // seconds, be generous to avoid flakey failures

    // ToDo - make sure these eventually get closed,
    //   probably with a Try/finally which closes br
    //   Leave hanging for now to see if underling layers, especially
    //   process pipe InputStream report EOF.
    val is = proc.getInputStream()
//    val isr = new ji.InputStreamReader(is)
//    val br = new ji.BufferedReader(isr)

    val bldr = new jl.StringBuilder()
    val processor = MockScalaBasicIO.processFully(s => bldr.append(s))

    processor(is)

    val response = bldr.toString()

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
        fail("Expected case C_1: Make it evident that Windows process exited")
      }

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  @Ignore
  @Test def testJavaString_C_2(): Unit = {
    /* This is testJavaString_C_1 with the Thread.sleep() commented out.
     * Mixup the timing.
     */

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("git")
    cmd.add("init")
    cmd.add("-b")
    cmd.add("main")

    val proc = new jl.ProcessBuilder(cmd).start()

//    Thread.sleep(1000 * 10) // seconds, be generous to avoid flakey failures

    // ToDo - make sure these eventually get closed,
    //   probably with a Try/finally which closes br
    //   Leave hanging for now to see if underling layers, especially
    //   process pipe InputStream report EOF.
    val is = proc.getInputStream()

    val bldr = new jl.StringBuilder()
    val processor = MockScalaBasicIO.processFully(s => bldr.append(s))

    processor(is)

    val response = bldr.toString()

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
        fail("Expected case C_2: Make it evident that Windows process exited")
      }

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  @Ignore // Appears to pass Windows & others, JVM & SN
  @Test def testJavaString_C_3(): Unit = {
    /* This is testJavaString_C_2 with the Thread.sleep() moved
     * after the I/O. Those routines should block until the
     * expected data is ready, read it, pass the assertion, and
     * continue to the sleep(). That is, the test should not
     * block/hang in the I/O.
     *
     * The sleep() gives plenty of time for the child process to
     * exit before being checked for isAlive().
     *
     * That is the theory any way.
     */

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("git")
    cmd.add("init")
    cmd.add("-b")
    cmd.add("main")

    val proc = new jl.ProcessBuilder(cmd).start()

    // ToDo - make sure these eventually get closed,
    //   probably with a Try/finally which closes br
    //   Leave hanging for now to see if underling layers, especially
    //   process pipe InputStream report EOF.
    val is = proc.getInputStream()

    val bldr = new jl.StringBuilder()
    val processor = MockScalaBasicIO.processFully(s => bldr.append(s))

    processor(is)

    val response = bldr.toString()

    assertTrue(
      s"process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )

    // On Windows, the wall clock time between the  when a process
    // closes its write pipe (EOF) and the time it actually exits
    // and proc.isAlive() becomes "false" seems to be longer than one
    // would expect.
    //
    Thread.sleep(1000 * 20) // seconds, be generous to avoid flakey failures

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

  // @Ignore
  @Test def testJavaString_C_4(): Unit = {
    /* This is testJavaString_C_3 using real Scala BasicIO methods directly.
     */

    import scala.sys.process.{BasicIO => ScalaBasicIO}

    // No List.of() in Java 8, so initialize the traditional hard way.
    val cmd = new ju.ArrayList[String]()
    cmd.add("git")
    cmd.add("init")
    cmd.add("-b")
    cmd.add("main")

    val proc = new jl.ProcessBuilder(cmd).start()

    // ToDo - make sure these eventually get closed,
    //   probably with a Try/finally which closes br
    //   Leave hanging for now to see if underling layers, especially
    //   process pipe InputStream report EOF.
    val is = proc.getInputStream()

    val bldr = new jl.StringBuilder()
    val processor = ScalaBasicIO.processFully(s => bldr.append(s))

    processor(is)

    val response = bldr.toString()

    assertTrue(
      s"_C_4 process response: <${response}>",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("Reinitialized existing Git repository in")
    )

    // On Windows, the wall clock time between the  when a process
    // closes its write pipe (EOF) and the time it actually exits
    // and proc.isAlive() becomes "false" seems to be longer than one
    // would expect.
    //
    Thread.sleep(1000 * 20) // seconds, be generous to avoid flakey failures

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }

}
