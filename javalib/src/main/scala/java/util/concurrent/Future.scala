package java.util.concurrent

// Ported from Harmony

trait Future[V] {

  def cancel(mayInterruptIfRunning: Boolean): Boolean

  def isCancelled: Boolean

  def isDone: Boolean

  def get: V

  def get(timeout: Long, unit: TimeUnit): V

}
