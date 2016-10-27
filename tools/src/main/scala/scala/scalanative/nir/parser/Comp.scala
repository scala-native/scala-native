package scala.scalanative
package nir
package parser

import fastparse.all._

object Comp extends Base[nir.Comp] {

  val Ieq = P("ieq".! map (_ => nir.Comp.Ieq))
  val Ine = P("ine".! map (_ => nir.Comp.Ine))
  val Ugt = P("ugt".! map (_ => nir.Comp.Ugt))
  val Uge = P("uge".! map (_ => nir.Comp.Uge))
  val Ult = P("ult".! map (_ => nir.Comp.Ult))
  val Ule = P("ule".! map (_ => nir.Comp.Ule))
  val Sgt = P("sgt".! map (_ => nir.Comp.Sgt))
  val Sge = P("sge".! map (_ => nir.Comp.Sge))
  val Slt = P("slt".! map (_ => nir.Comp.Slt))
  val Sle = P("sle".! map (_ => nir.Comp.Sle))
  val Feq = P("feq".! map (_ => nir.Comp.Feq))
  val Fne = P("fne".! map (_ => nir.Comp.Fne))
  val Fgt = P("fgt".! map (_ => nir.Comp.Fgt))
  val Fge = P("fge".! map (_ => nir.Comp.Fge))
  val Flt = P("flt".! map (_ => nir.Comp.Flt))
  val Fle = P("fle".! map (_ => nir.Comp.Fle))
  override val parser: P[nir.Comp] =
    Ieq | Ine | Ugt | Uge | Ult | Ule | Sgt | Sge | Slt | Sle | Feq | Fne | Fgt | Fge | Flt | Fle
}
