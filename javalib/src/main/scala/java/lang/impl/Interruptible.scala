package java.lang.impl

import scala.scalanative.libc.stdatomic.atomic_thread_fence
import scala.scalanative.libc.stdatomic.memory_order.memory_order_seq_cst
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/** A blocking operation that Thread.interrupt() can wake, such as a channel
 *  call. Calls do not nest.
 */
private[java] trait Interruptible {

  /** Wakes `thread`; runs on the interrupting thread. */
  private[java] def onInterrupt(thread: Thread): Unit

  /** Until endInterruptible(), Thread.interrupt() calls onInterrupt. A pending
   *  interrupt calls it now.
   */
  private[java] final def beginInterruptible(): Unit = {
    val thread = Thread.currentThread()
    thread.interruptible = this
    // Pairs with the fence in Thread.notifyInterruptible.
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_seq_cst)
    if (thread.isInterrupted())
      try interruptBy(thread)
      catch {
        case e: Throwable =>
          // The caller gets no end() for a failed begin.
          thread.interruptible = null
          throw e
      }
  }

  private[java] final def endInterruptible(): Unit =
    Thread.currentThread().interruptible = null

  private[java] final def interruptBy(thread: Thread): Unit =
    if ((thread.interruptible eq this) && thread.isInterrupted())
      onInterrupt(thread)
}
