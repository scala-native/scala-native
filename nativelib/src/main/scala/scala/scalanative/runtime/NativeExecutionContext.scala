package scala.scalanative
package runtime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object NativeExecutionContext {

  /** Single-threaded queue based execution context. Each runable is executed
   *  sequentially after termination of the main method
   */
  val queue: ExecutionContextExecutor = QueueExecutionContext

  private[runtime] object QueueExecutionContext
      extends ExecutionContextExecutor {
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

    def hasNextTask: Boolean = !queue.isEmpty
    def availableTasks: Int = queue.size

    def executeNextTask(): Unit = if (hasNextTask) {
      queue.dequeue() match {
        case null => ()
        case runnable =>
          try runnable.run()
          catch { case t: Throwable => QueueExecutionContext.reportFailure(t) }
      }
    }

    /** Execute all the available tasks. Returns the number of executed tasks */
    def executeAvailableTasks(): Unit = while (hasNextTask) {
      executeNextTask()
    }

    private trait Queue {
      def enqueue(runnable: Runnable): Unit
      def dequeue(): Runnable
      def size: Int
      def isEmpty: Boolean
    }
    private object Queue {
      class Concurrent() extends Queue {
        val backend = new java.util.concurrent.ConcurrentLinkedQueue[Runnable]()
        override def enqueue(runnable: Runnable): Unit = backend.add(runnable)
        override def dequeue(): Runnable = backend.poll()
        override def size: Int = backend.size()
        override def isEmpty: Boolean = backend.isEmpty()
      }
      class Singlethreaded() extends Queue {
        val backend = ListBuffer.empty[Runnable]
        override def enqueue(runnable: Runnable) = backend += runnable
        override def dequeue(): Runnable = backend.remove(0)
        override def size: Int = backend.size
        override def isEmpty: Boolean = backend.isEmpty
      }
    }
  }
}
