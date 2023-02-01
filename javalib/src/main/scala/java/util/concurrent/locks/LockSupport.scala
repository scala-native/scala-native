/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package java.util.concurrent.locks

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.NativeThread

object LockSupport {

  /** Returns the blocker object supplied to the most recent invocation of a
   *  park method that has not yet unblocked, or null if not blocked. The value
   *  returned is just a momentary snapshot -- the thread may have since
   *  unblocked or blocked on a different blocker object.
   *
   *  @param t
   *    the thread
   *  @return
   *    the blocker
   *  @throws java.lang.NullPointerException
   *    if argument is null
   *  @since 1.6
   */
  def getBlocker(t: Thread): Object = t.parkBlocker.get()

  /** Disables the current thread for thread scheduling purposes unless the
   *  permit is available.
   *
   *  <p>If the permit is available then it is consumed and the call returns
   *  immediately; otherwise the current thread becomes disabled for thread
   *  scheduling purposes and lies dormant until one of three things happens:
   *
   *  <ul>
   *
   *  <li>Some other thread invokes {@link #unpark unpark} with the current
   *  thread as the target; or
   *
   *  <li>Some other thread [[java.lang.Thread.interrupt interrupts]] the
   *  current thread; or
   *
   *  <li>The call spuriously (that is, for no reason) returns. </ul>
   *
   *  <p>This method does <em>not</em> report which of these caused the method
   *  to return. Callers should re-check the conditions which caused the thread
   *  to park in the first place. Callers may also determine, for example, the
   *  interrupt status of the thread upon return.
   */
  def park(): Unit = NativeThread.currentNativeThread.park()

  /** Disables the current thread for thread scheduling purposes, until the
   *  specified deadline, unless the permit is available.
   *
   *  <p>If the permit is available then it is consumed and the call returns
   *  immediately; otherwise the current thread becomes disabled for thread
   *  scheduling purposes and lies dormant until one of four things happens:
   *
   *  <ul> <li>Some other thread invokes {@link #unpark unpark} with the current
   *  thread as the target; or
   *
   *  <li>Some other thread [[java.lang.Thread.interrupt interrupts]] the current
   *  thread; or
   *
   *  <li>The specified deadline passes; or
   *
   *  <li>The call spuriously (that is, for no reason) returns. </ul>
   *
   *  <p>This method does <em>not</em> report which of these caused the method
   *  to return. Callers should re-check the conditions which caused the thread
   *  to park in the first place. Callers may also determine, for example, the
   *  interrupt status of the thread, or the current time upon return.
   *
   *  @param blocker
   *    the synchronization object responsible for this thread parking
   *  @param deadline
   *    the absolute time, in milliseconds from the Epoch, to wait until
   *  @since 1.6
   */
  def park(blocker: Object): Unit = {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    nativeThread.park()
    setBlocker(thread, null: Object)
  }

  /** Disables the current thread for thread scheduling purposes, for up to the
   *  specified waiting time, unless the permit is available.
   *
   *  <p>If the permit is available then it is consumed and the call returns
   *  immediately; otherwise the current thread becomes disabled for thread
   *  scheduling purposes and lies dormant until one of four things happens:
   *
   *  <ul> <li>Some other thread invokes {@link #unpark unpark} with the current
   *  thread as the target; or
   *
   *  <li>Some other thread [[java.lang.Thread.interrupt interrupts]] the
   *  current thread; or
   *
   *  <li>The specified waiting time elapses; or
   *
   *  <li>The call spuriously (that is, for no reason) returns. </ul>
   *
   *  <p>This method does <em>not</em> report which of these caused the method
   *  to return. Callers should re-check the conditions which caused the thread
   *  to park in the first place. Callers may also determine, for example, the
   *  interrupt status of the thread, or the elapsed time upon return.
   *
   *  @param nanos
   *    the maximum number of nanoseconds to wait
   */
  def parkNanos(nanos: Long): Unit =
    NativeThread.currentNativeThread.parkNanos(nanos)

  def parkNanos(blocker: Object, nanos: Long): Unit = if (nanos > 0) {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    nativeThread.parkNanos(nanos)
    setBlocker(thread, null: Object)
  }

  /** Disables the current thread for thread scheduling purposes, until the
   *  specified deadline, unless the permit is available.
   *
   *  <p>If the permit is available then it is consumed and the call returns
   *  immediately; otherwise the current thread becomes disabled for thread
   *  scheduling purposes and lies dormant until one of four things happens:
   *
   *  <ul> <li>Some other thread invokes {@link #unpark unpark} with the current
   *  thread as the target; or
   *
   *  <li>Some other thread [[java.lang.Thread.interrupt interrupts]] the
   *  current thread; or
   *
   *  <li>The specified deadline passes; or
   *
   *  <li>The call spuriously (that is, for no reason) returns. </ul>
   *
   *  <p>This method does <em>not</em> report which of these caused the method
   *  to return. Callers should re-check the conditions which caused the thread
   *  to park in the first place. Callers may also determine, for example, the
   *  interrupt status of the thread, or the current time upon return.
   *
   *  @param deadline
   *    the absolute time, in milliseconds from the Epoch, to wait until
   */
  def parkUntil(deadline: Long): Unit =
    NativeThread.currentNativeThread.parkUntil(deadline)

  def parkUntil(blocker: Object, deadline: Long): Unit = {
    val nativeThread = NativeThread.currentNativeThread
    val thread = nativeThread.thread
    setBlocker(thread, blocker)
    nativeThread.parkUntil(deadline)
    setBlocker(thread, null: Object)
  }

  /** Makes available the permit for the given thread, if it was not already
   *  available. If the thread was blocked on {@code park} then it will unblock.
   *  Otherwise, its next call to {@code park} is guaranteed not to block. This
   *  operation is not guaranteed to have any effect at all if the given thread
   *  has not been started.
   *
   *  @param thread
   *    the thread to unpark, or {@code null}, in which case this operation has
   *    no effect
   */
  def unpark(thread: Thread): Unit =
    if (thread != null && thread.nativeThread != null) {
      thread.nativeThread.unpark()
    }

  @alwaysinline private def setBlocker(
      thread: Thread,
      blocker: Object
  ): Unit = {
    // TODO: Java atomics: use setOpaque
    thread.parkBlocker.set(blocker)
  }

  /** Sets the object to be returned by invocations of [[getBlocker]] for the
   *  current thread. This method may be used before invoking the no-argument
   *  version of [[park()*]] from non-public objects, allowing more helpful
   *  diagnostics, or retaining compatibility with previous implementations of
   *  blocking methods. Previous values of the blocker are not automatically
   *  restored after blocking. To obtain the effects of {{{park(b)}}}, use
   *  {{{
   *  setCurrentBlocker(b)
   *  park()
   *  setCurrentBlocker(null)
   *  }}}
   *
   *  @param blocker
   *    the blocker object
   *  @since 14
   */
  @alwaysinline def setCurrentBlocker(blocker: Object): Unit =
    // TODO: Java atomics: use setOpaque
    Thread.currentThread().parkBlocker.set(blocker)

  private[locks] def getThreadId(thread: Thread) = thread.threadId
}
