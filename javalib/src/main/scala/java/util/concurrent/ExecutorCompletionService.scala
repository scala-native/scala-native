/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
import scala.scalanative.annotation.safePublish

class ExecutorCompletionService[V <: AnyRef](
    @safePublish val executor: Executor,
    @safePublish val completionQueue: BlockingQueue[Future[V]]
) extends CompletionService[V] {
  import ExecutorCompletionService._

  if (executor == null || completionQueue == null)
    throw new NullPointerException()

  def this(executor: Executor) = {
    this(executor, new LinkedBlockingQueue[Future[V]])
  }

  private final val aes: AbstractExecutorService = executor match {
    case exc: AbstractExecutorService => exc
    case _                            => null
  }

  private def newTaskFor(task: Callable[V]): RunnableFuture[V] = {
    if (aes == null) new FutureTask[V](task)
    else aes.newTaskFor(task)
  }

  private def newTaskFor(task: Runnable, result: V): RunnableFuture[V] = {
    if (aes == null) new FutureTask[V](task, result)
    else aes.newTaskFor(task, result)
  }

  override def submit(task: Callable[V]): Future[V] = {
    if (task == null) throw new NullPointerException()
    val f: RunnableFuture[V] = newTaskFor(task)
    executor.execute(new QueueingFuture(f, completionQueue))
    f
  }

  override def submit(task: Runnable, result: V): Future[V] = {
    if (task == null) throw new NullPointerException()
    val f: RunnableFuture[V] = newTaskFor(task, result)
    executor.execute(new QueueingFuture(f, completionQueue))
    f
  }

  override def take(): Future[V] = completionQueue.take()

  override def poll(): Future[V] = completionQueue.poll()

  override def poll(timeout: Long, unit: TimeUnit): Future[V] =
    completionQueue.poll(timeout, unit)

}

object ExecutorCompletionService {
  private class QueueingFuture[V <: AnyRef](
      @safePublish task: RunnableFuture[V],
      @safePublish completionQueue: BlockingQueue[Future[V]]
  ) extends FutureTask(task, null.asInstanceOf[V]) {
    override protected def done(): Unit = completionQueue.add(task)
  }
}
