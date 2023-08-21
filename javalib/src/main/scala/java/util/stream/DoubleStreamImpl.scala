package java.util.stream

import java.{lang => jl}
import java.{util => ju}
import java.util._
import java.util.function._

/* See "Design Note" at top of DoubleStream.scala for jl.Double & scala.Double
 * TL;DR - later is explicitly used where a primitive is desired.
 */

private[stream] class DoubleStreamImpl(
    val pipeline: ArrayDeque[DoubleStreamImpl]
) extends DoubleStream {
  var _spliterArg: Spliterator.OfDouble = _
  var _supplier: Supplier[Spliterator.OfDouble] = _
  var _parallel: Boolean = _ // Scaffolding for later improvements.
  var _characteristics: Int = 0

  lazy val _spliter: Spliterator.OfDouble =
    if (_spliterArg != null) _spliterArg
    else _supplier.get()

  var _operatedUpon: Boolean = false
  var _closed: Boolean = false

  // avoid allocating an onCloseQueue just to check if it is empty.
  var onCloseQueueActive = false
  lazy val onCloseQueue = new ArrayDeque[Runnable]()

  pipeline.addLast(this)

  def this(
      spliterator: Spliterator.OfDouble,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[DoubleStreamImpl])
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator.OfDouble,
      parallel: Boolean,
      parent: DoubleStream
  ) = {
    this(parent.asInstanceOf[DoubleStreamImpl].pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator.OfDouble,
      parallel: Boolean,
      pipeline: ArrayDeque[DoubleStreamImpl]
  ) = {
    this(pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      supplier: Supplier[Spliterator.OfDouble],
      characteristics: Int,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[DoubleStreamImpl])
    _supplier = supplier
    _parallel = parallel
    _characteristics = characteristics
  }

// Methods specified in interface BaseStream ----------------------------

  /* Throw IllegalStateException() if an attempt is made to operate
   * on a stream a second time or after it has been closed.
   * The JVM throws on most but not all "second" intermediate or terminal
   * stream operations. The intent is that Scala Native match that set.
   */

  protected def commenceOperation(): Unit = {
    if (_operatedUpon || _closed)
      StreamImpl.throwIllegalStateException()

    _operatedUpon = true
  }

  def close(): Unit = {
    if (!_closed) {
      val exceptionBuffer = new DoubleStreamImpl.CloseExceptionBuffer()
      val it = pipeline.iterator()

      while (it.hasNext()) {
        try {
          it.next().closeStage()
        } catch {
          case e: Exception => exceptionBuffer.add(e)
        }
      }

      exceptionBuffer.reportExceptions()
    }
  }

  private def closeStage(): Unit = {
    _closed = true

    val exceptionBuffer = new DoubleStreamImpl.CloseExceptionBuffer()

    if (onCloseQueueActive) {
      val it = onCloseQueue.iterator()
      while (it.hasNext()) {
        try {
          it.next().run()
        } catch {
          case e: Exception => exceptionBuffer.add(e)
        }
      }
    }

    exceptionBuffer.reportExceptions()
  }

  def isParallel(): Boolean = _parallel

  def iterator(): ju.PrimitiveIterator.OfDouble = {
    commenceOperation()
    Spliterators.iterator(_spliter)
  }

  def onClose(closeHandler: Runnable): DoubleStream = {
    // JVM appears to not set "operated upon" here.

    if (_closed)
      StreamImpl.throwIllegalStateException()

    // detects & throws on closeHandler == null
    onCloseQueue.addLast(closeHandler)

    if (!onCloseQueueActive)
      onCloseQueueActive = true

    this
  }

  def parallel(): DoubleStream = {
    if (!_parallel)
      _parallel = true
    this
  }

  def sequential(): DoubleStream = {
    if (_parallel)
      _parallel = false
    this
  }

  def spliterator(): Spliterator.OfDouble = {
    commenceOperation()
    _spliter
  }

  def unordered(): DoubleStream = {
    val masked = _spliter.characteristics() & Spliterator.ORDERED

    if (masked != Spliterator.ORDERED) this // already unordered.
    else {
      commenceOperation()

      val bitsToClear =
        (Spliterator.CONCURRENT
          | Spliterator.IMMUTABLE
          | Spliterator.NONNULL
          | Spliterator.ORDERED
          | Spliterator.SIZED
          | Spliterator.SUBSIZED)

      val purifiedBits = _characteristics & ~(bitsToClear)

      val spl = new Spliterators.AbstractDoubleSpliterator(
        _spliter.estimateSize(),
        purifiedBits
      ) {
        def tryAdvance(action: DoubleConsumer): Boolean =
          _spliter.tryAdvance((e: scala.Double) => action.accept(e))
      }

      new DoubleStreamImpl(spl, _parallel, pipeline)
    }
  }

// Methods specified in interface Stream --------------------------------

  def allMatch(pred: DoublePredicate): Boolean = {
    commenceOperation()

    // Be careful with documented "true" return for empty stream.
    var mismatchFound = false

    while (!mismatchFound &&
        _spliter.tryAdvance((e: scala.Double) =>
          if (!pred.test(e))
            mismatchFound = true
        )) { /* search */ }
    !mismatchFound
  }

  def anyMatch(pred: DoublePredicate): Boolean = {
    commenceOperation()

    var matchFound = false

    while (!matchFound &&
        _spliter.tryAdvance((e: scala.Double) =>
          if (pred.test(e))
            matchFound = true
        )) { /* search */ }
    matchFound
  }

  def average(): OptionalDouble = {
    commenceOperation()

    var count = 0
    var sum = 0.0

    _spliter.forEachRemaining((d: scala.Double) => { count += 1; sum += d })
    if (count == 0) OptionalDouble.empty()
    else OptionalDouble.of(sum / count)
  }

  def boxed(): Stream[jl.Double] =
    this.mapToObj[jl.Double](d => scala.Double.box(d))

  def collect[R](
      supplier: Supplier[R],
      accumulator: ObjDoubleConsumer[R],
      combiner: BiConsumer[R, R]
  ): R = {
    commenceOperation()

    val result = supplier.get()

    _spliter.forEachRemaining((e: scala.Double) =>
      accumulator.accept(result, e)
    )

    result
  }

  def count(): Long = {
    commenceOperation()

    var count = 0L
    _spliter.forEachRemaining((d: scala.Double) => count += 1)
    count
  }

  def distinct(): DoubleStream = {
    commenceOperation()

    val seenElements = new ju.HashSet[scala.Double]()

    // Some items may be dropped, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl =
      new Spliterators.AbstractDoubleSpliterator(
        estimatedSize,
        _spliter.characteristics()
      ) {
        def tryAdvance(action: DoubleConsumer): Boolean = {
          var success = false
          var done = false
          while (!done) {
            var advanced =
              _spliter.tryAdvance((e: scala.Double) => {
                val added = seenElements.add(e)

                if (added) {
                  action.accept(e)
                  done = true
                  success = true
                }
              })
            if (!advanced)
              done = true
          }
          success
        }
      }

    new DoubleStreamImpl(spl, _parallel, pipeline)
  }

  def filter(pred: DoublePredicate): DoubleStream = {
    commenceOperation()

    // Some items may be filtered out, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl = new Spliterators.AbstractDoubleSpliterator(
      estimatedSize,
      _spliter.characteristics()
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean = {
        var success = false
        var done = false
        while (!done) {
          var advanced =
            _spliter.tryAdvance((e: scala.Double) => {
              if (pred.test(e)) {
                action.accept(e)
                done = true
                success = true
              }
            })

          if (!advanced)
            done = true
        }
        success
      }
    }

    new DoubleStreamImpl(spl, _parallel, pipeline)
  }

  /* delegating to findFirst() is an implementation ~~hack~~ expediency.
   * Probably near-optimal for sequential streams. Parallel streams may
   * offer better possibilities.
   */
  def findAny(): OptionalDouble = {
    // commenceOperation() // findFirst will call, so do not do twice.
    findFirst()
  }

  def findFirst(): OptionalDouble = {
    commenceOperation()
    var optional = OptionalDouble.empty()
    _spliter.tryAdvance((e: scala.Double) => {
      optional = OptionalDouble.of(e)
    })
    optional
  }

  def flatMap(
      mapper: DoubleFunction[_ <: DoubleStream]
  ): DoubleStream = {
    commenceOperation()

    val supplier =
      new DoubleStreamImpl.DoublePrimitiveCompoundSpliteratorFactory(
        _spliter,
        mapper,
        closeOnFirstTouch = true
      )

    val coercedPriorStages = pipeline
      .asInstanceOf[ArrayDeque[DoubleStreamImpl]]

    new DoubleStreamImpl(supplier.get(), _parallel, coercedPriorStages)
  }

  def forEach(action: DoubleConsumer): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def forEachOrdered(action: DoubleConsumer): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def limit(maxSize: Long): DoubleStream = {

    /* Important:
     * See Issue #3309 & StreamImpl#limit for discussion of size
     * & characteristics in JVM 17 (and possibly as early as JVM 12)
     * for parallel ORDERED streams.
     * The behavior implemented here is Java 8 and at least Java 11.
     */

    if (maxSize < 0)
      throw new IllegalArgumentException(maxSize.toString())

    commenceOperation() // JVM tests argument before operatedUpon or closed.

    var nSeen = 0L

    val startingBits = _spliter.characteristics()

    val alwaysClearedBits =
      Spliterator.SIZED | Spliterator.SUBSIZED |
        Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.CONCURRENT

    val newStreamCharacteristics = startingBits & ~alwaysClearedBits

    val spl = new Spliterators.AbstractDoubleSpliterator(
      Long.MaxValue,
      newStreamCharacteristics
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean =
        if (nSeen >= maxSize) false
        else {
          var advanced =
            _spliter.tryAdvance((e: scala.Double) => action.accept(e))
          nSeen =
            if (advanced) nSeen + 1
            else Long.MaxValue

          advanced
        }
    }

    new DoubleStreamImpl(spl, _parallel, pipeline)
  }

  def map(
      mapper: DoubleUnaryOperator
  ): DoubleStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractDoubleSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Double) =>
          action.accept(mapper.applyAsDouble(e))
        )
    }

    new DoubleStreamImpl(spl, _parallel, pipeline)
  }

  def mapToInt(mapper: DoubleToIntFunction): IntStream =
    throw new UnsupportedOperationException("Not Yet Implemented")

  def mapToLong(mapper: DoubleToLongFunction): LongStream =
    throw new UnsupportedOperationException("Not Yet Implemented")

  def mapToObj[U](mapper: DoubleFunction[_ <: U]): Stream[U] = {

    val spl = new Spliterators.AbstractSpliterator[U](
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: Consumer[_ >: U]): Boolean =
        _spliter.tryAdvance((e: scala.Double) => action.accept(mapper(e)))
    }

    new StreamImpl[U](
      spl,
      _parallel,
      pipeline
        .asInstanceOf[ArrayDeque[StreamImpl[U]]]
    )
  }

  def max(): OptionalDouble = {
    commenceOperation()

    var max: scala.Double = jl.Double.NEGATIVE_INFINITY

    var exitEarly = false // leave loop after first NaN encountered, if any.

    def body(d: scala.Double): Unit = {
      if (d.isNaN()) {
        max = d
        exitEarly = true
      } else if (jl.Double.compare(max, d) < 0) { // sorts -0.0 lower than +0.0
        max = d
      }
    }

    val advanced = _spliter.tryAdvance((d: scala.Double) => body(d))

    if (!advanced) OptionalDouble.empty()
    else {
      while (!exitEarly &&
          _spliter.tryAdvance((d: scala.Double) => body(d))) { /* search */ }
      OptionalDouble.of(max)
    }
  }

  def min(): OptionalDouble = {
    commenceOperation()

    var min: scala.Double = jl.Double.POSITIVE_INFINITY

    var exitEarly = false // leave loop after first NaN encountered, if any.

    def body(d: scala.Double): Unit = {
      if (d.isNaN()) {
        min = d
        exitEarly = true
      } else if (jl.Double.compare(min, d) > 0) { // sorts -0.0 lower than +0.0
        min = d
      }
    }
    val advanced = _spliter.tryAdvance((d: scala.Double) => body(d))

    if (!advanced) OptionalDouble.empty()
    else {
      while (!exitEarly &&
          _spliter.tryAdvance((d: scala.Double) => body(d))) { /* search */ }
      OptionalDouble.of(min)
    }
  }

  def noneMatch(pred: DoublePredicate): Boolean = {
    // anyMatch() will call commenceOperation()
    !this.anyMatch(pred)
  }

  def peek(action: DoubleConsumer): DoubleStream = {
    commenceOperation()

    val peekAction = action

    val spl = new Spliterators.AbstractDoubleSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {

      def tryAdvance(action: DoubleConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Double) => {
          peekAction.accept(e)
          action.accept(e)
        })
    }

    new DoubleStreamImpl(spl, _parallel, pipeline)
  }

  def reduce(accumulator: DoubleBinaryOperator): OptionalDouble = {
    commenceOperation()

    var reduceOpt = OptionalDouble.empty()

    _spliter.tryAdvance((e: scala.Double) => reduceOpt = OptionalDouble.of(e))
    reduceOpt.ifPresent((first) => {
      var previous = first
      _spliter.forEachRemaining((e: scala.Double) =>
        previous = accumulator.applyAsDouble(previous, e)
      )
      reduceOpt = OptionalDouble.of(previous)
    })

    reduceOpt
  }

  def reduce(
      identity: scala.Double,
      accumulator: DoubleBinaryOperator
  ): scala.Double = {
    commenceOperation()

    var accumulated = identity

    _spliter.forEachRemaining((e: scala.Double) =>
      accumulated = accumulator.applyAsDouble(accumulated, e)
    )
    accumulated
  }

  def skip(n: Long): DoubleStream = {
    if (n < 0)
      throw new IllegalArgumentException(n.toString())

    commenceOperation() // JVM tests argument before operatedUpon or closed.

    var nSkipped = 0L

    while ((nSkipped < n)
        && (_spliter
          .tryAdvance((e: scala.Double) => nSkipped += 1L))) { /* skip */ }

    // Follow JVM practice; return new stream, not remainder of "this" stream.
    new DoubleStreamImpl(_spliter, _parallel, pipeline)
  }

  def sorted(): DoubleStream = {
    // No commenceOperation() here. This is an intermediate operation.

    class SortingSpliterOfDoubleSupplier(
        srcSpliter: Spliterator.OfDouble
    ) extends Supplier[Spliterator.OfDouble] {

      def get(): Spliterator.OfDouble = {
        val knownSize = _spliter.getExactSizeIfKnown()

        if (knownSize > Integer.MAX_VALUE) {
          throw new IllegalArgumentException(
            "Stream size exceeds max array size"
          )
        } else {
          /* Sufficiently large streams, with either known or unknown size may
           * eventually throw an OutOfMemoryError exception, same as JVM.
           *
           * sorting streams of unknown size is likely to be _slow_.
           */

          val buffer = toArray()

          Arrays.sort(buffer)

          val startingBits = _spliter.characteristics()
          val alwaysSetBits =
            Spliterator.SORTED | Spliterator.ORDERED |
              Spliterator.SIZED | Spliterator.SUBSIZED

          // Time & experience may show that additional bits need to be cleared
          val alwaysClearedBits = Spliterator.IMMUTABLE

          val newCharacteristics =
            (startingBits | alwaysSetBits) & ~alwaysClearedBits

          Spliterators.spliterator(buffer, newCharacteristics)
        }
      }
    }

    // Do the sort in the eventual terminal operation, not now.
    val spl = new SortingSpliterOfDoubleSupplier(_spliter)
    new DoubleStreamImpl(spl, 0, _parallel)
  }

  def sum(): scala.Double = {
    commenceOperation()

    var sum = 0.0

    _spliter.forEachRemaining((d: scala.Double) => sum += d)
    sum
  }

  def summaryStatistics(): DoubleSummaryStatistics = {
    commenceOperation()

    val stats = new DoubleSummaryStatistics()

    _spliter.forEachRemaining((d: scala.Double) => stats.accept(d))

    stats
  }

  def toArray(): Array[scala.Double] = {
    commenceOperation()

    val knownSize = _spliter.getExactSizeIfKnown()

    if (knownSize < 0) {
      val buffer = new ArrayList[scala.Double]()
      _spliter.forEachRemaining((e: scala.Double) => { buffer.add(e); () })

      // See if there is a more efficient way of doing this.
      val nElements = buffer.size()
      val primitiveDoubles = new Array[scala.Double](nElements)
      for (j <- 0 until nElements)
        primitiveDoubles(j) = buffer.get(j)

      primitiveDoubles
    } else {
      val primitiveDoubles = new Array[scala.Double](knownSize.toInt)
      var j = 0

      _spliter.forEachRemaining((e: scala.Double) => {
        primitiveDoubles(j) = e
        j += 1
      })
      primitiveDoubles
    }
  }

}

object DoubleStreamImpl {

  class Builder extends DoubleStream.Builder {
    private val buffer = new ArrayList[scala.Double]()
    private var built = false

    override def accept(t: scala.Double): Unit =
      if (built) StreamImpl.throwIllegalStateException()
      else buffer.add(t)

    override def build(): DoubleStream = {
      built = true
      // See if there is a more efficient way of doing this.
      val nElements = buffer.size()
      val primitiveDoubles = new Array[scala.Double](nElements)
      for (j <- 0 until nElements)
        primitiveDoubles(j) = buffer.get(j)

      val spliter = Arrays.spliterator(primitiveDoubles)

      new DoubleStreamImpl(spliter, parallel = false)
    }
  }

  /* This does not depend on Double. As IntStreamImpl and LongStreamImpl
   * are implemented, it should be moved to a common StreamHelpers.scala.
   * Let it prove itself before propagating.
   */
  private class CloseExceptionBuffer() {
    val buffer = new ArrayDeque[Exception]

    def add(e: Exception): Unit = buffer.addLast(e)

    def reportExceptions(): Unit = {
      if (!buffer.isEmpty()) {
        val firstException = buffer.removeFirst()

        buffer.forEach(e =>
          if (e != firstException)
            firstException.addSuppressed(e)
        )

        throw (firstException)
      }
    }
  }

  private class DoublePrimitiveCompoundSpliteratorFactory(
      spliter: Spliterator.OfDouble,
      mapper: DoubleFunction[_ <: DoubleStream],
      closeOnFirstTouch: Boolean
  ) {

    def get(): ju.Spliterator.OfDouble = {
      val substreams =
        new Spliterators.AbstractSpliterator[DoubleStream](
          Long.MaxValue,
          spliter.characteristics()
        ) {
          def tryAdvance(action: Consumer[_ >: DoubleStream]): Boolean = {
            spliter.tryAdvance((e: scala.Double) => action.accept(mapper(e)))
          }
        }

      new ju.Spliterator.OfDouble {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator.OfDouble =
          null.asInstanceOf[Spliterator.OfDouble]

        private var currentSpliter: ju.Spliterator.OfDouble =
          Spliterators.emptyDoubleSpliterator()

        var currentStream = Optional.empty[DoubleStreamImpl]()

        def tryAdvance(action: DoubleConsumer): Boolean = {
          var advanced = false
          var done = false

          while (!done) {
            if (currentSpliter.tryAdvance(action)) {
              /* JVM flatMap() closes substreams on first touch.
               * Stream.concat() does not.
               */

              if (closeOnFirstTouch)
                currentStream.get().close()

              advanced = true
              done = true
            } else {
              done = !substreams
                .tryAdvance((e: DoubleStream) =>
                  currentSpliter = {
                    val eOfDS = e.asInstanceOf[DoubleStreamImpl]
                    currentStream = Optional.of(eOfDS)

                    /* Tricky bit here!
                     *   Use internal _spliter and not public spliterator().
                     *   This method may have been called in a stream created
                     *   by concat(). Following JVM practice, concat()
                     *   set each of its input streams as "operated upon"
                     *   before returning its stream.
                     *
                     *   e.spliterator() checks _operatedUpon, which is true
                     *   in a stream from concat(), and throws.
                     *   Using _spliter skips that check and succeeds.
                     */

                    eOfDS._spliter
                  }
                )
            }
          }
          advanced
        }
      }
    }
  }

  private class DoubleConcatSpliteratorFactory(
      spliter: Spliterator[DoubleStream]
  ) {

    def get(): ju.Spliterator.OfDouble = {
      val substreams = spliter

      new ju.Spliterator.OfDouble {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator.OfDouble =
          null.asInstanceOf[Spliterator.OfDouble]

        private var currentSpliter: ju.Spliterator.OfDouble =
          Spliterators.emptyDoubleSpliterator()

        var currentStream = Optional.empty[DoubleStreamImpl]()

        def tryAdvance(action: DoubleConsumer): Boolean = {
          var advanced = false
          var done = false

          while (!done) {
            if (currentSpliter.tryAdvance(action)) {
              advanced = true
              done = true
            } else {
              done = !substreams
                .tryAdvance((e: DoubleStream) =>
                  currentSpliter = {
                    val eOfDS = e.asInstanceOf[DoubleStreamImpl]
                    currentStream = Optional.of(eOfDS)

                    /* Tricky bit here!
                     *   Use internal _spliter and not public spliterator().
                     *   This method may have been called in a stream created
                     *   by concat(). Following JVM practice, concat()
                     *   set each of its input streams as "operated upon"
                     *   before returning its stream.
                     *
                     *   e.spliterator() checks _operatedUpon, which is true
                     *   in a stream from concat(), and throws.
                     *   Using _spliter skips that check and succeeds.
                     */

                    eOfDS._spliter
                  }
                )
            }
          }
          advanced
        }
      }
    }
  }

  def concat(a: DoubleStream, b: DoubleStream): DoubleStream = {
    /* See ""Design Note" at corresponding place in StreamImpl.
     * This implementaton shares the same noted "features".
     */
    val aImpl = a.asInstanceOf[DoubleStreamImpl]
    val bImpl = b.asInstanceOf[DoubleStreamImpl]

    aImpl.commenceOperation()
    bImpl.commenceOperation()

    val arr = new Array[Object](2)
    arr(0) = aImpl
    arr(1) = bImpl

    val supplier =
      new DoubleStreamImpl.DoubleConcatSpliteratorFactory(
        Arrays.spliterator[DoubleStream](arr)
      )

    val pipelineA = aImpl.pipeline
    val pipelineB = bImpl.pipeline
    val pipelines = new ArrayDeque[DoubleStreamImpl](pipelineA)
    pipelines.addAll(pipelineB)

    new DoubleStreamImpl(supplier.get(), parallel = false, pipelines)
  }

}
