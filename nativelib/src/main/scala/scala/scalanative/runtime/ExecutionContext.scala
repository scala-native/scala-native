package scala.scalanative
package runtime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor

object ExecutionContext {
  def global: ExecutionContextExecutor = QueueExecutionContext

  private object QueueExecutionContext extends ExecutionContextExecutor {
    def execute(runnable: Runnable): Unit = queue += runnable
    def reportFailure(t: Throwable): Unit = t.printStackTrace()
  }

  private val queue: ListBuffer[Runnable] = new ListBuffer

  private[runtime] def loopRunOnce(): Boolean = {
    if (queue.nonEmpty) {
      val runnable = queue.remove(0)
      try {
        runnable.run()
      } catch {
        case t: Throwable =>
          QueueExecutionContext.reportFailure(t)
      }
    }
    queue.nonEmpty
  }

  private[runtime] def loop(): Unit = {
    while (loopRunOnce()) {
      // iterates until queue is empty
    }
  }
}
