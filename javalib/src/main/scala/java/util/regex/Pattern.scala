package java.util
package regex

import scalanative.native._, stdlib._, stdio._, string._
import cre2h._

// Inspired by: https://github.com/google/re2j/blob/master/java/com/google/re2j/Pattern.java

object Pattern {
  def CANON_EQ: Int                = 128
  def CASE_INSENSITIVE: Int        = 2
  def COMMENTS: Int                = 4
  def DOTALL: Int                  = 32
  def LITERAL: Int                 = 16
  def MULTILINE: Int               = 8
  def UNICODE_CASE: Int            = 64
  def UNICODE_CHARACTER_CLASS: Int = 256
  def UNIX_LINES: Int              = 1

  def compile(regex: String): Pattern = compile(regex, 0)

  def compile(regex: String, flags: Int): Pattern =
    compile(regex, 0, adapt = true)

  def compile(regex: String, flags: Int, adapt: Boolean): Pattern = {

    def notSupported(flag: Int, flagName: String): Unit = {
      if ((flags & flag) == flag) {
        assert(false, s"regex flag $flagName is not supported")
      }
    }

    notSupported(CANON_EQ, "CANON_EQ(canonical equivalences)")
    notSupported(COMMENTS, "COMMENTS")
    notSupported(UNICODE_CASE, "UNICODE_CASE")
    notSupported(UNICODE_CHARACTER_CLASS, "UNICODE_CHARACTER_CLASS")
    notSupported(UNIX_LINES, "UNIX_LINES")

    val options = cre2.optNew()
    cre2.setCaseSensitive(options, flags & CASE_INSENSITIVE)
    cre2.setDotNl(options, flags & DOTALL)
    cre2.setLiteral(options, flags & LITERAL)
    cre2.setLogErrors(options, 0)

    // setOneLine(false) is only available when limiting ourself to posix_syntax
    // https://github.com/google/re2/blob/2017-03-01/re2/re2.h#L548
    // regex flag MULTILINE cannot be disabled

    val re2 = cre2.compile(toCString(regex), regex.size, options)

    val code = new ErrorCode(cre2.errorCode(re2))
    import ErrorCode._

    if (code != NoError) {
      val errorPattern = {
        val arg = StringPart.stackalloc
        cre2.errorArg(re2, arg)
        arg.toString
      }

      // we try to find the index of the parsing error
      // this could return the wrong index it only finds the first match
      // see https://groups.google.com/forum/#!topic/re2-dev/rnvFZ9Ki8nk
      val index =
        if (code == ErrorCode.TrailingBackslash) regex.size - 1
        else regex.indexOfSlice(errorPattern)

      val reText = fromCString(cre2.errorString(re2))

      val description =
        code match {
          case BadEscape         => "Illegal/unsupported escape sequence"
          case MissingParent     => "Missing parenthesis"
          case TrailingBackslash => "Trailing Backslash"
          case MissingBracket    => "Unclosed character class"
          case BadCharRange      => "Illegal character range"
          case BadCharClass      => "Illegal/unsupported character class"
          case RepeatSize        => "Bad repetition argument"
          case RepeatArgument    => "Dangling meta character '*'"
          case RepeatOp          => "Bad repetition operator"
          case BadPerlOp         => "Bad perl operator"
          case BadUtf8           => "Invalid UTF-8 in regexp"
          case BadNamedCapture   => "Bad named capture group"
          case PatternTooLarge   => "Pattern too large (compilation failed)"
          case Internal          => "Internal Error"
          case _                 => reText
        }

      throw new PatternSyntaxException(
        description,
        regex,
        index
      )
    }

    cre2.optDelete(options)

    new Pattern(
      _pattern = regex,
      _flags = flags,
      _regex = re2
    )
  }

  def matches(regex: String, input: CharSequence): Boolean =
    compile(regex).matcher(input).matches

  def quote(s: String): String = {
    val original = StringPart(s)
    val quoted   = StringPart.stackalloc
    cre2.quoteMeta(quoted, original)
    quoted.toString
  }

  def adaptPatternToRe2(regex: String): String = {
    regex
  }
}

final class Pattern private[regex] (
    _pattern: String,
    _flags: Int,
    _regex: Ptr[Regex]
) {

  private[regex] def regex: Ptr[Regex] = _regex

  def split(input: CharSequence): Array[String] =
    split(input, 0)

  def split(input: CharSequence, limit: Int): Array[String] =
    split(new Matcher(this, input), limit)

  private def split(m: Matcher, limit: Int): Array[String] = {
    var matchCount = 0
    var arraySize  = 0
    var last       = 0
    while (m.find()) {
      matchCount += 1
      if (limit != 0 || last < m.start()) {
        arraySize = matchCount
      }
      last = m.end()
    }
    if (last < m.inputLength || limit != 0) {
      matchCount += 1
      arraySize = matchCount
    }
    var trunc = 0
    if (limit > 0 && arraySize > limit) {
      arraySize = limit
      trunc = 1
    }

    val array = Array.ofDim[String](arraySize)
    var i     = 0
    last = 0
    m.reset()
    while (m.find() && i < arraySize - trunc) {
      val t = i
      i += 1
      array(t) = m.substring(last, m.start())
      last = m.end()
    }
    if (i < arraySize) {
      array(i) = m.substring(last, m.inputLength)
    }
    array
  }

  def matcher(input: CharSequence): Matcher = new Matcher(this, input)

  def flags: Int                = _flags
  def pattern: String           = _pattern
  override def toString: String = _pattern

  override protected def finalize(): Unit = {
    super.finalize()
    cre2.delete(_regex)
  }
}
