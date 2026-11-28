package org.scalanative.testsuite.javalib.nio.channels

import java.nio.channels._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper._

// Issue #5260: FileChannel file locks.
object FileChannelLockTest {
  import FileChannelInterruptTest._

  final val Max = Long.MaxValue

  def withFile(fn: (Path, Path) => Unit): Unit =
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](100))
      fn(dir, f)
    }

  def rw(f: Path): FileChannel = FileChannel.open(f, READ, WRITE)

  // Runs op on a thread while a child holds [0, 10); the child's release frees it.
  def blockedOnChild(
      f: Path,
      op: => Any,
      action: Thread => Unit,
      start: Runnable => Thread = daemon(_)
  ): Outcome = {
    val child = LockHolder.start(f)
    try {
      child.lock(0, 10)
      blockedUntil(op, action, () => child.close(), start)
    } finally child.close()
  }

  // The JDK on Windows waits in the OS, which interrupt and close() can't end.
  private def assumeLockWaitWakes(): Unit =
    assumeFalse(
      "JDK on Windows: lock() waits in the OS",
      Platform.executingInJVM && Platform.isWindows
    )

  /* lock(), retried while a probe of awaitPendingRequest briefly holds an
   * overlapping request of its own.
   */
  def lockPastProbe(ch: FileChannel, pos: Long, size: Long): FileLock = {
    val deadline = System.nanoTime() + JoinMillis * 1000000L
    var lock: FileLock = null
    while (lock == null)
      try lock = ch.lock(pos, size, false)
      catch {
        case e: OverlappingFileLockException =>
          if (System.nanoTime() >= deadline) throw e
          Thread.sleep(1)
      }
    lock
  }

  // Waits until a request for [pos, pos + size) is registered by another channel.
  def awaitPendingRequest(ch: FileChannel, pos: Long, size: Long): Unit = {
    val deadline = System.nanoTime() + JoinMillis * 1000000L
    var pending = false
    while (!pending) {
      try assertNull("range is held", ch.tryLock(pos, size, false))
      catch { case _: OverlappingFileLockException => pending = true }
      if (!pending) {
        assertTrue("request registered", System.nanoTime() < deadline)
        Thread.sleep(5)
      }
    }
  }

  // Also run on virtual threads by FileChannelLockVirtualThreadTest.
  def checkInterruptWakesLock(start: Runnable => Thread): Unit =
    withFile { (_, f) =>
      assumeLockWaitWakes()
      val ch = rw(f)
      try {
        val out =
          blockedOnChild(f, ch.lock(0, 10, false), _.interrupt(), start)
        assertWoken(out)
        assertThrew(classOf[FileLockInterruptionException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }

  def checkCloseWakesLock(start: Runnable => Thread): Unit =
    withFile { (_, f) =>
      assumeLockWaitWakes()
      val ch = rw(f)
      try {
        val out =
          blockedOnChild(f, ch.lock(0, 10, false), _ => ch.close(), start)
        assertWoken(out)
        assertThrew(classOf[AsynchronousCloseException], out)
        assertFalse("interrupted", out.interrupted)
      } finally ch.close()
    }
}

class FileChannelLockTest {
  import FileChannelInterruptTest._
  import FileChannelLockTest._

  // D1: modes and arguments

  @Test def sharedLockOnReadOnlyChannel(): Unit = withFile { (_, f) =>
    val ch = FileChannel.open(f, READ)
    try {
      val l = ch.lock(0, 10, true)
      assertTrue("shared", l.isShared())
      assertTrue("valid", l.isValid())
      l.release()
      val t = ch.tryLock(0, 10, true)
      assertNotNull("tryLock", t)
      assertTrue("tryLock shared", t.isShared())
      t.release()
    } finally ch.close()
  }

  @Test def exclusiveLockNeedsWritableChannel(): Unit = withFile { (_, f) =>
    val ch = FileChannel.open(f, READ)
    try {
      assertThrows(classOf[NonWritableChannelException], ch.lock(0, 10, false))
      assertThrows(
        classOf[NonWritableChannelException],
        ch.tryLock(0, 10, false)
      )
      assertThrows(classOf[NonWritableChannelException], ch.lock())
      // Mode is checked before the arguments.
      assertThrows(classOf[NonWritableChannelException], ch.lock(-1, 10, false))
    } finally ch.close()
  }

  @Test def sharedLockNeedsReadableChannel(): Unit = withFile { (_, f) =>
    val ch = FileChannel.open(f, WRITE)
    try {
      assertThrows(classOf[NonReadableChannelException], ch.lock(0, 10, true))
      assertThrows(
        classOf[NonReadableChannelException],
        ch.tryLock(0, 10, true)
      )
      val l = ch.lock(0, 10, false)
      assertFalse("exclusive", l.isShared())
      l.release()
    } finally ch.close()
  }

  @Test def closedChannelIsCheckedFirst(): Unit = withFile { (_, f) =>
    val ch = FileChannel.open(f, READ)
    ch.close()
    assertThrows(classOf[ClosedChannelException], ch.lock(-1, 10, false))
    assertThrows(classOf[ClosedChannelException], ch.tryLock(0, 10, true))
  }

  @Test def invalidArguments(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      def iae(op: => Any, msg: String): Unit = {
        val e = assertThrows(classOf[IllegalArgumentException], op)
        assertEquals(msg, e.getMessage())
      }
      iae(ch.lock(-1, 10, false), "Negative position")
      iae(ch.lock(0, -1, false), "Negative size")
      iae(ch.lock(Max - 1, 10, false), "Negative position + size")
      iae(ch.tryLock(Max - 1, 10, false), "Negative position + size")
      iae(ch.lock(5, Max, false), "Negative position + size")
      iae(ch.tryLock(-1, 10, true), "Negative position")
    } finally ch.close()
  }

  @Test def largeRanges(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val all = ch.lock(0, Max, false)
      assertEquals(0L, all.position())
      assertEquals(Max, all.size())
      all.release()
      val end = ch.lock(Max - 10, 10, false)
      assertEquals(Max - 10, end.position())
      assertEquals(10L, end.size())
      end.release()
      val high = ch.tryLock(1L << 40, 10, false)
      assertNotNull(high)
      high.release()
    } finally ch.close()
  }

  @Test def lockPastEndOfFileKeepsSize(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val l = ch.lock(1000, 10, false)
      assertEquals(100L, ch.size())
      l.release()
    } finally ch.close()
  }

  // D3: overlap within the process

  @Test def overlapOnSameChannel(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val l = ch.lock(0, 10, false)
      assertThrows(classOf[OverlappingFileLockException], ch.lock(5, 10, false))
      assertThrows(
        classOf[OverlappingFileLockException],
        ch.tryLock(5, 10, false)
      )
      assertThrows(classOf[OverlappingFileLockException], ch.lock(0, 10, false))
      val adjacent = ch.lock(10, 10, false)
      assertTrue(adjacent.isValid())
      assertTrue("first lock still valid", l.isValid())
      adjacent.release()
      l.release()
    } finally ch.close()
  }

  @Test def overlapAcrossChannels(): Unit = withFile { (_, f) =>
    val c1 = rw(f)
    val c2 = rw(f)
    try {
      val l = c1.lock(0, 10, false)
      assertThrows(classOf[OverlappingFileLockException], c2.lock(5, 10, false))
      assertThrows(
        classOf[OverlappingFileLockException],
        c2.tryLock(5, 10, false)
      )
      val other = c2.tryLock(20, 10, false)
      assertNotNull(other)
      other.release()
      l.release()
      val after = c2.lock(5, 10, false)
      assertTrue(after.isValid())
      after.release()
    } finally {
      c1.close()
      c2.close()
    }
  }

  @Test def sharedLocksOverlapToo(): Unit = withFile { (_, f) =>
    val c1 = rw(f)
    val c2 = rw(f)
    try {
      val l = c1.lock(0, 10, true)
      assertThrows(classOf[OverlappingFileLockException], c1.lock(5, 10, true))
      assertThrows(classOf[OverlappingFileLockException], c2.lock(5, 10, true))
      l.release()
    } finally {
      c1.close()
      c2.close()
    }
  }

  @Test def overlapThroughLinks(): Unit = withFile { (dir, f) =>
    val hard = dir.resolve("hard")
    Files.createLink(hard, f)
    val c1 = rw(f)
    val ch = rw(hard)
    try {
      val l = c1.lock(0, 10, false)
      assertThrows(
        classOf[OverlappingFileLockException],
        ch.tryLock(5, 10, false)
      )
      if (Platform.canCreateSymbolicLinks) {
        val sym = dir.resolve("sym")
        Files.createSymbolicLink(sym, f)
        val cs = rw(sym)
        try
          assertThrows(
            classOf[OverlappingFileLockException],
            cs.tryLock(5, 10, false)
          )
        finally cs.close()
      }
      l.release()
    } finally {
      c1.close()
      ch.close()
    }
  }

  @Test def concurrentOverlappingLocks(): Unit = {
    assumeMultithreadingIsEnabled()
    withFile { (_, f) =>
      val channels = Array.fill(8)(rw(f))
      try {
        for (_ <- 0 until 50) {
          val go = new CountDownLatch(1)
          val results = channels.map(_ => new AtomicReference[Any]("running"))
          val threads = channels.indices.map { i =>
            daemon { () =>
              go.await()
              results(i).set(
                try channels(i).tryLock(0, 10, false)
                catch { case e: Throwable => e }
              )
            }
          }
          go.countDown()
          threads.foreach(_.join(JoinMillis))
          val outcomes = results.map(_.get())
          val granted = outcomes.collect { case l: FileLock => l }
          granted.foreach(_.release())
          assertEquals("exactly one lock granted", 1, granted.length)
          outcomes.foreach { o =>
            assertTrue(
              s"lock or overlap: $o",
              o.isInstanceOf[FileLock] ||
                o.isInstanceOf[OverlappingFileLockException]
            )
          }
        }
      } finally channels.foreach(_.close())
    }
  }

  // D4: release and close

  @Test def releaseIsIdempotent(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val l = ch.lock(0, 1, false)
      l.release()
      l.release()
      assertFalse(l.isValid())
      val again = ch.lock(0, 1, false)
      again.close()
      assertFalse("close() releases", again.isValid())
      ch.lock(0, 1, false).release()
    } finally ch.close()
  }

  @Test def lockReportsItsChannel(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val l = ch.lock(0, 1, false)
      assertSame(ch, l.channel())
      assertSame(ch, l.acquiredBy())
      l.release()
    } finally ch.close()
  }

  @Test def closingChannelInvalidatesItsLocks(): Unit = withFile { (_, f) =>
    val c1 = rw(f)
    val l = c1.lock(0, 10, false)
    val s = c1.lock(20, 10, false)
    c1.close()
    assertFalse(l.isValid())
    assertFalse(s.isValid())
    assertThrows(classOf[ClosedChannelException], l.release())
    assertThrows(classOf[ClosedChannelException], l.close())
    val c2 = rw(f)
    try {
      val again = c2.lock(0, 30, false)
      assertTrue(again.isValid())
      again.release()
    } finally c2.close()
  }

  @Test def closingStreamReleasesChannelLocks(): Unit = withFile { (_, f) =>
    val raf = new java.io.RandomAccessFile(f.toFile(), "rw")
    val l = raf.getChannel().lock(0, 10, false)
    raf.close()
    assertFalse(l.isValid())
    val ch = rw(f)
    try {
      val again = ch.lock(0, 10, false)
      assertTrue(again.isValid())
      again.release()
    } finally ch.close()
  }

  // D2: another process

  @Test def tryLockReturnsNullWhenAnotherProcessHoldsIt(): Unit =
    withFile { (_, f) =>
      val child = LockHolder.start(f)
      val ch = rw(f)
      try {
        child.lock(0, 10)
        assertNull("exclusive", ch.tryLock(5, 10, false))
        assertNull("shared", ch.tryLock(5, 10, true))
        val adjacent = ch.tryLock(10, 10, false)
        assertNotNull("adjacent", adjacent)
        adjacent.release()
        child.release()
        val l = ch.tryLock(5, 10, false)
        assertNotNull("after release", l)
        l.release()
      } finally {
        ch.close()
        child.close()
      }
    }

  @Test def sharedLocksAcrossProcesses(): Unit = withFile { (_, f) =>
    val child = LockHolder.start(f)
    val ch = rw(f)
    try {
      child.lock(0, 10, shared = true)
      val s = ch.tryLock(5, 10, true)
      assertNotNull("shared with shared", s)
      assertTrue(s.isShared())
      s.release()
      assertNull("exclusive with shared", ch.tryLock(5, 10, false))
      child.release()
      // Our shared lock must let the child take a shared lock, not an exclusive one.
      val mine = ch.lock(0, 10, true)
      assertEquals("locked", child.tryLock(0, 10, shared = true))
      assertEquals("null", child.tryLock(0, 10, shared = false))
      mine.release()
    } finally {
      ch.close()
      child.close()
    }
  }

  @Test def lockWaitsForAnotherProcess(): Unit = withFile { (_, f) =>
    assumeMultithreadingIsEnabled()
    val child = LockHolder.start(f)
    val ch = rw(f)
    try {
      child.lock(0, 10)
      val out = blockedUntil(
        ch.lock(5, 10, false),
        _ => child.release(),
        () => child.close()
      )
      assertWoken(out)
      assertTrue(s"lock acquired: $out", out.error == null)
      out.result.asInstanceOf[FileLock].release()
    } finally {
      ch.close()
      child.close()
    }
  }

  @Test def waitingLockCountsAsOverlap(): Unit = withFile { (_, f) =>
    assumeMultithreadingIsEnabled()
    val child = LockHolder.start(f)
    val c3 = rw(f)
    val c4 = rw(f)
    try {
      child.lock(0, 10)
      val out = blockedUntil(
        lockPastProbe(c3, 0, 10),
        _ => {
          awaitPendingRequest(c4, 5, 10)
          assertThrows(
            classOf[OverlappingFileLockException],
            c3.tryLock(5, 10, false)
          )
          val other = c4.tryLock(50, 10, false)
          assertNotNull(other)
          other.release()
          child.release()
        },
        () => child.close()
      )
      assertWoken(out)
      assertTrue(s"waiter acquired: $out", out.error == null)
      assertTrue(out.result.asInstanceOf[FileLock].isValid())
    } finally {
      c3.close()
      c4.close()
      child.close()
    }
  }

  // D7: interrupt and close

  @Test def interruptWakesLock(): Unit =
    checkInterruptWakesLock(daemon(_))

  @Test def closeWakesLock(): Unit =
    checkCloseWakesLock(daemon(_))

  @Test def failedWaitForgetsItsRange(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val out = blockedOnChild(f, ch.lock(0, 10, false), _ => ch.close())
      assertThrew(classOf[AsynchronousCloseException], out)
    } finally ch.close()
    val c2 = rw(f)
    try {
      val l = c2.lock(0, 10, false)
      assertTrue(l.isValid())
      l.release()
    } finally c2.close()
  }

  @Test def interruptFlagSetBeforeLock(): Unit = inThread {
    withFile { (_, f) =>
      val ch = rw(f)
      try {
        Thread.currentThread().interrupt()
        assertThrows(classOf[FileLockInterruptionException], ch.lock())
        assertTrue("interrupt status kept", Thread.interrupted())
        assertFalse("channel closed", ch.isOpen())
        Thread.currentThread().interrupt()
        assertThrows(classOf[ClosedChannelException], ch.lock())
        Thread.interrupted()
      } finally ch.close()
    }
  }

  @Test def tryLockIgnoresInterruptFlag(): Unit = inThread {
    withFile { (_, f) =>
      val ch = rw(f)
      try {
        Thread.currentThread().interrupt()
        val l = ch.tryLock()
        assertTrue("interrupt status kept", Thread.interrupted())
        assertNotNull(l)
        assertTrue("channel open", ch.isOpen())
        assertTrue(l.isValid())
        l.release()
      } finally ch.close()
    }
  }
}
