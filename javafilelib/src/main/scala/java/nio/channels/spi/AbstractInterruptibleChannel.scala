package java.nio.channels.spi

import java.nio.channels.{Channel, InterruptibleChannel}

abstract class AbstractInterruptibleChannel protected ()
    extends Channel
    with InterruptibleChannel {

  private var closed: Boolean = false

  protected final def begin(): Unit =
    ()

  final def close(): Unit =
    if (!closed) {
      closed = true
      implCloseChannel()
    }

  protected final def end(completed: Boolean): Unit =
    ()

  protected def implCloseChannel(): Unit

  final def isOpen(): Boolean =
    !closed

}
