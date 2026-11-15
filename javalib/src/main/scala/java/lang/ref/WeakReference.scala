package java.lang.ref

// The garbage collector does not mark the referent of a weak reference, and
// clears it once nothing else keeps it alive. See Reference.
class WeakReference[T](referent: T, queue: ReferenceQueue[_ >: T])
    extends Reference[T](referent, queue) {

  def this(referent: T) = this(referent, null)
}
