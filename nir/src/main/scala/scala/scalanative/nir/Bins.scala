package scala.scalanative
package nir

sealed abstract class Bin {
  final def show: String = nir.Show(this)
}

object Bin {
  case object Iadd extends Bin
  case object Fadd extends Bin
  case object Isub extends Bin
  case object Fsub extends Bin
  case object Imul extends Bin
  case object Fmul extends Bin
  case object Sdiv extends Bin
  case object Udiv extends Bin
  case object Fdiv extends Bin
  case object Srem extends Bin
  case object Urem extends Bin
  case object Frem extends Bin
  case object Shl extends Bin
  case object Lshr extends Bin
  case object Ashr extends Bin
  case object And extends Bin
  case object Or extends Bin
  case object Xor extends Bin
}
