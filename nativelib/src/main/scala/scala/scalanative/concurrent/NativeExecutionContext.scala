package scala.scalanative
package concurrent

import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}
import scala.concurrent.duration.*

object NativeExecutionContext {

  /** Single-threaded computeQueue based execution context. Points to the same
   *  instance as `queue` but grants additional access to internal API.
   */
  private[scalanative] val queueInternal: InternalQueueExecutionContext =
    new QueueExecutionContextImpl()

  /** Single-threaded computeQueue based execution context. Each runable is
   *  executed sequentially after termination of the main method
   */
  val queue: QueueExecutionContext = queueInternal

  object Implicits {
    implicit final def queue: ExecutionContext = NativeExecutionContext.queue
  }

  trait QueueExecutionContext extends ExecutionContextExecutor {

    /** Check if there are no tasks queued for execution */
    def isEmpty: Boolean

    /** Check if there are any tasks queued for execution */
    final def nonEmpty: Boolean = !isEmpty
  }

  private[scalanative] trait InternalQueueExecutionContext
      extends QueueExecutionContext
      with WorkStealing
      with AutoCloseable {

    /** Disallow scheduling any new tasks to the ExecutionContext */
    def shutdown(): Unit

    /** Checks if the ExecutionContext shutdown was started */
    def inShutdown: Boolean

    /** Await for gracefull termination of this ExecutionContext, by waiting
     *  until the pending tasks are finished until timeout reaches out.
     *  @return
     *    false if failed to finish the pending tasks before the timeout, true
     *    otherwise
     */
    def awaitTermination(timeout: FiniteDuration): Boolean
  }

  private[scalanative] trait WorkStealing { self: QueueExecutionContext =>

    /** Apply work-stealing mechanism to help with completion of any tasks
     *  available for execution.Returns after work-stealing maximal number of
     *  tasks or there are no more tasks available for execution
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
}
