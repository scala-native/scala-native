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
  val UByte  = P("ubyte".! map (_ => nir.Type.UByte))
  val UShort = P("ushort".! map (_ => nir.Type.UShort))
  val UInt   = P("uint".! map (_ => nir.Type.UInt))
  val ULong  = P("ulong".! map (_ => nir.Type.ULong))
  val Byte   = P("byte".! map (_ => nir.Type.Byte))
  val Short  = P("short".! map (_ => nir.Type.Short))
  val Int    = P("int".! map (_ => nir.Type.Int))
  val Long   = P("long".! map (_ => nir.Type.Long))
  val Float  = P("float".! map (_ => nir.Type.Float))
  val Double = P("double".! map (_ => nir.Type.Double))
  val ArrayValue =
    P("[" ~ Type.parser ~ "x" ~ int ~ "]" map {
      case (ty, n) => nir.Type.ArrayValue(ty, n)
    })
  val Function =
    P("(" ~ Type.parser.rep(sep = ",") ~ ")" ~ "=>" ~ Type.parser map {
      case (args, ret) => nir.Type.Function(args, ret)
    })
  val StructValue =
    P("{" ~ Type.parser.rep(sep = ",") ~ "}" map (nir.Type.StructValue(_)))
  val Nothing = P("nothing".! map (_ => nir.Type.Nothing))
  val Var     = P("var[" ~ Type.parser ~ "]" map (nir.Type.Var(_)))
  val Unit    = P("unit".! map (_ => nir.Type.Unit))
  val Array   = P("array[" ~ Type.parser ~ "]" map (nir.Type.Array(_)))
  val Ref     = Global.parser.map(nir.Type.Ref(_))

  override val parser: P[nir.Type] =
    None | Void | Vararg | Ptr | Bool | UByte | UShort | UInt | ULong | Byte | Short | Int | Long | Float | Double | ArrayValue | Function | StructValue | Nothing | Var | Unit | Array | Ref
}
