package java.lang.ref

import scala.collection.{immutable, mutable}
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWeakReferenceSupported
import scala.scalanative.runtime.GC

/* Should always be treated as a module by the compiler.
 * _gc_modified_postGCControlField is explicitly acccessed
 * by the internals of the immix and commix GC.
 */
private[java] object WeakReferenceRegistry {
  private var weakRefList: immutable.List[WeakReference[_]] =
    immutable.List()

  private val postGCHandlerMap
      : mutable.HashMap[WeakReference[_], Function0[Unit]] =
    new mutable.HashMap()

  if (isWeakReferenceSupported) {
    GC.registerWeakReferenceHandler(
      CFuncPtr.toPtr(CFuncPtr0.fromScalaFunction(postGCControl))
    )
  }

  // This method is designed for calls from C and therefore should not include
  // non statically reachable fields or methods.
  private def postGCControl(): Unit =
    weakRefList = weakRefList.filter { weakRef =>
      if (weakRef.get() == null) {
        weakRef.enqueue()
        if (postGCHandlerMap.contains(weakRef)) {
          postGCHandlerMap(weakRef)()
          postGCHandlerMap.remove(weakRef)
        }
        true
      } else {
        false
      }
    }

  private[ref] def add(weakRef: WeakReference[_]): Unit =
    if (isWeakReferenceSupported) weakRefList = weakRefList ++ List(weakRef)

  // Scala Native javalib exclusive functionality.
  // Can be used to emulate finalize for javalib classes where necessary.
  private[java] def addHandler(
      weakRef: WeakReference[_],
      handler: Function0[Unit]
  ): Unit =
    if (isWeakReferenceSupported) postGCHandlerMap += (weakRef -> handler)
}
