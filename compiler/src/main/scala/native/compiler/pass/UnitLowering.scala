package native
package compiler
package pass

import native.nir._

/** Eliminates unit type and unit value.
 *
 *  Eliminates:
 *  - Val.Unit
 *  - Type.Unit
 */
trait UnitLowering extends Pass {
  override def onType(ty: Type) = super.onType(ty match {
    case Type.Unit => Type.Void
    case ty        => ty
  })
}
