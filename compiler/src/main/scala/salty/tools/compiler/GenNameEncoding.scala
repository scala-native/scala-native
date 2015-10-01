package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir, ir.{Name => N, Extern, Desc}

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global._, definitions._

  def genParamId(sym: Symbol) = sym.name.toString
  def genLabelId(sym: Symbol) = sym.name.toString

  def genFieldDefn(sym: Symbol) = Extern(genFieldName(sym))
  def genFieldName(sym: Symbol) = N.Field(genClassName(sym.owner), sym.name.toString)

  def genClassDefn(sym: Symbol) = Extern(genClassName(sym))
  def genClassName(sym: Symbol): N = {
    val id = sym.fullName.toString
    val name =
      if (sym.isModule)
        genClassName(sym.moduleClass)
      else if (sym.isModuleClass || sym.isImplClass)
        N.Module(id)
      else if (sym.isInterface)
        N.Interface(id)
      else
        N.Class(id)
    //println(s"name for $sym is ${name.fullString}")
    name
  }

  def genDefDefn(sym: Symbol) = Extern(genDefName(sym))
  def genDefName(sym: Symbol) = {
    val owner  = genClassName(sym.owner)
    val id     = sym.name.toString
    val tpe    = sym.tpe.widen
    val params = tpe.params.map(kindName).toSeq
    val ret    = kindName(tpe.resultType)

    N.Method(owner, id, params, ret)
  }

  private def kindName(sym: Symbol): ir.Name =
    kindName(genKind(sym.tpe))

  private def kindName(tpe: Type): ir.Name =
    kindName(genKind(tpe.widen))

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
