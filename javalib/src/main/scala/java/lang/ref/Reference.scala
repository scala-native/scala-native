package java.lang.ref

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.RawPtr
import scala.scalanative.runtime.javalib.Proxy

/* Subclassed only by the reference types of this package, as on the JVM.
 *
 * The garbage collector reads and clears the referent in place: it must stay
 * the first field of Reference, so that it follows the object header in every
 * reference object. See codegen.FieldLayout and ReferenceObject in
 * ObjectHeader.h. Immix and Commix never mark it through the reference: weak
 * and phantom referents are cleared once nothing else keeps them alive, soft
 * ones only before an OutOfMemoryError.
 *
 * Boehm keeps weak and phantom referents in a disappearing link instead, and
 * soft referents in the field, as strong references.
 *
 * References with a queue are kept in ReferenceRegistry, which enqueues them
 * once the GC cleared their referent.
 */
abstract class Reference[T] private[ref] (
    @volatile private[ref] var _gc_modified_referent: T,
    queue: ReferenceQueue[_ >: T]
) {
  import Reference._

  @volatile private var boehmReferentSlot: RawPtr = _
  // Guarded by the queue
  private[ref] var state: Int = Active
  // Set once the program cleared or enqueued this reference: the GC no
  // longer enqueues it
  @volatile private var inactive = false
  // The next reference in the list of ReferenceRegistry
  @volatile private[ref] var nextReference: Reference[_] = _

  if (LinktimeInfo.gc.isBoehm) {
    if (_gc_modified_referent != null && !isSoft) {
      boehmReferentSlot = Proxy.GC_Boehm_weakRefSlotCreate(
        _gc_modified_referent.asInstanceOf[AnyRef]
      )
      // Keep this field null under Boehm; otherwise it is a strong reference.
      _gc_modified_referent = null.asInstanceOf[T]
    }
  }

  // Boehm never clears soft referents
  if (queue != null && referent != null) {
    if (LinktimeInfo.gc.isBoehm) {
      if (!isSoft) ReferenceRegistry.add(this)
    } else ReferenceRegistry.add(this)
  }

  // Soft references are cleared only when memory runs out
  private[ref] def isSoft: Boolean = false

  private def referent: T =
    if (LinktimeInfo.gc.isBoehm) boehmReferent
    else _gc_modified_referent

  // Soft referents stay in the field under Boehm
  private def boehmReferent: T = {
    val slot = boehmReferentSlot
    if (slot == null) _gc_modified_referent
    else Proxy.GC_Boehm_weakRefSlotGet[AnyRef](slot).asInstanceOf[T]
  }

  /** Called by ReferenceRegistry after a collection. Returns false while the
   *  referent is alive. Otherwise enqueues the reference, unless the program
   *  already cleared or enqueued it, and returns true.
   */
  private[ref] def handleCleared(): Boolean =
    referent == null && {
      if (!inactive) enqueueCleared()
      true
    }

  def get(): T = referent

  final def refersTo(obj: T): Boolean =
    referent.asInstanceOf[AnyRef] eq obj.asInstanceOf[AnyRef]

  def clear(): Unit = {
    inactive = true
    clearReferent()
  }

  private def clearReferent(): Unit = {
    if (LinktimeInfo.gc.isBoehm) clearBoehmSlot()
    _gc_modified_referent = null.asInstanceOf[T]
  }

  private def clearBoehmSlot(): Unit = {
    val slot = boehmReferentSlot
    if (slot != null) {
      Proxy.GC_Boehm_weakRefSlotClear(slot)
      boehmReferentSlot = null
    }
  }

  def isEnqueued(): Boolean =
    queue != null && queue.synchronized(state == Enqueued)

  def enqueue(): Boolean = {
    clear()
    enqueueCleared()
  }

  /** Adds this reference, with its referent already cleared, to its queue
   *  unless it was enqueued before.
   */
  private[ref] def enqueueCleared(): Boolean =
    queue != null && queue.enqueue(this)
}

private[ref] object Reference {
  final val Active = 0
  final val Enqueued = 1
  final val Dequeued = 2
}
