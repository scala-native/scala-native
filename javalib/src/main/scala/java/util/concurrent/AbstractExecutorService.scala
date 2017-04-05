package java.util
package concurrent

trait AbstractExecutorService {
  def submit[T](task: Callable[T]): Future[T]
  def submit[T](task: Runnable, result: T): Future[T]
  def invokeAll[T](tasks: Collection[Callable[T]]): List[Future[T]]
  def invokeAll[T](tasks: Collection[Callable[T]], timeout: Long, unit: TimeUnit): List[Future[T]]
  def invokeAny[T](tasks: Collection[Callable[T]]): T
  def invokeAny[T](tasks: Collection[Callable[T]], timeout: Long, unit: TimeUnit): T
  def submit[T](task: Runnable): Future[T]
  protected def newTaskFor[T](callable: Callable[T]): RunnableFuture[T]
  protected def newTaskFor[T](runnable: Runnable, value: T): RunnableFuture[T]
}
