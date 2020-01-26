package java.nio.channels

import java.nio.ByteBuffer

trait ScatteringByteChannel extends ReadableByteChannel {
  def read(dsts: Array[ByteBuffer]): Long
  def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long
}
