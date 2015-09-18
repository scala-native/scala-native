package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir
import salty.ir.{Name => N, Defn => D}

trait GenNameEncoding extends SubComponent {
  import global._, definitions._

  def genFieldDefn(sym: Symbol) = D.Extern(genFieldName(sym))
  def genFieldName(sym: Symbol) = N.Nested(genClassName(sym.owner),
                                           N.Global(sym.name.toString))

  def genDefDefn(sym: Symbol) = D.Extern(genDefName(sym))
  def genDefName(sym: Symbol) = N.Nested(genClassName(sym.owner),
                                         N.Global(sym.name.toString))

  def genClassDefn(sym: Symbol) = D.Extern(genClassName(sym))
  def genClassName(sym: Symbol) = N.Global(sym.fullName.toString)

  def genParamName(sym: Symbol) = N.Global(sym.name.toString)
  def genLabelName(sym: Symbol) = N.Global(sym.name.toString)
}
