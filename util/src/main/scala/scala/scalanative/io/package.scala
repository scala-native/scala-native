package scala.scalanative

import java.nio.ByteBuffer

package object io {
  // We allocate a pool of direct buffers. Due to virtual memory
  // semantics, only the used part is going to be actually provided
  // by the underlying OS.
  private val pool = new ByteBufferPool

  def withScratchBuffer(f: ByteBuffer => Unit): Unit = {
    val buffer = pool.claim()
    buffer.clear
    try f(buffer)
    finally pool.reclaim(buffer)
  }

  def cloneBuffer(original: ByteBuffer): ByteBuffer = {
    val clone = ByteBuffer.allocate(original.capacity())
    original.rewind()
    clone.put(original)
    original.rewind()
    clone.flip()
    clone
  }
}
