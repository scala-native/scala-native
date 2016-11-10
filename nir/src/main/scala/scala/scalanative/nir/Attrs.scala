package scala.scalanative
package nir

import scala.collection.mutable
import nir.Attr._

sealed abstract class Attr
object Attr {
  sealed abstract class Inline   extends Attr
  final case object MayInline    extends Inline // no information
  final case object InlineHint   extends Inline // user hinted at inlining
  final case object NoInline     extends Inline // should never inline
  final case object AlwaysInline extends Inline // should always inline

  final case object StructDisp   extends Attr //attr for defs that can be used for structural dispatch

  final case object Pure                  extends Attr
  final case object Extern                extends Attr
  final case class Override(name: Global) extends Attr

  // Linker attributes
  final case class Link(name: String)               extends Attr
  sealed abstract class Pin                         extends Attr
  final case class PinAlways(dep: Global)           extends Pin
  final case class PinIf(dep: Global, cond: Global) extends Pin
  final case class WeakPin(dep: Global)             extends Pin
}

final case class Attrs(inline: Inline = MayInline,
                       isPure: Boolean = false,
                       isExtern: Boolean = false,
                       usedForStructDisp: Boolean = false,
                       overrides: Seq[Global] = Seq(),
                       pins: Seq[Pin] = Seq(),
                       links: Seq[Attr.Link] = Seq()) {
  def toSeq: Seq[Attr] = {
    val out = mutable.UnrolledBuffer.empty[Attr]

    if (inline != MayInline) out += inline
    if (isPure) out += Pure
    if (isExtern) out += Extern
    if (usedForStructDisp) out += StructDisp
    overrides.foreach { out += Override(_) }
    out ++= pins
    out ++= links

    out
  }
}
object Attrs {
  val None = new Attrs()

  def fromSeq(attrs: Seq[Attr]) = {
    var inline    = None.inline
    var isPure    = false
    var isExtern  = false
    var usedForStructDisp = false
    val overrides = mutable.UnrolledBuffer.empty[Global]
    val pins      = mutable.UnrolledBuffer.empty[Pin]
    val links     = mutable.UnrolledBuffer.empty[Attr.Link]

    attrs.foreach {
      case attr: Inline    => inline = attr
      case Pure            => isPure = true
      case Extern          => isExtern = true
      case Override(name)  => overrides += name
      case attr: Pin       => pins += attr
      case link: Attr.Link => links += link
      case StructDisp      => usedForStructDisp = true
    }

    new Attrs(inline, isPure, isExtern, usedForStructDisp, overrides, pins, links)
  }
}
