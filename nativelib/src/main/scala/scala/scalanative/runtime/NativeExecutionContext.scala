package scala.scalanative
package runtime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object NativeExecutionContext {

  /** Single-threaded queue based execution context. Each runable is executed
   *  sequentially after termination of the main method
   */
  val queue: ExecutionContextExecutor with WorkStealingExecutor =
    QueueExecutionContext

  trait WorkStealingExecutor { self: ExecutionContextExecutor =>

    /** Apply work-stealing mechanism to help with completion of any tasks
     *  available for execution.Returns after work-stealing maximal number or
     *  tasks or there is no more tasks available for execution
     *  @param maxSteals
     *    maximal ammount of tasks that can be executed, if <= 0 then no tasks
     *    would be completed
     */
    def stealWork(maxSteals: Int): Unit

    /** Apply work-stealing mechanism to help with completion of any tasks
     *  available for execution. Returns when timeout passed out or there is no
     *  more tasks available for execution
     *  @param timeout
     *    maximal ammount of time for which execution of new tasks can be
     *    started
     */
    def stealWork(timeout: FiniteDuration): Unit

    /** Apply work-stealing mechanism to help with completion of available tasks
     *  available for execution. Returns when there is no more tasks available
     *  for execution
     */
    def helpComplete(): Unit
  }

  private[runtime] object QueueExecutionContext
      extends ExecutionContextExecutor
      with WorkStealingExecutor {

    private val queue: Queue =
      if (isMultithreadingEnabled) new Queue.Concurrent()
      else new Queue.Singlethreaded()

    override def execute(runnable: Runnable): Unit = {
      queue.enqueue(runnable)
      if (isMultithreadingEnabled) {
        MainThreadShutdownContext.onTaskEnqueued()
      }
    }
    override def reportFailure(t: Throwable): Unit = t.printStackTrace()

    private[runtime] def hasAvailableTasks: Boolean = queue.nonEmpty
    private[runtime] def availableTasks: Int = queue.size

    private def doStealWork(): Unit = queue.dequeue match {
      case null => ()
      case runnable =>
        try runnable.run()
        catch { case t: Throwable => reportFailure(t) }
    }

    override def stealWork(maxSteals: Int): Unit = {
      if (maxSteals <= 0) ()
      else {
        var steals = 0
        while (steals < maxSteals && hasAvailableTasks) {
          doStealWork()
          steals += 1
        }
      }
    }

    override def stealWork(timeout: FiniteDuration): Unit = {
      val deadline = System.currentTimeMillis() + timeout.toMillis
      while (System.currentTimeMillis() < deadline && hasAvailableTasks) {
        doStealWork()
      }
    }

    override def helpComplete(): Unit = while (hasAvailableTasks) doStealWork()

    private trait Queue {
      def enqueue(runnable: Runnable): Unit
      def dequeue(): Runnable
      def size: Int
      def isEmpty: Boolean
    }
    private object Queue {
      class Concurrent() extends Queue {
        private val tasks =
          new java.util.concurrent.ConcurrentLinkedQueue[Runnable]()
        override def enqueue(runnable: Runnable): Unit = tasks.add(runnable)
        override def dequeue(): Runnable = tasks.poll()
        override def size: Int = tasks.size()
        override def isEmpty: Boolean = tasks.isEmpty()
      }
      class Singlethreaded() extends Queue {
        private var tasks = List.empty[Runnable]
        private var _size = 0
        override def enqueue(runnable: Runnable) = {
          tasks = runnable :: tasks
          _size += 1
        }
        override def dequeue(): Runnable = tasks match {
          case head :: tail => tasks = next; _size -= 1; head
          case Nil          => null // fine, concurrent version can return null
        }
        override def size: Int = _size
        override def isEmpty: Boolean = tasks.isEmpty
      }
    }
  }
}
