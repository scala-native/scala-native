package java.util.concurrent

// Ported from Harmony

trait CompletionService[V] {

  def submit(task: Callable[V]): Future[V]

  def submit(task: Runnable, result: V): Future[V]

  def take: Future[V]

  def poll: Future[V]

  def poll(timeout: Long, unit: TimeUnit): Future[V]

}
