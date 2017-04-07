package java.util.regex

import scalanative.native._, stdlib._, stdio._
import scalanative.runtime.struct

object RegMatch {
  @struct
  class regmatch_t(val rm_so: Int, val rm_eo: Int)
}

class FullRegMatch(val rm_so: Int,
                   val rm_eo: Int,
                   input: String,
                   val retval: Int) {
  def length: Int               = rm_eo - rm_so
  def get: String               = input.substring(rm_so, rm_eo)
  override def toString: String = s"$rm_so $rm_eo $input $retval"
}

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

  private var inputstr = _input.substring(regionStart, regionEnd)

  private var lastMatch: FullRegMatch = new FullRegMatch(-1, -1, "", -1) // Not liking this: it creates a new struct on each instance of RegexMatcher
  private var lastMatchIsValid        = false
  private var canStillFind            = true
  private var appendPos: Int          = 0

  def inputstr(regionStart: Int, regionEnd: Int): String =
    _input.substring(regionStart, regionEnd)

  def matches(): Boolean = {
    Pattern.execute(_pattern, _input).retval == 0
  }

  def groupCount(): Int = ???

  def start(group: Int): Int    = ???
  def end(group: Int): Int      = ???
  def group(group: Int): String = ???

  // The following are based on the Matcher from scala-js

  def find(): Boolean =
    if (canStillFind) {
      lastMatchIsValid = true
      lastMatch = Pattern.execute(_pattern, inputstr)
      if (lastMatch.rm_so != -1) {
        regionStart = lastMatch.rm_eo
      } else {
        canStillFind = false
      }
      lastMatch.rm_so != -1
    } else false

  def find(start: Int): Boolean = {
    reset()
    find()
  }

  def reset(): RegexMatcher = {
    regionStart = 0
    lastMatch = null
    lastMatchIsValid = false
    canStillFind = true
    appendPos = 0
    this
  }

  private def ensureLastMatch: FullRegMatch = {
    if (lastMatch.rm_so == -1)
      throw new IllegalStateException("No match available")
    lastMatch
  }

  def start(): Int    = ensureLastMatch.rm_so
  def end(): Int      = start() + group().length
  def group(): String = ensureLastMatch.get

  def replaceFirst(replacement: String): String = {
    reset()

    if (find()) {
      val sb = new StringBuffer
      appendReplacement(sb, replacement)
      appendTail(sb)
      sb.toString
    } else {
      inputstr
    }
  }

  def replaceAll(replacement: String): String = {
    reset()

    val sb = new StringBuffer
    while (find()) {
      appendReplacement(sb, replacement)
    }
    appendTail(sb)

    sb.toString
  }

  def appendReplacement(sb: StringBuffer, replacement: String): RegexMatcher = {
    sb.append(inputstr.substring(appendPos, start))

    @inline def isDigit(c: Char) = c >= '0' && c <= '9'

    val len = replacement.length
    var i   = 0
    while (i < len) {
      replacement.charAt(i) match {
        case '$' =>
          i += 1
          val j = i
          while (i < len && isDigit(replacement.charAt(i))) i += 1
          val group = Integer.parseInt(replacement.substring(j, i))
          sb.append(this.group(group))

        case '\\' =>
          i += 1
          if (i < len)
            sb.append(replacement.charAt(i))
          i += 1

        case c =>
          sb.append(c)
          i += 1
      }
    }

    appendPos = end
    this
  }

  def appendTail(sb: StringBuffer): StringBuffer = {
    sb.append(inputstr.substring(appendPos))
    appendPos = inputstr.length
    sb
  }

}
