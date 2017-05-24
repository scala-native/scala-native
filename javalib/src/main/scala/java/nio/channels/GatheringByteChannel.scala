package java.nio.channels

import java.nio.ByteBuffer

trait GatheringByteChannel extends WritableByteChannel {
  def write(srcs: Array[ByteBuffer]): Long
  def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long
}
