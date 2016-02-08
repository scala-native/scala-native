package native
package nir

sealed abstract class Attr
object Attr {
  // op attributes
  final case object Usgn extends Attr

  // defn attributes
  final case class Inline(advice: Advice) extends Attr
  final case class Overrides(name: Global) extends Attr
}
