package java.lang.ref

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.javalib.Proxy
import scala.scalanative.runtime.{Intrinsics, RawPtr}

// The garbage collector does not mark the referent of a weak reference, the
// first field of Reference, and clears it once nothing else keeps it alive.
class WeakReference[T <: AnyRef](
    referent: T,
    queue: ReferenceQueue[T]
) extends Reference[T](referent) {

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
