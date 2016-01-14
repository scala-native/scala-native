package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Val.String
 *  - Type.StringClass
 *  - Op.String*
 */
trait StringLowering extends Pass {
  private val i8_* = Type.Ptr(Type.I8)

  override def onType(ty: Type): Type = super.onType(ty match {
    case Type.StringClass => i8_*
    case _                => ty
  })
}
