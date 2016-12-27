package scala.scalanative
package nir
package parser

import fastparse.all._

object Val extends Base[nir.Val] {

  import Base._
  import IgnoreWhitespace._

  val None  = P("none".! map (_ => nir.Val.None))
  val True  = P("true".! map (_ => nir.Val.True))
  val False = P("false".! map (_ => nir.Val.False))
  val Zero  = P("zero[" ~ Type.parser ~ "]" map (nir.Val.Zero(_)))
  val Undef = P("undef[" ~ Type.parser ~ "]" map (nir.Val.Undef(_)))
  val I8    = P(Base.I8 ~ "i8" map (nir.Val.I8(_)))
  val I16   = P(Base.I16 ~ "i16" map (nir.Val.I16(_)))
  val I32   = P(Base.I32 ~ "i32" map (nir.Val.I32(_)))
  val I64   = P(Base.I64 ~ "i64" map (nir.Val.I64(_)))
  val F32   = P(Base.F32 ~ "32" map (nir.Val.F32(_)))
  val F64   = P(Base.F64 ~ "64" map (nir.Val.F64(_)))
  val NoneStruct =
    P(
      "struct" ~ "{" ~ Val.parser.rep(sep = ",") ~ "}" map (nir.Val
        .Struct(nir.Global.None, _)))
  val Struct =
    P(
      "struct" ~ nir.parser.Global.parser ~ "{" ~ Val.parser
        .rep(sep = ",") ~ "}" map {
        case (n, values) => nir.Val.Struct(n, values)
      })
  val Array =
    P("array" ~ Type.parser ~ "{" ~ Val.parser.rep(sep = ",") ~ "}" map {
      case (ty, values) => nir.Val.Array(ty, values)
    })
  val Chars = P("c" ~ stringLit map (nir.Val.Chars(_)))
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
    None | True | False | Zero | Undef | I64 | I32 | I16 | I8 | F64 | F32 | NoneStruct | Struct | Array | Chars | Local | Global | Unit | Const | String

}
