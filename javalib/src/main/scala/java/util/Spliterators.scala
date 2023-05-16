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
 *    - spliterators specified by Java as late-binding may not be late-binding.
 *
 *    - spliterators never check for concurrent modification.
 *
 *    - A number of spliterator methods have JVM descriptions of what happens
 *      after iteration starts and one of certain methods, say, trySplit() is
 *      called. This implementation may not follow the JVM description. Even in
 *      Java, it is better to never trySplit() after having begun using a
 *      spliterator to iterate.
 *
 *  Also noted:
 *
 *    - Java documents that spliterators need not be thread-safe. This
 *      implementation follows that guidance.
 */

/* Developer Notes on evolving Spliterators
 *
 *   1) The limitations listed above should be corrected, or at least relaxed.
 *
 *   2) Performance, especially with spliterators which have a large,
 *      say million or US billion elements, should be measured. That
 *      will probably show that both execution time and memory usage
 *      need to be reduced.
 *
 *      For example, an individual development-only Test
 *      in SpliteratorsTrySplitTest showed an an un-optimized Scala Native
 *      executable having results matching the same Test on JVM but taking
 *      approximately 50% longer (a minute or so), possibly due to swapping
 *      caused by higher memory usage.
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

  /* This implementation of trySplit() is reverse engineered from the
   * default JVM algorithm for Iterable and Collection, without having
   * looked at the JVM code.
   *
   * It allows unit-tests to run in either JVM or Scala Native with the
   * matching results.
   *
   * The JVM algorithm switches from a first "count-them-out" iteration
   * algorithm to a reasonably efficient array based "bisection" algorithm.
   *
   * As advised by the Java documentation authors, sub-classes may benefit
   * from overriding this implementation with a more efficient one.
   *
   * Case in Point, JSR-166 implementations, which can be examined, tend to
   * use a different algorithm for batch sizing.
   */

  private final val ABSTRACT_TRYSPLIT_BATCH_SIZE = 1024

  private def getTrySplitBatchSize(multiplier: Long): Int = {
    /* To be discovered:
     *     JVM may have a lower maximum batch size.
     *
     *     JSR-166 LinkedBlockingQueue.scala specifies a MAX_BATCH of
     *     1 << 25 (33_554_432), well less than Integer.MAX_VALUE.
     */
    val computedSize = multiplier * ABSTRACT_TRYSPLIT_BATCH_SIZE
    Math.min(computedSize, Integer.MAX_VALUE).toInt
  }

  private def trySplitUsageRatioOK(used: Long, total: Long): Boolean = {
    /* This method concentrates the decision of whether trySplit() should take
     * the faster and easier route of passing its work buffer directly to
     * Spliterators or if it should reduce it to an exact size by copying.
     *
     * The issue is that the size of the allocated buffer grows after
     * repeated splits on the same spliterator. If a buffer is filled,
     * there is no need to copy. The opposite is also clear, if there is
     * one byte in a megabyte buffer, it makes sense to pay the Array allocation
     * and copy in order to free up the unused memory.
     *
     * Somewhere between the two scenarios is a sweet spot, which probably
     * varies by workload and available resources. Configuration is the
     * classical solution but it brings complexity. Auto-tuning of buffer size
     * or a different, perhaps capped, scale-up buffer size algorithm
     * is the other classical solution. Here that would mean no longer
     * matching the JVM size progression.
     *
     * This is a place to make it easier to tune heuristics. The current
     * ones are best guesses, without the benefit of configuration.
     *
     * Life is choices!
     */
    if (total < ABSTRACT_TRYSPLIT_BATCH_SIZE) true // avoid copy on first split
    else if (used == total) true
    else {
      val usageRatio = used / total
      usageRatio > 0.8 // Allow 20% wastage.
    }
  }

  abstract class AbstractDoubleSpliterator(
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator.OfDouble {
    private var remaining = est

    // JVM uses an arithmetic progression, incrementing factor with each split.
    private var trySplitsMultiplier = 1L // a Long to ease overflow checking

    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = remaining

    def trySplit(): Spliterator.OfDouble = {
      // Guard ArrayList(size) constructor by avoiding int overflow (to minus).
      val batchSize = getTrySplitBatchSize(trySplitsMultiplier)
      val buf = new Array[Double](batchSize)

      var count = 0

      val action: DoubleConsumer =
        (e: Double) => { buf(count) = e; count += 1 }

      while ((count < batchSize) && tryAdvance(action)) { /* side-effect */ }

      if (count == 0) null.asInstanceOf[Spliterator.OfDouble]
      else {
        remaining -= count
        trySplitsMultiplier += 1

        /* Passing an Array down allows the created spliterator to
         * traverse and split more efficiently.
         *
         * Pass accumulating buffer if small or if unused, wasted space is
         * tolerable. Otherwise, pay the cost of an allocation and
         * potentially large copy.
         */

        val batch =
          if (trySplitUsageRatioOK(count, batchSize)) buf
          else Arrays.copyOf(buf, count)

        Spliterators.spliterator(
          batch, // of AnyVal primitives
          0,
          count,
          additionalCharacteristics
        )
      }
    }
  }

  abstract class AbstractIntSpliterator(
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator.OfInt {
    private var remaining = est

    // JVM uses an arithmetic progression, incrementing factor with each split.
    private var trySplitsMultiplier = 1L // a Long to ease overflow checking

    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = remaining

    def trySplit(): Spliterator.OfInt = {
      // Guard ArrayList(size) constructor by avoiding int overflow (to minus).
      val batchSize = getTrySplitBatchSize(trySplitsMultiplier)
      val buf = new Array[Int](batchSize)

      var count = 0

      val action: IntConsumer =
        (e: Int) => { buf(count) = e; count += 1 }

      while ((count < batchSize) && tryAdvance(action)) { /* side-effect */ }

      if (count == 0) null.asInstanceOf[Spliterator.OfInt]
      else {
        remaining -= count
        trySplitsMultiplier += 1

        // See comment in corresponding place in AbstractDoubleSpliterator
        val batch =
          if (trySplitUsageRatioOK(count, batchSize)) buf
          else Arrays.copyOf(buf, count)

        Spliterators.spliterator(
          batch, // of AnyVal primitives
          0,
          count,
          additionalCharacteristics
        )
      }
    }
  }

  abstract class AbstractLongSpliterator(
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator.OfLong {
    private var remaining = est

    // JVM uses an arithmetic progression, incrementing factor with each split.
    private var trySplitsMultiplier = 1L // a Long to ease overflow checking

    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = remaining

    def trySplit(): Spliterator.OfLong = {
      // Guard ArrayList(size) constructor by avoiding int overflow (to minus).
      val batchSize = getTrySplitBatchSize(trySplitsMultiplier)
      val buf = new Array[Long](batchSize)

      var count = 0

      val action: LongConsumer =
        (e: Long) => { buf(count) = e; count += 1 }

      while ((count < batchSize) && tryAdvance(action)) { /* side-effect */ }

      if (count == 0) null.asInstanceOf[Spliterator.OfLong]
      else {
        remaining -= count
        trySplitsMultiplier += 1

        // See comment in corresponding place in AbstractDoubleSpliterator
        val batch =
          if (trySplitUsageRatioOK(count, batchSize)) buf
          else Arrays.copyOf(buf, count)

        Spliterators.spliterator(
          batch, // of AnyVal primitives
          0,
          count,
          additionalCharacteristics
        )
      }
    }
  }

  abstract class AbstractSpliterator[T](
      est: Long,
      additionalCharacteristics: Int
  ) extends Spliterator[T] {
    private var remaining = est

    // JVM uses an arithmetic progression, incrementing factor with each split.
    private var trySplitsMultiplier = 1L // a Long to ease overflow checking

    def characteristics(): Int = additionalCharacteristics

    def estimateSize(): Long = remaining

    def trySplit(): Spliterator[T] = {
      // Guard ArrayList(size) constructor by avoiding int overflow (to minus).
      val batchSize = getTrySplitBatchSize(trySplitsMultiplier)
      val buf = new Array[Object](batchSize)

      var count = 0

      /* Someday it would be nice to get rid of the cost of the runtime cast.
       * The current issue is that type T has no upper bound, such as
       * Object or AnyRef. With current declarations, an uninformed,
       * unwary, unfortunate, or malicious user could specify an AnyVal
       * for T, such as "new AbstractSplitertor[scala.Double]".
       *
       * The Scala Native compiler checks the signature of "action"
       * against the JDK, so that signature can not be modified.
       */
      val action: Consumer[_ >: T] =
        (e: T) => { buf(count) = e.asInstanceOf[Object]; count += 1 }

      while ((count < batchSize) && tryAdvance(action)) { /* side-effect */ }

      if (count == 0) null.asInstanceOf[Spliterator[T]]
      else {
        remaining -= count
        trySplitsMultiplier += 1

        // See comment in corresponding place in AbstractDoubleSpliterator
        val batch =
          if (trySplitUsageRatioOK(count, batchSize)) buf
          else Arrays.copyOf(buf, count)

        Spliterators.spliterator(
          batch, // of AnyRef Objects
          0,
          count,
          additionalCharacteristics
        )
      }
    }
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

      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        Objects.requireNonNull(action)
        if (!it.hasNext()) false
        else {
          action.accept(it.next())
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

    def trySplit(): Spliterator.OfDouble = {
      val hi = toIndex
      val lo = cursor
      val mid = (lo + hi) >>> 1
      if (lo >= mid) null
      else {
        cursor = mid
        new SpliteratorFromArrayDouble(
          array,
          lo,
          mid,
          additionalCharacteristics
        )
      }
    }

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

    def trySplit(): Spliterator.OfInt = {
      val hi = toIndex
      val lo = cursor
      val mid = (lo + hi) >>> 1
      if (lo >= mid) null
      else {
        cursor = mid
        new SpliteratorFromArrayInt(array, lo, mid, additionalCharacteristics)
      }
    }

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

  private final class SpliteratorFromArrayLong(
      array: Array[Long],
      fromIndex: Int,
      toIndex: Int,
      additionalCharacteristics: Int
  ) extends Spliterator.OfLong {
    // By contract, array == null & bounds have been checked by caller.

    // current index, modified on traverse/split
    private var cursor: Int = fromIndex

    def trySplit(): Spliterator.OfLong = {
      val hi = toIndex
      val lo = cursor
      val mid = (lo + hi) >>> 1
      if (lo >= mid) null
      else {
        cursor = mid
        new SpliteratorFromArrayLong(array, lo, mid, additionalCharacteristics)
      }
    }

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

    def trySplit(): Spliterator[T] = {
      val hi = toIndex
      val lo = cursor
      val mid = (lo + hi) >>> 1
      if (lo >= mid) null
      else {
        cursor = mid
        new SpliteratorFromArrayObject[T](
          array,
          lo,
          mid,
          additionalCharacteristics
        )
      }
    }

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

  def spliterator(
      iterator: PrimitiveIterator.OfInt,
      size: Long,
      characteristics: Int
  ): Spliterator.OfInt = {
    Objects.requireNonNull(iterator)

    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractIntSpliterator(size, harmonized) {
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

  def spliterator(
      iterator: PrimitiveIterator.OfLong,
      size: Long,
      characteristics: Int
  ): Spliterator.OfLong = {
    Objects.requireNonNull(iterator)

    val harmonized = maybeSetSizedCharacteristics(characteristics)
    new AbstractLongSpliterator(size, harmonized) {
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
