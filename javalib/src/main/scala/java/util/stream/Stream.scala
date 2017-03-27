package java.util.stream

import scala.collection.immutable.{Stream => SStream}

trait Stream[+T] extends BaseStream[T, Stream[T]] {
  def flatMap[R](mapper: Function[T, Stream[R]]): Stream[R]
}

object Stream {
  trait Builder[T] {
    def accept(t: T): Unit
    def add(t: T): Builder[T] = {
      accept(t)
      this
    }
    def build(): Stream[T]
  }

  def builder[T](): Builder[T] = new WrappedScalaStream.Builder[T]
  def empty[T](): Stream[T]    = new WrappedScalaStream(SStream.empty[T])
  def of[T](values: Array[T])  = new WrappedScalaStream(values.toStream)
}
