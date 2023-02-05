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

import scala.scalanative.libc.atomic._
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.scalanative.unsafe.{Ptr, stackalloc}

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
  private def casStatus(expected: Int, value: Int): Boolean =
    statusAtomic.compareExchangeWeak(expected, value)
  private def getAndBitwiseOrStatus(v: Int): Int = statusAtomic.fetchOr(v)
  private def casAux(c: Aux, v: Aux): Boolean =
    auxAtomic.compareExchangeStrong(c, v)

  @tailrec
  private def signalWaiters(): Unit = {
    @tailrec
    def unparkThreads(a: Aux): Unit = {
      if (a != null) {
        val t = a.thread
        if (t != Thread.currentThread() && t != null) {
          LockSupport.unpark(t)
        }
        unparkThreads(a.next)
      }
    }

    aux match {
      case null => ()
      case a =>
        if (a.ex == null) {
          if (casAux(a, null)) unparkThreads(a)
          else signalWaiters()
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
      s >= 0 && {
        val prevStatus = s
        s |= (DONE | ABNORMAL)
        !casStatus(prevStatus, s)
      }
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

  private def awaitJoin(
      ran: Boolean,
      _interruptible: Boolean,
      timed: Boolean,
      nanos: Long
  ): Int = {
    var interruptible = _interruptible
    val (internal, p, q) = Thread.currentThread() match {
      case wt: ForkJoinWorkerThread => (true, wt.pool, wt.workQueue)
      case _ =>
        if (interruptible && Thread.interrupted()) return ABNORMAL
        (false, ForkJoinPool.common, ForkJoinPool.commonQueue())
    }
    var s = status
    if (s < 0) return s
    var deadline = 0L
    if (timed) {
      if (nanos <= 0L) return 0
      else deadline = (nanos + System.nanoTime()).max(1L)
    }

    var uncompensate: ForkJoinPool = null
    if (q != null && p != null) { // try helping
      if ((!timed || p.isSaturated()) && {
            this match {
              case c: CountedCompleter[_] =>
                s = p.helpComplete(this, q, internal)
                s < 0
              case _ =>
                q.tryRemove(this, internal) && {
                  s = doExec()
                  s < 0
                }
            }
          }) return s

      if (internal) {
        s = p.helpJoin(this, q)
        if (s < 0) return s
        if (s == UNCOMPENSATE) uncompensate = p
        interruptible = false;
      }
    }
    awaitDone(interruptible, deadline, uncompensate)
  }

  private def awaitDone(
      interruptible: Boolean,
      deadline: Long,
      pool: ForkJoinPool
  ): Int = {
    var s = 0
    var interrupted = false
    var queued = false
    var parked = false
    var node: Aux = null
    var break = false
    while (!break && { s = status; s >= 0 }) {
      if (parked && Thread.interrupted()) {
        if (interruptible) {
          s = ABNORMAL
          break = true
        } else
          interrupted = true
      } else if (queued) {
        if (deadline != 0L) {
          val ns = deadline - System.nanoTime()
          if (ns <= 0L) break = true
          else LockSupport.parkNanos(ns)
        } else LockSupport.park()
        parked = true
      } else if (node != null) {
        val a = aux
        if (a != null && a.ex != null)
          Thread.onSpinWait() // exception in progress
        else {
          node.next = a
          queued = casAux(a, node)
          if (queued) LockSupport.setCurrentBlocker(this)
        }
      } else {
        try node = new Aux(Thread.currentThread(), null)
        catch {
          // try to cancel if cannot create
          case ex: Throwable => casStatus(s, s | DONE | ABNORMAL)
        }
      }
    }

    if (pool != null) pool.uncompensate()
    if (queued) {
      LockSupport.setCurrentBlocker(null)
      if (s >= 0) { // cancellation similar to AbstractQueuedSynchronizer
        // outer // todo: labels are not supported
        var a = aux
        var breakOuter = false
        while (!breakOuter && { a = aux; a != null && a.ex == null }) {
          var trail: Aux = null
          var break = false
          while (!break || !breakOuter) {
            val next = a.next
            if (a == node) {
              if (trail != null) trail.casNext(trail, next)
              else if (casAux(a, next)) breakOuter = true
              break = true // restart
            } else {

              trail = a
              a = next
              if (next == null) breakOuter = true
            }
          }
        }
      } else {
        signalWaiters() // help clean or signal
        if (interrupted) Thread.currentThread().interrupt()
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
          (s & THROWN) == 0 || {
            ex = getThrowableException()
            ex == null
          }
        }) ex = new CancellationException()
    ex
  }

  private def reportException(s: Int): Unit = {
    uncheckedThrow[RuntimeException](
      if ((s & THROWN) != 0) getThrowableException()
      else null
    )
  }

  private def reportExecutionException(s: Int): Unit = {
    val exception: Throwable =
      if (s == ABNORMAL) new InterruptedException()
      else if (s >= 0) new TimeoutException()
      else if ((s & THROWN) != 0) {
        getThrowableException() match {
          case null => null
          case ex   => new ExecutionException(ex)
        }
      } else null
    uncheckedThrow[RuntimeException](exception)
  }

  final def fork(): ForkJoinTask[V] = {
    Thread.currentThread() match {
      case worker: ForkJoinWorkerThread =>
        worker.workQueue.push(this, worker.pool)
      case _ =>
        ForkJoinPool.commonPool().externalPush(this)
    }
    this
  }

  final def join(): V = {
    var s = status
    if (s >= 0) s = awaitJoin(false, false, false, 0L)
    if ((s & ABNORMAL) != 0) reportException(s)
    getRawResult()
  }

  final def invoke(): V = {
    var s = doExec()
    if (s >= 0) {
      s = awaitJoin(true, false, false, 0L)
    }
    if ((s & ABNORMAL) != 0) reportException(s)
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

  final def getException(): Throwable = getException(status)

  def completeExceptionally(ex: Throwable): Unit = trySetException {
    ex match {
      case _: RuntimeException | _: Error => ex
      case ex                             => new RuntimeException(ex)
    }
  }

  def complete(value: V): Unit = {
    try {
      setRawResult(value)
      setDone()
    } catch {
      case rex: Throwable => trySetException(rex)
    }
  }

  final def quietlyComplete(): Unit = setDone()

  @throws[InterruptedException]
  @throws[ExecutionException]
  override final def get(): V = {
    val s = awaitJoin(false, true, false, 0L)
    if ((s & ABNORMAL) != 0) reportExecutionException(s)
    getRawResult()
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  override final def get(timeout: Long, unit: TimeUnit): V = {
    val s = awaitJoin(false, true, true, unit.toNanos(timeout))
    if (s >= 0 || (s & ABNORMAL) != 0) reportExecutionException(s)
    getRawResult()
  }

  final def quietlyJoin(): Unit = {
    if (status >= 0) awaitJoin(false, false, false, 0L)
  }

  final def quietlyInvoke(): Unit = {
    if (doExec() >= 0) awaitJoin(true, false, false, 0L)
  }

  def reinitialize(): Unit = {
    aux = null
    status = 0
  }

  def tryUnfork(): Boolean = Thread.currentThread() match {
    case worker: ForkJoinWorkerThread =>
      val q = worker.workQueue
      q != null && q.tryUnpush(this)
    case _ =>
      val q = ForkJoinPool.commonQueue()
      q != null && q.externalTryUnpush(this)
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

  @throws[java.io.IOException]
  private def writeObject(s: java.io.ObjectOutputStream): Unit = {
    val a = aux
    s.defaultWriteObject()
    s.writeObject(
      if (a == null) null
      else a.ex
    )
  }

  @throws[java.io.IOException]
  @throws[ClassNotFoundException]
  private def readObject(s: java.io.ObjectInputStream): Unit = {
    s.defaultReadObject()
    val ex = s.readObject
    if (ex != null) trySetThrown(ex.asInstanceOf[Throwable])
  }
}

object ForkJoinTask {

  final private[concurrent] class Aux private[concurrent] (
      val thread: Thread,
      val ex: Throwable // null if a waiter
  ) {
    @volatile var next: Aux = _
    final private val nextAtomic =
      new CAtomicRef[Aux](fromRawPtr(Intrinsics.classFieldRawPtr(this, "next")))
    final def casNext(c: Aux, v: Aux) = nextAtomic.compareExchangeStrong(c, v)
  }

  private final val DONE = 1 << 31 // must be negative
  private final val ABNORMAL = 1 << 16
  private final val THROWN = 1 << 17
  private final val SMASK = 0xffff // short bits for tags
  private final val UNCOMPENSATE = 1 << 16 // helpJoin return sentinel

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
    if (s1 >= 0) s1 = t1.awaitJoin(true, false, false, 0L)
    if ((s1 & ABNORMAL) != 0) {
      cancelIgnoringExceptions(t2)
      t1.reportException(s1)
    } else {
      var s2 = t2.awaitJoin(false, false, false, 0L)
      if ((s2 & ABNORMAL) != 0)
        t2.reportException(s2)
    }
  }

  def invokeAll(tasks: Array[ForkJoinTask[_]]): Unit = {
    var ex = null: Throwable
    val last = tasks.length - 1
    (last to 0 by -1).takeWhile { i =>
      val t = tasks(i)
      if (t == null) {
        ex = new NullPointerException()
        false
      } else if (i == 0) {
        var s = t.doExec()
        if (s >= 0) s = t.awaitJoin(true, false, false, 0L)
        if ((s & ABNORMAL) != 0) ex = t.getException(s)
        false
      } else {
        t.fork()
        true
      }
    }

    if (ex == null) (1 to last).takeWhile { i =>
      val t = tasks(i)
      t == null || {
        var s = t.status
        if (s >= 0) s = t.awaitJoin(false, false, false, 0L)
        if ((s & ABNORMAL) != 0) ex = t.getException(s)
        ex == null
      }
    }
    if (ex != null) {
      for (i <- 1 to last) { cancelIgnoringExceptions(tasks(i)) }
      rethrow(ex)
    }
  }

  def invokeAll[T <: ForkJoinTask[_]](tasks: Collection[T]): Collection[T] = {
    def invokeAllImpl(ts: java.util.List[_ <: ForkJoinTask[_]]): Unit = {
      var ex: Throwable = null
      val last = ts.size() - 1 // nearly same as array version
      (last to 0 by -1).takeWhile { i =>
        ts.get(i) match {
          case null =>
            ex = new NullPointerException()
            false

          case t if i == 0 =>
            var s = t.doExec()
            if (s >= 0) s = t.awaitJoin(true, false, false, 0L)
            if ((s & ABNORMAL) != 0) ex = t.getException(s)
            false

          case t =>
            t.fork()
            true
        }
      }
      if (ex == null) (1 to last).takeWhile { i =>
        val t = ts.get(i)
        t == null || {
          var s = t.status
          if (s >= 0) s = t.awaitJoin(false, false, false, 0L)
          if ((s & ABNORMAL) != 0) {
            ex = t.getException(s)
          }
          ex == null
        }
      }
      if (ex != null) {
        for (i <- 1 to last) cancelIgnoringExceptions((ts.get(i)))
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

  def helpQuiesce(): Unit = {
    Thread.currentThread() match {
      case t: ForkJoinWorkerThread if t.pool != null =>
        t.pool.helpQuiescePool(t.workQueue, java.lang.Long.MAX_VALUE, false)
      case _ =>
        ForkJoinPool.common
          .externalHelpQuiescePool(
            java.lang.Long.MAX_VALUE,
            false
          )
    }
  }

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

  def adapt(runnable: Runnable): ForkJoinTask[_] = new AdaptedRunnableAction(
    runnable
  )

  def adapt[T](runnable: Runnable, result: T): ForkJoinTask[T] =
    new AdaptedRunnable[T](runnable, result)

  def adapt[T](callable: Callable[T]): ForkJoinTask[T] =
    new AdaptedCallable[T](callable)

  def adaptInterruptible[T](callable: Callable[T]): ForkJoinTask[T] =
    new AdaptedInterruptibleCallable[T](callable)
}
