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

  // @Ignore
  @Test def testJavaString_1(): Unit = {
    /* Avoid doing IO; see if process exits cleanly.
     * 
     * Try to factor out parent/child pipe I/O handling
     * I/O goes to shared stdout, not write end of pipe.
     */

    val dirName = makeRandomDirName()

    // 'mkdir' takes no input and gives no output.

    val cmd = new ju.ArrayList[String](2)
    cmd.add("mkdir")
    cmd.add(s"${dirName}")

    val proc = new jl.ProcessBuilder(cmd).start()

    Thread.sleep(1000 * 10) // seconds, be generous to avoid flakey failures

    if (proc.isAlive()) {
      proc.destroy()
      fail("process should have exited but is alive")
    }
  }
}
