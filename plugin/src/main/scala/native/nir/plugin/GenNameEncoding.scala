package native
package nir
package plugin

import scala.tools.nsc._

trait GenNameEncoding extends SubComponent with GenTypeKinds {
  import global.{Name => _, _}, definitions._

  private val m   = nir.Global.Atom("m")
  private val i   = nir.Global.Atom("i")
  private val c   = nir.Global.Atom("c")
  private val arr = nir.Global.Atom("arr")

  def genLocalName(sym: Symbol) =
    nir.Global.Atom(sym.name.toString)

  def genFieldName(sym: Symbol) = {
    val owner = genClassName(sym.owner)
    val id0 = sym.name.toString
    val id =
      if (id0.charAt(id0.length()-1) != ' ') id0
      else id0.substring(0, id0.length()-1)

    nir.Global.Nested(owner, nir.Global.Atom(id))
  }

  def genClassName(sym: Symbol): nir.Global = {
    val id = sym.fullName.toString

    if (sym.isModule)
      genClassName(sym.moduleClass)
    else if (sym.isModuleClass || sym.isImplClass)
      nir.Global.Tagged(nir.Global.Atom(id), m)
    else if (sym.isInterface)
      nir.Global.Tagged(nir.Global.Atom(id), i)
    else
      nir.Global.Tagged(nir.Global.Atom(id), c)
  }

  def genDefName(sym: Symbol) = {
    val owner  = genClassName(sym.owner)
    val id     = sym.name.toString
    val tpe    = sym.tpe.widen
    val params = tpe.params.map(kindName).toSeq

    if (sym.name == nme.CONSTRUCTOR) {
      val name = params.foldLeft[nir.Global](nir.Global.Atom("init")) {
        nir.Global.Tagged(_, _)
      }
      nir.Global.Nested(owner, name)
    } else {
      val res = kindName(tpe.resultType)
      val name = (params :+ res).foldLeft[nir.Global](nir.Global.Atom(id)) {
        nir.Global.Tagged(_, _)
      }
      nir.Global.Nested(owner, name)
    }
  }

  def genForeignName(sym: Symbol) =
    nir.Global.Nested(genClassName(sym.owner), nir.Global.Atom(sym.name.toString))

  private def kindName(sym: Symbol): nir.Global =
    kindName(genKind(sym.tpe))

  private def kindName(tpe: Type): nir.Global =
    kindName(genKind(tpe.widen))

  private def kindName(kind: Kind): nir.Global = {
    val ty = toIRType(kind)
    kind match {
      case PrimitiveKind(sym) =>
        sym match {
          case UnitClass    => nir.Global.Atom("unit")
          case BooleanClass => nir.Global.Atom("bool")
          case ByteClass    => nir.Global.Atom("i8")
          case CharClass    => nir.Global.Atom("i16")
          case ShortClass   => nir.Global.Atom("i16")
          case IntClass     => nir.Global.Atom("i32")
          case LongClass    => nir.Global.Atom("i64")
          case FloatClass   => nir.Global.Atom("f32")
          case DoubleClass  => nir.Global.Atom("f64")
        }
      case BottomKind(sym) =>
        sym match {
          case NullClass    => nir.Global.Atom("null")
          case NothingClass => nir.Global.Atom("nothing")
        }
      case BuiltinClassKind(sym) =>
        sym match {
          case ObjectClass         => nir.Global.Atom("object")
          case ClassClass          => nir.Global.Atom("class")
          case StringClass         => nir.Global.Atom("string")
          case BoxedCharacterClass => nir.Global.Atom("character")
          case BoxedBooleanClass   => nir.Global.Atom("boolean")
          case BoxedByteClass      => nir.Global.Atom("byte")
          case BoxedShortClass     => nir.Global.Atom("short")
          case BoxedIntClass       => nir.Global.Atom("integer")
          case BoxedLongClass      => nir.Global.Atom("long")
          case BoxedFloatClass     => nir.Global.Atom("float")
          case BoxedDoubleClass    => nir.Global.Atom("double")
        }
      case ClassKind(sym)   => genClassName(sym)
      case ArrayKind(kind)  => nir.Global.Tagged(kindName(kind), arr)
    }
  }
}
