/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.util
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks._
import scala.annotation.tailrec

object ThreadPoolExecutor {
  private val COUNT_BITS: Int = Integer.SIZE - 3
  private val COUNT_MASK: Int = (1 << COUNT_BITS) - 1
// runState is stored in the high-order bits
  private val RUNNING: Int = -(1) << COUNT_BITS
  private val SHUTDOWN: Int = 0 << COUNT_BITS
  private val STOP: Int = 1 << COUNT_BITS
  private val TIDYING: Int = 2 << COUNT_BITS
  private val TERMINATED: Int = 3 << COUNT_BITS
// Packing and unpacking ctl
  private def workerCountOf(c: Int): Int = c & COUNT_MASK
  private def ctlOf(rs: Int, wc: Int): Int = rs | wc
  private def runStateLessThan(c: Int, s: Int): Boolean = c < s
  private def runStateAtLeast(c: Int, s: Int): Boolean = c >= s
  private def isRunning(c: Int): Boolean = c < SHUTDOWN

  private[concurrent] val defaultHandler: RejectedExecutionHandler =
    new AbortPolicy

  private val ONLY_ONE: Boolean = true

  class CallerRunsPolicy() extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = {
      if (!e.isShutdown()) r.run()
    }
  }

  class AbortPolicy() extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = {
      throw new RejectedExecutionException(
        "Task " + r.toString + " rejected from " + e.toString
      )
    }
  }

  class DiscardPolicy() extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = {}
  }

  class DiscardOldestPolicy() extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = {
      if (!e.isShutdown()) {
        e.getQueue().poll()
        e.execute(r)
      }
    }
  }
}

class ThreadPoolExecutor(
    /** Core pool size is the minimum number of workers to keep alive (and not
     *  allow to time out etc) unless allowCoreThreadTimeOut is set, in which
     *  case the minimum is zero.
     *
     *  Since the worker count is actually stored in COUNT_BITS bits, the
     *  effective limit is {@code corePoolSize & COUNT_MASK}.
     */
    @volatile private var corePoolSize: Int,
    /** Maximum pool size.
     *
     *  Since the worker count is actually stored in COUNT_BITS bits, the
     *  effective limit is {@code maximumPoolSize & COUNT_MASK}.
     */
    @volatile private var maximumPoolSize: Int,
    @volatile private var keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue[Runnable],
    @volatile private var threadFactory: ThreadFactory,
    @volatile private var handler: RejectedExecutionHandler
) extends AbstractExecutorService {
  import ThreadPoolExecutor._

  if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0)
    throw new IllegalArgumentException
  if (workQueue == null || threadFactory == null || handler == null)
    throw new NullPointerException
  this.keepAliveTime = unit.toNanos(keepAliveTime)

  /** The main pool control state, ctl, is an atomic integer packing two
   *  conceptual fields workerCount, indicating the effective number of threads
   *  runState, indicating whether running, shutting down etc
   *
   *  In order to pack them into one int, we limit workerCount to (2^29)-1
   *  (about 500 million) threads rather than (2^31)-1 (2 billion) otherwise
   *  representable. If this is ever an issue in the future, the variable can be
   *  changed to be an AtomicLong, and the shift/mask constants below adjusted.
   *  But until the need arises, this code is a bit faster and simpler using an
   *  int.
   *
   *  The workerCount is the number of workers that have been permitted to start
   *  and not permitted to stop. The value may be transiently different from the
   *  actual number of live threads, for example when a ThreadFactory fails to
   *  create a thread when asked, and when exiting threads are still performing
   *  bookkeeping before terminating. The user-visible pool size is reported as
   *  the current size of the workers set.
   *
   *  The runState provides the main lifecycle control, taking on values:
   *
   *  RUNNING: Accept new tasks and process queued tasks SHUTDOWN: Don't accept
   *  new tasks, but process queued tasks STOP: Don't accept new tasks, don't
   *  process queued tasks, and interrupt in-progress tasks TIDYING: All tasks
   *  have terminated, workerCount is zero, the thread transitioning to state
   *  TIDYING will run the terminated() hook method TERMINATED: terminated() has
   *  completed
   *
   *  The numerical order among these values matters, to allow ordered
   *  comparisons. The runState monotonically increases over time, but need not
   *  hit each state. The transitions are:
   *
   *  RUNNING -> SHUTDOWN On invocation of shutdown() (RUNNING or SHUTDOWN) ->
   *  STOP On invocation of shutdownNow() SHUTDOWN -> TIDYING When both queue
   *  and pool are empty STOP -> TIDYING When pool is empty TIDYING ->
   *  TERMINATED When the terminated() hook method has completed
   *
   *  Threads waiting in awaitTermination() will return when the state reaches
   *  TERMINATED.
   *
   *  Detecting the transition from SHUTDOWN to TIDYING is less straightforward
   *  than you'd like because the queue may become empty after non-empty and
   *  vice versa during SHUTDOWN state, but we can only terminate if, after
   *  seeing that it is empty, we see that workerCount is 0 (which sometimes
   *  entails a recheck -- see below).
   */
  final private val ctl: AtomicInteger = new AtomicInteger(ctlOf(RUNNING, 0))

  private def compareAndIncrementWorkerCount(expect: Int): Boolean =
    ctl.compareAndSet(expect, expect + 1)

  private def compareAndDecrementWorkerCount(expect: Int): Boolean =
    ctl.compareAndSet(expect, expect - 1)

  private def decrementWorkerCount(): Unit = ctl.addAndGet(-(1))

  final private val mainLock: ReentrantLock = new ReentrantLock

  final private val workers: util.HashSet[Worker] = new util.HashSet[Worker]

  final private val termination: Condition = mainLock.newCondition()

  private var largestPoolSize: Int = 0

  private var completedTaskCount: Long = 0L

  @volatile
  private var allowCoreThreadTimeOut: Boolean = false

  @SerialVersionUID(6138294804551838833L)
  final private[concurrent] class Worker private[concurrent] (
      var firstTask: Runnable
  ) extends AbstractQueuedSynchronizer
      with Runnable {
    setState(-1) // inhibit interrupts until runWorker

    final private[concurrent] var thread: Thread =
      getThreadFactory().newThread(this)

    @volatile private[concurrent] var completedTasks: Long = 0L

    override def run(): Unit = runWorker(this)
    override protected def isHeldExclusively(): Boolean = getState() != 0
    override protected def tryAcquire(unused: Int): Boolean = {
      if (compareAndSetState(0, 1)) {
        setExclusiveOwnerThread(Thread.currentThread())
        true
      } else false
    }
    override protected def tryRelease(unused: Int): Boolean = {
      setExclusiveOwnerThread(null)
      setState(0)
      true
    }

    def lock(): Unit = acquire(1)
    def tryLock(): Boolean = tryAcquire(1)
    def unlock(): Unit = release(1)
    def isLocked(): Boolean = isHeldExclusively()

    private[concurrent] def interruptIfStarted(): Unit = {
      var t: Thread = null
      if (getState() >= 0 && { t = thread; t != null } && !t.isInterrupted())
        try t.interrupt()
        catch {
          case ignore: SecurityException =>

        }
    }
  }

  @tailrec private def advanceRunState(targetState: Int): Unit = {
    // assert targetState == SHUTDOWN || targetState == STOP;
    val c: Int = ctl.get()
    def setNewState =
      ctl.compareAndSet(
        c,
        ctlOf(
          targetState,
          workerCountOf(c)
        )
      )
    if (runStateAtLeast(c, targetState) || setNewState) ()
    else advanceRunState(targetState)
  }

  final private[concurrent] def tryTerminate(): Unit = {
    while (true) {
      val c: Int = ctl.get()
      if (isRunning(c) ||
          runStateAtLeast(c, TIDYING) || {
            runStateLessThan(c, STOP) && !(workQueue.isEmpty())
          }) return ()
      if (workerCountOf(c) != 0) { // Eligible to terminate
        interruptIdleWorkers(ONLY_ONE)
        return ()
      }
      val mainLock: ReentrantLock = this.mainLock
      mainLock.lock()
      try
        if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
          try terminated()
          finally {
            ctl.set(ctlOf(TERMINATED, 0))
            termination.signalAll()
          }
          return
        }
      finally mainLock.unlock()
      // else retry on failed CAS
    }
  }

  private def interruptWorkers(): Unit = workers.forEach(_.interruptIfStarted())

  private def interruptIdleWorkers(onlyOne: Boolean): Unit = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    def interruptWorker(w: Worker) = {
      val t: Thread = w.thread
      if (!t.isInterrupted() && w.tryLock()) {
        try t.interrupt()
        catch { case ignore: SecurityException => () }
        finally w.unlock()
      }
    }
    val it = workers.iterator()
    try {
      if (onlyOne) {
        if (it.hasNext()) interruptWorker(it.next())
      } else it.forEachRemaining(interruptWorker(_))
    } finally mainLock.unlock()
  }

  private def interruptIdleWorkers(): Unit = interruptIdleWorkers(false)

  final private[concurrent] def reject(command: Runnable): Unit =
    handler.rejectedExecution(command, this)

  private[concurrent] def onShutdown(): Unit = {}

  private def drainQueue(): util.List[Runnable] = {
    val q: BlockingQueue[Runnable] = workQueue
    val taskList: util.ArrayList[Runnable] = new util.ArrayList[Runnable]
    q.drainTo(taskList)
    if (!(q.isEmpty())) for (r <- q.toArray(new Array[Runnable](0))) {
      if (q.remove(r)) taskList.add(r)
    }
    return taskList
  }

  private def addWorker(firstTask: Runnable, core: Boolean): Boolean = {
    // retry
    var c: Int = ctl.get()
    var break = false
    while (!break) {
      // Check if queue empty only if necessary.
      if (runStateAtLeast(c, SHUTDOWN) && {
            runStateAtLeast(c, STOP) ||
            firstTask != null ||
            workQueue.isEmpty()
          }) return false

      var retry = true
      while (retry && !break) {
        val maxSize = if (core) corePoolSize else maximumPoolSize
        if (workerCountOf(c) >= (maxSize & COUNT_MASK)) return false
        if (compareAndIncrementWorkerCount(c)) break = true
        else {
          c = ctl.get() // Re-read ctl
          if (runStateAtLeast(c, SHUTDOWN)) retry = false
          // else CAS failed due to workerCount change; retry inner loop
        }
      }
    }

    var workerStarted = false
    var workerAdded = false
    lazy val w = new Worker(firstTask)
    try {
      val t = w.thread
      if (t != null) {
        val mainLock = this.mainLock
        mainLock.lock()
        // Recheck while holding lock.
        // Back out on ThreadFactory failure or if
        // shut down before lock acquired.
        try {
          val c = ctl.get()
          if (isRunning(c) ||
              (runStateLessThan(c, STOP) && firstTask == null)) {
            if (t.getState() != Thread.State.NEW)
              throw new IllegalThreadStateException()
            workers.add(w)
            workerAdded = true
            val s = workers.size()
            if (s > largestPoolSize) largestPoolSize = s
          }
        } finally mainLock.unlock();
        if (workerAdded) {
          t.start()
          workerStarted = true
        }
      }
    } finally
      if (!workerStarted)
        addWorkerFailed(w)
    workerStarted
  }

  private def addWorkerFailed(w: Worker): Unit = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      if (w != null) workers.remove(w)
      decrementWorkerCount()
      tryTerminate()
    } finally mainLock.unlock()
  }

  private def processWorkerExit(
      w: ThreadPoolExecutor#Worker,
      completedAbruptly: Boolean
  ): Unit = {
    if (completedAbruptly) { // If abrupt, then workerCount wasn't adjusted
      decrementWorkerCount()
    }
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      completedTaskCount += w.completedTasks
      workers.remove(w)
    } finally mainLock.unlock()
    tryTerminate()
    val c: Int = ctl.get()
    if (runStateLessThan(c, STOP)) {
      if (!(completedAbruptly)) {
        var min: Int =
          if (allowCoreThreadTimeOut) 0
          else corePoolSize
        if (min == 0 && !(workQueue.isEmpty())) min = 1
        if (workerCountOf(c) >= min)
          return // replacement not needed
      }
      addWorker(null, false)
    }
  }

  private def getTask(): Runnable = {
    var timedOut: Boolean = false // Did the last poll() time out?
    while (true) {
      val c: Int = ctl.get()
      if (runStateAtLeast(c, SHUTDOWN) &&
          (runStateAtLeast(c, STOP) || workQueue.isEmpty())) {
        decrementWorkerCount()
        return null
      }
      val wc: Int = workerCountOf(c)
      // Are workers subject to culling?
      val timed: Boolean = allowCoreThreadTimeOut || wc > corePoolSize
      val shouldSkip =
        if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue
              .isEmpty())) {
          if (compareAndDecrementWorkerCount(c)) return null
          true // continue
        } else false
      if (!shouldSkip) {
        try {
          val r: Runnable =
            if (timed) workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS)
            else workQueue.take()
          if (r != null) return r
          timedOut = true
        } catch { case retry: InterruptedException => timedOut = false }
      }
    }
    null // unreachable
  }

  final private[concurrent] def runWorker(
      w: ThreadPoolExecutor#Worker
  ): Unit = {
    val wt: Thread = Thread.currentThread()
    var task: Runnable = w.firstTask
    w.firstTask = null
    w.unlock() // allow interrupts

    var completedAbruptly: Boolean = true
    try {
      while (task != null || { task = getTask(); task != null }) {
        w.lock()
        // If pool is stopping, ensure thread is interrupted;
        // if not, ensure thread is not interrupted.  This
        // requires a recheck in second case to deal with
        // shutdownNow race while clearing interrupt
        if ({
          runStateAtLeast(ctl.get(), STOP) ||
          (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))
        } && !(wt.isInterrupted())) wt.interrupt()
        try {
          beforeExecute(wt, task)
          try {
            task.run()
            afterExecute(task, null)
          } catch {
            case ex: Throwable =>
              afterExecute(task, ex)
              throw ex
          }
        } finally {
          task = null
          w.completedTasks += 1
          w.unlock()
        }
      }
      completedAbruptly = false
    } finally processWorkerExit(w, completedAbruptly)
  }

  def this(
      corePoolSize: Int,
      maximumPoolSize: Int,
      keepAliveTime: Long,
      unit: TimeUnit,
      workQueue: BlockingQueue[Runnable]
  ) = {
    this(
      corePoolSize,
      maximumPoolSize,
      keepAliveTime,
      unit,
      workQueue,
      Executors.defaultThreadFactory(),
      ThreadPoolExecutor.defaultHandler
    )
  }

  def this(
      corePoolSize: Int,
      maximumPoolSize: Int,
      keepAliveTime: Long,
      unit: TimeUnit,
      workQueue: BlockingQueue[Runnable],
      threadFactory: ThreadFactory
  ) = {
    this(
      corePoolSize,
      maximumPoolSize,
      keepAliveTime,
      unit,
      workQueue,
      threadFactory,
      ThreadPoolExecutor.defaultHandler
    )
  }

  def this(
      corePoolSize: Int,
      maximumPoolSize: Int,
      keepAliveTime: Long,
      unit: TimeUnit,
      workQueue: BlockingQueue[Runnable],
      handler: RejectedExecutionHandler
  ) = {
    this(
      corePoolSize,
      maximumPoolSize,
      keepAliveTime,
      unit,
      workQueue,
      Executors.defaultThreadFactory(),
      handler
    )
  }

  override def execute(command: Runnable): Unit = {
    if (command == null) { throw new NullPointerException }
    /*
     * Proceed in 3 steps:
     *
     * 1. If fewer than corePoolSize threads are running, try to
     * start a new thread with the given command as its first
     * task.  The call to addWorker atomically checks runState and
     * workerCount, and so prevents false alarms that would add
     * threads when it shouldn't, by returning false.
     *
     * 2. If a task can be successfully queued, then we still need
     * to double-check whether we should have added a thread
     * (because existing ones died since last checking) or that
     * the pool shut down since entry into this method. So we
     * recheck state and if necessary roll back the enqueuing if
     * stopped, or start a new thread if there are none.
     *
     * 3. If we cannot queue task, then we try to add a new
     * thread.  If it fails, we know we are shut down or saturated
     * and so reject the task.
     */
    var c: Int = ctl.get()
    if (workerCountOf(c) < corePoolSize) {
      if (addWorker(command, true)) { return }
      c = ctl.get()
    }
    if (isRunning(c) && workQueue.offer(command)) {
      val recheck: Int = ctl.get()
      if (!(isRunning(recheck)) && remove(command)) {
        reject(command)
      } else {
        if (workerCountOf(recheck) == 0) {
          addWorker(null, false)
        }
      }
    } else { if (!(addWorker(command, false))) { reject(command) } }
  }

  override def shutdown(): Unit = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      //  checkShutdownAccess()
      advanceRunState(SHUTDOWN)
      interruptIdleWorkers()
      onShutdown() // hook for ScheduledThreadPoolExecutor
    } finally {
      mainLock.unlock()
    }
    tryTerminate()
  }

  override def shutdownNow(): util.List[Runnable] = {
    var tasks: util.List[Runnable] = null
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      //  checkShutdownAccess()
      advanceRunState(STOP)
      interruptWorkers()
      tasks = drainQueue()
    } finally {
      mainLock.unlock()
    }
    tryTerminate()
    return tasks
  }
  override def isShutdown(): Boolean = {
    return runStateAtLeast(
      ctl.get(),
      SHUTDOWN
    )
  }

  private[concurrent] def isStopped(): Boolean = {
    return runStateAtLeast(ctl.get(), STOP)
  }

  def isTerminating(): Boolean = {
    val c: Int = ctl.get()
    return runStateAtLeast(
      c,
      SHUTDOWN
    ) && runStateLessThan(c, TERMINATED)
  }
  override def isTerminated(): Boolean = {
    return runStateAtLeast(
      ctl.get(),
      TERMINATED
    )
  }
  @throws[InterruptedException]
  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = {
    var nanos: Long = unit.toNanos(timeout)
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      while ({
        runStateLessThan(
          ctl.get(),
          TERMINATED
        )
      }) {
        if (nanos <= 0L) { return false }
        nanos = termination.awaitNanos(nanos)
      }
      return true
    } finally {
      mainLock.unlock()
    }
  }

  def setThreadFactory(threadFactory: ThreadFactory): Unit = {
    if (threadFactory == null) { throw new NullPointerException }
    this.threadFactory = threadFactory
  }

  def getThreadFactory(): ThreadFactory = threadFactory

  def setRejectedExecutionHandler(handler: RejectedExecutionHandler): Unit = {
    if (handler == null) throw new NullPointerException
    this.handler = handler
  }

  def getRejectedExecutionHandler(): RejectedExecutionHandler = handler

  def setCorePoolSize(corePoolSize: Int): Unit = {
    if (corePoolSize < 0 || maximumPoolSize < corePoolSize) {
      throw new IllegalArgumentException
    }
    val delta: Int = corePoolSize - this.corePoolSize
    this.corePoolSize = corePoolSize
    if (ThreadPoolExecutor.workerCountOf(ctl.get()) > corePoolSize)
      interruptIdleWorkers()
    else {
      if (delta > 0) {
        // We don't really know how many new threads are "needed".
        // As a heuristic, prestart enough new workers (up to new
        // core size) to handle the current number of tasks in
        // queue, but stop if queue becomes empty while doing so.
        var k: Int = delta min workQueue.size()
        while ({
          k -= 1
          k > 0 && addWorker(null, true) && !workQueue.isEmpty()
        }) ()
      }
    }
  }

  def getCorePoolSize(): Int = { return corePoolSize }

  def prestartCoreThread(): Boolean = {
    return workerCountOf(
      ctl.get()
    ) < corePoolSize && addWorker(null, true)
  }

  private[concurrent] def ensurePrestart(): Unit = {
    val wc: Int = workerCountOf(ctl.get())
    if (wc < corePoolSize) { addWorker(null, true) }
    else { if (wc == 0) { addWorker(null, false) } }
  }

  def prestartAllCoreThreads(): Int = {
    var n: Int = 0
    while ({ addWorker(null, true) }) { n += 1 }
    return n
  }

  def allowsCoreThreadTimeOut(): Boolean = { return allowCoreThreadTimeOut }

  def allowCoreThreadTimeOut(value: Boolean): Unit = {
    if (value && keepAliveTime <= 0) {
      throw new IllegalArgumentException(
        "Core threads must have nonzero keep alive times"
      )
    }
    if (value != allowCoreThreadTimeOut) {
      allowCoreThreadTimeOut = value
      if (value) { interruptIdleWorkers() }
    }
  }

  def setMaximumPoolSize(maximumPoolSize: Int): Unit = {
    if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) {
      throw new IllegalArgumentException
    }
    this.maximumPoolSize = maximumPoolSize
    if (workerCountOf(ctl.get()) > maximumPoolSize) {
      interruptIdleWorkers()
    }
  }

  def getMaximumPoolSize(): Int = { return maximumPoolSize }

  def setKeepAliveTime(time: Long, unit: TimeUnit): Unit = {
    if (time < 0) { throw new IllegalArgumentException }
    if (time == 0 && allowsCoreThreadTimeOut()) {
      throw new IllegalArgumentException(
        "Core threads must have nonzero keep alive times"
      )
    }
    val keepAliveTime: Long = unit.toNanos(time)
    val delta: Long = keepAliveTime - this.keepAliveTime
    this.keepAliveTime = keepAliveTime
    if (delta < 0) interruptIdleWorkers()
  }

  def getKeepAliveTime(unit: TimeUnit): Long =
    unit.convert(keepAliveTime, TimeUnit.NANOSECONDS)

  def getQueue(): BlockingQueue[Runnable] = { return workQueue }

  def remove(task: Runnable): Boolean = {
    val removed: Boolean = workQueue.remove(task)
    tryTerminate() // In case SHUTDOWN and now empty

    removed
  }

  def purge(): Unit = {
    val q: BlockingQueue[Runnable] = workQueue
    try {
      val it: util.Iterator[Runnable] = q.iterator()
      while (it.hasNext()) {
        it.next() match {
          case r: Future[_] if r.isCancelled() => it.remove()
          case _                               => ()
        }
      }
    } catch {
      case fallThrough: ConcurrentModificationException =>
        // Take slow path if we encounter interference during traversal.
        // Make copy for traversal and call remove for cancelled entries.
        // The slow path is more likely to be O(N*N).
        for (r <- q.toArray()) {
          r match {
            case r: Future[_] if r.isCancelled() => q.remove(r)
            case _                               => ()
          }
        }
    }
    // In case SHUTDOWN and now empty
    tryTerminate()
  }

  def getPoolSize(): Int = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    // Remove rare and surprising possibility of isTerminated() && getPoolSize() > 0
    try
      if (runStateAtLeast(ctl.get(), TIDYING)) 0
      else workers.size()
    finally mainLock.unlock()
  }

  def getActiveCount(): Int = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      var n: Int = 0
      workers.forEach { w =>
        if (w.isLocked()) n += 1
      }
      n
    } finally mainLock.unlock()
  }

  def getLargestPoolSize(): Int = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try return largestPoolSize
    finally {
      mainLock.unlock()
    }
  }

  def getTaskCount(): Long = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      var n: Long = completedTaskCount
      workers.forEach { w =>
        n += w.completedTasks
        if (w.isLocked()) n += 1

      }
      n + workQueue.size()
    } finally mainLock.unlock()
  }

  def getCompletedTaskCount(): Long = {
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      var n: Long = completedTaskCount
      workers.forEach(n += _.completedTasks)
      n
    } finally mainLock.unlock()
  }

  override def toString(): String = {
    var ncompleted: Long = 0L
    var nworkers: Int = 0
    var nactive: Int = 0
    val mainLock: ReentrantLock = this.mainLock
    mainLock.lock()
    try {
      ncompleted = completedTaskCount
      nactive = 0
      nworkers = workers.size()
      workers.forEach { w =>
        ncompleted += w.completedTasks
        if (w.isLocked()) { nactive += 1 }
      }
    } finally mainLock.unlock()
    val c: Int = ctl.get()
    val runState: String =
      if (isRunning(c)) "Running"
      else if (runStateAtLeast(c, TERMINATED)) "Terminated"
      else "Shutting down"

    return super
      .toString() + "[" + runState + ", pool size = " + nworkers + ", active threads = " + nactive + ", queued tasks = " + workQueue
      .size() + ", completed tasks = " + ncompleted + "]"
  }

  protected def beforeExecute(t: Thread, r: Runnable): Unit = {}

  protected def afterExecute(r: Runnable, t: Throwable): Unit = {}

  protected def terminated(): Unit = {}
}
