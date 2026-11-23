package java.lang.ref

// Cleared only before an OutOfMemoryError, see Reference
class SoftReference[T](referent: T, queue: ReferenceQueue[_ >: T])
    extends Reference[T](referent, queue) {

  def this(referent: T) = this(referent, null)
}
