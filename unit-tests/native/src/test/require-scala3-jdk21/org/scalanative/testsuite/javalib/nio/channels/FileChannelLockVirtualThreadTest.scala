package org.scalanative.testsuite.javalib.nio.channels

import java.nio.channels._

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper.assumeSupportsVirtualThreads

// Issue #5260: lock() on virtual threads; native only, as the JDK blocks there.
class FileChannelLockVirtualThreadTest {
  import FileChannelInterruptTest._
  import FileChannelLockTest._

  private val virtual: Runnable => Thread = Thread.ofVirtual().start(_)

  @Test def interruptWakesLock(): Unit = {
    assumeSupportsVirtualThreads()
    checkInterruptWakesLock(virtual)
  }

  @Test def closeWakesLock(): Unit = {
    assumeSupportsVirtualThreads()
    checkCloseWakesLock(virtual)
  }

  @Test def interruptFlagSetBeforeLock(): Unit = {
    assumeSupportsVirtualThreads()
    withFile { (_, f) =>
      val ch = rw(f)
      try {
        val out = new Outcome
        val t = virtual { () =>
          Thread.currentThread().interrupt()
          out.capture(ch.lock())
        }
        t.join(JoinMillis)
        assertThrew(classOf[FileLockInterruptionException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }
  }
}
