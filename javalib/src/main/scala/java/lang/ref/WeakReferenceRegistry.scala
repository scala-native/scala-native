package java.lang.ref

import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWeakReferenceSupported
import scala.scalanative.runtime.GC
import scala.scalanative.libc.stdatomic._
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.annotation.alwaysinline
import scala.util.control.NonFatal

/* Should always be treated as a module by the compiler.
 * _gc_modified_postGCControlField is explicitly acccessed
 * by the internals of the immix and commix GC.
 */
private[java] object WeakReferenceRegistry {
  @volatile private var weakRefsHead: WeakReference[_] = _

  @alwaysinline private def weakRefsHeadPtr = fromRawPtr[WeakReference[_]](
    classFieldRawPtr(this, "weakRefsHead")
  )

  if (isWeakReferenceSupported) {
    GC.registerWeakReferenceHandler(() => {
      // This method is designed for calls from C and therefore should not include
      // non statically reachable fields or methods.

      // Detach current weak refs linked-list to allow for unsynchronized updated
      val expected = stackalloc[WeakReference[_]]()
      var detached = null.asInstanceOf[WeakReference[_]]
      while ({
        detached = weakRefsHead
        !expected = detached
        !atomic_compare_exchange_strong(weakRefsHeadPtr, expected, null)
      }) ()

      var current = detached
      var prev = null.asInstanceOf[WeakReference[_]]
      while (current != null) {
        // Actual post GC logic
        val wasCollected = current.get() == null
        if (wasCollected) {
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
          // Update the detached linked list
          if (prev == null) detached = current.nextReference
          else prev.nextReference = current.nextReference
        } else prev = current
        current = current.nextReference
      }

      // Reattach the weak refs list to the possibly updated head
      if (detached != null) while ({
        val currentHead = weakRefsHead
        !expected = currentHead
        prev.nextReference = currentHead
        !atomic_compare_exchange_strong(weakRefsHeadPtr, expected, detached)
      }) ()
    })
  }

  private[ref] def add(weakRef: WeakReference[_]): Unit =
    if (isWeakReferenceSupported) {
      assert(weakRef.nextReference == null)
      var head = weakRefsHead
      val expected = stackalloc[WeakReference[_]]()
      !expected = null
      if (atomic_compare_exchange_strong(weakRefsHeadPtr, expected, weakRef)) ()
      else
        while ({
          var currentHead = !expected
          weakRef.nextReference = currentHead
          !expected = currentHead
          !atomic_compare_exchange_strong(weakRefsHeadPtr, expected, weakRef)
        }) ()
    }

  // Scala Native javalib exclusive functionality.
  // Can be used to emulate finalize for javalib classes where necessary.
  private[java] def addHandler(
      weakRef: WeakReference[_],
      handler: Function0[Unit]
  ): Unit =
    if (isWeakReferenceSupported) { weakRef.postGCHandler = handler }
}
