package java.util.regex

import scalanative.native._, stdlib._, stdio._

@extern
object Regex {
  def regcomp(regex: Ptr[Int], str: CString, num: Int): Int = extern
  def regexec(regex: Ptr[Int],
              str: CString,
              num: Int,
              ptr: Ptr[Int],
              num2: Int): Int = extern
}

case class RegexError(error: Int)

trait RegexPattern {
  def flags(): Int
  def pattern(): String
  override def toString(): String
}

final class Pattern private (_compiled: Ptr[Int],
                             _pattern: String,
                             _flags: Int)
    extends Serializable
    with RegexPattern {
  def flags(): Int                = _flags
  def compiled(): Ptr[Int]        = _compiled
  def pattern(): String           = _pattern
  override def toString(): String = _pattern
  def matcher(input: String): RegexMatcher =
    new RegexMatcher(this, input, 0, input.length)
}

object Pattern {
  def compile(regex: String, flags: Int): Pattern = {
    val compiledRegex = malloc(sizeof[Int] * 100).cast[Ptr[Int]] //Dirtiest hack ever
    val retval        = Regex.regcomp(compiledRegex, toCString(regex), 0)
    if (retval == 0) {
      new Pattern(compiledRegex, regex, flags)
    } else new Pattern(null, regex, flags)
  }

  def matches(regex: String, input: CharSequence): Boolean =
    compile(regex).matcher(input.toString).matches()

  def compile(regex: String): Pattern = compile(regex, 0)

  def execute(pattern: Pattern, text: String): Int = {
    if (pattern.compiled != null)
      Regex.regexec(pattern.compiled, toCString(text), 0, null, 0)
    else
      -1
  }
}
