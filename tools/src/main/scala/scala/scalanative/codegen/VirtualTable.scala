package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.nir.Rt.{Type => _, _}

class VirtualTable(meta: Metadata, cls: linker.Class) {
  private val slots: mutable.UnrolledBuffer[Sig] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[Sig]
    } { parent =>
      meta.vtable(parent).slots.clone
    }
  private val impls: mutable.Map[Sig, Val] =
    mutable.Map.empty[Sig, Val]
  locally {
    def addSlot(sig: Sig): Unit = {
      assert(!slots.contains(sig))
      val index = slots.size
      slots += sig
    }
    def addImpl(sig: Sig): Unit = {
      val impl =
        cls.resolve(sig).map(Val.Global(_, Type.Ptr)).getOrElse(Val.Null)
      impls(sig) = impl
    }
    slots.foreach { sig =>
      addImpl(sig)
    }
    cls.calls.foreach { sig =>
      if (cls.targets(sig).size > 1) {
        if (!impls.contains(sig)) {
          addSlot(sig)
          addImpl(sig)
        }
      }
    }
  }
  val value: Val =
    Val.ArrayValue(Type.Ptr, slots.map(impls))
  val ty =
    value.ty
  def index(sig: Sig): Int =
    slots.indexOf(sig)
  def at(index: Int): Val =
    impls(slots(index))
}
