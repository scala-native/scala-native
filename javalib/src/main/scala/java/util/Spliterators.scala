package java.util

import java.util.function._

import Spliterator._

/** This is a basic, limit implementation of Spliterators. It is a basis for
 *  further Scala Native development, especially in the java.util.concurrent
 *  package.
 *
 *  It is most empathically __NOT__ intended for production use.
 *
 *  The limitations of the this implementation may not be as strong as they
 *  appear at first blush. Many/most classes which extend Spliterator (no s)
 *  supply more competent and efficient implementations.
 *
 *  The implementation of methods on Spliterators are, to current knowledge,
 *  robust. Many of these methods return spliterators. Those spliterators have
 *  some known limitations and may have others.
 *
 *  Future evolutions should, over time, remove these limitations:
 *
 *    - Their trySplit() methods never split, they always return null.
 *
 *    - spliterators specified by Java as late-binding may not be late-binding.
 *
 *    - spliterators never check for concurrent modification.
 *
 *    - A number of spliterator methods have JVM descriptions of what happens
 *      after iteration starts and one of certain methods, say, trySplit() is
 *      called. This implementation may not follow the JVM description. Even in
 *      Java, it is better to never trySplit() after having begun iterating over
 *      a spliterator.
 *
 *  Also noted:
 *
 *    - Java documents that spliterators need not be thread-safe. This
 *      implementation follows that guidance.
 */

/* Developer Notes on evolving trySplit()
 *
 *   1) A first evolution could implement trySplit() for Spliterators which
 *      are backed by arrays.
 *
 *   2) A second evolution could implement trySplit() for Spliterators which
 *      are backed by a Collection. Collections are SIZED. That should make
 *      working with the underlying iterator easier.
 *
 *   3) Later evolutions can address issues with un-SIZED iterators and
 *      other deficiencies.
 */

object Spliterators {

  private final val sizedCharacteristicsMask =
    Spliterator.SIZED | Spliterator.SUBSIZED

  private def isMaskSet(characteristics: Int, mask: Int): Boolean =
    (characteristics & mask) == mask

  private def maskOff(characteristics: Int, mask: Int): Int =
    characteristics & ~mask

  private def maskOn(characteristics: Int, mask: Int): Int =
    characteristics | mask

  private def maybeSetSizedCharacteristics(characteristics: Int): Int = {
    if (isMaskSet(characteristics, Spliterator.CONCURRENT)) characteristics
    else maskOn(characteristics, sizedCharacteristicsMask)
  }

  abstract class AbstractDoubleSpliterator(
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator.OfDouble {
    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = est

    def trySplit(): Spliterator.OfDouble =
      null.asInstanceOf[Spliterator.OfDouble]
  }

  abstract class AbstractIntSpliterator(
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator.OfInt {
    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = est

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator.OfInt =
      null.asInstanceOf[Spliterator.OfInt]
  }

  abstract class AbstractLongSpliterator(
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator.OfLong {
    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = est

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator.OfLong =
      null.asInstanceOf[Spliterator.OfLong]
  }

  abstract class AbstractSpliterator[T](
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator[T] {
    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = est

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator[T] = null.asInstanceOf[Spliterator[T]]
  }

  def emptyDoubleSpliterator(): Spliterator.OfDouble = {
    new AbstractDoubleSpliterator(0L, sizedCharacteristicsMask) {
      def tryAdvance(action: DoubleConsumer): Boolean = false
    }
  }

  def emptyIntSpliterator(): Spliterator.OfInt = {
    new AbstractIntSpliterator(0L, sizedCharacteristicsMask) {
      def tryAdvance(action: IntConsumer): Boolean = false
    }
  }

  def emptyLongSpliterator(): Spliterator.OfLong = {
    new AbstractLongSpliterator(0L, sizedCharacteristicsMask) {
      def tryAdvance(action: LongConsumer): Boolean = false
    }
  }

  def emptySpliterator[T](): Spliterator[T] = {
    new AbstractSpliterator[T](0, sizedCharacteristicsMask) {
      def tryAdvance(action: Consumer[_ >: T]): Boolean = false
    }
  }

  def iterator(
      spliterator: Spliterator.OfDouble
  ): PrimitiveIterator.OfDouble = {
    Objects.requireNonNull(spliterator)

    new PrimitiveIterator.OfDouble {
      // One element lookahead
      var cached: Option[scala.Double] = None

      def hasNext(): Boolean = {
        if (cached.nonEmpty) true
        else {
          spliterator.tryAdvance((e: Double) => (cached = Some(e)))
          cached.nonEmpty
        }
      }

      def nextDouble(): scala.Double = {
        if (!hasNext()) {
          throw new NoSuchElementException()
        } else {
          val nxt = cached.get
          cached = None
          nxt
        }
      }
    }
  }

  def iterator(spliterator: Spliterator.OfInt): PrimitiveIterator.OfInt = {
    Objects.requireNonNull(spliterator)

    new PrimitiveIterator.OfInt {
      // One element lookahead
      var cached: Option[scala.Int] = None

      def hasNext(): Boolean = {
        if (cached.nonEmpty) true
        else {
          spliterator.tryAdvance((e: Int) => (cached = Some(e)))
          cached.nonEmpty
        }
      }

      def nextInt(): scala.Int = {
        if (!hasNext()) {
          throw new NoSuchElementException()
        } else {
          val nxt = cached.get
          cached = None
          nxt
        }
      }
    }
  }

  def iterator(spliterator: Spliterator.OfLong): PrimitiveIterator.OfLong = {
    Objects.requireNonNull(spliterator)

    new PrimitiveIterator.OfLong {
      // One element lookahead
      var cached: Option[scala.Long] = None

      def hasNext(): Boolean = {
        if (cached.nonEmpty) true
        else {
          spliterator.tryAdvance((e: Long) => (cached = Some(e)))
          cached.nonEmpty
        }
      }

      def nextLong(): scala.Long = {
        if (!hasNext()) {
          throw new NoSuchElementException()
        } else {
          val nxt = cached.get
          cached = None
          nxt
        }
      }
    }
  }

  def iterator[T](spliterator: Spliterator[_ <: T]): Iterator[T] = {
    Objects.requireNonNull(spliterator)

    new Iterator[T] {
      // One element lookahead
      var cached: Option[T] = None

      def hasNext(): Boolean = {
        if (cached.nonEmpty) true
        else {
          spliterator.tryAdvance((e: T) => (cached = Some(e)))
          cached.nonEmpty
        }
      }

      def next(): T = {
        if (!hasNext()) {
          throw new NoSuchElementException()
        } else {
          val nxt = cached.get
          cached = None
          nxt
        }
      }
    }
  }

  def spliterator[T](
      c: Collection[_ <: T],
      characteristics: Int
  ): Spliterator[T] = {
    Objects.requireNonNull(c)

    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractSpliterator[T](c.size(), harmonized) {
      lazy val it = c.iterator()
      var remaining = c.size()

      override def estimateSize(): Long =
        if (isMaskSet(harmonized, sizedCharacteristicsMask)) remaining
        else Long.MaxValue

      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        Objects.requireNonNull(action)
        if (!it.hasNext()) false
        else {
          action.accept(it.next())
          remaining -= 1
          true
        }
      }
    }
  }

  private final class SpliteratorFromArrayDouble(
      array: Array[Double],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ) extends Spliterator.OfDouble {
    // By contract, array == null & bounds have been checked by caller.

    // current index, modified on traverse/split
    private var cursor: Int = fromIndex

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator.OfDouble =
      null.asInstanceOf[Spliterator.OfDouble]

    def tryAdvance(action: DoubleConsumer): Boolean = {
      Objects.requireNonNull(action)
      if (cursor == toIndex) false
      else {
        action.accept(array(cursor))
        cursor += 1
        true
      }
    }

    def estimateSize(): Long = { toIndex - cursor }

    def characteristics(): Int =
      maskOn(additionalCharacteristics, sizedCharacteristicsMask)
  }

  def spliterator(
      array: Array[Double],
      additionalCharacteristics: Int
  ): Spliterator.OfDouble = {
    Objects.requireNonNull(array)
    spliterator(array, 0, array.size, additionalCharacteristics)
  }

  private def checkArrayBounds(
      arraySize: Int,
      fromIndex: Int,
      toIndex: Int
  ): Unit = {
    if (fromIndex < 0)
      throw new ArrayIndexOutOfBoundsException(fromIndex)

    if (toIndex < fromIndex) {
      throw new ArrayIndexOutOfBoundsException(
        s"origin(${toIndex}) > fence(${fromIndex})"
      )
    }

    if (toIndex > arraySize) {
      throw new ArrayIndexOutOfBoundsException(
        s"Array index out of range: ${toIndex}"
      )
    }
  }

  def spliterator(
      array: Array[Double],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ): Spliterator.OfDouble = {
    Objects.requireNonNull(array)
    checkArrayBounds(array.length, fromIndex, toIndex)

    new SpliteratorFromArrayDouble(
      array,
      fromIndex,
      toIndex,
      additionalCharacteristics
    )
  }

  private final class SpliteratorFromArrayInt(
      array: Array[Int],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ) extends Spliterator.OfInt {
    // By contract, array == null & bounds have been checked by caller.

    // current index, modified on traverse/split
    private var cursor: Int = fromIndex

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator.OfInt =
      null.asInstanceOf[Spliterator.OfInt]

    def tryAdvance(action: IntConsumer): Boolean = {
      Objects.requireNonNull(action)
      if (cursor == toIndex) false
      else {
        action.accept(array(cursor))
        cursor += 1
        true
      }
    }

    def estimateSize(): Long = { toIndex - cursor }

    def characteristics(): Int =
      maskOn(additionalCharacteristics, sizedCharacteristicsMask)
  }

  def spliterator(
      array: Array[Int],
      additionalCharacteristics: Int
  ): Spliterator.OfInt = {
    Objects.requireNonNull(array)
    spliterator(array, 0, array.size, additionalCharacteristics)
  }

  def spliterator(
      array: Array[Int],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ): Spliterator.OfInt = {
    Objects.requireNonNull(array)
    checkArrayBounds(array.length, fromIndex, toIndex)

    new SpliteratorFromArrayInt(
      array,
      fromIndex,
      toIndex,
      additionalCharacteristics
    )
  }

  def spliterator[T](
      iterator: Iterator[_ <: T],
      size: Long,
      characteristics: Int
  ): Spliterator[T] = {
    Objects.requireNonNull(iterator)
    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractSpliterator[T](size, harmonized) {
      var remaining = size

      override def estimateSize(): Long =
        if (isMaskSet(harmonized, sizedCharacteristicsMask)) remaining
        else Long.MaxValue

      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.next())
          remaining -= 1
          true
        }
      }
    }
  }

  private final class SpliteratorFromArrayLong(
      array: Array[Long],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ) extends Spliterator.OfLong {
    // By contract, array == null & bounds have been checked by caller.

    // current index, modified on traverse/split
    private var cursor: Int = fromIndex

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator.OfLong =
      null.asInstanceOf[Spliterator.OfLong]

    def tryAdvance(action: LongConsumer): Boolean = {
      Objects.requireNonNull(action)
      if (cursor == toIndex) false
      else {
        action.accept(array(cursor))
        cursor += 1
        true
      }
    }

    def estimateSize(): Long = { toIndex - cursor }

    def characteristics(): Int =
      maskOn(additionalCharacteristics, sizedCharacteristicsMask)
  }

  def spliterator(
      array: Array[Long],
      additionalCharacteristics: Int
  ): Spliterator.OfLong = {
    Objects.requireNonNull(array)
    spliterator(array, 0, array.size, additionalCharacteristics)
  }

  def spliterator(
      array: Array[Long],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ): Spliterator.OfLong = {
    Objects.requireNonNull(array)
    checkArrayBounds(array.length, fromIndex, toIndex)

    new SpliteratorFromArrayLong(
      array,
      fromIndex,
      toIndex,
      additionalCharacteristics
    )
  }

  private final class SpliteratorFromArrayObject[T](
      array: Array[Object],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ) extends Spliterator[T] {
    // By contract, array == null & bounds have been checked by caller.

    // current index, modified on traverse/split
    private var cursor: Int = fromIndex

    // BEWARE: non-functional, never splits, always returns null
    def trySplit(): Spliterator[T] = null.asInstanceOf[Spliterator[T]]

    def tryAdvance(action: Consumer[_ >: T]): Boolean = {
      Objects.requireNonNull(action)
      if (cursor == toIndex) false
      else {
        action.accept(array(cursor).asInstanceOf[T])
        cursor += 1
        true
      }
    }

    def estimateSize(): Long = { toIndex - cursor }

    def characteristics(): Int = {
      additionalCharacteristics |
        sizedCharacteristicsMask
    }
  }

  def spliterator[T](
      array: Array[Object],
      additionalCharacteristics: Int
  ): Spliterator[T] = {
    Objects.requireNonNull(array)

    new SpliteratorFromArrayObject[T](
      array,
      0,
      array.size,
      additionalCharacteristics
    )
  }

  def spliterator[T](
      array: Array[Object],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ): Spliterator[T] = {
    Objects.requireNonNull(array)
    checkArrayBounds(array.length, fromIndex, toIndex)

    new SpliteratorFromArrayObject[T](
      array,
      fromIndex,
      toIndex,
      additionalCharacteristics
    )
  }

  def spliterator(
      iterator: PrimitiveIterator.OfDouble,
      size: Long,
      characteristics: Int
  ): Spliterator.OfDouble = {
    Objects.requireNonNull(iterator)

    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractDoubleSpliterator(size, harmonized) {
      var remaining = size

      override def estimateSize(): Long =
        if (isMaskSet(harmonized, sizedCharacteristicsMask)) remaining
        else Long.MaxValue

      def tryAdvance(action: DoubleConsumer): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.nextDouble())
          remaining -= 1
          true
        }
      }
    }
  }

  def spliterator(
      iterator: PrimitiveIterator.OfInt,
      size: Long,
      characteristics: Int
  ): Spliterator.OfInt = {
    Objects.requireNonNull(iterator)

    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractIntSpliterator(size, harmonized) {
      var remaining = size

      override def estimateSize(): Long =
        if (isMaskSet(harmonized, sizedCharacteristicsMask)) remaining
        else Long.MaxValue

      def tryAdvance(action: IntConsumer): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.nextInt())
          remaining -= 1
          true
        }
      }
    }
  }

  def spliterator(
      iterator: PrimitiveIterator.OfLong,
      size: Long,
      characteristics: Int
  ): Spliterator.OfLong = {
    Objects.requireNonNull(iterator)

    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractLongSpliterator(size, harmonized) {
      var remaining = size

      override def estimateSize(): Long =
        if (isMaskSet(harmonized, sizedCharacteristicsMask)) remaining
        else Long.MaxValue

      def tryAdvance(action: LongConsumer): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.nextLong())
          remaining -= 1
          true
        }
      }
    }
  }

  def spliteratorUnknownSize[T](
      iterator: Iterator[_ <: T],
      characteristics: Int
  ): Spliterator[T] = {
    Objects.requireNonNull(iterator)

    new AbstractSpliterator[T](
      Long.MaxValue,
      maskOff(characteristics, sizedCharacteristicsMask)
    ) {
      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.next())
          true
        }
      }
    }
  }

  def spliteratorUnknownSize(
      iterator: PrimitiveIterator.OfDouble,
      characteristics: Int
  ): Spliterator.OfDouble = {
    Objects.requireNonNull(iterator)

    new AbstractDoubleSpliterator(
      Long.MaxValue,
      maskOff(characteristics, sizedCharacteristicsMask)
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.nextDouble())
          true
        }
      }
    }
  }

  def spliteratorUnknownSize(
      iterator: PrimitiveIterator.OfInt,
      characteristics: Int
  ): Spliterator.OfInt = {
    Objects.requireNonNull(iterator)

    new AbstractIntSpliterator(
      Long.MaxValue,
      maskOff(characteristics, sizedCharacteristicsMask)
    ) {
      def tryAdvance(action: IntConsumer): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.nextInt())
          true
        }
      }
    }
  }

  def spliteratorUnknownSize(
      iterator: PrimitiveIterator.OfLong,
      characteristics: Int
  ): Spliterator.OfLong = {
    Objects.requireNonNull(iterator)

    new AbstractLongSpliterator(
      Long.MaxValue,
      maskOff(characteristics, sizedCharacteristicsMask)
    ) {
      def tryAdvance(action: LongConsumer): Boolean = {
        Objects.requireNonNull(action)
        if (!iterator.hasNext()) false
        else {
          action.accept(iterator.nextLong())
          true
        }
      }
    }
  }
}
