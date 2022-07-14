package scala.scalanative
package nir

sealed abstract class Conv {
  final def show: String = nir.Show(this)
}

object Conv {
  case object ZSizeCast extends Conv
  case object SSizeCast extends Conv
  case object Trunc extends Conv
  case object Zext extends Conv
  case object Sext extends Conv
  case object Fptrunc extends Conv
  case object Fpext extends Conv
  case object Fptoui extends Conv
  case object Fptosi extends Conv
  case object Uitofp extends Conv
  case object Sitofp extends Conv
  case object Ptrtoint extends Conv
  case object Inttoptr extends Conv
  case object Bitcast extends Conv
}
