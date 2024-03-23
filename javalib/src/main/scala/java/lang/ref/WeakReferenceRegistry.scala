package java.lang.ref

import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWeakReferenceSupported
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.runtime.Proxy
import scala.scalanative.libc.stdatomic._
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.annotation.alwaysinline
import scala.util.control.NonFatal
import java.util.concurrent.locks.LockSupport
import scala.annotation.tailrec
import scala.scalanative.runtime.Proxy.GCWeakReferencesCollectedCallback

/* Should always be treated as a module by the compiler.
 * _gc_modified_postGCControlField is explicitly acccessed
 * by the internals of the immix and commix GC.
 */
private[java] object WeakReferenceRegistry {
  @volatile private var weakRefsHead: WeakReference[_] = _

  @alwaysinline private def weakRefsHeadPtr = fromRawPtr[WeakReference[_]](
    classFieldRawPtr(this, "weakRefsHead")
  )

  @tailrec private def enqueueCollectedReferences(
      head: WeakReference[_],
      current: WeakReference[_],
      prev: WeakReference[_]
  ): (WeakReference[Any], WeakReference[Any]) =
    if (current == null)
      (
        head.asInstanceOf[WeakReference[Any]],
        prev.asInstanceOf[WeakReference[Any]]
      )
    else
      current.get() match {
        case collected @ null =>
          current.enqueue()
          val handler = current.postGCHandler
          if (handler != null) {
            try handler()
            catch {
              case NonFatal(err) =>
                val thread = Thread.currentThread()
                thread
                  .getUncaughtExceptionHandler()
                  .uncaughtException(thread, err)
            }
          }
          if (prev == null)
            enqueueCollectedReferences(
              current.nextReference,
              current.nextReference,
              current
            )
          else {
            prev.nextReference = current.nextReference
            enqueueCollectedReferences(head, current.nextReference, current)
          }
        case _ => enqueueCollectedReferences(head, current.nextReference, prev)
      }
  private def handleCollectedReferences(): Unit = {
    // This method is designed for calls from C and therefore should not include
    // non statically reachable fields or methods.
    if (!isMultithreadingEnabled) {
      enqueueCollectedReferences(weakRefsHead, weakRefsHead, null)
    } else {
      // Detach current weak refs linked-list to allow for unsynchronized updated
      val expected = stackalloc[WeakReference[_]]()
      var detached = null.asInstanceOf[WeakReference[_]]
      while ({
        detached = weakRefsHead
        !expected = detached
        !atomic_compare_exchange_strong(weakRefsHeadPtr, expected, null)
      }) ()

      val (newDetachedHead, detachedTail) =
        enqueueCollectedReferences(detached, detached, null)

      // Reattach the weak refs list to the possibly updated head
      if (newDetachedHead != null) while ({
        val currentHead = weakRefsHead
        !expected = currentHead
        detachedTail.nextReference = currentHead
        !atomic_compare_exchange_strong(
          weakRefsHeadPtr,
          expected,
          newDetachedHead
        )
      }) ()
    }
  }

  private lazy val referenceHandlerThread = Thread
    .ofPlatform()
    .daemon()
    .group(ThreadGroup.System)
    .name("GC-WeakReferenceHandler")
    .start(() =>
      while (true) {
        handleCollectedReferences()
        LockSupport.park()
      }
    )

  if (isWeakReferenceSupported) {
    Proxy.GC_setWeakReferencesCollectedCallback { () =>
      if (isMultithreadingEnabled) LockSupport.unpark(referenceHandlerThread)
      else handleCollectedReferences()
    }
  }

  private[ref] def add(weakRef: WeakReference[_]): Unit =
    if (isWeakReferenceSupported) {
      assert(weakRef.nextReference == null)
      var head = weakRefsHead
      if (!isMultithreadingEnabled) {
        weakRef.nextReference = head
        weakRefsHead = weakRef
      } else {
        val expected = stackalloc[WeakReference[_]]()
        !expected = null
        if (atomic_compare_exchange_weak(weakRefsHeadPtr, expected, weakRef)) ()
        else
          while ({
            var currentHead = !expected
            weakRef.nextReference = currentHead
            !expected = currentHead
            !atomic_compare_exchange_weak(weakRefsHeadPtr, expected, weakRef)
          }) ()
      }
    }

  // Scala Native javalib exclusive functionality.
  // Can be used to emulate finalize for javalib classes where necessary.
  private[java] def addHandler(
      weakRef: WeakReference[_],
      handler: Function0[Unit]
  ): Unit =
    if (isWeakReferenceSupported) { weakRef.postGCHandler = handler }
}
