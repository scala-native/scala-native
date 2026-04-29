package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.locks.StampedLock

object StampedLockTestPlatform {
  private final val WriteBit = 1L << 62
  private final val ReadBit = 1L << 61
  private final val ModeMask = WriteBit | ReadBit

  def assumeStampStateInspectionMethods(): Unit = ()

  def isWriteLockStamp(stamp: Long): Boolean =
    stamp != 0L && (stamp & ModeMask) == WriteBit

  def isReadLockStamp(stamp: Long): Boolean =
    stamp != 0L && (stamp & ModeMask) == ReadBit

  def isLockStamp(stamp: Long): Boolean =
    isWriteLockStamp(stamp) || isReadLockStamp(stamp)

  def isOptimisticReadStamp(stamp: Long): Boolean =
    stamp != 0L && (stamp & ModeMask) == 0L
}
