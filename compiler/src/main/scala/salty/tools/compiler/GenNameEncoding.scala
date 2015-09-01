package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir
import salty.ir.{Name => N}

trait GenNameEncoding extends SubComponent {
  import global._, definitions._

  def encodeFullFieldName(sym: Symbol): N = N.Nested(encodeClassName(sym.owner),
                                                     encodeFieldName(sym))

  def encodeFieldName(sym: Symbol): N = N.Global(sym.name.toString)

  def encodeFullDefName(sym: Symbol): N = N.Nested(encodeClassName(sym.owner),
                                                encodeDefName(sym))

  def encodeDefName(sym: Symbol): N = N.Global(sym.name.toString)

  def encodeClassName(sym: Symbol): N = N.Global(sym.fullName.toString)
}
