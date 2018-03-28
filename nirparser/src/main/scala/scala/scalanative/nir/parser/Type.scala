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
  val Byte   = P("byte".! map (_ => nir.Type.Byte))
  val Short  = P("short".! map (_ => nir.Type.Short))
  val Int    = P("int".! map (_ => nir.Type.Int))
  val Long   = P("long".! map (_ => nir.Type.Long))
  val Float  = P("float".! map (_ => nir.Type.Float))
  val Double = P("double".! map (_ => nir.Type.Double))
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
    None | Void | Vararg | Ptr | Bool | Byte | Short | Int | Long | Float | Double | Array | Function | NoneStruct | Struct | Unit | Nothing | Class | Trait | Module
}
