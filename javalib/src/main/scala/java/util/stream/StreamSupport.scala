package java.util.stream

import java.util.function.Supplier
import java.util.Spliterator

object StreamSupport {

  def doubleStream(
      spliterator: Spliterator.OfDouble,
      parallel: Boolean
  ): DoubleStream = {
    new DoubleStreamImpl(spliterator, parallel)
  }

  def doubleStream(
      supplier: Supplier[Spliterator.OfDouble],
      characteristics: Int,
      parallel: Boolean
  ): DoubleStream = {
    new DoubleStreamImpl(supplier, characteristics, parallel)
  }

  def intStream(
      spliterator: Spliterator.OfInt,
      parallel: Boolean
  ): IntStream = {
    new IntStreamImpl(spliterator, parallel)
  }

  def intStream(
      supplier: Supplier[Spliterator.OfInt],
      characteristics: Int,
      parallel: Boolean
  ): IntStream = {
    new IntStreamImpl(supplier, characteristics, parallel)
  }

  def longStream(
      spliterator: Spliterator.OfLong,
      parallel: Boolean
  ): LongStream = {
    new LongStreamImpl(spliterator, parallel)
  }

  def longStream(
      supplier: Supplier[Spliterator.OfLong],
      characteristics: Int,
      parallel: Boolean
  ): LongStream = {
    new LongStreamImpl(supplier, characteristics, parallel)
  }

  def stream[T](
      spliterator: Spliterator[T],
      parallel: Boolean
  ): Stream[T] = {
    new StreamImpl[T](spliterator, parallel)
  }

  def stream[T](
      supplier: Supplier[Spliterator[T]],
      characteristics: Int,
      parallel: Boolean
  ): Stream[T] = {
    new StreamImpl[T](supplier, characteristics, parallel)
  }

}
