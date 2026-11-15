package java.lang.ref

// The garbage collector keeps the referent of a soft reference alive, except
// in the last collection before it would throw an OutOfMemoryError: there it
// clears it like the referent of a weak reference. See Reference.
class SoftReference[T](referent: T, queue: ReferenceQueue[_ >: T])
    extends Reference[T](referent, queue) {

  def this(referent: T) = this(referent, null)

  override private[ref] def isSoft: Boolean = true
}
