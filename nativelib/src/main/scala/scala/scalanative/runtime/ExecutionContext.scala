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

  @inline
  private def loopRunOnceUnsafe(): Unit = {
    val runnable = queue.remove(0)
    try {
      runnable.run()
    } catch {
      case t: Throwable =>
        QueueExecutionContext.reportFailure(t)
    }
  }

  private[runtime] def loop(): Unit = {
    while (queue.nonEmpty) {
      loopRunOnceUnsafe()
    }
  }

  private[runtime] def loopRunOnce(): Int = {
    if (queue.nonEmpty) {
      loopRunOnceUnsafe()
    }
    queue.size
  }
}
