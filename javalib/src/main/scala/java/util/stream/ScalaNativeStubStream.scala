package java.util.stream

import java.util.Iterator

private[stream] final class ScalaNativeStubStream[T](elements: Seq[T])
    extends Stream[T] {
  override def close(): Unit         = ()
  override def isParallel(): Boolean = false
  override def iterator(): Iterator[T] =
    new Iterator[T] {
      private val it              = elements.toIterator
      override def hasNext()      = it.hasNext
      override def next(): T      = it.next()
      override def remove(): Unit = throw new UnsupportedOperationException()
    }
  override def parallel(): ScalaNativeStubStream[T]   = this
  override def sequential(): ScalaNativeStubStream[T] = this
  // TODO:
  // override def spliterator(): Spliterator[T] = ???
  override def unordered: ScalaNativeStubStream[T] = this
}

private[stream] object ScalaNativeStubStream {
  class Builder[T] extends Stream.Builder[T] {
    val buffer                      = new scala.collection.mutable.ListBuffer[T]()
    override def accept(t: T): Unit = buffer += t
    override def build(): Stream[T] = new ScalaNativeStubStream(buffer)
  }
}
