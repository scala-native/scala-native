package java.util.regex

import scalanative.posix.{RegexPattern, PosixPattern, RegexMatcher, PosixRegexError}

case class RegexError(error: Int)

final class Pattern private (_pattern: String,
                             _flags: Int)
    extends Serializable with RegexPattern {
  def flags(): Int                = _flags
  def pattern(): String           = _pattern
  override def toString(): String = _pattern
}

object Pattern {
  implicit def portPattern(posix: Either[PosixRegexError, PosixPattern]): Either[RegexError, Pattern] = posix match {
    case Right(pattern) => Right(pattern.asInstanceOf[Pattern])
    case Left(error) => Left(error.asInstanceOf[RegexError])
  }
  implicit def portError(posix: Either[PosixRegexError, Boolean]): Either[RegexError, Boolean] = posix match {
    case Right(bool) => Right(bool)
    case Left(error) => Left(error.asInstanceOf[RegexError])
  }
  def compile(regex: String,
              flags: Int): Either[RegexError, Pattern] = {
    PosixPattern.compile(regex, flags)
  }
  def matches(regex: String, input: String): Either[RegexError, Boolean] =
    PosixPattern.matches(regex, input)
}
