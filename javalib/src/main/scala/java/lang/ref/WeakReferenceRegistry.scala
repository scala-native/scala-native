package java.lang.ref

import scala.collection.{immutable, mutable}
import scala.scalanative.unsafe._
import scala.scalanative.runtime.GCInfo
import scala.scalanative.runtime.GCInfo._

/* Should always be treated as a module by the compiler.
 * _gc_modified_postGCControlField is explicitly acccessed
 * by the internals of the immix and commix GC.
 */
private[java] object WeakReferenceRegistry {
  private var weakRefList: immutable.List[WeakReference[_ >: Null <: AnyRef]] =
    immutable.List()

  private val postGCHandlerMap
      : mutable.HashMap[WeakReference[_ >: Null <: AnyRef], Function0[Unit]] =
    new mutable.HashMap()

  // _gc_modified_ is used in codegen to recognize a post gc handler
  // function and register a pointer to it.
  // This happens only in the context of WeakReferenceRegistry.
  val _gc_modified_postGCControlField = CFuncPtr
    .toPtr(CFuncPtr0.fromScalaFunction(WeakReferenceRegistry.postGCControl))
    .toLong

  private def postGCControl(): Unit =
    WeakReferenceRegistry.weakRefList =
      WeakReferenceRegistry.weakRefList.filter { weakRef =>
        if (weakRef.get() == null) {
          weakRef.enqueue()
          if (WeakReferenceRegistry.postGCHandlerMap.contains(weakRef)) {
            WeakReferenceRegistry.postGCHandlerMap(weakRef)()
            WeakReferenceRegistry.postGCHandlerMap.remove(weakRef)
          }
          true
        } else {
          false
        }
      }

  private[ref] def add(weakRef: WeakReference[_ >: Null <: AnyRef]): Unit =
    GCInfo.getType() match {
      case Immix() | Commix() => weakRefList = weakRefList ++ List(weakRef)
      case _                  =>
    }

  // Scala Native javalib exclusive functionality.
  // Can be used to emulate finalize for javalib classes where necessary.
  private[java] def addHandler(
      weakRef: WeakReference[_ >: Null <: AnyRef],
      handler: Function0[Unit]
  ): Unit =
    GCInfo.getType() match {
      case Immix() | Commix() => postGCHandlerMap += (weakRef -> handler)
      case _                  =>
    }
}
