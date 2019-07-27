package java.nio.channels

import java.nio.ByteBuffer

trait SeekableByteChannel extends ByteChannel {
  override def read(dst: ByteBuffer): Int
  override def write(src: ByteBuffer): Int
  def position(): Long
  def size(): Long
  def truncate(size: Long): SeekableByteChannel
}
