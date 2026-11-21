package java.lang.ref

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.javalib.Proxy

/* The referent must stay the first field: the GC finds it right after the
 * object header (codegen.FieldLayout, ReferenceObject in ObjectHeader.h).
 * Immix and Commix clear weak and phantom referents once unreachable, and soft
 * ones only before an OutOfMemoryError. Boehm keeps weak and phantom referents
 * as hidden pointers and treats soft ones as strong.
 *
 * References with a queue are tracked by ReferenceRegistry, which enqueues
 * them after the GC cleared their referent.
 */
abstract class Reference[T] private[ref] (
    @volatile private[ref] var _gc_modified_referent: T,
    queue: ReferenceQueue[_ >: T]
) {
  import Reference._

  // Guarded by the queue
  private[ref] var state: Int = Active
  // Cleared or enqueued by the program, never enqueued by the GC
  @volatile private var inactive = false
  // Next reference tracked by ReferenceRegistry
  @volatile private[ref] var nextReference: Reference[_] = _

  if (_gc_modified_referent != null) {
    if (LinktimeInfo.gc.isBoehm) {
      // Boehm never clears soft referents
      if (!isSoft) {
        Proxy.GC_Boehm_weakRefInit(referentField)
        if (queue != null) ReferenceRegistry.add(this)
      }
    } else if (queue != null) ReferenceRegistry.add(this)
  }

  private def isSoft: Boolean = this.isInstanceOf[SoftReference[_]]

  private def referentField = classFieldRawPtr(this, "_gc_modified_referent")

  private def referent: T =
    if (LinktimeInfo.gc.isBoehm) {
      if (isSoft) _gc_modified_referent
      else Proxy.GC_Boehm_weakRefGet(referentField).asInstanceOf[T]
    } else _gc_modified_referent

  /** After a collection: false while the referent is alive, otherwise enqueues
   *  this reference unless the program cleared or enqueued it.
   */
  private[ref] def handleCleared(): Boolean =
    referent == null && {
      if (!inactive) enqueueCleared()
      true
    }

  def get(): T = referent

  final def refersTo(obj: T): Boolean =
    referent.asInstanceOf[AnyRef] eq obj.asInstanceOf[AnyRef]

  def clear(): Unit = clearReferent()

  // Not overridable, enqueue() clears even if a subclass overrides clear()
  private def clearReferent(): Unit = {
    inactive = true
    if (LinktimeInfo.gc.isBoehm) {
      if (isSoft) _gc_modified_referent = null.asInstanceOf[T]
      else Proxy.GC_Boehm_weakRefClear(referentField)
    } else _gc_modified_referent = null.asInstanceOf[T]
  }

  def isEnqueued(): Boolean =
    queue != null && queue.synchronized(state == Enqueued)

  def enqueue(): Boolean = {
    clearReferent()
    enqueueCleared()
  }

  // At most once per reference
  private[ref] def enqueueCleared(): Boolean =
    queue != null && queue.enqueue(this)
}

object Reference {
  def reachabilityFence(ref: Any): Unit =
    Proxy.reachabilityFence(ref.asInstanceOf[AnyRef])

  // States of a reference, see ReferenceQueue
  private[ref] final val Active = 0
  private[ref] final val Enqueued = 1
  private[ref] final val Dequeued = 2
}
