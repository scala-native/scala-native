package scala.scalanative
package io

import java.nio.ByteBuffer

final class ByteBufferPool {
  private var buffers: List[ByteBuffer] = Nil

  private def alloc(): ByteBuffer = {
    ByteBuffer.allocateDirect(32 * 1024 * 1024)
  }

  def reclaim(buffer: ByteBuffer): Unit = synchronized {
    buffers = buffer :: buffers
  }

  def claim(): ByteBuffer = synchronized {
    if (buffers.isEmpty)
      alloc
    else {
      val buffer = buffers.head
      buffers = buffers.tail
      buffer
    }
  }
}
