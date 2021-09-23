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
      if (timeout == 0) {
        while (underlying.isEmpty) wait()
      } else {
        if (underlying.isEmpty) wait(timeout)
      }
      poll()
    }
  }
}
