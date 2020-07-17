package scala.scalanative
package nir
package parser

import fastparse.all._

object Val extends Base[nir.Val] {

  import Base._
  import IgnoreWhitespace._

  val Char   = P("char" ~ Base.Int map (i => nir.Val.Char(i.toChar)))
  val True   = P("true".! map (_ => nir.Val.True))
  val False  = P("false".! map (_ => nir.Val.False))
  val Null   = P("null".! map (_ => nir.Val.Null))
  val Zero   = P("zero[" ~ Type.parser ~ "]" map (nir.Val.Zero(_)))
  val Byte   = P("byte" ~ Base.Byte map (nir.Val.Byte(_)))
  val Short  = P("short" ~ Base.Short map (nir.Val.Short(_)))
  val Int    = P("int" ~ Base.Int map (nir.Val.Int(_)))
  val Long   = P("long" ~ Base.Long map (nir.Val.Long(_)))
  val Float  = P("float" ~ Base.Float map (nir.Val.Float(_)))
  val Double = P("double" ~ Base.Double map (nir.Val.Double(_)))
  val StructValue =
    P(
      "structvalue" ~ "{" ~ Val.parser.rep(sep = ",") ~ "}" map (nir.Val
        .StructValue(_)))
  val ArrayValue =
    P("arrayvalue" ~ Type.parser ~ "{" ~ Val.parser.rep(sep = ",") ~ "}" map {
      case (ty, values) => nir.Val.ArrayValue(ty, values)
    })
  val Chars = P(
    "c" ~ stringLit map (_.getBytes("UTF-8")) map (nir.Val.Chars(_)))
  val Local =
    P(nir.parser.Local.parser ~ ":" ~ Type.parser map {
      case (name, ty) => nir.Val.Local(name, ty)
    })
  val Global =
    P(nir.parser.Global.parser ~ ":" ~ Type.parser map {
      case (name, valty) => nir.Val.Global(name, valty)
    })
  val Unit   = P("unit".! map (_ => nir.Val.Unit))
  val Const  = P("const" ~ Val.parser map (nir.Val.Const(_)))
  val String = P(stringLit map (nir.Val.String(_)))

  override val parser: P[nir.Val] =
    Char | True | False | Null | Zero | Long | Int | Short | Byte | Double | Float | StructValue | ArrayValue | Chars | Local | Global | Unit | Const | String

}
