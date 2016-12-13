package scala.scalanative.posix.regex

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

case class PosixRegexError(error: Int)

final class PosixPattern private (_compiled: Ptr[Int],
                                  _pattern: String,
                                  _flags: Int)
    extends Serializable {
  def flags(): Int                = _flags
  def pattern(): String           = _pattern
  def compiled(): Ptr[Int]        = _compiled
  override def toString(): String = _pattern
  def matcher(input: String): Matcher =
    new Matcher(this, input, 0, input.length)
}

object PosixPattern {
  def compile(regex: String,
              flags: Int): Either[PosixRegexError, PosixPattern] = {
    val compiledRegex = malloc(sizeof[Int]).cast[Ptr[Int]]
    val retval        = Regex.regcomp(compiledRegex, toCString(regex), 0)
    if (retval == 0) {
      Right(new PosixPattern(compiledRegex, regex, flags))
    } else Left(PosixRegexError(retval))
  }

  def matches(regex: String, input: String): Either[PosixRegexError, Boolean] =
    compile(regex) match {
      case Right(compiled) => Right(compiled.matcher(input).matches())
      case Left(error)     => Left(error)
    }

  def compile(regex: String): Either[PosixRegexError, PosixPattern] =
    compile(regex, 0)

  def execute(pattern: PosixPattern, text: String): Int = {
    Regex.regexec(pattern.compiled, toCString(text), 0, null, 0)
  }
}
