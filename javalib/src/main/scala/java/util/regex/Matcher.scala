// format: off
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

  /** Mirrors JDK: after {@code usePattern}, capture tuples are cleared but
   *  the previous match span is kept for {@code start}/{@code end}/
   *  {@code appendReplacement}; {@code group(n)} returns {@code null}.
   */
  private var groupsClearedByUsePattern: Boolean = false

  private var appendPos: Int = 0

  def matches(): Boolean = {
    resetMatch()

    lastMatch = pattern().execMatches(inputstr)
    lastMatchIsForMatches = true
    if (lastMatch != null)
      groupsClearedByUsePattern = false
    lastMatch != null
  }

  def lookingAt(): Boolean = {
    resetMatch()
    find()
    if ((lastMatch != null) && (engine.getIndex(ensureLastMatch) != 0))
      resetMatch()
    if (lastMatch != null)
      groupsClearedByUsePattern = false
    lastMatch != null
  }

  def find(): Boolean = {
    val (mtch, end) = pattern().execFind(inputstr, position)
    position =
      if (mtch != null) (if (end == engine.getIndex(mtch)) end + 1 else end)
      else inputstr.length() + 1
    lastMatch = mtch
    lastMatchIsForMatches = false
    if (mtch != null)
      groupsClearedByUsePattern = false
    mtch != null
  }

  def find(start: Int): Boolean = {
    if (start < 0 || start > input0.length)
      throw new IndexOutOfBoundsException("Illegal start index")
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
          if (i >= len) {
            sb.append('$')
          } else if (replacement.charAt(i) == '{') {
            i += 1
            val nameStart = i
            while (i < len && replacement.charAt(i) != '}')
              i += 1
            if (i >= len)
              throw new IllegalArgumentException("named capturing group is missing trailing '}'")
            val name = replacement.substring(nameStart, i)
            i += 1
            val replaced = this.group(name)
            if (replaced != null)
              sb.append(replaced)
          } else if (isDigit(replacement.charAt(i))) {
            val j = i
            while (i < len && isDigit(replacement.charAt(i)))
              i += 1
            val group = Integer.parseInt(replacement.substring(j, i))
            val replaced = this.group(group)
            if (replaced != null)
              sb.append(replaced)
          } else {
            sb.append('$')
          }

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
    groupsClearedByUsePattern = false
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
    groupsClearedByUsePattern = true
    this
  }

  private def ensureLastMatch: engine.ExecResult = {
    if (lastMatch == null)
      throw new IllegalStateException("No match available")
    lastMatch
  }

  def groupCount(): Int = pattern().groupCount

  def start(): Int = engine.getIndex(ensureLastMatch) + regionStart()

  def end(): Int = {
    ensureLastMatch
    val ix = engine.getIndices(lastMatch)
    val raw = engine.getEnd(ix, 0)
    if (raw < 0) raw
    else raw + regionStart()
  }

  def group(): String = group(0)

  private def indices: engine.IndicesArray =
    pattern().getIndices(ensureLastMatch, lastMatchIsForMatches)

  private def checkGroupIndex(group: Int): Unit = {
    if (group < 0 || group > pattern().groupCount)
      throw new IndexOutOfBoundsException(s"No group $group")
  }

  private def startInternal(compiledGroup: Int): Int = {
    val rawResult = engine.getStart(indices, compiledGroup)
    if (rawResult < 0) rawResult
    else rawResult + regionStart()
  }

  def start(group: Int): Int = {
    checkGroupIndex(group)
    if (groupsClearedByUsePattern) -1
    else startInternal(pattern().numberedGroup(group))
  }

  def start(name: String): Int = {
    ensureLastMatch
    val num = pattern().namedGroup(name)
    if (groupsClearedByUsePattern) -1
    else startInternal(num)
  }

  private def endInternal(compiledGroup: Int): Int = {
    val rawResult = engine.getEnd(indices, compiledGroup)
    if (rawResult < 0) rawResult
    else rawResult + regionStart()
  }

  def end(group: Int): Int = {
    checkGroupIndex(group)
    if (groupsClearedByUsePattern) -1
    else endInternal(pattern().numberedGroup(group))
  }

  def end(name: String): Int = {
    ensureLastMatch
    val num = pattern().namedGroup(name)
    if (groupsClearedByUsePattern) -1
    else endInternal(num)
  }

  def group(group: Int): String = {
    ensureLastMatch
    checkGroupIndex(group)
    if (groupsClearedByUsePattern)
      null
    else
      engine.getGroup(lastMatch, pattern().numberedGroup(group))
  }

  def group(name: String): String = {
    ensureLastMatch
    if (groupsClearedByUsePattern)
      null
    else
      engine.getGroup(lastMatch, pattern().namedGroup(name))
  }

  def toMatchResult(): MatchResult =
    new SealedResult(lastMatch, lastMatchIsForMatches, pattern(), regionStart(),
        groupsClearedByUsePattern)

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
        val g = group()
        if (g == null) "" else g
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
      lastMatchIsForMatches: Boolean, pattern: Pattern, regionStart: Int,
      groupsClearedByUsePattern: Boolean)
      extends MatchResult {

    def groupCount(): Int = pattern.groupCount

    def start(): Int = engine.getIndex(ensureLastMatch) + regionStart

    def end(): Int = {
      val ix = engine.getIndices(lastMatch)
      val raw = engine.getEnd(ix, 0)
      if (raw < 0) raw
      else raw + regionStart
    }

    def group(): String = group(0)

    private def indices: engine.IndicesArray =
      pattern.getIndices(ensureLastMatch, lastMatchIsForMatches)

    private def checkGroupIndex(group: Int): Unit = {
      if (group < 0 || group > pattern.groupCount)
        throw new IndexOutOfBoundsException(s"No group $group")
    }

    def start(group: Int): Int = {
      checkGroupIndex(group)
      if (groupsClearedByUsePattern) -1
      else {
        val rawResult = engine.getStart(indices, pattern.numberedGroup(group))
        if (rawResult < 0) rawResult
        else rawResult + regionStart
      }
    }

    def end(group: Int): Int = {
      checkGroupIndex(group)
      if (groupsClearedByUsePattern) -1
      else {
        val rawResult = engine.getEnd(indices, pattern.numberedGroup(group))
        if (rawResult < 0) rawResult
        else rawResult + regionStart
      }
    }

    def group(group: Int): String = {
      checkGroupIndex(group)
      if (groupsClearedByUsePattern) null
      else engine.getGroup(lastMatch, pattern.numberedGroup(group))
    }

    private def ensureLastMatch: engine.ExecResult = {
      if (lastMatch == null)
        throw new IllegalStateException("No match available")
      lastMatch
    }
  }
}
