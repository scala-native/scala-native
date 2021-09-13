package java.lang.ref

// Class strongly connected to GC.
// _gc_modified_ modifier is used by codegen
// to register which fields shall not be marked by GC.
// _gc_unmarked_ works like this only in the context of
// the WeakReference class.
class WeakReference[T >: Null <: AnyRef](
    var _gc_modified_referent: T,
    queue: ReferenceQueue[_ >: T]
) extends Reference[T](null) {
  private var enqueued = false

  def this(referent: T) = this(referent, null)

  WeakReferenceRegistry.add(this)

  override def get(): T = _gc_modified_referent

  override def enqueue(): Boolean =
    enqueued match {
      case true => false
      case false if (queue == null) => false
      case _ =>
        queue.add(this)
        enqueued = true
        true
    }

  override def isEnqueued(): Boolean = enqueued

  override def clear(): Unit = {
    super.clear()
    _gc_modified_referent = null
    enqueue()
  }

}
