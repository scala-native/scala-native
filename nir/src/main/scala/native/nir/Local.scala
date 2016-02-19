package native
package nir

final case class Local(scope: String, id: Int) {
  override def toString = (scope, id) match {
    case ("", -1) => "Local.None"
    case _        => s"Local($scope, $id)"
  }
}
object Local {
  val None = Local("", -1)
}
