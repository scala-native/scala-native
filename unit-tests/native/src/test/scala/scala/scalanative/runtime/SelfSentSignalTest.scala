package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.libc.signal._
import scala.scalanative.meta.LinktimeInfo

class SelfSentSignalTest {
  // Regression for #4805: libclang re-raises faults (e.g. GC yieldpoint traps)
  // after restoring our handlers; the self-sent signal must not abort.
  @Test def survivesSelfSentSegv(): Unit = {
    assumeTrue("Linux only", LinktimeInfo.isLinux)
    assertEquals(0, raise(SIGSEGV))
    if (LinktimeInfo.isMultithreadingEnabled) {
      var result = -1
      val thread = new Thread(() => result = raise(SIGSEGV))
      thread.start()
      thread.join()
      assertEquals(0, result)
    }
  }
}
