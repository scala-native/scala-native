package java.nio.channels.spi

import java.io.IOException
import java.lang.impl.Interruptible
import java.nio.channels.{
  AsynchronousCloseException, Channel, ClosedByInterruptException,
  InterruptibleChannel
}

abstract class AbstractInterruptibleChannel protected ()
    extends Channel
    with InterruptibleChannel
    with Interruptible {

  @volatile private var closed: Boolean = false
  // The thread whose interrupt closed this channel.
  @volatile private var interruptedThread: Thread = _

  // Until end(), Thread.interrupt() closes this channel.
  protected final def begin(): Unit = beginInterruptible()

  /* close() uses a synchronized() block, not an AtomicBoolean because the
   * Java documentation for java.nio.channels.Channel#close describes a
   * simultaneous second invocation as blocking for the first.
   *
   * The synchronized block will also make the new value of 'closed' visible
   * to other threads, if any, when they read it.
   */

  final def close(): Unit = synchronized(closeBy(null))

  // `closed` before the marker, so end() never sees the marker while open.
  private def closeBy(interrupted: Thread): Unit =
    if (!closed) {
      closed = true
      if (interrupted != null) interruptedThread = interrupted
      implCloseChannel() // relies upon this caller being synchronized.
    }

  protected final def end(completed: Boolean): Unit = {
    endInterruptible()
    val thread = Thread.currentThread()
    if (interruptedThread eq thread) {
      interruptedThread = null
      throw new ClosedByInterruptException()
    }
    if (!completed && closed) throw new AsynchronousCloseException()
  }

  // The interrupted thread's end() then throws ClosedByInterruptException.
  private[java] final def onInterrupt(thread: Thread): Unit = synchronized {
    try closeBy(thread)
    catch { case _: IOException => () }
  }

  protected def implCloseChannel(): Unit

  final def isOpen(): Boolean =
    !closed

}
