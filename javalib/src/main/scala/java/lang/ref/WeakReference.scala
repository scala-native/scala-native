package java.lang.ref

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.javalib.Proxy
import scala.scalanative.runtime.{Intrinsics, RawPtr}

// Class strongly connected to GC.
// _gc_modified_ modifier is used by codegen
// to register which fields shall not be marked by GC.
// _gc_unmarked_ works like this only in the context of
// the WeakReference class.
class WeakReference[T <: AnyRef](
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
  @volatile private var boehmReferentSlot: RawPtr = _

  if (_gc_modified_referent != null) {
    if (LinktimeInfo.gc.isBoehm) {
      boehmReferentSlot =
        Proxy.GC_Boehm_weakRefSlotCreate(_gc_modified_referent)
      // Keep this field null under Boehm; otherwise it is a strong reference.
      _gc_modified_referent = null.asInstanceOf[T]
    }
    WeakReferenceRegistry.add(this)
  }

  // A next weak reference in the form linked-list used by WeakReferenceRegistry
  @volatile private[ref] var nextReference: WeakReference[_] = _

  override def get(): T =
    if (LinktimeInfo.gc.isBoehm) {
      if (boehmReferentSlot == null) null.asInstanceOf[T]
      else Proxy.GC_Boehm_weakRefSlotGet[T](boehmReferentSlot)
    } else {
      _gc_modified_referent
    }

  override def enqueue(): Boolean = {
    clear()
    if (!enqueued && queue != null) {
      queue.enqueue(this)
      enqueued = true
      true
    } else false
  }

  override def isEnqueued(): Boolean = enqueued

  override def clear(): Unit =
    if (LinktimeInfo.gc.isBoehm) {
      if (boehmReferentSlot != null) {
        Proxy.GC_Boehm_weakRefSlotClear(boehmReferentSlot)
        boehmReferentSlot = null
      }
    } else {
      _gc_modified_referent = null.asInstanceOf[T]
    }

  override private[ref] def markDequeued(): Unit =
    enqueued = false

}
