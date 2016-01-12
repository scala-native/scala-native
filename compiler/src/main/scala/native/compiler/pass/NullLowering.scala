package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Val.Null
 *  - Type.NullClass
 */
trait NullLowering extends Pass {
  private val i8_*      = Type.Ptr(Type.I8)
  private val zero_i8_* = Val.Zero(i8_*)

  override def onType(ty: Type) = super.onType(ty match {
    case Type.NullClass => i8_*
    case _              => ty
  })

  override def onVal(value: Val) = super.onVal(value match {
    case Val.Null => zero_i8_*
    case _        => value
  })
}
