/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: /src/test/tck/StampedLongTest
 *  revision 1.47, dated: 2021-01-26
 */

/* Scala Native (SN) Notes:
 *
 *   1) StampedLock was introduced in Java 8. This Test is under
 *     'require-jdk10' because several tests such as testSampleUsage
 *     testConcurrentAccess, use the is* stamp inspection methods introduced
 *     in Java 10.
 */

/*
 * Written by Doug Lea and Martin Buchholz
 * with assistance from members of JCP JSR-166 Expert Group and
 * released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent
package locks

import java.util.concurrent.TimeUnit.{DAYS, MILLISECONDS}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.StampedLock.{
  isLockStamp, isOptimisticReadStamp, isReadLockStamp, isWriteLockStamp
}
import java.util.concurrent.locks.{Lock, StampedLock}
import java.util.concurrent.{
  Callable, CompletableFuture, CountDownLatch, Future, ThreadLocalRandom,
  TimeUnit
}
import java.util.function.{BiConsumer, Consumer}
import java.util.{ArrayList, List}
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StampedLockTestOnJDK10 extends JSR166Test {
  import JSR166Test._

  /* Releases write lock, checking isWriteLocked before and after
   */
  private def releaseWriteLock(lock: StampedLock, stamp: scala.Long): Unit = {
    assertTrue("a1", lock.isWriteLocked())
    assertValid(lock, stamp)
    lock.unlockWrite(stamp)
    assertFalse("a2", lock.isWriteLocked())
    assertFalse("a3", lock.validate(stamp))
  }

  /* Releases read lock, checking isReadLocked before and after
   */
  private def releaseReadLock(lock: StampedLock, stamp: scala.Long): Unit = {
    assertTrue("a1", lock.isReadLocked())
    assertValid(lock, stamp)
    lock.unlockRead(stamp)
    assertFalse("a2", lock.isReadLocked())
    assertTrue("a3", lock.validate(stamp))
  }

  private def assertNonZero(v: scala.Long): scala.Long = {
    assertTrue(v != 0L)
    v
  }

  private def assertValid(lock: StampedLock, stamp: scala.Long): scala.Long = {
    assertTrue("a1", stamp != 0L)
    assertTrue("a2", lock.validate(stamp))
    stamp
  }

  private def assertUnlocked(lock: StampedLock): Unit = {
    assertFalse("a1", lock.isReadLocked())
    assertFalse("a2", lock.isWriteLocked())
    assertEquals(0, lock.getReadLockCount())
    assertValid(lock, lock.tryOptimisticRead())
  }

  /* makeBiConsumer() and makeConsumer() are used below when creating
   * Lists of BiConsumer or Consumer. This usage allows Scala 2.12 to
   * pass type checking. It is ugly and not idiomatic but tolerated
   * by Scala 3.
   */

  private def makeBiConsumer[T, U](f: (T, U) => Unit): BiConsumer[T, U] = {
    new BiConsumer[T, U] {
      def accept(t: T, u: U): Unit = f(t, u)
    }
  }

  def makeConsumer[T](f: T => Unit): Consumer[T] = {
    new Consumer[T] {
      def accept(t: T): Unit = f(t)
    }
  }

  def lockLockers(lock: Lock): List[Action] =
    List.of(
      () => lock.lock(),
      () => lock.lockInterruptibly(),
      () => { lock.tryLock(); () },
      () => { lock.tryLock(jl.Long.MIN_VALUE, DAYS); () },
      () => { lock.tryLock(0L, DAYS); () },
      () => { lock.tryLock(jl.Long.MAX_VALUE, DAYS); () }
    )

  def readLockers(): List[(StampedLock) => scala.Long] =
    List.of(
      (sl: StampedLock) => sl.readLock(),
      (sl: StampedLock) => sl.tryReadLock(),
      (sl: StampedLock) => readLockInterruptiblyUninterrupted(sl),
      (sl: StampedLock) =>
        tryReadLockUninterrupted(sl, jl.Long.MIN_VALUE, DAYS),
      (sl: StampedLock) => tryReadLockUninterrupted(sl, 0L, DAYS),
      (sl: StampedLock) => sl.tryConvertToReadLock(sl.tryOptimisticRead())
    )

  def readUnlockers(): List[BiConsumer[StampedLock, scala.Long]] =
    List.of(
      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, stamp: Long) =>
        sl.unlockRead(stamp)
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, _: Long) =>
        assertTrue(sl.tryUnlockRead())
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, _: Long) =>
        sl.asReadLock().unlock()
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, stamp: Long) =>
        sl.unlock(stamp)
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, stamp: Long) =>
        assertValid(sl, sl.tryConvertToOptimisticRead(stamp))
      )
    )

  def writeLockers(): List[(StampedLock) => scala.Long] =
    List.of(
      (sl: StampedLock) => sl.writeLock(),
      (sl: StampedLock) => sl.tryWriteLock(),
      (sl: StampedLock) => writeLockInterruptiblyUninterrupted(sl),
      (sl: StampedLock) =>
        tryWriteLockUninterrupted(sl, jl.Long.MIN_VALUE, DAYS),
      (sl: StampedLock) => tryWriteLockUninterrupted(sl, 0L, DAYS),
      (sl: StampedLock) => sl.tryConvertToWriteLock(sl.tryOptimisticRead())
    )

  def writeUnlockers(): List[BiConsumer[StampedLock, scala.Long]] =
    List.of(
      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, stamp: Long) =>
        sl.unlockWrite(stamp)
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, _: Long) =>
        assertTrue(sl.tryUnlockWrite())
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, _: Long) =>
        sl.asWriteLock().unlock()
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, stamp: Long) =>
        sl.unlock(stamp)
      ),

      makeBiConsumer[StampedLock, scala.Long]((sl: StampedLock, stamp: Long) =>
        assertValid(sl, sl.tryConvertToOptimisticRead(stamp))
      )
    )

  /* // FIXME Scala 2.12 Start
  def writeUnlockers(): List[BiConsumer[StampedLock, scala.Long]] = {
    List.of(
      (sl: StampedLock, stamp: Long) => sl.unlockWrite(stamp),
      (sl: StampedLock, _: Long) => assertTrue(sl.tryUnlockWrite()),
      (sl: StampedLock, _: Long) => sl.asWriteLock().unlock(),
      (sl: StampedLock, stamp: Long) => sl.unlock(stamp),
      (sl: StampedLock, stamp: Long) => assertValid(sl, sl.tryConvertToOptimisticRead(stamp))
    )
  }
   */ // FIXME Scala 2.12 Start

  def writeLockInterruptiblyUninterrupted(sl: StampedLock): scala.Long = {
    try { sl.writeLockInterruptibly() }
    catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }
  }

  def tryWriteLockUninterrupted(
      sl: StampedLock,
      time: scala.Long,
      unit: TimeUnit
  ): scala.Long = {
    try {
      sl.tryWriteLock(time, unit)
    } catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }
  }

  def readLockInterruptiblyUninterrupted(sl: StampedLock): scala.Long = {
    try {
      sl.readLockInterruptibly()
    } catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }
  }

  def tryReadLockUninterrupted(
      sl: StampedLock,
      time: scala.Long,
      unit: TimeUnit
  ): scala.Long = {
    try {
      sl.tryReadLock(time, unit)
    } catch {
      case ex: InterruptedException => throw new AssertionError(ex)
    }
  }

  /* Constructed StampedLock is in unlocked state
   */
  @Test def testConstructor(): Unit =
    assertUnlocked(new StampedLock())

  /* write-locking, then unlocking, an unlocked lock succeed
   */
  @Test def testWriteLock_lockUnlock(): Unit = {
    val lock = new StampedLock()

    writeLockers().forEach(writeLocker =>
      writeUnlockers().forEach(writeUnlocker => {
        assertFalse(lock.isWriteLocked())
        assertFalse(lock.isReadLocked())
        assertEquals(0, lock.getReadLockCount())

        val s = writeLocker.apply(lock)
        assertValid(lock, s)
        assertTrue(lock.isWriteLocked())
        assertFalse(lock.isReadLocked())
        assertEquals(0, lock.getReadLockCount())
        writeUnlocker.accept(lock, s)
        assertUnlocked(lock)
      })
    )
  }

  /* read-locking, then unlocking, an unlocked lock succeed
   */
  @Test def testReadLock_lockUnlock(): Unit = {
    val lock = new StampedLock()

    readLockers().forEach(readLocker =>
      readUnlockers().forEach(readUnlocker => {
        var s = 42L

        for (i <- 0 until 2) {
          s = assertValid(lock, readLocker.apply(lock))
          assertFalse(lock.isWriteLocked())
          assertTrue(lock.isReadLocked())
          assertEquals(i + 1, lock.getReadLockCount())
        }

        for (i <- 0 until 2) {
          assertFalse(lock.isWriteLocked())
          assertTrue(lock.isReadLocked())
          assertEquals(2 - i, lock.getReadLockCount())
          readUnlocker.accept(lock, s)
        }
        assertUnlocked(lock)
      })
    )
  }

  /* tryUnlockWrite fails if not write locked
   */
  @Test def testTryUnlockWrite_failure(): Unit = {
    val lock = new StampedLock()
    assertFalse("a1", lock.tryUnlockWrite())

    readLockers().forEach(readLocker =>
      readUnlockers().forEach(readUnlocker => {
        val s = assertValid(lock, readLocker.apply(lock))
        assertFalse("a2", lock.tryUnlockWrite())
        assertTrue("a3", lock.isReadLocked())
        readUnlocker.accept(lock, s)
        assertUnlocked(lock)
      })
    )
  }

  /* tryUnlockRead fails if not read locked
   */
  @Test def testTryUnlockRead_failure(): Unit = {
    val lock = new StampedLock()
    assertFalse(lock.tryUnlockRead())

    writeLockers().forEach(writeLocker =>
      writeUnlockers().forEach(writeUnlocker => {
        val s = writeLocker.apply(lock)
        assertFalse(lock.tryUnlockRead())
        assertTrue(lock.isWriteLocked())
        writeUnlocker.accept(lock, s)
        assertUnlocked(lock)
      })
    )
  }

  /* validate(0L) fails
   */
  @Test def testValidate0(): Unit = {
    val lock = new StampedLock()
    assertFalse(lock.validate(0L))
  }

  /* A stamp obtained from a successful lock operation validates while the lock
   *  is held
   */
  @Test def testValidate(): Unit = {
    val lock = new StampedLock()

    readLockers().forEach(readLocker =>
      readUnlockers().forEach(readUnlocker => {

        val s = assertNonZero(readLocker.apply(lock))
        assertTrue(lock.validate(s))
        readUnlocker.accept(lock, s)
      })
    )

    writeLockers().forEach(writeLocker =>
      writeUnlockers().forEach(writeUnlocker => {
        val s = assertNonZero(writeLocker.apply(lock))
        assertTrue(lock.validate(s))
        writeUnlocker.accept(lock, s)
      })
    )
  }

  /* A stamp obtained from an unsuccessful lock operation does not validate
   */
  @Test def testValidate2(): Unit = {
    val lock = new StampedLock()
    val s = assertNonZero(lock.writeLock())
    assertTrue("a1", lock.validate(s))
    assertFalse("a2", lock.validate(lock.tryWriteLock()))
    assertFalse(
      "a3",
      lock.validate(lock.tryWriteLock(randomExpiredTimeout(), randomTimeUnit()))
    )
    assertFalse("a4", lock.validate(lock.tryReadLock()))
    assertFalse(
      "a5",
      lock.validate(lock.tryWriteLock(randomExpiredTimeout(), randomTimeUnit()))
    )
    assertFalse("a6", lock.validate(lock.tryOptimisticRead()))
    lock.unlockWrite(s)
  }

  def assertThrowInterruptedExceptionWhenPreInterrupted(
      actions: Array[Action]
  ): Unit = {
    actions.foreach(action => {
      Thread.currentThread().interrupt()
      try {
        action.run()
        shouldThrow()
      } catch {
        case _: InterruptedException => // do nothing, success
        case fail: Throwable         => threadUnexpectedException(fail)
      }

      assertFalse(Thread.interrupted())
    })
  }

  /* interruptible operations throw InterruptedException when pre-interrupted
   */
  @Test def testInterruptibleOperationsThrowInterruptedExceptionWhenPreInterrupted()
      : Unit = {
    val lock = new StampedLock()

    val interruptibleLockActions = Array[Action](
      () => lock.writeLockInterruptibly(),
      () => lock.tryWriteLock(jl.Long.MIN_VALUE, DAYS),
      () => lock.tryWriteLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.readLockInterruptibly(),
      () => lock.tryReadLock(jl.Long.MIN_VALUE, DAYS),
      () => lock.tryReadLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.asWriteLock().lockInterruptibly(),
      () => lock.asWriteLock().tryLock(0L, DAYS),
      () => lock.asWriteLock().tryLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.asReadLock().lockInterruptibly(),
      () => lock.asReadLock().tryLock(0L, DAYS),
      () => lock.asReadLock().tryLock(jl.Long.MAX_VALUE, DAYS)
    )

    shuffle(interruptibleLockActions)

    assertThrowInterruptedExceptionWhenPreInterrupted(interruptibleLockActions)

    locally {
      val s = lock.writeLock()
      assertThrowInterruptedExceptionWhenPreInterrupted(
        interruptibleLockActions
      )
      lock.unlockWrite(s)
    }

    locally {
      val s = lock.readLock()
      assertThrowInterruptedExceptionWhenPreInterrupted(
        interruptibleLockActions
      )
      lock.unlockRead(s)
    }
  }

  def assertThrowInterruptedExceptionWhenInterrupted(
      actions: Array[Action]
  ): Unit = {
    val n = actions.length
    val futures = new Array[Future[_]](n)

    val threadsStarted = new CountDownLatch(n)
    val done = new CountDownLatch(n)

    for (i <- 0 until n) {
      val action = actions(i)
      futures(i) = cachedThreadPool.submit(new CheckedRunnable() {
        override def realRun(): Unit = {
          threadsStarted.countDown()
          try {
            action.run()
            shouldThrow()
          } catch {
            case _: InterruptedException => // do nothing, success
            case fail: Throwable         => threadUnexpectedException(fail)
          }

          assertFalse(Thread.interrupted())
          done.countDown()
        }
      })
    }

    await(threadsStarted)
    assertEquals(n, done.getCount())

    for (i <- 0 until n) // Interrupt all the tasks
      futures(i).cancel(true)

    await(done)
  }

  /* interruptible operations throw InterruptedException when write locked and
   *  interrupted
   */
  @Test def testInterruptibleOperationsThrowInterruptedExceptionWriteLockedInterrupted()
      : Unit = {
    val lock = new StampedLock()
    val stamp = lock.writeLock()

    val interruptibleLockBlockingActions = Array[Action](
      () => lock.writeLockInterruptibly(),
      () => lock.tryWriteLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.readLockInterruptibly(),
      () => lock.tryReadLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.asWriteLock().lockInterruptibly(),
      () => lock.asWriteLock().tryLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.asReadLock().lockInterruptibly(),
      () => lock.asReadLock().tryLock(jl.Long.MAX_VALUE, DAYS)
    )

    shuffle(interruptibleLockBlockingActions)

    assertThrowInterruptedExceptionWhenInterrupted(
      interruptibleLockBlockingActions
    )

    releaseWriteLock(lock, stamp)
  }

  /* interruptible operations throw InterruptedException when read locked and
   *  interrupted
   */
  @Test def testInterruptibleOperationsThrowInterruptedExceptionReadLockedInterrupted()
      : Unit = {
    val lock = new StampedLock()
    val stamp = lock.readLock()

    val interruptibleLockBlockingActions = Array[Action](
      () => lock.writeLockInterruptibly(),
      () => lock.tryWriteLock(jl.Long.MAX_VALUE, DAYS),
      () => lock.asWriteLock().lockInterruptibly(),
      () => lock.asWriteLock().tryLock(jl.Long.MAX_VALUE, DAYS)
    )

    shuffle(interruptibleLockBlockingActions)

    assertThrowInterruptedExceptionWhenInterrupted(
      interruptibleLockBlockingActions
    )

    releaseReadLock(lock, stamp)
  }

  /* Non-interruptible operations ignore and preserve interrupt status
   */
  @Test def testNonInterruptibleOperationsIgnoreInterrupts(): Unit = {
    val lock = new StampedLock()
    Thread.currentThread().interrupt()

    readUnlockers().forEach(readUnlocker => {
      var s = assertValid(lock, lock.readLock())
      readUnlocker.accept(lock, s)
      s = assertValid(lock, lock.tryReadLock())
      readUnlocker.accept(lock, s)
    })

    lock.asReadLock().lock()
    lock.asReadLock().unlock()

    writeUnlockers().forEach(writeUnlocker => {
      var s = assertValid(lock, lock.writeLock())
      writeUnlocker.accept(lock, s)
      s = assertValid(lock, lock.tryWriteLock())
      writeUnlocker.accept(lock, s)
    })

    lock.asWriteLock().lock()
    lock.asWriteLock().unlock()

    assertTrue(Thread.interrupted())
  }

  /* tryWriteLock on an unlocked lock succeeds
   */
  @Test def testTryWriteLock(): Unit = {
    val lock = new StampedLock()
    val s = lock.tryWriteLock()
    assertTrue("a1", s != 0L)
    assertTrue("a2", lock.isWriteLocked())
    assertEquals("a3", 0L, lock.tryWriteLock())
    releaseWriteLock(lock, s)
  }

  /* tryWriteLock fails if locked
   */
  @Test def testTryWriteLockWhenLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.writeLock()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertEquals(0L, lock.tryWriteLock())
      }
    })

    assertEquals(0L, lock.tryWriteLock())
    awaitTermination(t)
    releaseWriteLock(lock, s)
  }

  /* tryReadLock fails if write-locked
   */
  @Test def testTryReadLockWhenLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.writeLock()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertEquals(0L, lock.tryReadLock())
      }
    })

    assertEquals(0L, lock.tryReadLock())
    awaitTermination(t)
    releaseWriteLock(lock, s)
  }

  /* Multiple threads can hold a read lock when not write-locked
   */
  @Test def testMultipleReadLocks(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
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
        lock.asReadLock().tryLock(jl.Long.MIN_VALUE, DAYS)
        lock.asReadLock().unlock()
      }
    })

    awaitTermination(t)
    lock.unlockRead(s)
  }

  /* writeLock() succeeds only after a reading thread unlocks
   */
  @Test def testWriteAfterReadLock(): Unit = {
    val aboutToLock = new CountDownLatch(1)
    val lock = new StampedLock()
    val rs = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        aboutToLock.countDown()
        val s = lock.writeLock()
        assertTrue(lock.isWriteLocked())
        assertFalse(lock.isReadLocked())
        lock.unlockWrite(s)
      }
    })

    await(aboutToLock)
    assertThreadBlocks(t, Thread.State.WAITING)
    assertFalse("a1", lock.isWriteLocked())
    assertTrue("a2", lock.isReadLocked())
    lock.unlockRead(rs)
    awaitTermination(t)
    assertUnlocked(lock)
  }

  /* writeLock() succeeds only after reading threads unlock
   */
  @Test def testWriteAfterMultipleReadLocks(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t1 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        val rs = lock.readLock()
        lock.unlockRead(rs)
      }
    })

    awaitTermination(t1)

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        val ws = lock.writeLock()
        lock.unlockWrite(ws)
      }
    })

    assertTrue("a1", lock.isReadLocked())
    assertFalse("a2", lock.isWriteLocked())
    lock.unlockRead(s)
    awaitTermination(t2)
    assertUnlocked(lock)
  }

  /* readLock() succeed only after a writing thread unlocks
   */
  @Test def testReadAfterWriteLock(): Unit = {
    val lock = new StampedLock()
    val threadsStarted = new CountDownLatch(2)
    val s = lock.writeLock()
    val acquireReleaseReadLock = new CheckedRunnable() {
      def realRun(): Unit = {
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
    assertTrue("a1", lock.isWriteLocked())
    assertFalse("a2", lock.isReadLocked())
    releaseWriteLock(lock, s)
    awaitTermination(t1)
    awaitTermination(t2)
    assertUnlocked(lock)
  }

  /* tryReadLock succeeds if read locked but not write locked
   */
  @Test def testTryLockWhenReadLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        val rs = lock.tryReadLock()
        assertValid(lock, rs)
        lock.unlockRead(rs)
      }
    })

    awaitTermination(t)
    lock.unlockRead(s)
  }

  /* tryWriteLock fails when read locked
   */
  @Test def testTryWriteLockWhenReadLocked(): Unit = {
    val lock = new StampedLock()
    val s = lock.readLock()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertEquals(0L, lock.tryWriteLock())
      }
    })

    awaitTermination(t)
    lock.unlockRead(s)
  }

  /* timed lock operations time out if lock not available
   */
  @Test def testTimedLock_Timeout(): Unit = {
    val futures = new ArrayList[Future[_]]()

    // Write locked
    val lock = new StampedLock()
    val stamp = lock.writeLock()
    assertEquals("a1", 0L, lock.tryReadLock(0L, DAYS))
    assertEquals("a2", 0L, lock.tryReadLock(jl.Long.MIN_VALUE, DAYS))
    assertFalse("a3", lock.asReadLock().tryLock(0L, DAYS))
    assertFalse("a4", lock.asReadLock().tryLock(jl.Long.MIN_VALUE, DAYS))
    assertEquals("a5", 0L, lock.tryWriteLock(0L, DAYS))
    assertEquals("a6", 0L, lock.tryWriteLock(jl.Long.MIN_VALUE, DAYS))
    assertFalse("a7", lock.asWriteLock().tryLock(0L, DAYS))
    assertFalse("a8", lock.asWriteLock().tryLock(jl.Long.MIN_VALUE, DAYS))

    futures.add(cachedThreadPool.submit(new CheckedRunnable() {
      def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertEquals(0L, lock.tryWriteLock(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    }))

    futures.add(cachedThreadPool.submit(new CheckedRunnable() {
      def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertEquals(0L, lock.tryReadLock(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    }))

    // Read locked
    val lock2 = new StampedLock()
    val stamp2 = lock2.readLock()
    assertEquals("a9", 0L, lock2.tryWriteLock(0L, DAYS))
    assertEquals("a10", 0L, lock2.tryWriteLock(jl.Long.MIN_VALUE, DAYS))
    assertFalse("a11", lock2.asWriteLock().tryLock(0L, DAYS))
    assertFalse("a12", lock2.asWriteLock().tryLock(jl.Long.MIN_VALUE, DAYS))

    futures.add(cachedThreadPool.submit(new CheckedRunnable() {
      def realRun(): Unit = {
        val startTime = System.nanoTime()
        assertEquals(0L, lock2.tryWriteLock(timeoutMillis(), MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    }))

    futures.forEach(future => assertNull(future.get()))

    releaseWriteLock(lock, stamp)
    releaseReadLock(lock2, stamp2)
  }

  /* writeLockInterruptibly succeeds if unlocked
   */
  @Test def testWriteLockInterruptibly(): Unit = {
    val lock = new StampedLock()
    val s = lock.writeLockInterruptibly()
    assertTrue(lock.isWriteLocked())
    releaseWriteLock(lock, s)
  }

  /* readLockInterruptibly succeeds if lock free
   */
  @Test def testReadLockInterruptibly(): Unit = {
    val lock = new StampedLock()

    val s = assertValid(lock, lock.readLockInterruptibly())
    assertTrue(lock.isReadLocked())
    lock.unlockRead(s)

    lock.asReadLock().lockInterruptibly()
    assertTrue(lock.isReadLocked())
    lock.asReadLock().unlock()
  }

  /* // Serialization is not supported by Scala Native port
    /*
   * A serialized lock deserializes as unlocked
   */
    @Test def testSerialization(): Unit = {
        val lock = new StampedLock()
        lock.writeLock()
        StampedLock clone = serialClone(lock)
        assertTrue(lock.isWriteLocked())
        assertFalse(clone.isWriteLocked())
        long s = clone.writeLock()
        assertTrue(clone.isWriteLocked())
        clone.unlockWrite(s)
        assertFalse(clone.isWriteLocked())
    }
   */ // Serialization is not supported by Scala Native port

  /* toString indicates current lock state
   */
  @Test def testToString(): Unit = {
    val lock = new StampedLock()
    assertTrue("a1", lock.toString().contains("Unlocked"))
    var s = lock.writeLock()
    assertTrue("a2", lock.toString().contains("Write-locked"))
    lock.unlockWrite(s)
    s = lock.readLock()
    assertTrue("a3", lock.toString().contains("Read-locks"))
    releaseReadLock(lock, s)
  }

  /* tryOptimisticRead succeeds and validates if unlocked, fails if exclusively
   *  locked
   */
  @Test def testValidateOptimistic(): Unit = {
    val lock = new StampedLock()

    assertValid(lock, lock.tryOptimisticRead())

    writeLockers().forEach(writeLocker => {
      val s = assertValid(lock, writeLocker.apply(lock))
      assertEquals(0L, lock.tryOptimisticRead())
      releaseWriteLock(lock, s)
    })

    readLockers().forEach(readLocker => {
      val s = assertValid(lock, readLocker.apply(lock))
      val p = assertValid(lock, lock.tryOptimisticRead())
      releaseReadLock(lock, s)
      assertTrue(lock.validate(p))
    })

    assertValid(lock, lock.tryOptimisticRead())
  }

  /* tryOptimisticRead stamp does not validate if a write lock intervenes
   */
  @Test def testValidateOptimisticWriteLocked(): Unit = {
    val lock = new StampedLock()
    val p = assertValid(lock, lock.tryOptimisticRead())
    val s = assertValid(lock, lock.writeLock())
    assertFalse(lock.validate(p))
    assertEquals(0L, lock.tryOptimisticRead())
    assertTrue(lock.validate(s))
    lock.unlockWrite(s)
  }

  /* tryOptimisticRead stamp does not validate if a write lock intervenes in
   *  another thread
   */
  @Test def testValidateOptimisticWriteLocked2(): Unit = {
    val locked = new CountDownLatch(1)
    val lock = new StampedLock()
    val p = assertValid(lock, lock.tryOptimisticRead())

    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        lock.writeLockInterruptibly()
        locked.countDown()
        lock.writeLockInterruptibly()
      }
    })

    await(locked)
    assertFalse("a1", lock.validate(p))
    assertEquals("a2", 0L, lock.tryOptimisticRead())
    assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
    assertTrue("a3", lock.isWriteLocked())
  }

  /* tryConvertToOptimisticRead succeeds and validates if successfully locked
   */
  @Test def testTryConvertToOptimisticRead(): Unit = {
    val lock = new StampedLock()
    var s = -1L
    var p = -1L
    var q = -1L

    assertEquals("a1", 0L, lock.tryConvertToOptimisticRead(0L))

    s = assertValid(lock, lock.tryOptimisticRead())
    assertEquals("a2", s, lock.tryConvertToOptimisticRead(s))
    assertTrue("a3", lock.validate(s))

    writeLockers().forEach(writeLocker => {
      s = assertValid(lock, writeLocker.apply(lock))
      p = assertValid(lock, lock.tryConvertToOptimisticRead(s))
      assertFalse("a4", lock.validate(s))
      assertTrue("a5", lock.validate(p))
      assertUnlocked(lock)
    })

    readLockers().forEach(readLocker => {
      s = assertValid(lock, readLocker.apply(lock))
      q = assertValid(lock, lock.tryOptimisticRead())
      assertEquals("a6", q, lock.tryConvertToOptimisticRead(q))
      assertTrue("a7", lock.validate(q))
      assertTrue("a8", lock.isReadLocked())
      p = assertValid(lock, lock.tryConvertToOptimisticRead(s))
      assertTrue("a9", lock.validate(p))
      assertTrue("a10", lock.validate(s))
      assertUnlocked(lock)
      assertEquals("a11", q, lock.tryConvertToOptimisticRead(q))
      assertTrue("a12", lock.validate(q))
    })
  }

  /* tryConvertToReadLock succeeds for valid stamps
   */
  @Test def testTryConvertToReadLock(): Unit = {
    val lock = new StampedLock()
    var s = -1L
    var p = -1L

    assertEquals("a1", 0L, lock.tryConvertToReadLock(0L))

    s = assertValid(lock, lock.tryOptimisticRead())
    p = assertValid(lock, lock.tryConvertToReadLock(s))
    assertTrue("a1", lock.isReadLocked())
    assertEquals("a2", 1, lock.getReadLockCount())
    assertTrue("a3", lock.validate(s))
    lock.unlockRead(p)

    s = assertValid(lock, lock.tryOptimisticRead())
    lock.readLock()
    p = assertValid(lock, lock.tryConvertToReadLock(s))
    assertTrue("a4", lock.isReadLocked())
    assertEquals("a5", 2, lock.getReadLockCount())
    lock.unlockRead(p)
    lock.unlockRead(p)
    assertUnlocked(lock)

    readUnlockers().forEach(readUnlocker => {
      writeLockers().forEach(writeLocker => {
        s = assertValid(lock, writeLocker.apply(lock))
        p = assertValid(lock, lock.tryConvertToReadLock(s))
        assertFalse("a6", lock.validate(s))
        assertTrue("a7", lock.isReadLocked())
        assertEquals("a8", 1, lock.getReadLockCount())
        readUnlocker.accept(lock, p)
      })

      readLockers().forEach(readLocker => {
        s = assertValid(lock, readLocker.apply(lock))
        assertEquals("a9", s, lock.tryConvertToReadLock(s))
        assertTrue("a10", lock.validate(s))
        assertTrue("a11", lock.isReadLocked())
        assertEquals("a12", 1, lock.getReadLockCount())
        readUnlocker.accept(lock, s)
      })
    })
  }

  /* tryConvertToWriteLock succeeds if lock available; fails if multiply read
   *  locked
   */
  @Test def testTryConvertToWriteLock(): Unit = {
    val lock = new StampedLock()
    var s = -1L
    var p = -1L

    assertEquals("a1", 0L, lock.tryConvertToWriteLock(0L))

    assertTrue("a2", { s = lock.tryOptimisticRead(); s } != 0L)
    assertTrue("a3", { p = lock.tryConvertToWriteLock(s); p } != 0L)
    assertTrue("a4", lock.isWriteLocked())
    lock.unlockWrite(p)

    writeUnlockers().forEach(writeUnlocker => {
      writeLockers().forEach(writeLocker => {
        s = assertValid(lock, writeLocker.apply(lock))
        assertEquals("a5", s, lock.tryConvertToWriteLock(s))
        assertTrue("a6", lock.validate(s))
        assertTrue("a7", lock.isWriteLocked())
        writeUnlocker.accept(lock, s)
      })

      readLockers().forEach(readLocker => {
        s = assertValid(lock, readLocker.apply(lock))
        p = assertValid(lock, lock.tryConvertToWriteLock(s))
        assertFalse("a8", lock.validate(s))
        assertTrue("a9", lock.validate(p))
        assertTrue("a10", lock.isWriteLocked())
        writeUnlocker.accept(lock, p)
      })
    })

    // failure if multiply read locked
    readLockers().forEach(readLocker => {
      s = assertValid(lock, readLocker.apply(lock))
      p = assertValid(lock, readLocker.apply(lock))
      assertEquals(0L, lock.tryConvertToWriteLock(s))
      assertTrue("a11", lock.validate(s))
      assertTrue("a12", lock.validate(p))
      assertEquals("a13", 2, lock.getReadLockCount())
      lock.unlock(p)
      lock.unlock(s)
      assertUnlocked(lock)
    })
  }

  /* asWriteLock can be locked and unlocked
   */
  @Test def testAsWriteLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asWriteLock()

    lockLockers(lock).forEach(locker => {
      locker.run()
      assertTrue("a1", sl.isWriteLocked())
      assertFalse("a2", sl.isReadLocked())
      assertFalse("a3", lock.tryLock())
      lock.unlock()
      assertUnlocked(sl)
    })
  }

  /* asReadLock can be locked and unlocked
   */
  @Test def testAsReadLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asReadLock()

    lockLockers(lock).forEach(locker => {
      locker.run()
      assertTrue("a1", sl.isReadLocked())
      assertFalse("a2", sl.isWriteLocked())
      assertEquals("a3", 1, sl.getReadLockCount())
      locker.run()
      assertTrue("a4", sl.isReadLocked())
      assertEquals(2, sl.getReadLockCount())
      lock.unlock()
      lock.unlock()
      assertUnlocked(sl)
    })
  }

  /* asReadWriteLock.writeLock can be locked and unlocked
   */
  @Test def testAsReadWriteLockWriteLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asReadWriteLock().writeLock()

    lockLockers(lock).forEach(locker => {
      locker.run()
      assertTrue("a1", sl.isWriteLocked())
      assertFalse("a2", sl.isReadLocked())
      assertFalse("a3", lock.tryLock())
      lock.unlock()
      assertUnlocked(sl)
    })
  }

  /* asReadWriteLock.readLock can be locked and unlocked
   */
  @Test def testAsReadWriteLockReadLock(): Unit = {
    val sl = new StampedLock()
    val lock = sl.asReadWriteLock().readLock()

    lockLockers(lock).forEach(locker => {

      locker.run()
      assertTrue("a1", sl.isReadLocked())
      assertFalse("a2", sl.isWriteLocked())
      assertEquals("a3", 1, sl.getReadLockCount())
      locker.run()
      assertTrue("a4", sl.isReadLocked())
      assertEquals("a5", 2, sl.getReadLockCount())
      lock.unlock()
      lock.unlock()
      assertUnlocked(sl)
    })
  }

  /* Lock.newCondition throws UnsupportedOperationException
   */
  @Test def testLockViewsDoNotSupportConditions(): Unit = {
    val sl = new StampedLock()
    assertThrows(
      classOf[UnsupportedOperationException],
      sl.asWriteLock().newCondition()
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      sl.asReadLock().newCondition()
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      sl.asReadWriteLock().writeLock().newCondition()
    )

    assertThrows(
      classOf[UnsupportedOperationException],
      sl.asReadWriteLock().readLock().newCondition()
    )

  }

  /* Passing optimistic read stamps to unlock operations result in
   *  IllegalMonitorStateException
   */
  @Test def testCannotUnlockOptimisticReadStamps(): Unit = {
    locally {
      val sl = new StampedLock()
      val stamp = assertValid(sl, sl.tryOptimisticRead())
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryOptimisticRead()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryOptimisticRead()
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = assertValid(sl, sl.tryOptimisticRead())
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }

    locally {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = assertValid(sl, sl.tryOptimisticRead())
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      assertValid(sl, stamp)
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockWrite(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.writeLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      assertValid(sl, stamp)
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockWrite(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.writeLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }

    locally {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      assertValid(sl, stamp)
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(stamp))
    }

    locally {
      val sl = new StampedLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }

    locally {
      val sl = new StampedLock()
      sl.readLock()
      val stamp = sl.tryConvertToOptimisticRead(sl.readLock())
      sl.readLock()
      assertThrows(classOf[IllegalMonitorStateException], sl.unlock(stamp))
    }
  }

  /* Invalid stamps result in IllegalMonitorStateException
   */
  @Test def testInvalidStampsThrowIllegalMonitorStateException(): Unit = {
    val sl = new StampedLock()

    assertThrows(
      classOf[IllegalMonitorStateException],
      sl.unlockWrite(0L)
    )

    assertThrows(
      classOf[IllegalMonitorStateException],
      sl.unlockRead(0L)
    )

    assertThrows(
      classOf[IllegalMonitorStateException],
      sl.unlock(0L)
    )

    val optimisticStamp = sl.tryOptimisticRead()
    val readStamp = sl.readLock()
    sl.unlockRead(readStamp)

    val writeStamp = sl.writeLock()
    sl.unlockWrite(writeStamp)
    assertTrue(
      "a1",
      optimisticStamp != 0L && readStamp != 0L && writeStamp != 0L
    )

    val noLongerValidStamps = Array(optimisticStamp, readStamp, writeStamp)

    val assertNoLongerValidStampsThrow: Runnable = () => {
      noLongerValidStamps.foreach(noLongerValidStamp => {
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlockWrite(noLongerValidStamp)
        )
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlockRead(noLongerValidStamp)
        )
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlock(noLongerValidStamp)
        )
      })
    }

    assertNoLongerValidStampsThrow.run()

    readLockers().forEach(readLocker =>
      readUnlockers().forEach(readUnlocker => {
        val stamp = readLocker.apply(sl)
        assertValid(sl, stamp)
        assertNoLongerValidStampsThrow.run()
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlockWrite(stamp)
        )
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlockRead(sl.tryOptimisticRead())
        )
        assertThrows(classOf[IllegalMonitorStateException], sl.unlockRead(0L))

        readUnlocker.accept(sl, stamp)
        assertUnlocked(sl)
        assertNoLongerValidStampsThrow.run()
      })
    )

    writeLockers().forEach(writeLocker =>
      writeUnlockers().forEach(writeUnlocker => {
        val stamp = writeLocker.apply(sl)
        assertValid(sl, stamp)
        assertNoLongerValidStampsThrow.run()
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlockRead(stamp)
        )
        assertThrows(
          classOf[IllegalMonitorStateException],
          sl.unlockWrite(0L)
        )
        writeUnlocker.accept(sl, stamp)
        assertUnlocked(sl)
        assertNoLongerValidStampsThrow.run()
      })
    )
  }

  /* Read locks can be very deeply nested
   */
  @Test def testDeeplyNestedReadLocks(): Unit = {
    val lock = new StampedLock()
    val depth = 300
    val stamps = new Array[scala.Long](depth)

    val readLockerz = readLockers()
    val readUnlockerz = readUnlockers()

    for (i <- 0 until depth) {
      val readLocker = readLockerz.get(i % readLockerz.size())
      val stamp = readLocker.apply(lock)
      assertEquals("a1", i + 1, lock.getReadLockCount())
      assertTrue("a2", lock.isReadLocked())
      stamps(i) = stamp
    }

    for (i <- 0 until depth) {
      val readUnlocker = readUnlockerz.get(i % readUnlockerz.size())
      assertEquals("a3", depth - i, lock.getReadLockCount())
      assertTrue("a4", lock.isReadLocked())
      readUnlocker.accept(lock, stamps(depth - 1 - i))
    }

    assertUnlocked(lock)
  }

  /* Stamped locks are not reentrant.
   */
  @Test def testNonReentrant(): Unit = {
    val lock = new StampedLock()
    var stamp = lock.writeLock()

    assertValid(lock, stamp)
    assertEquals("a1", 0L, lock.tryWriteLock(0L, DAYS))
    assertEquals("a2", 0L, lock.tryReadLock(0L, DAYS))
    assertValid(lock, stamp)
    lock.unlockWrite(stamp)

    stamp = lock.tryWriteLock(1L, DAYS)
    assertEquals("a3", 0L, lock.tryWriteLock(0L, DAYS))
    assertValid(lock, stamp)
    lock.unlockWrite(stamp)

    stamp = lock.readLock()
    assertEquals("a4", 0L, lock.tryWriteLock(0L, DAYS))
    assertValid(lock, stamp)
    lock.unlockRead(stamp)
  }

  /* """StampedLocks have no notion of ownership. Locks acquired in one thread
   *  can be released or converted in another."""
   */
  @Test def testNoOwnership(): Unit = {
    val futures = new ArrayList[Future[_]]()

    writeLockers().forEach(writeLocker =>
      writeUnlockers().forEach(writeUnlocker => {
        val lock = new StampedLock()
        val stamp = writeLocker.apply(lock)
        futures.add(cachedThreadPool.submit(new CheckedRunnable() {
          def realRun(): Unit = {
            writeUnlocker.accept(lock, stamp)
            assertUnlocked(lock)
            assertFalse(lock.validate(stamp))
          }
        }))
      })
    )

    futures.forEach(future => assertNull(future.get()))
  }

  /* Tries out sample usage code from StampedLock javadoc. */
  @Test def testSampleUsage(): Unit = {
    class Point {
      private var x = 0.0
      private var y = 0.0
      private val sl = new StampedLock()

      def move(deltaX: scala.Double, deltaY: scala.Double): Unit = { // an exclusively locked method
        val stamp = sl.writeLock()
        try {
          x += deltaX;
          y += deltaY;
        } finally {
          sl.unlockWrite(stamp)
        }
      }

      def distanceFromOrigin(): scala.Double = { // A read-only method
        var currentX = 0.0
        var currentY = 0.0
        var stamp = sl.tryOptimisticRead()

        while ({
          if (stamp == 0L)
            stamp = sl.readLock()
          try {
            // possibly racy reads
            currentX = x;
            currentY = y;
          } finally {
            stamp = sl.tryConvertToOptimisticRead(stamp)
          }

          stamp == 0
        }) ()

        Math.hypot(currentX, currentY)
      }

      /* SN: This method is highly modified. The Java original example used,
       *     and was proud of using, a 'continue' statement with a label.
       *     OK  for Java but Yeech! for Scala.
       */
      def distanceFromOrigin2(): scala.Double = {
        if (true)
          distanceFromOrigin()
        else {
          var currentX = 0.0
          var currentY = 0.0
          var stamp = sl.tryOptimisticRead()
          try {
            var done = false
            while (!done) {
              // possibly racy reads
              currentX = x
              currentY = y

              if (sl.validate(stamp))
                done = true
              else {
                stamp = sl.readLock()
              }
            }

            Math.hypot(currentX, currentY)

          } finally {
            if (StampedLock.isReadLockStamp(stamp))
              sl.unlockRead(stamp)
          }
        }
      }

      def moveIfAtOrigin(newX: scala.Double, newY: scala.Double): Unit = {
        var stamp = sl.readLock()

        var done = false

        try {
          while ((x == 0.0 && y == 0.0) && !done) {
            val ws = sl.tryConvertToWriteLock(stamp)
            if (ws != 0L) {
              stamp = ws
              x = newX
              y = newY
              done = true
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
    assertEquals("a1", 5.0, p.distanceFromOrigin(), 0.0)
    p.moveIfAtOrigin(5.0, 12.0)
    assertEquals("a2", 5.0, p.distanceFromOrigin2(), 0.0)
  }

  /* Stamp inspection methods work as expected, and do not inspect the state of
   *  the lock itself.
   */
  @Test def testStampStateInspectionMethods(): Unit = {
    val lock = new StampedLock()

    assertFalse("a1", isWriteLockStamp(0L))
    assertFalse("a2", isReadLockStamp(0L))
    assertFalse("a3", isLockStamp(0L))
    assertFalse("a4", isOptimisticReadStamp(0L))

    locally {
      val stamp = lock.writeLock()
      for (i <- 0 until 2) {
        assertTrue("a5", isWriteLockStamp(stamp))
        assertFalse("a6", isReadLockStamp(stamp))
        assertTrue("a7", isLockStamp(stamp))
        assertFalse("a8", isOptimisticReadStamp(stamp))
        if (i == 0)
          lock.unlockWrite(stamp)
      }
    }

    locally {
      val stamp = lock.readLock()
      for (i <- 0 until 2) {
        assertFalse("a9", isWriteLockStamp(stamp))
        assertTrue("a10", isReadLockStamp(stamp))
        assertTrue("a11", isLockStamp(stamp))
        assertFalse(isOptimisticReadStamp(stamp))
        if (i == 0)
          lock.unlockRead(stamp)
      }
    }

    locally {
      val optimisticStamp = lock.tryOptimisticRead()
      val readStamp = lock.tryConvertToReadLock(optimisticStamp)
      val writeStamp = lock.tryConvertToWriteLock(readStamp)
      for (i <- 0 until 2) {
        assertFalse("a12", isWriteLockStamp(optimisticStamp))
        assertFalse("a13", isReadLockStamp(optimisticStamp))
        assertFalse("a14", isLockStamp(optimisticStamp))
        assertTrue("a15", isOptimisticReadStamp(optimisticStamp))

        assertFalse("a16", isWriteLockStamp(readStamp))
        assertTrue("a17", isReadLockStamp(readStamp))
        assertTrue("a18", isLockStamp(readStamp))
        assertFalse("a19", isOptimisticReadStamp(readStamp))

        assertTrue("a20", isWriteLockStamp(writeStamp))
        assertFalse("a21", isReadLockStamp(writeStamp))
        assertTrue("a22", isLockStamp(writeStamp))
        assertFalse("a23", isOptimisticReadStamp(writeStamp))
        if (i == 0)
          lock.unlockWrite(writeStamp)
      }
    }
  }

  /* Multiple threads repeatedly contend for the same lock.
   */
  @Test def testConcurrentAccess(): Unit = {
    val sl = new StampedLock()
    val wl = sl.asWriteLock()
    val rl = sl.asReadLock()

    val testDurationMillis = if (expensiveTests) 1000 else 2
    val nTasks = ThreadLocalRandom.current().nextInt(1, 10)
    val done = new AtomicBoolean(false)
    val futures = new ArrayList[Future[_]]()

    val stampedWriteLockers: List[Callable[Long]] = List.of(
      () => sl.writeLock(),
      () => writeLockInterruptiblyUninterrupted(sl),
      () => tryWriteLockUninterrupted(sl, LONG_DELAY_MS, MILLISECONDS),
      () => {
        var stamp = 0L

        while ({
          stamp = sl.tryConvertToWriteLock(sl.tryOptimisticRead())
          stamp == 0L
        }) ()

        stamp
      },
      () => {
        var stamp = 0L

        // do { stamp = sl.tryWriteLock() } while (stamp == 0L)
        while ({
          stamp = sl.tryWriteLock()
          stamp == 0L
        }) ()

        stamp
      },

      () => {
        var stamp = 0L

        // do { stamp = sl.tryWriteLock(0L, DAYS) } while (stamp == 0L)
        while ({
          stamp = sl.tryWriteLock(0L, DAYS)
          stamp == 0L
        }) ()

        stamp
      }
    )

    val stampedReadLockers: List[Callable[Long]] = List.of(
      () => sl.readLock(),
      () => readLockInterruptiblyUninterrupted(sl),
      () => tryReadLockUninterrupted(sl, LONG_DELAY_MS, MILLISECONDS),
      () => {
        var stamp = 0L

        while ({
          stamp = sl.tryConvertToReadLock(sl.tryOptimisticRead())
          stamp == 0L
        }) ()

        stamp
      },
      () => {
        var stamp = 0L

        while ({
          stamp = sl.tryReadLock()
          stamp == 0L
        }) ()

        stamp
      },
      () => {
        var stamp = 0L

        while ({
          stamp = sl.tryReadLock(0L, DAYS)
          stamp == 0L
        }) ()

        stamp
      }
    )

    val stampedWriteUnlockers: List[Consumer[Long]] = List.of(
      makeConsumer[scala.Long]((stamp: Long) => sl.unlockWrite(stamp)),
      makeConsumer[scala.Long]((stamp: Long) => sl.unlock(stamp)),
      makeConsumer[scala.Long]((_: Long) => assertTrue(sl.tryUnlockWrite())),
      makeConsumer[scala.Long]((_: Long) => wl.unlock()),
      makeConsumer[scala.Long]((stamp: Long) =>
        sl.tryConvertToOptimisticRead(stamp)
      )
    )

    val stampedReadUnlockers: List[Consumer[Long]] = List.of(
      makeConsumer[scala.Long]((stamp: Long) => sl.unlockRead(stamp)),
      makeConsumer[scala.Long]((stamp: Long) => sl.unlock(stamp)),
      makeConsumer[scala.Long]((_: Long) => assertTrue(sl.tryUnlockRead())),
      makeConsumer[scala.Long]((_: Long) => rl.unlock()),
      makeConsumer[scala.Long]((stamp: Long) =>
        sl.tryConvertToOptimisticRead(stamp)
      )
    )

    val writer: Action = () => {
      // repeatedly acquires write lock
      val locker = chooseRandomly(stampedWriteLockers)
      val unlocker = chooseRandomly(stampedWriteUnlockers)
      while (!done.getAcquire()) {
        val stamp = locker.call()
        try {
          assertTrue(isWriteLockStamp(stamp))
          assertTrue(sl.isWriteLocked())
          assertFalse(isReadLockStamp(stamp))
          assertFalse(sl.isReadLocked())
          assertEquals(0, sl.getReadLockCount())
          assertTrue(sl.validate(stamp))
        } finally {
          unlocker.accept(stamp)
        }
      }
    }

    val reader: Action = () => {
      // repeatedly acquires read lock
      val locker = chooseRandomly(stampedReadLockers)
      val unlocker = chooseRandomly(stampedReadUnlockers)
      while (!done.getAcquire()) {
        val stamp = locker.call()
        try {
          assertFalse(isWriteLockStamp(stamp))
          assertFalse(sl.isWriteLocked())
          assertTrue(isReadLockStamp(stamp))
          assertTrue(sl.isReadLocked())
          assertTrue(sl.getReadLockCount() > 0)
          assertTrue(sl.validate(stamp))
        } finally {
          unlocker.accept(stamp)
        }
      }
    }

    for (_ <- nTasks until 0 by -1) {
      /* SN: List.of() works around a chooseRandomly() varargs overload
       *     which is missing in JSR166Test.scala.
       */
      val task = chooseRandomly(List.of(writer, reader))
      futures.add(CompletableFuture.runAsync(checkedRunnable(task)))
    }

    Thread.sleep(testDurationMillis)

    done.setRelease(true)
    for (i <- 0 until futures.size())
      futures.get(i).cancel(true)
  }
}
