package java.nio.file.glob

trait GlobSpecification
case class GlobChar(c: Char) extends GlobSpecification // char
case object AnyChar extends GlobSpecification // `?`
case object NonCrossingName extends GlobSpecification // `*`
case object CrossingName extends GlobSpecification // `**`
case object Divider extends GlobSpecification // `/`
case class NegationBracket(excluded: Set[Char])
    extends GlobSpecification // `[!(...)]`
case class Bracket(included: Set[Char]) extends GlobSpecification // `[(...)]`
case class Groups(elements: List[List[GlobSpecification]])
    extends GlobSpecification // requires optimization // `{(...)}`
