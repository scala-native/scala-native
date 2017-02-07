package scala.scalanative
package nir

sealed abstract class Conv {
  final def show: String = nir.Show(this)
}

object Conv {
  final case object Trunc    extends Conv
  final case object Zext     extends Conv
  final case object Sext     extends Conv
  final case object Fptrunc  extends Conv
  final case object Fpext    extends Conv
  final case object Fptoui   extends Conv
  final case object Fptosi   extends Conv
  final case object Uitofp   extends Conv
  final case object Sitofp   extends Conv
  final case object Ptrtoint extends Conv
  final case object Inttoptr extends Conv
  final case object Bitcast  extends Conv
}
