package java.lang.ref

class ReferenceQueue[T] {
  import Reference._

  private type Ref = Reference[_ <: T]
  private val underlying = new java.util.ArrayDeque[Ref](64)

  // False if the reference was enqueued before
  private[ref] def enqueue(reference: Reference[_]): Boolean =
    synchronized {
      if (reference.state != Active) false
      else {
        reference.state = Enqueued
        underlying.addLast(reference.asInstanceOf[Ref])
        notifyAll()
        true
      }
    }

  private def dequeue(): Ref = {
    val entry = underlying.removeFirst()
    entry.state = Dequeued
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
      // Clamp so ms -> ns (here and in wait) cannot overflow; ~292 years.
      val deadlineOpt = timeout.map { ms =>
        System.nanoTime() + Math.min(ms, Long.MaxValue / 1000000L) * 1000000L
      }

      while (underlying.isEmpty()) {
        deadlineOpt match {
          case Some(deadline) =>
            val remaining = deadline - System.nanoTime()
            if (remaining <= 0) return null // timed out
            wait((remaining - 1L) / 1000000L + 1L)
          case None =>
            wait()
        }
      }
      dequeue()
    }
}
