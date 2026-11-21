package java.lang.ref

// Cleared like a weak reference, its referent is never returned
class PhantomReference[T](referent: T, queue: ReferenceQueue[_ >: T])
    extends Reference[T](referent, queue) {

  override def get(): T = null.asInstanceOf[T]
}
