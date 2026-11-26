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

  /* null while open; once closed, the thread whose interrupt closed it, or
   * `this`. One field, so end() sees the state and reason together.
   */
  @volatile private var closedBy: AnyRef = null

  // Until end(), Thread.interrupt() closes this channel.
  protected final def begin(): Unit = beginInterruptible()

  /* close() uses a synchronized() block, not an AtomicBoolean because the
   * Java documentation for java.nio.channels.Channel#close describes a
   * simultaneous second invocation as blocking for the first.
   *
   * The synchronized block will also make the new value of 'closedBy'
   * visible to other threads, if any, when they read it.
   */

  final def close(): Unit = synchronized(closeBy(null))

  private def closeBy(interrupted: Thread): Unit =
    if (closedBy == null) {
      closedBy = if (interrupted != null) interrupted else this
      implCloseChannel() // relies upon this caller being synchronized.
    }

  protected final def end(completed: Boolean): Unit = {
    endInterruptible()
    val by = closedBy
    if (by eq Thread.currentThread()) {
      closedBy = this // reported once
      throw new ClosedByInterruptException()
    }
    if (!completed && by != null) throw new AsynchronousCloseException()
  }

  // The interrupted thread's end() then throws ClosedByInterruptException.
  private[java] final def onInterrupt(thread: Thread): Unit = synchronized {
    try closeBy(thread)
    catch { case _: IOException => () }
  }

  protected def implCloseChannel(): Unit

  final def isOpen(): Boolean =
    closedBy == null

}
