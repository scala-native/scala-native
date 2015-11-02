package native
package plugin

import scala.tools.nsc._
import native.ir, ir.{Name, Defn, Desc}

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global.{Name => _, _}, definitions._

  def genLocalName(sym: Symbol) = Name.Local(sym.name.toString)

  def genFieldDefn(sym: Symbol) = Defn.Extern(genFieldName(sym))
  def genFieldName(sym: Symbol) = {
    val owner = genClassName(sym.owner)
    val id0 = sym.name.toString
    val id =
      if (id0.charAt(id0.length()-1) != ' ') id0
      else id0.substring(0, id0.length()-1)

    Name.Field(owner, id)
  }

  def genClassDefn(sym: Symbol) = Defn.Extern(genClassName(sym))
  def genClassName(sym: Symbol): Name = {
    val id = sym.fullName.toString

    if (sym.isModule)
      genClassName(sym.moduleClass)
    else if (sym.isModuleClass || sym.isImplClass)
      Name.Module(id)
    else if (sym.isInterface)
      Name.Interface(id)
    else
      Name.Class(id)
  }

  def genDefDefn(sym: Symbol) = Defn.Extern(genDefName(sym))
  def genDefName(sym: Symbol) = {
    val owner  = genClassName(sym.owner)
    val id     = sym.name.toString
    val tpe    = sym.tpe.widen
    val params = tpe.params.map(kindName).toSeq

    if (sym.name == nme.CONSTRUCTOR)
      Name.Constructor(owner, params)
    else
      Name.Method(owner, id, params, kindName(tpe.resultType))
  }

  private def kindName(sym: Symbol): ir.Name =
    kindName(genKind(sym.tpe))

  private def kindName(tpe: Type): ir.Name =
    kindName(genKind(tpe.widen))

  private def kindName(kind: Kind): ir.Name = {
    val node = toIRType(kind)
    kind match {
      case PrimitiveKind(_) => node.name
      case BottomKind(_)    => node.name
      case ClassKind(sym)   => genClassName(sym)
      case ArrayKind(kind)  => Name.Slice(kindName(kind))
    }
  }
}
