/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.io.Serializable
import java.util._
import java.util.RandomAccess
import java.util.concurrent.locks.LockSupport
import java.lang.invoke.VarHandle

import scala.scalanative.libc.atomic._
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.scalanative.annotation.alwaysinline

import scala.annotation.tailrec

abstract class ForkJoinTask[V]() extends Future[V] with Serializable {
  import ForkJoinTask._

  // Fields
  // accessed directly by pool and workers
  @volatile private[concurrent] var status: Int = 0
  @volatile private var aux: Aux = _ // either waiters or thrown Exception

  // Support for atomic operations
  private val statusAtomic = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "status"))
  )
  private val auxAtomic = new CAtomicRef[Aux](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "aux"))
  )
  @alwaysinline private def getAndBitwiseOrStatus(v: Int): Int =
    statusAtomic.fetchOr(v)
  @alwaysinline private def casStatus(expected: Int, value: Int): Boolean =
    statusAtomic.compareExchangeStrong(expected, value)
  @alwaysinline private def casAux(c: Aux, v: Aux): Boolean =
    auxAtomic.compareExchangeStrong(c, v)

  private[concurrent] final def markPoolSubmission(): Unit =
    getAndBitwiseOrStatus(POOLSUBMIT)

  private def signalWaiters(): Unit = {
    var a: Aux = aux
    while ({ a = aux; a != null } && a.ex == null) {
      if (casAux(a, null)) { // detach entire list
        while (a != null) {
          val t = a.thread
          if ((t ne Thread.currentThread()) && t != null)
            LockSupport.unpark(t) // don't self signal
          a = a.next
        }
        return
      }
    }
  }

  private def setDone() = {
    val s = getAndBitwiseOrStatus(DONE) | DONE
    signalWaiters()
    s
  }

  private def trySetCancelled(): Int = {
    var s = status
    while ({
      s = status
      s >= 0 && !casStatus(s, { s |= (DONE | ABNORMAL); s })
    }) ()
    signalWaiters()
    s
  }

  private[concurrent] final def trySetThrown(ex: Throwable): Int = {
    val h = new Aux(Thread.currentThread(), ex)
    var p: Aux = null
    var installed = false
    var s = status
    var break = false
    while (!break && { s = status; s >= 0 }) {
      val a = aux
      if (!installed && {
            (a == null || a.ex == null) && {
              installed = casAux(a, h)
              installed
            }
          }) p = a // list of waiters replaced by h
      if (installed && casStatus(s, { s |= (DONE | ABNORMAL | THROWN); s }))
        break = true
    }
    while (p != null) {
      LockSupport.unpark(p.thread)
      p = p.next
    }
    s
  }

  private[concurrent] def trySetException(ex: Throwable) = trySetThrown(ex)

  private[concurrent] final def doExec(): Int = {
    var s = status
    if (s >= 0) {
      val completed =
        try exec()
        catch {
          case rex: Throwable =>
            s = trySetException(rex)
            false
        }
      if (completed) s = setDone()
    }
    s
  }

  private def awaitDone(
      how: Int,
      deadline: Long
  ): Int = {
    val timed = (how & TIMED) != 0
    var owned, uncompensate = false
    var s: Int = -1
    var q: ForkJoinPool.WorkQueue = null
    var p: ForkJoinPool = null
    Thread.currentThread() match {
      case wt: ForkJoinWorkerThread =>
        owned = true
        q = wt.workQueue
        p = wt.pool
      case t =>
        p = ForkJoinPool.common
        if (p != null && (how & POOLSUBMIT) == 0)
          q = p.externalQueue()
    }
    if (q != null && p != null) { // try helping
      if (isInstanceOf[CountedCompleter[_]])
        s = p.helpComplete(this, q, owned, timed)
      else if ((how & RAN) != 0 || {
            s = q.tryRemoveAndExec(this, owned); s >= 0
          })
        s = if (owned) p.helpJoin(this, q, timed) else 0
      if (s < 0)
        return s
      if (s == UNCOMPENSATE)
        uncompensate = true
    }
    var node: Aux = null: Aux
    var ns = 0L
    var interrupted, queued, break = false
    while (!break) {
      var a: Aux = null: Aux
      if ({ s = status; s < 0 })
        break = true
      else if (node == null)
        node = new Aux(Thread.currentThread(), null)
      else if (!queued) {
        if (({ a = aux; a == null || a.ex == null }) && {
              node.next = a
              queued = casAux(a, node)
              queued
            })
          LockSupport.setCurrentBlocker(this)
      } else if (timed && { ns = deadline - System.nanoTime(); ns <= 0 }) {
        s = 0
        break = true
      } else if (Thread.interrupted()) {
        interrupted = true
        if ((how & POOLSUBMIT) != 0 && p != null && p.runState < 0)
          cancelIgnoringExceptions(this) // cancel on shutdown
        else if ((how & INTERRUPTIBLE) != 0) {
          s = ABNORMAL
          break = true
        }
      } else if ({ s = status; s < 0 }) // recheck
        break = true
      else if (timed)
        LockSupport.parkNanos(ns)
      else
        LockSupport.park()
    }
    if (uncompensate)
      p.uncompensate()

    if (queued) {
      LockSupport.setCurrentBlocker(null)
      if (s >= 0) {
        // outer:
        var breakOuter = false
        var a: Aux = aux
        while (!breakOuter && { a = aux; a != null } && a.ex == null) {
          var trail: Aux = null
          var break = false
          while (!break) {
            val next = a.next
            if (a eq node) {
              if (trail != null)
                trail.casNext(trail, next)
              else if (casAux(a, next)) {
                breakOuter = true
              }
              break = true
            } else {
              trail = a
              a = next
              if (a == null) {
                break = true
                breakOuter = true
              }
            }
          }
        }
      } else {
        signalWaiters() // help clean or signal
        if (interrupted)
          Thread.currentThread().interrupt()
      }
    }
    s
  }

  private def getThrowableException(): Throwable = {
    val a = aux
    val ex = if (a != null) a.ex else null
    // if(ex != nulll && a.thread != Thread.currentThread()){
    //   // JSR166 used reflective initialization here
    // }
    ex
  }

  private def getException(s: Int): Throwable = {
    var ex: Throwable = null
    if ((s & ABNORMAL) != 0 && {
          ex = getThrowableException()
          ex == null
        }) ex = new CancellationException()
    ex
  }

  private def reportException(s: Int): Unit = uncheckedThrow[RuntimeException] {
    getThrowableException()
  }

  private def reportExecutionException(s: Int): Unit = {
    val exception: Throwable =
      if (s == ABNORMAL) new InterruptedException()
      else if (s >= 0) new TimeoutException()
      else
        getThrowableException() match {
          case null => null
          case ex   => new ExecutionException(ex)
        }
    uncheckedThrow[RuntimeException](exception)
  }

  final def fork(): ForkJoinTask[V] = {
    VarHandle.storeStoreFence()
    def push(p: ForkJoinPool, q: ForkJoinPool.WorkQueue) = q.push(this, p, true)
    Thread.currentThread() match {
      case wt: ForkJoinWorkerThread =>
        val p = wt.pool
        val q = wt.workQueue
        push(p, q)
      case _ =>
        val p = ForkJoinPool.common
        val q = p.submissionQueue(false)
        push(p, q)
    }
    this
  }

  final def join(): V = {
    var s = status
    if (s >= 0) s = awaitDone(s & POOLSUBMIT, 0L)
    if ((s & ABNORMAL) != 0) reportException(s)
    getRawResult()
  }

  final def invoke(): V = {
    var s = doExec()
    if (s >= 0)
      s = awaitDone(RAN, 0L)
    if ((s & ABNORMAL) != 0)
      reportException(s)
    getRawResult()
  }

  override def cancel(mayInterruptIfRunning: Boolean): Boolean =
    (trySetCancelled() & (ABNORMAL | THROWN)) == ABNORMAL
  override final def isDone(): Boolean = status < 0
  override final def isCancelled(): Boolean =
    (status & (ABNORMAL | THROWN)) == ABNORMAL

  final def isCompletedAbnormally(): Boolean = (status & ABNORMAL) != 0

  final def isCompletedNormally(): Boolean =
    (status & (DONE | ABNORMAL)) == DONE

  override def state(): Future.State = {
    val s = status
    if (s >= 0) Future.State.RUNNING
    else if ((s & (DONE | ABNORMAL)) == DONE) Future.State.SUCCESS
    else if ((s & (ABNORMAL | THROWN)) == (ABNORMAL | THROWN))
      Future.State.FAILED
    else Future.State.CANCELLED
  }

  override def resultNow(): V = {
    if (!isCompletedNormally())
      throw new IllegalStateException()
    getRawResult()
  }

  override def exceptionNow(): Throwable = {
    if ((status & (ABNORMAL | THROWN)) != (ABNORMAL | THROWN))
      throw new IllegalStateException()
    getThrowableException()
  }

  final def getException(): Throwable = getException(status)

  def completeExceptionally(ex: Throwable): Unit = trySetException {
    ex match {
      case _: RuntimeException | _: Error => ex
      case ex                             => new RuntimeException(ex)
    }
  }

  def complete(value: V): Unit = {
    try setRawResult(value)
    catch {
      case rex: Throwable =>
        trySetException(rex)
        return
    }
    setDone()
  }

  final def quietlyComplete(): Unit = setDone()

  @throws[InterruptedException]
  @throws[ExecutionException]
  override final def get(): V = {
    var s = -1
    if (Thread.interrupted())
      s = ABNORMAL
    else if ({ s = status; s >= 0 })
      s = awaitDone((s & POOLSUBMIT) | INTERRUPTIBLE, 0L)
    if ((s & ABNORMAL) != 0)
      reportExecutionException(s)
    getRawResult()
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  override final def get(timeout: Long, unit: TimeUnit): V = {
    var s = -1
    val nanos = unit.toNanos(timeout)
    if (Thread.interrupted())
      s = ABNORMAL
    else if ({ s = status; s >= 0 } && nanos > 0L)
      s = awaitDone(
        (s & POOLSUBMIT) | INTERRUPTIBLE | TIMED,
        nanos + System.nanoTime()
      )
    if (s >= 0 || (s & ABNORMAL) != 0)
      reportExecutionException(s)
    getRawResult()
  }

  final def quietlyJoin(): Unit = {
    val s = status
    if (s >= 0)
      awaitDone(s & POOLSUBMIT, 0L)
  }

  final def quietlyInvoke(): Unit = {
    if (doExec() >= 0)
      awaitDone(RAN, 0L)
  }

  // since JDK 19
  final def quietlyJoin(timeout: Long, unit: TimeUnit): Boolean = {
    val nanos = unit.toNanos(timeout)
    var s = -1
    if (Thread.interrupted())
      s = ABNORMAL
    else if ({ s = status; s >= 0 } && nanos > 0L)
      s = awaitDone(
        (s & POOLSUBMIT) | INTERRUPTIBLE | TIMED,
        nanos + System.nanoTime()
      )
    if (s == ABNORMAL)
      throw new InterruptedException()
    else
      s < 0
  }

  // Since JDK 19
  final def quietlyJoinUninterruptibly(
      timeout: Long,
      unit: TimeUnit
  ): Boolean = {
    val nanos = unit.toNanos(timeout)
    var s = status
    if (s >= 0 && nanos > 0L)
      s = awaitDone((s & POOLSUBMIT) | TIMED, nanos + System.nanoTime())
    s < 0
  }

  def reinitialize(): Unit = {
    aux = null
    status = 0
  }

  def tryUnfork(): Boolean = Thread.currentThread() match {
    case worker: ForkJoinWorkerThread =>
      val q = worker.workQueue
      q != null && q.tryUnpush(this, true)
    case _ =>
      val q = ForkJoinPool.commonQueue()
      q != null && q.tryUnpush(this, false)
  }

  def getRawResult(): V

  protected def setRawResult(value: V): Unit

  protected def exec(): Boolean

  final def getForkJoinTaskTag(): Short = status.toShort

  @tailrec
  final def setForkJoinTaskTag(newValue: Short): Short = {
    val s = status
    if (casStatus(s, (s & ~SMASK) | (newValue & SMASK))) s.toShort
    else setForkJoinTaskTag(newValue)
  }

  @tailrec
  final def compareAndSetForkJoinTaskTag(
      expect: Short,
      update: Short
  ): Boolean = {
    val s = status
    if (s.toShort != expect) false
    else if (casStatus(s, (s & ~SMASK) | (update & SMASK))) true
    else compareAndSetForkJoinTaskTag(expect, update)
  }
}

object ForkJoinTask {

  final private[concurrent] class Aux(
      val thread: Thread,
      val ex: Throwable // null if a waiter
  ) {
    var next: Aux = _ // accessed only via memory-acquire chains
    final private val nextAtomic =
      new CAtomicRef[Aux](fromRawPtr(Intrinsics.classFieldRawPtr(this, "next")))
    final def casNext(c: Aux, v: Aux) = nextAtomic.compareExchangeStrong(c, v)
  }

  private final val DONE = 1 << 31 // must be negative
  private final val ABNORMAL = 1 << 16
  private final val THROWN = 1 << 17
  private final val SMASK = 0xffff // short bits for tags
  private final val UNCOMPENSATE = 1 << 16 // helpJoin return sentinel
  private final val POOLSUBMIT = 1 << 18 // for pool.submit vs fork

  // flags for awaitDone (in addition to above)
  private final val RAN = 1;
  private final val INTERRUPTIBLE = 2;
  private final val TIMED = 4;

  private[concurrent] def isExceptionalStatus(s: Int) = (s & THROWN) != 0

  private[concurrent] def cancelIgnoringExceptions(t: Future[_]): Unit = {
    if (t != null)
      try t.cancel(true)
      catch { case _: Throwable => () }
  }

  /** A version of "sneaky throw" to relay exceptions in other contexts.
   */
  private[concurrent] def rethrow(ex: Throwable): Unit = {
    uncheckedThrow[RuntimeException](ex)
  }

  private[concurrent] def uncheckedThrow[T <: Throwable](t: Throwable): Unit = {
    // In the Java t would need to be casted to T to satisfy exceptions handling
    // however in Scala we don't have a checked exceptions so throw exception as it is
    t match {
      case null => throw new CancellationException()
      case _    => throw t
    }
  }

  def invokeAll(t1: ForkJoinTask[_], t2: ForkJoinTask[_]): Unit = {
    if (t1 == null || t2 == null) throw new NullPointerException
    t2.fork()
    var s1 = t1.doExec()
    if (s1 >= 0)
      s1 = t1.awaitDone(RAN, 0L)
    if ((s1 & ABNORMAL) != 0) {
      cancelIgnoringExceptions(t2)
      t1.reportException(s1)
    } else {
      var s2 = t2.status
      if (s2 >= 0)
        s2 = t2.awaitDone(0, 0L)
      if ((s2 & ABNORMAL) != 0)
        t2.reportException(s2)
    }
  }

  def invokeAll(tasks: Array[ForkJoinTask[_]]): Unit = {
    var ex = null: Throwable
    val last = tasks.length - 1
    var i = last
    var break = false
    while (!break && i >= 0) {
      val t = tasks(i)
      if (t == null) {
        ex = new NullPointerException()
        break = true
      } else if (i == 0) {
        var s = t.doExec()
        if (s >= 0)
          s = t.awaitDone(RAN, 0L)
        if ((s & ABNORMAL) != 0)
          ex = t.getException(s)
        break = true
      } else {
        t.fork()
      }
      i -= 1
    }

    i = 1
    break = false
    if (ex == null) while (!break && i <= last) {
      val t = tasks(i)
      if (t != null) {
        var s = t.status
        if (s >= 0)
          s = t.awaitDone(0, 0L)
        if ((s & ABNORMAL) != 0 && { ex = t.getException(s); ex != null })
          break = true
      }
      i += 1
    }
    if (ex != null) {
      for (i <- 1 to last)
        cancelIgnoringExceptions(tasks(i))
      rethrow(ex)
    }
  }

  def invokeAll[T <: ForkJoinTask[_]](tasks: Collection[T]): Collection[T] = {
    def invokeAllImpl(ts: java.util.List[_ <: ForkJoinTask[_]]): Unit = {
      var ex: Throwable = null
      val last = ts.size() - 1 // nearly same as array version
      var i = last
      var break = false
      while (!break && i >= 0) {
        val t = ts.get(i)
        if (t == null) {
          ex = new NullPointerException()
          break = true
        } else if (i == 0) {
          var s = t.doExec()
          if (s >= 0)
            s = t.awaitDone(RAN, 0L)
          if ((s & ABNORMAL) != 0)
            ex = t.getException(s)
          break = true
        } else {
          t.fork()
        }
        i -= 1
      }

      i = 1
      break = false
      if (ex == null) while (!break && i <= last) {
        val t = ts.get(i)
        if (t != null) {
          var s = t.status
          if (s >= 0)
            s = t.awaitDone(0, 0L)
          if ((s & ABNORMAL) != 0 && { ex = t.getException(s); ex != null })
            break = true
        }
        i += 1
      }
      if (ex != null) {
        for (i <- 1 to last)
          cancelIgnoringExceptions(ts.get(i))
        rethrow(ex)
      }
    }

    tasks match {
      case list: java.util.List[T] with RandomAccess @unchecked =>
        invokeAllImpl(list)
      case _ =>
        invokeAll(tasks.toArray(Array.empty[ForkJoinTask[_]]))
    }
    tasks
  }

  def helpQuiesce(): Unit =
    ForkJoinPool.helpQuiescePool(null, java.lang.Long.MAX_VALUE, false);

  def getPool(): ForkJoinPool = {
    Thread.currentThread() match {
      case t: ForkJoinWorkerThread => t.pool
      case _                       => null
    }
  }

  def inForkJoinPool(): Boolean =
    Thread.currentThread().isInstanceOf[ForkJoinWorkerThread]

  def getQueuedTaskCount(): Int = {
    val q = Thread.currentThread() match {
      case t: ForkJoinWorkerThread => t.workQueue
      case _                       => ForkJoinPool.commonQueue()
    }
    if (q == null) 0 else q.queueSize()
  }

  def getSurplusQueuedTaskCount(): Int =
    ForkJoinPool.getSurplusQueuedTaskCount()

  protected def peekNextLocalTask(): ForkJoinTask[_] = {
    val q = Thread.currentThread() match {
      case t: ForkJoinWorkerThread => t.workQueue
      case _                       => ForkJoinPool.commonQueue()
    }
    if (q == null) null else q.peek()
  }

  protected def pollNextLocalTask(): ForkJoinTask[_] = {
    Thread.currentThread() match {
      case t: ForkJoinWorkerThread => t.workQueue.nextLocalTask()
      case _                       => null
    }
  }

  protected def pollTask(): ForkJoinTask[_] =
    Thread.currentThread() match {
      case wt: ForkJoinWorkerThread => wt.pool.nextTaskFor(wt.workQueue)
      case _                        => null
    }

  protected def pollSubmission(): ForkJoinTask[_] =
    Thread.currentThread() match {
      case t: ForkJoinWorkerThread => t.pool.pollSubmission()
      case _                       => null
    }

  @SerialVersionUID(5232453952276885070L)
  final private[concurrent] class AdaptedRunnable[T] private[concurrent] (
      val runnable: Runnable,
      var result: T
  ) // OK to set this even before completion
      extends ForkJoinTask[T]
      with RunnableFuture[T] {
    if (runnable == null) throw new NullPointerException
    override final def getRawResult(): T = result
    override final def setRawResult(v: T): Unit = { result = v }
    override final def exec(): Boolean = {
      runnable.run()
      true
    }
    override final def run(): Unit = invoke()
    override def toString(): String =
      super.toString + "[Wrapped task = " + runnable + "]"
  }

  @SerialVersionUID(5232453952276885070L)
  final private[concurrent] class AdaptedRunnableAction private[concurrent] (
      val runnable: Runnable
  ) extends ForkJoinTask[Void]
      with RunnableFuture[Void] {
    if (runnable == null) throw new NullPointerException
    override final def getRawResult(): Void = null
    override final def setRawResult(v: Void): Unit = {}
    override final def exec(): Boolean = {
      runnable.run()
      true
    }
    override final def run(): Unit = invoke()
    override def toString(): String =
      super.toString + "[Wrapped task = " + runnable + "]"
  }

  @SerialVersionUID(5232453952276885070L)
  final private[concurrent] class RunnableExecuteAction private[concurrent] (
      val runnable: Runnable
  ) extends ForkJoinTask[Void] {
    if (runnable == null) throw new NullPointerException
    override final def getRawResult(): Void = null
    override final def setRawResult(v: Void): Unit = ()
    override final def exec(): Boolean = {
      runnable.run()
      true
    }
    override private[concurrent] def trySetException(ex: Throwable) = {
// if a handler, invoke it
      val s: Int = trySetThrown(ex)
      if (isExceptionalStatus(s)) {
        val t: Thread = Thread.currentThread()
        val h: Thread.UncaughtExceptionHandler = t.getUncaughtExceptionHandler()
        if (h != null)
          try h.uncaughtException(t, ex)
          catch { case _: Throwable => () }
      }
      s
    }
  }

  @SerialVersionUID(2838392045355241008L)
  final private[concurrent] class AdaptedCallable[T] private[concurrent] (
      val callable: Callable[T]
  ) extends ForkJoinTask[T]
      with RunnableFuture[T] {
    if (callable == null) throw new NullPointerException
    private[concurrent] var result: T = _
    override final def getRawResult() = result
    override final def setRawResult(v: T): Unit = result = v
    override final def exec(): Boolean = try {
      result = callable.call()
      true
    } catch {
      case rex: RuntimeException => throw rex
      case ex: Exception         => throw new RuntimeException(ex)
    }
    override final def run(): Unit = invoke()
    override def toString(): String =
      super.toString + "[Wrapped task = " + callable + "]"
  }
  @SerialVersionUID(2838392045355241008L)
  final private[concurrent] class AdaptedInterruptibleCallable[T](
      val callable: Callable[T]
  ) extends ForkJoinTask[T]
      with RunnableFuture[T] {
    if (callable == null) throw new NullPointerException
    @volatile var runner: Thread = _
    private var result: T = _
    override final def getRawResult(): T = result
    override final def setRawResult(v: T): Unit = result = v
    override final def exec(): Boolean = {
      Thread.interrupted()
      runner = Thread.currentThread()
      try {
        if (!isDone()) result = callable.call()
        true
      } catch {
        case rex: RuntimeException => throw rex
        case ex: Exception         => throw new RuntimeException(ex)
      } finally {
        runner = null
        Thread.interrupted()
      }
    }
    override final def run(): Unit = invoke()
    override final def cancel(mayInterruptIfRunning: Boolean): Boolean = {
      val status = super.cancel(false)
      if (mayInterruptIfRunning) runner match {
        case null => ()
        case t =>
          try t.interrupt()
          catch { case _: Throwable => () }
      }
      status
    }
    override def toString(): String =
      super.toString + "[Wrapped task = " + callable + "]"
  }

  def adapt(runnable: Runnable): ForkJoinTask[_] =
    new AdaptedRunnableAction(runnable)

  def adapt[T](runnable: Runnable, result: T): ForkJoinTask[T] =
    new AdaptedRunnable[T](runnable, result)

  def adapt[T](callable: Callable[T]): ForkJoinTask[T] =
    new AdaptedCallable[T](callable)

  // since JDK 19
  def adaptInterruptible[T](callable: Callable[T]): ForkJoinTask[T] =
    new AdaptedInterruptibleCallable[T](callable)
}
