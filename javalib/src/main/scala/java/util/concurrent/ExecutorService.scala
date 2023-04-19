/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util
package concurrent

trait ExecutorService extends Executor with AutoCloseable {

  def shutdown(): Unit

  def shutdownNow(): java.util.List[Runnable]

  def isShutdown(): Boolean

  def isTerminated(): Boolean

  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean

  def submit[T <: AnyRef](task: Callable[T]): Future[T]

  def submit[T <: AnyRef](task: Runnable, result: T): Future[T]

  def submit(task: Runnable): Future[_]

  def invokeAll[T <: AnyRef](
      tasks: java.util.Collection[_ <: Callable[T]]
  ): java.util.List[Future[T]]

  def invokeAll[T <: AnyRef](
      tasks: java.util.Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): java.util.List[Future[T]]

  def invokeAny[T <: AnyRef](tasks: java.util.Collection[_ <: Callable[T]]): T

  def invokeAny[T <: AnyRef](
      tasks: java.util.Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): T

  // Since JDK 19
  override def close(): Unit = {
    var terminated = isTerminated()
    if (!terminated) {
      shutdown()
      var interrupted = false
      while (!terminated) {
        try terminated = awaitTermination(1L, TimeUnit.DAYS)
        catch {
          case e: InterruptedException =>
            if (!interrupted) {
              shutdownNow()
              interrupted = true
            }
        }
      }
      if (interrupted) {
        Thread.currentThread().interrupt()
      }
    }
  }

}
