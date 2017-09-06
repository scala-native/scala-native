package java.util.concurrent

// Ported from Harmony

import locks._
import scala.scalanative.runtime.CAtomicRef
import scala.scalanative.runtime.CAtomicsImplicits._

class FutureTask[V] extends RunnableFuture[V] {

  private final var sync: Sync = _

  def this(callable: Callable[V]) = {
    this()
    if (callable == null) throw new NullPointerException()
    sync = new Sync(callable)
  }

  def this(runnable: Runnable, result: V) = {
    this()
    sync = new Sync(Executors.callable(runnable, result))
  }

  override def isCancelled: Boolean = sync.innerIsCancelled

  override def isDone: Boolean = sync.innerIsDone

  override def cancel(mayInterruptsIfRunning: Boolean): Boolean =
    sync.innerCancel(mayInterruptsIfRunning)

  override def get(): V = sync.innerGet

  override def get(timeout: Long, unit: TimeUnit): V =
    sync.innerGet(unit.toNanos(timeout))

  protected def done(): Unit = {}

  protected def set(v: V): Unit = sync.innerSet(v)

  protected def setException(t: Throwable): Unit = sync.innerSetException(t)

  override def run(): Unit = sync.innerRun()

  protected def runAndReset: Boolean = sync.innerRunAndReset

  private final class Sync(val callable: Callable[V])
      extends AbstractQueuedSynchronizer {

    import Sync._

    /** The result to return from get() */
    private var result: V = null.asInstanceOf[V]

    /** The exception to throw from get() */
    private var exception: Throwable = null

    /**
     * The thread running task. When nulled after set/cancel, this
     * indicates that the results are accessible.  Must be
     * volatile, to ensure visibility upon completion.
     */
    private val runner: CAtomicRef[Thread] = CAtomicRef[Thread]()

    private def ranOrCancelled(state: Int): Boolean =
      (state & (RAN | CANCELLED)) != 0

    override protected def tryAcquireShared(ignore: Int): Int =
      if (innerIsDone) 1 else -1

    override def tryReleaseShared(ignore: Int): Boolean = {
      runner.store(null.asInstanceOf[Thread])
      true
    }

    def innerIsCancelled: Boolean = getState == CANCELLED

    def innerIsDone: Boolean =
      ranOrCancelled(getState) && runner == null.asInstanceOf[Thread]

    def innerGet: V = {
      acquireSharedInterruptibly(0)
      if (getState == CANCELLED)
        throw new CancellationException()
      if (exception != null)
        throw new ExecutionException(exception)
      result
    }

    def innerGet(nanosTimeout: Long): V = {
      if (!tryAcquireSharedNanos(0, nanosTimeout))
        throw new TimeoutException()
      if (getState == CANCELLED)
        throw new CancellationException()
      if (exception != null)
        throw new ExecutionException(exception)
      result
    }

    def innerSet(v: V): Unit = {
      while (true) {
        val s: Int = getState
        if (s == RAN)
          return
        if (s == CANCELLED) {
          // aggressively release to set runner to null,
          // in case we are racing with a cancel request
          // that will try to interrupt runner
          releaseShared(0)
          return
        }
        if (compareAndSetState(s, RAN)) {
          result = v
          releaseShared(0)
          done()
          return
        }

      }
    }

    def innerSetException(t: Throwable): Unit = {
      while (true) {
        val s: Int = getState
        if (s == RAN)
          return
        if (s == CANCELLED) {
          // aggressively release to set runner to null,
          // in case we are racing with a cancel request
          // that will try to interrupt runner
          releaseShared(0)
          return
        }
        if (compareAndSetState(s, RAN)) {
          exception = t
          releaseShared(0)
          done()
          return
        }

      }
    }

    def innerCancel(mayInterruptIfRunning: Boolean): Boolean = {
      var break: Boolean = false
      while (!break) {
        val s: Int = getState
        if (ranOrCancelled(s))
          return false
        if (compareAndSetState(s, CANCELLED))
          break = true
      }
      if (mayInterruptIfRunning) {
        val r: Thread = runner
        if (r != null)
          r.interrupt()
      }
      releaseShared(0)
      done()
      true
    }

    def innerRun(): Unit = {
      if (!compareAndSetState(READY, RUNNING))
        return

      runner.store(Thread.currentThread())
      if (getState == RUNNING) { // recheck after setting thread
        var result: V = null.asInstanceOf[V]
        try {
          result = callable.call()
        } catch {
          case ex: Throwable => {
            setException(ex)
            return
          }
        }
        set(result)
      } else {
        releaseShared(0) // cancel
      }
    }

    def innerRunAndReset: Boolean = {
      if (!compareAndSetState(READY, RUNNING))
        return false
      try {
        runner.store(Thread.currentThread())
        if (getState == RUNNING)
          callable.call() // don't set result
        runner.store(null.asInstanceOf[Thread])
        compareAndSetState(RUNNING, READY)
      } catch {
        case ex: Throwable => {
          setException(ex)
          false
        }
      }
    }

  }
  object Sync {

    private final val serialVersionUID: Long = -7828117401763700385L

    /** State value representing that task is ready to run */
    private final val READY: Int = 0

    /** State value representing that task is running */
    private final val RUNNING: Int = 1

    /** State value representing that task ran */
    private final val RAN: Int = 2

    /** State value representing that task was cancelled */
    private final val CANCELLED: Int = 4

  }

}
