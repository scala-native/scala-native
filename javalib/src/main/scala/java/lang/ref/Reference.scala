package java.lang.ref

abstract class Reference[T >: Null <: AnyRef] {

  /* volatile */
  private[this] var referent: T = _

  var queue: ReferenceQueue[_ >: T] = _

  var next: Reference[_ <: AnyRef] = _

  def this(referent: T) = {
    this()
    this.referent = referent
  }

  def this(referent: T, q: ReferenceQueue[_ >: T]) = {
    this()
    this.queue = q
    this.referent = referent
  }

  def get(): T = referent

  def clear(): Unit = referent = null

  def isEnqueued: Boolean = next != null

  def enqueue(): Boolean = {
    if (next == null && queue != null) {
      queue.enqueue(this)
      queue = null
      return true
    }
    false
  }
}