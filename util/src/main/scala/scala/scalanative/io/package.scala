package scala.scalanative

import java.nio.ByteBuffer
import java.nio.file.Path
import scala.collection.JavaConverters._

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

  def packageNameFromPath(path: Path, extension: String): String = {
    val fileName = path.getFileName.toString.replace(extension, "")

    Option(path.getParent) match {
      case Some(parent) => parent.resolve(fileName).asScala.mkString(".")
      case None         => fileName
    }
  }
}
