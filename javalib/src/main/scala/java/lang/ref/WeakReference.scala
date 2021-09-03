package java.lang.ref

// Reference queues are not supported
class WeakReference[T >: Null <: AnyRef](
    referent: T,
    queue: ReferenceQueue[_ >: T]
) extends Reference[T](referent) {
  //TODO register itself on creation

  def this(referent: T) = this(referent, null)
}
