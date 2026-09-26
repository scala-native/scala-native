/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.{NativeThread, fromRawPtr}
import scala.scalanative.unsafe.Ptr

object LockSupport {

  def getBlocker(t: Thread): Object = t.parkBlocker

  def park(): Unit = Thread.currentThread() match {
    case vThread: VirtualThread => vThread.park()
    case thread                 => thread.platformCtx.nativeThread.park()
  }

  def park(blocker: Object): Unit = {
    val thread = Thread.currentThread()
    setBlocker(thread, blocker)
    try park()
    finally setBlocker(thread, null: Object)
  }

  def parkNanos(nanos: Long): Unit = Thread.currentThread() match {
    case vThread: VirtualThread => vThread.parkNanos(nanos)
    case thread => thread.platformCtx.nativeThread.parkNanos(nanos)
  }

  def parkNanos(blocker: Object, nanos: Long): Unit = if (nanos > 0) {
    val thread = Thread.currentThread()
    setBlocker(thread, blocker)
    try parkNanos(nanos)
    finally setBlocker(thread, null: Object)
  }

  def parkUntil(deadline: Long): Unit =
    Thread.currentThread() match {
      case vThread: VirtualThread =>
        val millis = deadline - System.currentTimeMillis()
        vThread.parkNanos(TimeUnit.MILLISECONDS.toNanos(millis))
      case thread => thread.platformCtx.nativeThread.parkUntil(deadline)
    }

  def parkUntil(blocker: Object, deadline: Long): Unit = {
    val thread = Thread.currentThread()
    setBlocker(thread, blocker)
    try parkUntil(deadline)
    finally setBlocker(thread, null: Object)
  }

  def unpark(thread: Thread): Unit = thread match {
    case null                   => ()
    case vThread: VirtualThread => vThread.unpark()
    case _                      =>
      thread.platformCtx.nativeThread match {
        case null         => ()
        case nativeThread => nativeThread.unpark()
      }
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
