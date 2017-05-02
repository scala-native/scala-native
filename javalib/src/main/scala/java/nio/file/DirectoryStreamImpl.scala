package java.nio.file

import scala.collection.{Iterator => SIterator}
import java.util.Iterator

private[file] class DirectoryStreamImpl[T](
    stream: Stream[T],
    filter: DirectoryStream.Filter[_ >: T])
    extends DirectoryStream[T] {
  private var iteratorCalled: Boolean = false
  private var closed: Boolean         = false
  private val underlying              = stream.filter(filter.accept).iterator

  override def iterator(): Iterator[T] =
    if (closed || iteratorCalled)
      throw new IllegalStateException("Iterator already obtained")
    else {
      iteratorCalled = true
      new Iterator[T] {
        override def hasNext(): Boolean = !closed && underlying.hasNext
        override def next(): T =
          if (!hasNext()) throw new NoSuchElementException()
          else underlying.next
        override def remove(): Unit = throw new UnsupportedOperationException()
      }
    }

  override def close(): Unit = closed = true
}
