package scala.scalanative
package runtime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

object ExecutionContext {
  @deprecated(
    "Use `singleThreaded` instead, since `global` may infer that it supports concurrent execution.",
    since = "0.5.0"
  )
  def global: ExecutionContextExecutor = singleThreaded
  def singleThreaded: ExecutionContextExecutor = QueueExecutionContext

  private object QueueExecutionContext extends ExecutionContextExecutor {
    private val queue: ListBuffer[Runnable] = new ListBuffer
    def execute(runnable: Runnable): Unit = queue += runnable
    def reportFailure(t: Throwable): Unit = t.printStackTrace()

    def loop(): Unit = {
      while (queue.nonEmpty) {
        val runnable = queue.remove(0)
        try runnable.run()
        catch {
          case t: Throwable =>
            QueueExecutionContext.reportFailure(t)
        }
      }
    }
  }

  private[runtime] def loop(): Unit = QueueExecutionContext.loop()
}
