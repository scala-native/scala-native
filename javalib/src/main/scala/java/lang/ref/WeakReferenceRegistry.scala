package java.lang.ref

import scala.collection.{immutable, mutable}
import scala.scalanative.unsafe._

/* Should always be treated as a module by the compiler.
   _gc_modified_postGCControlField is explicitly acccessed by the GC.
 */
private[java] object WeakReferenceRegistry {
  private var weakRefList: immutable.List[WeakReference[_ >: Null <: AnyRef]] =
    immutable.List()

  // Sn javalib exclusive functionality.
  // Can be used to simulate finalize for javalib classes where necessary.
  private val postGCHandlerMap
      : mutable.HashMap[WeakReference[_ >: Null <: AnyRef], Function0[Unit]] =
    new mutable.HashMap()

  // _gc_modified_ is used in codegen
  // to register pointer to a post gc handler function.
  // This happens only in the context of WeakReferenceRegistry.
  val _gc_modified_postGCControlField = CFuncPtr
    .toPtr(CFuncPtr0.fromScalaFunction(WeakReferenceRegistry.postGCControl))
    .toLong

  def postGCControl(): Unit = {
    WeakReferenceRegistry.weakRefList.foreach { weakRef =>
      if (weakRef.get() == null) {
        weakRef.enqueue()
        if (WeakReferenceRegistry.postGCHandlerMap.contains(weakRef)) {
          WeakReferenceRegistry.postGCHandlerMap(weakRef)()
          WeakReferenceRegistry.postGCHandlerMap.remove(weakRef)
        }
      }
    }
  }

  private[java] def add(weakRef: WeakReference[_ >: Null <: AnyRef]): Unit =
    weakRefList = weakRefList ++ List(weakRef)

  private[java] def addHandler(
      weakRef: WeakReference[_ >: Null <: AnyRef],
      handler: Function0[Unit]
  ): Unit =
    postGCHandlerMap += (weakRef -> handler)
}
