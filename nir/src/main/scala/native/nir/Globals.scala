package native
package nir

sealed abstract class Global
object Global {
  final case class Atom(id: String)                      extends Global
  final case class Nested(owner: Global, member: Global) extends Global
  final case class Tagged(name: Global, tag: Global)     extends Global
}



