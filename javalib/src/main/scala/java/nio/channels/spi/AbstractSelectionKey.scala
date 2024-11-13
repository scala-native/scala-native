package java.nio.channels.spi

import java.nio.channels.SelectionKey

// Ported from Apache Harmony
abstract class AbstractSelectionKey protected[spi] () extends SelectionKey {

  private[spi] var valid = true

  override final def isValid: Boolean = valid

  override final def cancel(): Unit = {
    if (valid) {
      valid = false
      selector.asInstanceOf[AbstractSelector].cancel(this)
    }
  }
}
