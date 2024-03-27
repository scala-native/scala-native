package scala.scalanative
package runtime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object NativeExecutionContext {

  /** Single-threaded queue based execution context. Each runable is executed
   *  sequentially after termination of the main method
   */
  def queue: ExecutionContextExecutor = QueueExecutionContext

  @deprecated(
    "Use `queue` instead, since `global` may infer that it supports concurrent execution.",
    since = "0.5.0"
  )
  def global: ExecutionContextExecutor = queue

  object Implicits {
    implicit val queue: ExecutionContextExecutor = NativeExecutionContext.queue
  }

  private object QueueExecutionContext extends ExecutionContextExecutor {
    private val queue: ListBuffer[Runnable] = new ListBuffer
    def execute(runnable: Runnable): Unit = queue += runnable
    def reportFailure(t: Throwable): Unit = t.printStackTrace()

    def hasNext: Boolean = queue.nonEmpty
    def scheduled: Int = queue.size

    def runNext(): Unit = if (hasNext) {
      val runnable = queue.remove(0)
      try runnable.run()
      catch {
        case t: Throwable =>
          QueueExecutionContext.reportFailure(t)
      }
    }

    def loop(): Unit = while (hasNext) runNext()
  }

  // The proxy methods here are defined to allow integrating projects
  // to interact with our private execution context without leaking the abstraction

  /** Checks if there is any scheduled runnable which can be executed */
  def hasNext: Boolean = QueueExecutionContext.hasNext

  /** Exuecute next available runnable in the queue. No-op if the queue is empty
   */
  def runNext(): Unit = QueueExecutionContext.runNext()

  /** Returns the number of currently scheduled tasks */
  def scheduled: Int = QueueExecutionContext.scheduled

  /** Execute all the tasks in the queue until there is none left */
  def loop(): Unit = QueueExecutionContext.loop()
}
