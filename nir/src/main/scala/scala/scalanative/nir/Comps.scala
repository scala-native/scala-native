package scala.scalanative
package nir

sealed abstract class Comp {
  final def show: String = nir.Show(this)
}

object Comp {
  sealed abstract class Icmp extends Comp
  case object Ieq extends Icmp
  case object Ine extends Icmp
  case object Ugt extends Icmp
  case object Uge extends Icmp
  case object Ult extends Icmp
  case object Ule extends Icmp
  case object Sgt extends Icmp
  case object Sge extends Icmp
  case object Slt extends Icmp
  case object Sle extends Icmp

  sealed abstract class Fcmp extends Comp
  case object Feq extends Fcmp
  case object Fne extends Fcmp
  case object Fgt extends Fcmp
  case object Fge extends Fcmp
  case object Flt extends Fcmp
  case object Fle extends Fcmp
}
