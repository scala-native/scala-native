package java.nio.channels

import java.nio.ByteBuffer

trait WritableByteChannel extends Channel {
  def write(src: ByteBuffer): Int
}
