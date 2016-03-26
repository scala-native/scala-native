package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import util.ScopedVar, ScopedVar.scoped
import nir._

/** Eliminates:
 *  - Type.Size
 *  - Op.{SizeOf, ArrSizeOf}
 */
class SizeLowering(implicit fresh: Fresh) extends Pass {
  override def preInst = {
    case Inst(n, Op.SizeOf(ty)) =>
      val typtr = Type.Ptr(ty)
      val elem = Val.Local(fresh(), typtr)
      Seq(
        Inst(elem.name, Op.Elem(typtr, Val.Zero(typtr), Seq(Val.I32(1)))),
        Inst(n,         Op.Conv(Conv.Ptrtoint, Type.I64, elem))
      )
  }

  override def preType = {
    case Type.Size => Type.I64
  }
}
