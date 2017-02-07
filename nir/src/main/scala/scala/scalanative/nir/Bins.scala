package scala.scalanative
package nir

sealed abstract class Bin {
  final def show: String = nir.Show(this)
}

object Bin {
  final case object Iadd extends Bin
  final case object Fadd extends Bin
  final case object Isub extends Bin
  final case object Fsub extends Bin
  final case object Imul extends Bin
  final case object Fmul extends Bin
  final case object Sdiv extends Bin
  final case object Udiv extends Bin
  final case object Fdiv extends Bin
  final case object Srem extends Bin
  final case object Urem extends Bin
  final case object Frem extends Bin
  final case object Shl  extends Bin
  final case object Lshr extends Bin
  final case object Ashr extends Bin
  final case object And  extends Bin
  final case object Or   extends Bin
  final case object Xor  extends Bin
}
