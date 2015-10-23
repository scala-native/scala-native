package salty.tools.compiler.reductions

import salty.ir._, Reduction._

/** Lowers unit to zero-element struct.
 *  Currently it's naively lowered to zero-element struct.
 *
 *  TODO: unit should become Empty type as function return type (i.e. void)
 *  TODO: unit as struct field should disappear
 */
object UnitLowering extends Reduction {
  val ty = Defn.Struct(Seq(), Prim.Unit.name)
  val lit = Lit.Struct(ty, Seq())

  def reduce = {
    case unit @ Lit.Unit() => replaceAll(lit)
    case unit @ Prim.Unit  => replaceAll(ty)
  }
}
