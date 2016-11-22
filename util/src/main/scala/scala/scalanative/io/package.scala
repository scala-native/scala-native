package scala.scalanative

import java.nio.ByteBuffer

package object io {
  // We allocate huge direct buffer, but due to virtual memory
  // semantics, only the used part is going to be actually provided
  // by the underlying OS.
  private val scratchBuffer = ByteBuffer.allocateDirect(128 * 1024 * 1024)

  def withScratchBuffer(f: ByteBuffer => Unit): Unit = {
    scratchBuffer.clear
    f(scratchBuffer)
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
