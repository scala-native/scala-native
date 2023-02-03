/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.locks

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.NativeThread

object LockSupport {

  def getBlocker(t: Thread): Object = t.parkBlocker.get()

  def park(): Unit = NativeThread.currentNativeThread.park()

  def park(blocker: Object): Unit = {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    nativeThread.park()
    setBlocker(thread, null: Object)
  }

  def parkNanos(nanos: Long): Unit =
    NativeThread.currentNativeThread.parkNanos(nanos)

  def parkNanos(blocker: Object, nanos: Long): Unit = if (nanos > 0) {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    nativeThread.parkNanos(nanos)
    setBlocker(thread, null: Object)
  }

  def parkUntil(deadline: Long): Unit =
    NativeThread.currentNativeThread.parkUntil(deadline)

  def parkUntil(blocker: Object, deadline: Long): Unit = {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    nativeThread.parkUntil(deadline)
    setBlocker(thread, null: Object)
  }

  def unpark(thread: Thread): Unit =
    if (thread != null && thread.nativeThread != null) {
      thread.nativeThread.unpark()
    }

  @alwaysinline private def setBlocker(
      thread: Thread,
      blocker: Object
  ): Unit = {
    thread.parkBlocker.setOpaque(blocker)
  }

  @alwaysinline def setCurrentBlocker(blocker: Object): Unit =
    Thread.currentThread().parkBlocker.setOpaque(blocker)

  private[locks] def getThreadId(thread: Thread) = thread.threadId
}
