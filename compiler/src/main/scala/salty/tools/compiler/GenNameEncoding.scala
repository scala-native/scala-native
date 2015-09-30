package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir, ir.{Name => N, Extern, Desc}

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global._, definitions._

  def genFieldDefn(sym: Symbol) = Extern(genFieldName(sym))
  def genFieldName(sym: Symbol) = N.Nested(genClassName(sym.owner),
                                           N.Simple(sym.name.toString))

  def genClassDefn(sym: Symbol) = Extern(genClassName(sym))
  def genClassName(sym: Symbol) = N.Simple(sym.fullName.toString)

  def genParamName(sym: Symbol) = N.Simple(sym.name.toString)
  def genLabelName(sym: Symbol) = N.Simple(sym.name.toString)

  def genDefDefn(sym: Symbol) = Extern(genDefName(sym))
  def genDefName(sym: Symbol) = {
    val base   = N.Nested(genClassName(sym.owner), N.Simple(sym.name.toString))
    val tpe    = sym.tpe
    val params = tpe.params.map(kindName).toSeq
    val ret    = kindName(tpe.resultType)

    N.Overload(base, params, ret)
  }

  private def kindName(sym: Symbol): ir.Name =
    kindName(genKind(sym.tpe))

  private def kindName(tpe: Type): ir.Name =
    kindName(genKind(tpe))

  private def kindName(kind: Kind): ir.Name = {
    val node = toIRType(kind)
    kind match {
      case PrimitiveKind(_) => node.desc.asInstanceOf[Desc.Primitive].name
      case BottomKind(_)    => node.desc.asInstanceOf[Desc.Primitive].name
      case ClassKind(sym)   => genClassName(sym)
      case ArrayKind(kind)  => N.Slice(kindName(kind))
    }
  }
}
