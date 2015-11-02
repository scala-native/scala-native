package native
package ir

sealed abstract class Schema
object Schema {
  final case object Val              extends Schema
  final case object Cf               extends Schema
  final case object Ef               extends Schema
  final case object Ref              extends Schema
  final case class  Many(of: Schema) extends Schema
}


