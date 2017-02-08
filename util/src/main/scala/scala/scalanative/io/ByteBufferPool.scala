package scala.scalanative
package io

import java.nio.ByteBuffer

final class ByteBufferPool {
  private def alloc   = ByteBuffer.allocateDirect(128 * 1024 * 1024)
  private var buffers = List.empty[ByteBuffer]

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
