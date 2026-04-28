/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

@SerialVersionUID(-6001602636862214147L)
object StampedLock {
  private final val WriteBit = 1L << 62
  private final val ReadBit = 1L << 61
  private final val ModeMask = WriteBit | ReadBit
  private final val VersionMask = ~ModeMask

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
  private var readers = 0
  private var writer = false
  private var version = 1L

  private def nextVersion(): Unit = {
    version = (version + 1L) & VersionMask
    if (version == 0L) version = 1L
  }

  private def optimisticStamp(): Long = version
  private def readStamp(): Long = ReadBit | version
  private def writeStamp(): Long = WriteBit | version

  private def validVersion(stamp: Long): Boolean =
    stamp != 0L && versionOf(stamp) == version

  @throws[InterruptedException]
  private def awaitRemainingNanos(remaining: Long): Long = {
    val start = System.nanoTime()
    val millis = remaining / 1000000L
    val nanos = (remaining % 1000000L).toInt
    monitor.wait(millis, nanos)
    val elapsed = System.nanoTime() - start
    if (elapsed <= 0L) remaining else remaining - elapsed
  }

  def writeLock(): Long = {
    var interrupted = false
    monitor.synchronized {
      while (writer || readers != 0) {
        try monitor.wait()
        catch {
          case _: InterruptedException =>
            interrupted = true
        }
      }
      writer = true
      val stamp = writeStamp()
      if (interrupted) Thread.currentThread().interrupt()
      stamp
    }
  }

  def tryWriteLock(): Long =
    monitor.synchronized {
      if (writer || readers != 0) 0L
      else {
        writer = true
        writeStamp()
      }
    }

  @throws[InterruptedException]
  def tryWriteLock(time: Long, unit: TimeUnit): Long = {
    var nanos = unit.toNanos(time)
    if (Thread.interrupted()) throw new InterruptedException()
    monitor.synchronized {
      if (!writer && readers == 0) {
        writer = true
        return writeStamp()
      }
      if (nanos <= 0L) return 0L
      while (writer || readers != 0) {
        nanos = awaitRemainingNanos(nanos)
        if (nanos <= 0L) return 0L
      }
      writer = true
      writeStamp()
    }
  }

  @throws[InterruptedException]
  def writeLockInterruptibly(): Long = {
    if (Thread.interrupted()) throw new InterruptedException()
    monitor.synchronized {
      while (writer || readers != 0) monitor.wait()
      writer = true
      writeStamp()
    }
  }

  def readLock(): Long = {
    var interrupted = false
    monitor.synchronized {
      while (writer) {
        try monitor.wait()
        catch {
          case _: InterruptedException =>
            interrupted = true
        }
      }
      readers += 1
      val stamp = readStamp()
      if (interrupted) Thread.currentThread().interrupt()
      stamp
    }
  }

  def tryReadLock(): Long =
    monitor.synchronized {
      if (writer) 0L
      else {
        readers += 1
        readStamp()
      }
    }

  @throws[InterruptedException]
  def tryReadLock(time: Long, unit: TimeUnit): Long = {
    var nanos = unit.toNanos(time)
    if (Thread.interrupted()) throw new InterruptedException()
    monitor.synchronized {
      if (!writer) {
        readers += 1
        return readStamp()
      }
      if (nanos <= 0L) return 0L
      while (writer) {
        nanos = awaitRemainingNanos(nanos)
        if (nanos <= 0L) return 0L
      }
      readers += 1
      readStamp()
    }
  }

  @throws[InterruptedException]
  def readLockInterruptibly(): Long = {
    if (Thread.interrupted()) throw new InterruptedException()
    monitor.synchronized {
      while (writer) monitor.wait()
      readers += 1
      readStamp()
    }
  }

  def tryOptimisticRead(): Long =
    monitor.synchronized {
      if (writer) 0L else optimisticStamp()
    }

  def validate(stamp: Long): Boolean =
    monitor.synchronized {
      if (!validVersion(stamp)) false
      else if (isWriteLockStamp(stamp)) writer
      else !writer
    }

  def unlockWrite(stamp: Long): Unit =
    monitor.synchronized {
      if (!isWriteLockStamp(stamp) || !validVersion(stamp) || !writer)
        throw new IllegalMonitorStateException()
      writer = false
      nextVersion()
      monitor.notifyAll()
    }

  def unlockRead(stamp: Long): Unit =
    monitor.synchronized {
      if (!isReadLockStamp(stamp) || !validVersion(stamp) || readers == 0)
        throw new IllegalMonitorStateException()
      readers -= 1
      if (readers == 0) monitor.notifyAll()
    }

  def unlock(stamp: Long): Unit =
    if (isWriteLockStamp(stamp)) unlockWrite(stamp)
    else if (isReadLockStamp(stamp)) unlockRead(stamp)
    else throw new IllegalMonitorStateException()

  def tryConvertToWriteLock(stamp: Long): Long =
    monitor.synchronized {
      if (!validVersion(stamp)) 0L
      else if (isWriteLockStamp(stamp)) {
        if (writer) stamp else 0L
      } else if (isReadLockStamp(stamp)) {
        if (!writer && readers == 1) {
          readers = 0
          writer = true
          writeStamp()
        } else 0L
      } else {
        if (!writer && readers == 0) {
          writer = true
          writeStamp()
        } else 0L
      }
    }

  def tryConvertToReadLock(stamp: Long): Long =
    monitor.synchronized {
      if (!validVersion(stamp)) 0L
      else if (isReadLockStamp(stamp)) {
        if (!writer) stamp else 0L
      } else if (isWriteLockStamp(stamp)) {
        if (writer) {
          writer = false
          readers += 1
          monitor.notifyAll()
          readStamp()
        } else 0L
      } else {
        if (!writer) {
          readers += 1
          readStamp()
        } else 0L
      }
    }

  def tryConvertToOptimisticRead(stamp: Long): Long =
    monitor.synchronized {
      if (!validVersion(stamp)) 0L
      else if (isWriteLockStamp(stamp)) {
        if (!writer) 0L
        else {
          writer = false
          nextVersion()
          monitor.notifyAll()
          optimisticStamp()
        }
      } else if (isReadLockStamp(stamp)) {
        if (writer || readers == 0) 0L
        else {
          readers -= 1
          if (readers == 0) monitor.notifyAll()
          optimisticStamp()
        }
      } else if (!writer) stamp
      else 0L
    }

  def tryUnlockWrite(): Boolean =
    monitor.synchronized {
      if (!writer) false
      else {
        writer = false
        nextVersion()
        monitor.notifyAll()
        true
      }
    }

  def tryUnlockRead(): Boolean =
    monitor.synchronized {
      if (readers == 0) false
      else {
        readers -= 1
        if (readers == 0) monitor.notifyAll()
        true
      }
    }

  private def unstampedUnlockWrite(): Unit =
    if (!tryUnlockWrite()) throw new IllegalMonitorStateException()

  private def unstampedUnlockRead(): Unit =
    if (!tryUnlockRead()) throw new IllegalMonitorStateException()

  def isWriteLocked(): Boolean = monitor.synchronized(writer)

  def isReadLocked(): Boolean = monitor.synchronized(readers != 0)

  def getReadLockCount(): Int = monitor.synchronized(readers)

  override def toString(): String =
    monitor.synchronized {
      val state =
        if (writer) "Write-locked"
        else if (readers != 0) s"Read-locks:$readers"
        else "Unlocked"
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
