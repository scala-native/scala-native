package scala.scalanative.nio.fs.zipfs

import java.nio.file.{DirectoryStream, Path}
import java.util.{ArrayList, Iterator => JIterator}

/** Eager `DirectoryStream` over a snapshot of child names. Cheap to build for
 *  the ZIP case because `ZipFile.entries()` is already in memory.
 */
private[zipfs] final class ZipDirectoryStream(
    children: ArrayList[Path]
) extends DirectoryStream[Path] {

  private var closed = false
  private var iteratorCalled = false

  override def iterator(): JIterator[Path] = {
    if (closed) throw new IllegalStateException("DirectoryStream is closed")
    if (iteratorCalled)
      throw new IllegalStateException("iterator() already called")
    iteratorCalled = true
    val under = children.iterator()
    new JIterator[Path] {
      override def hasNext(): Boolean = !closed && under.hasNext()
      override def next(): Path = {
        if (closed) throw new java.util.NoSuchElementException()
        under.next()
      }
      override def remove(): Unit =
        throw new UnsupportedOperationException()
    }
  }

  override def close(): Unit = { closed = true }
}
