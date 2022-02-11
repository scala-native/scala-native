package java.nio.file.glob

private[glob] sealed trait GlobSpecification
private[glob] case class GlobChar(c: Char) extends GlobSpecification // char
private[glob] case object AnyChar extends GlobSpecification // `?`
private[glob] case object Star extends GlobSpecification // `*`
private[glob] case object DoubleStar extends GlobSpecification // `**`
private[glob] case object Divider extends GlobSpecification // `/`
private[glob] case class NegationBracket(excluded: Set[Char])
    extends GlobSpecification // `[!(...)]`
private[glob] case class Bracket(included: Set[Char])
    extends GlobSpecification // `[(...)]`
private[glob] case class Groups(elements: List[List[GlobSpecification]])
    extends GlobSpecification // `{(...)}`
