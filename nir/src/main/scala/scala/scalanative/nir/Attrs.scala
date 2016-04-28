package scala.scalanative
package nir

sealed abstract class Attr
object Attr {
  sealed abstract class Inline extends Attr
  final case object InlineHint extends Inline
  final case object NoInline   extends Inline
  final case object MustInline extends Inline

  sealed abstract class Link extends Attr
  final case object Private             extends Link
  final case object Internal            extends Link
  final case object AvailableExternally extends Link
  final case object LinkOnce            extends Link
  final case object Weak                extends Link
  final case object Common              extends Link
  final case object Appending           extends Link
  final case object ExternWeak          extends Link
  final case object LinkOnceODR         extends Link
  final case object WeakODR             extends Link
  final case object External            extends Link

  final case class Override(name: Global)           extends Attr
  final case class Pin(dep: Global)                 extends Attr
  final case class PinIf(dep: Global, cond: Global) extends Attr
}
