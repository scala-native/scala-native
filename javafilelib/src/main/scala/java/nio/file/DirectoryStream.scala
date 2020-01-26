package java.nio.file

import java.io.Closeable
import java.lang.Iterable
import java.util.Iterator

trait DirectoryStream[T] extends Closeable with Iterable[T] {
  def iterator(): Iterator[T]
}

object DirectoryStream {
  trait Filter[T] {
    def accept(entry: T): Boolean
  }
}
