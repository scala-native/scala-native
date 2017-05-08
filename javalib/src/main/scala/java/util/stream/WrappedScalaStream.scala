package java.util.stream

import java.util.Iterator
import java.util.function.{Function, Predicate}
import scala.collection.immutable.{Stream => SStream}

class WrappedScalaStream[T](private val underlying: SStream[T],
                            closeHandler: Option[Runnable])
    extends Stream[T] {
  override def close(): Unit         = closeHandler.foreach(_.run)
  override def isParallel(): Boolean = false
  override def iterator(): Iterator[T] =
    WrappedScalaStream.scala2javaIterator(underlying.iterator)
  override def parallel(): Stream[T]   = this
  override def sequential(): Stream[T] = this
  override def unordered: Stream[T]    = this
  override def onClose(closeHandler: Runnable): Stream[T] =
    new WrappedScalaStream(underlying, Some(closeHandler))

  override def flatMap[R](
      mapper: Function[_ >: T, _ <: Stream[_ <: R]]): Stream[R] = {
    val streams: Seq[Stream[R]] = underlying.map(v => mapper(v))
    new CompositeStream(streams, closeHandler)
  }

  override def filter(pred: Predicate[_ >: T]): Stream[T] =
    new WrappedScalaStream(underlying.filter(pred.test), closeHandler)
}

object WrappedScalaStream {
  class Builder[T] extends Stream.Builder[T] {
    val buffer                      = new scala.collection.mutable.ListBuffer[T]()
    override def accept(t: T): Unit = buffer += t
    override def build(): Stream[T] =
      new WrappedScalaStream(buffer.toStream, None)
  }

  def scala2javaIterator[T](
      it: scala.collection.Iterator[T]): java.util.Iterator[T] =
    new java.util.Iterator[T] {
      override def hasNext(): Boolean = it.hasNext
      override def next(): T          = it.next()
      override def remove(): Unit     = throw new UnsupportedOperationException()
    }
}

private final class CompositeStream[T](substreams: Seq[Stream[T]],
                                       closeHandler: Option[Runnable])
    extends Stream[T] {
  override def close(): Unit = {
    substreams.foreach(_.close())
    closeHandler.foreach(_.run())
  }
  override def isParallel(): Boolean = false
  override def iterator(): Iterator[T] =
    new Iterator[T] {
      private val its                         = substreams.iterator
      private var currentIt: Iterator[_ <: T] = EmptyIterator

      override def hasNext(): Boolean =
        if (currentIt.hasNext) true
        else if (its.hasNext) {
          currentIt = its.next().iterator
          hasNext()
        } else {
          false
        }

      override def next(): T =
        if (hasNext()) currentIt.next()
        else throw new NoSuchElementException()

      override def remove(): Unit =
        throw new UnsupportedOperationException()

    }

  override def parallel(): Stream[T]   = this
  override def sequential(): Stream[T] = this
  override def unordered: Stream[T]    = this
  override def onClose(closeHandler: Runnable): Stream[T] =
    new CompositeStream(substreams, Some(closeHandler))

  override def flatMap[R](
      mapper: Function[_ >: T, _ <: Stream[_ <: R]]): Stream[R] = {
    val newStreams: Seq[Stream[R]] =
      substreams.map((js: Stream[T]) => js.flatMap[R](mapper))
    new CompositeStream(newStreams, closeHandler)
  }

  override def filter(pred: Predicate[_ >: T]): Stream[T] = {
    val newStreams: Seq[Stream[T]] = substreams.map(s => s.filter(pred))
    new CompositeStream(newStreams, closeHandler)
  }
}

private object EmptyIterator extends Iterator[Nothing] {
  override def hasNext(): Boolean = false
  override def next(): Nothing    = throw new NoSuchElementException()
  override def remove(): Unit     = throw new UnsupportedOperationException()
}
