package native
package nir

sealed abstract class Attr
object Attr {
  final case class Inline(advice: Advice) extends Attr
  final case class Override(name: Global) extends Attr
}
