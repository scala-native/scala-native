package org.scalanative.testsuite.javalib.nio.channels

import java.io.IOException
import java.nio.channels._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
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

  /* Like blocked(), but unblock is any action: runs op on a thread, lets it
   * block, then runs action; unblock frees op if it is still blocked.
   */
  def blockedUntil(
      op: => Any,
      action: Thread => Unit,
      unblock: () => Unit,
      start: Runnable => Thread = daemon(_)
  ): Outcome = {
    val out = new Outcome
    val started = new CountDownLatch(1)
    val t = start { () =>
      started.countDown()
      out.capture(op)
    }
    assertTrue("thread started", started.await(JoinMillis, MILLISECONDS))
    Thread.sleep(BlockMillis)
    assertFalse(s"operation did not block: $out", out.finished)
    val begin = System.nanoTime()
    val actor = daemon(() => action(t))
    actor.join(JoinMillis)
    if (!actor.isAlive())
      out.actionMillis = (System.nanoTime() - begin) / 1000000L
    t.join(JoinMillis)
    out.woken = out.finished
    if (t.isAlive() || actor.isAlive()) {
      unblock()
      t.join(JoinMillis)
      actor.join(JoinMillis)
    }
    out
  }

  // Runs op on a thread while a child holds [0, 10); the child's release frees it.
  def blockedOnChild(
      dir: Path,
      f: Path,
      op: => Any,
      action: Thread => Unit,
      start: Runnable => Thread = daemon(_)
  ): Outcome = {
    val child = LockHolder.start(dir, f)
    try {
      child.lock(0, 10)
      blockedUntil(op, action, () => child.close(), start)
    } finally child.close()
  }

  // Also run on virtual threads by FileChannelLockVirtualThreadTest.
  def checkInterruptWakesLock(start: Runnable => Thread): Unit =
    withFile { (dir, f) =>
      val ch = rw(f)
      try {
        val out =
          blockedOnChild(dir, f, ch.lock(0, 10, false), _.interrupt(), start)
        assertWoken(out)
        assertThrew(classOf[FileLockInterruptionException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }

  def checkCloseWakesLock(start: Runnable => Thread): Unit =
    withFile { (dir, f) =>
      val ch = rw(f)
      val out =
        blockedOnChild(dir, f, ch.lock(0, 10, false), _ => ch.close(), start)
      assertWoken(out)
      assertThrew(classOf[AsynchronousCloseException], out)
      assertFalse("interrupted", out.interrupted)
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
      if (!Platform.isWindows) {
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
          val locks = channels.map(_ => new AtomicReference[FileLock]())
          val threads = channels.indices.map { i =>
            daemon { () =>
              go.await()
              try locks(i).set(channels(i).tryLock(0, 10, false))
              catch { case _: OverlappingFileLockException => () }
            }
          }
          go.countDown()
          threads.foreach(_.join(JoinMillis))
          val granted = locks.map(_.get()).filter(_ != null)
          assertEquals("exactly one lock granted", 1, granted.length)
          granted.foreach(_.release())
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

  // D2: another process

  @Test def tryLockReturnsNullWhenAnotherProcessHoldsIt(): Unit =
    withFile { (dir, f) =>
      val child = LockHolder.start(dir, f)
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

  @Test def sharedLocksAcrossProcesses(): Unit = withFile { (dir, f) =>
    val child = LockHolder.start(dir, f)
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

  @Test def lockWaitsForAnotherProcess(): Unit = withFile { (dir, f) =>
    assumeMultithreadingIsEnabled()
    val child = LockHolder.start(dir, f)
    val ch = rw(f)
    try {
      child.lock(0, 10)
      val out = blockedUntil(
        ch.lock(5, 10, false),
        _ => child.release(),
        () => ch.close()
      )
      assertTrue(s"lock acquired: $out", out.finished && out.error == null)
      out.result.asInstanceOf[FileLock].release()
    } finally {
      ch.close()
      child.close()
    }
  }

  @Test def waitingLockCountsAsOverlap(): Unit = withFile { (dir, f) =>
    assumeMultithreadingIsEnabled()
    val child = LockHolder.start(dir, f)
    val c3 = rw(f)
    val c4 = rw(f)
    try {
      child.lock(0, 10)
      val out = blockedUntil(
        c3.lock(0, 10, false),
        _ => {
          assertThrows(
            classOf[OverlappingFileLockException],
            c4.tryLock(5, 10, false)
          )
          assertThrows(
            classOf[OverlappingFileLockException],
            c3.tryLock(5, 10, false)
          )
          val other = c4.tryLock(50, 10, false)
          assertNotNull(other)
          other.release()
          child.release()
        },
        () => c3.close()
      )
      assertTrue(s"waiter acquired: $out", out.finished && out.error == null)
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

  @Test def failedWaitForgetsItsRange(): Unit = withFile { (dir, f) =>
    val ch = rw(f)
    val out = blockedOnChild(dir, f, ch.lock(0, 10, false), _ => ch.close())
    assertThrew(classOf[AsynchronousCloseException], out)
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
        val e = assertThrows(classOf[IOException], ch.lock())
        assertTrue(
          s"expected FileLockInterruptionException: $e",
          e.isInstanceOf[FileLockInterruptionException]
        )
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
