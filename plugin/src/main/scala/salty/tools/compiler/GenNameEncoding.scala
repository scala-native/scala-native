package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir, ir.{Name => N, Extern, Desc}

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global._, definitions._

  def genParamId(sym: Symbol) = sym.name.toString
  def genLabelId(sym: Symbol) = sym.name.toString

  def genFieldDefn(sym: Symbol) = Extern(genFieldName(sym))
  def genFieldName(sym: Symbol) = {
    val owner = genClassName(sym.owner)
    val id0 = sym.name.toString
    val id =
      if (id0.charAt(id0.length()-1) != ' ') id0
      else id0.substring(0, id0.length()-1)

    N.Field(owner, id)
  }

  def genClassDefn(sym: Symbol) = Extern(genClassName(sym))
  def genClassName(sym: Symbol): N = {
    val id = sym.fullName.toString

    if (sym.isModule)
      genClassName(sym.moduleClass)
    else if (sym.isModuleClass || sym.isImplClass)
      N.Module(id)
    else if (sym.isInterface)
      N.Interface(id)
    else
      N.Class(id)
  }

  def genDefDefn(sym: Symbol) = Extern(genDefName(sym))
  def genDefName(sym: Symbol) = {
    val owner  = genClassName(sym.owner)
    val id     = sym.name.toString
    val tpe    = sym.tpe.widen
    val params = tpe.params.map(kindName).toSeq
    val ret    =
      if (sym.name == nme.CONSTRUCTOR)
        ir.Prim.Unit.name
      else
        kindName(tpe.resultType)

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
