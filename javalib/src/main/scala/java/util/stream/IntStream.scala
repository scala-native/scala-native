package java.util.stream

import java.util._
import java.util.function._
import java.{lang => jl}

/* Design Note:
 *
 * IntStream extends BaseStream[jl.Int, IntStream]
 * in correspondence to the documentation & usage of Spliterator.Of*
 * and PrimitiveIterator.Of*. That is, the first type is a Java container.
 */

trait IntStream extends BaseStream[jl.Integer, IntStream] {

  def allMatch(pred: IntPredicate): Boolean

  def anyMatch(pred: IntPredicate): Boolean

  def asDoubleStream(): DoubleStream

  def asLongStream(): LongStream

  def average(): OptionalDouble

  def boxed(): Stream[jl.Integer]

  def collect[R](
      supplier: Supplier[R],
      accumulator: ObjIntConsumer[R],
      combiner: BiConsumer[R, R]
  ): R

  def count(): scala.Long

  def distinct(): IntStream

  // Since: Java 9
  def dropWhile(pred: IntPredicate): IntStream = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for dropWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractIntSpliterator(
      Long.MaxValue,
      unSized
    ) {

      override def trySplit(): Spliterator.OfInt =
        null.asInstanceOf[Spliterator.OfInt]

      var doneDropping = false

      def tryAdvance(action: IntConsumer): Boolean = {
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

    new IntStreamImpl(spl, parallel = false, parent = this)
  }

  def filter(pred: IntPredicate): IntStream

  def findAny(): OptionalInt

  def findFirst(): OptionalInt

  def flatMap(mapper: IntFunction[_ <: IntStream]): IntStream

  def forEach(action: IntConsumer): Unit

  def forEachOrdered(action: IntConsumer): Unit

  def limit(maxSize: scala.Long): IntStream

  def map(mapper: IntUnaryOperator): IntStream

  // Since: Java 16
  def mapMulti(mapper: IntStream.IntMapMultiConsumer): IntStream = {

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

    val buffer = new ArrayDeque[Int]()

    // Can not predict replacements, so Spliterator can not be SIZED.
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl =
      new Spliterators.AbstractIntSpliterator(Long.MaxValue, unSized) {

        def tryAdvance(action: IntConsumer): Boolean = {
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

    new IntStreamImpl(
      spl,
      parallel = false,
      parent = this.asInstanceOf[IntStream]
    )
  }

  def mapToDouble(mapper: IntToDoubleFunction): DoubleStream

  def mapToLong(mapper: IntToLongFunction): LongStream

  def mapToObj[U](mapper: IntFunction[_ <: U]): Stream[U]

  def max(): OptionalInt

  def min(): OptionalInt

  def noneMatch(pred: IntPredicate): Boolean

  def peek(action: IntConsumer): IntStream

  def reduce(identity: scala.Int, op: IntBinaryOperator): scala.Int

  def reduce(op: IntBinaryOperator): OptionalInt

  def skip(n: scala.Long): IntStream

  def sorted(): IntStream

  def sum(): scala.Int

  def summaryStatistics(): IntSummaryStatistics

  // Since: Java 9
  def takeWhile(pred: IntPredicate): IntStream = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for takeWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractIntSpliterator(
      Long.MaxValue,
      unSized
    ) {
      var done = false // short-circuit

      override def trySplit(): Spliterator.OfInt =
        null.asInstanceOf[Spliterator.OfInt]

      def tryAdvance(action: IntConsumer): Boolean = {
        if (done) false
        else
          spliter.tryAdvance(e =>
            if (!pred.test(e)) done = true
            else action.accept(e)
          )
      }
    }

    new IntStreamImpl(spl, parallel = false, parent = this)
  }

  def toArray(): Array[scala.Int]

}

object IntStream {

  trait Builder extends IntConsumer {
    def accept(t: scala.Int): Unit
    def add(t: scala.Int): IntStream.Builder = {
      accept(t)
      this
    }
    def build(): IntStream
  }

  @FunctionalInterface
  trait IntMapMultiConsumer {
    def accept(value: scala.Int, dc: IntConsumer): Unit
  }

  def builder(): IntStream.Builder =
    new IntStreamImpl.Builder

  def concat(a: IntStream, b: IntStream): IntStream =
    IntStreamImpl.concat(a, b)

  def empty(): IntStream =
    new IntStreamImpl(
      Spliterators.emptyIntSpliterator(),
      parallel = false
    )

  def generate(s: IntSupplier): IntStream = {
    val spliter =
      new Spliterators.AbstractIntSpliterator(Long.MaxValue, 0) {
        def tryAdvance(action: IntConsumer): Boolean = {
          action.accept(s.getAsInt())
          true
        }
      }

    new IntStreamImpl(spliter, parallel = false)
  }

  // Since: Java 9
  def iterate(
      seed: scala.Int,
      hasNext: IntPredicate,
      next: IntUnaryOperator
  ): IntStream = {
    // "seed" on RHS here is to keep compiler happy with local var initialize.
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractIntSpliterator(
        Long.MaxValue,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
      ) {
        def tryAdvance(action: IntConsumer): Boolean = {
          val current =
            if (seedUsed) next.applyAsInt(previous)
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

    new IntStreamImpl(spliter, parallel = false)
  }

  def iterate(
      seed: scala.Int,
      f: IntUnaryOperator
  ): IntStream = {
    // "seed" on RHS here is to keep compiler happy with local var initialize.
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractIntSpliterator(
        Long.MaxValue,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
      ) {
        def tryAdvance(action: IntConsumer): Boolean = {
          val current =
            if (seedUsed) f.applyAsInt(previous)
            else {
              seedUsed = true
              seed
            }

          action.accept(current)
          previous = current
          true
        }
      }

    new IntStreamImpl(spliter, parallel = false)
  }

  def of(values: Array[Int]): IntStream = {
    /* One would expect variables arguments to be declared as
     * "values: Objects*" here.
     * However, that causes "symbol not found" errors at OS link time.
     * An implicit conversion must be missing in the javalib environment.
     */

    Arrays.stream(values)
  }

  def of(t: Int): IntStream = {
    val values = new Array[Int](1)
    values(0) = t
    IntStream.of(values)
  }

  private def rangeImpl(start: Int, end: Int, inclusive: Boolean): IntStream = {

    val exclusiveSpan = end - start
    val size =
      if (inclusive) exclusiveSpan + 1
      else exclusiveSpan

    val spl = new Spliterators.AbstractIntSpliterator(
      size,
      Spliterator.SIZED | Spliterator.SUBSIZED
    ) {

      override def trySplit(): Spliterator.OfInt =
        null.asInstanceOf[Spliterator.OfInt]

      var cursor = start

      def tryAdvance(action: IntConsumer): Boolean = {
        val advance = (cursor < end) || ((cursor == end) && inclusive)
        if (advance) {
          action.accept(cursor)
          cursor += 1
        }
        advance
      }
    }

    new IntStreamImpl(spl, parallel = false)
  }

  def range(startInclusive: Int, endExclusive: Int): IntStream =
    IntStream.rangeImpl(startInclusive, endExclusive, inclusive = false)

  def rangeClosed(startInclusive: Int, endInclusive: Int): IntStream =
    IntStream.rangeImpl(startInclusive, endInclusive, inclusive = true)

}
