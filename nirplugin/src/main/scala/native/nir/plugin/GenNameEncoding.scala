package native
package nir
package plugin

import scala.tools.nsc._

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global.{Name => _, _}, definitions._

  def genLocalName(sym: Symbol) =
    Name.Local(sym.name.toString)

  def genFieldName(sym: Symbol) = {
    val owner = genClassName(sym.owner)
    val id0 = sym.name.toString
    val id =
      if (id0.charAt(id0.length()-1) != ' ') id0
      else id0.substring(0, id0.length()-1)

    Name.Nested(owner, Name.Field(id))
  }

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

  def genDefName(sym: Symbol) = {
    val owner  = genClassName(sym.owner)
    val id     = sym.name.toString
    val tpe    = sym.tpe.widen
    val params = tpe.params.map(kindName).toSeq

    if (sym.name == nme.CONSTRUCTOR)
      Name.Nested(owner, Name.Constructor(params))
    else
      Name.Nested(owner, Name.Method(id, params, kindName(tpe.resultType)))
  }

  def genForeignName(sym: Symbol) =
    Name.Nested(genClassName(sym.owner), Name.Foreign(sym.name.toString))

  private def kindName(sym: Symbol): Name =
    kindName(genKind(sym.tpe))

  private def kindName(tpe: Type): Name =
    kindName(genKind(tpe.widen))

  private def kindName(kind: Kind): Name = {
    val ty = toIRType(kind)
    kind match {
      case PrimitiveKind(sym) =>
        sym match {
          case UnitClass    => nir.Name.Prim("unit")
          case BooleanClass => nir.Name.Prim("bool")
          case ByteClass    => nir.Name.Prim("i8")
          case CharClass    => nir.Name.Prim("i16")
          case ShortClass   => nir.Name.Prim("i16")
          case IntClass     => nir.Name.Prim("i32")
          case LongClass    => nir.Name.Prim("i64")
          case FloatClass   => nir.Name.Prim("f32")
          case DoubleClass  => nir.Name.Prim("f64")
          case StringClass  => nir.Name.Prim("string")
        }
      case BottomKind(sym) =>
        sym match {
          case NullClass    => nir.Name.Prim("null")
          case NothingClass => nir.Name.Prim("nothing")
        }
      case ClassKind(sym)   => genClassName(sym)
      case ArrayKind(kind)  => Name.Array(kindName(kind))
    }
  }
}
