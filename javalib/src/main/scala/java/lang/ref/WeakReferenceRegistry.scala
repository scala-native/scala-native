package java.lang.ref

import java.{util => ju}
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWeakReferenceSupported
import scala.scalanative.runtime.GC
import scala.collection.concurrent.TrieMap
import scala.scalanative.libc.stdatomic._
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr

/* Should always be treated as a module by the compiler.
 * _gc_modified_postGCControlField is explicitly acccessed
 * by the internals of the immix and commix GC.
 */
private[java] object WeakReferenceRegistry {
  private type WeakRefs = List[WeakReference[_]]
  @volatile private var weakRefList: WeakRefs = List.empty[WeakReference[_]]
  private val postGCHandlerMap = TrieMap.empty[WeakReference[_], () => Unit]

  if (isWeakReferenceSupported) {
    GC.registerWeakReferenceHandler(() =>
      // This method is designed for calls from C and therefore should not include
      // non statically reachable fields or methods.
      updateWeakRefList {
        _.filter { weakRef =>
          val wasCollected = weakRef.get() == null
          if (wasCollected) {
            weakRef.enqueue()
            postGCHandlerMap.remove(weakRef).foreach(_.apply())
          }
          !wasCollected
        }
      }
    )
  }

  private[ref] def add(weakRef: WeakReference[_]): Unit =
    if (isWeakReferenceSupported) updateWeakRefList(weakRef :: _)

  // Scala Native javalib exclusive functionality.
  // Can be used to emulate finalize for javalib classes where necessary.
  private[java] def addHandler(
      weakRef: WeakReference[_],
      handler: Function0[Unit]
  ): Unit =
    if (isWeakReferenceSupported) {
      postGCHandlerMap.putIfAbsent(weakRef, handler)
    }

  @inline
  private def updateWeakRefList(updateFunction: WeakRefs => WeakRefs): Unit = {
    // Normally we would use j.u.c.atomic.AtomicReference or s.sn.libc.stdatomic.AtomicRef
    // however their usage leads to SIGSEGV, but only in tests. I'm not sure why exactly it's happening,
    // but it might be an issue in initialization of main thread which depends on WeakReference by ThreadLocal values
    val ptr = fromRawPtr[WeakRefs](
      classFieldRawPtr(this, "weakRefList")
    )
    val expected = stackalloc[WeakRefs]()
    var prev = weakRefList
    while ({
      val newValue = updateFunction(prev)
      !expected = prev
      !atomic_compare_exchange_weak(ptr, expected, newValue)
    }) prev = !expected
  }
}
