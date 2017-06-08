package java.util.concurrent

import java.util.{List, Collection}

trait ExecutorService extends Executor {
  def shutdown(): Unit
  def shutdownNow(): List[Runnable]
  def isShutdown(): Boolean
  def isTerminated(): Boolean
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean
  def submit[T](task: Callable[T]): Future[T]
  def submit[T](task: Runnable, result: T): Future[T]
  def submit(task: Runnable): Future[_]
  def invokeAll[T](tasks: Collection[Callable[T]]): List[Future[T]]
  def invokeAll[T](tasks: Collection[Callable[T]],
                   timeout: Long,
                   unit: TimeUnit): List[Future[T]]
  def invokeAny[T](tasks: Collection[Callable[T]]): T
  def invokeAny[T](tasks: Collection[Callable[T]],
                   timeout: Long,
                   unit: TimeUnit): T

}
