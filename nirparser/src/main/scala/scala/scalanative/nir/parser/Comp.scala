package scala.scalanative
package nir
package parser

import fastparse._

object Comp extends Base[nir.Comp] {

  def Ieq[_: P] = P("ieq".!.map(_ => nir.Comp.Ieq))
  def Ine[_: P] = P("ine".!.map(_ => nir.Comp.Ine))
  def Ugt[_: P] = P("ugt".!.map(_ => nir.Comp.Ugt))
  def Uge[_: P] = P("uge".!.map(_ => nir.Comp.Uge))
  def Ult[_: P] = P("ult".!.map(_ => nir.Comp.Ult))
  def Ule[_: P] = P("ule".!.map(_ => nir.Comp.Ule))
  def Sgt[_: P] = P("sgt".!.map(_ => nir.Comp.Sgt))
  def Sge[_: P] = P("sge".!.map(_ => nir.Comp.Sge))
  def Slt[_: P] = P("slt".!.map(_ => nir.Comp.Slt))
  def Sle[_: P] = P("sle".!.map(_ => nir.Comp.Sle))
  def Feq[_: P] = P("feq".!.map(_ => nir.Comp.Feq))
  def Fne[_: P] = P("fne".!.map(_ => nir.Comp.Fne))
  def Fgt[_: P] = P("fgt".!.map(_ => nir.Comp.Fgt))
  def Fge[_: P] = P("fge".!.map(_ => nir.Comp.Fge))
  def Flt[_: P] = P("flt".!.map(_ => nir.Comp.Flt))
  def Fle[_: P] = P("fle".!.map(_ => nir.Comp.Fle))
  override def parser[_: P]: P[nir.Comp] =
    Ieq | Ine | Ugt | Uge | Ult | Ule | Sgt | Sge | Slt | Sle | Feq | Fne | Fgt | Fge | Flt | Fle
}
