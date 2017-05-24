package java.nio.channels

import java.io.Closeable

trait Channel extends Closeable {
  def close(): Unit
  def isOpen(): Boolean
}
