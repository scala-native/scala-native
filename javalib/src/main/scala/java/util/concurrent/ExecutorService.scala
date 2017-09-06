package java.util
package concurrent

// Ported from Harmony

import java.security.{PrivilegedAction, PrivilegedExceptionAction}

trait ExecutorService extends Executor {

  def shutdownNow: java.util.List[Runnable]

  def isShutdown: Boolean

  def isTerminated: Boolean

  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean

  def submit[T](task: Callable[T]): Future[T]

  def submit[T](task: Runnable, result: T): Future[T]

  def submit(task: Runnable): Future[_]

  def invokeAll[T](
      tasks: java.util.Collection[_ <: Callable[T]]): java.util.List[Future[T]]

  def invokeAll[T](tasks: java.util.Collection[_ <: Callable[T]],
                   timeout: Long,
                   unit: TimeUnit): java.util.List[Future[T]]

  def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]]): T

  def invokeAny[T](tasks: java.util.Collection[_ <: Callable[T]],
                   timeout: Long,
                   unit: TimeUnit): T

}
