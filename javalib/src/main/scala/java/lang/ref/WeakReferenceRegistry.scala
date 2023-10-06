package java.lang.ref

import java.{util => ju}
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWeakReferenceSupported
import scala.scalanative.runtime.GC
import scala.collection.concurrent.TrieMap
import java.util.concurrent.atomic.AtomicReference

/* Should always be treated as a module by the compiler.
 * _gc_modified_postGCControlField is explicitly acccessed
 * by the internals of the immix and commix GC.
 */
private[java] object WeakReferenceRegistry {
  private var weakRefList: AtomicReference[List[WeakReference[_]]] =
    new AtomicReference(Nil)

  private val postGCHandlerMap: TrieMap[WeakReference[_], Function0[Unit]] =
    TrieMap.empty

  if (isWeakReferenceSupported) {
    GC.registerWeakReferenceHandler(
      CFuncPtr.toPtr(CFuncPtr0.fromScalaFunction(postGCControl))
    )
  }

  // This method is designed for calls from C and therefore should not include
  // non statically reachable fields or methods.
  private def postGCControl(): Unit = weakRefList.getAndUpdate {
    _.filter { weakRef =>
      val wasCollected = weakRef.get() == null
      if (wasCollected) {
        weakRef.enqueue()
        postGCHandlerMap.remove(weakRef).foreach(_.apply())
      }
      !wasCollected
    }
  }

  private[ref] def add(weakRef: WeakReference[_]): Unit =
    if (isWeakReferenceSupported) weakRefList.getAndUpdate(weakRef :: _)

  // Scala Native javalib exclusive functionality.
  // Can be used to emulate finalize for javalib classes where necessary.
  private[java] def addHandler(
      weakRef: WeakReference[_],
      handler: Function0[Unit]
  ): Unit =
    if (isWeakReferenceSupported) {
      postGCHandlerMap.putIfAbsent(weakRef, handler)
    }
}
