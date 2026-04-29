/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.ArrayList
import java.util.concurrent.TimeUnit.{DAYS, MILLISECONDS}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.{Lock, StampedLock}
import java.util.concurrent.{
  CompletableFuture, CountDownLatch, Future, ThreadLocalRandom, TimeUnit
}

import org.junit.Assert._
import org.junit.Assume.assumeFalse
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class StampedLockTest extends JSR166Test {
  import JSR166Test._

  private type Locker = StampedLock => Long
  private type Unlocker = (StampedLock, Long) => Unit

  private def assumeNotJDK8StampedLock(): Unit =
    assumeFalse(
      "JDK8 StampedLock behavior differs from current JSR166 TCK",
      Platform.executingInJVMOnJDK8OrLower
    )

  private def releaseWriteLock(lock: StampedLock, stamp: Long): Unit = {
    assertTrue(lock.isWriteLocked())
    assertValid(lock, stamp)
    lock.unlockWrite(stamp)
    assertFalse(lock.isWriteLocked())
    assertFalse(lock.validate(stamp))
  }

  private def releaseReadLock(lock: StampedLock, stamp: Long): Unit = {
    assertTrue(lock.isReadLocked())
    assertValid(lock, stamp)
    lock.unlockRead(stamp)
    assertFalse(lock.isReadLocked())
    assertTrue(lock.validate(stamp))
  }

  private def assertNonZero(v: Long): Long = {
    assertTrue(v != 0L)
    v
  }

  private def assertValid(lock: StampedLock, stamp: Long): Long = {
    assertTrue(stamp != 0L)
    assertTrue(lock.validate(stamp))
    stamp
  }

  private def assertUnlocked(lock: StampedLock): Unit = {
    assertFalse(lock.isReadLocked())
    assertFalse(lock.isWriteLocked())
    assertEquals(0, lock.getReadLockCount())
    assertValid(lock, lock.tryOptimisticRead())
  }

  private def action(body: => Unit): Action = new Action {
    override def run(): Unit = body
  }

  private def assertThrowsAll[T <: Throwable](
      expected: Class[T],
      actions: Action*
  ): Unit =
    actions.foreach(a => assertThrows(expected, a.run()))

  private def lockLockers(lock: Lock): Array[Action] =
    Array[Action](
      action(lock.lock()),
      action(lock.lockInterruptibly()),
      action(lock.tryLock()),
      action(lock.tryLock(Long.MinValue, DAYS)),
      action(lock.tryLock(0L, DAYS)),
      action(lock.tryLock(Long.MaxValue, DAYS))
    )

  private def readLockers(): Array[Locker] =
    Array[Locker](
      _.readLock(),
      _.tryReadLock(),
      readLockInterruptiblyUninterrupted,
      tryReadLockUninterrupted(_, Long.MinValue, DAYS),
      tryReadLockUninterrupted(_, 0L, DAYS),
      sl => sl.tryConvertToReadLock(sl.tryOptimisticRead())
    )

  private def readUnlockers(): Array[Unlocker] =
    Array[Unlocker](
      _.unlockRead(_),
      (sl, _) => assertTrue(sl.tryUnlockRead()),
      (sl, _) => sl.asReadLock().unlock(),
      _.unlock(_),
      (sl, stamp) => assertValid(sl, sl.tryConvertToOptimisticRead(stamp))
    )

  private def writeLockers(): Array[Locker] =
    Array[Locker](
      _.writeLock(),
      _.tryWriteLock(),
      writeLockInterruptiblyUninterrupted,
      tryWriteLockUninterrupted(_, Long.MinValue, DAYS),
      tryWriteLockUninterrupted(_, 0L, DAYS),
      sl => sl.tryConvertToWriteLock(sl.tryOptimisticRead())
    )

  private def writeUnlockers(): Array[Unlocker] =
    Array[Unlocker](
      _.unlockWrite(_),
      (sl, _) => assertTrue(sl.tryUnlockWrite()),
      (sl, _) => sl.asWriteLock().unlock(),
      _.unlock(_),
      (sl, stamp) => assertValid(sl, sl.tryConvertToOptimisticRead(stamp))
    )

  @Test def testConstructor(): Unit = {
    assertUnlocked(new StampedLock())
  }

  @Test def testWriteLock_lockUnlock(): Unit = {
    val lock = new StampedLock()
    for (writeLocker <- writeLockers(); writeUnlocker <- writeUnlockers()) {
      assertFalse(lock.isWriteLocked())
      assertFalse(lock.isReadLocked())
      assertEquals(0, lock.getReadLockCount())

      val s = writeLocker(lock)
      assertValid(lock, s)
      assertTrue(lock.isWriteLocked())
      assertFalse(lock.isReadLocked())
      assertEquals(0, lock.getReadLockCount())
      writeUnlocker(lock, s)
      assertUnlocked(lock)
    }
  }

  @Test def testReadLock_lockUnlock(): Unit = {
    assumeNotJDK8StampedLock()

    val lock = new StampedLock()
    for (readLocker <- readLockers(); readUnlocker <- readUnlockers()) {
      var s = 42L
      for (i <- 0 until 2) {
        s = assertValid(lock, readLocker(lock))
        assertFalse(lock.isWriteLocked())
        assertTrue(lock.isReadLocked())
        assertEquals(i + 1, lock.getReadLockCount())
      }
      for (i <- 0 until 2) {
        assertFalse(lock.isWriteLocked())
        assertTrue(lock.isReadLocked())
        assertEquals(2 - i, lock.getReadLockCount())
        readUnlocker(lock, s)
      }
      assertUnlocked(lock)
    }
  }

  @Test def testTryUnlockWrite_failure(): Unit = {
    val lock = new StampedLock()
    assertFalse(lock.tryUnlockWrite())

    for (readLocker <- readLockers(); readUnlocker <- readUnlockers()) {
      val s = assertValid(lock, readLocker(lock))
      assertFalse(lock.tryUnlockWrite())
      assertTrue(lock.isReadLocked())
      readUnlocker(lock, s)
      assertUnlocked(lock)
    }
  }

  @Test def testTryUnlockRead_failure(): Unit = {
    val lock = new StampedLock()
    assertFalse(lock.tryUnlockRead())

    for (writeLocker <- writeLockers(); writeUnlocker <- writeUnlockers()) {
      val s = writeLocker(lock)
      assertFalse(lock.tryUnlockRead())
      assertTrue(lock.isWriteLocked())
      writeUnlocker(lock, s)
      assertUnlocked(lock)
    }
  }

  @Test def testValidate0(): Unit = {
    val lock = new StampedLock()
    assertFalse(lock.validate(0L))
  }

  @Test def testValidate(): Unit = {
    val lock = new StampedLock()

    for (readLocker <- readLockers(); readUnlocker <- readUnlockers()) {
      val s = assertNonZero(readLocker(lock))
      assertTrue(lock.validate(s))
      readUnlocker(lock, s)
    }

    for (writeLocker <- writeLockers(); writeUnlocker <- writeUnlockers()) {
      val s = assertNonZero(writeLocker(lock))
      assertTrue(lock.validate(s))
      writeUnlocker(lock, s)
    }
  }

  @Test def testValidate2(): Unit = {
    val lock = new StampedLock()
    val s = assertNonZero(lock.writeLock())
    assertTrue(lock.validate(s))
    assertFalse(lock.validate(lock.tryWriteLock()))
    assertFalse(
      lock.validate(lock.tryWriteLock(randomExpiredTimeout(), randomTimeUnit()))
    )
    assertFalse(lock.validate(lock.tryReadLock()))
    assertFalse(
      lock.validate(lock.tryWriteLock(randomExpiredTimeout(), randomTimeUnit()))
    )
    assertFalse(lock.validate(lock.tryOptimisticRead()))
    lock.unlockWrite(s)
  }

  private def assertThrowInterruptedExceptionWhenPreInterrupted(
      actions: Array[Action]
  ): Unit =
    for (action <- actions) {
      Thread.currentThread().interrupt()
      try {
        action.run()
        shouldThrow()
      } catch {
        case _: InterruptedException => ()
        case fail: Throwable         => threadUnexpectedException(fail)
      }
      assertFalse(Thread.interrupted())
    }

  @Test def testInterruptibleOperationsThrowInterruptedExceptionWhenPreInterrupted()
      : Unit = {
    val lock = new StampedLock()
    val interruptibleLockActions = Array[Action](
      action(lock.writeLockInterruptibly()),
      action(lock.tryWriteLock(Long.MinValue, DAYS)),
      action(lock.tryWriteLock(Long.MaxValue, DAYS)),
      action(lock.readLockInterruptibly()),
      action(lock.tryReadLock(Long.MinValue, DAYS)),
      action(lock.tryReadLock(Long.MaxValue, DAYS)),
      action(lock.asWriteLock().lockInterruptibly()),
      action(lock.asWriteLock().tryLock(0L, DAYS)),
      action(lock.asWriteLock().tryLock(Long.MaxValue, DAYS)),
      action(lock.asReadLock().lockInterruptibly()),
      action(lock.asReadLock().tryLock(0L, DAYS)),
      action(lock.asReadLock().tryLock(Long.MaxValue, DAYS))
    )
    shuffle(interruptibleLockActions)

    assertThrowInterruptedExceptionWhenPreInterrupted(interruptibleLockActions)

    val writeStamp = lock.writeLock()
    assertThrowInterruptedExceptionWhenPreInterrupted(interruptibleLockActions)
    lock.unlockWrite(writeStamp)

    val readStamp = lock.readLock()
    assertThrowInterruptedExceptionWhenPreInterrupted(interruptibleLockActions)
    lock.unlockRead(readStamp)
  }

  private def assertThrowInterruptedExceptionWhenInterrupted(
      actions: Array[Action]
  ): Unit = {
    val n = actions.length
    val futures = new Array[Future[_]](n)
    val threadsStarted = new CountDownLatch(n)
    val done = new CountDownLatch(n)

    for (i <- 0 until n) {
      val runnableAction = actions(i)
      futures(i) = cachedThreadPool.submit(new CheckedRunnable() {
        override def realRun(): Unit = {
          threadsStarted.countDown()
          try {
            runnableAction.run()
            shouldThrow()
          } catch {
            case _: InterruptedException => ()
            case fail: Throwable         => threadUnexpectedException(fail)
          }
          assertFalse(Thread.interrupted())
          done.countDown()
        }
      })
    }

    await(threadsStarted)
    assertEquals(n.toLong, done.getCount())
    futures.foreach(_.cancel(true))
    await(done)
  }

  @Test def testInterruptibleOperationsThrowInterruptedExceptionWriteLockedInterrupted()
      : Unit = {
    val lock = new StampedLock()
    val stamp = lock.writeLock()
    val interruptibleLockBlockingActions = Array[Action](
      action(lock.writeLockInterruptibly()),
      action(lock.tryWriteLock(Long.MaxValue, DAYS)),
      action(lock.readLockInterruptibly()),
      action(lock.tryReadLock(Long.MaxValue, DAYS)),
      action(lock.asWriteLock().lockInterruptibly()),
      action(lock.asWriteLock().tryLock(Long.MaxValue, DAYS)),
      action(lock.asReadLock().lockInterruptibly()),
      action(lock.asReadLock().tryLock(Long.MaxValue, DAYS))
    )
    shuffle(interruptibleLockBlockingActions)

    assertThrowInterruptedExceptionWhenInterrupted(
      interruptibleLockBlockingActions
    )
    releaseWriteLock(lock, stamp)
  }

  @Test def testInterruptibleOperationsThrowInterruptedExceptionReadLockedInterrupted()
      : Unit = {
    val lock = new StampedLock()
    val stamp = lock.readLock()
    val interruptibleLockBlockingActions = Array[Action](
      action(lock.writeLockInterruptibly()),
      action(lock.tryWriteLock(Long.MaxValue, DAYS)),
      action(lock.asWriteLock().lockInterruptibly()),
      action(lock.asWriteLock().tryLock(Long.MaxValue, DAYS))
    )
    shuffle(interruptibleLockBlockingActions)

    assertThrowInterruptedExceptionWhenInterrupted(
      interruptibleLockBlockingActions
    )
    releaseReadLock(lock, stamp)
  }

  @Test def testNonInterruptibleOperationsIgnoreInterrupts(): Unit = {
    val lock = new StampedLock()
    Thread.currentThread().interrupt()

    for (readUnlocker <- readUnlockers()) {
      var s = assertValid(lock, lock.readLock())
      readUnlocker(lock, s)
      s = assertValid(lock, lock.tryReadLock())
      readUnlocker(lock, s)
    }

    lock.asReadLock().lock()
    lock.asReadLock().unlock()

    for (writeUnlocker <- writeUnlockers()) {
      var s = assertValid(lock, lock.writeLock())
      writeUnlocker(lock, s)
      s = assertValid(lock, lock.tryWriteLock())
      writeUnlocker(lock, s)
    }

    lock.asWriteLock().lock()
    lock.asWriteLock().unlock()

    assertTrue(Thread.interrupted())
  }

  @Test def testTryWriteLock(): Unit = {
    val lock = new StampedLock()
    val s = lock.tryWriteLock()
    assertTrue(s != 0L)
    assertTrue(lock.isWriteLocked())
    assertEquals(0L, lock.tryWriteLock())
    releaseWriteLock(lock, s)
  }

  @Test def testTryWriteLockWhenLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.writeLock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = assertEquals(0L, lock.tryWriteLock())
    })

    assertEquals(0L, lock.tryWriteLock())
    awaitTermination(t)
    releaseWriteLock(lock, s)
  }

  @Test def testTryReadLockWhenLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.writeLock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = assertEquals(0L, lock.tryReadLock())
    })

    assertEquals(0L, lock.tryReadLock())
    awaitTermination(t)
    releaseWriteLock(lock, s)
  }

  @Test def testMultipleReadLocks(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        val s2 = lock.tryReadLock()
        assertValid(lock, s2)
        lock.unlockRead(s2)
        val s3 = lock.tryReadLock(LONG_DELAY_MS, MILLISECONDS)
        assertValid(lock, s3)
        lock.unlockRead(s3)
        val s4 = lock.readLock()
        assertValid(lock, s4)
        lock.unlockRead(s4)
        lock.asReadLock().lock()
        lock.asReadLock().unlock()
        lock.asReadLock().lockInterruptibly()
        lock.asReadLock().unlock()
        lock.asReadLock().tryLock(Long.MinValue, DAYS)
        lock.asReadLock().unlock()
      }
    })

    awaitTermination(t)
    lock.unlockRead(s)
  }

  @Test def testWriteAfterReadLock(): Unit = {
    val aboutToLock = new CountDownLatch(1)
    val lock = new StampedLock()
    val rs = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        aboutToLock.countDown()
        val s = lock.writeLock()
        assertTrue(lock.isWriteLocked())
        assertFalse(lock.isReadLocked())
        lock.unlockWrite(s)
      }
    })

    await(aboutToLock)
    assertThreadBlocks(t, Thread.State.WAITING)
    assertFalse(lock.isWriteLocked())
    assertTrue(lock.isReadLocked())
    lock.unlockRead(rs)
    awaitTermination(t)
    assertUnlocked(lock)
  }

  @Test def testWriteAfterMultipleReadLocks(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        val rs = lock.readLock()
        lock.unlockRead(rs)
      }
    })

    awaitTermination(t1)
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        val ws = lock.writeLock()
        lock.unlockWrite(ws)
      }
    })

    assertTrue(lock.isReadLocked())
    assertFalse(lock.isWriteLocked())
    lock.unlockRead(s)
    awaitTermination(t2)
    assertUnlocked(lock)
  }

  @Test def testReadAfterWriteLock(): Unit = {
    val lock = new StampedLock()
    val threadsStarted = new CountDownLatch(2)
    val s = lock.writeLock()
    val acquireReleaseReadLock = new CheckedRunnable() {
      override def realRun(): Unit = {
        threadsStarted.countDown()
        val rs = lock.readLock()
        assertTrue(lock.isReadLocked())
        assertFalse(lock.isWriteLocked())
        lock.unlockRead(rs)
      }
    }
    val t1 = newStartedThread(acquireReleaseReadLock)
    val t2 = newStartedThread(acquireReleaseReadLock)

    await(threadsStarted)
    assertThreadBlocks(t1, Thread.State.WAITING)
    assertThreadBlocks(t2, Thread.State.WAITING)
    assertTrue(lock.isWriteLocked())
    assertFalse(lock.isReadLocked())
    releaseWriteLock(lock, s)
    awaitTermination(t1)
    awaitTermination(t2)
    assertUnlocked(lock)
  }

  @Test def testTryLockWhenReadLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        val rs = lock.tryReadLock()
        assertValid(lock, rs)
        lock.unlockRead(rs)
      }
    })

    awaitTermination(t)
    lock.unlockRead(s)
  }

  @Test def testTryWriteLockWhenReadLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = assertEquals(0L, lock.tryWriteLock())
    })

    awaitTermination(t)
    lock.unlockRead(s)
  }

  @Test def testTimedLock_Timeout(): Unit = {
    val futures = new ArrayList[Future[_]]()

    val lock = new StampedLock()
    val stamp = lock.writeLock()
    assertEquals(0L, lock.tryReadLock(0L, DAYS))
    assertEquals(0L, lock.tryReadLock(Long.MinValue, DAYS))
    assertFalse(lock.asReadLock().tryLock(0L, DAYS))
    assertFalse(lock.asReadLock().tryLock(Long.MinValue, DAYS))
    assertEquals(0L, lock.tryWriteLock(0L, DAYS))
    assertEquals(0L, lock.tryWriteLock(Long.MinValue, DAYS))
    assertFalse(lock.asWriteLock().tryLock(0L, DAYS))
    assertFalse(lock.asWriteLock().tryLock(Long.MinValue, DAYS))

    futures.add(cachedThreadPool.submit(new CheckedRunnable() {
      override def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertEquals(0L, lock.tryWriteLock(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    }))

    futures.add(cachedThreadPool.submit(new CheckedRunnable() {
      override def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertEquals(0L, lock.tryReadLock(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    }))

    val lock2 = new StampedLock()
    val stamp2 = lock2.readLock()
    assertEquals(0L, lock2.tryWriteLock(0L, DAYS))
    assertEquals(0L, lock2.tryWriteLock(Long.MinValue, DAYS))
    assertFalse(lock2.asWriteLock().tryLock(0L, DAYS))
    assertFalse(lock2.asWriteLock().tryLock(Long.MinValue, DAYS))

    futures.add(cachedThreadPool.submit(new CheckedRunnable() {
      override def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertEquals(0L, lock2.tryWriteLock(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    }))

    val it = futures.iterator()
    while (it.hasNext()) assertNull(it.next().get())

    releaseWriteLock(lock, stamp)
    releaseReadLock(lock2, stamp2)
  }

  @Test def testWriteLockInterruptibly(): Unit = {
    val lock = new StampedLock()
    val s = lock.writeLockInterruptibly()
    assertTrue(lock.isWriteLocked())
    releaseWriteLock(lock, s)
  }

  @Test def testReadLockInterruptibly(): Unit = {
    val lock = new StampedLock()

    val s = assertValid(lock, lock.readLockInterruptibly())
    assertTrue(lock.isReadLocked())
    lock.unlockRead(s)

    lock.asReadLock().lockInterruptibly()
    assertTrue(lock.isReadLocked())
    lock.asReadLock().unlock()
  }

  @Ignore("scala-native#4852: ObjectInputStream is unsupported")
  @Test def testSerialization(): Unit = {
    ()
  }

  @Test def testToString(): Unit = {
    val lock = new StampedLock()
    assertTrue(lock.toString().contains("Unlocked"))
    var s = lock.writeLock()
    assertTrue(lock.toString().contains("Write-locked"))
    lock.unlockWrite(s)
    s = lock.readLock()
    assertTrue(lock.toString().contains("Read-locks"))
    releaseReadLock(lock, s)
  }

  @Test def testValidateOptimistic(): Unit = {
    val lock = new StampedLock()

    assertValid(lock, lock.tryOptimisticRead())

    for (writeLocker <- writeLockers()) {
      val s = assertValid(lock, writeLocker(lock))
      assertEquals(0L, lock.tryOptimisticRead())
      releaseWriteLock(lock, s)
    }

    for (readLocker <- readLockers()) {
      val s = assertValid(lock, readLocker(lock))
      val p = assertValid(lock, lock.tryOptimisticRead())
      releaseReadLock(lock, s)
      assertTrue(lock.validate(p))
    }

    assertValid(lock, lock.tryOptimisticRead())
  }

  @Test def testValidateOptimisticWriteLocked(): Unit = {
    val lock = new StampedLock()
    val p = assertValid(lock, lock.tryOptimisticRead())
    val s = assertValid(lock, lock.writeLock())
    assertFalse(lock.validate(p))
    assertEquals(0L, lock.tryOptimisticRead())
    assertTrue(lock.validate(s))
    lock.unlockWrite(s)
  }

  @Test def testValidateOptimisticWriteLocked2(): Unit = {
    val locked = new CountDownLatch(1)
    val lock = new StampedLock()
    val p = assertValid(lock, lock.tryOptimisticRead())

    val t = newStartedThread(new CheckedInterruptedRunnable() {
      override def realRun(): Unit = {
        lock.writeLockInterruptibly()
        locked.countDown()
        lock.writeLockInterruptibly()
        ()
      }
    })

    await(locked)
    assertFalse(lock.validate(p))
    assertEquals(0L, lock.tryOptimisticRead())
    assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    assertTrue(lock.isWriteLocked())
  }

  @Test def testTryConvertToOptimisticRead(): Unit = {
    assumeNotJDK8StampedLock()

    val lock = new StampedLock()
    assertEquals(0L, lock.tryConvertToOptimisticRead(0L))

    var s = assertValid(lock, lock.tryOptimisticRead())
    assertEquals(s, lock.tryConvertToOptimisticRead(s))
    assertTrue(lock.validate(s))

    for (writeLocker <- writeLockers()) {
      s = assertValid(lock, writeLocker(lock))
      val p = assertValid(lock, lock.tryConvertToOptimisticRead(s))
      assertFalse(lock.validate(s))
      assertTrue(lock.validate(p))
      assertUnlocked(lock)
    }

    for (readLocker <- readLockers()) {
      s = assertValid(lock, readLocker(lock))
      val q = assertValid(lock, lock.tryOptimisticRead())
      assertEquals(q, lock.tryConvertToOptimisticRead(q))
      assertTrue(lock.validate(q))
      assertTrue(lock.isReadLocked())
      val p = assertValid(lock, lock.tryConvertToOptimisticRead(s))
      assertTrue(lock.validate(p))
      assertTrue(lock.validate(s))
      assertUnlocked(lock)
      assertEquals(q, lock.tryConvertToOptimisticRead(q))
      assertTrue(lock.validate(q))
    }
  }

  @Test def testTryConvertToReadLock(): Unit = {
    assumeNotJDK8StampedLock()

    val lock = new StampedLock()
    assertEquals(0L, lock.tryConvertToReadLock(0L))

    var s = assertValid(lock, lock.tryOptimisticRead())
    var p = assertValid(lock, lock.tryConvertToReadLock(s))
    assertTrue(lock.isReadLocked())
    assertEquals(1, lock.getReadLockCount())
    assertTrue(lock.validate(s))
    lock.unlockRead(p)

    s = assertValid(lock, lock.tryOptimisticRead())
    lock.readLock()
    p = assertValid(lock, lock.tryConvertToReadLock(s))
    assertTrue(lock.isReadLocked())
    assertEquals(2, lock.getReadLockCount())
    lock.unlockRead(p)
    lock.unlockRead(p)
    assertUnlocked(lock)

    for (readUnlocker <- readUnlockers()) {
      for (writeLocker <- writeLockers()) {
        s = assertValid(lock, writeLocker(lock))
        p = assertValid(lock, lock.tryConvertToReadLock(s))
        assertFalse(lock.validate(s))
        assertTrue(lock.isReadLocked())
        assertEquals(1, lock.getReadLockCount())
        readUnlocker(lock, p)
      }

      for (readLocker <- readLockers()) {
        s = assertValid(lock, readLocker(lock))
        assertEquals(s, lock.tryConvertToReadLock(s))
        assertTrue(lock.validate(s))
        assertTrue(lock.isReadLocked())
        assertEquals(1, lock.getReadLockCount())
        readUnlocker(lock, s)
      }
    }
  }

  @Test def testTryConvertToWriteLock(): Unit = {
    assumeNotJDK8StampedLock()

    val lock = new StampedLock()
    assertEquals(0L, lock.tryConvertToWriteLock(0L))

    var s = lock.tryOptimisticRead()
    assertTrue(s != 0L)
    var p = lock.tryConvertToWriteLock(s)
    assertTrue(p != 0L)
    assertTrue(lock.isWriteLocked())
    lock.unlockWrite(p)

    for (writeUnlocker <- writeUnlockers()) {
      for (writeLocker <- writeLockers()) {
        s = assertValid(lock, writeLocker(lock))
        assertEquals(s, lock.tryConvertToWriteLock(s))
        assertTrue(lock.validate(s))
        assertTrue(lock.isWriteLocked())
        writeUnlocker(lock, s)
      }

      for (readLocker <- readLockers()) {
        s = assertValid(lock, readLocker(lock))
        p = assertValid(lock, lock.tryConvertToWriteLock(s))
        assertFalse(lock.validate(s))
        assertTrue(lock.validate(p))
        assertTrue(lock.isWriteLocked())
        writeUnlocker(lock, p)
      }
    }

    for (readLocker <- readLockers()) {
      s = assertValid(lock, readLocker(lock))
      p = assertValid(lock, readLocker(lock))
      assertEquals(0L, lock.tryConvertToWriteLock(s))
      assertTrue(lock.validate(s))
      assertTrue(lock.validate(p))
      assertEquals(2, lock.getReadLockCount())
      lock.unlock(p)
      lock.unlock(s)
      assertUnlocked(lock)
    }
  }

  @Test def testAsWriteLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asWriteLock()
    for (locker <- lockLockers(lock)) {
      locker.run()
      assertTrue(sl.isWriteLocked())
      assertFalse(sl.isReadLocked())
      assertFalse(lock.tryLock())
      lock.unlock()
      assertUnlocked(sl)
    }
  }

  @Test def testAsReadLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asReadLock()
    for (locker <- lockLockers(lock)) {
      locker.run()
      assertTrue(sl.isReadLocked())
      assertFalse(sl.isWriteLocked())
      assertEquals(1, sl.getReadLockCount())
      locker.run()
      assertTrue(sl.isReadLocked())
      assertEquals(2, sl.getReadLockCount())
      lock.unlock()
      lock.unlock()
      assertUnlocked(sl)
    }
  }

  @Test def testAsReadWriteLockWriteLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asReadWriteLock().writeLock()
    for (locker <- lockLockers(lock)) {
      locker.run()
      assertTrue(sl.isWriteLocked())
      assertFalse(sl.isReadLocked())
      assertFalse(lock.tryLock())
      lock.unlock()
      assertUnlocked(sl)
    }
  }

  @Test def testAsReadWriteLockReadLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asReadWriteLock().readLock()
    for (locker <- lockLockers(lock)) {
      locker.run()
      assertTrue(sl.isReadLocked())
      assertFalse(sl.isWriteLocked())
      assertEquals(1, sl.getReadLockCount())
      locker.run()
      assertTrue(sl.isReadLocked())
      assertEquals(2, sl.getReadLockCount())
      lock.unlock()
      lock.unlock()
      assertUnlocked(sl)
    }
  }

  @Test def testLockViewsDoNotSupportConditions(): Unit = {
    val sl = new StampedLock()
    assertThrowsAll(
      classOf[UnsupportedOperationException],
      action(sl.asWriteLock().newCondition()),
      action(sl.asReadLock().newCondition()),
      action(sl.asReadWriteLock().writeLock().newCondition()),
      action(sl.asReadWriteLock().readLock().newCondition())
    )
  }

  @Test def testCannotUnlockOptimisticReadStamps(): Unit = {
    {
      val sl = new StampedLock()
      val stamp = assertValid(sl, sl.tryOptimisticRead())
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryOptimisticRead()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryOptimisticRead()
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = assertValid(sl, sl.tryOptimisticRead())
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }
    {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = assertValid(sl, sl.tryOptimisticRead())
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      assertValid(sl, stamp)
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockWrite(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      assertValid(sl, stamp)
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockWrite(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }
    {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      assertValid(sl, stamp)
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }
    {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
    {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
  }

  private def writeLockInterruptiblyUninterrupted(sl: StampedLock): Long =
    try sl.writeLockInterruptibly()
    catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }

  private def tryWriteLockUninterrupted(
      sl: StampedLock,
      time: Long,
      unit: TimeUnit
  ): Long =
    try sl.tryWriteLock(time, unit)
    catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }

  private def readLockInterruptiblyUninterrupted(sl: StampedLock): Long =
    try sl.readLockInterruptibly()
    catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }

  private def tryReadLockUninterrupted(
      sl: StampedLock,
      time: Long,
      unit: TimeUnit
  ): Long =
    try sl.tryReadLock(time, unit)
    catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }

  @Test def testInvalidStampsThrowIllegalMonitorStateException(): Unit = {
    val sl = new StampedLock()

    assertThrowsAll(
      classOf[IllegalMonitorStateException],
      action(sl.unlockWrite(0L)),
      action(sl.unlockRead(0L)),
      action(sl.unlock(0L))
    )

    val optimisticStamp = sl.tryOptimisticRead()
    val readStamp = sl.readLock()
    sl.unlockRead(readStamp)
    val writeStamp = sl.writeLock()
    sl.unlockWrite(writeStamp)
    assertTrue(optimisticStamp != 0L && readStamp != 0L && writeStamp != 0L)
    val noLongerValidStamps = Array(optimisticStamp, readStamp, writeStamp)
    val assertNoLongerValidStampsThrow = new Runnable {
      override def run(): Unit =
        for (noLongerValidStamp <- noLongerValidStamps)
          assertThrowsAll(
            classOf[IllegalMonitorStateException],
            action(sl.unlockWrite(noLongerValidStamp)),
            action(sl.unlockRead(noLongerValidStamp)),
            action(sl.unlock(noLongerValidStamp))
          )
    }
    assertNoLongerValidStampsThrow.run()

    for (readLocker <- readLockers(); readUnlocker <- readUnlockers()) {
      val stamp = readLocker(sl)
      assertValid(sl, stamp)
      assertNoLongerValidStampsThrow.run()
      assertThrowsAll(
        classOf[IllegalMonitorStateException],
        action(sl.unlockWrite(stamp)),
        action(sl.unlockRead(sl.tryOptimisticRead())),
        action(sl.unlockRead(0L))
      )
      readUnlocker(sl, stamp)
      assertUnlocked(sl)
      assertNoLongerValidStampsThrow.run()
    }

    for (writeLocker <- writeLockers(); writeUnlocker <- writeUnlockers()) {
      val stamp = writeLocker(sl)
      assertValid(sl, stamp)
      assertNoLongerValidStampsThrow.run()
      assertThrowsAll(
        classOf[IllegalMonitorStateException],
        action(sl.unlockRead(stamp)),
        action(sl.unlockWrite(0L))
      )
      writeUnlocker(sl, stamp)
      assertUnlocked(sl)
      assertNoLongerValidStampsThrow.run()
    }
  }

  @Test def testDeeplyNestedReadLocks(): Unit = {
    assumeNotJDK8StampedLock()

    val lock = new StampedLock()
    val depth = 300
    val stamps = new Array[Long](depth)
    val lockers = readLockers()
    val unlockers = readUnlockers()
    for (i <- 0 until depth) {
      val readLocker = lockers(i % lockers.length)
      val stamp = readLocker(lock)
      assertEquals(i + 1, lock.getReadLockCount())
      assertTrue(lock.isReadLocked())
      stamps(i) = stamp
    }
    for (i <- 0 until depth) {
      val readUnlocker = unlockers(i % unlockers.length)
      assertEquals(depth - i, lock.getReadLockCount())
      assertTrue(lock.isReadLocked())
      readUnlocker(lock, stamps(depth - 1 - i))
    }
    assertUnlocked(lock)
  }

  @Test def testNonReentrant(): Unit = {
    val lock = new StampedLock()
    var stamp = lock.writeLock()
    assertValid(lock, stamp)
    assertEquals(0L, lock.tryWriteLock(0L, DAYS))
    assertEquals(0L, lock.tryReadLock(0L, DAYS))
    assertValid(lock, stamp)
    lock.unlockWrite(stamp)

    stamp = lock.tryWriteLock(1L, DAYS)
    assertEquals(0L, lock.tryWriteLock(0L, DAYS))
    assertValid(lock, stamp)
    lock.unlockWrite(stamp)

    stamp = lock.readLock()
    assertEquals(0L, lock.tryWriteLock(0L, DAYS))
    assertValid(lock, stamp)
    lock.unlockRead(stamp)
  }

  @Test def testNoOwnership(): Unit = {
    val futures = new ArrayList[Future[_]]()
    for (writeLocker <- writeLockers(); writeUnlocker <- writeUnlockers()) {
      val lock = new StampedLock()
      val stamp = writeLocker(lock)
      futures.add(cachedThreadPool.submit(new CheckedRunnable() {
        override def realRun(): Unit = {
          writeUnlocker(lock, stamp)
          assertUnlocked(lock)
          assertFalse(lock.validate(stamp))
        }
      }))
    }
    val it = futures.iterator()
    while (it.hasNext()) assertNull(it.next().get())
  }

  @Test def testSampleUsage(): Unit = {
    StampedLockTestPlatform.assumeStampStateInspectionMethods()

    class Point {
      private var x = 0.0
      private var y = 0.0
      private val sl = new StampedLock()

      def move(deltaX: Double, deltaY: Double): Unit = {
        val stamp = sl.writeLock()
        try {
          x += deltaX
          y += deltaY
        } finally {
          sl.unlockWrite(stamp)
        }
      }

      def distanceFromOrigin(): Double = {
        var currentX = 0.0
        var currentY = 0.0
        var stamp = sl.tryOptimisticRead()
        var retry = true
        while (retry) {
          if (stamp == 0L) stamp = sl.readLock()
          try {
            currentX = x
            currentY = y
          } finally {
            stamp = sl.tryConvertToOptimisticRead(stamp)
          }
          retry = stamp == 0L
        }
        Math.hypot(currentX, currentY)
      }

      def distanceFromOrigin2(): Double = {
        var stamp = sl.tryOptimisticRead()
        try {
          while (true) {
            if (stamp != 0L) {
              val currentX = x
              val currentY = y
              if (sl.validate(stamp)) return Math.hypot(currentX, currentY)
            }
            stamp = sl.readLock()
          }
          0.0
        } finally {
          if (StampedLockTestPlatform.isReadLockStamp(stamp))
            sl.unlockRead(stamp)
        }
      }

      def moveIfAtOrigin(newX: Double, newY: Double): Unit = {
        var stamp = sl.readLock()
        try {
          while (x == 0.0 && y == 0.0) {
            val ws = sl.tryConvertToWriteLock(stamp)
            if (ws != 0L) {
              stamp = ws
              x = newX
              y = newY
              return
            } else {
              sl.unlockRead(stamp)
              stamp = sl.writeLock()
            }
          }
        } finally {
          sl.unlock(stamp)
        }
      }
    }

    val p = new Point()
    p.move(3.0, 4.0)
    assertEquals(5.0, p.distanceFromOrigin(), 0.0)
    p.moveIfAtOrigin(5.0, 12.0)
    assertEquals(5.0, p.distanceFromOrigin2(), 0.0)
  }

  @Test def testStampStateInspectionMethods(): Unit = {
    StampedLockTestPlatform.assumeStampStateInspectionMethods()

    val lock = new StampedLock()

    assertFalse(StampedLockTestPlatform.isWriteLockStamp(0L))
    assertFalse(StampedLockTestPlatform.isReadLockStamp(0L))
    assertFalse(StampedLockTestPlatform.isLockStamp(0L))
    assertFalse(StampedLockTestPlatform.isOptimisticReadStamp(0L))

    {
      val stamp = lock.writeLock()
      for (i <- 0 until 2) {
        assertTrue(StampedLockTestPlatform.isWriteLockStamp(stamp))
        assertFalse(StampedLockTestPlatform.isReadLockStamp(stamp))
        assertTrue(StampedLockTestPlatform.isLockStamp(stamp))
        assertFalse(StampedLockTestPlatform.isOptimisticReadStamp(stamp))
        if (i == 0) lock.unlockWrite(stamp)
      }
    }

    {
      val stamp = lock.readLock()
      for (i <- 0 until 2) {
        assertFalse(StampedLockTestPlatform.isWriteLockStamp(stamp))
        assertTrue(StampedLockTestPlatform.isReadLockStamp(stamp))
        assertTrue(StampedLockTestPlatform.isLockStamp(stamp))
        assertFalse(StampedLockTestPlatform.isOptimisticReadStamp(stamp))
        if (i == 0) lock.unlockRead(stamp)
      }
    }

    {
      val optimisticStamp = lock.tryOptimisticRead()
      val readStamp = lock.tryConvertToReadLock(optimisticStamp)
      val writeStamp = lock.tryConvertToWriteLock(readStamp)
      for (i <- 0 until 2) {
        assertFalse(StampedLockTestPlatform.isWriteLockStamp(optimisticStamp))
        assertFalse(StampedLockTestPlatform.isReadLockStamp(optimisticStamp))
        assertFalse(StampedLockTestPlatform.isLockStamp(optimisticStamp))
        assertTrue(
          StampedLockTestPlatform.isOptimisticReadStamp(optimisticStamp)
        )

        assertFalse(StampedLockTestPlatform.isWriteLockStamp(readStamp))
        assertTrue(StampedLockTestPlatform.isReadLockStamp(readStamp))
        assertTrue(StampedLockTestPlatform.isLockStamp(readStamp))
        assertFalse(StampedLockTestPlatform.isOptimisticReadStamp(readStamp))

        assertTrue(StampedLockTestPlatform.isWriteLockStamp(writeStamp))
        assertFalse(StampedLockTestPlatform.isReadLockStamp(writeStamp))
        assertTrue(StampedLockTestPlatform.isLockStamp(writeStamp))
        assertFalse(StampedLockTestPlatform.isOptimisticReadStamp(writeStamp))
        if (i == 0) lock.unlockWrite(writeStamp)
      }
    }
  }

  @Test def testConcurrentAccess(): Unit = {
    StampedLockTestPlatform.assumeStampStateInspectionMethods()

    val sl = new StampedLock()
    val wl = sl.asWriteLock()
    val rl = sl.asReadLock()
    val testDurationMillis = if (expensiveTests) 1000L else 2L
    val nTasks = ThreadLocalRandom.current().nextInt(1, 10)
    val done = new AtomicBoolean(false)
    val futures = new ArrayList[CompletableFuture[Void]]()

    val stampedWriteLockers = Array[() => Long](
      () => sl.writeLock(),
      () => writeLockInterruptiblyUninterrupted(sl),
      () => tryWriteLockUninterrupted(sl, LONG_DELAY_MS, MILLISECONDS),
      () => {
        var stamp = 0L
        while (stamp == 0L)
          stamp = sl.tryConvertToWriteLock(sl.tryOptimisticRead())
        stamp
      },
      () => {
        var stamp = 0L
        while (stamp == 0L)
          stamp = sl.tryWriteLock()
        stamp
      },
      () => {
        var stamp = 0L
        while (stamp == 0L)
          stamp = sl.tryWriteLock(0L, DAYS)
        stamp
      }
    )
    val stampedReadLockers = Array[() => Long](
      () => sl.readLock(),
      () => readLockInterruptiblyUninterrupted(sl),
      () => tryReadLockUninterrupted(sl, LONG_DELAY_MS, MILLISECONDS),
      () => {
        var stamp = 0L
        while (stamp == 0L)
          stamp = sl.tryConvertToReadLock(sl.tryOptimisticRead())
        stamp
      },
      () => {
        var stamp = 0L
        while (stamp == 0L)
          stamp = sl.tryReadLock()
        stamp
      },
      () => {
        var stamp = 0L
        while (stamp == 0L)
          stamp = sl.tryReadLock(0L, DAYS)
        stamp
      }
    )
    val stampedWriteUnlockers = Array[Long => Unit](
      stamp => sl.unlockWrite(stamp),
      stamp => sl.unlock(stamp),
      _ => assertTrue(sl.tryUnlockWrite()),
      _ => wl.unlock(),
      stamp => sl.tryConvertToOptimisticRead(stamp)
    )
    val stampedReadUnlockers = Array[Long => Unit](
      stamp => sl.unlockRead(stamp),
      stamp => sl.unlock(stamp),
      _ => assertTrue(sl.tryUnlockRead()),
      _ => rl.unlock(),
      stamp => sl.tryConvertToOptimisticRead(stamp)
    )

    val writer = action {
      val locker = chooseRandomly(stampedWriteLockers)
      val unlocker = chooseRandomly(stampedWriteUnlockers)
      while (!done.get()) {
        val stamp = locker()
        try {
          assertTrue(StampedLockTestPlatform.isWriteLockStamp(stamp))
          assertTrue(sl.isWriteLocked())
          assertFalse(StampedLockTestPlatform.isReadLockStamp(stamp))
          assertFalse(sl.isReadLocked())
          assertEquals(0, sl.getReadLockCount())
          assertTrue(sl.validate(stamp))
        } finally {
          unlocker(stamp)
        }
      }
    }
    val reader = action {
      val locker = chooseRandomly(stampedReadLockers)
      val unlocker = chooseRandomly(stampedReadUnlockers)
      while (!done.get()) {
        val stamp = locker()
        try {
          assertFalse(StampedLockTestPlatform.isWriteLockStamp(stamp))
          assertFalse(sl.isWriteLocked())
          assertTrue(StampedLockTestPlatform.isReadLockStamp(stamp))
          assertTrue(sl.isReadLocked())
          assertTrue(sl.getReadLockCount() > 0)
          assertTrue(sl.validate(stamp))
        } finally {
          unlocker(stamp)
        }
      }
    }

    var i = nTasks
    while (i > 0) {
      i -= 1
      futures.add(
        CompletableFuture.runAsync(
          checkedRunnable(chooseRandomly(Array(writer, reader)))
        )
      )
    }
    Thread.sleep(testDurationMillis)
    done.set(true)
    val it = futures.iterator()
    while (it.hasNext()) checkTimedGet(it.next(), null)
  }
}
