package java.util.stream

import java.{util => ju}
import java.util._
import java.util.function._
import java.util.stream.Collector._

private[stream] class StreamImpl[T](
    val pipeline: ArrayDeque[StreamImpl[T]]
) extends Stream[T] {
  var _spliterArg: Spliterator[T] = _
  var _supplier: Supplier[Spliterator[T]] = _
  var _parallel: Boolean = _ // Scaffolding for later improvements.
  var _characteristics: Int = 0

  lazy val _spliter: Spliterator[T] =
    if (_spliterArg != null) _spliterArg
    else _supplier.get()

  var _operatedUpon: Boolean = false
  var _closed: Boolean = false

  // avoid allocating an onCloseQueue just to check if it is empty.
  var onCloseQueueActive = false
  lazy val onCloseQueue = new ArrayDeque[Runnable]()

  pipeline.addLast(this)

  def this(
      spliterator: Spliterator[T],
      parallel: Boolean
  ) = {
    this(new ArrayDeque[StreamImpl[T]])
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator[T],
      parallel: Boolean,
      parent: Stream[_ <: T]
  ) = {
    this(parent.asInstanceOf[StreamImpl[T]].pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      spliterator: Spliterator[T],
      parallel: Boolean,
      pipeline: ArrayDeque[StreamImpl[T]]
  ) = {
    this(pipeline)
    _spliterArg = spliterator
    _parallel = parallel
  }

  def this(
      supplier: Supplier[Spliterator[T]],
      characteristics: Int,
      parallel: Boolean
  ) = {
    this(new ArrayDeque[StreamImpl[T]])
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
      val exceptionBuffer = new StreamImpl.CloseExceptionBuffer()
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

    val exceptionBuffer = new StreamImpl.CloseExceptionBuffer()

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

  def iterator(): ju.Iterator[T] = {
    commenceOperation()
    Spliterators.iterator[T](_spliter)
  }

  def onClose(closeHandler: Runnable): Stream[T] = {
    // JVM appears to not set "operated upon" here.

    if (_closed)
      StreamImpl.throwIllegalStateException()

    // detects & throws on closeHandler == null
    onCloseQueue.addLast(closeHandler)

    if (!onCloseQueueActive)
      onCloseQueueActive = true

    this
  }

  def parallel(): Stream[T] = {
    if (!_parallel)
      _parallel = true
    this
  }

  def sequential(): Stream[T] = {
    if (_parallel)
      _parallel = false
    this
  }

  def spliterator(): Spliterator[_ <: T] = {
    commenceOperation()
    _spliter
  }

  def unordered(): Stream[T] = {
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

      val spl = new Spliterators.AbstractSpliterator[T](
        _spliter.estimateSize(),
        purifiedBits
      ) {
        def tryAdvance(action: Consumer[_ >: T]): Boolean =
          _spliter.tryAdvance((e) => action.accept(e))
      }

      new StreamImpl[T](spl, _parallel, pipeline)
    }
  }

// Methods specified in interface Stream --------------------------------

  def allMatch(pred: Predicate[_ >: T]): Boolean = {
    commenceOperation()

    // Be careful with documented "true" return for empty stream.
    var mismatchFound = false

    while (!mismatchFound &&
        _spliter.tryAdvance((e: T) =>
          if (!pred.test(e))
            mismatchFound = true
        )) { /* search */ }
    !mismatchFound
  }

  def anyMatch(pred: Predicate[_ >: T]): Boolean = {
    commenceOperation()

    var matchFound = false

    while (!matchFound &&
        _spliter.tryAdvance((e: T) =>
          if (pred.test(e))
            matchFound = true
        )) { /* search */ }
    matchFound
  }

  def collect[R, A](collector: Collector[_ >: T, A, R]): R = {
    // Loosely following the example in the JDK 8 stream.Collector doc.
    commenceOperation()

    val supplier = collector.supplier()
    val accumulator = collector.accumulator()
    // combiner unused in this sequential-only implementation
    val finisher = collector.finisher()

    val workInProgress = supplier.get()

    _spliter.forEachRemaining((e) => accumulator.accept(workInProgress, e))

    /* This check is described in the JVM docs. Seems more costly to
     * create & check the Characteristics set than to straight out
     * execute an identity finisher().
     * Go figure, it made sense to the JVM doc writers.
     */
    if (collector.characteristics().contains(Characteristics.IDENTITY_FINISH))
      workInProgress.asInstanceOf[R]
    else
      finisher.apply(workInProgress)
  }

  def collect[R](
      supplier: Supplier[R],
      accumulator: BiConsumer[R, _ >: T],
      combiner: BiConsumer[R, R]
  ): R = {
    commenceOperation()

    val result = supplier.get()

    _spliter.forEachRemaining((e) => accumulator.accept(result, e))

    result
  }

  def count(): Long = {
    commenceOperation()

    var count = 0L
    _spliter.forEachRemaining(e => count += 1)
    count
  }

  def distinct(): Stream[T] = {
    commenceOperation()

    val seenElements = new ju.HashSet[T]()

    // Some items may be dropped, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl =
      new Spliterators.AbstractSpliterator[T](
        estimatedSize,
        _spliter.characteristics()
      ) {
        def tryAdvance(action: Consumer[_ >: T]): Boolean = {
          var success = false
          var done = false
          while (!done) {
            var advanced =
              _spliter.tryAdvance((e) => {
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

    new StreamImpl[T](spl, _parallel, pipeline)
  }

  def filter(pred: Predicate[_ >: T]): Stream[T] = {
    commenceOperation()

    // Some items may be filtered out, so the estimated size is a high bound.
    val estimatedSize = _spliter.estimateSize()

    val spl = new Spliterators.AbstractSpliterator[T](
      estimatedSize,
      _spliter.characteristics()
    ) {
      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        var success = false
        var done = false
        while (!done) {
          var advanced =
            _spliter.tryAdvance((e) => {
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
    new StreamImpl[T](spl, _parallel, pipeline)
  }

  /* delegating to findFirst() is an implementation ~~hack~~ expediency.
   * Probably near-optimal for sequential streams. Parallel streams may
   * offer better possibilities.
   */
  def findAny(): Optional[T] = {
    // commenceOperation() // findFirst will call, so do not do twice.
    findFirst()
  }

  def findFirst(): Optional[T] = {
    commenceOperation()
    var optional = Optional.empty[T]()
    _spliter.tryAdvance((e) => { optional = Optional.of(e.asInstanceOf[T]) })
    optional
  }

  def flatMap[R](
      mapper: Function[_ >: T, _ <: Stream[_ <: R]]
  ): Stream[R] = {
    commenceOperation()

    val csf = new StreamImpl.CompoundSpliteratorFactory[T, R](
      _spliter,
      mapper,
      closeOnFirstTouch = true
    )

    val coercedPriorStages = pipeline
      .asInstanceOf[ArrayDeque[StreamImpl[R]]]

    new StreamImpl[R](csf.get(), _parallel, coercedPriorStages)
  }

  def flatMapToDouble(
      mapper: Function[_ >: T, _ <: DoubleStream]
  ): DoubleStream = {
    commenceOperation()

    val supplier =
      new StreamImpl.DoublePrimitiveCompoundSpliteratorFactory[T](
        _spliter,
        mapper,
        closeOnFirstTouch = true
      )

    val coercedPriorStages = pipeline
      .asInstanceOf[ArrayDeque[DoubleStreamImpl]]

    new DoubleStreamImpl(supplier.get(), _parallel, coercedPriorStages)
  }

  def flatMapToInt(
      mapper: Function[_ >: T, _ <: IntStream]
  ): IntStream = {
    commenceOperation()

    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def flatMapToLong(
      mapper: Function[_ >: T, _ <: LongStream]
  ): LongStream = {
    commenceOperation()

    throw new UnsupportedOperationException("Not Yet Implemented")
  }

  def forEach(action: Consumer[_ >: T]): Unit = {
    _spliter.forEachRemaining(action)
  }

  def forEachOrdered(action: Consumer[_ >: T]): Unit = {
    commenceOperation()
    _spliter.forEachRemaining(action)
  }

  def limit(maxSize: Long): Stream[T] = {

    /* Important:
     * See Issue #3309 for discussion of size & characteristics
     * in JVM 17 (and possibly as early as JVM 12) for parallel ORDERED
     * streams.  The behavior implemented here is Java 8 and at least Java 11.
     *
     * If you are reading this block with more than a passing interest,
     * prepare yourself for not having a good day, the muck is deep.
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

    val spl = new Spliterators.AbstractSpliterator[T](
      Long.MaxValue,
      newStreamCharacteristics
    ) {
      def tryAdvance(action: Consumer[_ >: T]): Boolean =
        if (nSeen >= maxSize) false
        else {
          var advanced =
            _spliter.tryAdvance((e) => action.accept(e))
          nSeen =
            if (advanced) nSeen + 1
            else Long.MaxValue

          advanced
        }
    }

    new StreamImpl[T](spl, _parallel, pipeline)
  }

  def map[R](
      mapper: Function[_ >: T, _ <: R]
  ): Stream[R] = {
    commenceOperation()

    val spl = new Spliterators.AbstractSpliterator[R](
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: Consumer[_ >: R]): Boolean =
        _spliter.tryAdvance((e) => action.accept(mapper(e)))
    }

    /* Ugly type handling! but part of map()'s job is to mung types.
     * Type erasure is what makes this work, once one lies to the compiler
     * about the types involved.
     */
    new StreamImpl[T](
      spl.asInstanceOf[Spliterator[T]],
      _parallel,
      pipeline
    )
      .asInstanceOf[Stream[R]]
  }

  def mapToDouble(mapper: ToDoubleFunction[_ >: T]): DoubleStream = {
    commenceOperation()

    val spl = new Spliterators.AbstractDoubleSpliterator(
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {
      def tryAdvance(action: DoubleConsumer): Boolean =
        _spliter.tryAdvance((e: T) => action.accept(mapper.applyAsDouble(e)))
    }

    val coercedPriorStages = pipeline
      .asInstanceOf[ArrayDeque[DoubleStreamImpl]]

    new DoubleStreamImpl(
      spl,
      _parallel,
      coercedPriorStages
    )
      .asInstanceOf[DoubleStream]

  }

  def mapToInt(mapper: ToIntFunction[_ >: T]): IntStream =
    throw new UnsupportedOperationException("Not Yet Implemented")

  def mapToLong(mapper: ToLongFunction[_ >: T]): LongStream =
    throw new UnsupportedOperationException("Not Yet Implemented")

  def max(comparator: Comparator[_ >: T]): Optional[T] = {
    commenceOperation()

    var maxOpt = Optional.empty[T]()

    _spliter.tryAdvance((e) => maxOpt = Optional.of(e.asInstanceOf[T]))

    maxOpt.ifPresent((first) => {
      var max = first
      _spliter.forEachRemaining((e) =>
        if (comparator.compare(max, e.asInstanceOf[T]) < 0)
          max = e.asInstanceOf[T]
      )
      maxOpt = Optional.of(max)
    })

    maxOpt
  }

  def min(comparator: Comparator[_ >: T]): Optional[T] = {
    commenceOperation()

    var minOpt = Optional.empty[T]()

    _spliter.tryAdvance((e) => minOpt = Optional.of(e.asInstanceOf[T]))

    minOpt.ifPresent((first) => {
      var min = first
      _spliter.forEachRemaining((e) =>
        if (comparator.compare(min, e.asInstanceOf[T]) > 0)
          min = e.asInstanceOf[T]
      )
      minOpt = Optional.of(min)
    })

    minOpt
  }

  def noneMatch(pred: Predicate[_ >: T]): Boolean = {
    // anyMatch() will call commenceOperation()
    !this.anyMatch(pred)
  }

  def peek(action: Consumer[_ >: T]): Stream[T] = {
    commenceOperation()

    val peekAction = action

    val spl = new Spliterators.AbstractSpliterator[T](
      _spliter.estimateSize(),
      _spliter.characteristics()
    ) {

      def tryAdvance(action: Consumer[_ >: T]): Boolean =
        _spliter.tryAdvance((e) => {
          peekAction.accept(e)
          action.accept(e)
        })
    }

    new StreamImpl[T](spl, _parallel, pipeline)
  }

  def reduce(accumulator: BinaryOperator[T]): Optional[T] = {
    commenceOperation()

    var reduceOpt = Optional.empty[T]()

    _spliter.tryAdvance((e) => reduceOpt = Optional.of(e.asInstanceOf[T]))
    reduceOpt.ifPresent((first) => {
      var previous = first
      _spliter.forEachRemaining((e) =>
        previous = accumulator.apply(previous, e)
      )
      reduceOpt = Optional.of(previous)
    })

    reduceOpt
  }

  def reduce(identity: T, accumulator: BinaryOperator[T]): T = {
    commenceOperation()

    var accumulated = identity

    _spliter.forEachRemaining((e) =>
      accumulated = accumulator.apply(accumulated, e)
    )
    accumulated
  }

  def reduce[U](
      identity: U,
      accumulator: BiFunction[U, _ >: T, U],
      combiner: BinaryOperator[U]
  ): U = {
    commenceOperation()

    var accumulated = identity

    _spliter.forEachRemaining((e) =>
      accumulated = accumulator.apply(accumulated, e)
    )
    accumulated
  }

  def skip(n: Long): Stream[T] = {
    if (n < 0)
      throw new IllegalArgumentException(n.toString())

    commenceOperation() // JVM tests argument before operatedUpon or closed.

    var nSkipped = 0L

    while ((nSkipped < n)
        && (_spliter.tryAdvance((e) => nSkipped += 1L))) { /* skip */ }

    // Follow JVM practice; return new stream, not remainder of "this" stream.
    new StreamImpl[T](_spliter, _parallel, pipeline)
  }

  def sorted(): Stream[T] = {
    // No commenceOperation() here. This is an intermediate operation.

    /* Be aware that this method will/should throw on first use if type
     * T is not Comparable[T]. This is described in the Java Stream doc.
     *
     * Implementation note:
     *   It would seem that Comparator.naturalOrder()
     *   could be used here. The SN complier complains, rightly, that
     *   T is not known to be  [T <: Comparable[T]]. That is because
     *   T may actually not _be_ comparable. The comparator below punts
     *   the issue and raises an exception if T is, indeed, not comparable.
     */

    val comparator = new Comparator[T] {
      def compare(o1: T, o2: T): Int =
        o1.asInstanceOf[Comparable[Any]].compareTo(o2)
    }

    sorted(comparator)
  }

  def sorted(comparator: Comparator[_ >: T]): Stream[T] = {
    // No commenceOperation() here. This is an intermediate operation.

    /* Someday figure out the types for the much cleaner 'toArray(generator)'
     * There is a bit of type nastiness/abuse going on here.
     * The hidden assumption is that type is a subclass of Object, or at
     * least AnyRef (T <: Object). However the class declaration places
     * no such restriction on T. It is T <: Any.
     *
     * The Ancients, in their wisdom, must have had a reason for declaring
     * the type that way.
     *
     * I think the class declaration is _wrong_, or at leads one to
     * type abuse, such as here. However, that declaration is pretty
     * hardwired at this point.  Perhaps it will get corrected across
     * the board for Scala Native 1.0.
     *
     * Until then make the shaky assumption that the class creator is always
     * specifying T <: AnyRef so that the coercion will work at
     * runtime.
     */
    val buffer = toArray()

    /* Scala 3 and 2.13.11 both allow ""Arrays.sort(" here.
     * Scala 2.12.18 requires "sort[Object](".
     */
    Arrays
      .sort[Object](buffer, comparator.asInstanceOf[Comparator[_ >: Object]])

    val startingBits = _spliter.characteristics()
    val alwaysSetBits =
      Spliterator.SORTED | Spliterator.ORDERED |
        Spliterator.SIZED | Spliterator.SUBSIZED

    // Time & experience may show that additional bits need to be cleared here.
    val alwaysClearedBits = Spliterator.IMMUTABLE

    val newCharacteristics =
      (startingBits | alwaysSetBits) & ~alwaysClearedBits

    val spl = Spliterators.spliterator[T](buffer, newCharacteristics)

    new StreamImpl[T](spl, _parallel, pipeline)
  }

  def toArray(): Array[Object] = {
    commenceOperation()

    val knownSize = _spliter.getExactSizeIfKnown()

    if (knownSize < 0) {
      val buffer = new ArrayList[T]()
      _spliter.forEachRemaining((e: T) => buffer.add(e))
      buffer.toArray()
    } else {
      val dst = new Array[Object](knownSize.toInt)
      var j = 0
      _spliter.forEachRemaining((e) => {
        dst(j) = e.asInstanceOf[Object]
        j += 1
      })
      dst
    }
  }

  private class ArrayBuilder[A <: Object](generator: IntFunction[Array[A]]) {
    /* The supplied generator is used to create the final Array and
     * to allocate the accumulated chunks.
     *
     * This implementation honors the spirit but perhaps not the letter
     * of the JVM description.
     *
     * The 'chunks' ArrayList accumulator is allocated using the 'normal'
     * allocator for Java Objects.  One could write a custom ArrayList
     * (or other) implementation which uses the supplied generator
     * to allocate & grow the accumulator.  That is outside the bounds
     * and resources of the current effort.
     */

    final val chunkSize = 1024 // A wild guestimate, see what experience brings

    class ArrayChunk(val contents: Array[A]) {
      var nUsed = 0

      def add(e: A): Unit = {
        /* By contract, the sole caller accept() has already checked for
         * sufficient remaining size. Minimize number of index bounds checks.
         */
        contents(nUsed) = e
        nUsed += 1
      }
    }

    var currentChunk: ArrayChunk = _
    val chunks = new ArrayList[ArrayChunk]()

    def createChunk(): Unit = {
      currentChunk = new ArrayChunk(generator(chunkSize))
      chunks.add(currentChunk)
    }

    createChunk() // prime the list with an initial chunk.

    def accept(e: A): Unit = {
      if (currentChunk.nUsed >= chunkSize)
        createChunk()

      currentChunk.add(e)
    }

    def getTotalSize(): Int = { // Largest possible Array size is an Int

      // Be careful with a potentially partially filled trailing chunk.
      var total = 0

      val spliter = chunks.spliterator()

      // Be friendly to Scala 2.12
      val action: Consumer[ArrayChunk] = (e: ArrayChunk) => total += e.nUsed

      while (spliter.tryAdvance(action)) { /* side-effect */ }

      total
    }

    def build(): Array[A] = {
      /* Unfortunately, the chunks list is traversed twice.
       * Someday fate & cleverness may bring a better algorithm.
       * For now, existence & correctness bring more benefit than perfection.
       */
      val dest = generator(getTotalSize())

      var srcPos = 0

      val spliter = chunks.spliterator()

      // Be friendly to Scala 2.12
      val action: Consumer[ArrayChunk] = (e: ArrayChunk) => {
        val length = e.nUsed
        System.arraycopy(
          e.contents,
          0,
          dest,
          srcPos,
          length
        )

        srcPos += length
      }

      while (spliter.tryAdvance(action)) { /* side-effect */ }

      dest
    }
  }

  private def toArrayUnknownSize[A <: Object](
      generator: IntFunction[Array[A]]
  ): Array[A] = {
    val arrayBuilder = new ArrayBuilder[A](generator)

    _spliter.forEachRemaining((e: T) => arrayBuilder.accept(e.asInstanceOf[A]))

    arrayBuilder.build()
  }

  def toArray[A <: Object](generator: IntFunction[Array[A]]): Array[A] = {
    commenceOperation()

    val knownSize = _spliter.getExactSizeIfKnown()
    if (knownSize < 0) {
      toArrayUnknownSize(generator)
    } else {
      val dst = generator(knownSize.toInt)
      var j = 0
      _spliter.forEachRemaining((e: T) => {
        dst(j) = e.asInstanceOf[A]
        j += 1
      })
      dst
    }
  }

}

object StreamImpl {

  class Builder[T] extends Stream.Builder[T] {
    private var built = false
    private val buffer = new ArrayList[T]()

    override def accept(t: T): Unit =
      if (built) StreamImpl.throwIllegalStateException()
      else buffer.add(t)

    override def build(): Stream[T] = {
      built = true
      val spliter = buffer.spliterator()
      new StreamImpl(spliter, parallel = false)
    }
  }

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

  private class CompoundSpliteratorFactory[T, R](
      spliter: Spliterator[T],
      mapper: Function[_ >: T, _ <: Stream[_ <: R]],
      closeOnFirstTouch: Boolean
  ) {
    /* Design note:
     *   Yes, it is passing strange that flatMap
     *   (closeOnFirstTouch == true ) tryAdvance() is advancing
     *   along closed streams.  Unusual!
     *
     *   That seems to be what Java flatMap() traversal is doing:
     *     run close handler once, on first successful tryAdvance() of
     *     each component stream.
     */

    def get(): ju.Spliterator[R] = {
      val substreams =
        new Spliterators.AbstractSpliterator[Stream[T]](
          Long.MaxValue,
          spliter.characteristics()
        ) {
          def tryAdvance(action: Consumer[_ >: Stream[T]]): Boolean = {
            spliter.tryAdvance(e =>
              action.accept(mapper(e).asInstanceOf[Stream[T]])
            )
          }
        }

      new ju.Spliterator[R] {
        override def getExactSizeIfKnown(): Long = -1
        def characteristics(): Int = 0
        def estimateSize(): Long = Long.MaxValue
        def trySplit(): Spliterator[R] = null.asInstanceOf[Spliterator[R]]

        private var currentSpliter: ju.Spliterator[_ <: R] =
          Spliterators.emptySpliterator[R]()

        var currentStream = Optional.empty[StreamImpl[R]]()

        def tryAdvance(action: Consumer[_ >: R]): Boolean = {
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
                .tryAdvance((e) =>
                  currentSpliter = {
                    val eOfR = e.asInstanceOf[StreamImpl[R]]
                    currentStream = Optional.of(eOfR)

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

                    eOfR._spliter
                  }
                )
            }
          }
          advanced
        }
      }
    }
  }

  private class DoublePrimitiveCompoundSpliteratorFactory[T](
      spliter: Spliterator[T],
      mapper: Function[_ >: T, _ <: DoubleStream],
      closeOnFirstTouch: Boolean
  ) {

    def get(): ju.Spliterator.OfDouble = {
      val substreams =
        new Spliterators.AbstractSpliterator[DoubleStream](
          Long.MaxValue,
          spliter.characteristics()
        ) {
          def tryAdvance(action: Consumer[_ >: DoubleStream]): Boolean = {
            spliter.tryAdvance(e => action.accept(mapper(e)))
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

  def concat[T](a: Stream[_ <: T], b: Stream[_ <: T]): Stream[T] = {
    /* Design Note:
     *  This implementation may not comply with the following section
     *  of the JVM description of the Stream#concat method.
     *     "This method operates on the two input streams and binds each
     *      stream to its source. As a result subsequent modifications to an
     *      input stream source may not be reflected in the concatenated
     *      stream result."
     *
     *  If I understand correctly, this implementation is late binding
     *  and the specification is for early binding. This is a rare event.
     *  Usually the defect is the other way around: early when late needed.
     */

    /* Design Note:
     *   At first impression, concat could be succinctly implemented as:
     *     Stream.of(a, b).flatMap[T](Function.identity())
     *
     *   This implementation exists because JVM flatMap(), hence SN flatMap(),
     *   closes each stream as it touches it. JVM concat() closes zero
     *   streams until a final explicit close() happens. A subtle difference,
     *   until the bug reports start pouring in.
     */

    val aImpl = a.asInstanceOf[StreamImpl[T]]
    val bImpl = b.asInstanceOf[StreamImpl[T]]

    aImpl.commenceOperation()
    bImpl.commenceOperation()

    val arr = new Array[Object](2)
    arr(0) = aImpl
    arr(1) = bImpl

    val csf = new CompoundSpliteratorFactory[Stream[T], T](
      Arrays.spliterator[Stream[T]](arr),
      Function.identity(),
      closeOnFirstTouch = false
    )

    val pipelineA = aImpl.pipeline
    val pipelineB = bImpl.pipeline
    val pipelines = new ArrayDeque[StreamImpl[T]](pipelineA)
    pipelines.addAll(pipelineB)

    new StreamImpl[T](csf.get(), parallel = false, pipelines)
  }

  def throwIllegalStateException(): Unit = {
    throw new IllegalStateException(
      "stream has already been operated upon or closed"
    )
  }

}
