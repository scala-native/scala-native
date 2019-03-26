package scala.scalanative
package nir

import scala.collection.mutable
import nir.Attr._

sealed abstract class Attr {
  final def show: String = nir.Show(this)
}

object Attr {
  sealed abstract class Inline   extends Attr
  final case object MayInline    extends Inline // no information
  final case object InlineHint   extends Inline // user hinted at inlining
  final case object NoInline     extends Inline // should never inline
  final case object AlwaysInline extends Inline // should always inline

  final case object Dyn               extends Attr
  final case object Stub              extends Attr
  final case object Extern            extends Attr
  final case class Link(name: String) extends Attr
  final case object Abstract          extends Attr

  sealed abstract class Opt             extends Attr
  final case object UnOpt               extends Opt
  final case object DidOpt              extends Opt
  final case class BailOpt(msg: String) extends Opt
}

final case class Attrs(inline: Inline = MayInline,
                       isExtern: Boolean = false,
                       isDyn: Boolean = false,
                       isStub: Boolean = false,
                       isAbstract: Boolean = false,
                       links: Seq[Attr.Link] = Seq(),
                       opt: Opt = UnOpt) {
  def toSeq: Seq[Attr] = {
    val out = mutable.UnrolledBuffer.empty[Attr]

    if (inline != MayInline) out += inline
    if (isExtern) out += Extern
    if (isDyn) out += Dyn
    if (isStub) out += Stub
    if (isAbstract) out += Abstract
    if (opt != UnOpt) out += opt
    out ++= links

    out
  }
}
object Attrs {
  val None = new Attrs()

  def fromSeq(attrs: Seq[Attr]) = {
    var inline     = None.inline
    var isExtern   = false
    var isDyn      = false
    var isStub     = false
    var isAbstract = false
    val overrides  = mutable.UnrolledBuffer.empty[Global]
    val links      = mutable.UnrolledBuffer.empty[Attr.Link]
    var opt: Opt   = UnOpt

    attrs.foreach {
      case attr: Inline    => inline = attr
      case Extern          => isExtern = true
      case Dyn             => isDyn = true
      case Stub            => isStub = true
      case link: Attr.Link => links += link
      case Abstract        => isAbstract = true
      case o: Opt          => opt = o
    }

    new Attrs(inline, isExtern, isDyn, isStub, isAbstract, links, opt)
  }
}
