package java.lang.ref

import scala.collection.mutable

class ReferenceQueue[T] {
  private val underlying = mutable.Queue[Reference[T]]()
  private[ref] def enqueue(reference: Reference[T]): Unit =
    synchronized {
      underlying += reference
      notifyAll()
    }

  def poll(): Reference[T] = {
    synchronized[Reference[T]] {
      underlying
        .dequeueFirst(_ => true)
        .map(_.dequeue())
        .orNull
        .asInstanceOf[Reference[T]]
    }
  }

  def remove(): Reference[? <: T] = remove(None)
  def remove(timeout: Long): Reference[? <: T] = {
    if (timeout < 0) throw new IllegalArgumentException()
    remove(Some(timeout))
  }

  private def remove(timeout: Option[Long]): Reference[? <: T] =
    synchronized[Reference[? <: T]] {
      def now() = System.currentTimeMillis()
      val hasTimeout = timeout.isDefined
      val deadline = now() + timeout.getOrElse(0L)
      def timeoutExceeded(current: Long): Boolean =
        hasTimeout && current > deadline

      while (underlying.isEmpty && !timeoutExceeded(now())) {
        if (hasTimeout) wait((deadline - now()).min(0L))
        else wait()
      }
      poll()
    }
}
