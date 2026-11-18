package java.nio.file

import java.util.Iterator
import java.util.function.Predicate
import java.util.stream.Stream

class DirectoryStreamImpl[T](
    stream: Stream[T],
    filter: DirectoryStream.Filter[_ >: T]
) extends DirectoryStream[T] {
  private var iteratorCalled: Boolean = false
  private var closed: Boolean = false
  private val underlying = {
    val predicate = new Predicate[T] {
      override def test(t: T): Boolean = filter.accept(t)
    }
    stream.filter(predicate).iterator()
  }

  override def iterator(): Iterator[T] =
    if (closed || iteratorCalled)
      throw new IllegalStateException("Iterator already obtained")
    else {
      iteratorCalled = true
      new Iterator[T] {
        /* After close the underlying directory reader reports end of
         * stream; only an element already read ahead is still returned.
         */
        override def hasNext(): Boolean = underlying.hasNext()
        override def next(): T = underlying.next()
        override def remove(): Unit = throw new UnsupportedOperationException()
      }
    }

  override def close(): Unit = if (!closed) {
    closed = true
    stream.close()
  }
}
