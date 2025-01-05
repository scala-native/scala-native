package scala.scalanative
package nir

import nir.Attr._

sealed abstract class Attr {
  final def show: String = nir.Show(this)
}

object Attr {
  sealed abstract class Inline extends Attr
  case object MayInline extends Inline // no information
  case object InlineHint extends Inline // user hinted at inlining
  case object NoInline extends Inline // should never inline
  case object AlwaysInline extends Inline // should always inline

  sealed abstract class Specialize extends Attr
  case object MaySpecialize extends Specialize
  case object NoSpecialize extends Specialize

  sealed abstract class Opt extends Attr
  case object UnOpt extends Opt
  case object NoOpt extends Opt
  case object DidOpt extends Opt
  final case class BailOpt(msg: String) extends Opt

  case object Dyn extends Attr
  case object Stub extends Attr
  case class Extern(blocking: Boolean) extends Attr
  final case class Link(name: String) extends Attr
  case object LinkCppRuntime extends Attr
  final case class Define(name: String) extends Attr
  case object Abstract extends Attr
  case object Volatile extends Attr
  case object Final extends Attr
  case object SafePublish extends Attr
  case object LinktimeResolved extends Attr
  case object UsesIntrinsic extends Attr
  case class Alignment(size: Int, group: Option[String]) extends Attr
  object Alignment {
    // Alignment by defintion must be positive integer, magic value treated specially by compiler
    final val linktimeResolved = -1
  }
}

final case class Attrs(
    inlineHint: Inline = MayInline,
    specialize: Specialize = MaySpecialize,
    opt: Opt = UnOpt,
    align: Option[Alignment] = Option.empty,
    isExtern: Boolean = false,
    isBlocking: Boolean = false,
    isDyn: Boolean = false,
    isStub: Boolean = false,
    isAbstract: Boolean = false,
    isVolatile: Boolean = false,
    isFinal: Boolean = false,
    isSafePublish: Boolean = false,
    isLinktimeResolved: Boolean = false,
    isUsingIntrinsics: Boolean = false,
    links: Seq[Attr.Link] = Seq.empty,
    preprocessorDefinitions: Seq[Attr.Define] = Seq.empty,
    linkCppRuntime: Boolean = false
) {
  def finalWithSafePublish: Boolean = isFinal && isSafePublish
  def toSeq: Seq[Attr] = {
    val out = Seq.newBuilder[Attr]

    if (inlineHint != MayInline) out += inlineHint
    if (specialize != MaySpecialize) out += specialize
    if (opt != UnOpt) out += opt
    out ++= align
    if (isExtern) out += Extern(isBlocking)
    if (isDyn) out += Dyn
    if (isStub) out += Stub
    if (linkCppRuntime) out += LinkCppRuntime
    if (isAbstract) out += Abstract
    if (isVolatile) out += Volatile
    if (isFinal) out += Final
    if (isSafePublish) out += SafePublish
    if (isLinktimeResolved) out += LinktimeResolved
    if (isUsingIntrinsics) out += UsesIntrinsic
    out ++= links
    out ++= preprocessorDefinitions

    out.result()
  }
}
object Attrs {
  val None = new Attrs()

  def fromSeq(attrs: Seq[Attr]): Attrs = {
    var inline = None.inlineHint
    var specialize = None.specialize
    var opt = None.opt
    var align = None.align
    var isExtern = false
    var isDyn = false
    var isStub = false
    var linkCppRuntime = false
    var isAbstract = false
    var isBlocking = false
    var isVolatile = false
    var isFinal = false
    var isSafePublish = false
    var isLinktimeResolved = false
    var isUsingIntrinsics = false
    val links = Seq.newBuilder[Attr.Link]
    val preprocessorDefinitions = Seq.newBuilder[Attr.Define]

    attrs.foreach {
      case attr: Inline     => inline = attr
      case attr: Specialize => specialize = attr
      case attr: Opt        => opt = attr
      case attr: Alignment =>
        align = Some(attr)
      case Extern(blocking) =>
        isExtern = true
        isBlocking = blocking
      case Dyn                 => isDyn = true
      case Stub                => isStub = true
      case LinkCppRuntime      => linkCppRuntime = true
      case link: Attr.Link     => links += link
      case define: Attr.Define => preprocessorDefinitions += define
      case Abstract            => isAbstract = true
      case Volatile            => isVolatile = true
      case Final               => isFinal = true
      case SafePublish         => isSafePublish = true

      case LinktimeResolved => isLinktimeResolved = true
      case UsesIntrinsic    => isUsingIntrinsics = true
    }

    new Attrs(
      inlineHint = inline,
      specialize = specialize,
      opt = opt,
      align = align,
      isExtern = isExtern,
      isBlocking = isBlocking,
      isDyn = isDyn,
      isStub = isStub,
      isAbstract = isAbstract,
      isVolatile = isVolatile,
      isFinal = isFinal,
      isSafePublish = isSafePublish,
      isLinktimeResolved = isLinktimeResolved,
      isUsingIntrinsics = isUsingIntrinsics,
      links = links.result(),
      preprocessorDefinitions = preprocessorDefinitions.result(),
      linkCppRuntime = linkCppRuntime
    )
  }
}
