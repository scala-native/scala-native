package java.nio.channels.spi

import java.io.IOException
import java.nio.channels.{
  AsynchronousCloseException, Channel, ClosedByInterruptException,
  InterruptibleChannel
}

import scala.scalanative.libc.stdatomic.atomic_thread_fence
import scala.scalanative.libc.stdatomic.memory_order.memory_order_seq_cst
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

abstract class AbstractInterruptibleChannel protected ()
    extends Channel
    with InterruptibleChannel {

  @volatile private var closed: Boolean = false
  // The thread whose interrupt closed this channel.
  @volatile private var interruptedThread: Thread = _

  /* Until end(), Thread.interrupt() closes this channel; a pending interrupt
   * closes it now. Calls do not nest.
   */
  protected final def begin(): Unit = {
    val thread = Thread.currentThread()
    thread.nioBlocker = this
    // Pairs with the fence in Thread.interruptNioBlocker.
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_seq_cst)
    if (thread.isInterrupted()) interruptBy(thread)
  }

  /* close() uses a synchronized() block, not an AtomicBoolean because the
   * Java documentation for java.nio.channels.Channel#close describes a
   * simultaneous second invocation as blocking for the first.
   *
   * The synchronized block will also make the new value of 'closed' visible
   * to other threads, if any, when they read it.
   */

  final def close(): Unit = synchronized {
    if (!closed) {
      closed = true
      implCloseChannel() // relies upon this caller being synchronized.
    }
  }

  protected final def end(completed: Boolean): Unit = {
    val thread = Thread.currentThread()
    thread.nioBlocker = null
    if (interruptedThread eq thread) {
      interruptedThread = null
      throw new ClosedByInterruptException()
    }
    if (!completed && closed) throw new AsynchronousCloseException()
  }

  // Closes this channel for an interrupted thread still between begin() and
  // end(); its end() then throws ClosedByInterruptException.
  private[java] final def interruptBy(thread: Thread): Unit = synchronized {
    if (!closed && (thread.nioBlocker eq this)) {
      interruptedThread = thread
      try close()
      catch { case _: IOException => () }
    }
  }

  protected def implCloseChannel(): Unit

  final def isOpen(): Boolean =
    !closed

}
