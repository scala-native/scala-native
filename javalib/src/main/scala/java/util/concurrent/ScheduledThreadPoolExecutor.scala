/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.TimeUnit.*
import java.util
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.*
import scala.annotation.tailrec
import scala.scalanative.annotation.safePublish

object ScheduledThreadPoolExecutor {

  @safePublish
  private val sequencer = new AtomicLong

  private val DEFAULT_KEEPALIVE_MILLIS = 10L

  private[concurrent] object DelayedWorkQueue {
    private val INITIAL_CAPACITY = 16

    private def setIndex(f: RunnableScheduledFuture[AnyRef], idx: Int): Unit =
      f match {
        case f: ScheduledThreadPoolExecutor#ScheduledFutureTask[?] =>
          f.heapIndex = idx
        case _ => ()
      }
  }
  private[concurrent] class DelayedWorkQueue
      extends util.AbstractQueue[Runnable]
      with BlockingQueue[Runnable] {
    private var queue =
      new Array[RunnableScheduledFuture[AnyRef]](
        DelayedWorkQueue.INITIAL_CAPACITY
      )
    private final val lock = new ReentrantLock
    private var _size = 0

    /** Thread designated to wait for the task at the head of the queue. This
     *  variant of the Leader-Follower pattern
     *  (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to minimize
     *  unnecessary timed waiting. When a thread becomes the leader, it waits
     *  only for the next delay to elapse, but other threads await indefinitely.
     *  The leader thread must signal some other thread before returning from
     *  take() or poll(...), unless some other thread becomes leader in the
     *  interim. Whenever the head of the queue is replaced with a task with an
     *  earlier expiration time, the leader field is invalidated by being reset
     *  to null, and some waiting thread, but not necessarily the current
     *  leader, is signalled. So waiting threads must be prepared to acquire and
     *  lose leadership while waiting.
     */
    private var leader: Thread = null

    private final val available = lock.newCondition()

    private def siftUp(_k: Int, key: RunnableScheduledFuture[AnyRef]): Unit = {
      var k = _k
      var break = false
      while (!break && k > 0) {
        val parent = (k - 1) >>> 1
        val e = queue(parent)
        if (key.compareTo(e) >= 0) break = true
        else {
          queue(k) = e
          DelayedWorkQueue.setIndex(e, k)
          k = parent
        }
      }
      queue(k) = key
      DelayedWorkQueue.setIndex(key, k)
    }

    private def siftDown(
        _k: Int,
        key: RunnableScheduledFuture[AnyRef]
    ): Unit = {
      var k = _k
      val half = _size >>> 1
      var break = false
      while (!break && k < half) {
        var child = (k << 1) + 1
        var c = queue(child)
        val right = child + 1
        if (right < _size && c.compareTo(queue(right)) > 0) {
          child = right
          c = queue(child)
        }
        if (key.compareTo(c) <= 0) break = true
        else {
          queue(k) = c
          DelayedWorkQueue.setIndex(c, k)
          k = child
        }
      }
      queue(k) = key
      DelayedWorkQueue.setIndex(key, k)
    }

    private def grow(): Unit = {
      val oldCapacity = queue.length
      var newCapacity = oldCapacity + (oldCapacity >> 1) // grow 50%
      if (newCapacity < 0) { // overflow
        newCapacity = Integer.MAX_VALUE
      }
      queue = util.Arrays.copyOf(queue, newCapacity)
    }

    private def indexOf(x: Any): Int = x match {
      case null                                                  => -1
      case t: ScheduledThreadPoolExecutor#ScheduledFutureTask[?] =>
        val i = t.heapIndex
        // Sanity check; x could conceivably be a
        // ScheduledFutureTask from some other pool.
        if (i >= 0 && i < _size && (queue(i) == x)) i
        else -1
      case _ =>
        var i = 0
        while (i < _size) {
          if (x == queue(i)) return i
          i += 1
        }
        -1
    }

    override def contains(x: Any): Boolean = {
      val lock = this.lock
      lock.lock()
      try indexOf(x) != -1
      finally lock.unlock()
    }

    override def remove(x: Any): Boolean = {
      val lock = this.lock
      lock.lock()
      try {
        val i = indexOf(x)
        if (i < 0) return false
        DelayedWorkQueue.setIndex(queue(i), -1)
        _size -= 1
        val s = _size
        val replacement = queue(s)
        queue(s) = null
        if (s != i) {
          siftDown(i, replacement)
          if (queue(i) eq replacement) siftUp(i, replacement)
        }
        true
      } finally lock.unlock()
    }

    override def size(): Int = {
      val lock = this.lock
      lock.lock()
      try this._size
      finally lock.unlock()
    }

    override def isEmpty(): Boolean = _size == 0
    override def remainingCapacity(): Int = Integer.MAX_VALUE
    override def peek(): RunnableScheduledFuture[AnyRef] = {
      val lock = this.lock
      lock.lock()
      try queue(0)
      finally lock.unlock()
    }

    override def offer(x: Runnable): Boolean = {
      if (x == null) throw new NullPointerException
      val e = x.asInstanceOf[RunnableScheduledFuture[AnyRef]]
      val lock = this.lock
      lock.lock()
      try {
        val i = _size
        if (i >= queue.length) grow()
        _size = i + 1
        if (i == 0) {
          queue(0) = e
          DelayedWorkQueue.setIndex(e, 0)
        } else siftUp(i, e)
        if (queue(0) eq e) {
          leader = null
          available.signal()
        }
      } finally lock.unlock()
      true
    }

    override def put(e: Runnable): Unit = { offer(e) }
    override def add(e: Runnable): Boolean = offer(e)
    override def offer(e: Runnable, timeout: Long, unit: TimeUnit): Boolean =
      offer(e)

    private def finishPoll(f: RunnableScheduledFuture[AnyRef]) = {
      val s = { _size -= 1; _size }
      val x = queue(s)
      queue(s) = null
      if (s != 0) siftDown(0, x)
      DelayedWorkQueue.setIndex(f, -1)
      f
    }

    override def poll(): RunnableScheduledFuture[AnyRef] = {
      val lock = this.lock
      lock.lock()
      try {
        val first = queue(0)
        if (first == null || first.getDelay(NANOSECONDS) > 0) null
        else finishPoll(first)
      } finally lock.unlock()
    }

    @throws[InterruptedException]
    override def take(): RunnableScheduledFuture[AnyRef] = {
      @tailrec def loop(): RunnableScheduledFuture[AnyRef] = {
        var first = queue(0)
        if (first == null) {
          available.await()
          loop()
        } else {
          val delay = first.getDelay(NANOSECONDS)
          if (delay <= 0L) finishPoll(first)
          else {
            first = null // don't retain ref while waiting
            if (leader != null) available.await()
            else {
              val thisThread = Thread.currentThread()
              leader = thisThread
              try available.awaitNanos(delay)
              finally if (leader eq thisThread) leader = null
            }
            loop()
          }
        }
      }

      val lock = this.lock
      lock.lockInterruptibly()
      try loop()
      finally {
        if (leader == null && queue(0) != null) available.signal()
        lock.unlock()
      }
    }

    @throws[InterruptedException]
    override def poll(
        timeout: Long,
        unit: TimeUnit
    ): RunnableScheduledFuture[AnyRef] = {
      @tailrec def loop(nanos: Long): RunnableScheduledFuture[AnyRef] = {
        var first = queue(0)
        if (first == null)
          if (nanos <= 0L) null
          else loop(available.awaitNanos(nanos))
        else {
          val delay = first.getDelay(NANOSECONDS)
          if (delay <= 0L) finishPoll(first)
          else if (nanos <= 0L) null
          else {
            first = null
            if (nanos < delay || leader != null)
              loop(available.awaitNanos(nanos))
            else {
              val thisThread = Thread.currentThread()
              leader = thisThread
              loop(try {
                val timeLeft = available.awaitNanos(delay)
                nanos - (delay - timeLeft)
              } finally if (leader eq thisThread) leader = null)
            }
          }
        }
      }

      val lock = this.lock
      lock.lockInterruptibly()
      try loop(unit.toNanos(timeout))
      finally {
        if (leader == null && queue(0) != null) available.signal()
        lock.unlock()
      }
    }

    override def clear(): Unit = {
      val lock = this.lock
      lock.lock()
      try {
        for (i <- 0 until _size) {
          val t = queue(i)
          if (t != null) {
            queue(i) = null
            DelayedWorkQueue.setIndex(t, -1)
          }
        }
        _size = 0
      } finally lock.unlock()
    }

    override def drainTo(c: util.Collection[? >: Runnable]): Int =
      drainTo(c, Integer.MAX_VALUE)

    override def drainTo(
        c: util.Collection[? >: Runnable],
        maxElements: Int
    ): Int = {
      Objects.requireNonNull(c)
      if (c eq this) throw new IllegalArgumentException
      if (maxElements <= 0) return 0
      val lock = this.lock
      lock.lock()
      try {
        var n = 0
        var first: RunnableScheduledFuture[AnyRef] = null
        while ({
          n < maxElements && { first = queue(0); first != null } &&
          first.getDelay(NANOSECONDS) <= 0
        }) {
          c.add(first) // In this order, in case add() throws.

          finishPoll(first)
          n += 1
        }
        n
      } finally lock.unlock()
    }

    override def toArray(): Array[AnyRef] = {
      val lock = this.lock
      lock.lock()
      try util.Arrays.copyOf(queue, _size, classOf[Array[AnyRef]])
      finally lock.unlock()
    }

    @SuppressWarnings(Array("unchecked"))
    override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
      val lock = this.lock
      lock.lock()
      try {
        if (a.length < _size)
          return util.Arrays
            .copyOf(queue, _size)
            .asInstanceOf[Array[T]]
        System.arraycopy(queue, 0, a, 0, _size)
        if (a.length > _size) a(_size) = null.asInstanceOf[T]
        a
      } finally lock.unlock()
    }
    override def iterator(): util.Iterator[Runnable] = {
      val lock = this.lock
      lock.lock()
      try
        new Itr(util.Arrays.copyOf(queue, _size))
      finally lock.unlock()
    }

    private[concurrent] class Itr private[concurrent] (
        val array: Array[RunnableScheduledFuture[AnyRef]]
    ) extends util.Iterator[Runnable] {

      // index of next element to return; initially 0
      private[concurrent] var cursor = 0

      // index of last element returned; -1 if no such
      private[concurrent] var lastRet = -1

      override def hasNext(): Boolean = cursor < array.length

      override def next(): Runnable = {
        if (cursor >= array.length) throw new NoSuchElementException
        lastRet = cursor
        cursor += 1
        array(lastRet)
      }

      override def remove(): Unit = {
        if (lastRet < 0) throw new IllegalStateException
        DelayedWorkQueue.this.remove(array(lastRet))
        lastRet = -1
      }
    }
  }
}

class ScheduledThreadPoolExecutor(
    corePoolSize: Int,
    threadFactory: ThreadFactory,
    handler: RejectedExecutionHandler
) extends ThreadPoolExecutor(
      corePoolSize,
      Integer.MAX_VALUE,
      ScheduledThreadPoolExecutor.DEFAULT_KEEPALIVE_MILLIS,
      MILLISECONDS,
      new ScheduledThreadPoolExecutor.DelayedWorkQueue,
      threadFactory,
      handler
    )
    with ScheduledExecutorService {

  @volatile
  private var continueExistingPeriodicTasksAfterShutdown = false

  @volatile
  private var executeExistingDelayedTasksAfterShutdown = true

  @volatile
  private[concurrent] var removeOnCancel = false

  private sealed trait ScheduledFutureTask[V <: AnyRef]
      extends RunnableScheduledFuture[V] { self: FutureTask[V] =>

    @volatile
    protected var time: Long

    protected var period: Long

    protected def sequenceNumber: Long

    private[concurrent] var outerTask: RunnableScheduledFuture[V] = this

    private[concurrent] var heapIndex: Int = 0

    override def getDelay(unit: TimeUnit): Long =
      unit.convert(time - System.nanoTime(), NANOSECONDS)
    override def compareTo(other: Delayed): Int = {
      if (other eq this) { // compare zero if same object
        return 0
      }
      if (other
            .isInstanceOf[ScheduledFutureTask[?]]) {
        val x =
          other.asInstanceOf[ScheduledFutureTask[?]]
        val diff = time - x.time
        if (diff < 0) return -1
        else if (diff > 0) return 1
        else if (sequenceNumber < x.sequenceNumber) return -1
        else return 1
      }
      val diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS)
      if (diff < 0) -1
      else if (diff > 0) 1
      else 0
    }

    override def isPeriodic(): Boolean = period != 0

    protected def setNextRunTime(): Unit = {
      val p = period
      if (p > 0) time += p
      else time = triggerTime(-p)
    }

  }

  private class ScheduledFutureRunableTask[V <: AnyRef](
      runnable: Runnable,
      result: V,
      protected var time: Long,
      protected var period: Long,
      protected val sequenceNumber: Long
  ) extends FutureTask(runnable, result)
      with ScheduledFutureTask[V] {
    def this(
        runnable: Runnable,
        result: V,
        time: Long,
        sequenceNumber: Long
    ) = this(
      runnable,
      result,
      time = time,
      period = 0,
      sequenceNumber = sequenceNumber
    )

    override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
      // The racy read of heapIndex below is benign:
      // if heapIndex < 0, then OOTA guarantees that we have surely
      // been removed; else we recheck under lock in remove()
      val cancelled = super.cancel(mayInterruptIfRunning)
      if (cancelled && removeOnCancel && heapIndex >= 0) remove(this)
      cancelled
    }

    override def run(): Unit = {
      if (!canRunInCurrentRunState(this)) cancel(false)
      else if (!isPeriodic()) super.run()
      else if (runAndReset()) {
        setNextRunTime()
        reExecutePeriodic(outerTask)
      }
    }
  }

  private class ScheduledFutureCallableTask[V <: AnyRef](
      callable: Callable[V],
      protected var time: Long,
      protected val sequenceNumber: Long
  ) extends FutureTask(callable)
      with ScheduledFutureTask[V] {
    protected var period: Long = 0

    override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
      // The racy read of heapIndex below is benign:
      // if heapIndex < 0, then OOTA guarantees that we have surely
      // been removed; else we recheck under lock in remove()
      val cancelled = super.cancel(mayInterruptIfRunning)
      if (cancelled && removeOnCancel && heapIndex >= 0) remove(this)
      cancelled
    }

    override def run(): Unit = {
      if (!canRunInCurrentRunState(this)) cancel(false)
      else if (!isPeriodic()) super.run()
      else if (runAndReset()) {
        setNextRunTime()
        reExecutePeriodic(outerTask)
      }
    }
  }

  private[concurrent] def canRunInCurrentRunState(
      task: RunnableScheduledFuture[?]
  ): Boolean = {
    if (!isShutdown()) return true
    if (isStopped()) return false
    if (task.isPeriodic()) continueExistingPeriodicTasksAfterShutdown
    else
      executeExistingDelayedTasksAfterShutdown ||
        task.getDelay(NANOSECONDS) <= 0
  }

  private def delayedExecute(task: RunnableScheduledFuture[?]): Unit = {
    if (isShutdown()) reject(task)
    else {
      super.getQueue().add(task)
      if (!canRunInCurrentRunState(task) && remove(task)) task.cancel(false)
      else ensurePrestart()
    }
  }

  private[concurrent] def reExecutePeriodic(
      task: RunnableScheduledFuture[?]
  ): Unit = {
    if (canRunInCurrentRunState(task)) {
      super.getQueue().add(task)
      if (canRunInCurrentRunState(task) || !remove(task)) {
        ensurePrestart()
        return
      }
    }
    task.cancel(false)
  }

  override private[concurrent] def onShutdown(): Unit = {
    val q = super.getQueue()
    val keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy()
    val keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy()
    // Traverse snapshot to avoid iterator exceptions
    // TODO: implement and use efficient removeIf
    // super.getQueue().removeIf(...);
    for (e <- q.toArray()) {
      e match {
        case t: RunnableScheduledFuture[?] =>
          def check =
            if (t.isPeriodic()) !keepPeriodic
            else !keepDelayed && t.getDelay(NANOSECONDS) > 0
          if (check || t.isCancelled()) { // also remove if already cancelled
            if (q.remove(t)) t.cancel(false)
          }

        case _ => ()
      }
    }
    tryTerminate()
  }

  protected def decorateTask[V](
      runnable: Runnable,
      task: RunnableScheduledFuture[V]
  ): RunnableScheduledFuture[V] = task

  protected def decorateTask[V](
      callable: Callable[V],
      task: RunnableScheduledFuture[V]
  ): RunnableScheduledFuture[V] = task

  def this(corePoolSize: Int, threadFactory: ThreadFactory) =
    this(corePoolSize, threadFactory, new ThreadPoolExecutor.AbortPolicy())

  def this(corePoolSize: Int) = this(
    corePoolSize,
    Executors.defaultThreadFactory()
  )

  def this(corePoolSize: Int, handler: RejectedExecutionHandler) =
    this(corePoolSize, Executors.defaultThreadFactory(), handler)

  private def triggerTime(delay: Long, unit: TimeUnit): Long = triggerTime(
    unit.toNanos(
      if (delay < 0) 0
      else delay
    )
  )
  private[concurrent] def triggerTime(delay: Long): Long =
    System.nanoTime() + (if (delay < (java.lang.Long.MAX_VALUE >> 1)) delay
                         else overflowFree(delay))

  private def overflowFree(delay: Long): Long = {
    val head = super.getQueue().peek().asInstanceOf[Delayed]
    if (head != null) {
      val headDelay = head.getDelay(NANOSECONDS)
      if (headDelay < 0 && (delay - headDelay < 0))
        return java.lang.Long.MAX_VALUE + headDelay
    }
    delay
  }

  override def schedule(
      command: Runnable,
      delay: Long,
      unit: TimeUnit
  ): ScheduledFuture[AnyRef] = {
    if (command == null || unit == null) throw new NullPointerException
    val t = decorateTask(
      command,
      new ScheduledFutureRunableTask(
        command,
        null: AnyRef,
        triggerTime(delay, unit),
        ScheduledThreadPoolExecutor.sequencer.getAndIncrement()
      )
    )
    delayedExecute(t)
    t
  }

  override def schedule[V <: AnyRef](
      callable: Callable[V],
      delay: Long,
      unit: TimeUnit
  ): ScheduledFuture[V] = {
    if (callable == null || unit == null) throw new NullPointerException
    val t = decorateTask(
      callable,
      new ScheduledFutureCallableTask[V](
        callable,
        triggerTime(delay, unit),
        ScheduledThreadPoolExecutor.sequencer.getAndIncrement()
      )
    )
    delayedExecute(t)
    t
  }

  override def scheduleAtFixedRate(
      command: Runnable,
      initialDelay: Long,
      period: Long,
      unit: TimeUnit
  ): ScheduledFuture[AnyRef] = {
    if (command == null || unit == null) throw new NullPointerException
    if (period <= 0L) throw new IllegalArgumentException
    val sft = new ScheduledFutureRunableTask(
      command,
      null: AnyRef,
      triggerTime(initialDelay, unit),
      unit.toNanos(period),
      ScheduledThreadPoolExecutor.sequencer.getAndIncrement()
    )
    val t = decorateTask(command, sft)
    sft.outerTask = t
    delayedExecute(t)
    t
  }

  override def scheduleWithFixedDelay(
      command: Runnable,
      initialDelay: Long,
      delay: Long,
      unit: TimeUnit
  ): ScheduledFuture[AnyRef] = {
    if (command == null || unit == null) throw new NullPointerException
    if (delay <= 0L) throw new IllegalArgumentException
    val sft = new ScheduledFutureRunableTask(
      command,
      null: AnyRef,
      triggerTime(initialDelay, unit),
      -unit.toNanos(delay),
      ScheduledThreadPoolExecutor.sequencer.getAndIncrement()
    )
    val t = decorateTask(command, sft)
    sft.outerTask = t
    delayedExecute(t)
    t
  }

  override def execute(command: Runnable): Unit = {
    schedule(command, 0, NANOSECONDS)
  }

  override def submit(task: Runnable): Future[?] =
    schedule(task, 0, NANOSECONDS)

  override def submit[T <: AnyRef](task: Runnable, result: T): Future[T] =
    schedule(Executors.callable(task, result), 0L, NANOSECONDS)

  override def submit[T <: AnyRef](task: Callable[T]): Future[T] =
    schedule(task, 0, NANOSECONDS)

  def setContinueExistingPeriodicTasksAfterShutdownPolicy(
      value: Boolean
  ): Unit = {
    continueExistingPeriodicTasksAfterShutdown = value
    if (!value && isShutdown()) onShutdown()
  }

  def getContinueExistingPeriodicTasksAfterShutdownPolicy(): Boolean =
    continueExistingPeriodicTasksAfterShutdown

  def setExecuteExistingDelayedTasksAfterShutdownPolicy(
      value: Boolean
  ): Unit = {
    executeExistingDelayedTasksAfterShutdown = value
    if (!value && isShutdown()) onShutdown()
  }

  def getExecuteExistingDelayedTasksAfterShutdownPolicy(): Boolean =
    executeExistingDelayedTasksAfterShutdown

  def setRemoveOnCancelPolicy(value: Boolean): Unit = removeOnCancel = value

  def getRemoveOnCancelPolicy(): Boolean = removeOnCancel

  override def shutdown(): Unit = super.shutdown()

  override def shutdownNow(): List[Runnable] = super.shutdownNow()

  override def getQueue(): BlockingQueue[Runnable] = super.getQueue()
}
