package native
package nir

import Shows._
import native.util.sh

sealed abstract class Global {
  def stringValue: Val.String =
    Val.String(sh"$this".toString)
}
object Global {
  final case class Atom(id: String)                      extends Global
  final case class Nested(owner: Global, member: Global) extends Global
  final case class Tagged(name: Global, tag: Global)     extends Global
}



