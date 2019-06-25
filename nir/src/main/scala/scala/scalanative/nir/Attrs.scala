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

  sealed abstract class Specialize extends Attr
  final case object MaySpecialize  extends Specialize
  final case object NoSpecialize   extends Specialize

  sealed abstract class Opt             extends Attr
  final case object UnOpt               extends Opt
  final case object NoOpt               extends Opt
  final case object DidOpt              extends Opt
  final case class BailOpt(msg: String) extends Opt

  final case object Dyn               extends Attr
  final case object Stub              extends Attr
  final case object Extern            extends Attr
  final case class Link(name: String) extends Attr
  final case object Abstract          extends Attr
}

final case class Attrs(inline: Inline = MayInline,
                       specialize: Specialize = MaySpecialize,
                       opt: Opt = UnOpt,
                       isExtern: Boolean = false,
                       isDyn: Boolean = false,
                       isStub: Boolean = false,
                       isAbstract: Boolean = false,
                       links: Seq[Attr.Link] = Seq()) {
  def toSeq: Seq[Attr] = {
    val out = mutable.UnrolledBuffer.empty[Attr]

    if (inline != MayInline) out += inline
    if (specialize != MaySpecialize) out += specialize
    if (opt != UnOpt) out += opt
    if (isExtern) out += Extern
    if (isDyn) out += Dyn
    if (isStub) out += Stub
    if (isAbstract) out += Abstract
    out ++= links

    out
  }
}
object Attrs {
  val None = new Attrs()

  def fromSeq(attrs: Seq[Attr]) = {
    var inline     = None.inline
    var specialize = None.specialize
    var opt        = None.opt
    var isExtern   = false
    var isDyn      = false
    var isStub     = false
    var isAbstract = false
    val overrides  = mutable.UnrolledBuffer.empty[Global]
    val links      = mutable.UnrolledBuffer.empty[Attr.Link]

    attrs.foreach {
      case attr: Inline     => inline = attr
      case attr: Specialize => specialize = attr
      case attr: Opt        => opt = attr
      case Extern           => isExtern = true
      case Dyn              => isDyn = true
      case Stub             => isStub = true
      case link: Attr.Link  => links += link
      case Abstract         => isAbstract = true
    }

    new Attrs(inline,
              specialize,
              opt,
              isExtern,
              isDyn,
              isStub,
              isAbstract,
              links)
  }
}
