package java.lang.ref

// Cleared once the referent is unreachable, see Reference
class WeakReference[T](referent: T, queue: ReferenceQueue[_ >: T])
    extends Reference[T](referent, queue) {

  def this(referent: T) = this(referent, null)
}
