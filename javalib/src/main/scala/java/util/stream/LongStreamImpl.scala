package java.util.stream

import java.{lang => jl}
import java.{util => ju}
import java.util._
import java.util.function._

/* See "Design Note" at top of DoubleStream.scala for jl.Double & scala.Double
 * TL;DR - later is explicitly used where a primitive is desired.
 */

private[stream] class LongStreamImpl(
    val pipeline: ArrayDeque[LongStreamImpl]
) extends LongStream {
  var _spliterArg: Spliterator.OfLong = _
  var _supplier: Supplier[Spliterator.OfLong] = _
  var _parallel: Boolean = _ // Scaffolding for later improvements.
  var _characteristics: Int = 0

  lazy val _spliter: Spliterator.OfLong =
    if (_spliterArg != null) _spliterArg
    else _supplier.get()

  var _operatedUpon: Boolean = false
  var _closed: Boolean = false

  // avoid allocating an onCloseQueue just to check if it is empty.
  var onCloseQueueActive = false
  lazy val onCloseQueue = new ArrayDeque[Runnable]()

  pipeline.addLast(this)

  def this(
      spliterator: Spliterator.OfLong,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[LongStreamImpl])
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator.OfLong,
      parallel: Boolean,
      parent: LongStream
  ) = {
    this(parent.asInstanceOf[LongStreamImpl].pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator.OfLong,
      parallel: Boolean,
      pipeline: ArrayDeque[LongStreamImpl]
  ) = {
    this(pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      supplier: Supplier[Spliterator.OfLong],
      characteristics: Int,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[LongStreamImpl])
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
      val exceptionBuffer = new LongStreamImpl.CloseExceptionBuffer()
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

    val exceptionBuffer = new LongStreamImpl.CloseExceptionBuffer()

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

  def iterator(): ju.PrimitiveIterator.OfLong = {
    commenceOperation()
    Spliterators.iterator(_spliter)
  }

  def onClose(closeHandler: Runnable): LongStream = {
    // JVM appears to not set "operated upon" here.

    if (_closed)
      StreamImpl.throwIllegalStateException()

    // detects & throws on closeHandler == null
    onCloseQueue.addLast(closeHandler)

    if (!onCloseQueueActive)
      onCloseQueueActive = true

    this
  }

  def parallel(): LongStream = {
    if (!_parallel)
      _parallel = true
    this
  }

  def sequential(): LongStream = {
    if (_parallel)
      _parallel = false
    this
  }

  def spliterator(): Spliterator.OfLong = {
    commenceOperation()
    _spliter
  }

  def unordered(): LongStream = {
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

      val spl = new Spliterators.AbstractLongSpliterator(
        _spliter.estimateSize(),
        purifiedBits
      ) {
        def tryAdvance(action: LongConsumer): Boolean =
          _spliter.tryAdvance((e: scala.Long) => action.accept(e))
      }

      new LongStreamImpl(spl, _parallel, pipeline)
    }
  }

// Methods specified in interface Stream --------------------------------

  def allMatch(pred: LongPredicate): Boolean = {
    commenceOperation()

    // Be careful with documented "true" return for empty stream.
    var mismatchFound = false

    while (!mismatchFound &&
        _spliter.tryAdvance((e: scala.Long) =>
          if (!pred.test(e))
            mismatchFound = true
        )) { /* search */ }
    !mismatchFound
  }

  def anyMatch(pred: LongPredicate): Boolean = {
    commenceOperation()

    var matchFound = false

    while (!matchFound &&
        _spliter.tryAdvance((e: scala.Long) =>
          if (pred.test(e))
            matchFound = true
        )) { /* search */ }
    matchFound
  }

  def asDoubleStream(): DoubleStream =
    this.mapToDouble(e => e.toDouble)

  def average(): OptionalDouble = {
    commenceOperation()

    var count = 0L
    var sum = 0L

    _spliter.forEachRemaining((d: scala.Long) => { count += 1; sum += d })
    if (count == 0) OptionalDouble.empty()
    else OptionalDouble.of(sum.toDouble / count)
  }

  def boxed(): Stream[jl.Long] =
    this.mapToObj[jl.Long](d => scala.Long.box(d))

  def collect[R](
      supplier: Supplier[R],
      accumulator: ObjLongConsumer[R],
      combiner: BiConsumer[R, R]
  ): R = {
    commenceOperation()

    val result = supplier.get()

    _spliter.forEachRemaining((e: scala.Long) => accumulator.accept(result, e))

    result
  }

  def count(): scala.Long = {
    commenceOperation()

    var count = 0L
    _spliter.forEachRemaining((d: scala.Long) => count += 1)
    count
  }

  def distinct(): LongStream = {
    commenceOperation()

    val seenElements = new ju.HashSet[scala.Long]()

    // Some items may be dropped, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl =
      new Spliterators.AbstractLongSpliterator(
        estimatedSize,
        _spliter.characteristics()
      ) {
        def tryAdvance(action: LongConsumer): Boolean = {
          var success = false
          var done = false
          while (!done) {
            var advanced =
              _spliter.tryAdvance((e: scala.Long) => {
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

    new LongStreamImpl(spl, _parallel, pipeline)
  }

  def filter(pred: LongPredicate): LongStream = {
    commenceOperation()

    // Some items may be filtered out, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl = new Spliterators.AbstractLongSpliterator(
      estimatedSize,
      _spliter.characteristics()
    ) {
      def tryAdvance(action: LongConsumer): Boolean = {
        var success = false
        var done = false
        while (!done) {
          var advanced =
            _spliter.tryAdvance((e: scala.Long) => {
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

    new LongStreamImpl(spl, _parallel, pipeline)
  }

  /* delegating to findFirst() is an implementation ~~hack~~ expediency.
   * Probably near-optimal for sequential streams. Parallel streams may
   * offer better possibilities.
   */
  def findAny(): OptionalLong = {
    // commenceOperation() // findFirst will call, so do not do twice.
    findFirst()
  }

  def findFirst(): OptionalLong = {
    commenceOperation()
    var optional = OptionalLong.empty()
    _spliter.tryAdvance((e: scala.Long) => {
      optional = OptionalLong.of(e)
    })
    optional
  }

  def flatMap(
      mapper: LongFunction[_ <: LongStream]
  ): LongStream = {
    commenceOperation()

    val supplier =
      new LongStreamImpl.LongPrimitiveCompoundSpliteratorFactory(
        _spliter,
        mapper,
        closeOnFirstTouch = true
      )

    val coercedPriorStages = pipeline
      .asInstanceOf[ArrayDeque[LongStreamImpl]]

    new LongStreamImpl(supplier.get(), _parallel, coercedPriorStages)
  }

  def forEach(action: LongConsumer): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def forEachOrdered(action: LongConsumer): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def limit(maxSize: Long): LongStream = {

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

    val spl = new Spliterators.AbstractLongSpliterator(
      Long.MaxValue,
      newStreamCharacteristics
    ) {
      def tryAdvance(action: LongConsumer): Boolean =
        if (nSeen >= maxSize) false
        else {
          var advanced =
            _spliter.tryAdvance((e: scala.Long) => action.accept(e))
          nSeen =
            if (advanced) nSeen + 1
            else Long.MaxValue

          advanced
        }
    }

    new LongStreamImpl(spl, _parallel, pipeline)
  }

  def map(
      mapper: LongUnaryOperator
  ): LongStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractLongSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: LongConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Long) =>
          action.accept(mapper.applyAsLong(e))
        )
    }

    new LongStreamImpl(spl, _parallel, pipeline)
  }

  def mapToDouble(mapper: LongToDoubleFunction): DoubleStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractDoubleSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Long) =>
          action.accept(mapper.applyAsDouble(e))
        )
    }

    new DoubleStreamImpl(
      spl,
      _parallel,
      pipeline.asInstanceOf[ArrayDeque[DoubleStreamImpl]]
    )
  }

  def mapToInt(mapper: LongToIntFunction): IntStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractIntSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: IntConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Long) =>
          action.accept(mapper.applyAsInt(e))
        )
    }

    new IntStreamImpl(
      spl,
      _parallel,
      pipeline.asInstanceOf[ArrayDeque[IntStreamImpl]]
    )
  }

  def mapToObj[U](mapper: LongFunction[_ <: U]): Stream[U] = {

    val spl = new Spliterators.AbstractSpliterator[U](
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: Consumer[_ >: U]): Boolean =
        _spliter.tryAdvance((e: scala.Long) => action.accept(mapper(e)))
    }

    new StreamImpl[U](
      spl,
      _parallel,
      pipeline
        .asInstanceOf[ArrayDeque[StreamImpl[U]]]
    )
  }

  def max(): OptionalLong = {
    commenceOperation()

    var max: scala.Long = jl.Long.MIN_VALUE

    def body(d: scala.Long): Unit = {
      if (max < d)
        max = d
    }

    val advanced = _spliter.tryAdvance((d: scala.Long) => body(d))

    if (!advanced) OptionalLong.empty()
    else {
      while (_spliter.tryAdvance((d: scala.Long) => body(d))) { /* search */ }

      OptionalLong.of(max)
    }
  }

  def min(): OptionalLong = {
    commenceOperation()

    var min: scala.Long = jl.Long.MAX_VALUE

    def body(d: scala.Long): Unit = {
      if (min > d)
        min = d
    }

    val advanced = _spliter.tryAdvance((d: scala.Long) => body(d))

    if (!advanced) OptionalLong.empty()
    else {
      while (_spliter.tryAdvance((d: scala.Long) => body(d))) { /* search */ }

      OptionalLong.of(min)
    }
  }

  def noneMatch(pred: LongPredicate): Boolean = {
    // anyMatch() will call commenceOperation()
    !this.anyMatch(pred)
  }

  def peek(action: LongConsumer): LongStream = {
    commenceOperation()

    val peekAction = action

    val spl = new Spliterators.AbstractLongSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {

      def tryAdvance(action: LongConsumer): Boolean =
        _spliter.tryAdvance((e: scala.Long) => {
          peekAction.accept(e)
          action.accept(e)
        })
    }

    new LongStreamImpl(spl, _parallel, pipeline)
  }

  def reduce(accumulator: LongBinaryOperator): OptionalLong = {
    commenceOperation()

    var reduceOpt = OptionalLong.empty()

    _spliter.tryAdvance((e: scala.Long) => reduceOpt = OptionalLong.of(e))
    reduceOpt.ifPresent((first) => {
      var previous = first
      _spliter.forEachRemaining((e: scala.Long) =>
        previous = accumulator.applyAsLong(previous, e)
      )
      reduceOpt = OptionalLong.of(previous)
    })

    reduceOpt
  }

  def reduce(
      identity: scala.Long,
      accumulator: LongBinaryOperator
  ): scala.Long = {
    commenceOperation()

    var accumulated = identity

    _spliter.forEachRemaining((e: scala.Long) =>
      accumulated = accumulator.applyAsLong(accumulated, e)
    )
    accumulated
  }

  def skip(n: scala.Long): LongStream = {
    if (n < 0)
      throw new IllegalArgumentException(n.toString())

    commenceOperation() // JVM tests argument before operatedUpon or closed.

    val preSkipSize = _spliter.getExactSizeIfKnown()

    var nSkipped = 0L

    while ((nSkipped < n)
        && (_spliter
          .tryAdvance((e: scala.Long) => nSkipped += 1L))) { /* skip */ }

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

          new Spliterators.AbstractLongSpliterator(
            preSkipSize - nSkipped,
            _spliter.characteristics()
          ) {
            def tryAdvance(action: LongConsumer): Boolean =
              _spliter.tryAdvance((e: scala.Long) => action.accept(e))
          }
        }
      }

    // Follow JVM practice; return new stream, not remainder of "this" stream.
    new LongStreamImpl(spl, _parallel, pipeline)
  }

  def sorted(): LongStream = {
    // No commenceOperation() here. This is an intermediate operation.

    class SortingSpliterOfLongSupplier(
        srcSpliter: Spliterator.OfLong
    ) extends Supplier[Spliterator.OfLong] {

      def get(): Spliterator.OfLong = {
        val knownSize = _spliter.getExactSizeIfKnown()

        if (knownSize > jl.Integer.MAX_VALUE) {
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
    val spl = new SortingSpliterOfLongSupplier(_spliter)
    new LongStreamImpl(spl, 0, _parallel)
  }

  def sum(): scala.Long = {
    commenceOperation()

    var sum = 0L

    _spliter.forEachRemaining((d: scala.Long) => sum += d)
    sum
  }

  def summaryStatistics(): LongSummaryStatistics = {
    commenceOperation()

    val stats = new LongSummaryStatistics()

    _spliter.forEachRemaining((d: scala.Long) => stats.accept(d))

    stats
  }

  def toArray(): Array[scala.Long] = {
    commenceOperation()

    val knownSize = _spliter.getExactSizeIfKnown()

    if (knownSize < 0) {
      val buffer = new ArrayList[scala.Long]()
      _spliter.forEachRemaining((e: scala.Long) => { buffer.add(e); () })

      // See if there is a more efficient way of doing this.
      val nElements = buffer.size()
      val primitiveLongs = new Array[scala.Long](nElements)
      for (j <- 0 until nElements)
        primitiveLongs(j) = buffer.get(j)

      primitiveLongs
    } else {
      val primitiveLongs = new Array[scala.Long](knownSize.toInt)
      var j = 0

      _spliter.forEachRemaining((e: scala.Long) => {
        primitiveLongs(j) = e
        j += 1
      })
      primitiveLongs
    }
  }

}

object LongStreamImpl {

  class Builder extends LongStream.Builder {
    private val buffer = new ArrayList[scala.Long]()
    private var built = false

    override def accept(t: scala.Long): Unit =
      if (built) StreamImpl.throwIllegalStateException()
      else buffer.add(t)

    override def build(): LongStream = {
      built = true
      // See if there is a more efficient way of doing this.
      val nElements = buffer.size()
      val primitiveLongs = new Array[scala.Long](nElements)
      for (j <- 0 until nElements)
        primitiveLongs(j) = buffer.get(j)

      val spliter = Arrays.spliterator(primitiveLongs)

      new LongStreamImpl(spliter, parallel = false)
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

  private class LongPrimitiveCompoundSpliteratorFactory(
      spliter: Spliterator.OfLong,
      mapper: LongFunction[_ <: LongStream],
      closeOnFirstTouch: Boolean
  ) {

    def get(): ju.Spliterator.OfLong = {
      val substreams =
        new Spliterators.AbstractSpliterator[LongStream](
          Long.MaxValue,
          spliter.characteristics()
        ) {
          def tryAdvance(action: Consumer[_ >: LongStream]): Boolean = {
            spliter.tryAdvance((e: scala.Long) => action.accept(mapper(e)))
          }
        }

      new ju.Spliterator.OfLong {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator.OfLong =
          null.asInstanceOf[Spliterator.OfLong]

        private var currentSpliter: ju.Spliterator.OfLong =
          Spliterators.emptyLongSpliterator()

        var currentStream = Optional.empty[LongStreamImpl]()

        def tryAdvance(action: LongConsumer): Boolean = {
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
                .tryAdvance((e: LongStream) =>
                  currentSpliter = {
                    val eOfDS = e.asInstanceOf[LongStreamImpl]
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
      spliter: Spliterator[LongStream]
  ) {

    def get(): ju.Spliterator.OfLong = {
      val substreams = spliter

      new ju.Spliterator.OfLong {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator.OfLong =
          null.asInstanceOf[Spliterator.OfLong]

        private var currentSpliter: ju.Spliterator.OfLong =
          Spliterators.emptyLongSpliterator()

        var currentStream = Optional.empty[LongStreamImpl]()

        def tryAdvance(action: LongConsumer): Boolean = {
          var advanced = false
          var done = false

          while (!done) {
            if (currentSpliter.tryAdvance(action)) {
              advanced = true
              done = true
            } else {
              done = !substreams
                .tryAdvance((e: LongStream) =>
                  currentSpliter = {
                    val eOfDS = e.asInstanceOf[LongStreamImpl]
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

  def concat(a: LongStream, b: LongStream): LongStream = {
    /* See ""Design Note" at corresponding place in StreamImpl.
     * This implementaton shares the same noted "features".
     */
    val aImpl = a.asInstanceOf[LongStreamImpl]
    val bImpl = b.asInstanceOf[LongStreamImpl]

    aImpl.commenceOperation()
    bImpl.commenceOperation()

    val arr = new Array[Object](2)
    arr(0) = aImpl
    arr(1) = bImpl

    val supplier =
      new LongStreamImpl.IntConcatSpliteratorFactory(
        Arrays.spliterator[LongStream](arr)
      )

    val pipelineA = aImpl.pipeline
    val pipelineB = bImpl.pipeline
    val pipelines = new ArrayDeque[LongStreamImpl](pipelineA)
    pipelines.addAll(pipelineB)

    new LongStreamImpl(supplier.get(), parallel = false, pipelines)
  }

}
