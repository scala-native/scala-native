package scala.concurrent.forkjoin

import java.io.Serializable
import java.lang.ref.{WeakReference, Reference}
import java.lang.ref.ReferenceQueue
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.RunnableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import java.lang.reflect.Constructor

import scalanative.runtime.{CAtomicInt, CAtomicLong}
import scalanative.runtime.CAtomicsImplicits._

abstract class ForkJoinTask[V] extends Future[V] with Serializable {

  import ForkJoinTask._

  // volatile
  var status = CAtomicInt() // accessed directly by pool and workers

  private def setCompletion(completion: Int): Int = {
    var s: Int = 0
    while (true) {
      s = status
      if (s < 0)
        return s
      if (status.compareAndSwapStrong(s, s | completion)) {
        if ((s >>> 16) != 0)
          this.synchronized(notifyAll())
        completion
      }
    }
    // for the compiler
    0
  }

  final def doExec: Int = {
    var s: Int             = status
    var completed: Boolean = false
    if (s >= 0) {
      try completed = exec
      catch {
        case rex: Throwable =>
          return setExceptionalCompletion(rex)
      }
      if (completed) s = setCompletion(NORMAL)
    }
    s
  }

  final def trySetSignal: Boolean = {
    val s: Int = status
    s >= 0 && status.compareAndSwapStrong(s, s | SIGNAL)
  }

  private def externalAwaitDone: Int = {
    var s: Int = status
    ForkJoinPool.externalHelpJoin(this)
    var interrupted: Boolean = false
    while (s >= 0) {
      if (status.compareAndSwapStrong(s, s | SIGNAL)) {
        this.synchronized {
          if (status >= 0) {
            try wait()
            catch {
              case ie: InterruptedException =>
                interrupted = true
            }
          } else notifyAll()
        }

      }
      s = status
    }
    if (interrupted) Thread.currentThread.interrupt()
    s
  }

  private def externalInterruptibleAwaitDone: Int = {
    var s = status
    if (Thread.interrupted)
      throw new InterruptedException
    ForkJoinPool.externalHelpJoin(this)
    while (s >= 0) {
      if (status.compareAndSwapStrong(s, s | SIGNAL)) {
        this synchronized {
          if (status >= 0) wait()
          else notifyAll()
        }

      }
      s = status
    }
    s
  }

  private def doJoin(): Int = {
    var s: Int                    = status
    var t: Thread                 = Thread.currentThread()
    var wt: ForkJoinWorkerThread  = t.asInstanceOf[ForkJoinWorkerThread]
    var w: ForkJoinPool.WorkQueue = wt.workQueue
    if (s < 0)
      s
    else {
      s = doExec
      if (t.isInstanceOf[ForkJoinWorkerThread]) {
        if (w.tryUnpush(this) && s < 0)
          s
        else
          wt.pool.awaitJoin(w, this)
      } else externalAwaitDone
    }
  }

  private def doInvoke(): Int = {
    var s: Int                   = doExec
    var t: Thread                = null
    var wt: ForkJoinWorkerThread = null
    if (s < 0)
      s
    else {
      if ({ t = Thread.currentThread; t }.isInstanceOf[ForkJoinWorkerThread]) {
        wt = t.asInstanceOf[ForkJoinWorkerThread]; wt
      }.pool.awaitJoin(wt.workQueue, this)
      else externalAwaitDone
    }
  }

  // Exception table support

  final def recordExceptionalCompletion(ex: Throwable): Int = {
    var s: Int = status
    if (s >= 0) {
      val h: Int              = System.identityHashCode(this)
      val lock: ReentrantLock = exceptionTableLock
      lock.lock()
      try {
        expungeStaleExceptions()
        val t: Array[ExceptionNode] = exceptionTable
        val i: Int                  = h & (t.length - 1)
        var e: ExceptionNode        = t(i)
        var break: Boolean          = false
        while (!break) {
          if (e == null) {
            t(i) = new ExceptionNode(this, ex, t(i))
            break = true
          }
          if (e.get() == this && !break) // already present
            break = true
          if (!break) e = e.nextNode
        }
      } finally {
        lock.unlock()
      }
      s = setCompletion(EXCEPTIONAL)
    }
    s
  }

  private def setExceptionalCompletion(ex: Throwable): Int = {
    val s: Int = recordExceptionalCompletion(ex)
    if ((s & DONE_MASK) == EXCEPTIONAL)
      internalPropagateException(ex)
    s
  }

  def internalPropagateException(ex: Throwable) = {}

  private def clearExceptionalCompletion(): Unit = {
    val h: Int              = System.identityHashCode(this)
    val lock: ReentrantLock = exceptionTableLock
    lock.lock()
    try {
      val t: Array[ExceptionNode] = exceptionTable
      val i: Int                  = h & (t.length - 1)
      var e: ExceptionNode        = t(i)
      var pred: ExceptionNode     = null
      var break: Boolean          = false
      while (e != null && !break) {
        val next: ExceptionNode = e.nextNode
        if (e.get() == this) {
          if (pred == null)
            t(i) = next
          else
            pred.nextNode = next
          break = true
        }
        if (!break) {
          pred = e
          e = next
        }
      }
      expungeStaleExceptions()
      status.store(0)
    } finally {
      lock.unlock()
    }
  }

  private def getThrowableException: Throwable = {
    if ((status & DONE_MASK) != EXCEPTIONAL)
      return null
    val h: Int              = System.identityHashCode(this)
    var e: ExceptionNode    = null
    val lock: ReentrantLock = exceptionTableLock
    lock.lock()
    try {
      expungeStaleExceptions()
      var t: Array[ExceptionNode] = exceptionTable
      e = t(h & (t.length - 1))
      while (e != null && e.get() != this) e = e.nextNode
    } finally {
      lock.unlock()
    }
    val ex: Throwable = e.ex
    if (e == null || ex == null)
      return null
    if (false && e.thrower != Thread.currentThread().getId) {
      val ec: Class[_ <: Throwable] = ex.getClass
      try {
        val noArgCtor: Constructor[_] = null
        val cs
          : Array[Constructor[_]] = ec.getConstructors() // public ctors only
        var i: Int                = 0
        while (i < cs.length) {
          val c: Constructor[_]   = cs(i)
          val ps: Array[Class[_]] = c.getParameterTypes
          if (ps.length == 1 && ps(0) == classOf[Throwable])
            return c.newInstance(ex).asInstanceOf[Throwable]
          i += 1
        }
        if (noArgCtor != null) {
          val wx: Throwable = noArgCtor.newInstance().asInstanceOf[Throwable]
          wx.initCause(ex)
          return wx
        }
      } catch {
        case ignore: Exception =>
      }
    }
    ex
  }

  def reportException(s: Int): Unit = {
    if (s == CANCELLED)
      throw new CancellationException()
    if (s == EXCEPTIONAL)
      rethrow(getThrowableException)
  }

  // public methods

  def fork: ForkJoinTask[V] = {
    val t: Thread = Thread.currentThread()
    if (t.isInstanceOf[ForkJoinWorkerThread])
      t.asInstanceOf[ForkJoinWorkerThread].workQueue.push(this)
    else
      ForkJoinPool.common.externalPush(this)
    this
  }

  final def join: V = {
    val s: Int = doJoin()
    if ((s & DONE_MASK) != NORMAL)
      reportException(s)
    getRawResult
  }

  final def invoke: V = {
    val s: Int = doInvoke()
    if ((s & DONE_MASK) != NORMAL)
      reportException(s)
    getRawResult
  }

  final def cancel(mayInterruptIfRunning: Boolean): Boolean =
    (setCompletion(CANCELLED) & DONE_MASK) == CANCELLED

  final def isDone: Boolean = status < 0

  final override def isCancelled: Boolean = (status & DONE_MASK) == CANCELLED

  final def isCompletedAbnormally: Boolean = status < NORMAL

  final def isCompletedNormally: Boolean = (status & DONE_MASK) == NORMAL

  final def getException: Throwable = {
    val s: Int = status & DONE_MASK
    if (s >= NORMAL)
      null
    else {
      if (s == CANCELLED)
        new CancellationException()
      else getThrowableException
    }
  }

  def completeExceptionally(ex: Throwable): Unit = {
    setExceptionalCompletion(
      if (ex.isInstanceOf[RuntimeException] || ex.isInstanceOf[Error]) ex
      else new RuntimeException(ex))
  }

  def complete(value: V): Unit = {
    try {
      setRawResult(value)
    } catch {
      case rex: Throwable =>
        setExceptionalCompletion(rex)
        return
    }
    setCompletion(NORMAL)
  }

  final def quietlyComplete(): Unit = {
    setCompletion(NORMAL)
  }

  final def get: V = {
    var s: Int = {
      if (Thread.currentThread().isInstanceOf[ForkJoinWorkerThread])
        doJoin()
      else externalInterruptibleAwaitDone
    }
    var ex: Throwable = null
    s &= DONE_MASK
    if (s == CANCELLED)
      throw new CancellationException()
    ex = getThrowableException
    if (s == EXCEPTIONAL && ex != null)
      throw new ExecutionException(ex)
    getRawResult
  }

  final def get(timeout: Long, unit: TimeUnit): V = {
    if (Thread.interrupted())
      throw new InterruptedException()
    // Messy in part because we measure in nanosecs, but wait in millisecs
    var s: Int   = status
    var ms: Long = 0L
    var ns: Long = unit.toNanos(timeout)
    if (s >= 0 && ns > 0L) {
      val deadline: Long            = System.nanoTime() + ns
      var p: ForkJoinPool           = null
      var w: ForkJoinPool.WorkQueue = null
      val t: Thread                 = Thread.currentThread()
      if (t.isInstanceOf[ForkJoinWorkerThread]) {
        val wt: ForkJoinWorkerThread = t.asInstanceOf[ForkJoinWorkerThread]
        p = wt.pool
        w = wt.workQueue
        p.helpJoinOnce(w, this) // no retries on failure
      } else
        ForkJoinPool.externalHelpJoin(this)
      var canBlock: Boolean    = false
      var interrupted: Boolean = false
      try {
        s = status
        var break: Boolean = false
        while (s >= 0 && !break) {
          if (w != null && w.qlock < 0)
            cancelIgnoringExceptions(this)
          else if (!canBlock) {
            if (p == null || p.tryCompensate)
              canBlock = true
          } else {
            ms = TimeUnit.NANOSECONDS.toMillis(ns)
            if (ms > 0L && status.compareAndSwapStrong(s, s | SIGNAL)) {
              this.synchronized {
                if (status >= 0) {
                  try {
                    wait(ms)
                  } catch {
                    case ie: InterruptedException =>
                      if (p == null)
                        interrupted = true
                  }
                } else
                  notifyAll()
              }
            }
            s = status
            ns = deadline - System.nanoTime()
            if (s < 0 || interrupted || ns <= 0L)
              break = true
          }
          if (!break) s = status
        }
      } finally {
        if (p != null && canBlock)
          p.incrementActiveCount
      }
      if (interrupted)
        throw new InterruptedException()
    }
    s &= DONE_MASK
    if (s != NORMAL) {
      var ex: Throwable = null
      if (s == CANCELLED)
        throw new CancellationException()
      if (s != EXCEPTIONAL)
        throw new TimeoutException()
      ex = getThrowableException
      if (ex != null)
        throw new ExecutionException(ex)
    }
    getRawResult
  }

  final def quietlyJoin(): Unit = doJoin()

  final def quietlyInvoke(): Unit = doInvoke()

  def reinitialize(): Unit = {
    if ((status & DONE_MASK) == EXCEPTIONAL)
      clearExceptionalCompletion()
    else
      status.store(0)
  }

  def tryUnfork: Boolean = {
    val t: Thread = Thread.currentThread()
    if (t.isInstanceOf[ForkJoinWorkerThread])
      t.asInstanceOf[ForkJoinWorkerThread].workQueue.tryUnpush(this)
    else ForkJoinPool.tryExternalUnpush(this)
  }

  // Extension methods

  def getRawResult: V

  protected def setRawResult(value: V): Unit

  protected def exec: Boolean

  // tag operations

  final def getForkJoinTaskTag: Short = status toShort

  final def setForkJoinTaskTag(tag: Short) = {
    var s: Int = 0
    while (true) {
      s = status
      if (status.compareAndSwapStrong(s, (s & ~SMASK) | (tag & SMASK)))
        s toShort
    }
  }

  final def compareAndSetForkJoinTaskTag(e: Short, tag: Short): Boolean = {
    var s: Int = 0
    while (true) {
      s = status
      if (s.toShort != e)
        return false
      if (status.compareAndSwapStrong(s, (s & ~SMASK) | (tag & SMASK)))
        return true
    }
    // for the compiler
    false
  }

  private def writeObject(s: java.io.ObjectOutputStream): Unit = {
    s.defaultWriteObject()
    s.writeObject(getException)
  }

  private def readObject(s: java.io.ObjectInputStream): Unit = {
    s.defaultReadObject()
    val ex: Object = s.readObject()
    if (ex != null)
      setExceptionalCompletion(ex.asInstanceOf[Throwable])
  }

}

object ForkJoinTask {

  final val DONE_MASK: Int   = 0xf0000000 // mask out non-completien bits
  final val NORMAL: Int      = 0xf0000000 // must be negative
  final val CANCELLED: Int   = 0xc0000000; // must be < NORMAL
  final val EXCEPTIONAL: Int = 0x80000000; // must be < CANCELLED
  final val SIGNAL: Int      = 0x00010000; // must be >= 1 << 16
  final val SMASK: Int       = 0x0000ffff; // short bits for tags

  /**
   * Table of exceptions thrown by tasks, to enable reporting by
   * callers. Because exceptions are rare, we don't directly keep
   * them with task objects, but instead use a weak ref table.  Note
   * that cancellation exceptions don't appear in the table, but are
   * instead recorded as status values.
   *
   * Note: These statics are initialized below in static block.
   */
  private final val exceptionTable: Array[ExceptionNode]           = null
  private final val exceptionTableLock: ReentrantLock              = null
  private final val exceptionTableRefQueue: ReferenceQueue[Object] = null

  /**
   * Fixed capacity for exceptionTable.
   */
  private final val EXCEPTION_MAP_CAPACITY: Int = 32

  final class ExceptionNode(var task: ForkJoinTask[_],
                            var ex: Throwable,
                            var nextNode: ExceptionNode)
      extends WeakReference[ForkJoinTask[_]](task) {

    var thrower: Long = Thread.currentThread().getId

  }

  final def cancelIgnoringExceptions(t: ForkJoinTask[_]): Unit = {
    if (t != null && (t.status) >= 0) {
      try {
        t.cancel(false)
      } catch {
        case ignore: Throwable =>
      }
    }
  }

  private def expungeStaleExceptions(): Unit = {
    var x: Object = exceptionTableRefQueue.poll()
    while (x != null) {
      if (x.isInstanceOf[ExceptionNode]) {
        val key: ForkJoinTask[_]    = x.asInstanceOf[ExceptionNode].get()
        val t: Array[ExceptionNode] = exceptionTable
        val i: Int                  = System.identityHashCode(key) & (t.length - 1)
        var e: ExceptionNode        = t(i)
        var pred: ExceptionNode     = null
        var break: Boolean          = false
        while (e != null && !break) {
          val next: ExceptionNode = e.nextNode
          if (e == x) {
            if (pred == null)
              t(i) = next
            else
              pred.nextNode = next
            break = true
          }
          if (!break) {
            pred = e
            e = next
          }
        }
      }
      x = exceptionTableRefQueue.poll()
    }
  }

  final def helpExpungeStaleExceptions(): Unit = {
    val lock: ReentrantLock = exceptionTableLock
    if (lock.tryLock()) {
      try {
        expungeStaleExceptions()
      } finally {
        lock.unlock()
      }
    }
  }

  def rethrow(ex: Throwable): Unit = {
    if (ex != null) {
      if (ex.isInstanceOf[Error])
        throw ex.asInstanceOf[Error]
      if (ex.isInstanceOf[RuntimeException])
        throw ex.asInstanceOf[RuntimeException]
      ForkJoinTask.uncheckedThrow(ex)
    }
  }

  @SuppressWarnings(Array("unchecked"))
  def uncheckedThrow[T <: Throwable](t: Throwable): Unit = {
    if (t != null)
      throw t.asInstanceOf[T] // rely on vacuous cast
  }

  def invokeAll(t1: ForkJoinTask[_], t2: ForkJoinTask[_]): Unit = {
    t2.fork
    val s1: Int = t1.doInvoke()
    if ((s1 & DONE_MASK) != NORMAL)
      t1.reportException(s1)
    val s2: Int = t2.doJoin()
    if ((s2 & DONE_MASK) != NORMAL)
      t2.reportException(s2)
  }

  def invokeAll(tasks: ForkJoinTask[_]*): Unit = {
    var ex: Throwable = null
    val last: Int     = tasks.length - 1
    var i: Int        = last
    while (i >= 0) {
      val t: ForkJoinTask[_] = tasks(i)
      if (t == null) {
        if (ex == null)
          ex = new NullPointerException()
      } else if (i != 0)
        t.fork
      else if (t.doInvoke() < NORMAL && ex == null)
        ex = t.getException
      i -= 1
    }
    i = 1
    while (i <= last) {
      val t: ForkJoinTask[_] = tasks(i)
      if (t != null) {
        if (ex != null)
          t.cancel(false)
        else if (t.doJoin() < NORMAL)
          ex = t.getException
      }
      i += 1
    }
    if (ex != null)
      rethrow(ex)
  }

  // TODO invokeAll(collection)

  def helpQuiesce(): Unit = {
    val t: Thread = Thread.currentThread()
    if (t.isInstanceOf[ForkJoinWorkerThread]) {
      val wt: ForkJoinWorkerThread = t.asInstanceOf[ForkJoinWorkerThread]
      wt.pool.helpQuiescePool(wt.workQueue)
    } else
      ForkJoinPool.quiesceCommonPool
  }

  def getPool: ForkJoinPool = {
    val t: Thread = Thread.currentThread()
    if (t.isInstanceOf[ForkJoinWorkerThread])
      t.asInstanceOf[ForkJoinWorkerThread].pool
    else null
  }

  def inForkJoinPool: Boolean =
    Thread.currentThread().isInstanceOf[ForkJoinWorkerThread]

  def getQueuedTaskCount: Int = {
    val t: Thread                 = Thread.currentThread()
    var q: ForkJoinPool.WorkQueue = null
    if (t.isInstanceOf[ForkJoinWorkerThread])
      q = t.asInstanceOf[ForkJoinWorkerThread].workQueue
    else q = ForkJoinPool.commonSubmitterQueue
    if (q == null) 0 else q.queueSize
  }

  def getSurplusQueuedTaskCount: Int = ForkJoinPool.getSurplusQueuedTaskCount

  protected def peekNextLocalTask: ForkJoinTask[_] = {
    val t: Thread                 = Thread.currentThread()
    var q: ForkJoinPool.WorkQueue = null
    if (t.isInstanceOf[ForkJoinWorkerThread])
      q = t.asInstanceOf[ForkJoinWorkerThread].workQueue
    else
      q = ForkJoinPool.commonSubmitterQueue
    if (q == null) null else q.peek
  }

  protected def pollTask: ForkJoinTask[_] = {
    val t: Thread                = Thread.currentThread()
    var wt: ForkJoinWorkerThread = null
    if (t.isInstanceOf[ForkJoinWorkerThread]) {
      wt = t.asInstanceOf[ForkJoinWorkerThread]
      wt.pool.nextTaskFor(wt.workQueue)
    } else null
  }

  final class AdaptedRunnable[T](val runnable: Runnable, var result: T)
      extends ForkJoinTask[T]
      with RunnableFuture[T] {

    if (runnable == null)
      throw new NullPointerException()

    override def getRawResult: T = result

    override def setRawResult(value: T): Unit = result = value

    override def exec: Boolean = {
      runnable.run()
      true
    }

    override def run(): Unit = invoke

  }

  object AdaptedRunnable {

    private final val serialVersionUID: Long = 5232453952276885070L

  }

  final class AdaptedRunnableAction(val runnable: Runnable)
      extends ForkJoinTask[Void]
      with RunnableFuture[Void] {

    if (runnable == null)
      throw new NullPointerException()

    override def getRawResult: Void = null

    override def setRawResult(value: Void): Unit = {}

    override def exec: Boolean = {
      runnable.run()
      true
    }

    override def run(): Unit = invoke

  }

  object AdaptedRunnableAction {

    private final val serialVersionUID: Long = 5232453952276885070L

  }

  final class AdaptedCallable[T](val callable: Callable[_ <: T])
      extends ForkJoinTask[T]
      with RunnableFuture[T] {

    var result: T = null.asInstanceOf[T]

    if (callable == null)
      throw new NullPointerException()

    override def getRawResult: T = result

    override def setRawResult(value: T): Unit = result = value

    override def exec: Boolean = {
      try {
        result = callable.call()
        true
      } catch {
        case err: Error            => throw err
        case rex: RuntimeException => throw rex
        case ex: Exception         => throw new RuntimeException(ex)
      }
    }

    override def run(): Unit = invoke

  }

  object AdaptedCallable {

    private final val serialVersionUID: Long = 2838392045355241008L

  }

  def adapt(runnable: Runnable): ForkJoinTask[_] =
    new AdaptedRunnableAction(runnable)

  def adapt[T](runnable: Runnable, result: T): ForkJoinTask[T] =
    new AdaptedRunnable[T](runnable, result)

  def adapt[T](callable: Callable[_ <: T]): ForkJoinTask[T] =
    new AdaptedCallable[T](callable)

  private final val serialVersionUID: Long = -7721805057305804111L

}
