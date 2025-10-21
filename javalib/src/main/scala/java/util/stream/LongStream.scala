package java.util.stream

import java.util._
import java.util.function._
import java.{lang => jl}

/* Design Note:
 *
 * LongStream extends BaseStream[jl.Long, LongStream]
 * in correspondence to the documentation & usage of Spliterator.Of*
 * and PrimitiveIterator.Of*. That is, the first type is a Java container.
 */

trait LongStream extends BaseStream[jl.Long, LongStream] {

  def allMatch(pred: LongPredicate): Boolean

  def anyMatch(pred: LongPredicate): Boolean

  def asDoubleStream(): DoubleStream

  def average(): OptionalDouble

  def boxed(): Stream[jl.Long]

  def collect[R](
      supplier: Supplier[R],
      accumulator: ObjLongConsumer[R],
      combiner: BiConsumer[R, R]
  ): R

  def count(): scala.Long

  def distinct(): LongStream

  // Since: Java 9
  def dropWhile(pred: LongPredicate): LongStream = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for dropWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractLongSpliterator(
      Long.MaxValue,
      unSized
    ) {

      override def trySplit(): Spliterator.OfLong =
        null.asInstanceOf[Spliterator.OfLong]

      var doneDropping = false

      def tryAdvance(action: LongConsumer): Boolean = {
        if (doneDropping) {
          spliter.tryAdvance(e => action.accept(e))
        } else {
          var doneLooping = false
          while (!doneLooping) {
            val advanced =
              spliter.tryAdvance(e => {
                if (!pred.test(e)) {
                  action.accept(e)
                  doneDropping = true
                  doneLooping = true
                }

              })
            if (!advanced)
              doneLooping = true
          }
          doneDropping // true iff some element was accepted
        }
      }
    }

    new LongStreamImpl(spl, parallel = false, parent = this)
  }

  def filter(pred: LongPredicate): LongStream

  def findAny(): OptionalLong

  def findFirst(): OptionalLong

  def flatMap(mapper: LongFunction[_ <: LongStream]): LongStream

  def forEach(action: LongConsumer): Unit

  def forEachOrdered(action: LongConsumer): Unit

  def limit(maxSize: scala.Long): LongStream

  def map(mapper: LongUnaryOperator): LongStream

  // Since: Java 16
  def mapMulti(mapper: LongStream.LongMapMultiConsumer): LongStream = {

    /* Design Note:
     *    This implementation differs from the reference default implementation
     *   described in the Java Stream#mapMulti documentation.
     *
     * That implementation is basically:
     *    this.flatMap(e => {
     *      val buffer = new ArrayList[R]()
     *      mapper.accept(e, r => buffer.add(r))
     *      buffer.stream()
     *    })
     *
     * It offers few of the benefits described for the multiMap method:
     * reduced number of streams created, runtime efficiency, etc.
     *
     * This implementation should actually provide the benefits of mapMulti().
     */

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    val buffer = new ArrayDeque[Long]()

    // Can not predict replacements, so Spliterator can not be SIZED.
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl =
      new Spliterators.AbstractLongSpliterator(Long.MaxValue, unSized) {

        def tryAdvance(action: LongConsumer): Boolean = {
          var advanced = false

          var done = false
          while (!done) {
            if (buffer.size() == 0) {
              val stepped =
                spliter.tryAdvance(e => mapper.accept(e, r => buffer.add(r)))
              done = !stepped
            } else {
              action.accept(buffer.removeFirst())
              advanced = true
              done = true
            }
          }

          advanced
        }
      }

    new LongStreamImpl(
      spl,
      parallel = false,
      parent = this.asInstanceOf[LongStream]
    )
  }

  def mapToDouble(mapper: LongToDoubleFunction): DoubleStream

  def mapToInt(mapper: LongToIntFunction): IntStream

  def mapToObj[U](mapper: LongFunction[_ <: U]): Stream[U]

  def max(): OptionalLong

  def min(): OptionalLong

  def noneMatch(pred: LongPredicate): Boolean

  def peek(action: LongConsumer): LongStream

  def reduce(identity: scala.Long, op: LongBinaryOperator): scala.Long

  def reduce(op: LongBinaryOperator): OptionalLong

  def skip(n: scala.Long): LongStream

  def sorted(): LongStream

  def sum(): scala.Long

  def summaryStatistics(): LongSummaryStatistics

  // Since: Java 9
  def takeWhile(pred: LongPredicate): LongStream = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for takeWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractLongSpliterator(
      Long.MaxValue,
      unSized
    ) {
      var done = false // short-circuit

      override def trySplit(): Spliterator.OfLong =
        null.asInstanceOf[Spliterator.OfLong]

      def tryAdvance(action: LongConsumer): Boolean = {
        if (done) false
        else
          spliter.tryAdvance(e =>
            if (!pred.test(e)) done = true
            else action.accept(e)
          )
      }
    }

    new LongStreamImpl(spl, parallel = false, parent = this)
  }

  def toArray(): Array[scala.Long]

}

object LongStream {

  trait Builder extends LongConsumer {
    def accept(t: scala.Long): Unit
    def add(t: scala.Long): LongStream.Builder = {
      accept(t)
      this
    }
    def build(): LongStream
  }

  @FunctionalInterface
  trait LongMapMultiConsumer {
    def accept(value: scala.Long, dc: LongConsumer): Unit
  }

  def builder(): LongStream.Builder =
    new LongStreamImpl.Builder

  def concat(a: LongStream, b: LongStream): LongStream =
    LongStreamImpl.concat(a, b)

  def empty(): LongStream =
    new LongStreamImpl(
      Spliterators.emptyLongSpliterator(),
      parallel = false
    )

  def generate(s: LongSupplier): LongStream = {
    val spliter =
      new Spliterators.AbstractLongSpliterator(Long.MaxValue, 0) {
        def tryAdvance(action: LongConsumer): Boolean = {
          action.accept(s.getAsLong())
          true
        }
      }

    new LongStreamImpl(spliter, parallel = false)
  }

  // Since: Java 9
  def iterate(
      seed: scala.Long,
      hasNext: LongPredicate,
      next: LongUnaryOperator
  ): LongStream = {
    // "seed" on RHS here is to keep compiler happy with local var initialize.
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractLongSpliterator(
        Long.MaxValue,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
      ) {
        def tryAdvance(action: LongConsumer): Boolean = {
          val current =
            if (seedUsed) next.applyAsLong(previous)
            else {
              seedUsed = true
              seed
            }

          val advanceOK = hasNext.test(current)
          if (advanceOK) {
            action.accept(current)
            previous = current
          }
          advanceOK
        }
      }

    new LongStreamImpl(spliter, parallel = false)
  }

  def iterate(
      seed: scala.Long,
      f: LongUnaryOperator
  ): LongStream = {
    // "seed" on RHS here is to keep compiler happy with local var initialize.
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractLongSpliterator(
        Long.MaxValue,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
      ) {
        def tryAdvance(action: LongConsumer): Boolean = {
          val current =
            if (seedUsed) f.applyAsLong(previous)
            else {
              seedUsed = true
              seed
            }

          action.accept(current)
          previous = current
          true
        }
      }

    new LongStreamImpl(spliter, parallel = false)
  }

  def of(values: Array[Long]): LongStream = {
    /* One would expect variables arguments to be declared as
     * "values: Objects*" here.
     * However, that causes "symbol not found" errors at OS link time.
     * An implicit conversion must be missing in the javalib environment.
     */

    Arrays.stream(values)
  }

  def of(t: Long): LongStream = {
    val values = new Array[Long](1)
    values(0) = t
    LongStream.of(values)
  }

  private def rangeImpl(
      start: Long,
      end: Long,
      inclusive: Boolean
  ): LongStream = {

    val exclusiveSpan = end - start
    val size =
      if (inclusive) exclusiveSpan + 1L
      else exclusiveSpan

    val spl = new Spliterators.AbstractLongSpliterator(
      size,
      Spliterator.SIZED | Spliterator.SUBSIZED
    ) {

      override def trySplit(): Spliterator.OfLong =
        null.asInstanceOf[Spliterator.OfLong]

      var cursor = start

      def tryAdvance(action: LongConsumer): Boolean = {
        val advance = (cursor < end) || ((cursor == end) && inclusive)
        if (advance) {
          action.accept(cursor)
          cursor += 1
        }
        advance
      }
    }

    new LongStreamImpl(spl, parallel = false)
  }

  def range(startInclusive: Long, endExclusive: Long): LongStream =
    LongStream.rangeImpl(startInclusive, endExclusive, inclusive = false)

  def rangeClosed(startInclusive: Long, endInclusive: Long): LongStream =
    LongStream.rangeImpl(startInclusive, endInclusive, inclusive = true)

}
