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
    private val queue: ListBuffer[Runnable] = new ListBuffer
    override def execute(runnable: Runnable): Unit = queue += runnable
    override def reportFailure(t: Throwable): Unit = t.printStackTrace()

    private[runtime] def hasAvailableTasks: Boolean = queue.nonEmpty
    private[runtime] def availableTasks: Int = queue.size

    private def doStealWork(): Unit = {
      val runnable = queue.remove(0)
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
  }
}
