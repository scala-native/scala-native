package scala.scalanative
package nir

final case class Local(scope: String, id: Int) {
  override def toString = (scope, id) match {
    case ("", -1) => "Local.None"
    case _        => s"Local($scope, $id)"
  }

  final def isEmpty  = this eq Local.empty
  final def nonEmpty = this ne Local.empty
}
object Local {
  val empty = Local("", -1)
}
