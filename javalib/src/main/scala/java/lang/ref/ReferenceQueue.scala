package java.lang.ref

class ReferenceQueue[T] {
  private type Ref = Reference[_ <: T]
  private val underlying = new java.util.ArrayDeque[Ref](64)

  private[ref] def enqueue(reference: Ref): Unit =
    synchronized {
      underlying.addLast(reference)
      notifyAll()
    }

  private def dequeue(): Ref = {
    val entry = underlying.removeFirst()
    entry.markDequeued()
    entry
  }

  def poll(): Ref =
    synchronized {
      if (underlying.isEmpty()) null else dequeue()
    }

  def remove(): Ref = remove(None)
  def remove(timeout: Long): Ref = {
    if (timeout < 0) throw new IllegalArgumentException()
    remove(if (timeout == 0) None else Some(timeout))
  }

  private def remove(timeout: Option[Long]): Ref =
    synchronized {
      val deadlineOpt = timeout.map(System.nanoTime() + _ * 1000000L)

      while (underlying.isEmpty()) {
        deadlineOpt match {
          case Some(deadline) =>
            val remaining = deadline - System.nanoTime()
            if (remaining <= 0) return null // timed out
            wait((remaining + 999999L) / 1000000L)
          case None =>
            wait()
        }
      }
      dequeue()
    }
}
