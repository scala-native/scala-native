package java.util.regex

import scalanative.native._, stdlib._, stdio._
import scalanative.runtime.struct

@extern
object Regex {
  def regcomp(regex: Ptr[Int], str: CString, num: Int): Int = extern
  def regexec(regex: Ptr[Int],
              str: CString,
              nmatch: Int,
              matchposition: Ptr[RegMatch.regmatch_t],
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
  def split(input: CharSequence): Array[String] =
    split(input, 0)

  def split(input: CharSequence, limit: Int): Array[String] = {
    val inputStr = input.toString

    if (inputStr == "") {
      Array("")
    } else {
      val lim     = if (limit > 0) limit else Int.MaxValue
      val matcher = this.matcher(inputStr)
      val builder = Array.newBuilder[String]
      var prevEnd = 0
      var size    = 0
      while ((size < lim - 1) && matcher.find()) {
        if (matcher.end == 0) {} else {
          builder += inputStr.substring(prevEnd, matcher.start)
          size += 1
        }
        prevEnd = matcher.end
      }
      builder += inputStr.substring(prevEnd)
      val result = builder.result()

      if (limit != 0) {
        result
      } else {
        var actualLength = result.length
        while (actualLength != 0 && result(actualLength - 1) == "") actualLength -= 1

        if (actualLength == result.length) {
          result
        } else {
          val actualResult = new Array[String](actualLength)
          System.arraycopy(result, 0, actualResult, 0, actualLength)
          actualResult
        }
      }
    }
  }
}

object Pattern {
  def compile(regex: String, flags: Int): Pattern = {
    val compiledRegex = malloc(sizeof[Int] * 100).cast[Ptr[Int]] //Dirtiest hack ever
    val retval        = Regex.regcomp(compiledRegex, toCString(regex), 0)
    if (retval == 0) {
      new Pattern(compiledRegex, regex, flags)
    } else new Pattern(null, regex, flags)
  }

  def execute(pattern: Pattern, text: String): FullRegMatch = {
    val regmatch = malloc(sizeof[RegMatch.regmatch_t] * 1)
      .cast[Ptr[RegMatch.regmatch_t]]
    regmatch(0) = new RegMatch.regmatch_t(-1, -1)
    if (pattern.compiled != null) {
      val retval =
        Regex.regexec(pattern.compiled, toCString(text), 1, regmatch, 0)
      new FullRegMatch(regmatch(0).rm_so, regmatch(0).rm_eo, text, retval)
    } else {
      new FullRegMatch(-1, -1, text, -1)
    }
  }

  def matches(regex: String, input: CharSequence): Boolean =
    compile(regex).matcher(input.toString).matches()

  def compile(regex: String): Pattern = compile(regex, 0)
}
