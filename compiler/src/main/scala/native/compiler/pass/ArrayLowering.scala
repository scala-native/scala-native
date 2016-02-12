package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.ArrayClass
 *  - Op.{ArrAlloc, ArrLength, ArrElem}
 */
trait ArrayLowering extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  override def onInstr(instr: Instr) = instr match {
    case Instr(_, _, _: Op.ArrAlloc) =>
      ???

    case Instr(_, _, _: Op.ArrLength) =>
      ???

    case Instr(_, _, _: Op.ArrElem) =>
      ???

    case _ =>
      super.onInstr(instr)
  }

  override def onType(ty: Type): Type = super.onType(ty match {
    case Type.ArrayClass(_) => i8_*
    case _                  => ty
  })
}
