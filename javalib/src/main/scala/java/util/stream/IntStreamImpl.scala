package java.util.stream

import java.{lang => jl}
import java.{util => ju}
import java.util._
import java.util.function._

/* See "Design Note" at top of DoubleStream.scala for jl.Double & scala.Double
 * TL;DR - later is explicitly used where a primitive is desired.
 */

private[stream] class IntStreamImpl(
    val pipeline: ArrayDeque[IntStreamImpl]
) extends IntStream {
  var _spliterArg: Spliterator.OfInt = _
  var _supplier: Supplier[Spliterator.OfInt] = _
  var _parallel: Boolean = _ // Scaffolding for later improvements.
  var _characteristics: Int = 0

  lazy val _spliter: Spliterator.OfInt =
    if (_spliterArg != null) _spliterArg
    else _supplier.get()

  var _operatedUpon: Boolean = false
  var _closed: Boolean = false

  // avoid allocating an onCloseQueue just to check if it is empty.
  var onCloseQueueActive = false
  lazy val onCloseQueue = new ArrayDeque[Runnable]()

  pipeline.addLast(this)

  def this(
      spliterator: Spliterator.OfInt,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[IntStreamImpl])
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator.OfInt,
      parallel: Boolean,
      parent: IntStream
  ) = {
    this(parent.asInstanceOf[IntStreamImpl].pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator.OfInt,
      parallel: Boolean,
      pipeline: ArrayDeque[IntStreamImpl]
  ) = {
    this(pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      supplier: Supplier[Spliterator.OfInt],
      characteristics: Int,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[IntStreamImpl])
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
      val exceptionBuffer = new IntStreamImpl.CloseExceptionBuffer()
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

    val exceptionBuffer = new IntStreamImpl.CloseExceptionBuffer()

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

  def iterator(): ju.PrimitiveIterator.OfInt = {
    commenceOperation()
    Spliterators.iterator(_spliter)
  }

  def onClose(closeHandler: Runnable): IntStream = {
    // JVM appears to not set "operated upon" here.

    if (_closed)
      StreamImpl.throwIllegalStateException()

    // detects & throws on closeHandler == null
    onCloseQueue.addLast(closeHandler)

    if (!onCloseQueueActive)
      onCloseQueueActive = true

    this
  }

  def parallel(): IntStream = {
    if (!_parallel)
      _parallel = true
    this
  }

  def sequential(): IntStream = {
    if (_parallel)
      _parallel = false
    this
  }

  def spliterator(): Spliterator.OfInt = {
    commenceOperation()
    _spliter
  }

  def unordered(): IntStream = {
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

      val spl = new Spliterators.AbstractIntSpliterator(
        _spliter.estimateSize(),
        purifiedBits
      ) {
        def tryAdvance(action: IntConsumer): Boolean =
          _spliter.tryAdvance((e: scala.Int) => action.accept(e))
      }

      new IntStreamImpl(spl, _parallel, pipeline)
    }
  }

// Methods specified in interface Stream --------------------------------

  def allMatch(pred: IntPredicate): Boolean = {
    commenceOperation()

    // Be careful with documented "true" return for empty stream.
    var mismatchFound = false

    while (!mismatchFound &&
        _spliter.tryAdvance((e: scala.Int) =>
          if (!pred.test(e))
            mismatchFound = true
        )) { /* search */ }
    !mismatchFound
  }

  def anyMatch(pred: IntPredicate): Boolean = {
    commenceOperation()

    var matchFound = false

    while (!matchFound &&
        _spliter.tryAdvance((e: scala.Int) =>
          if (pred.test(e))
            matchFound = true
        )) { /* search */ }
    matchFound
  }

  def asDoubleStream(): DoubleStream =
    this.mapToDouble(e => e.toDouble)

  def asLongStream(): LongStream =
    this.mapToLong(e => e.toLong)

  def average(): OptionalDouble = {
    commenceOperation()

    var count = 0
    var sum = 0

    _spliter.forEachRemaining((d: scala.Int) => { count += 1; sum += d })
    if (count == 0) OptionalDouble.empty()
    else OptionalDouble.of(sum.toDouble / count)
  }

  def boxed(): Stream[jl.Integer] =
    this.mapToObj[jl.Integer](d => scala.Int.box(d))

  def collect[R](
      supplier: Supplier[R],
      accumulator: ObjIntConsumer[R],
      combiner: BiConsumer[R, R]
  ): R = {
    commenceOperation()

    val result = supplier.get()

    _spliter.forEachRemaining((e: scala.Int) => accumulator.accept(result, e))

    result
  }

  def count(): scala.Long = {
    commenceOperation()

    var count = 0L
    _spliter.forEachRemaining((d: scala.Int) => count += 1)
    count
  }

  def distinct(): IntStream = {
    commenceOperation()

    val seenElements = new ju.HashSet[scala.Int]()

    // Some items may be dropped, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl =
      new Spliterators.AbstractIntSpliterator(
        estimatedSize,
        _spliter.characteristics()
      ) {
        def tryAdvance(action: IntConsumer): Boolean = {
          var success = false
          var done = false
          while (!done) {
            var advanced =
              _spliter.tryAdvance((e: scala.Int) => {
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

    new IntStreamImpl(spl, _parallel, pipeline)
  }

  def filter(pred: IntPredicate): IntStream = {
    commenceOperation()

    // Some items may be filtered out, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl = new Spliterators.AbstractIntSpliterator(
      estimatedSize,
      _spliter.characteristics()
    ) {
      def tryAdvance(action: IntConsumer): Boolean = {
        var success = false
        var done = false
        while (!done) {
          var advanced =
            _spliter.tryAdvance((e: scala.Int) => {
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

    new IntStreamImpl(spl, _parallel, pipeline)
  }

  /* delegating to findFirst() is an implementation ~~hack~~ expediency.
   * Probably near-optimal for sequential streams. Parallel streams may
   * offer better possibilities.
   */
  def findAny(): OptionalInt = {
    // commenceOperation() // findFirst will call, so do not do twice.
    findFirst()
  }

  def findFirst(): OptionalInt = {
    commenceOperation()
    var optional = OptionalInt.empty()
    _spliter.tryAdvance((e: scala.Int) => {
      optional = OptionalInt.of(e)
    })
    optional
  }

  def flatMap(
      mapper: IntFunction[_ <: IntStream]
  ): IntStream = {
    commenceOperation()

    val supplier =
      new IntStreamImpl.IntPrimitiveCompoundSpliteratorFactory(
        _spliter,
        mapper,
        closeOnFirstTouch = true
      )

    val coercedPriorStages = pipeline
      .asInstanceOf[ArrayDeque[IntStreamImpl]]

    new IntStreamImpl(supplier.get(), _parallel, coercedPriorStages)
  }

  def forEach(action: IntConsumer): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def forEachOrdered(action: IntConsumer): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def limit(maxSize: Long): IntStream = {

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

    val spl = new Spliterators.AbstractIntSpliterator(
      Long.MaxValue,
      newStreamCharacteristics
    ) {
      def tryAdvance(action: IntConsumer): Boolean =
        if (nSeen >= maxSize) false
        else {
          var advanced =
            _spliter.tryAdvance((e: scala.Int) => action.accept(e))
          nSeen =
            if (advanced) nSeen + 1
            else Long.MaxValue

          advanced
        }
    }

    new IntStreamImpl(spl, _parallel, pipeline)
  }

  def map(
      mapper: IntUnaryOperator
  ): IntStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractIntSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: IntConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Int) =>
          action.accept(mapper.applyAsInt(e))
        )
    }

    new IntStreamImpl(spl, _parallel, pipeline)
  }

  def mapToDouble(mapper: IntToDoubleFunction): DoubleStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractDoubleSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Int) =>
          action.accept(mapper.applyAsDouble(e))
        )
    }

    new DoubleStreamImpl(
      spl,
      _parallel,
      pipeline.asInstanceOf[ArrayDeque[DoubleStreamImpl]]
    )
  }

  def mapToLong(mapper: IntToLongFunction): LongStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractLongSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: LongConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Int) =>
          action.accept(mapper.applyAsLong(e))
        )
    }

    new LongStreamImpl(
      spl,
      _parallel,
      pipeline.asInstanceOf[ArrayDeque[LongStreamImpl]]
    )
  }

  def mapToObj[U](mapper: IntFunction[_ <: U]): Stream[U] = {

    val spl = new Spliterators.AbstractSpliterator[U](
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: Consumer[_ >: U]): Boolean =
        _spliter.tryAdvance((e: scala.Int) => action.accept(mapper(e)))
    }

    new StreamImpl[U](
      spl,
      _parallel,
      pipeline
        .asInstanceOf[ArrayDeque[StreamImpl[U]]]
    )
  }

  def max(): OptionalInt = {
    commenceOperation()

    var max: scala.Int = jl.Integer.MIN_VALUE

    def body(d: scala.Int): Unit = {
      if (max < d)
        max = d
    }

    val advanced = _spliter.tryAdvance((d: scala.Int) => body(d))

    if (!advanced) OptionalInt.empty()
    else {
      while (_spliter.tryAdvance((d: scala.Int) => body(d))) { /* search */ }

      OptionalInt.of(max)
    }
  }

  def min(): OptionalInt = {
    commenceOperation()

    var min: scala.Int = jl.Integer.MAX_VALUE

    def body(d: scala.Int): Unit = {
      if (min > d)
        min = d
    }

    val advanced = _spliter.tryAdvance((d: scala.Int) => body(d))

    if (!advanced) OptionalInt.empty()
    else {
      while (_spliter.tryAdvance((d: scala.Int) => body(d))) { /* search */ }

      OptionalInt.of(min)
    }
  }

  def noneMatch(pred: IntPredicate): Boolean = {
    // anyMatch() will call commenceOperation()
    !this.anyMatch(pred)
  }

  def peek(action: IntConsumer): IntStream = {
    commenceOperation()

    val peekAction = action

    val spl = new Spliterators.AbstractIntSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {

      def tryAdvance(action: IntConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Int) => {
          peekAction.accept(e)
          action.accept(e)
        })
    }

    new IntStreamImpl(spl, _parallel, pipeline)
  }

  def reduce(accumulator: IntBinaryOperator): OptionalInt = {
    commenceOperation()

    var reduceOpt = OptionalInt.empty()

    _spliter.tryAdvance((e: scala.Int) => reduceOpt = OptionalInt.of(e))
    reduceOpt.ifPresent((first) => {
      var previous = first
      _spliter.forEachRemaining((e: scala.Int) =>
        previous = accumulator.applyAsInt(previous, e)
      )
      reduceOpt = OptionalInt.of(previous)
    })

    reduceOpt
  }

  def reduce(
      identity: scala.Int,
      accumulator: IntBinaryOperator
  ): scala.Int = {
    commenceOperation()

    var accumulated = identity

    _spliter.forEachRemaining((e: scala.Int) =>
      accumulated = accumulator.applyAsInt(accumulated, e)
    )
    accumulated
  }

  def skip(n: scala.Long): IntStream = {
    if (n < 0)
      throw new IllegalArgumentException(n.toString())

    commenceOperation() // JVM tests argument before operatedUpon or closed.

    val preSkipSize = _spliter.getExactSizeIfKnown()

    var nSkipped = 0L

    while ((nSkipped < n)
        && (_spliter
          .tryAdvance((e: scala.Int) => nSkipped += 1L))) { /* skip */ }

    val spl =
      if (preSkipSize == -1) _spliter // Not SIZED at beginning
      else {
        val postSkipSize = _spliter.getExactSizeIfKnown()
        if (postSkipSize != preSkipSize) {
          _spliter // save allocation, use tryAdvance's bookkeeping
        } else {
          /* Current stream is SIZED and its tryAdvance does not do
           * bookkeeping. Give downstream an accurate exactSize().
           */

          new Spliterators.AbstractIntSpliterator(
            preSkipSize - nSkipped,
            _spliter.characteristics()
          ) {
            def tryAdvance(action: IntConsumer): Boolean =
              _spliter.tryAdvance((e: scala.Int) => action.accept(e))
          }
        }
      }

    // Follow JVM practice; return new stream, not remainder of "this" stream.
    new IntStreamImpl(spl, _parallel, pipeline)
  }

  def sorted(): IntStream = {
    // No commenceOperation() here. This is an intermediate operation.

    class SortingSpliterOfIntSupplier(
        srcSpliter: Spliterator.OfInt
    ) extends Supplier[Spliterator.OfInt] {

      def get(): Spliterator.OfInt = {
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
    val spl = new SortingSpliterOfIntSupplier(_spliter)
    new IntStreamImpl(spl, 0, _parallel)
  }

  def sum(): scala.Int = {
    commenceOperation()

    var sum = 0

    _spliter.forEachRemaining((d: scala.Int) => sum += d)
    sum
  }

  def summaryStatistics(): IntSummaryStatistics = {
    commenceOperation()

    val stats = new IntSummaryStatistics()

    _spliter.forEachRemaining((d: scala.Int) => stats.accept(d))

    stats
  }

  def toArray(): Array[scala.Int] = {
    commenceOperation()

    val knownSize = _spliter.getExactSizeIfKnown()

    if (knownSize < 0) {
      val buffer = new ArrayList[scala.Int]()
      _spliter.forEachRemaining((e: scala.Int) => { buffer.add(e); () })

      // See if there is a more efficient way of doing this.
      val nElements = buffer.size()
      val primitiveInts = new Array[scala.Int](nElements)
      for (j <- 0 until nElements)
        primitiveInts(j) = buffer.get(j)

      primitiveInts
    } else {
      val primitiveInts = new Array[scala.Int](knownSize.toInt)
      var j = 0

      _spliter.forEachRemaining((e: scala.Int) => {
        primitiveInts(j) = e
        j += 1
      })
      primitiveInts
    }
  }

}

object IntStreamImpl {

  class Builder extends IntStream.Builder {
    private val buffer = new ArrayList[scala.Int]()
    private var built = false

    override def accept(t: scala.Int): Unit =
      if (built) StreamImpl.throwIllegalStateException()
      else buffer.add(t)

    override def build(): IntStream = {
      built = true
      // See if there is a more efficient way of doing this.
      val nElements = buffer.size()
      val primitiveInts = new Array[scala.Int](nElements)
      for (j <- 0 until nElements)
        primitiveInts(j) = buffer.get(j)

      val spliter = Arrays.spliterator(primitiveInts)

      new IntStreamImpl(spliter, parallel = false)
    }
  }

  /* This does not depend on Int. As LongStreamImpl
   * is implemented, it should be moved to a common StreamHelpers.scala.
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

  private class IntPrimitiveCompoundSpliteratorFactory(
      spliter: Spliterator.OfInt,
      mapper: IntFunction[_ <: IntStream],
      closeOnFirstTouch: Boolean
  ) {

    def get(): ju.Spliterator.OfInt = {
      val substreams =
        new Spliterators.AbstractSpliterator[IntStream](
          Long.MaxValue,
          spliter.characteristics()
        ) {
          def tryAdvance(action: Consumer[_ >: IntStream]): Boolean = {
            spliter.tryAdvance((e: scala.Int) => action.accept(mapper(e)))
          }
        }

      new ju.Spliterator.OfInt {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator.OfInt =
          null.asInstanceOf[Spliterator.OfInt]

        private var currentSpliter: ju.Spliterator.OfInt =
          Spliterators.emptyIntSpliterator()

        var currentStream = Optional.empty[IntStreamImpl]()

        def tryAdvance(action: IntConsumer): Boolean = {
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
                .tryAdvance((e: IntStream) =>
                  currentSpliter = {
                    val eOfDS = e.asInstanceOf[IntStreamImpl]
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

  private class IntConcatSpliteratorFactory(
      spliter: Spliterator[IntStream]
  ) {

    def get(): ju.Spliterator.OfInt = {
      val substreams = spliter

      new ju.Spliterator.OfInt {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator.OfInt =
          null.asInstanceOf[Spliterator.OfInt]

        private var currentSpliter: ju.Spliterator.OfInt =
          Spliterators.emptyIntSpliterator()

        var currentStream = Optional.empty[IntStreamImpl]()

        def tryAdvance(action: IntConsumer): Boolean = {
          var advanced = false
          var done = false

          while (!done) {
            if (currentSpliter.tryAdvance(action)) {
              advanced = true
              done = true
            } else {
              done = !substreams
                .tryAdvance((e: IntStream) =>
                  currentSpliter = {
                    val eOfDS = e.asInstanceOf[IntStreamImpl]
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

  def concat(a: IntStream, b: IntStream): IntStream = {
    /* See ""Design Note" at corresponding place in StreamImpl.
     * This implementaton shares the same noted "features".
     */
    val aImpl = a.asInstanceOf[IntStreamImpl]
    val bImpl = b.asInstanceOf[IntStreamImpl]

    aImpl.commenceOperation()
    bImpl.commenceOperation()

    val arr = new Array[Object](2)
    arr(0) = aImpl
    arr(1) = bImpl

    val supplier =
      new IntStreamImpl.IntConcatSpliteratorFactory(
        Arrays.spliterator[IntStream](arr)
      )

    val pipelineA = aImpl.pipeline
    val pipelineB = bImpl.pipeline
    val pipelines = new ArrayDeque[IntStreamImpl](pipelineA)
    pipelines.addAll(pipelineB)

    new IntStreamImpl(supplier.get(), parallel = false, pipelines)
  }

}
