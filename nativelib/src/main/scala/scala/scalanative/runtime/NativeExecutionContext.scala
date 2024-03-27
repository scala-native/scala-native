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

  private object QueueExecutionContext extends ExecutionContextExecutor {
    private val queue: ListBuffer[Runnable] = new ListBuffer
    override def execute(runnable: Runnable): Unit = queue += runnable
    override def reportFailure(t: Throwable): Unit = t.printStackTrace()

    def hasNextTask: Boolean = queue.nonEmpty

    def executeNextTask(): Unit = if (hasNextTask) {
      val runnable = queue.remove(0)
      try runnable.run()
      catch {
        case t: Throwable =>
          QueueExecutionContext.reportFailure(t)
      }
    }

    /** Execute all the available tasks. Returns the number of executed tasks */
    def executeAvailableTasks(): Unit = while (hasNextTask) {
      executeNextTask()
    }
  }

  /** Execute all the tasks in the queue until there is none left */
  private[runtime] def loop(): Unit =
    QueueExecutionContext.executeAvailableTasks()
}
