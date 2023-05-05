package java.util.stream

import java.util.function.Supplier
import java.util.Spliterator

object StreamSupport {

  /* Design Note:
   *   stream() and doubleStream() are implemented. intStream() and
   *   longStream() are not.  The first two need to mature before
   *   doubleStream() gets propagated into the latter two. No sense
   *   multiplying bugs beyond necessity, said William.
   */

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
    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def intStream(
      supplier: Supplier[Spliterator.OfInt],
      characteristics: Int,
      parallel: Boolean
  ): IntStream = {
    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def longStream(
      spliterator: Spliterator.OfLong,
      parallel: Boolean
  ): LongStream = {
    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def longStream(
      supplier: Supplier[Spliterator.OfLong],
      characteristics: Int,
      parallel: Boolean
  ): LongStream = {
    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def stream[T](
      spliterator: Spliterator[T],
      parallel: Boolean
  ): Stream[T] = {
    new ObjectStreamImpl[T](spliterator, parallel)
  }

  def stream[T](
      supplier: Supplier[Spliterator[T]],
      characteristics: Int,
      parallel: Boolean
  ): Stream[T] = {
    new ObjectStreamImpl[T](supplier, characteristics, parallel)
  }
}
