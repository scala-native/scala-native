package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Op.{Alloc, Size}
 *  - Type.Size
 */
trait AllocLowering extends Pass {
  private val allocSig = Type.Function(Seq(Type.Size), Type.Ptr(Type.I8))
  private val allocVal = Val.Global(Global.Atom("sm_alloc"), Type.Ptr(allocSig))

  override def onInstr(instr: Instr) = instr match {
    case Instr(Some(alloc), _, Op.Alloc(ty)) =>
      val size    = fresh()
      val sizeTy  = Type.Size
      val sizeVal = Val.Local(size, sizeTy)
      Seq(Instr(size,  Op.Size(ty)),
          Instr(alloc, Op.Call(allocSig, allocVal, Seq(sizeVal)))).flatMap(onInstr)
    case Instr(Some(size), _, Op.Size(ty)) =>
      val offset    = fresh()
      val offsetTy  = Type.Ptr(ty)
      val offsetVal = Val.Local(offset, offsetTy)
      Seq(Instr(offset, Op.Elem(ty, Val.Zero(offsetTy), Seq(Val.I32(1)))),
          Instr(size,   Op.Conv(Conv.Ptrtoint, Type.Size, offsetVal))).flatMap(onInstr)
    case _ =>
      super.onInstr(instr)
  }

  override def onType(ty: Type) = super.onType(ty match {
    case Type.Size => Type.I64
    case _         => ty
  })
}
