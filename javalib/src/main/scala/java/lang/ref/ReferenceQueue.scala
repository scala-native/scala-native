package java.lang.ref

import scala.collection.mutable

class ReferenceQueue[T] {
  private val underlying = mutable.Queue[Reference[T]]()
  private[ref] def enqueue(reference: Reference[T]): Unit =
    synchronized {
      underlying += reference
      notifyAll()
    }

  private def dequeue(): Reference[T] = {
    val entry = underlying.dequeue()
    entry.markDequeued()
    entry
  }

  def poll(): Reference[T] =
    synchronized {
      if (underlying.isEmpty) null else dequeue()
    }

  def remove(): Reference[_ <: T] = remove(None)
  def remove(timeout: Long): Reference[_ <: T] = {
    if (timeout < 0) throw new IllegalArgumentException()
    remove(Some(timeout))
  }

  private def remove(timeout: Option[Long]): Reference[_ <: T] =
    synchronized[Reference[_ <: T]] {
      def now() = System.currentTimeMillis()
      val deadlineOpt = timeout.map(now() + _)

      while (underlying.isEmpty) {
        deadlineOpt match {
          case Some(deadline) =>
            val remaining = deadline - now()
            if (remaining < 0) return null // timed out
            wait(remaining)
          case None =>
            wait()
        }
      }
      dequeue()
    }
}
