package scala.scalanative
package runtime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object NativeExecutionContext {

  /** Single-threaded queue based execution context. Each runable is executed
   *  sequentially after termination of the main method
   */
  val queue: ExecutionContextExecutor with VoluntaryTaskExecutor =
    QueueExecutionContext

  trait VoluntaryTaskExecutor { self: ExecutionContextExecutor =>

    /** Checks if there is any available task to be executed */
    def hasNextTask: Boolean

    /** Returns the number of tasks available for execution */
    def availableTasks: Int

    /** Execute next available task. No-op if there is no available tasks */
    def executeNextTask(): Unit

    /** Execute all the available tasks. Returns the number of executed tasks */
    final def executeAvailableTasks(): Int = {
      var counter = 0
      while (hasNextTask) {
        counter += 1
        executeNextTask()
      }
      counter
    }
  }

  private object QueueExecutionContext
      extends ExecutionContextExecutor
      with VoluntaryTaskExecutor {
    private val queue: ListBuffer[Runnable] = new ListBuffer
    override def execute(runnable: Runnable): Unit = queue += runnable
    override def reportFailure(t: Throwable): Unit = t.printStackTrace()

    override def hasNextTask: Boolean = queue.nonEmpty
    override def availableTasks: Int = queue.size

    override def executeNextTask(): Unit = if (hasNextTask) {
      val runnable = queue.remove(0)
      try runnable.run()
      catch { case t: Throwable => reportFailure(t) }
    }
  }
}
