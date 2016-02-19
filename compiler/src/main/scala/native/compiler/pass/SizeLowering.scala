package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

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

    case Inst(n, Op.ArrSizeOf(ty, len)) =>
      ???
  }

  override def preType = {
    case Type.Size => Type.I64
  }
}
