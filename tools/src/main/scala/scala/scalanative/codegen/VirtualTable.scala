package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.nir.Rt.{Type => _, _}
import scalanative.sema._

class VirtualTable(meta: Metadata, cls: linker.Class) {
  private val slots: mutable.UnrolledBuffer[String] =
    cls.parent.fold {
      mutable.UnrolledBuffer.empty[String]
    } { parent =>
      meta.vtable(parent).slots.clone
    }
  private val impls: mutable.Map[String, Val] =
    mutable.Map.empty[String, Val]
  locally {
    def addSlot(sig: String): Unit = {
      assert(!slots.contains(sig))
      val index = slots.size
      slots += sig
    }
    def addImpl(sig: String): Unit = {
      val impl =
        cls.resolve(sig).map(Val.Global(_, Type.Ptr)).getOrElse(Val.Null)
      impls(sig) = impl
    }
    slots.foreach { sig =>
      addImpl(sig)
    }
    cls.calls.foreach { sig =>
      if (meta.linked.targets(Type.Class(cls.name), sig).size > 1) {
        if (!impls.contains(sig)) {
          addSlot(sig)
          addImpl(sig)
        }
      }
    }
  }
  val value: Val =
    Val.Array(Type.Ptr, slots.map(impls))
  val ty =
    value.ty
  def index(sig: String): Int =
    slots.indexOf(sig)
  def at(index: Int): Val =
    impls(slots(index))
}
