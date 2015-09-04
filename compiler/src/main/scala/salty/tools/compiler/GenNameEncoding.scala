package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir
import salty.ir.{Name => N, Defn => D}

trait GenNameEncoding extends SubComponent {
  import global._, definitions._

  def getFieldDefn(sym: Symbol) = D.Extern(getFieldName(sym))
  def getFieldName(sym: Symbol) = N.Nested(getClassName(sym.owner),
                                           N.Global(sym.name.toString))

  def getDefDefn(sym: Symbol) = D.Extern(getDefName(sym))
  def getDefName(sym: Symbol) = N.Nested(getClassName(sym.owner),
                                         N.Global(sym.name.toString))

  def getClassDefn(sym: Symbol) = D.Extern(getClassName(sym))
  def getClassName(sym: Symbol) = N.Global(sym.fullName.toString)
}
