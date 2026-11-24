package org.scalanative.testsuite.javalib.nio.channels

import java.nio.ByteBuffer
import java.nio.channels._
import java.nio.file.Files
import java.nio.file.StandardOpenOption._

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper.assumeSupportsVirtualThreads

/* Issue #5260 on virtual threads. The JDK does not wake a virtual thread
 * blocked in FileChannel I/O, so these run on Scala Native only.
 */
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
        try out.result = ch.read(ByteBuffer.allocate(1))
        catch { case e: Throwable => out.error = e }
        out.interrupted = Thread.currentThread().isInterrupted()
        out.finished = true
      }
      t.join(JoinMillis)
      assertThrew(classOf[ClosedByInterruptException], out)
      assertTrue("interrupt status kept", out.interrupted)
      assertFalse("channel closed", ch.isOpen())
    }
  }

  @Test def closeWakesBlockedRead(): Unit = {
    assumeSupportsVirtualThreads()
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      val out = blocked(
        ch.read(ByteBuffer.allocate(1)),
        _ => ch.close(),
        () => feed(fifo),
        virtual
      )
      assertPrompt(out)
      assertThrew(classOf[AsynchronousCloseException], out)
    }
  }

  @Test def interruptWakesBlockedRead(): Unit = {
    assumeSupportsVirtualThreads()
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.read(ByteBuffer.allocate(1)),
          _.interrupt(),
          () => feed(fifo),
          virtual
        )
        assertPrompt(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }
  }

  @Test def interruptWakesBlockedWrite(): Unit = {
    assumeSupportsVirtualThreads()
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.write(ByteBuffer.allocate(1 << 20)),
          _.interrupt(),
          () => drain(fifo),
          virtual
        )
        assertPrompt(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
      } finally ch.close()
    }
  }
}
