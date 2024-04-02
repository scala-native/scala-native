package scala.scalanative
package concurrent

import scala.concurrent.{ExecutionContextExecutor, ExecutionContext}
import scala.concurrent.duration._
import javax.naming.spi.DirStateFactory.Result
import scala.concurrent.Promise
import scala.concurrent.Future

object NativeExecutionContext {

  /** Single-threaded computeQueue based execution context. Each runable is
   *  executed sequentially after termination of the main method
   */
  val queue: EventLoopExecutionContext = new QueueExecutionContext()

  object Implicits {
    implicit final def queue: ExecutionContext = NativeExecutionContext.queue
  }

  trait EventLoopExecutionContext
      extends ExecutionContextExecutor
      with WorkStealing
      with Scheduling
      with EventHandling
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

    /** Check if there are no pending tasks to be executed */
    def isEmpty: Boolean
  }

  trait WorkStealing { self: ExecutionContextExecutor =>

    /** Check if there are any tasks available for work stealing.
     *  @return
     *    true if there are tasks available, false otherwise
     */
    def isWorkStealingPossible: Boolean

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

  trait Cancellable {
    def cancel(): Unit
  }

  trait ScheduledTask
      extends Runnable
      with Comparable[ScheduledTask]
      with Cancellable

  trait Scheduling { self: ExecutionContextExecutor =>
    def hasScheduledTasks: Boolean
    def untilNextScheduledTask: Option[FiniteDuration]
    def schedule(delay: FiniteDuration, task: Runnable): ScheduledTask
  }

  // I/O
  trait EventHandler[State, Result] extends Runnable with Cancellable {

    /** Last value returned from `onEvent` method or initial state when not yet
     *  evaluated
     */
    def state: State

    /** Final result of computation */
    def result: Future[Result]

    /** Signal readines to evaluated to handler on next iteration. Can be called
     *  from within the handler logic to allow for restarting the handler on
     *  next iteration
     */
    def signal(): Unit

    /** Fail the execution and report failure using error */
    def fail(error: Throwable): Unit

    /** Callback called when evaluating the handler using its latest state. If
     *  the returned value is `Left[State]` then the handler would be
     *  rescheduled for next evaluation using returned value as new state,
     *  otherwise if returned value is `Right[Result]` then the handler would
     *  become completed.
     *  @return
     *    Left[State] containing new state for the next evaluation or
     *    Right[Result] containig the final result of computation
     */
    private[concurrent] def onEvent(
        handler: EventHandler[State, Result]
    ): Either[State, Result]
  }

  trait EventHandling { self: ExecutionContextExecutor =>

    /** Check if there are any handlers waiting for signaling */
    def hasWaitingHandlers: Boolean

    /** Creates a new event handler used to perform a statefull computation
     *  @param onEvent
     *    Callback called when evaluating the handler. If the returned value is
     *    `Left[State]` then the handler would be rescheduled for next
     *    evaluation using returned value as new state, otherwise if returned
     *    value is `Right[Result]` then the handler would become completed
     */
    def createEventHandler[I, O](
        initialState: I,
        onEvent: EventHandler[I, O] => Either[I, O]
    ): EventHandler[I, O]
  }

}
