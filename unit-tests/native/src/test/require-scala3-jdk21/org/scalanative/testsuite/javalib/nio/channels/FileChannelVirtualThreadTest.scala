package org.scalanative.testsuite.javalib.nio.channels

import java.nio.ByteBuffer
import java.nio.channels._
import java.nio.file.Files
import java.nio.file.StandardOpenOption._

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper.assumeSupportsVirtualThreads

// Issue #5260 on virtual threads; native only, as the JDK does not wake them.
class FileChannelVirtualThreadTest {
  import FileChannelInterruptTest._

  private val virtual: Runnable => Thread = Thread.ofVirtual().start(_)

  @Test def interruptFlagSetBeforeCallClosesChannel(): Unit = {
    assumeSupportsVirtualThreads()
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      val ch = FileChannel.open(f, READ, WRITE)
      val out = new Outcome
      val t = virtual { () =>
        Thread.currentThread().interrupt()
        out.capture(ch.read(ByteBuffer.allocate(1)))
      }
      t.join(JoinMillis)
      assertThrew(classOf[ClosedByInterruptException], out)
      assertTrue("interrupt status kept", out.interrupted)
      assertFalse("channel closed", ch.isOpen())
    }
  }

  @Test def closeWakesBlockedRead(): Unit = {
    assumeSupportsVirtualThreads()
    checkCloseWakesRead(virtual)
  }

  @Test def interruptWakesBlockedRead(): Unit = {
    assumeSupportsVirtualThreads()
    checkInterruptWakesRead(virtual)
  }

  @Test def interruptWakesBlockedWrite(): Unit = {
    assumeSupportsVirtualThreads()
    checkInterruptWakesWrite(virtual)
  }
}
