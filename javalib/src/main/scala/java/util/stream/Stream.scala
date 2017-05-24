package java.util.stream

import java.util.function.{Function, Predicate}

import scala.collection.immutable.{Stream => SStream}

trait Stream[+T] extends BaseStream[T, Stream[T]] {
  def flatMap[R](mapper: Function[_ >: T, _ <: Stream[_ <: R]]): Stream[R]
  def filter(pred: Predicate[_ >: T]): Stream[T]
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
  def empty[T](): Stream[T]    = new WrappedScalaStream(SStream.empty[T], None)
  def of[T](values: Array[AnyRef]): Stream[T] =
    new WrappedScalaStream(values.asInstanceOf[Array[T]].toStream, None)
}
