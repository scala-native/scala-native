/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.locks

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.{NativeThread, fromRawPtr}
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.unsafe.Ptr

object LockSupport {

  def getBlocker(t: Thread): Object = t.parkBlocker

  def park(): Unit = NativeThread.currentNativeThread.park()

  def park(blocker: Object): Unit = {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    try nativeThread.park()
    finally setBlocker(thread, null: Object)
  }

  def parkNanos(nanos: Long): Unit =
    NativeThread.currentNativeThread.parkNanos(nanos)

  def parkNanos(blocker: Object, nanos: Long): Unit = if (nanos > 0) {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    try nativeThread.parkNanos(nanos)
    finally setBlocker(thread, null: Object)
  }

  def parkUntil(deadline: Long): Unit =
    NativeThread.currentNativeThread.parkUntil(deadline)

  def parkUntil(blocker: Object, deadline: Long): Unit = {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    try nativeThread.parkUntil(deadline)
    finally setBlocker(thread, null: Object)
  }

  def unpark(thread: Thread): Unit = {
    if (thread != null) thread.platformCtx.unpark()
  }

  @alwaysinline private def parkBlockerRef(thread: Thread): Ptr[Object] =
    fromRawPtr(classFieldRawPtr(thread, "parkBlocker"))

  @alwaysinline private def setBlocker(
      thread: Thread,
      blocker: Object
  ): Unit = !parkBlockerRef(thread) = blocker

  @alwaysinline def setCurrentBlocker(blocker: Object): Unit =
    setBlocker(Thread.currentThread(), blocker)

  private[locks] def getThreadId(thread: Thread) = thread.threadId()
}
