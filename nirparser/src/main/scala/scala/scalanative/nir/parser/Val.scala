package scala.scalanative
package nir
package parser

import fastparse._

object Val extends Base[nir.Val] {

  import Base.stringLit
  import MultiLineWhitespace._
  //import IgnoreWhitespace._

  def Char[_: P]   = P("char" ~ Base.Int.map(i => nir.Val.Char(i.toChar)))
  def True[_: P]   = P("true".!.map(_ => nir.Val.True))
  def False[_: P]  = P("false".!.map(_ => nir.Val.False))
  def Null[_: P]   = P("null".!.map(_ => nir.Val.Null))
  def Zero[_: P]   = P("zero[" ~ Type.parser ~ "]" map (nir.Val.Zero(_)))
  def Byte[_: P]   = P("byte" ~ Base.Byte.map(nir.Val.Byte(_)))
  def Short[_: P]  = P("short" ~ Base.Short.map(nir.Val.Short(_)))
  def Int[_: P]    = P("int" ~ Base.Int.map(nir.Val.Int(_)))
  def Long[_: P]   = P("long" ~ Base.Long.map(nir.Val.Long(_)))
  def Float[_: P]  = P("float" ~ Base.Float.map(nir.Val.Float(_)))
  def Double[_: P] = P("double" ~ Base.Double.map(nir.Val.Double(_)))
  def StructValue[_: P] =
    P(
      "structvalue" ~ "{" ~ Val.parser.rep(sep = ",") ~ "}" map (nir.Val
        .StructValue(_)))
  def ArrayValue[_: P] =
    P("arrayvalue" ~ Type.parser ~ "{" ~ Val.parser.rep(sep = ",") ~ "}" map {
      case (ty, values) => nir.Val.ArrayValue(ty, values)
    })
  def Chars[_: P] = P("c" ~ stringLit.map(nir.Val.Chars(_)))
  def Local[_: P] =
    P(nir.parser.Local.parser ~ ":" ~ Type.parser map {
      case (name, ty) => nir.Val.Local(name, ty)
    })
  def Global[_: P] =
    P(nir.parser.Global.parser ~ ":" ~ Type.parser map {
      case (name, valty) => nir.Val.Global(name, valty)
    })
  def Unit[_: P]   = P("unit".!.map(_ => nir.Val.Unit))
  def Const[_: P]  = P("const" ~ Val.parser.map(nir.Val.Const(_)))
  def String[_: P] = P(stringLit.map(nir.Val.String(_)))

  override def parser[_: P]: P[nir.Val] =
    Char | True | False | Null | Zero | Long | Int | Short | Byte | Double | Float | StructValue | ArrayValue | Chars | Local | Global | Unit | Const | String

}
