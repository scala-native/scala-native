package java.util

import java.util.function._

import Spliterator._

object PrimitiveIterator {
  trait OfDouble extends PrimitiveIterator[Double, DoubleConsumer] {
    override def forEachRemaining(action: Consumer[_ >: Double]): Unit = {
      Objects.requireNonNull(action)

      if (action.isInstanceOf[DoubleConsumer]) {
        forEachRemaining(action.asInstanceOf[DoubleConsumer])
      } else {

        while (hasNext())
          action.accept(next())
      }
    }

    def forEachRemaining(action: DoubleConsumer): Unit = {
      Objects.requireNonNull(action)
      while (hasNext())
        action.accept(nextDouble())
    }

    /* BEWARE: The Java Doc says that the result from next() should
     *          be boxed, i.e. new java.lang.Double(nextDouble).
     *          The Scala Native implementation of Iterator demands
     *          that this be an unboxed, primitive. The boxed result
     *          conflicts with Iterator.next() declaration.
     *
     *          Similar consideration exists for OfInt and OfLong.
     */
    def next() = nextDouble() // return should be boxed primitive but is not.

    // Throws NoSuchElementException if iterator has no more elements
    def nextDouble(): scala.Double // Abstract
  }

  trait OfInt extends PrimitiveIterator[Int, IntConsumer] {
    override def forEachRemaining(action: Consumer[_ >: Int]): Unit = {
      Objects.requireNonNull(action)

      if (action.isInstanceOf[IntConsumer]) {
        forEachRemaining(action.asInstanceOf[IntConsumer])
      } else {

        while (hasNext())
          action.accept(next())
      }
    }

    def forEachRemaining(action: IntConsumer): Unit = {
      Objects.requireNonNull(action)
      while (hasNext())
        action.accept(nextInt())
    }

    // See BEWARE above for OfDouble.next()
    def next() = nextInt() // return should be boxed primitive but is not.

    // Throws NoSuchElementException if iterator has no more elements
    def nextInt(): Int // Abstract
  }

  trait OfLong extends PrimitiveIterator[Long, LongConsumer] {
    override def forEachRemaining(action: Consumer[_ >: Long]): Unit = {
      Objects.requireNonNull(action)
      if (action.isInstanceOf[LongConsumer]) {
        forEachRemaining(action.asInstanceOf[LongConsumer])
      } else {

        while (hasNext())
          action.accept(next())
      }
    }

    def forEachRemaining(action: LongConsumer): Unit = {
      Objects.requireNonNull(action)
      while (hasNext())
        action.accept(nextLong())
    }

    // See BEWARE above for OfDouble.next()
    def next() = nextLong() // return should be boxed primitive but is not.

    // Throws NoSuchElementException if iterator has no more elements
    def nextLong(): Long // Abstract
  }
}

trait PrimitiveIterator[T, T_CONS] extends Iterator[T] {
  def forEachRemaining(action: T_CONS): Unit
}
