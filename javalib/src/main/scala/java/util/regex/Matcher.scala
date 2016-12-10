package java.util.regex

trait MatchResult {
  def groupCount(): Int

  def start(): Int
  def end(): Int
  def group(): String

  def start(group: Int): Int
  def end(group: Int): Int
  def group(group: Int): String
}

final class RegexMatcher private[regex] (private var _pattern: Pattern,
                                         private var _input: String,
                                         private var regionStart: Int,
                                         private var regionEnd: Int)
    extends AnyRef
    with MatchResult {

  def pattern(): Pattern = _pattern

  private var inputstr = _input.subSequence(regionStart, regionEnd).toString

  def matches(): Boolean =
    Pattern.execute(_pattern, _input) == 0

  def groupCount(): Int = ???

  def start(): Int    = ???
  def end(): Int      = ???
  def group(): String = ???

  def start(group: Int): Int    = ???
  def end(group: Int): Int      = ???
  def group(group: Int): String = ???
}
