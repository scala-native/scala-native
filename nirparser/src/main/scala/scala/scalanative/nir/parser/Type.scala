package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._

object Type extends Base[nir.Type] {

  import Base._
  //import IgnoreWhitespace._

  def Vararg[_: P] = P("...".!.map(_ => nir.Type.Vararg))
  def Ptr[_: P]    = P("ptr".!.map(_ => nir.Type.Ptr))
  def Bool[_: P]   = P("bool".!.map(_ => nir.Type.Bool))
  def Byte[_: P]   = P("byte".!.map(_ => nir.Type.Byte))
  def Short[_: P]  = P("short".!.map(_ => nir.Type.Short))
  def Int[_: P]    = P("int".!.map(_ => nir.Type.Int))
  def Long[_: P]   = P("long".!.map(_ => nir.Type.Long))
  def Float[_: P]  = P("float".!.map(_ => nir.Type.Float))
  def Double[_: P] = P("double".!.map(_ => nir.Type.Double))
  def ArrayValue[_: P] =
    P("[" ~ Type.parser ~ "x" ~ int ~ "]" map {
      case (ty, n) => nir.Type.ArrayValue(ty, n)
    })
  def Function[_: P] =
    P("(" ~ Type.parser.rep(sep = ",") ~ ")" ~ "=>" ~ Type.parser map {
      case (args, ret) => nir.Type.Function(args, ret)
    })
  def StructValue[_: P] =
    P("{" ~ Type.parser.rep(sep = ",") ~ "}" map (nir.Type.StructValue(_)))
  def Nothing[_: P] = P("nothing".!.map(_ => nir.Type.Nothing))
  def Var[_: P]     = P("var[" ~ Type.parser ~ "]" map (nir.Type.Var(_)))
  def Unit[_: P]    = P("unit".!.map(_ => nir.Type.Unit))
  def Array[_: P]   = P("array[" ~ Type.parser ~ "]" map (nir.Type.Array(_)))
  def Ref[_: P]     = Global.parser.map(nir.Type.Ref(_))

  override def parser[_: P]: P[nir.Type] =
    Vararg | Ptr | Bool | Byte | Short | Int | Long | Float | Double | ArrayValue | Function | StructValue | Nothing | Var | Unit | Array | Ref
}
