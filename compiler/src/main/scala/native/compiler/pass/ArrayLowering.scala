package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.ArrayClass
 *  - Op.{ArrAlloc, ArrLength, ArrElem}
 */
class ArrayLowering(implicit fresh: Fresh) extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  override def preInst = {
    case Inst(Some(n), Op.ArrAlloc(ty, len)) =>
      val arrty = Intr.type_of_array.get(ty).getOrElse(Intr.type_of_array_object)
      val size  = Val.Local(fresh(), Type.Size)
      Seq(
        Inst(size.name, Op.ArrSizeOf(ty, len)),
        Inst(n,         Intr.call(Intr.alloc, arrty, size))
      )

    case Inst(Some(n), Op.ArrLength(arr)) =>
      val arrptr = Type.Ptr(Intr.array_object)
      val cast = Val.Local(fresh(), arrptr)
      val elem = Val.Local(fresh(), Type.Ptr(Type.I32))
      Seq(
        Inst(cast.name, Op.Conv(Conv.Bitcast, arrptr, arr)),
        Inst(elem.name, Op.Elem(Type.I32, cast, Seq(Val.I32(0), Val.I32(1)))),
        Inst(n,         Op.Load(Type.I32, elem))
      )

    case Inst(Some(n), Op.ArrElem(ty, arr, idx)) =>
      val arrptr = Type.Ptr(Intr.array.get(ty).getOrElse(Intr.array_object))
      val cast = Val.Local(fresh(), arrptr)
      val elem = Val.Local(fresh(), Type.Ptr(ty))
      Seq(
        Inst(cast.name, Op.Conv(Conv.Bitcast, arrptr, arr)),
        Inst(elem.name, Op.Elem(ty, cast, Seq(Val.I32(0), Val.I32(2), idx))),
        Inst(n,         Op.Load(ty, elem))
      )
  }

  override def preType = {
    case Type.ArrayClass(_) => i8_*
  }
}
