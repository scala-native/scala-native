package scala.scalanative
package nir

sealed abstract class PassConv {
  final def show: String = nir.Show(this)
}

object PassConv {
  final case class Byval(ty: Type) extends PassConv
  final case class Sret(ty: Type)  extends PassConv
}
