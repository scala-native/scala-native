package java.lang.ref

import scala.collection.mutable

class ReferenceQueue[T] {
  private type Ref = Reference[_ <: T]
  private val underlying = mutable.Queue[Ref]()

  private[ref] def enqueue(reference: Ref): Unit =
    synchronized {
      underlying += reference
      notifyAll()
    }

  private def dequeue(): Ref = {
    val entry = underlying.dequeue()
    entry.markDequeued()
    entry
  }

  def poll(): Ref =
    synchronized {
      if (underlying.isEmpty) null else dequeue()
    }

  def remove(): Ref = remove(None)
  def remove(timeout: Long): Ref = {
    if (timeout < 0) throw new IllegalArgumentException()
    remove(if (timeout == 0) None else Some(timeout))
  }

  private def remove(timeout: Option[Long]): Ref =
    synchronized {
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
