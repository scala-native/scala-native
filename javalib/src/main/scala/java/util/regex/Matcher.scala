/*
 * Derived from Scala.js / scala-wasm (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 */

package java.util.regex

import scala.annotation.switch

import java.lang.{StringBuilder => JStringBuilder}
import java.util.function.Function

final class Matcher private[regex] (
    private var pattern0: Pattern,
    private var input0: String)
    extends AnyRef
    with MatchResult
    with Cloneable {

  import Matcher._
  import Engine.engine

  def pattern(): Pattern = pattern0

  private var regionStart0 = 0
  private var regionEnd0 = input0.length()
  private var inputstr = input0

  private var position: Int = 0
  private var lastMatch: engine.ExecResult = null
  private var lastMatchIsForMatches = false

  private var appendPos: Int = 0

  def matches(): Boolean = {
    resetMatch()

    lastMatch = pattern().execMatches(inputstr)
    lastMatchIsForMatches = true
    lastMatch != null
  }

  def lookingAt(): Boolean = {
    resetMatch()
    find()
    if ((lastMatch != null) && (engine.getIndex(ensureLastMatch) != 0))
      resetMatch()
    lastMatch != null
  }

  def find(): Boolean = {
    val (mtch, end) = pattern().execFind(inputstr, position)
    position =
      if (mtch != null) (if (end == engine.getIndex(mtch)) end + 1 else end)
      else inputstr.length() + 1
    lastMatch = mtch
    lastMatchIsForMatches = false
    mtch != null
  }

  def find(start: Int): Boolean = {
    reset()
    position = start
    find()
  }

  @noinline
  def appendReplacement(sb: StringBuffer, replacement: String): Matcher =
    appendReplacementGeneric(sb, replacement)

  @noinline
  def appendReplacement(sb: JStringBuilder, replacement: String): Matcher =
    appendReplacementGeneric(sb, replacement)

  @inline
  private def appendReplacementGeneric(sb: Appendable, replacement: String): Matcher = {
    sb.append(inputstr.substring(appendPos, start()))

    @inline def isDigit(c: Char) = c >= '0' && c <= '9'

    val len = replacement.length
    var i = 0
    while (i < len) {
      replacement.charAt(i) match {
        case '$' =>
          i += 1
          val j = i
          while (i < len && isDigit(replacement.charAt(i)))
            i += 1
          val group = Integer.parseInt(replacement.substring(j, i))
          val replaced = this.group(group)
          if (replaced != null)
            sb.append(replaced)

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

    appendPos = end()
    this
  }

  def appendTail(sb: StringBuffer): StringBuffer = {
    sb.append(inputstr.substring(appendPos))
    appendPos = inputstr.length
    sb
  }

  def appendTail(sb: JStringBuilder): JStringBuilder = {
    sb.append(inputstr.substring(appendPos))
    appendPos = inputstr.length
    sb
  }

  @noinline
  def replaceFirst(replacement: String): String =
    replaceFirst(_ => replacement)

  def replaceFirst(replacer: Function[MatchResult, String]): String = {
    reset()

    if (find()) {
      val sb = new JStringBuilder()
      appendReplacement(sb, replacer(this))
      appendTail(sb)
      sb.toString()
    } else {
      inputstr
    }
  }

  @noinline
  def replaceAll(replacement: String): String =
    replaceAll(_ => replacement)

  def replaceAll(replacer: Function[MatchResult, String]): String = {
    reset()

    val sb = new JStringBuilder()
    while (find()) {
      appendReplacement(sb, replacer(this))
    }
    appendTail(sb)

    sb.toString()
  }

  private def resetMatch(): Matcher = {
    position = 0
    lastMatch = null
    appendPos = 0
    this
  }

  def reset(): Matcher = {
    regionStart0 = 0
    regionEnd0 = input0.length()
    inputstr = input0
    resetMatch()
  }

  def reset(input: CharSequence): Matcher = {
    input0 = input.toString()
    reset()
  }

  def usePattern(pattern: Pattern): Matcher = {
    if (pattern == null)
      throw new IllegalArgumentException("Pattern cannot be null")
    pattern0 = pattern
    lastMatch = null
    this
  }

  private def ensureLastMatch: engine.ExecResult = {
    if (lastMatch == null)
      throw new IllegalStateException("No match available")
    lastMatch
  }

  def groupCount(): Int = pattern().groupCount

  def start(): Int = engine.getIndex(ensureLastMatch) + regionStart()
  def end(): Int = start() + group().length
  def group(): String = engine.getGroup(ensureLastMatch, 0)

  private def indices: engine.IndicesArray =
    pattern().getIndices(ensureLastMatch, lastMatchIsForMatches)

  private def startInternal(compiledGroup: Int): Int = {
    val rawResult = engine.getStart(indices, compiledGroup)
    if (rawResult < 0) rawResult
    else rawResult + regionStart()
  }

  def start(group: Int): Int =
    startInternal(pattern().numberedGroup(group))

  def start(name: String): Int =
    startInternal(pattern().namedGroup(name))

  private def endInternal(compiledGroup: Int): Int = {
    val rawResult = engine.getEnd(indices, compiledGroup)
    if (rawResult < 0) rawResult
    else rawResult + regionStart()
  }

  def end(group: Int): Int =
    endInternal(pattern().numberedGroup(group))

  def end(name: String): Int =
    endInternal(pattern().namedGroup(name))

  def group(group: Int): String =
    engine.getGroup(ensureLastMatch, pattern().numberedGroup(group))

  def group(name: String): String =
    engine.getGroup(ensureLastMatch, pattern().namedGroup(name))

  def toMatchResult(): MatchResult =
    new SealedResult(lastMatch, lastMatchIsForMatches, pattern(), regionStart())

  def regionStart(): Int = regionStart0
  def regionEnd(): Int = regionEnd0

  def region(start: Int, end: Int): Matcher = {
    regionStart0 = start
    regionEnd0 = end
    inputstr = input0.substring(start, end)
    resetMatch()
  }

  def hasTransparentBounds(): Boolean = false

  def useTransparentBounds(b: Boolean): Matcher =
    throw new UnsupportedOperationException("useTransparentBounds is not supported.")

  def hasAnchoringBounds(): Boolean = true

  def useAnchoringBounds(b: Boolean): Matcher =
    throw new UnsupportedOperationException("useAnchoringBounds is not supported.")

  def hitEnd(): Boolean =
    throw new UnsupportedOperationException("hitEnd is not supported.")

  def requireEnd(): Boolean =
    throw new UnsupportedOperationException("requireEnd is not supported.")

  override def toString: String = {
    val regStart = regionStart()
    val regEnd = regionEnd()
    val last =
      try {
        group()
      } catch {
        case _: IllegalStateException => ""
      }
    s"java.util.regex.Matcher[pattern=${pattern0}" +
      s" region=${regStart},${regEnd}" +
      s" lastmatch=${last}]"
  }
}

object Matcher {
  import Engine.engine

  def quoteReplacement(s: String): String = {
    var result = ""
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      result += ((c: @switch) match {
        case '\\' | '$' => "\\" + c
        case _          => c.toString
      })
      i += 1
    }
    result
  }

  private final class SealedResult(lastMatch: engine.ExecResult,
      lastMatchIsForMatches: Boolean, pattern: Pattern, regionStart: Int)
      extends MatchResult {

    def groupCount(): Int = pattern.groupCount

    def start(): Int = engine.getIndex(ensureLastMatch) + regionStart
    def end(): Int = start() + group().length
    def group(): String = engine.getGroup(ensureLastMatch, 0)

    private def indices: engine.IndicesArray =
      pattern.getIndices(ensureLastMatch, lastMatchIsForMatches)

    def start(group: Int): Int = {
      val rawResult = engine.getStart(indices, pattern.numberedGroup(group))
      if (rawResult < 0) rawResult
      else rawResult + regionStart
    }

    def end(group: Int): Int = {
      val rawResult = engine.getEnd(indices, pattern.numberedGroup(group))
      if (rawResult < 0) rawResult
      else rawResult + regionStart
    }

    def group(group: Int): String =
      engine.getGroup(ensureLastMatch, pattern.numberedGroup(group))

    private def ensureLastMatch: engine.ExecResult = {
      if (lastMatch == null)
        throw new IllegalStateException("No match available")
      lastMatch
    }
  }
}
