/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.lang.invoke.VarHandle
import java.util.Objects.requireNonNull
import java.util.concurrent.locks.{LockSupport, ReentrantLock}
import java.util.function.{BiConsumer, BiPredicate, Consumer}
import java.util.{ArrayList, Arrays, List as JList}

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.memory_order.{
  memory_order_acquire, memory_order_release
}
import scala.scalanative.libc.stdatomic.{AtomicInt, AtomicLongLong, AtomicRef}
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.{ObjectArray, fromRawPtr}
import scala.scalanative.unsafe.Ptr

// @since JDK 9
class SubmissionPublisher[T](
    private val executor: Executor,
    private val maxBufferCapacity: Int,
    private val handler: BiConsumer[? >: Flow.Subscriber[
      ? >: T
    ], ? >: Throwable]
) extends Flow.Publisher[T]
    with AutoCloseable {

  import SubmissionPublisher.*

  def this() =
    this(SubmissionPublisher.ASYNC_POOL, Flow.defaultBufferSize(), null)

  def this(executor: Executor, maxBufferCapacity: Int) =
    this(executor, maxBufferCapacity, null)

  requireNonNull(executor, "executor cannot be null")
  require(maxBufferCapacity > 0, "maxBufferCapacity must be positive")

  val maxCap = SubmissionPublisher.roundToPowerOfTwo(maxBufferCapacity)

  /*
   * Clients (BufferedSubscriptions) are maintained in a linked list (via their
   * "next" fields). This works well for publish loops. It requires O(n)
   * traversal to check for duplicate subscribers, but we expect that
   * subscribing is much less common than publishing. Unsubscribing occurs only
   * during traversal loops, when BufferedSubscription methods return negative
   * values signifying that they have been closed. To reduce head-of-line
   * blocking, submit and offer methods first call BufferedSubscription.offer
   * on each subscriber, and place saturated ones in retries list (using
   * nextRetry field), and retry, possibly blocking or dropping.
   */
  private var clients: BufferedSubscription[T] = _

  /** Run status, updated only within locks */
  @volatile private var closed = false

  /** Set true on first call to subscribe, to initialize possible owner */
  private var subscribed = false

  /** The first caller thread to subscribe, or null if thread ever changed */
  private var ownerThread: Thread = _

  /** If non-null, the exception in closeExceptionally */
  @volatile private var closedException: Throwable = _

  /** A lock for operations that need to be synchronous */
  private final val lock = new ReentrantLock()

  override def subscribe(subscriber: Flow.Subscriber[? >: T]): Unit = {
    requireNonNull(subscriber, "subscriber cannot be null")
    val initCap = if (maxCap < INITIAL_CAPACITY) maxCap else INITIAL_CAPACITY
    val subscription = new BufferedSubscription[T](
      subscriber,
      executor,
      initCap,
      maxCap,
      handler
    )

    lock.lock()
    try {
      if (!subscribed) {
        subscribed = true
        ownerThread = Thread.currentThread()
      }

      var pred: BufferedSubscription[T] = null
      var curr = clients

      var break = false
      while (!break) {
        if (curr == null) { // insert as tail
          subscription.onSubscribe()

          if (closedException != null)
            subscription.onError(closedException)
          else if (closed)
            subscription.onComplete()
          else if (pred == null)
            clients = subscription
          else
            pred.next = subscription

          break = true
        } // force a new line for formatting
        else {
          val next = curr.next
          if (curr.isClosed()) { // detach curr node
            curr.next = null
            if (pred == null)
              clients = next
            else
              pred.next = next

            curr = next
          } // force a new line for formatting
          else if (subscriber.equals(curr.subscriber)) {
            curr.onError(new IllegalStateException("Duplicate subscribe"))
            break = true
          } // force a new line for formatting
          else {
            pred = curr
            curr = next
          }
        }
      }
    } finally {
      lock.unlock()
    }
  }

  def submit(item: T): Int =
    _offer(item, Long.MaxValue, null)

  def offer(
      item: T,
      onDrop: BiPredicate[Flow.Subscriber[? >: T], ? >: T]
  ): Int =
    _offer(item, 0L, onDrop)

  def offer(
      item: T,
      timeout: Long,
      unit: TimeUnit,
      onDrop: BiPredicate[Flow.Subscriber[? >: T], ? >: T]
  ): Int = {
    var timeoutNanos = unit.toNanos(timeout)
    // distinguishes from untimed (only wrt interrupt policy)
    if (timeoutNanos == Long.MaxValue) timeoutNanos -= 1L
    _offer(item, timeoutNanos, onDrop)
  }

  def close(): Unit =
    if (!closed) {
      var curr: BufferedSubscription[T] = null

      lock.lock()
      try {
        // no need to re-check closed here
        curr = clients
        clients = null
        ownerThread = null
        closed = true
      } finally {
        lock.unlock()
      }

      while (curr != null) {
        val next = curr.next
        curr.next = null
        curr.onComplete()
        curr = next
      }
    }

  def closeExceptionally(error: Throwable): Unit = {
    requireNonNull(error, "error cannot be null")
    if (!closed) {
      var curr: BufferedSubscription[T] = null

      lock.lock()
      try {
        curr = clients
        if (!closed) { // don't clobber racing close
          closedException = error
          clients = null
          ownerThread = null
          closed = true
        }
      } finally {
        lock.unlock()
      }

      while (curr != null) {
        val next = curr.next
        curr.next = null
        curr.onError(error)
        curr = next
      }
    }
  }

  def isClosed(): Boolean =
    closed

  def getClosedException(): Throwable =
    closedException

  def hasSubscribers(): Boolean =
    if (closed)
      false
    else {
      lock.lock()
      try {
        var curr = clients
        var found = false

        while (!found && curr != null) {
          val next = curr.next
          if (curr.isClosed()) { // remove this node
            curr.next = null
            curr = next
            clients = next
          } else {
            found = true
          }
        }

        found
      } finally {
        lock.unlock()
      }
    }

  def getNumberOfSubscribers(): Int =
    if (closed)
      0
    else {
      lock.lock()
      try
        cleanAndCount()
      finally
        lock.unlock()
    }

  def getExecutor(): Executor =
    executor

  def getMaxBufferCapacity(): Int =
    maxCap

  def getSubscribers(): JList[Flow.Subscriber[? >: T]] = {
    val subs = new ArrayList[Flow.Subscriber[? >: T]]

    lock.lock()
    try {
      var pred: BufferedSubscription[T] = null
      var next: BufferedSubscription[T] = null
      var curr = clients

      while (curr != null) {
        next = curr.next

        if (curr.isClosed()) {
          curr.next = null
          if (pred == null)
            clients = next
          else
            pred.next = next
        } else { // append to list
          subs.add(curr.subscriber)
          pred = curr
        }

        curr = next
      }
    } finally {
      lock.unlock()
    }

    subs
  }

  def isSubscribed(subscriber: Flow.Subscriber[? >: T]): Boolean = {
    requireNonNull(subscriber, "subscriber cannot be null")
    if (closed)
      false
    else {
      lock.lock()
      try {
        var pred: BufferedSubscription[T] = null
        var next: BufferedSubscription[T] = null
        var curr = clients
        var found = false

        while (!found && curr != null) {
          next = curr.next

          if (curr.isClosed()) {
            curr.next = null
            if (pred == null)
              clients = next
            else
              pred.next = next
            curr = next
          } // force a new line for formatting
          else if (subscriber.equals(curr.subscriber)) {
            found = true
          } // force a new line for formatting
          else {
            pred = curr
            curr = next
          }
        }

        found
      } finally {
        lock.unlock()
      }
    }
  }

  def estimateMinimumDemand(): Long = {
    var min = Long.MaxValue
    var nonEmpty = false

    lock.lock()
    try {
      var pred: BufferedSubscription[T] = null
      var next: BufferedSubscription[T] = null
      var curr = clients

      while (curr != null) {
        val lag = curr.estimateLag()
        val demand = curr.getDemand()
        next = curr.next

        if (lag < 0) {
          curr.next = null
          if (pred == null)
            clients = next
          else
            pred.next = next
        } else {
          val _diff = demand - lag
          if (_diff < min)
            min = _diff
          nonEmpty = true
          pred = curr
        }

        curr = next
      }
    } finally {
      lock.unlock()
    }

    if (nonEmpty)
      min
    else
      0
  }

  def estimateMaximumLag(): Int = {
    var max = 0

    lock.lock()
    try {
      var pred: BufferedSubscription[T] = null
      var next: BufferedSubscription[T] = null
      var curr = clients

      while (curr != null) {
        val lag = curr.estimateLag()
        next = curr.next

        if (lag < 0) {
          curr.next = null
          if (pred == null)
            clients = next
          else
            pred.next = next
        } // force a new line for formatting
        else {
          if (lag > max) max = lag
          pred = curr
        }

        curr = next
      }
    } finally {
      lock.unlock()
    }

    max
  }

  def consume(consumer: Consumer[? >: T]): CompletableFuture[Void] = {
    requireNonNull(consumer, "consumer cannot be null")
    val status = new CompletableFuture[Void]()
    subscribe(new ConsumerSubscriber[T](status, consumer))
    status
  }

  //
  // Private methods and implementation details
  //

  /** Common implementation for all three forms of submit and offer.
   *
   *  Acts as submit if nanos == Long.MaxValue, else offer.
   */
  private def _offer(
      item: T,
      timeoutNanos: Long,
      onDrop: BiPredicate[Flow.Subscriber[? >: T], ? >: T]
  ): Int = {
    requireNonNull(item, "item cannot be null")

    var complete = false
    var lag = 0 // highest lag observed

    lock.lock()
    try {
      val _thread = Thread.currentThread()
      val _ownerThread = ownerThread
      val unowned = _ownerThread != _thread && _ownerThread != null
      if (unowned && _ownerThread != null)
        ownerThread = null // disable bias

      var curr = clients

      if (curr == null)
        complete = closed
      else {
        complete = false

        var cleanMe = false
        var retries: BufferedSubscription[T] = null
        var rtail: BufferedSubscription[T] = null

        while ({
          val next = curr.next
          val stat = curr.offer(item, unowned)

          if (stat == 0) {
            curr.nextRetry = null

            if (rtail == null)
              retries = curr
            else
              rtail.nextRetry = curr

            rtail = curr
          } // force a new line for formatting
          else if (stat < 0) { // closed
            cleanMe = true // remove later
          } // force a new line for formatting
          else if (stat > lag)
            lag = stat

          curr = next
          curr != null
        }) {}

        if (retries != null || cleanMe)
          lag = retryOffer(item, timeoutNanos, onDrop, retries, lag, cleanMe)
      }
    } finally {
      lock.unlock()
    }

    if (complete)
      throw new IllegalStateException("Closed")
    else
      lag
  }

  /** Helps, (timed) waits for, and/or drops buffers on list
   *
   *  returns lag or negative drops (for use in offer).
   */
  private def retryOffer(
      item: T,
      timeoutNanos: Long,
      onDrop: BiPredicate[Flow.Subscriber[? >: T], ? >: T],
      retries: BufferedSubscription[T],
      lag: Int,
      cleanMe: Boolean
  ): Int = {
    var _lag = lag
    var _cleanMe = cleanMe

    var currRetry = retries
    while (currRetry != null) {
      val nextRetry = currRetry.nextRetry
      currRetry.nextRetry = null

      if (timeoutNanos > 0L)
        currRetry.awaitSpace(timeoutNanos)

      var stat = currRetry.retryOffer(item)
      if (stat == 0 && onDrop != null && onDrop.test(
            currRetry.subscriber,
            item
          ))
        stat = currRetry.retryOffer(item)

      if (stat == 0)
        _lag = if (_lag >= 0) -1 else _lag - 1
      else if (stat < 0)
        _cleanMe = true
      else if (_lag >= 0 && stat > _lag)
        _lag = stat

      currRetry = nextRetry
    }

    if (_cleanMe)
      cleanAndCount(): Unit

    _lag
  }

  /** Returns current list count after removing closed subscribers. Call only
   *  while holding lock. Used mainly by retryOffer for cleanup.
   */
  private def cleanAndCount(): Int = {
    var count = 0

    var pred: BufferedSubscription[T] = null
    var next: BufferedSubscription[T] = null
    var curr = clients

    while (curr != null) {
      next = curr.next

      if (curr.isClosed()) {
        curr.next = null
        if (pred == null)
          clients = next
        else
          pred.next = next
      } // force a new line for formatting
      else {
        pred = curr
        count += 1
      }

      curr = next
    }

    count
  }

}

object SubmissionPublisher {

  /** The largest possible power of two array size. */
  final val BUFFER_CAPACITY_LIMIT: Int = 1 << 30

  /** Initial buffer capacity used when maxBufferCapacity is greater. Must be a
   *  power of two.
   */
  final val INITIAL_CAPACITY: Int = 32

  private def roundToPowerOfTwo(capacity: Int): Int = {
    var n = capacity - 1
    n |= n >>> 1
    n |= n >>> 2
    n |= n >>> 4
    n |= n >>> 8
    n |= n >>> 16

    if (n <= 0)
      1
    else if (n >= BUFFER_CAPACITY_LIMIT)
      BUFFER_CAPACITY_LIMIT
    else
      n + 1
  }

  // default Executor setup; nearly the same as CompletableFuture

  /** Default executor -- ForkJoinPool.commonPool() unless it cannot support
   *  parallelism.
   */
  private[SubmissionPublisher] val ASYNC_POOL: Executor =
    if (ForkJoinPool.getCommonPoolParallelism() > 1)
      ForkJoinPool.commonPool()
    else
      new ThreadPerTaskExecutor()

  /** Fallback if ForkJoinPool.commonPool() cannot support parallelism */
  private[SubmissionPublisher] final class ThreadPerTaskExecutor
      extends Executor {
    override def execute(r: Runnable): Unit = {
      requireNonNull(r)
      new Thread(r).start()
    }
  }

  /** Subscriber for method consume */
  private[SubmissionPublisher] final class ConsumerSubscriber[T](
      status: CompletableFuture[Void],
      consumer: Consumer[? >: T]
  ) extends Flow.Subscriber[T] {
    var subscription: Flow.Subscription = _

    final def onSubscribe(subscription: Flow.Subscription): Unit = {
      this.subscription = subscription
      status.whenComplete((v, e) => subscription.cancel())
      if (!status.isDone()) subscription.request(Long.MaxValue)
    }

    final def onError(ex: Throwable): Unit =
      status.completeExceptionally(ex): Unit

    final def onComplete(): Unit =
      status.complete(null.asInstanceOf[Void]): Unit

    final def onNext(item: T): Unit =
      try consumer.accept(item)
      catch {
        case exc: Throwable => {
          subscription.cancel()
          status.completeExceptionally(exc): Unit
        }
      }
  }

  /** A task for consuming buffer items and signals, created and executed
   *  whenever they become available. A task consumes as many items/signals as
   *  possible before terminating, at which point another task is created when
   *  needed. The dual Runnable and ForkJoinTask declaration saves overhead when
   *  executed by ForkJoinPools, without impacting other kinds of Executors.
   */
  private[SubmissionPublisher] final class ConsumerTask[T](
      private[SubmissionPublisher] val consumer: BufferedSubscription[T]
  ) extends ForkJoinTask[Unit]
      with Runnable {
    override final def getRawResult(): Unit =
      ()

    override protected[SubmissionPublisher] final def setRawResult(
        value: Unit
    ): Unit =
      ()

    override protected[SubmissionPublisher] final def exec(): Boolean = {
      consumer.consume()
      false
    }

    override final def run(): Unit =
      consumer.consume()
  }

  private[SubmissionPublisher] object BufferedSubscription {

    /** ctl bit values */
    private[SubmissionPublisher] object Ctl {
      final val CLOSED = 0x01 // if set, other bits ignored
      final val ACTIVE = 0x02 // keep-alive for consumer task
      final val REQS = 0x04 // (possibly) nonzero demand
      final val ERROR = 0x08 // issues onError when noticed
      final val COMPLETE = 0x10 // issues onComplete when done
      final val RUN = 0x20 // task is or will be running
      final val OPEN = 0x40 // true after subscribe
    }

    /** timeout vs interrupt sentinel */
    private[SubmissionPublisher] final val INTERRUPTED = -1L

  }

  private type Contended = scala.scalanative.annotation.align

  @Contended()
  final class BufferedSubscription[T] private[SubmissionPublisher] (
      private[SubmissionPublisher] val subscriber: Flow.Subscriber[? >: T],
      private var executor: Executor,
      private val initCapacity: Int,
      private val maxCapacity: Int,
      private val handler: BiConsumer[
        ? >: Flow.Subscriber[? >: T],
        ? >: Throwable
      ]
  ) extends Flow.Subscription
      with ForkJoinPool.ManagedBlocker {

    requireNonNull(subscriber, "subscriber cannot be null")
    requireNonNull(executor, "executor cannot be null")
    require(initCapacity > 0, "initCapacity must be positive")
    require(maxCapacity > 0, "maxCapacity must be positive")

    import BufferedSubscription.Ctl

    // > 0 if timed wait, Long.MAX_VALUE if untimed wait
    private var timeout: Long = 0L
    // next position to take
    private var head: Int = 0
    // next position to put
    private var tail: Int = 0
    // atomic run state flags
    private var ctl: Int = 0
    // buffer array, the type constraint is T <: AnyRef
    private var buffer = new Array[Object](initCapacity)
    // blocked producer thread
    private var waiter: Thread = _
    // holds until onError issued
    private var pendingError: Throwable = _

    // next node for main linked list
    private[SubmissionPublisher] var next: BufferedSubscription[T] = _
    // next node for retry linked list
    private[SubmissionPublisher] var nextRetry: BufferedSubscription[T] = _

    // unfilled requests
    @Contended("c") private var demand: Long = 0L
    // nonzero if producer blocked
    @Contended("c") @volatile private var waiting: Int = 0

    // Utilities for atomic access to fields

    @alwaysinline
    private def tailAtm: AtomicInt =
      new AtomicInt(fromRawPtr[Int](classFieldRawPtr(this, "tail")))
    @alwaysinline
    private def tailIncrementAndGet(): Int =
      tailAtm.fetchAdd(1) + 1
    @alwaysinline
    private def tailGetAndIncrement(): Int =
      tailAtm.fetchAdd(1)

    @alwaysinline
    private def ctlAtm =
      new AtomicInt(fromRawPtr[Int](classFieldRawPtr(this, "ctl")))
    @alwaysinline
    private def ctlGetAndBitwiseOr(bits: Int): Int =
      ctlAtm.fetchOr(bits)
    @alwaysinline
    private def ctlWeakCompareAndSet(expectValue: Int, value: Int): Boolean =
      ctlAtm.compareExchangeWeak(expectValue, value)

    @alwaysinline
    private def demandAtm =
      new AtomicLongLong(fromRawPtr[Long](classFieldRawPtr(this, "demand")))
    @alwaysinline
    private def demandCompareAndSet(expectValue: Long, value: Long): Boolean =
      demandAtm.compareExchangeStrong(expectValue, value)
    @alwaysinline
    private def demandSubtractAndGet(k: Long): Long =
      demandAtm.fetchSub(k) - k

    @alwaysinline
    private def arrayGetAtomicRef[E <: AnyRef](
        a: Array[E],
        i: Int
    ): AtomicRef[E] = {
      val elemRef = a.asInstanceOf[ObjectArray].at(i).asInstanceOf[Ptr[E]]
      new AtomicRef[E](elemRef)
    }
    @alwaysinline
    private def arrayCompareAndSet[E <: AnyRef](
        a: Array[E],
        i: Int,
        e: E,
        v: E
    ): Boolean =
      arrayGetAtomicRef(a, i).compareExchangeStrong(e, v)
    @alwaysinline
    private def arraySetRelease[E <: AnyRef](a: Array[E], i: Int, e: E): Unit =
      arrayGetAtomicRef(a, i).store(e, memory_order_release)
    @alwaysinline
    private def arrayGetAcquire[E <: AnyRef](a: Array[E], i: Int): E =
      arrayGetAtomicRef(a, i).load(memory_order_acquire)
    @alwaysinline
    private def arrayGetAndSet[E <: AnyRef](a: Array[E], i: Int, v: E): E =
      arrayGetAtomicRef(a, i).exchange(v)

    // Utilities used by SubmissionPublisher

    /** Returns true if closed (consumer task may still be running). */
    def isClosed() =
      (ctl & Ctl.CLOSED) != 0

    /** Returns estimated number of buffered items, or negative if closed. */
    def estimateLag(): Int = {
      val lag = tail - head
      if (isClosed()) -1
      else if (lag < 0) 0
      else lag
    }

    @alwaysinline
    def getDemand(): Long =
      demand

    // Methods for submitting items

    /** Tries to add item and start consumer task if necessary.
     *
     *  @return
     *    negative if closed, 0 if saturated, else estimated lag
     */
    def offer(item: T, unowned: Boolean): Int = {
      val _tail = tail
      val size = _tail + 1 - head
      val cap = buffer.length
      val index = _tail & (cap - 1)

      var stat = 0
      if (cap > 0) {
        val added =
          if (size >= cap && cap < maxCapacity) // resize
            growAndOffer(item, _tail)
          else if (size >= cap || unowned) // need volatile CAS
            arrayCompareAndSet(buffer, index, null, item.asInstanceOf[AnyRef])
          else {
            arraySetRelease(buffer, index, item.asInstanceOf[AnyRef])
            true
          }

        if (added) {
          tailGetAndIncrement()
          stat = size
        }
      }

      startOnOffer(stat)
    }

    /** Tries to create or expand buffer, then adds item if possible. */
    private def growAndOffer(item: T, tail: Int): Boolean = {
      val cap = buffer.length
      val newCap = cap << 1
      var newBuffer: Array[Object] = null

      if (newCap > 0)
        try {
          newBuffer = new Array[Object](newCap)
        } catch {
          case exc: OutOfMemoryError => ()
        }

      if (newBuffer == null)
        false
      else { // take and move items
        val mask = cap - 1
        val newMask = newCap - 1
        newBuffer(tail & newMask) = item.asInstanceOf[AnyRef]

        var t = tail - 1
        var k = mask
        var break = false
        while (!break && k >= 0) {
          val x = arrayGetAndSet(buffer, t & mask, null)

          if (x == null)
            break = true // already consumed, exits loop
          else {
            newBuffer(t & newMask) = x
            t -= 1
            k -= 1
          }
        }

        buffer = newBuffer
        VarHandle.releaseFence() // release array and slots
        true
      }
    }

    /** Version of offer for retries (no resize or bias) */
    def retryOffer(item: T): Int = {
      var stat = 0
      val mask = buffer.length - 1
      val idx = mask & tail

      if (arrayCompareAndSet(buffer, idx, null, item.asInstanceOf[AnyRef]))
        stat = tailIncrementAndGet() - head

      startOnOffer(stat);
    }

    /** Tries to start consumer task after offer.
     *
     *  @return
     *    negative if now closed, else argument
     */
    private def startOnOffer(stat: Int): Int = {
      var _ctl = ctl
      var _stat = stat

      // start or keep alive if requests exist and not active
      if ((_ctl & (Ctl.REQS | Ctl.ACTIVE)) == Ctl.REQS
          && {
            _ctl = ctlGetAndBitwiseOr(Ctl.RUN | Ctl.ACTIVE)
            (_ctl & (Ctl.RUN | Ctl.CLOSED)) == 0
          })
        tryStart()
      else if ((_ctl & Ctl.CLOSED) != 0)
        _stat = -1

      _stat
    }

    /** Tries to start consumer task. Sets error state on failure. */
    private def tryStart(): Unit =
      try {
        val task = new ConsumerTask[T](this)
        if (executor != null) // skip if disabled on error (executor will be set to null)
          executor.execute(task)
      } catch {
        case exc @ (_: RuntimeException | _: Error) =>
          ctlGetAndBitwiseOr(Ctl.ERROR | Ctl.CLOSED)
          throw exc
      }

    // Signals to consumer tasks

    /** Sets the given control bits, starting task if not running or closed.
     *
     *  @param bits
     *    state bits, assumed to include RUN but not CLOSED
     */
    def startOnSignal(bits: Int): Unit =
      if ((ctl & bits) != bits
          && (ctlGetAndBitwiseOr(bits) & (Ctl.RUN | Ctl.CLOSED)) == 0)
        tryStart()

    def onSubscribe(): Unit =
      startOnSignal(Ctl.RUN | Ctl.ACTIVE)

    def onComplete(): Unit =
      startOnSignal(Ctl.RUN | Ctl.ACTIVE | Ctl.COMPLETE)

    /** Issues error signal, asynchronously if a task is running, else
     *  synchronously.
     */
    def onError(exc: Throwable): Unit = {
      if (exc != null)
        pendingError = exc // races are OK

      val _ctl = ctlGetAndBitwiseOr(Ctl.ERROR | Ctl.RUN | Ctl.ACTIVE)
      if ((_ctl & Ctl.CLOSED) == 0) {
        if ((_ctl & Ctl.RUN) == 0)
          tryStart()
        else
          Arrays.fill(buffer, null)
      }
    }

    /** Causes consumer task to exit if active (without reporting onError unless
     *  there is already a pending error), and disables.
     */
    override def cancel(): Unit =
      onError(null)

    /** Adds to demand and possibly starts task. */
    override def request(n: Long): Unit =
      if (n > 0L) {
        while ({
          val p = demand
          val d = p + n // saturate
          !demandCompareAndSet(p, if (d < p) Long.MaxValue else d)
        }) {}

        startOnSignal(Ctl.RUN | Ctl.ACTIVE | Ctl.REQS)
      } // force a new line for formatting
      else
        onError(
          new IllegalArgumentException("non-positive subscription request")
        )

    // Consumer task actions

    /** Consumer loop, called from ConsumerTask, or indirectly when helping
     *  during submit.
     */
    def consume(): Unit = {
      // `subscriber != null` has been checked in the constructor
      subscribeOnOpen(subscriber)

      var _demand = demand
      var _head = head
      var _tail = tail

      var break = false
      while (!break) {
        val _ctl = ctl
        var taken = 0
        var empty = false

        if ((_ctl & Ctl.ERROR) != 0) {
          closeOnError(subscriber, null)
          break = true
        } // force a new line for formatting
        else if ({
          taken = takeItems(subscriber, _demand, _head)
          taken > 0
        }) {
          _head += taken
          head = _head
          _demand = demandSubtractAndGet(taken.toLong)
        } // force a new line for formatting
        else if ({ _demand = demand; _demand == 0L }
            && ((_ctl & Ctl.REQS) != 0)) // exhausted demand
          ctlWeakCompareAndSet(_ctl, _ctl & ~Ctl.REQS): Unit
        else if (_demand != 0L && (_ctl & Ctl.REQS) == 0) // new demand
          ctlWeakCompareAndSet(_ctl, _ctl | Ctl.REQS): Unit
        else if ({
          val _tail_old = _tail; _tail = tail;
          _tail_old == _tail // stability check
        }) {
          if ({ empty = _tail == _head; empty } && (_ctl & Ctl.COMPLETE) != 0) {
            closeOnComplete(subscriber) // end of stream
            break = true
          } // force a new line for formatting
          else if (empty || _demand == 0L) {
            val bit =
              if ((_ctl & Ctl.ACTIVE) != 0)
                Ctl.ACTIVE
              else
                Ctl.RUN
            if (ctlWeakCompareAndSet(_ctl, _ctl & ~bit) && bit == Ctl.RUN)
              break = true
          }
        }
      }
    }

    /** Consumes some items until unavailable or bound or error.
     *
     *  @param sub
     *    subscriber
     *  @param demand
     *    current demand
     *  @param head
     *    current head
     *  @return
     *    number taken
     */
    def takeItems(
        sub: Flow.Subscriber[? >: T],
        demand: Long,
        head: Int
    ): Int = {
      var h = head
      var k = 0
      val cap = buffer.length

      if (cap > 0) {
        val m = cap - 1
        val b = (m >>> 3) + 1 // min(1, cap/8)
        val n = if (demand < b.toLong) demand.toInt else b

        var break = false
        while (!break && k < n) {
          val x = arrayGetAndSet(buffer, h & m, null)

          if (waiting != 0) signalWaiter()

          if (x == null)
            break = true
          else if (!consumeNext(sub, x.asInstanceOf[T]))
            break = true
          else {
            h += 1
            k += 1
          }
        }
      }

      k
    }

    def consumeNext(sub: Flow.Subscriber[? >: T], x: T): Boolean =
      try {
        sub.onNext(x)
        true
      } // force a new line for formatting
      catch {
        case exc: Throwable =>
          handleOnNext(sub, exc)
          false
      }

    /** Processes exception in Subscriber.onNext */
    def handleOnNext(sub: Flow.Subscriber[? >: T], exc: Throwable): Unit = {
      try {
        if (handler != null) handler.accept(sub, exc)
      } catch {
        case ignore: Throwable => ()
      }

      closeOnError(sub, exc)
    }

    /** Issues subscriber.onSubscribe if this is first signal. */
    def subscribeOnOpen(sub: Flow.Subscriber[? >: T]): Unit =
      if ((ctl & Ctl.OPEN) == 0
          && (ctlGetAndBitwiseOr(Ctl.OPEN) & Ctl.OPEN) == 0) {
        consumeSubscribe(sub)
      }

    def consumeSubscribe(sub: Flow.Subscriber[? >: T]): Unit =
      try {
        sub.onSubscribe(this) // ignore if disabled
      } catch {
        case exc: Throwable =>
          closeOnError(sub, exc)
      }

    /** Issues subscriber.onComplete unless already closed. */
    def closeOnComplete(sub: Flow.Subscriber[? >: T]): Unit = {
      if ((ctlGetAndBitwiseOr(Ctl.CLOSED) & Ctl.CLOSED) == 0)
        consumeComplete(sub)
    }

    def consumeComplete(sub: Flow.Subscriber[? >: T]): Unit =
      try {
        sub.onComplete()
      } catch {
        case ignore: Throwable => ()
      }

    /** Issues subscriber.onError, and unblocks producer if needed. */
    def closeOnError(sub: Flow.Subscriber[? >: T], exc: Throwable): Unit = {
      var _exc = exc
      if ((ctlGetAndBitwiseOr(Ctl.ERROR | Ctl.CLOSED) & Ctl.CLOSED) == 0) {
        if (exc == null)
          _exc = pendingError
        pendingError = null // detach
        executor = null // suppress racing start calls
        signalWaiter()
        consumeError(sub, _exc)
      }
    }

    def consumeError(sub: Flow.Subscriber[? >: T], exc: Throwable): Unit =
      try {
        if (exc != null) sub.onError(exc)
      } catch {
        case ignore: Throwable => ()
      }

    // Blocking support

    /** Unblocks waiting producer. */
    def signalWaiter(): Unit = {
      val w: Thread = waiter
      waiting = 0
      if (w != null) LockSupport.unpark(w)
    }

    /** Returns true if closed or space available. For ManagedBlocker. */
    def isReleasable(): Boolean =
      ((ctl & Ctl.CLOSED) != 0) || {
        val cap = buffer.length
        cap > 0 && (arrayGetAcquire(buffer, (cap - 1) & tail) == null)
      }

    /** Helps or blocks until timeout, closed, or space available. */
    def awaitSpace(nanos: Long): Unit =
      if (!isReleasable()) {
        ForkJoinPool.helpAsyncBlocker(executor, this)
        if (!isReleasable()) {
          timeout = nanos

          try ForkJoinPool.managedBlock(this)
          catch {
            case ie: InterruptedException =>
              timeout = BufferedSubscription.INTERRUPTED
          }

          if (timeout == BufferedSubscription.INTERRUPTED)
            Thread.currentThread().interrupt()
        }
      }

    /** Blocks until closed, space available or timeout. For ManagedBlocker. */
    def block(): Boolean = {
      var nanos = timeout
      val timed = timeout < Long.MaxValue
      val deadline = if (timed) System.nanoTime() + nanos else 0L

      var break = false
      while (!break && !isReleasable()) {
        if (Thread.interrupted()) {
          timeout = BufferedSubscription.INTERRUPTED
          if (timed)
            break = true
        } // force a new line for formatting
        else if (timed && { nanos = deadline - System.nanoTime(); nanos <= 0L })
          break = true
        else if (waiter == null)
          waiter = Thread.currentThread()
        else if (waiting == 0)
          waiting = 1
        else if (timed)
          LockSupport.parkNanos(this, nanos)
        else
          LockSupport.park(this)
      }

      waiter = null
      waiting = 0
      true
    }

  }

}
