package java.nio.channels

import java.nio.ByteBuffer

trait ReadableByteChannel extends Channel {
  def read(dst: ByteBuffer): Int
}
