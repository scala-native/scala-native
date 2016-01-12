package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.ArrayClass
 *  - Op.{AllocArray, ArrayLength, ArrayElem}
 */
trait ArrayLowering extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  override def onType(ty: Type): Type = super.onType(ty match {
    case Type.ArrayClass(_) => i8_*
    case _                  => ty
  })
}
