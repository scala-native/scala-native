/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicLongLong
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

@SerialVersionUID(-6001602636862214147L)
object StampedLock {
  private final val WriteBit = 1L << 62
  private final val ReadBit = 1L << 61
  private final val ModeMask = WriteBit | ReadBit
  private final val ReaderBits = 31
  private final val ReaderMask = (1L << ReaderBits) - 1L
  private final val VersionUnit = 1L << ReaderBits
  private final val VersionMask = ~(ModeMask | ReaderMask)
  private final val Origin = VersionUnit

  private def versionOf(stamp: Long): Long = stamp & VersionMask

  def isWriteLockStamp(stamp: Long): Boolean =
    stamp != 0L && (stamp & ModeMask) == WriteBit

  def isReadLockStamp(stamp: Long): Boolean =
    stamp != 0L && (stamp & ModeMask) == ReadBit

  def isLockStamp(stamp: Long): Boolean =
    isWriteLockStamp(stamp) || isReadLockStamp(stamp)

  def isOptimisticReadStamp(stamp: Long): Boolean =
    stamp != 0L && (stamp & ModeMask) == 0L
}

@SerialVersionUID(-6001602636862214147L)
class StampedLock extends Serializable {
  import StampedLock._

  private final val monitor = new Object
  @volatile private[locks] var state = Origin

  @alwaysinline
  private def stateRef: AtomicLongLong =
    new AtomicLongLong(fromRawPtr(Intrinsics.classFieldRawPtr(this, "state")))

  @alwaysinline
  private def loadState(): Long = stateRef.load()

  @alwaysinline
  private def casState(expect: Long, update: Long): Boolean =
    stateRef.compareExchangeStrong(expect, update)

  @alwaysinline
  private def readerCount(s: Long): Int = (s & ReaderMask).toInt

  @alwaysinline
  private def isWriteState(s: Long): Boolean = (s & WriteBit) != 0L

  @alwaysinline
  private def stateVersion(s: Long): Long = s & VersionMask

  @alwaysinline
  private def optimisticStamp(s: Long): Long = stateVersion(s)

  @alwaysinline
  private def readStamp(s: Long): Long = ReadBit | stateVersion(s)

  @alwaysinline
  private def writeStamp(s: Long): Long = WriteBit | stateVersion(s)

  @alwaysinline
  private def validVersion(stamp: Long, s: Long): Boolean =
    stamp != 0L && versionOf(stamp) == stateVersion(s)

  private def nextVersionState(s: Long): Long = {
    val next = (stateVersion(s) + VersionUnit) & VersionMask
    if (next == 0L) Origin else next
  }

  private def signalWaiters(): Unit =
    monitor.synchronized(monitor.notifyAll())

  @throws[InterruptedException]
  private def awaitNanos(remaining: Long): Unit = {
    val millis = remaining / 1000000L
    val nanos = (remaining % 1000000L).toInt
    monitor.wait(millis, nanos)
  }

  @tailrec
  private def tryAcquireWrite(): Long = {
    val s = loadState()
    if (isWriteState(s) || readerCount(s) != 0) 0L
    else {
      val next = stateVersion(s) | WriteBit
      if (casState(s, next)) writeStamp(next)
      else tryAcquireWrite()
    }
  }

  @tailrec
  private def tryAcquireRead(): Long = {
    val s = loadState()
    if (isWriteState(s)) 0L
    else {
      val readers = readerCount(s)
      if (readers == ReaderMask)
        throw new Error("Maximum read lock count exceeded")
      val next = s + 1L
      if (casState(s, next)) readStamp(next)
      else tryAcquireRead()
    }
  }

  def writeLock(): Long = {
    var stamp = tryAcquireWrite()
    if (stamp != 0L) return stamp

    var interrupted = false
    monitor.synchronized {
      while (stamp == 0L) {
        stamp = tryAcquireWrite()
        if (stamp != 0L) {
          if (interrupted) Thread.currentThread().interrupt()
          return stamp
        }
        try monitor.wait()
        catch {
          case _: InterruptedException =>
            interrupted = true
        }
      }
    }
    if (interrupted) Thread.currentThread().interrupt()
    stamp
  }

  def tryWriteLock(): Long = tryAcquireWrite()

  @throws[InterruptedException]
  def tryWriteLock(time: Long, unit: TimeUnit): Long = {
    var nanos = unit.toNanos(time)
    if (Thread.interrupted()) throw new InterruptedException()
    var stamp = tryAcquireWrite()
    if (stamp != 0L) return stamp
    monitor.synchronized {
      if (nanos <= 0L) return 0L
      val deadline = System.nanoTime() + nanos
      while (stamp == 0L) {
        stamp = tryAcquireWrite()
        if (stamp != 0L) return stamp
        if (nanos <= 0L) return 0L
        awaitNanos(nanos)
        nanos = deadline - System.nanoTime()
      }
      stamp
    }
  }

  @throws[InterruptedException]
  def writeLockInterruptibly(): Long = {
    if (Thread.interrupted()) throw new InterruptedException()
    var stamp = tryAcquireWrite()
    if (stamp != 0L) return stamp
    monitor.synchronized {
      while (stamp == 0L) {
        stamp = tryAcquireWrite()
        if (stamp != 0L) return stamp
        monitor.wait()
      }
      stamp
    }
  }

  def readLock(): Long = {
    var stamp = tryAcquireRead()
    if (stamp != 0L) return stamp

    var interrupted = false
    monitor.synchronized {
      while (stamp == 0L) {
        stamp = tryAcquireRead()
        if (stamp != 0L) {
          if (interrupted) Thread.currentThread().interrupt()
          return stamp
        }
        try monitor.wait()
        catch {
          case _: InterruptedException =>
            interrupted = true
        }
      }
    }
    if (interrupted) Thread.currentThread().interrupt()
    stamp
  }

  def tryReadLock(): Long = tryAcquireRead()

  @throws[InterruptedException]
  def tryReadLock(time: Long, unit: TimeUnit): Long = {
    var nanos = unit.toNanos(time)
    if (Thread.interrupted()) throw new InterruptedException()
    var stamp = tryAcquireRead()
    if (stamp != 0L) return stamp
    monitor.synchronized {
      if (nanos <= 0L) return 0L
      val deadline = System.nanoTime() + nanos
      while (stamp == 0L) {
        stamp = tryAcquireRead()
        if (stamp != 0L) return stamp
        if (nanos <= 0L) return 0L
        awaitNanos(nanos)
        nanos = deadline - System.nanoTime()
      }
      stamp
    }
  }

  @throws[InterruptedException]
  def readLockInterruptibly(): Long = {
    if (Thread.interrupted()) throw new InterruptedException()
    var stamp = tryAcquireRead()
    if (stamp != 0L) return stamp
    monitor.synchronized {
      while (stamp == 0L) {
        stamp = tryAcquireRead()
        if (stamp != 0L) return stamp
        monitor.wait()
      }
      stamp
    }
  }

  def tryOptimisticRead(): Long = {
    val s = loadState()
    if (isWriteState(s)) 0L else optimisticStamp(s)
  }

  def validate(stamp: Long): Boolean = {
    val s = loadState()
    if (!validVersion(stamp, s)) false
    else if (isWriteLockStamp(stamp)) isWriteState(s)
    else !isWriteState(s)
  }

  @tailrec
  private def unlockWriteLoop(stamp: Long): Unit = {
    val s = loadState()
    if (!isWriteLockStamp(stamp) || !validVersion(stamp, s) || !isWriteState(s))
      throw new IllegalMonitorStateException()
    if (casState(s, nextVersionState(s))) signalWaiters()
    else unlockWriteLoop(stamp)
  }

  def unlockWrite(stamp: Long): Unit = unlockWriteLoop(stamp)

  @tailrec
  private def unlockReadLoop(stamp: Long): Unit = {
    val s = loadState()
    val readers = readerCount(s)
    if (!isReadLockStamp(stamp) || !validVersion(stamp, s) ||
        isWriteState(s) || readers == 0)
      throw new IllegalMonitorStateException()
    val next = s - 1L
    if (casState(s, next)) {
      if (readers == 1) signalWaiters()
    } else unlockReadLoop(stamp)
  }

  def unlockRead(stamp: Long): Unit = unlockReadLoop(stamp)

  def unlock(stamp: Long): Unit =
    if (isWriteLockStamp(stamp)) unlockWrite(stamp)
    else if (isReadLockStamp(stamp)) unlockRead(stamp)
    else throw new IllegalMonitorStateException()

  @tailrec
  final def tryConvertToWriteLock(stamp: Long): Long = {
    val s = loadState()
    val readers = readerCount(s)
    if (!validVersion(stamp, s)) 0L
    else if (isWriteLockStamp(stamp)) {
      if (isWriteState(s)) stamp else 0L
    } else if (isReadLockStamp(stamp)) {
      if (!isWriteState(s) && readers == 1) {
        val next = stateVersion(s) | WriteBit
        if (casState(s, next)) writeStamp(next)
        else tryConvertToWriteLock(stamp)
      } else 0L
    } else {
      if (!isWriteState(s) && readers == 0) {
        val next = stateVersion(s) | WriteBit
        if (casState(s, next)) writeStamp(next)
        else tryConvertToWriteLock(stamp)
      } else 0L
    }
  }

  @tailrec
  final def tryConvertToReadLock(stamp: Long): Long = {
    val s = loadState()
    val readers = readerCount(s)
    if (!validVersion(stamp, s)) 0L
    else if (isWriteLockStamp(stamp)) {
      if (!isWriteState(s)) 0L
      else {
        val next = nextVersionState(s) | 1L
        if (casState(s, next)) {
          signalWaiters()
          readStamp(next)
        } else tryConvertToReadLock(stamp)
      }
    } else if (isReadLockStamp(stamp)) {
      if (!isWriteState(s) && readers != 0) stamp else 0L
    } else {
      if (isWriteState(s)) 0L
      else if (readers == ReaderMask)
        throw new Error("Maximum read lock count exceeded")
      else {
        val next = s + 1L
        if (casState(s, next)) readStamp(next)
        else tryConvertToReadLock(stamp)
      }
    }
  }

  @tailrec
  final def tryConvertToOptimisticRead(stamp: Long): Long = {
    val s = loadState()
    val readers = readerCount(s)
    if (!validVersion(stamp, s)) 0L
    else if (isWriteLockStamp(stamp)) {
      if (!isWriteState(s)) 0L
      else {
        val next = nextVersionState(s)
        if (casState(s, next)) {
          signalWaiters()
          optimisticStamp(next)
        } else tryConvertToOptimisticRead(stamp)
      }
    } else if (isReadLockStamp(stamp)) {
      if (isWriteState(s) || readers == 0) 0L
      else {
        val next = s - 1L
        if (casState(s, next)) {
          if (readers == 1) signalWaiters()
          optimisticStamp(next)
        } else tryConvertToOptimisticRead(stamp)
      }
    } else if (!isWriteState(s)) stamp
    else 0L
  }

  @tailrec
  final def tryUnlockWrite(): Boolean = {
    val s = loadState()
    if (!isWriteState(s)) false
    else if (casState(s, nextVersionState(s))) {
      signalWaiters()
      true
    } else tryUnlockWrite()
  }

  @tailrec
  final def tryUnlockRead(): Boolean = {
    val s = loadState()
    val readers = readerCount(s)
    if (isWriteState(s) || readers == 0) false
    else {
      val next = s - 1L
      if (casState(s, next)) {
        if (readers == 1) signalWaiters()
        true
      } else tryUnlockRead()
    }
  }

  private def unstampedUnlockWrite(): Unit =
    if (!tryUnlockWrite()) throw new IllegalMonitorStateException()

  private def unstampedUnlockRead(): Unit =
    if (!tryUnlockRead()) throw new IllegalMonitorStateException()

  def isWriteLocked(): Boolean = isWriteState(loadState())

  def isReadLocked(): Boolean = readerCount(loadState()) != 0

  def getReadLockCount(): Int = readerCount(loadState())

  override def toString(): String = {
    val s = loadState()
    val state =
      if (isWriteState(s)) "Write-locked"
      else {
        val readers = readerCount(s)
        if (readers != 0) s"Read-locks:$readers"
        else "Unlocked"
      }
    super.toString() + "[" + state + "]"
  }

  def asReadLock(): Lock = new ReadLockView

  def asWriteLock(): Lock = new WriteLockView

  def asReadWriteLock(): ReadWriteLock = new ReadWriteLockView

  private final class ReadLockView extends Lock {
    override def lock(): Unit = { readLock(); () }
    override def lockInterruptibly(): Unit = { readLockInterruptibly(); () }
    override def tryLock(): Boolean = tryReadLock() != 0L
    override def tryLock(time: Long, unit: TimeUnit): Boolean =
      tryReadLock(time, unit) != 0L
    override def unlock(): Unit = unstampedUnlockRead()
    override def newCondition(): Condition =
      throw new UnsupportedOperationException()
  }

  private final class WriteLockView extends Lock {
    override def lock(): Unit = { writeLock(); () }
    override def lockInterruptibly(): Unit = { writeLockInterruptibly(); () }
    override def tryLock(): Boolean = tryWriteLock() != 0L
    override def tryLock(time: Long, unit: TimeUnit): Boolean =
      tryWriteLock(time, unit) != 0L
    override def unlock(): Unit = unstampedUnlockWrite()
    override def newCondition(): Condition =
      throw new UnsupportedOperationException()
  }

  private final class ReadWriteLockView extends ReadWriteLock {
    override def readLock(): Lock = asReadLock()
    override def writeLock(): Lock = asWriteLock()
  }
}
