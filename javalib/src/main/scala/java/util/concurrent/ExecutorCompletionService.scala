package java.util.concurrent

// Ported from Harmony

class ExecutorCompletionService[V](
    val executor: Executor,
    val completionQueue: BlockingQueue[Future[V]] =
      new LinkedBlockingQueue[Future[V]]())
    extends CompletionService[V] {

  if (executor == null || completionQueue == null)
    throw new NullPointerException()

  private final val aes: AbstractExecutorService = executor match {
    case exc: AbstractExecutorService => exc
    case _                            => null
  }

  private class QueueingFuture(val task: RunnableFuture[V])
      extends FutureTask[Void](task, null) {

    override protected def done(): Unit = completionQueue.add(task)

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
    executor.execute(new QueueingFuture(f))
    f
  }

  override def submit(task: Runnable, result: V): Future[V] = {
    if (task == null) throw new NullPointerException()
    val f: RunnableFuture[V] = newTaskFor(task, result)
    executor.execute(new QueueingFuture(f))
    f
  }

  override def take(): Future[V] = completionQueue.take

  override def poll(): Future[V] = completionQueue.poll()

  override def poll(timeout: Long, unit: TimeUnit): Future[V] =
    completionQueue.poll(timeout, unit)

}
