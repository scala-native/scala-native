package java.nio.channels.spi

import java.nio.channels.{Channel, InterruptibleChannel}

abstract class AbstractInterruptibleChannel protected ()
    extends Channel
    with InterruptibleChannel {

  private var closed: Boolean = false

  protected final def begin(): Unit =
    ()

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

  protected final def end(completed: Boolean): Unit =
    ()

  protected def implCloseChannel(): Unit

  final def isOpen(): Boolean =
    !closed

}
