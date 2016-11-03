package scala.scalanative
package nir
package parser

import fastparse.all._

object Bin extends Base[nir.Bin] {

  val Iadd = P("iadd".! map (_ => nir.Bin.Iadd))
  val Fadd = P("fadd".! map (_ => nir.Bin.Fadd))
  val Isub = P("isub".! map (_ => nir.Bin.Isub))
  val Fsub = P("fsub".! map (_ => nir.Bin.Fsub))
  val Imul = P("imul".! map (_ => nir.Bin.Imul))
  val Fmul = P("fmul".! map (_ => nir.Bin.Fmul))
  val Sdiv = P("sdiv".! map (_ => nir.Bin.Sdiv))
  val Udiv = P("udiv".! map (_ => nir.Bin.Udiv))
  val Fdiv = P("fdiv".! map (_ => nir.Bin.Fdiv))
  val Srem = P("srem".! map (_ => nir.Bin.Srem))
  val Urem = P("urem".! map (_ => nir.Bin.Urem))
  val Frem = P("frem".! map (_ => nir.Bin.Frem))
  val Shl  = P("shl".! map (_ => nir.Bin.Shl))
  val Lshr = P("lshr".! map (_ => nir.Bin.Lshr))
  val Ashr = P("ashr".! map (_ => nir.Bin.Ashr))
  val And  = P("and".! map (_ => nir.Bin.And))
  val Or   = P("or".! map (_ => nir.Bin.Or))
  val Xor  = P("xor".! map (_ => nir.Bin.Xor))
  override val parser: P[nir.Bin] =
    Iadd | Fadd | Isub | Fsub | Imul | Fmul | Sdiv | Udiv | Fdiv | Srem | Urem | Frem | Shl | Lshr | Ashr | And | Or | Xor
}
