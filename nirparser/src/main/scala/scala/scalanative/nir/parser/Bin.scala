package scala.scalanative
package nir
package parser

import fastparse._
import NoWhitespace._

object Bin extends Base[nir.Bin] {

  def Iadd[_: P] = P("iadd".!.map(_ => nir.Bin.Iadd))
  def Fadd[_: P] = P("fadd".!.map(_ => nir.Bin.Fadd))
  def Isub[_: P] = P("isub".!.map(_ => nir.Bin.Isub))
  def Fsub[_: P] = P("fsub".!.map(_ => nir.Bin.Fsub))
  def Imul[_: P] = P("imul".!.map(_ => nir.Bin.Imul))
  def Fmul[_: P] = P("fmul".!.map(_ => nir.Bin.Fmul))
  def Sdiv[_: P] = P("sdiv".!.map(_ => nir.Bin.Sdiv))
  def Udiv[_: P] = P("udiv".!.map(_ => nir.Bin.Udiv))
  def Fdiv[_: P] = P("fdiv".!.map(_ => nir.Bin.Fdiv))
  def Srem[_: P] = P("srem".!.map(_ => nir.Bin.Srem))
  def Urem[_: P] = P("urem".!.map(_ => nir.Bin.Urem))
  def Frem[_: P] = P("frem".!.map(_ => nir.Bin.Frem))
  def Shl[_: P]  = P("shl".!.map(_ => nir.Bin.Shl))
  def Lshr[_: P] = P("lshr".!.map(_ => nir.Bin.Lshr))
  def Ashr[_: P] = P("ashr".!.map(_ => nir.Bin.Ashr))
  def And[_: P]  = P("and".!.map(_ => nir.Bin.And))
  def Or[_: P]   = P("or".!.map(_ => nir.Bin.Or))
  def Xor[_: P]  = P("xor".!.map(_ => nir.Bin.Xor))
  override def parser[_: P]: P[nir.Bin] =
    Iadd | Fadd | Isub | Fsub | Imul | Fmul | Sdiv | Udiv | Fdiv | Srem | Urem | Frem | Shl | Lshr | Ashr | And | Or | Xor
}
