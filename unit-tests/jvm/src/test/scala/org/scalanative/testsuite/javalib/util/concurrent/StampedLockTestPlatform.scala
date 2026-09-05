package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.reflect.InvocationTargetException
import java.util.concurrent.locks.StampedLock

import org.junit.Assume.assumeTrue

object StampedLockTestPlatform {
  private def booleanMethod(name: String) =
    try Some(classOf[StampedLock].getMethod(name, java.lang.Long.TYPE))
    catch {
      case _: NoSuchMethodException => None
    }

  private val isWriteLockStampMethod = booleanMethod("isWriteLockStamp")
  private val isReadLockStampMethod = booleanMethod("isReadLockStamp")
  private val isLockStampMethod = booleanMethod("isLockStamp")
  private val isOptimisticReadStampMethod =
    booleanMethod("isOptimisticReadStamp")

  private def allMethodsAvailable: Boolean =
    isWriteLockStampMethod.isDefined &&
      isReadLockStampMethod.isDefined &&
      isLockStampMethod.isDefined &&
      isOptimisticReadStampMethod.isDefined

  def assumeStampStateInspectionMethods(): Unit =
    assumeTrue(
      "StampedLock stamp state inspection methods require JDK 10+",
      allMethodsAvailable
    )

  private def invoke(method: java.lang.reflect.Method, stamp: Long): Boolean =
    try
      method
        .invoke(null, java.lang.Long.valueOf(stamp))
        .asInstanceOf[java.lang.Boolean]
        .booleanValue()
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }

  def isWriteLockStamp(stamp: Long): Boolean =
    invoke(isWriteLockStampMethod.get, stamp)

  def isReadLockStamp(stamp: Long): Boolean =
    invoke(isReadLockStampMethod.get, stamp)

  def isLockStamp(stamp: Long): Boolean =
    invoke(isLockStampMethod.get, stamp)

  def isOptimisticReadStamp(stamp: Long): Boolean =
    invoke(isOptimisticReadStampMethod.get, stamp)
}
