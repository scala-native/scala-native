package java.lang.ref

import scalanative.annotation.stub
import scala.collection.mutable

class ReferenceQueue[T >: Null <: AnyRef] {
  private val underlying = mutable.Queue[Reference[_ <: T]]()
  private[ref] def enqueue(reference: Reference[_ <: T]): Unit =
    synchronized {
      underlying += reference
      notify()
    }

  def poll(): Reference[_ <: T] = {
    synchronized[Reference[_ <: T]] {
      underlying
        .dequeueFirst(_ => true)
        .map(_.dequeue())
        .orNull
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
