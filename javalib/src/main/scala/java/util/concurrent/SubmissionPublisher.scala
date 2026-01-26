/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.lang.invoke.VarHandle
import java.util.Objects.requireNonNull
import java.util.concurrent.atomic.{
  AtomicBoolean, AtomicInteger, AtomicLong, AtomicReference,
  AtomicReferenceArray
}
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{
  CompletableFuture, ConcurrentHashMap, Executor, Flow, ForkJoinPool, TimeUnit
}
import java.util.function.{BiConsumer, BiPredicate, Consumer}
import java.util.{ArrayList, Collections, List as JList}

import scala.util.boundary
import scala.util.boundary.break

// Reference:
//
// - https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/SubmissionPublisher.html
//
// @since JDK 9
class SubmissionPublisher[T](
    private val executor: Executor,
    private val maxBufferCapacity: Int,
    private val handler: BiConsumer[? >: Flow.Subscriber[
      ? >: T
    ], ? >: Throwable]
) extends Flow.Publisher[T]
    with AutoCloseable {

  import SubmissionPublisher.{
    BUFFER_CAPACITY_LIMIT, BufferedSubscription, ConsumerSubscriber,
    INITIAL_CAPACITY
  }

  def this() =
    this(ForkJoinPool.commonPool(), Flow.defaultBufferSize(), null)

  def this(executor: Executor, maxBufferCapacity: Int) =
    this(
      executor,
      SubmissionPublisher.roundToPowerOfTwo(maxBufferCapacity),
      null
    )

  requireNonNull(executor, "executor cannot be null")
  require(maxBufferCapacity > 0, "maxBufferCapacity must be positive")

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
  private var clients: BufferedSubscription[T] = null

  /** Run status, updated only within locks */
  private val closed = new AtomicBoolean(false)

  /** If non-null, the exception in closeExceptionally */
  private var closedException: Throwable = null

  /** Set true on first call to subscribe, to initialize possible owner */
  private val subscribed = new AtomicBoolean(false)

  /** The first caller thread to subscribe, or null if thread ever changed */
  private var ownerThread: Thread = null

  override def subscribe(subscriber: Flow.Subscriber[? >: T]): Unit = {
    requireNonNull(subscriber, "subscriber cannot be null")
    val subscription = new BufferedSubscription[T](
      subscriber,
      executor,
      if (maxBufferCapacity > BUFFER_CAPACITY_LIMIT) INITIAL_CAPACITY
      else maxBufferCapacity,
      handler
    )

    synchronized {
      if (subscribed.compareAndExchange(false, true)) {
        ownerThread = Thread.currentThread()
      }

      var pred: BufferedSubscription[T] = null
      var curr = clients

      boundary {
        while (true) {
          if (curr == null) { // insert as tail
            subscription.onSubscribe()

            if (closedException != null)
              subscription.onError(closedException)
            else if (closed.get())
              subscription.onComplete()
            else if (pred == null)
              clients = subscription
            else
              pred.next = subscription

            break(())
          }

          val next = curr.next
          if (curr.isClosed()) { // detach curr node
            curr.next = null

            if (pred == null)
              clients = next
            else
              pred.next = next
          } // force fmt style
          else if (subscriber eq curr.subscriber) {
            curr.onError(new IllegalStateException("Duplicate subscribe"))
            break(())
          } // force fmt style
          else {
            pred = curr
          }
          curr = next
        }
        ()
      }
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
  ): Int =
    _offer(item, unit.toNanos(timeout), onDrop)

  def close(): Unit =
    if (closed.compareAndExchange(false, true)) {
      var curr = clients
      synchronized {
        clients = null
        ownerThread = null
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
    if (closed.compareAndExchange(false, true)) {
      var curr = clients
      synchronized {
        clients = null
        ownerThread = null
        closedException = error
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
    closed.get()

  def getClosedException(): Throwable =
    closedException

  def hasSubscribers(): Boolean =
    if (closed.get())
      false
    else
      synchronized {
        var curr = clients
        boundary {
          while (curr != null) {
            val next = curr.next
            if (curr.isClosed()) { // remove this node
              curr.next = null
              curr = next
              clients = next
            } else {
              break(true)
            }
          }
          false
        }
      }

  def getNumberOfSubscribers(): Int =
    if (closed.get())
      0
    else
      synchronized {
        cleanAndCount()
      }

  def getExecutor(): Executor =
    executor

  def getMaxBufferCapacity(): Int =
    maxBufferCapacity

  def getSubscribers(): JList[Flow.Subscriber[? >: T]] = {
    val subs = new ArrayList[Flow.Subscriber[? >: T]]
    synchronized {
      val pred: BufferedSubscription[T] = null
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
        } else // append to list
          subs.add(curr.subscriber)

        curr = next
      }
    }
    Collections.unmodifiableList(subs)
  }

  def isSubscribed(subscriber: Flow.Subscriber[? >: T]): Boolean = {
    requireNonNull(subscriber, "subscriber cannot be null")
    if (closed.get())
      false
    else {
      synchronized {
        var pred: BufferedSubscription[T] = null
        var next: BufferedSubscription[T] = null
        var curr = clients

        boundary {
          while (curr != null) {
            next = curr.next

            if (curr.isClosed()) {
              curr.next = null
              if (pred == null)
                clients = next
              else
                pred.next = next
            } // force fmt style
            else if (subscriber eq curr.subscriber)
              break(true)
            else
              pred = curr

            curr = next
          }
          false
        }
      }
    }
  }

  def estimateMinimumDemand(): Long = {
    var min = Long.MaxValue
    var nonEmpty = false

    synchronized {
      var pred: BufferedSubscription[T] = null
      var next: BufferedSubscription[T] = null
      var curr = clients

      while (curr != null) {
        val lag = curr.estimateLag()
        val demand = curr.demand.get()
        next = curr.next

        if (lag < 0) {
          curr.next = null
          if (pred == null)
            clients = next
          else
            pred.next = next
        } else {
          if ((demand - lag) < min)
            min = demand
          nonEmpty = true
          pred = curr
        }

        curr = next
      }
    }

    if (nonEmpty)
      min
    else
      0
  }

  def estimateMaximumLag(): Int = {
    var max = 0
    synchronized {
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
        } // force fmt style
        else {
          if (lag > max) max = lag
          pred = curr
        }

        curr = next
      }
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

    val complete = new AtomicBoolean(false)
    var lag = 0 // highest lag observed

    synchronized {

      val _thread = Thread.currentThread()
      val _ownerThread = ownerThread
      val unowned = _ownerThread != _thread && _ownerThread != null
      if (unowned)
        ownerThread = null // disable bias

      var curr = clients

      if (curr == null)
        complete.set(closed.get())
      else {
        complete.set(false)
        val cleanMe = new AtomicBoolean(false)

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
          } // force fmt style
          else if (stat < 0) { // closed
            cleanMe.set(true) // remove later
          } // force fmt style
          else if (stat > lag)
            lag = stat

          curr = next
          curr != null
        }) {}

        if (retries != null || cleanMe.get())
          lag =
            retryOffer(item, timeoutNanos, onDrop, retries, lag, cleanMe.get())
      }

    }

    if (complete.get())
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
        _lag = if (lag >= 0) -1 else lag - 1
      else if (stat < 0)
        _cleanMe = true
      else if (lag >= 0 && stat > lag)
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
      } // force fmt style
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
      2
    else if (n >= BUFFER_CAPACITY_LIMIT)
      BUFFER_CAPACITY_LIMIT
    else
      n + 1
  }

  /** Subscriber for method consume */
  final class ConsumerSubscriber[T](
      status: CompletableFuture[Void],
      consumer: Consumer[? >: T]
  ) extends Flow.Subscriber[T] {
    var subscription: Flow.Subscription = null

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
        case exc: Throwable =>
          subscription.cancel()
          status.completeExceptionally(exc): Unit
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

    override protected[concurrent] final def setRawResult(value: Unit): Unit =
      ()

    override protected[concurrent] final def exec(): Boolean = {
      consumer.consume()
      false
    }

    override final def run(): Unit =
      consumer.consume()
  }

  private[concurrent] object BufferedSubscription {

    /** ctl bit values */
    private[concurrent] object CtlFlag {
      val CLOSED = 0x01 // if set, other bits ignored
      val ACTIVE = 0x02 // keep-alive for consumer task
      val REQS = 0x04 // (possibly) nonzero demand
      val ERROR = 0x08 // issues onError when noticed
      val COMPLETE = 0x10 // issues onComplete when done
      val RUN = 0x20 // task is or will be running
      val OPEN = 0x40 // true after subscribe
    }

    /** timeout vs interrupt sentinel */
    private[concurrent] val INTERRUPTED = -1L

  }

  private[concurrent] final class BufferedSubscription[T] private[concurrent] (
      private[concurrent] val subscriber: Flow.Subscriber[? >: T],
      private var executor: Executor,
      private val maxCapacity: Int,
      private val handler: BiConsumer[? >: Flow.Subscriber[
        ? >: T
      ], ? >: Throwable]
  ) extends Flow.Subscription
      with ForkJoinPool.ManagedBlocker {

    requireNonNull(subscriber, "subscriber cannot be null")
    requireNonNull(executor, "executor cannot be null")
    require(maxCapacity > 0, "maxCapacity must be positive")

    import BufferedSubscription.CtlFlag

    private val ctl = new AtomicInteger(0) // atomic run state flags

    private var timeout = 0L // > 0 if timed wait
    private val head = new AtomicInteger(0) // next position to take
    private val tail = new AtomicInteger(0) // next position to put
    // buffer array
    private var buffer = new AtomicReferenceArray[AnyRef](maxCapacity)

    val demand = new AtomicLong(0L) // unfilled requests
    val waiting = new AtomicInteger(0) // nonzero if producer blocked

    // holds until onError issued
    @volatile private var pendingError: Throwable = null
    @volatile private var waiter: Thread = null // blocked producer thread

    // next node for main linked list
    private[SubmissionPublisher] var next: BufferedSubscription[T] = null
    // next node for retry linked list
    private[SubmissionPublisher] var nextRetry: BufferedSubscription[T] = null

    //
    // Wrappers for demand VarHandle
    //

    private def demandSubtractAndGet(k: Long): Long = {
      val n = -k
      n + demand.getAndAdd(n)
    }

    //
    // Utilities used by SubmissionPublisher
    //

    /** Returns true if closed (consumer task may still be running). */
    def isClosed() =
      (ctl.get() & CtlFlag.CLOSED) != 0

    /** Returns estimated number of buffered items, or negative if closed. */
    def estimateLag(): Int = {
      val lag = tail.get() - head.get()
      if (isClosed()) -1
      else if (lag < 0) 0
      else lag
    }

    //
    // Methods for submitting items
    //

    /** Tries to add item and start consumer task if necessary.
     *
     *  @return
     *    negative if closed, 0 if saturated, else estimated lag
     */
    def offer(item: T, unowned: Boolean): Int = {
      val _tail = tail.get()
      val size = _tail + 1 - head.get()
      val cap = if (buffer == null) 0 else buffer.length()
      val index = _tail & (cap - 1)

      var stat = 0
      if (cap > 0) {
        val added =
          if (size >= cap && cap < maxCapacity) // resize
            growAndOffer(item, _tail)
          else if (size >= cap || unowned) // need volatile CAS
            buffer.compareAndExchange(
              index,
              null,
              item.asInstanceOf[AnyRef]
            ) != null
          else {
            buffer.setRelease(index, item.asInstanceOf[AnyRef])
            true
          }

        if (added) {
          tail.getAndIncrement()
          stat = size
        }
      }

      startOnOffer(stat)
    }

    /** Tries to create or expand buffer, then adds item if possible. */
    private def growAndOffer(item: T, tail: Int): Boolean = {
      var cap = 0
      var newCap: Int = 0
      var newBuffer: AtomicReferenceArray[AnyRef] = null

      if ((buffer != null)
          && {
            cap = buffer.length()
            cap > 0
          } // force fmt style
          && {
            newCap = buffer.length() << 1
            newCap > 0
          })
        try {
          newBuffer = new AtomicReferenceArray[AnyRef](newCap)
        } catch {
          case exc: OutOfMemoryError => ()
        }

      if (newBuffer == null)
        false
      else { // take and move items
        val mask = cap - 1
        val newMask = newCap - 1
        var _tail = tail - 1
        newBuffer.set(_tail & newMask, item.asInstanceOf[AnyRef])

        boundary {
          for (k <- mask to 0 by -1) {
            val x = buffer.getAndSet(_tail & mask, null)

            if (x == null)
              break(()) // already consumed
            else {
              _tail -= 1
              newBuffer.set(_tail & newMask, x)
            }
          }
          ()
        }

        buffer = newBuffer
        VarHandle.releaseFence() // release array and slots
        true
      }
    }

    /** Version of offer for retries (no resize or bias) */
    def retryOffer(item: T): Int = {
      var stat = 0
      var cap = 0

      if (buffer != null
          && {
            cap = buffer.length()
            cap > 0
          }
          && buffer.compareAndExchange(
            (cap - 1) & tail.get(),
            null,
            item.asInstanceOf[AnyRef]
          ) != null)
        stat = tail.incrementAndGet() - head.get()

      startOnOffer(stat);
    }

    /** Tries to start consumer task after offer.
     *
     *  @return
     *    negative if now closed, else argument
     */
    private def startOnOffer(stat: Int): Int = {
      var _ctl = ctl.get()
      var _stat = stat

      // start or keep alive if requests exist and not active
      if ((_ctl & (CtlFlag.REQS | CtlFlag.ACTIVE)) == CtlFlag.REQS
          && {
            _ctl = ctl.getAndSet(_ctl | CtlFlag.RUN | CtlFlag.ACTIVE)
            (_ctl & (CtlFlag.RUN | CtlFlag.CLOSED)) == 0
          })
        tryStart()
      else if ((_ctl & CtlFlag.CLOSED) != 0)
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
        case exc: (RuntimeException | Error) =>
          ctl.set(ctl.get() | CtlFlag.ERROR | CtlFlag.CLOSED)
          throw exc
      }

    //
    // Signals to consumer tasks
    //

    /** Sets the given control bits, starting task if not running or closed.
     *
     *  @param bits
     *    state bits, assumed to include RUN but not CLOSED
     */
    def startOnSignal(bits: Int): Unit = {
      val _ctl = ctl.get()
      if ((_ctl & bits) != bits
          && {
            ctl.set(_ctl | bits)
            (_ctl & (CtlFlag.RUN | CtlFlag.CLOSED)) == 0
          })
        tryStart()
    }

    def onSubscribe(): Unit =
      startOnSignal(CtlFlag.RUN | CtlFlag.ACTIVE)

    def onComplete(): Unit =
      startOnSignal(CtlFlag.RUN | CtlFlag.ACTIVE | CtlFlag.COMPLETE)

    /** Issues error signal, asynchronously if a task is running, else
     *  synchronously.
     */
    def onError(exc: Throwable): Unit = {
      val _ctl = ctl.get()

      if (exc != null)
        pendingError = exc // races are OK

      ctl.set(_ctl | CtlFlag.ERROR | CtlFlag.RUN | CtlFlag.ACTIVE)
      if ((_ctl & CtlFlag.CLOSED) == 0) {
        if ((_ctl & CtlFlag.RUN) == 0)
          tryStart()
        else if (buffer != null)
          (0 until buffer.length()).foreach(i => buffer.lazySet(i, null))
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
          val p = demand.get()
          val d = p + n // saturate
          !demand.compareAndSet(p, if (d < p) Long.MaxValue else d)
        }) {}

        startOnSignal(CtlFlag.RUN | CtlFlag.ACTIVE | CtlFlag.REQS)
      } // force fmt style
      else
        onError(
          new IllegalArgumentException("non-positive subscription request")
        )

    //
    // Consumer task actions
    //

    /** Consumer loop, called from ConsumerTask, or indirectly when helping
     *  during submit.
     */
    def consume(): Unit =
      if (subscriber != null) { // hoist checks else disabled
        subscribeOnOpen(subscriber)

        var _demand = demand.get()
        var _head = head.get()
        var _tail = tail.get()

        boundary {
          while (true) {
            val _ctl = ctl.get()
            var taken = 0
            var empty = false

            if ((_ctl & CtlFlag.ERROR) != 0) {
              closeOnError(subscriber, null)
              break(())
            } // force fmt style
            else if ({
              taken = takeItems(subscriber, _demand, _head); taken > 0
            }) {
              _head += taken
              head.set(_head)
              _demand = demandSubtractAndGet(taken.toLong)
            } // force fmt style
            else if ({
                  _demand = demand.get(); _demand == 0L
                } && ((_ctl & CtlFlag.REQS) != 0))
              ctl.weakCompareAndSetPlain(
                _ctl,
                _ctl & ~CtlFlag.REQS
              ): Unit // exhausted demand
            else if (_demand != 0L && (_ctl & CtlFlag.REQS) == 0)
              ctl.weakCompareAndSetPlain(
                _ctl,
                _ctl | CtlFlag.REQS
              ): Unit // new demand
            else if ({
              val _tail_old = _tail; _tail = tail.get(); _tail_old == _tail
            }) {
              // stability check
              if ({
                    empty = _tail == _head; empty
                  } && (_ctl & CtlFlag.COMPLETE) != 0) {
                closeOnComplete(subscriber) // end of stream
                break(())
              } // force fmt style
              else if (empty || _demand == 0L) {
                val bit =
                  if ((_ctl & CtlFlag.ACTIVE) != 0) CtlFlag.ACTIVE
                  else CtlFlag.RUN
                if (ctl.weakCompareAndSetPlain(
                      _ctl,
                      _ctl & ~bit
                    ) && bit == CtlFlag.RUN)
                  break(())
              }
            }
          }
          ()
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
      var cap = 0

      if (buffer != null
          && { cap = buffer.length(); cap > 0 }) {
        val m = cap - 1
        val b = (m >>> 3) + 1 // min(1, cap/8)

        val n = if (demand < b.toLong) demand.toInt else b

        boundary {
          while (k < n) {
            val x = buffer.getAndSet(h & m, null)

            if (waiting != 0) signalWaiter()

            if (x == null) break(())
            else if (!consumeNext(sub, x.asInstanceOf[T])) break(())

            h += 1
            k += 1
          }
          ()
        }
      }

      k
    }

    def consumeNext(sub: Flow.Subscriber[? >: T], x: T): Boolean =
      try {
        if (sub != null) sub.onNext(x)
        true
      } // force fmt style
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
    def subscribeOnOpen(sub: Flow.Subscriber[? >: T]): Unit = {
      val _ctl = ctl.get()
      if (((_ctl & CtlFlag.OPEN) == 0)
          && {
            ctl.set(_ctl | CtlFlag.OPEN)
            (_ctl & CtlFlag.OPEN) == 0
          })
        consumeSubscribe(sub)
    }

    def consumeSubscribe(sub: Flow.Subscriber[? >: T]): Unit =
      try if (sub != null) sub.onSubscribe(this) // ignore if disabled
      catch {
        case exc: Throwable =>
          closeOnError(sub, exc)
      }

    /** Issues subscriber.onComplete unless already closed. */
    def closeOnComplete(sub: Flow.Subscriber[? >: T]): Unit = {
      val _ctl = ctl.get()
      ctl.set(_ctl | CtlFlag.CLOSED)
      if ((_ctl & CtlFlag.CLOSED) == 0) consumeComplete(sub)
    }

    def consumeComplete(sub: Flow.Subscriber[? >: T]): Unit =
      try {
        if (sub != null) sub.onComplete()
      } catch {
        case ignore: Throwable => ()
      }

    /** Issues subscriber.onError, and unblocks producer if needed. */
    def closeOnError(sub: Flow.Subscriber[? >: T], exc: Throwable): Unit = {
      var _exc = exc
      val _ctl = ctl.get()
      ctl.set(_ctl | CtlFlag.ERROR | CtlFlag.CLOSED)
      if ((_ctl & CtlFlag.CLOSED) == 0) {
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
        if (exc != null && sub != null) sub.onError(exc)
      } catch {
        case ignore: Throwable => ()
      }

    //
    // Blocking support
    //

    /** Unblocks waiting producer. */
    def signalWaiter(): Unit = {
      var w: Thread = null
      waiting.set(0)
      if (waiter != null) LockSupport.unpark(w)
    }

    /** Returns true if closed or space available. For ManagedBlocker. */
    def isReleasable(): Boolean = {
      var cap = 0

      (ctl.get() & CtlFlag.CLOSED) != 0 // closed
        || (
          buffer != null
          && { cap = buffer.length(); cap > 0 }
          && buffer.getAcquire((cap - 1) & tail.get()) == null
        )
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

      boundary {
        while (!isReleasable()) {
          if (Thread.interrupted()) {
            timeout = BufferedSubscription.INTERRUPTED
            if (timed) break(())
          } // force fmt style
          else if (timed && {
                nanos = deadline - System.nanoTime(); nanos <= 0L
              })
            break(())
          else if (waiter == null) waiter = Thread.currentThread()
          else if (waiting == 0) waiting.set(1)
          else if (timed) LockSupport.parkNanos(this, nanos)
          else LockSupport.park(this)
        }
        ()
      }

      waiter = null
      waiting.set(0)
      true
    }

  }

}
