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

  def packageNameFromPath(path: Path): String = {
    @scala.annotation.tailrec
    def replace(
        b: StringBuilder,
        lastIndex: Int,
        before: String,
        after: String
    ): StringBuilder = {
      val index = b.indexOf(before, lastIndex)
      if (index == -1) b
      else {
        replace(b.replace(index, index + 1, after), index, before, after)
      }
    }

    val fileName = path.getFileName.toString.stripSuffix(".nir")
    val parent   = path.getParent
    val result: String = {
      if (parent == null) fileName
      else {
        val builder = new StringBuilder(parent.toString)
        // Delete starting `/` in case path comes from zip file
        if (builder.indexOf("/") == 0) builder.delete(0, 1)
        replace(builder, 0, "/", ".")
        builder.++=(".")
        builder.++=(fileName)
        builder.result()
      }
    }

    val ref = internedStrings.get(result)
    if (ref == null) {
      val ref = new java.lang.ref.WeakReference(result)
      internedStrings.put(result, ref)
      result
    } else {
      ref.get()
    }
  }

  import java.util.WeakHashMap
  import java.lang.ref.WeakReference
  private[scalanative] val internedStrings
      : WeakHashMap[String, WeakReference[String]] =
    new WeakHashMap[String, WeakReference[String]]()
}
