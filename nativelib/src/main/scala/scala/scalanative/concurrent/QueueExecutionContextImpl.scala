package scala.scalanative.concurrent

import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}
import scala.concurrent.duration._
import scala.collection.mutable

import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.concurrent.NativeExecutionContext._
import scala.scalanative.runtime.MainThreadShutdownContext

import java.util.{AbstractQueue, ArrayDeque, Comparator, Deque, PriorityQueue}
import java.util.concurrent.{ConcurrentLinkedQueue, RejectedExecutionException}

private[concurrent] class QueueExecutionContextImpl()
    extends InternalQueueExecutionContext {

  private val computeQueue: Queue =
    if (isMultithreadingEnabled) new Queue.Concurrent
    else new Queue.SingleThreaded

  private def nowMillis(): Long = System.currentTimeMillis()

  // QueueExecutionContext
  override def isEmpty: Boolean = computeQueue.isEmpty

  // EventEventLoopExecutionContext
  private var isClosed = false
  override def inShutdown: Boolean = isClosed
  override def shutdown(): Unit = isClosed = true
  override def awaitTermination(timeout: FiniteDuration): Boolean = {
    stealWork(timeout)
    nonEmpty
  }

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
    computeQueue.enqueue(runnable)
    if (isMultithreadingEnabled) {
      MainThreadShutdownContext.onTaskEnqueued()
    }
  }

  override def reportFailure(t: Throwable): Unit = t.printStackTrace()

  //
  // Work stealing
  //
  private[scalanative] def availableTasks: Int = computeQueue.size

  override def stealWork(maxSteals: Int): Unit = if (maxSteals > 0) {
    var steals = 0
    while (nonEmpty && steals < maxSteals) {
      doStealWork()
      steals += 1
    }
  }

  override def stealWork(timeout: FiniteDuration): Unit =
    if (timeout > Duration.Zero) {
      var clock = nowMillis()
      val deadline = clock + timeout.toMillis + 1
      while (nonEmpty && clock <= deadline) {
        doStealWork()
        clock = nowMillis()
      }
    }

  override def helpComplete(): Unit =
    while (nonEmpty) stealWork(Int.MaxValue)

  private def doStealWork(): Unit = computeQueue.dequeue() match {
    case null => ()
    case runnable =>
      try runnable.run()
      catch { case t: Throwable => reportFailure(t) }
  }

  private trait Queue {
    def enqueue(task: Runnable): Unit
    def dequeue(): Runnable
    def size: Int
    def isEmpty: Boolean
    final def nonEmpty: Boolean = !isEmpty
  }
  private object Queue {
    class Concurrent extends Queue {
      private val tasks = new ConcurrentLinkedQueue[Runnable]()
      override def enqueue(task: Runnable): Unit = tasks.offer(task)
      override def dequeue(): Runnable = tasks.poll()
      override def size: Int = tasks.size()
      override def isEmpty: Boolean = tasks.isEmpty()
    }
    class SingleThreaded() extends Queue {
      private val tasks = mutable.ListBuffer.empty[Runnable]
      override def enqueue(runnable: Runnable) = tasks += runnable
      override def dequeue(): Runnable =
        if (tasks.nonEmpty) tasks.remove(0)
        else null
      override def size: Int = tasks.size
      override def isEmpty: Boolean = tasks.isEmpty
    }
  }
}
