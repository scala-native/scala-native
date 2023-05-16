/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
import java.util.concurrent.locks.LockSupport
import scalanative.libc.atomic.{CAtomicInt, CAtomicRef}
import scalanative.libc.atomic.memory_order._

import scalanative.runtime.{fromRawPtr, Intrinsics}

object FutureTask {
  private final val NEW = 0
  private final val COMPLETING = 1
  private final val NORMAL = 2
  private final val EXCEPTIONAL = 3
  private final val CANCELLED = 4
  private final val INTERRUPTING = 5
  private final val INTERRUPTED = 6

  final private[concurrent] class WaitNode(@volatile var thread: Thread) {
    @volatile var next: WaitNode = _
    def this() = this(Thread.currentThread())
  }
}

class FutureTask[V <: AnyRef](private var callable: Callable[V])
    extends RunnableFuture[V] {
  if (callable == null) throw new NullPointerException()
  import FutureTask._

  @volatile private var _state = NEW

  @volatile private var runner: Thread = _

  @volatile private var waiters: WaitNode = _

  private var outcome: AnyRef = _

  private val atomicState = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "_state"))
  )
  private val atomicRunner = new CAtomicRef[Thread](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "runner"))
  )
  private val atomicWaiters = new CAtomicRef[WaitNode](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "waiters"))
  )

  @throws[ExecutionException]
  private def report(s: Int): V = {
    val x = outcome
    if (s == NORMAL) return x.asInstanceOf[V]
    if (s >= CANCELLED) throw new CancellationException
    throw new ExecutionException(x.asInstanceOf[Throwable])
  }

  def this(runnable: Runnable, result: V) =
    this(Executors.callable(runnable, result))

  override def isCancelled(): Boolean = _state >= CANCELLED
  override def isDone(): Boolean = _state != NEW
  override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
    def newState = if (mayInterruptIfRunning) INTERRUPTING else CANCELLED
    if (!(_state == NEW &&
          atomicState.compareExchangeStrong(NEW, newState))) return false
    try { // in case call to interrupt throws exception
      if (mayInterruptIfRunning) try {
        val t = runner
        if (t != null) t.interrupt()
      } finally atomicState.store(INTERRUPTED, memory_order_release)
    } finally finishCompletion()
    true
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  override def get(): V = {
    var s = _state
    if (s <= COMPLETING) s = awaitDone(false, 0L)
    report(s)
  }
  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  override def get(timeout: Long, unit: TimeUnit): V = {
    if (unit == null) throw new NullPointerException
    var s = _state
    if (s <= COMPLETING && {
          s = awaitDone(true, unit.toNanos(timeout))
          s <= COMPLETING
        }) throw new TimeoutException
    report(s)
  }

  override def resultNow(): V = state() match {
    case Future.State.SUCCESS => outcome.asInstanceOf[V]
    case Future.State.FAILED =>
      throw new IllegalStateException("Task completed with exception");
    case Future.State.CANCELLED =>
      throw new IllegalStateException("Task was cancelled");
    case _ => throw new IllegalStateException("Task has not completed");
  }

  override def exceptionNow(): Throwable = state() match {
    case Future.State.SUCCESS =>
      throw new IllegalStateException("Task completed with a result")
    case Future.State.FAILED => outcome.asInstanceOf[Throwable]
    case Future.State.CANCELLED =>
      throw new IllegalStateException("Task was cancelled");
    case _ => throw new IllegalStateException("Task has not completed");
  }

  override def state(): Future.State = {
    var s = _state
    while (s == COMPLETING) {
      // waiting for transition to NORMAL or EXCEPTIONAL
      Thread.`yield`()
      s = _state
    }
    s match {
      case NORMAL                                 => Future.State.SUCCESS;
      case EXCEPTIONAL                            => Future.State.FAILED;
      case CANCELLED | INTERRUPTING | INTERRUPTED => Future.State.CANCELLED;
      case _                                      => Future.State.RUNNING;
    }
  }

  protected def done(): Unit = {}

  protected def set(v: V): Unit = {
    if (atomicState.compareExchangeStrong(NEW, COMPLETING)) {
      outcome = v
      atomicState.store(NORMAL, memory_order_release)
      finishCompletion()
    }
  }

  protected def setException(t: Throwable): Unit = {
    if (atomicState.compareExchangeStrong(NEW, COMPLETING)) {
      outcome = t
      atomicState.store(EXCEPTIONAL, memory_order_release)
      finishCompletion()
    }
  }
  override def run(): Unit = {
    if (_state != NEW || !atomicRunner.compareExchangeStrong(
          null: Thread,
          Thread.currentThread()
        )) return ()
    try {
      val c = callable
      if (c != null && _state == NEW) {
        var result: V = null.asInstanceOf[V]
        var ran = false
        try {
          result = c.call()
          ran = true
        } catch {
          case ex: Throwable =>
            ran = false
            setException(ex)
        }
        if (ran) set(result)
      }
    } finally {
      // runner must be non-null until _state is settled to
      // prevent concurrent calls to run()
      runner = null
      // state must be re-read after nulling runner to prevent
      // leaked interrupts
      val s = _state
      if (s >= INTERRUPTING) handlePossibleCancellationInterrupt(s)
    }
  }

  protected def runAndReset(): Boolean = {
    if (_state != NEW || !atomicRunner.compareExchangeStrong(
          null: Thread,
          Thread.currentThread()
        )) return false
    var ran = false
    var s = _state
    try {
      val c = callable
      if (c != null && s == NEW) try {
        c.call() // don't set result

        ran = true
      } catch { case ex: Throwable => setException(ex) }
    } finally {
      runner = null
      s = _state
      if (s >= INTERRUPTING) handlePossibleCancellationInterrupt(s)
    }
    ran && s == NEW
  }

  private def handlePossibleCancellationInterrupt(s: Int): Unit = {
    // It is possible for our interrupter to stall before getting a
    // chance to interrupt us.  Let's spin-wait patiently.
    if (s == INTERRUPTING)
      while (_state == INTERRUPTING)
        Thread.`yield`() // wait out pending interrupt
    // assert state == INTERRUPTED;
    // We want to clear any interrupt we may have received from
    // cancel(true).  However, it is permissible to use interrupts
    // as an independent mechanism for a task to communicate with
    // its caller, and there is no way to clear only the
    // cancellation interrupt.
    //
    // Thread.interrupted();
  }

  private def finishCompletion(): Unit = {
    // assert state > COMPLETING;
    var q = waiters
    var break = false
    while (!break && { q = waiters; q != null })
      if (atomicWaiters.compareExchangeWeak(q, null: WaitNode)) {
        while (!break) {
          val t = q.thread
          if (t != null) {
            q.thread = null
            LockSupport.unpark(t)
          }
          val next = q.next
          if (next == null) break = true
          else {
            q.next = null // unlink to help gc
            q = next
          }
        }
      }
    done()
    callable = null // to reduce footprint
  }

  @throws[InterruptedException]
  private def awaitDone(timed: Boolean, nanos: Long): Int = {
    // The code below is very delicate, to achieve these goals:
    // - call nanoTime exactly once for each call to park
    // - if nanos <= 0L, return promptly without allocation or nanoTime
    // - if nanos == Long.MIN_VALUE, don't underflow
    // - if nanos == Long.MAX_VALUE, and nanoTime is non-monotonic
    //   and we suffer a spurious wakeup, we will do no worse than
    //   to park-spin for a while
    var startTime = 0L // Special value 0L means not yet parked
    var q = null.asInstanceOf[WaitNode]
    var queued = false

    while (true) {
      val s = _state
      if (s > COMPLETING) {
        if (q != null) q.thread = null
        return s
      } else if (s == COMPLETING) { // We may have already promised (via isDone) that we are done
        // so never return empty-handed or throw InterruptedException
        Thread.`yield`()
      } else if (Thread.interrupted()) {
        removeWaiter(q)
        throw new InterruptedException
      } else if (q == null) {
        if (timed && nanos <= 0L) return s
        q = new WaitNode
      } else if (!queued) {
        q.next = waiters
        queued = atomicWaiters.compareExchangeWeak(waiters, q)
      } else if (timed) {
        var parkNanos = 0L
        if (startTime == 0L) { // first time
          startTime = System.nanoTime()
          if (startTime == 0L) startTime = 1L
          parkNanos = nanos
        } else {
          val elapsed = System.nanoTime() - startTime
          if (elapsed >= nanos) {
            removeWaiter(q)
            return _state
          }
          parkNanos = nanos - elapsed
        }
        // nanoTime may be slow; recheck before parking
        if (_state < COMPLETING) LockSupport.parkNanos(this, parkNanos)
      } else LockSupport.park(this)
    }
    -1 // unreachable
  }

  private def removeWaiter(node: WaitNode): Unit = {
    if (node != null) {
      node.thread = null
      var break = false
      while (!break) { // restart on removeWaiter race
        var pred = null.asInstanceOf[WaitNode]
        var s = null.asInstanceOf[WaitNode]
        var q = waiters
        while (q != null) {
          var continue = false
          s = q.next
          if (q.thread != null) pred = q
          else if (pred != null) {
            pred.next = s
            if (pred.thread == null) { // check for race
              continue = true
            }
          } else if (!atomicWaiters.compareExchangeStrong(q, s)) {
            continue = true
          }
          if (!continue) {
            q = s
          }
        }
        break = true
      }
    }
  }

  override def toString: String = {
    val status = _state match {
      case NORMAL      => "[Completed normally]"
      case EXCEPTIONAL => "[Completed exceptionally: " + outcome + "]"
      case CANCELLED | INTERRUPTED | INTERRUPTING => "[Cancelled]"
      case _ =>
        val callable = this.callable
        if (callable == null) "[Not completed]"
        else "[Not completed, task = " + callable + "]"
    }
    super.toString + status
  }
}
