package scala.scalanative
package codegen

import scala.collection.mutable

private[codegen] class VirtualTable(cls: linker.Class)(implicit meta: Metadata) {

  private val slots: mutable.UnrolledBuffer[nir.Sig] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[nir.Sig]
    } { parent => meta.vtable(parent).slots.clone }
  private val impls: mutable.Map[nir.Sig, nir.Val] =
    mutable.Map.empty[nir.Sig, nir.Val]
  locally {
    def addSlot(sig: nir.Sig): Unit = {
      assert(!slots.contains(sig))
      val index = slots.size
      slots += sig
    }
    def addImpl(sig: nir.Sig): Unit = {
      val impl =
        cls
          .resolve(sig)
          .map(nir.Val.Global(_, nir.Type.Ptr))
          .getOrElse(nir.Val.Null)
      impls(sig) = impl
    }
    slots.foreach { sig => addImpl(sig) }
    cls.calls.foreach { sig =>
      if (cls.targets(sig).size > 1) {
        if (!impls.contains(sig)) {
          addSlot(sig)
          addImpl(sig)
        }
      }
    }
  }
  val value: nir.Val =
    nir.Val.ArrayValue(nir.Type.Ptr, slots.map(impls).toSeq)
  val ty =
    value.ty
  def index(sig: nir.Sig): Int =
    slots.indexOf(sig)
  def at(index: Int): nir.Val =
    impls(slots(index))

}
