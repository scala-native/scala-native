package java.lang.ref

// The referent of a phantom reference is never returned: get() is always
// null. The garbage collector clears it once nothing else keeps it alive, like
// the referent of a weak reference, and the reference is then enqueued. See
// Reference.
class PhantomReference[T](referent: T, queue: ReferenceQueue[_ >: T])
    extends Reference[T](referent, queue) {

  override def get(): T = null.asInstanceOf[T]
}
