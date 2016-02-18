package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.ArrayClass
 *  - Op.{ArrAlloc, ArrLength, ArrElem}
 */
class ArrayLowering extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  override def preInst = {
    case Inst(_, _: Op.ArrAlloc) =>
      ???

    case Inst(_, _: Op.ArrLength) =>
      ???

    case Inst(_, _: Op.ArrElem) =>
      ???
  }

  override def preType = {
    case Type.ArrayClass(_) => i8_*
  }
}
