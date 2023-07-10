package java.util.stream

import java.{lang => jl}

import java.util._
import java.util.function._

/* Design Note:
 *
 * DoubleStream extends BaseStream[jl.Double, DoubleStream]
 * in correspondence to the documentation & usage of Spliterator.Of*
 * and PrimitiveIterator.Of*. That is, the first type is a Java container.
 *
 * In this file "Double" types should be qualified to ease tracing the code
 * and prevent confusion & defects.
 *   * jl.Double indicates an Java Object qua Scala AnyRef is desired.
 *   * scala.Double indicates a Java "double" primitive is desired.
 *     Someday, the generated code should be examined to ensure that
 *     unboxed primitives are actually being used.
 */

trait DoubleStream extends BaseStream[jl.Double, DoubleStream] {

  def allMatch(pred: DoublePredicate): Boolean

  def anyMatch(pred: DoublePredicate): Boolean

  def average(): OptionalDouble

  def boxed(): Stream[jl.Double]

  def collect[R](
      supplier: Supplier[R],
      accumulator: ObjDoubleConsumer[R],
      combiner: BiConsumer[R, R]
  ): R

  def count(): Long

  def distinct(): DoubleStream

  // Since: Java 9
  def dropWhile(pred: DoublePredicate): DoubleStream = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for dropWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractDoubleSpliterator(
      Long.MaxValue,
      unSized
    ) {

      override def trySplit(): Spliterator.OfDouble =
        null.asInstanceOf[Spliterator.OfDouble]

      var doneDropping = false

      def tryAdvance(action: DoubleConsumer): Boolean = {
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

    new DoubleStreamImpl(spl, parallel = false, parent = this)
  }

  def filter(pred: DoublePredicate): DoubleStream

  def findAny(): OptionalDouble

  def findFirst(): OptionalDouble

  def flatMap(mapper: DoubleFunction[_ <: DoubleStream]): DoubleStream

  def forEach(action: DoubleConsumer): Unit

  def forEachOrdered(action: DoubleConsumer): Unit

  def limit(maxSize: Long): DoubleStream

  def map(mapper: DoubleUnaryOperator): DoubleStream

  // Since: Java 16
  def mapMulti(mapper: DoubleStream.DoubleMapMultiConsumer): DoubleStream = {

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

    val buffer = new ArrayDeque[scala.Double]()

    // Can not predict replacements, so Spliterator can not be SIZED.
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl =
      new Spliterators.AbstractDoubleSpliterator(Long.MaxValue, unSized) {

        def tryAdvance(action: DoubleConsumer): Boolean = {
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

    new DoubleStreamImpl(
      spl,
      parallel = false,
      parent = this.asInstanceOf[DoubleStream]
    )
  }

  def mapToInt(mapper: DoubleToIntFunction): IntStream

  def mapToLong(mapper: DoubleToLongFunction): LongStream

  def mapToObj[U](mapper: DoubleFunction[_ <: U]): Stream[U]

  def max(): OptionalDouble

  def min(): OptionalDouble

  def noneMatch(pred: DoublePredicate): Boolean

  def peek(action: DoubleConsumer): DoubleStream

  def reduce(identity: scala.Double, op: DoubleBinaryOperator): Double

  def reduce(op: DoubleBinaryOperator): OptionalDouble

  def skip(n: Long): DoubleStream

  def sorted(): DoubleStream

  def sum(): scala.Double

  def summaryStatistics(): DoubleSummaryStatistics

  // Since: Java 9
  def takeWhile(pred: DoublePredicate): DoubleStream = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for takeWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractDoubleSpliterator(
      Long.MaxValue,
      unSized
    ) {
      var done = false // short-circuit

      override def trySplit(): Spliterator.OfDouble =
        null.asInstanceOf[Spliterator.OfDouble]

      def tryAdvance(action: DoubleConsumer): Boolean = {
        if (done) false
        else
          spliter.tryAdvance(e =>
            if (!pred.test(e)) done = true
            else action.accept(e)
          )
      }
    }

    new DoubleStreamImpl(spl, parallel = false, parent = this)
  }

  def toArray(): Array[scala.Double]

}

object DoubleStream {

  trait Builder extends DoubleConsumer {
    def accept(t: Double): Unit
    def add(t: Double): DoubleStream.Builder = {
      accept(t)
      this
    }
    def build(): DoubleStream
  }

  @FunctionalInterface
  trait DoubleMapMultiConsumer {
    def accept(value: scala.Double, dc: DoubleConsumer): Unit
  }

  def builder(): DoubleStream.Builder =
    new DoubleStreamImpl.Builder

  def concat(a: DoubleStream, b: DoubleStream): DoubleStream =
    DoubleStreamImpl.concat(a, b)

  def empty(): DoubleStream =
    new DoubleStreamImpl(
      Spliterators.emptyDoubleSpliterator(),
      parallel = false
    )

  def generate(s: DoubleSupplier): DoubleStream = {
    val spliter =
      new Spliterators.AbstractDoubleSpliterator(Long.MaxValue, 0) {
        def tryAdvance(action: DoubleConsumer): Boolean = {
          action.accept(s.getAsDouble())
          true
        }
      }

    new DoubleStreamImpl(spliter, parallel = false)
  }

  // Since: Java 9
  def iterate(
      seed: scala.Double,
      hasNext: DoublePredicate,
      next: DoubleUnaryOperator
  ): DoubleStream = {
    // "seed" on RHS here is to keep compiler happy with local var initialize.
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractDoubleSpliterator(
        Long.MaxValue,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
      ) {
        def tryAdvance(action: DoubleConsumer): Boolean = {
          val current =
            if (seedUsed) next.applyAsDouble(previous)
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

    new DoubleStreamImpl(spliter, parallel = false)
  }

  def iterate(
      seed: scala.Double,
      f: DoubleUnaryOperator
  ): DoubleStream = {
    // "seed" on RHS here is to keep compiler happy with local var initialize.
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractDoubleSpliterator(
        Long.MaxValue,
        Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
      ) {
        def tryAdvance(action: DoubleConsumer): Boolean = {
          val current =
            if (seedUsed) f.applyAsDouble(previous)
            else {
              seedUsed = true
              seed
            }

          action.accept(current)
          previous = current
          true
        }
      }

    new DoubleStreamImpl(spliter, parallel = false)
  }

  def of(values: Array[scala.Double]): DoubleStream = {
    /* One would expect variables arguments to be declared as
     * "values: Objects*" here.
     * However, that causes "symbol not found" errors at OS link time.
     * An implicit conversion must be missing in the javalib environment.
     */

    Arrays.stream(values)
  }

  def of(t: scala.Double): DoubleStream = {
    val values = new Array[Double](1)
    values(0) = t
    DoubleStream.of(values)
  }

}
