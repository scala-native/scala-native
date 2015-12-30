package native
package nir

sealed abstract class Attr
object Attr {
  // op attributes
  final case object Usgn extends Attr

  // defn attributes
  final case object NoInline extends Attr
  final case object AlwaysInline extends Attr
  final case object InlineHint extends Attr
  final case object Final extends Attr
}
