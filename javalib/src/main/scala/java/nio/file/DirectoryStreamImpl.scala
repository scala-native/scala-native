package java.nio.file

import java.io.UncheckedIOException
import java.util.stream.Stream
import java.util.{Iterator, NoSuchElementException}

class DirectoryStreamImpl[T](
    stream: Stream[T],
    filter: DirectoryStream.Filter[_ >: T]
) extends DirectoryStream[T] {
  private var iteratorCalled: Boolean = false
  private var closed: Boolean = false
  private val listing = stream.iterator()

  override def iterator(): Iterator[T] =
    if (closed || iteratorCalled)
      throw new IllegalStateException("Iterator already obtained")
    else {
      iteratorCalled = true
      new Iterator[T] {
        private var pending: T = _
        private var hasPending = false

        /* After close the underlying directory reader reports end of
         * stream; only an element already read ahead is still returned.
         * Exceptions from the filter propagate unchanged.
         */
        override def hasNext(): Boolean = {
          while (!hasPending && listingHasNext()) {
            val entry = listingNext()
            if (filter.accept(entry)) {
              pending = entry
              hasPending = true
            }
          }
          hasPending
        }

        override def next(): T =
          if (!hasNext()) throw new NoSuchElementException()
          else {
            hasPending = false
            val entry = pending
            pending = null.asInstanceOf[T]
            entry
          }

        override def remove(): Unit = throw new UnsupportedOperationException()
      }
    }

  private def listingHasNext(): Boolean =
    try listing.hasNext()
    catch {
      case e: UncheckedIOException =>
        throw new DirectoryIteratorException(e.getCause())
    }

  private def listingNext(): T =
    try listing.next()
    catch {
      case e: UncheckedIOException =>
        throw new DirectoryIteratorException(e.getCause())
    }

  override def close(): Unit = if (!closed) {
    closed = true
    stream.close()
  }
}
