package java.lang.ref

import scala.collection.mutable

class ReferenceQueue[T] {
  private val underlying = mutable.Queue[Reference[T]]()
  private[ref] def enqueue(reference: Reference[T]): Unit =
    synchronized {
      underlying += reference
      notify()
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

  def remove(): Reference[_ <: T] =
    remove(0)

  def remove(timeout: Long): Reference[_ <: T] = {
    if (timeout < 0) throw new IllegalArgumentException()

    synchronized[Reference[_ <: T]] {
      def now() = System.currentTimeMillis()
      val deadline = now() + timeout
      def timeoutExceeded(current: Long): Boolean = {
        if (timeout == 0) false
        else current > deadline
      }

      while (underlying.isEmpty && !timeoutExceeded(now())) {
        val timeoutMillis = (deadline - now()).min(0L)
        wait(timeoutMillis)
      }
      poll()
    }
  }
}
