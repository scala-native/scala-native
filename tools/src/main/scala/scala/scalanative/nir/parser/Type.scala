package scala.scalanative
package nir
package parser

import fastparse.all._

object Type extends Base[nir.Type] {

  import Base._
  import IgnoreWhitespace._

  val None   = P("none".! map (_ => nir.Type.None))
  val Void   = P("void".! map (_ => nir.Type.Void))
  val Vararg = P("...".! map (_ => nir.Type.Vararg))
  val Ptr    = P("ptr".! map (_ => nir.Type.Ptr))
  val Bool   = P("bool".! map (_ => nir.Type.Bool))
  val I8     = P("i8".! map (_ => nir.Type.I8))
  val I16    = P("i16".! map (_ => nir.Type.I16))
  val I32    = P("i32".! map (_ => nir.Type.I32))
  val I64    = P("i64".! map (_ => nir.Type.I64))
  val F32    = P("f32".! map (_ => nir.Type.F32))
  val F64    = P("f64".! map (_ => nir.Type.F64))
  val Array =
    P("[" ~ Type.parser ~ "x" ~ int ~ "]" map {
      case (ty, n) => nir.Type.Array(ty, n)
    })
  val Function =
    P("(" ~ Type.parser.rep(sep = ",") ~ ")" ~ "=>" ~ Type.parser map {
      case (args, ret) => nir.Type.Function(args, ret)
    })
  val NoneStruct =
    P(
      "{" ~ Type.parser.rep(sep = ",") ~ "}" map (nir.Type
        .Struct(nir.Global.None, _)))
  val Struct  = P("struct" ~ Global.parser map (nir.Type.Struct(_, Nil)))
  val Unit    = P("unit".! map (_ => nir.Type.Unit))
  val Nothing = P("nothing".! map (_ => nir.Type.Nothing))
  val Class   = P("class" ~ Global.parser map (nir.Type.Class(_)))
  val Trait   = P("trait" ~ Global.parser map (nir.Type.Trait(_)))
  val Module  = P("module" ~ Global.parser map (nir.Type.Module(_)))

  override val parser: P[nir.Type] =
    None | Void | Vararg | Ptr | Bool | I8 | I16 | I32 | I64 | F32 | F64 | Array | Function | NoneStruct | Struct | Unit | Nothing | Class | Trait | Module
}
