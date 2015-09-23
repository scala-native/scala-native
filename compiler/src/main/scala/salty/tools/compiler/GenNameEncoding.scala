package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir.{Name => N, Extern}

trait GenNameEncoding extends SubComponent {
  import global._, definitions._

  def genFieldDefn(sym: Symbol) = Extern(genFieldName(sym))
  def genFieldName(sym: Symbol) = N.Nested(genClassName(sym.owner),
                                           N.Simple(sym.name.toString))

  def genDefDefn(sym: Symbol) = Extern(genDefName(sym))
  def genDefName(sym: Symbol) = N.Nested(genClassName(sym.owner),
                                         N.Simple(sym.name.toString))

  def genClassDefn(sym: Symbol) = Extern(genClassName(sym))
  def genClassName(sym: Symbol) = N.Simple(sym.fullName.toString)

  def genParamName(sym: Symbol) = N.Simple(sym.name.toString)
  def genLabelName(sym: Symbol) = N.Simple(sym.name.toString)
}
