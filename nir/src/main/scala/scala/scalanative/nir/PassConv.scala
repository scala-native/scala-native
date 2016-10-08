package scala.scalanative.nir

sealed abstract class PassConv
object PassConv {
  final case class Byval(ty: Type) extends PassConv
  final case class Sret(ty: Type)  extends PassConv
}
