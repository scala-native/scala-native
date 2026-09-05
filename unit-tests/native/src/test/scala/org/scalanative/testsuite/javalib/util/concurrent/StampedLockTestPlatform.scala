package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.locks.StampedLock

object StampedLockTestPlatform {
  def assumeStampStateInspectionMethods(): Unit = ()

  def isWriteLockStamp(stamp: Long): Boolean =
    StampedLock.isWriteLockStamp(stamp)

  def isReadLockStamp(stamp: Long): Boolean =
    StampedLock.isReadLockStamp(stamp)

  def isLockStamp(stamp: Long): Boolean =
    StampedLock.isLockStamp(stamp)

  def isOptimisticReadStamp(stamp: Long): Boolean =
    StampedLock.isOptimisticReadStamp(stamp)
}
