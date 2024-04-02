package scala.scalanative.concurrent

import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}
import scala.concurrent.duration._
import scala.collection.mutable
import java.util.concurrent.{ConcurrentLinkedDeque, PriorityBlockingQueue}
import java.util.{AbstractQueue, ArrayDeque, Comparator, Deque, PriorityQueue}

import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.concurrent.NativeExecutionContext._
import scala.scalanative.runtime.MainThreadShutdownContext
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.RejectedExecutionException
import scala.concurrent.Promise
import java.util.concurrent.CancellationException
import scala.concurrent.Future
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.LockSupport

private[concurrent] class QueueExecutionContext()
    extends EventLoopExecutionContext {

  private val computeQueue: Deque[Runnable] =
    // todo: ConcurrentLinkedDeque not yet ported from JSR-166
    // if (isMultithreadingEnabled) new ConcurrentLinkedDeque
    new ArrayDeque

  private val scheduledQueue: Queue[DelayedTask] =
    if (isMultithreadingEnabled) new Queue.Concurrent(Some(implicitly))
    else new Queue.SingleThreaded(Some(implicitly))

  // todo: ConcurrentSkipListSet is not thread-safe yet
  private val eventHandlers =
    new ConcurrentSkipListSet[EventHandler[_, _]]()

  private def nowMillis(): Long = System.currentTimeMillis()

  // EventEventLoopExecutionContext
  private var isClosed = false
  override def inShutdown: Boolean = isClosed
  override def shutdown(): Unit = isClosed = true
  override def awaitTermination(timeout: FiniteDuration): Boolean = {
    stealWork(timeout)
    !isEmpty
  }

  override def isEmpty: Boolean =
    !isWorkStealingPossible && !hasScheduledTasks && !hasWaitingHandlers
  override def close(): Unit = shutdown()

  // ExecutionContextExecutor
  private def ensureNotClosed() = {
    if (inShutdown)
      throw new RejectedExecutionException(
        "ExecutionContext was closed, queuing new tasks in not allowed"
      )
  }
  override def execute(runnable: Runnable): Unit = {
    ensureNotClosed()
    computeQueue.offerLast(runnable)
    if (isMultithreadingEnabled) {
      MainThreadShutdownContext.onTaskEnqueued()
    }
  }

  override def reportFailure(t: Throwable): Unit = t.printStackTrace()

  //
  // Work stealing
  //
  private[scalanative] def availableTasks: Int = computeQueue.size

  override def isWorkStealingPossible: Boolean =
    !computeQueue.isEmpty() || untilNextScheduledTask.exists(_ <= Duration.Zero)

  override def stealWork(maxSteals: Int): Unit = if (maxSteals > 0) {
    var steals = 0
    while (isWorkStealingPossible && steals < maxSteals) {
      enqueueExpiredTimers(nowMillis())
      doStealWork()
      steals += 1
    }
  }

  override def stealWork(timeout: FiniteDuration): Unit =
    if (timeout > Duration.Zero) {
      var clock = nowMillis()
      val deadline = clock + timeout.toMillis
      while (isWorkStealingPossible && clock < deadline) {
        enqueueExpiredTimers(clock)
        doStealWork()
        clock = nowMillis()
      }
    }

  override def helpComplete(): Unit =
    while (!isEmpty) {
      stealWork(32)
      // TODO: Should we sleep to wait for scheduled/unsignaled handlers
    }

  private def doStealWork(): Unit = computeQueue.pollFirst() match {
    case null => ()
    case runnable =>
      try runnable.run()
      catch { case t: Throwable => reportFailure(t) }
  }

  //
  // Scheduling
  //
  override def hasScheduledTasks: Boolean = scheduledQueue.nonEmpty
  override def schedule(
      delay: FiniteDuration,
      task: Runnable
  ): ScheduledTask = {
    ensureNotClosed()
    if (delay <= Duration.Zero) {
      execute(task)
      DelayedTask.empty
    } else {
      val scheduled = DelayedTask(
        at = nowMillis() + delay.toMillis,
        task = task
      )
      scheduledQueue.enqueue(scheduled)
      scheduled
    }
  }
  override def untilNextScheduledTask: Option[FiniteDuration] =
    if (!hasScheduledTasks) None
    else
      scheduledQueue.peek() match {
        case DelayedTask(at, _) => Some((at - nowMillis()).millis)
        case null               => None
      }

  private def enqueueExpiredTimers(now: => Long): Unit =
    while (scheduledQueue.nonEmpty && scheduledQueue.peek().at <= now) {
      computeQueue.offerFirst(scheduledQueue.dequeue())
    }

  private[concurrent] case class DelayedTask(
      at: Long,
      task: Runnable
  ) extends ScheduledTask {
    override def cancel(): Unit = scheduledQueue.remove(this)
    override def run(): Unit = {
      cancel()
      execute(task)
    }
    override def compareTo(that: ScheduledTask): Int =
      that match {
        case that: DelayedTask =>
          DelayedTask.ordering.compare(this, that)
        case _ => -1
      }
  }

  object DelayedTask {
    implicit val ordering: Ordering[DelayedTask] = Ordering.by(_.at)
    val empty: ScheduledTask = new DelayedTask(-1, () => ()) {
      override def cancel(): Unit = ()
    }
  }

  // I/O
  override def hasWaitingHandlers: Boolean = !eventHandlers.isEmpty()
  override def createEventHandler[I, O](
      initialState: I,
      onEvent: EventHandler[I, O] => Either[I, O]
  ): EventHandler[I, O] = {
    ensureNotClosed()
    val handler = HandlerTask(initialState, onEvent)
    eventHandlers.add(handler)
    handler
  }

  private case class HandlerTask[State, Result](
      private var _state: State,
      _onEvent: EventHandler[State, Result] => Either[State, Result]
  ) extends EventHandler[State, Result]
      with Comparable[EventHandler[State, Result]] {
    var isSignaled: Boolean = false
    val promise: Promise[Result] = Promise[Result]()

    override def state: State = _state
    override def result: Future[Result] = promise.future
    override def onEvent(
        self: EventHandler[State, Result]
    ): Either[State, Result] = _onEvent(this)

    override def cancel(): Unit = {
      eventHandlers.remove(this)
      fail(new CancellationException("Task was cancelled"))
    }
    override def signal(): Unit = if (!isSignaled) {
      isSignaled = true
      eventHandlers.remove(this)
      computeQueue.offerFirst(this)
      if (isMultithreadingEnabled) {
        MainThreadShutdownContext.onTaskEnqueued()
      }
    }

    override def fail(error: Throwable): Unit = promise.tryFailure(error)

    override def run(): Unit = {
      isSignaled = false
      onEvent(this) match {
        case Right(result) =>
          promise.trySuccess(result)
        case Left(newState) =>
          _state = newState
          if (!promise.isCompleted) {
            ensureNotClosed()
            if (!isSignaled) {
              ensureNotClosed()
              eventHandlers.add(this)
            }
          }
      }
    }
    // Pseudo natural odering, required for ConcurrentSkipListSet
    override def compareTo(o: EventHandler[State, Result]): Int =
      this.##.compareTo(o.##)
  }

  private trait Queue[Task <: Runnable] {
    def enqueue(task: Task): Unit
    def dequeue(): Task
    def peek(): Task
    def remove(task: Task): Boolean
    def size: Int
    def isEmpty: Boolean
    final def nonEmpty: Boolean = !isEmpty
  }
  private object Queue {
    class Concurrent[Task <: Runnable](comparator: Option[Comparator[Task]])
        extends Queue[Task] {
      private val tasks: AbstractQueue[Task] = comparator match {
        case Some(comparator) => new PriorityBlockingQueue(32, comparator)
        case _                => new ConcurrentLinkedQueue()
      }
      override def enqueue(task: Task): Unit = tasks.offer(task)
      override def dequeue(): Task = tasks.poll()
      override def peek(): Task = tasks.peek()
      override def remove(task: Task): Boolean = tasks.remove(task)
      override def size: Int = tasks.size()
      override def isEmpty: Boolean = tasks.isEmpty()
    }
    class SingleThreaded[Task <: Runnable](
        comparator: Option[Comparator[Task]]
    ) extends Queue[Task] {
      private val tasks = new java.util.PriorityQueue(32, comparator.orNull)
      override def enqueue(task: Task) = tasks.add(task)
      override def dequeue(): Task =
        if (tasks.isEmpty) null.asInstanceOf[Task]
        else tasks.poll()
      override def peek(): Task = tasks.peek()
      override def remove(task: Task): Boolean = tasks.remove(task)
      override def size: Int = tasks.size
      override def isEmpty: Boolean = tasks.isEmpty
    }
  }
}
