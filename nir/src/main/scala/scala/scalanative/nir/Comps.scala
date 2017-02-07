package scala.scalanative
package nir

sealed abstract class Comp {
  final def show: String = nir.Show(this)
}

object Comp {
  sealed abstract class Icmp extends Comp
  final case object Ieq      extends Icmp
  final case object Ine      extends Icmp
  final case object Ugt      extends Icmp
  final case object Uge      extends Icmp
  final case object Ult      extends Icmp
  final case object Ule      extends Icmp
  final case object Sgt      extends Icmp
  final case object Sge      extends Icmp
  final case object Slt      extends Icmp
  final case object Sle      extends Icmp

  sealed abstract class Fcmp extends Comp
  final case object Feq      extends Fcmp
  final case object Fne      extends Fcmp
  final case object Fgt      extends Fcmp
  final case object Fge      extends Fcmp
  final case object Flt      extends Fcmp
  final case object Fle      extends Fcmp
}
