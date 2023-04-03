package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent._
import java.util.{List, Collection, LinkedList}

class DelegatingExecutorService(delegate: ExecutorService)
    extends ExecutorService {

  private def wrap[V](future: Future[V]): Future[V] = new Future[V] {
    override def cancel(mayInterruptIfRunning: Boolean): Boolean =
      future.cancel(mayInterruptIfRunning)
    override def isCancelled(): Boolean = future.isCancelled()
    override def isDone(): Boolean = future.isDone()
    override def get(): V = future.get()
    override def get(timeout: Long, unit: TimeUnit) =
      future.get(timeout, unit)
  }

  override def shutdown(): Unit = delegate.shutdown()
  override def shutdownNow(): List[Runnable] = delegate.shutdownNow()
  override def isShutdown(): Boolean = delegate.isShutdown()
  override def isTerminated(): Boolean = delegate.isTerminated()
  override def awaitTermination(timeout: Long, unit: TimeUnit) =
    delegate.awaitTermination(timeout, unit)
  override def submit[T](task: Callable[T]): Future[T] = wrap(
    delegate.submit(task)
  )
  override def submit[T](task: Runnable, result: T): Future[T] = wrap(
    delegate.submit(task, result)
  )
  override def submit(task: Runnable): Future[_] = wrap(
    delegate.submit(task): Future[_]
  )
  override def invokeAll[T](
      tasks: Collection[_ <: Callable[T]]
  ): List[Future[T]] = {
    val result = new LinkedList[Future[T]]()
    delegate.invokeAll(tasks).forEach { f => result.add(wrap(f)) }
    result
  }
  override def invokeAll[T](
      tasks: Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): List[Future[T]] = {
    val result = new LinkedList[Future[T]]()
    delegate.invokeAll(tasks, timeout, unit).forEach { f =>
      result.add(wrap(f))
    }
    result
  }

  override def invokeAny[T](tasks: Collection[_ <: Callable[T]]): T =
    delegate.invokeAny(tasks)
  override def invokeAny[T](
      tasks: Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): T = delegate.invokeAny(tasks, timeout, unit)
  override def execute(task: Runnable): Unit = delegate.execute(task)
}
