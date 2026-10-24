package scala.scalanative.runtime

import java.io.{File, PrintStream}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.annotation.nooptimize
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._

class StackOverflowExceptionHandlingTest {
  // Regression for #4190. Run with LTO and custom exception handling
  // (cppOptions = Seq("-fno-cxx-exceptions")) to bypass the C++ fallback.
  @Test def releasesNestedPrintStreamMonitor(): Unit = {
    assumeTrue("requires multithreading", LinktimeInfo.isMultithreadingEnabled)
    val devNull = new PrintStream(
      new File(if (LinktimeInfo.isWindows) "NUL" else "/dev/null")
    )

    @noinline
    @nooptimize
    def stackoverflow(): Int = {
      // Keep the frame small enough to overflow inside println, which acquires
      // the same monitor twice. Prevent tail-call elimination of the recursion.
      val buf = stackalloc[Byte](512)
      devNull.println(".")
      buf.toInt + stackoverflow()
    }

    var caught = false
    try {
      try stackoverflow()
      catch { case _: StackOverflowError => caught = true }
      assertTrue("StackOverflowError was not caught", caught)
      assertFalse(
        "PrintStream monitor was not released",
        Thread.holdsLock(devNull)
      )
    } finally {
      devNull.close()
    }
  }
}
