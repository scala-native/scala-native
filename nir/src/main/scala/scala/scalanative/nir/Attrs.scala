package scala.scalanative
package nir

import nir.Attr.*

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

final class Attrs private (
    val inlineHint: Inline,
    val specialize: Specialize,
    val opt: Opt,
    val align: Option[Alignment],
    val isExtern: Boolean,
    val isBlocking: Boolean,
    val isDyn: Boolean,
    val isStub: Boolean,
    val isAbstract: Boolean,
    val isVolatile: Boolean,
    val isFinal: Boolean,
    val isSafePublish: Boolean,
    val isLinktimeResolved: Boolean,
    val isUsingIntrinsics: Boolean,
    val links: Seq[Attr.Link],
    val preprocessorDefinitions: Seq[Attr.Define],
    val linkCppRuntime: Boolean
    // update equals, hashCode, toString, productArity and producteElement when adding new fields
) extends scala.Product
    with scala.Serializable {
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

  // TODO: rename to copy when deleting the deprecated `copy` method
  private def myCopy(
      inlineHint: Inline = inlineHint,
      specialize: Specialize = specialize,
      opt: Opt = opt,
      align: Option[Alignment] = align,
      isExtern: Boolean = isExtern,
      isBlocking: Boolean = isBlocking,
      isDyn: Boolean = isDyn,
      isStub: Boolean = isStub,
      isAbstract: Boolean = isAbstract,
      isVolatile: Boolean = isVolatile,
      isFinal: Boolean = isFinal,
      isSafePublish: Boolean = isSafePublish,
      isLinktimeResolved: Boolean = isLinktimeResolved,
      isUsingIntrinsics: Boolean = isUsingIntrinsics,
      links: Seq[Attr.Link] = links,
      preprocessorDefinitions: Seq[Attr.Define] = preprocessorDefinitions,
      linkCppRuntime: Boolean = linkCppRuntime
  ): Attrs =
    new Attrs(
      inlineHint = inlineHint,
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
      links = links,
      preprocessorDefinitions = preprocessorDefinitions,
      linkCppRuntime = linkCppRuntime
    )

  def withInlineHint(value: Inline): Attrs =
    myCopy(inlineHint = value)
  def withSpecialize(value: Specialize): Attrs =
    myCopy(specialize = value)
  def withOpt(value: Opt): Attrs =
    myCopy(opt = value)
  def withAlign(value: Option[Alignment]): Attrs =
    myCopy(align = value)
  def withIsExtern(value: Boolean): Attrs =
    myCopy(isExtern = value)
  def withIsBlocking(value: Boolean): Attrs =
    myCopy(isBlocking = value)
  def withIsDyn(value: Boolean): Attrs =
    myCopy(isDyn = value)
  def withIsStub(value: Boolean): Attrs =
    myCopy(isStub = value)
  def withIsAbstract(value: Boolean): Attrs =
    myCopy(isAbstract = value)
  def withIsVolatile(value: Boolean): Attrs =
    myCopy(isVolatile = value)
  def withIsFinal(value: Boolean): Attrs =
    myCopy(isFinal = value)
  def withIsSafePublish(value: Boolean): Attrs =
    myCopy(isSafePublish = value)
  def withIsLinktimeResolved(value: Boolean): Attrs =
    myCopy(isLinktimeResolved = value)
  def withIsUsingIntrinsics(value: Boolean): Attrs =
    myCopy(isUsingIntrinsics = value)
  def withLinks(value: Seq[Attr.Link]): Attrs =
    myCopy(links = value)
  def withPreprocessorDefinitions(value: Seq[Attr.Define]): Attrs =
    myCopy(preprocessorDefinitions = value)
  def withLinkCppRuntime(value: Boolean): Attrs =
    myCopy(linkCppRuntime = value)

  override def equals(that: Any): Boolean = that match {
    case that: Attrs =>
      this.inlineHint == that.inlineHint &&
        this.specialize == that.specialize &&
        this.opt == that.opt &&
        this.align == that.align &&
        this.isExtern == that.isExtern &&
        this.isBlocking == that.isBlocking &&
        this.isDyn == that.isDyn &&
        this.isStub == that.isStub &&
        this.isAbstract == that.isAbstract &&
        this.isVolatile == that.isVolatile &&
        this.isFinal == that.isFinal &&
        this.isSafePublish == that.isSafePublish &&
        this.isLinktimeResolved == that.isLinktimeResolved &&
        this.isUsingIntrinsics == that.isUsingIntrinsics &&
        this.links == that.links &&
        this.preprocessorDefinitions == that.preprocessorDefinitions &&
        this.linkCppRuntime == that.linkCppRuntime
    case _ => false
  }

  override def toString(): String =
    s"""Attrs(
       |  inlineHint = $inlineHint,
       |  specialize = $specialize,
       |  opt = $opt,
       |  align = $align,
       |  isExtern = $isExtern,
       |  isBlocking = $isBlocking,
       |  isDyn = $isDyn,
       |  isStub = $isStub,
       |  isAbstract = $isAbstract,
       |  isVolatile = $isVolatile,
       |  isFinal = $isFinal,
       |  isSafePublish = $isSafePublish,
       |  isLinktimeResolved = $isLinktimeResolved,
       |  isUsingIntrinsics = $isUsingIntrinsics,
       |  links = $links,
       |  preprocessorDefinitions = $preprocessorDefinitions,
       |  linkCppRuntime = $linkCppRuntime
       |)""".stripMargin

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3.*
    var acc = Attrs.HashSeed
    acc = mix(acc, inlineHint.##)
    acc = mix(acc, specialize.##)
    acc = mix(acc, opt.##)
    acc = mix(acc, align.##)
    acc = mix(acc, isExtern.##)
    acc = mix(acc, isBlocking.##)
    acc = mix(acc, isDyn.##)
    acc = mix(acc, isStub.##)
    acc = mix(acc, isAbstract.##)
    acc = mix(acc, isVolatile.##)
    acc = mix(acc, isFinal.##)
    acc = mix(acc, isSafePublish.##)
    acc = mix(acc, isLinktimeResolved.##)
    acc = mix(acc, isUsingIntrinsics.##)
    acc = mix(acc, links.##)
    acc = mix(acc, preprocessorDefinitions.##)
    acc = mixLast(acc, linkCppRuntime.##)
    finalizeHash(acc, 17)
  }

  @deprecated("Use nir.Attrs.None.withXYZ() instead", since = "0.5.7")
  def this(
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
      preprocessorDefinitions: Seq[Attr.Define] = Seq.empty
  ) = this(
    inlineHint = inlineHint,
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
    links = links,
    preprocessorDefinitions = preprocessorDefinitions,
    linkCppRuntime = false
  )

  @deprecated("Use .withXYZ methods instead", since = "0.5.7")
  def copy(
      inlineHint: Inline = inlineHint,
      specialize: Specialize = specialize,
      opt: Opt = opt,
      align: Option[Alignment] = align,
      isExtern: Boolean = isExtern,
      isBlocking: Boolean = isBlocking,
      isDyn: Boolean = isDyn,
      isStub: Boolean = isStub,
      isAbstract: Boolean = isAbstract,
      isVolatile: Boolean = isVolatile,
      isFinal: Boolean = isFinal,
      isSafePublish: Boolean = isSafePublish,
      isLinktimeResolved: Boolean = isLinktimeResolved,
      isUsingIntrinsics: Boolean = isUsingIntrinsics,
      links: Seq[Attr.Link] = links,
      preprocessorDefinitions: Seq[Attr.Define] = preprocessorDefinitions
  ): Attrs =
    new Attrs(
      inlineHint = inlineHint,
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
      links = links,
      preprocessorDefinitions = preprocessorDefinitions,
      linkCppRuntime = linkCppRuntime
    )

  @deprecated("To be removed", since = "0.5.7")
  def canEqual(that: Any): Boolean = equals(that)

  @deprecated("To be removed", since = "0.5.7")
  def productArity: Int = 17

  @deprecated("To be removed", since = "0.5.7")
  def productElement(n: Int): Any = n match {
    case 0  => inlineHint
    case 1  => specialize
    case 2  => opt
    case 3  => align
    case 4  => isExtern
    case 5  => isBlocking
    case 6  => isDyn
    case 7  => isStub
    case 8  => isAbstract
    case 9  => isVolatile
    case 10 => isFinal
    case 11 => isSafePublish
    case 12 => isLinktimeResolved
    case 13 => isUsingIntrinsics
    case 14 => links
    case 15 => preprocessorDefinitions
    case 16 => linkCppRuntime
    case i  => throw new java.lang.IndexOutOfBoundsException(i.toString())
  }

}
object Attrs {
  private val HashSeed =
    scala.util.hashing.MurmurHash3.stringHash("scala.scalanative.nir.Attrs")

  val None = new Attrs(
    inlineHint = MayInline,
    specialize = MaySpecialize,
    opt = UnOpt,
    align = Option.empty,
    isExtern = false,
    isBlocking = false,
    isDyn = false,
    isStub = false,
    isAbstract = false,
    isVolatile = false,
    isFinal = false,
    isSafePublish = false,
    isLinktimeResolved = false,
    isUsingIntrinsics = false,
    links = Seq.empty,
    preprocessorDefinitions = Seq.empty,
    linkCppRuntime = false
  )

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
      case attr: Alignment  =>
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

  @deprecated("unapply is not supported anymore on nir.Attrs", since = "0.5.7")
  def unapply(attrs: Attrs): Option[Attrs] = Some(attrs)

  @deprecated("Use nir.Attrs.None.with instead", since = "0.5.7")
  def apply(
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
      preprocessorDefinitions: Seq[Attr.Define] = Seq.empty
  ): Attrs = new Attrs(
    inlineHint = inlineHint,
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
    links = links,
    preprocessorDefinitions = preprocessorDefinitions,
    linkCppRuntime = false
  )
}
