package java.lang.ref

// Class strongly connected to GC.
// _gc_modified_ modifier is used by codegen
// to register which fields shall not be marked by GC.
// _gc_unmarked_ works like this only in the context of
// the WeakReference class.
class WeakReference[T](
    @volatile private var _gc_modified_referent: T,
    queue: ReferenceQueue[T]
) extends Reference[T](null.asInstanceOf[T]) {
  // Since compiler generates _gc_modified_referent and referent
  // (of the Reference class) as two separate fields and GC only
  // controls _gc_modified_ referent field, we pass null to the
  // superclass to avoid adding additional control to the GC.
  // This should not be a problem as all Reference class methods were
  // overriden.
  // If we were to pass referent to Reference class without changes
  // to the GC, WeakReference class would hold a strong reference
  // therefore not fulfilling its purpose

  def this(referent: T) = this(referent, null)

  @volatile private var enqueued = false
  if (_gc_modified_referent != null) WeakReferenceRegistry.add(this)

  // A next weak reference in the form linked-list used by WeakReferenceRegistry
  @volatile private[ref] var nextReference: WeakReference[?] = _
  // Callback registered for given WeakReference, called after WeakReference pointee would be garbage collected
  @volatile private[java] var postGCHandler: () => Unit = _

  override def get(): T = _gc_modified_referent

  override def enqueue(): Boolean =
    if (!enqueued && queue != null) {
      queue.enqueue(this)
      enqueued = true
      true
    } else false

  override def isEnqueued(): Boolean = enqueued

  override def clear(): Unit = {
    _gc_modified_referent = null.asInstanceOf[T]
    enqueue()
  }

  override private[ref] def dequeue(): Reference[T] = {
    enqueued = false
    this
  }

}
