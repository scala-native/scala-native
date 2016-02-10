package native
package nir
package plugin

import scala.tools.nsc._

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global.{Name => _, _}, definitions._

  def genClassName(sym: Symbol): nir.Global = {
    val id = sym.fullName.toString

    sym match {
      case IntrinsicClass(n)                         => n
      case _ if sym.isModule                         => genClassName(sym.moduleClass)
      case _ if sym.isModuleClass || sym.isImplClass => nir.Global(id, "m")
      case _ if sym.isInterface                      => nir.Global(id, "i")
      case _                                         => nir.Global(id, "c")
    }
  }

  def genFieldName(sym: Symbol) = {
    val owner = genClassName(sym.owner)
    val id0 = sym.name.toString
    val id =
      if (id0.charAt(id0.length()-1) != ' ') id0
      else id0.substring(0, id0.length()-1)

    owner + id
  }

  def genDefName(sym: Symbol) = {
    val owner  = genClassName(sym.owner)
    val id     = sym.name.toString
    val tpe    = sym.tpe.widen
    val params = tpe.params.toSeq.flatMap(kindTag)

    if (sym.name == nme.CONSTRUCTOR) {
      owner + "init" ++ params
    } else {
      val res = kindTag(tpe.resultType)
      owner + id ++ params ++ res
    }
  }

  private def kindTag(sym: Symbol): Seq[String] =
    kindTag(genKind(sym.tpe))

  private def kindTag(tpe: Type): Seq[String] =
    kindTag(genKind(tpe.widen))

  private def kindTag(kind: Kind): Seq[String] = {
    val ty = toIRType(kind)
    kind match {
      case PrimitiveKind(sym) =>
        Seq(sym match {
          case UnitClass    => "unit"
          case BooleanClass => "bool"
          case ByteClass    => "i8"
          case CharClass    => "i16"
          case ShortClass   => "i16"
          case IntClass     => "i32"
          case LongClass    => "i64"
          case FloatClass   => "f32"
          case DoubleClass  => "f64"
        })
      case BottomKind(sym) =>
        Seq(sym match {
          case NullClass    => "null"
          case NothingClass => "nothing"
        })
      case BuiltinClassKind(sym) =>
        Seq(sym match {
          case ObjectClass         => "object"
          case ClassClass          => "class"
          case StringClass         => "string"
          case BoxedCharacterClass => "character"
          case BoxedBooleanClass   => "boolean"
          case BoxedByteClass      => "byte"
          case BoxedShortClass     => "short"
          case BoxedIntClass       => "integer"
          case BoxedLongClass      => "long"
          case BoxedFloatClass     => "float"
          case BoxedDoubleClass    => "double"
        })
      case ClassKind(sym)  => genClassName(sym).parts
      case ArrayKind(kind) => kindTag(kind) :+ "arr"
    }
  }
}
