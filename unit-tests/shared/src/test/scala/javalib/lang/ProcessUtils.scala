package javalib.lang

import java.lang._

import java.io.InputStream

import java.util.concurrent.TimeUnit

import scala.io.Source

import org.junit.Assert._

object ProcessUtils {
  def readInputStream(s: InputStream) = Source.fromInputStream(s).mkString

  val resourceDir =
    s"${System.getProperty("user.dir")}/unit-tests/shared/src/test/resources/process"

  val scripts = Set("echo.sh", "err.sh", "ls", "hello.sh")

  def assertProcessExitOrTimeout(process: Process): Unit = {
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
    // Ten seconds is an order of magnitude guess for a "reasonable"
    // completion time.  If a process expected to exit in milliseconds
    // takes that three orders of magnitude longer, it must be reported.

    val tmo = 10
    val tmUnit = TimeUnit.SECONDS

    assertTrue(
      s"Process took more than $tmo ${tmUnit.name} to exit.",
      process.waitFor(tmo, tmUnit)
    )
  }

}
