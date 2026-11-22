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
 * cleared referents, the GC callback enqueues the cleared references. A scan
 * also drops the references the program cleared or enqueued; enough of them
 * trigger one from `add`.
 */
private[java] object ReferenceRegistry {
  @volatile private var refsHead: Reference[_] = _

  // Heuristics only: listed references, and those cleared by the program
  // since the last scan
  private val listed = new AtomicInteger(0)
  private val retired = new AtomicInteger(0)
  private final val MinRetiredToScan = 64

  // Scans requested and not yet run, a single thread scans at a time
  private val scanRequests = new AtomicInteger(0)

  @alwaysinline private def refsHeadPtr = fromRawPtr[Reference[_]](
    classFieldRawPtr(this, "refsHead")
  )

  // Raw memory, a Tag would call ClassTag.apply, which creates a WeakReference
  @alwaysinline private def loadHead(slot: RawPtr): Reference[_] =
    loadObject(slot).asInstanceOf[Reference[_]]

  // Last reference kept by the scan, written by the single scanning thread
  private var scanLast: Reference[_] = _

  // A reference that failed to be enqueued, for lack of memory, stays listed
  private def handleCleared(ref: Reference[_]): Boolean =
    try ref.handleCleared()
    catch { case _: Throwable => false }

  // Returns the first reference kept, without allocating
  @tailrec private def enqueueCollectedReferences(
      head: Reference[_],
      current: Reference[_],
      prev: Reference[_]
  ): Reference[_] =
    if (current == null) {
      scanLast = if (prev != null) prev else head
      head
    } else {
      val next = current.nextReference
      if (!handleCleared(current))
        enqueueCollectedReferences(head, next, current)
      else {
        current.nextReference = null
        listed.decrementAndGet()
        if (prev == null) enqueueCollectedReferences(next, next, null)
        else {
          prev.nextReference = next
          enqueueCollectedReferences(head, next, prev)
        }
      }
    }

  /* A request made while a scan runs, also by a collection nested in it, is
   * handled by another pass of that scan: a referent cleared after the scan
   * visited its reference is not missed.
   */
  private def handleCollectedReferences(): Unit =
    if (scanRequests.getAndIncrement() == 0) {
      var requests = 1
      try
        while (requests != 0) {
          retired.set(0)
          scanReferences()
          requests = scanRequests.addAndGet(-requests)
        }
      catch {
        case ex: Throwable =>
          scanRequests.set(0) // Let a later request scan
          throw ex
      }
    }

  private def scanReferences(): Unit = {
    // This method is designed for calls from C and therefore should not include
    // non statically reachable fields or methods.
    // Allocates only before detaching the list, which an OutOfMemoryError
    // must not lose.
    val detachedHeadSlot = rawStackalloc[RawPtr]()
    val detachedHeadPtr = fromRawPtr[Reference[_]](detachedHeadSlot)
    val headPtr = refsHeadPtr

    // Detach current refs linked-list to allow for unsynchronized updated
    storeObject(detachedHeadSlot, refsHead)
    while (!atomic_compare_exchange_strong(headPtr, detachedHeadPtr, null)) {}

    val detachedHead = loadHead(detachedHeadSlot)
    val newHead = enqueueCollectedReferences(detachedHead, detachedHead, null)
    val newLast = scanLast
    scanLast = null

    // Reattach the refs list to the possibly updated head
    if (newHead ne null) {
      assert(newLast ne null)
      storeObject(detachedHeadSlot, refsHead)
      while ({
        newLast.nextReference = loadHead(detachedHeadSlot)
        !atomic_compare_exchange_strong(headPtr, detachedHeadPtr, newHead)
      }) {}
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
        // A failed scan, e.g. out of memory, is retried on the next request
        try handleCollectedReferences()
        catch { case _: Throwable => () }
        LockSupport.park()
      }
    )

  // Scans the references outside of a collection
  private def requestScan(): Unit =
    if (LinktimeInfo.isMultithreadingEnabled) {
      handlerState.compareAndSet(Idle, Requested)
      startRequestedHandler()
      if (handlerState.get() == Started)
        LockSupport.unpark(referenceHandlerThread)
    } else handleCollectedReferences()

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
      val count = listed.incrementAndGet()
      val cleared = retired.get()
      if (cleared >= MinRetiredToScan && cleared >= count / 2) requestScan()
      // After publishing, so a collection while starting the handler sees ref
      else if (LinktimeInfo.isMultithreadingEnabled) startRequestedHandler()
    }

  // A listed reference was cleared or enqueued by the program
  private[ref] def retire(): Unit =
    if (LinktimeInfo.isWeakReferenceSupported) retired.incrementAndGet()

}
