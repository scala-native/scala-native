package java.util.stream

import java.util._
import java.util.function._

trait Stream[T] extends BaseStream[T, Stream[T]] {

  def allMatch(pred: Predicate[_ >: T]): Boolean

  def anyMatch(pred: Predicate[_ >: T]): Boolean

  def collect[R, A](collector: Collector[_ >: T, A, R]): R

  def collect[R](
      supplier: Supplier[R],
      accumulator: BiConsumer[R, _ >: T],
      combiner: BiConsumer[R, R]
  ): R

  def count(): Long

  def distinct(): Stream[T]

  // Since: Java 9
  def dropWhile(pred: Predicate[_ >: T]): Stream[T] = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for dropWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractSpliterator[T](
      Long.MaxValue,
      unSized
    ) {

      override def trySplit(): Spliterator[T] =
        null.asInstanceOf[Spliterator[T]]

      var doneDropping = false

      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        if (doneDropping) {
          spliter.tryAdvance((e) => action.accept(e))
        } else {
          var doneLooping = false
          while (!doneLooping) {
            val advanced =
              spliter.tryAdvance((e) => {
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

    new ObjectStreamImpl[T](spl, parallel = false, parent = this)
  }

  def filter(pred: Predicate[_ >: T]): Stream[T]

  def findAny(): Optional[T]

  def findFirst(): Optional[T]

  def flatMap[R](mapper: Function[_ >: T, _ <: Stream[_ <: R]]): Stream[R]

  def flatMapToDouble(
      mapper: Function[_ >: T, _ <: DoubleStream]
  ): DoubleStream

  def flatMapToInt(
      mapper: Function[_ >: T, _ <: IntStream]
  ): IntStream

  def flatMapToLong(
      mapper: Function[_ >: T, _ <: LongStream]
  ): LongStream

  def forEach(action: Consumer[_ >: T]): Unit

  def forEachOrdered(action: Consumer[_ >: T]): Unit

  def limit(maxSize: Long): Stream[T]

  def map[R](mapper: Function[_ >: T, _ <: R]): Stream[R]

  // Since: Java 16
  def mapMulti[R](mapper: BiConsumer[_ >: T, Consumer[_ >: R]]): Stream[R] = {
    /* Design Note:
     *  This implementation differs from the reference default implementation
     *  described in the Java Stream#mapMulti documentation.
     *
     *  That implementation is basically:
     *      this.flatMap(e => {
     *        val buffer = new ArrayList[R]()
     *        mapper.accept(e, r => buffer.add(r))
     *        buffer.stream()
     *      })
     *
     *  It offers few of the benefits described for the multiMap method:
     *  reduced number of streams created, runtime efficiency, etc.
     *
     *  This implementation should actually provide the benefits of mapMulti().
     */

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    val buffer = new ArrayDeque[R]()

    // Can not predict replacements, so Spliterator can not be SIZED.
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractSpliterator[R](Long.MaxValue, unSized) {

      def tryAdvance(action: Consumer[_ >: R]): Boolean = {
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

    (new ObjectStreamImpl[R](
      spl,
      parallel = false,
      parent = this.asInstanceOf[Stream[R]]
    ))
      .asInstanceOf[Stream[R]]
  }

  // Since: Java 16
  def mapMultiToDouble(
      mapper: BiConsumer[_ >: T, _ >: DoubleConsumer]
  ): DoubleStream = {
    // See implementation notes in mapMulti[R]()

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    val buffer = new ArrayDeque[Double]()

    // Can not predict replacements, so Spliterator can not be SIZED.
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl =
      new Spliterators.AbstractDoubleSpliterator(Long.MaxValue, unSized) {
        val dc: DoubleConsumer = doubleValue => buffer.add(doubleValue)

        def tryAdvance(action: DoubleConsumer): Boolean = {
          var advanced = false

          var done = false
          while (!done) {
            if (buffer.size() == 0) {
              val stepped = spliter.tryAdvance(e => mapper.accept(e, dc))
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

    val coercedPriorStages = this
      .asInstanceOf[ObjectStreamImpl[T]]
      .pipeline
      .asInstanceOf[ArrayDeque[DoubleStreamImpl]]

    (new DoubleStreamImpl(
      spl,
      parallel = false,
      coercedPriorStages
    ))
      .asInstanceOf[DoubleStream]
  }

  // Since: Java 16
  def mapMultiToInt(
      mapper: BiConsumer[_ >: T, _ >: IntConsumer]
  ): IntStream = {
    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  // Since: Java 16
  def mapMultiToLong(
      mapper: BiConsumer[_ >: T, _ >: LongConsumer]
  ): LongStream = {
    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def mapToDouble(mapper: ToDoubleFunction[_ >: T]): DoubleStream

  def mapToInt(mapper: ToIntFunction[_ >: T]): IntStream

  def mapToLong(mapper: ToLongFunction[_ >: T]): LongStream

  def max(comparator: Comparator[_ >: T]): Optional[T]

  def min(comparator: Comparator[_ >: T]): Optional[T]

  def noneMatch(pred: Predicate[_ >: T]): Boolean

  def peek(action: Consumer[_ >: T]): Stream[T]

  def reduce(accumulator: BinaryOperator[T]): Optional[T]

  def reduce(identity: T, accumulator: BinaryOperator[T]): T

  def reduce[U](
      identity: U,
      accumulator: BiFunction[U, _ >: T, U],
      combiner: BinaryOperator[U]
  ): U

  def skip(n: Long): Stream[T]

  def spliterator(): Spliterator[_ <: T]

  def sorted(): Stream[T]

  def sorted(comparator: Comparator[_ >: T]): Stream[T]

  // Since: Java 9
  def takeWhile(pred: Predicate[_ >: T]): Stream[T] = {
    Objects.requireNonNull(pred)

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // JVM appears to use an unsized iterator for takeWhile()
    // May need to adjust other characteristics.
    val unSized = spliter.characteristics() &
      ~(Spliterator.SIZED | Spliterator.SUBSIZED)

    val spl = new Spliterators.AbstractSpliterator[T](
      Long.MaxValue,
      unSized
    ) {
      var done = false // short-circuit

      override def trySplit(): Spliterator[T] =
        null.asInstanceOf[Spliterator[T]]

      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        if (done) false
        else
          spliter.tryAdvance((e) =>
            if (!pred.test(e)) done = true
            else action.accept(e)
          )
      }
    }

    new ObjectStreamImpl[T](spl, parallel = false, parent = this)
  }

  def toArray(): Array[Object]

  def toArray[A <: Object](generator: IntFunction[Array[A]]): Array[A]

  // Since: Java 16
  def toList[T](): List[T] = {
    // A loose translation of the Java 19 toList example implementation.
    // That doc suggests that implementations override this inelegant
    // implementation.

    val spliter = this.spliterator() //  also marks this stream "operated upon"

    // Use size knowledge, if available, to reduce list re-sizing overhead.
    val knownSize = spliter.getExactSizeIfKnown()
    val initialSize =
      if (knownSize < 0) 50 // a guess, intended to be better than default 16
      else knownSize.toInt

    val aL = new ArrayList[T](initialSize)

    spliter.forEachRemaining((e) => aL.add(e.asInstanceOf[T]))

    Collections.unmodifiableList(aL)
  }
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

  def builder[T](): Builder[T] = new ObjectStreamImpl.Builder[T]

  def concat[T](a: Stream[_ <: T], b: Stream[_ <: T]): Stream[T] =
    ObjectStreamImpl.concat(a, b)

  def empty[T](): Stream[T] =
    new ObjectStreamImpl(Spliterators.emptySpliterator[T](), parallel = false)

  def generate[T](s: Supplier[T]): Stream[T] = {
    val spliter =
      new Spliterators.AbstractSpliterator[T](Long.MaxValue, 0) {
        def tryAdvance(action: Consumer[_ >: T]): Boolean = {
          action.accept(s.get())
          true
        }
      }

    new ObjectStreamImpl(spliter, parallel = false)
  }

  // Since: Java 9
  def iterate[T](
      seed: T,
      hasNext: Predicate[T],
      next: UnaryOperator[T]
  ): Stream[T] = {
    // "seed" on RHS here is to keep compiler happy with local var init
    var previous = seed
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractSpliterator[T](Long.MaxValue, 0) {
        def tryAdvance(action: Consumer[_ >: T]): Boolean = {
          val current =
            if (seedUsed) next(previous)
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

    new ObjectStreamImpl(spliter, parallel = false)
  }

  def iterate[T](seed: T, f: UnaryOperator[T]): Stream[T] = {
    var previous = seed // "seed" here is just to keep compiler happy.
    var seedUsed = false

    val spliter =
      new Spliterators.AbstractSpliterator[T](Long.MaxValue, 0) {
        def tryAdvance(action: Consumer[_ >: T]): Boolean = {
          val current =
            if (seedUsed) f(previous)
            else {
              seedUsed = true
              seed
            }

          action.accept(current)
          previous = current
          true
        }
      }

    new ObjectStreamImpl(spliter, parallel = false)
  }

  def of[T](values: Array[Object]): Stream[T] = {
    /* One would expect variables arguments to be declared as
     * "values: Objects*" here.
     * However, that causes "symbol not found" errors at OS link time.
     * An implicit conversion must be missing in the javalib environment.
     */

    val bldr = Stream.builder[T]()
    for (j <- values)
      bldr.add(j.asInstanceOf[T])
    bldr.build()
  }

  def of[T](t: Object): Stream[T] =
    Stream.builder[T]().add(t.asInstanceOf[T]).build()

  // Since: Java 9
  def ofNullable[T <: Object](t: T): Stream[T] = {
    if (t == null) Stream.empty[T]()
    else Stream.of[T](t)
  }
}
