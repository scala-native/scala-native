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

  // @Ignore
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

}
