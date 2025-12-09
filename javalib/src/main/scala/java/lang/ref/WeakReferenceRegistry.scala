package java.lang.ref

import java.util.concurrent.locks.LockSupport

import scala.annotation.tailrec

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic._
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.javalib.Proxy
import scala.scalanative.unsafe._

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
    if (current == null) {
      val last = if (prev != null) prev else head
      (
        head.asInstanceOf[WeakReference[Any]],
        last.asInstanceOf[WeakReference[Any]]
      )
    } else {
      val next = current.nextReference
      val headNew =
        if (null != current.get()) head
        else {
          current.enqueue()
          if (prev == null) next
          else {
            prev.nextReference = next
            head
          }
        }
      enqueueCollectedReferences(headNew, next, current)
    }

  private def handleCollectedReferences(): Unit = {
    // This method is designed for calls from C and therefore should not include
    // non statically reachable fields or methods.
    // Detach current weak refs linked-list to allow for unsynchronized updated
    val detachedHeadPtr = stackalloc[WeakReference[_]]()
    def replaceHead(head: WeakReference[_])(reset: => Unit): Unit = {
      !detachedHeadPtr = weakRefsHead
      while ({
        reset
        !atomic_compare_exchange_strong(weakRefsHeadPtr, detachedHeadPtr, head)
      }) {}
    }

    replaceHead(null) {}

    val detachedHead = !detachedHeadPtr
    val (newHead, newLast) =
      enqueueCollectedReferences(detachedHead, detachedHead, null)

    // Reattach the weak refs list to the possibly updated head
    if (newHead ne null) {
      assert(newLast ne null)
      replaceHead(newHead) { newLast.nextReference = !detachedHeadPtr }
    }
  }

  private object Multithreaded {
    private val referenceHandlerThread = Thread
      .ofPlatform()
      .daemon()
      .group(ThreadGroup.System)
      .name("GC-WeakReferenceHandler")
      .startInternal(() =>
        while (true) {
          handleCollectedReferences()
          LockSupport.park()
        }
      )

    def unpark(): Unit =
      LockSupport.unpark(referenceHandlerThread)
  }

  if (LinktimeInfo.isWeakReferenceSupported) {
    Proxy.GC_setWeakReferencesCollectedCallback { () =>
      if (LinktimeInfo.isMultithreadingEnabled) Multithreaded.unpark()
      else handleCollectedReferences()
    }
  }

  private[ref] def add(weakRef: WeakReference[_]): Unit =
    if (LinktimeInfo.isWeakReferenceSupported) {
      assert(weakRef.nextReference == null)
      val prevHeadPtr = stackalloc[WeakReference[_]]()
      !prevHeadPtr = null
      while (!atomic_compare_exchange_weak(
            weakRefsHeadPtr,
            prevHeadPtr,
            weakRef
          )) {
        weakRef.nextReference = !prevHeadPtr
      }
    }

}
