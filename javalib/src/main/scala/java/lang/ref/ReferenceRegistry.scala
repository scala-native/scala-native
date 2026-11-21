package java.lang.ref

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic._
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.Intrinsics.{
  classFieldRawPtr, loadObject, stackalloc => rawStackalloc, storeObject
}
import scala.scalanative.runtime.javalib.Proxy
import scala.scalanative.runtime.{RawPtr, fromRawPtr}
import scala.scalanative.unsafe._

/* Lock-free list of the references with a queue. After a collection that
 * cleared referents, the GC callback enqueues the cleared references.
 */
private[java] object ReferenceRegistry {
  @volatile private var refsHead: Reference[_] = _

  @alwaysinline private def refsHeadPtr = fromRawPtr[Reference[_]](
    classFieldRawPtr(this, "refsHead")
  )

  // Raw memory, a Tag would call ClassTag.apply, which creates a WeakReference
  @alwaysinline private def loadHead(slot: RawPtr): Reference[_] =
    loadObject(slot).asInstanceOf[Reference[_]]

  @tailrec private def enqueueCollectedReferences(
      head: Reference[_],
      current: Reference[_],
      prev: Reference[_]
  ): (Reference[_], Reference[_]) =
    if (current == null) {
      val last = if (prev != null) prev else head
      (head, last)
    } else {
      val next = current.nextReference
      if (!current.handleCleared())
        enqueueCollectedReferences(head, next, current)
      else {
        current.nextReference = null
        if (prev == null) enqueueCollectedReferences(next, next, null)
        else {
          prev.nextReference = next
          enqueueCollectedReferences(head, next, prev)
        }
      }
    }

  private def handleCollectedReferences(): Unit = {
    // This method is designed for calls from C and therefore should not include
    // non statically reachable fields or methods.
    // Detach current refs linked-list to allow for unsynchronized updated
    val detachedHeadSlot = rawStackalloc[RawPtr]()
    def replaceHead(head: Reference[_])(reset: => Unit): Unit = {
      storeObject(detachedHeadSlot, refsHead)
      while ({
        reset
        !atomic_compare_exchange_strong(
          refsHeadPtr,
          fromRawPtr[Reference[_]](detachedHeadSlot),
          head
        )
      }) {}
    }

    replaceHead(null) {}

    val detachedHead = loadHead(detachedHeadSlot)
    val remaining = enqueueCollectedReferences(detachedHead, detachedHead, null)
    val newHead = remaining._1
    val newLast = remaining._2

    // Reattach the refs list to the possibly updated head
    if (newHead ne null) {
      assert(newLast ne null)
      replaceHead(newHead) {
        newLast.nextReference = loadHead(detachedHeadSlot)
      }
    }
  }

  // The GC callback runs inside an allocation, so it must not start a thread.
  // It handles references inline and requests the handler, started by `add`.
  private object HandlerState {
    final val Idle = 0
    final val Requested = 1
    final val Starting = 2
    final val Started = 3
  }
  import HandlerState._
  private val handlerState = new AtomicInteger(Idle)

  private lazy val referenceHandlerThread: Thread = Thread
    .ofPlatform()
    .daemon()
    .group(ThreadGroup.System)
    .name("GC-ReferenceHandler")
    .startInternal(() =>
      while (true) {
        handleCollectedReferences()
        LockSupport.park()
      }
    )

  private def startRequestedHandler(): Unit =
    if (handlerState.compareAndSet(Requested, Starting)) {
      referenceHandlerThread
      handlerState.set(Started)
      // Callbacks while Starting handled references inline, rescan
      LockSupport.unpark(referenceHandlerThread)
    }

  if (LinktimeInfo.isWeakReferenceSupported) {
    Proxy.GC_setWeakReferencesCollectedCallback { () =>
      if (LinktimeInfo.isMultithreadingEnabled) {
        if (handlerState.get() == Started)
          LockSupport.unpark(referenceHandlerThread)
        else {
          handlerState.compareAndSet(Idle, Requested)
          handleCollectedReferences()
        }
      } else handleCollectedReferences()
    }
  }

  private[ref] def add(ref: Reference[_]): Unit =
    if (LinktimeInfo.isWeakReferenceSupported) {
      assert(ref.nextReference == null)
      val prevHeadSlot = rawStackalloc[RawPtr]()
      storeObject(prevHeadSlot, null)
      while (!atomic_compare_exchange_weak(
            refsHeadPtr,
            fromRawPtr[Reference[_]](prevHeadSlot),
            ref
          )) {
        ref.nextReference = loadHead(prevHeadSlot)
      }
      // After publishing, so a collection while starting the handler sees ref
      if (LinktimeInfo.isMultithreadingEnabled) startRequestedHandler()
    }

}
