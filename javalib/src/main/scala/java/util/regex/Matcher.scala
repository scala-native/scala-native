package java.util
package regex

import cre2h._
import scalanative.native._, stdlib._, stdio._, string._

// Inspired by: https://github.com/google/re2j/blob/master/java/com/google/re2j/Matcher.java

object Matcher {
  def quoteReplacement(s: String): String = {
    if (s.indexOf('\\') < 0 && s.indexOf('$') < 0) {
      s
    } else {
      val sb = new StringBuilder()
      var i  = 0
      while (i < s.length) {
        val c = s.charAt(i)
        if (c == '\\' || c == '$') {
          sb.append('\\')
        }
        sb.append(c)
        i += 1
      }
      sb.toString
    }
  }
}

final class Matcher private[regex] (var _pattern: Pattern,
                                    var inputSequence: CharSequence)
    extends MatchResult {

  private val regex = _pattern.regex

  private var hasMatch  = false
  private var hasGroups = false
  private var appendPos = 0

  private var groups =
    Array.ofDim[(Int, Int)](cre2.numCapturingGroups(regex) + 1)

  private var lastAnchor: Option[cre2.anchor_t] = None

  private[regex] var inputLength = inputSequence.length

  def matches(): Boolean = genMatch(0, ANCHOR_BOTH)

  def lookingAt(): Boolean = genMatch(0, ANCHOR_START)

  def find(start: Int): Boolean = genMatch(0, UNANCHORED)

  def find(): Boolean = {
    var startIndex = 0
    if (hasMatch) {
      startIndex = end
      if (start == end) {
        startIndex += 1
      }
    }

    genMatch(startIndex, UNANCHORED)
  }

  private def doMatch(start: Int,
                      end: Int,
                      nMatches: Int,
                      anchor: cre2.anchor_t): Boolean =
    Zone { implicit z =>
      val n       = nMatches
      val matches = alloc[cre2.string_t](n)
      val in      = toCString(inputSequence.toString)

      val ok = cre2.matches(
        regex = regex,
        text = in,
        textlen = inputLength,
        startpos = start,
        endpos = end,
        anchor = anchor,
        matches = matches,
        nMatches = nMatches
      ) == 1

      if (ok) {
        var i = 0
        while (i < nMatches) {
          val m     = matches + i
          val start = if (m.length == 0) 0 else (m.data - in).toInt
          val end   = if (m.length == 0) 0 else start + m.length
          groups(i) = ((start, end))

          i += 1
        }
      }

      ok
    }

  private def genMatch(start: Int, anchor: cre2.anchor_t): Boolean = {
    val ok = doMatch(start, inputLength, 1, anchor)

    if (ok) {
      hasMatch = true
      hasGroups = false
      lastAnchor = Some(anchor)
    }

    ok
  }

  def replaceFirst(replacement: String): String =
    replace(replacement, global = false)

  def replaceAll(replacement: String): String =
    replace(replacement, global = true)

  private def replace(replacement: String, global: Boolean): String =
    Zone { implicit z =>
      val textAndTarget, rewrite = stackalloc[cre2.string_t]

      toRE2String(inputSequence.toString, textAndTarget)
      toRE2String(replacement, rewrite)

      if (global) cre2.globalReplace(regex, textAndTarget, rewrite)
      else cre2.replace(regex, textAndTarget, rewrite)

      val res = fromRE2String(textAndTarget)

      res
    }

  def group(): String = group(0)

  def group(group: Int): String = {
    val startIndex = start(group)
    val endIndex   = end(group)

    if (startIndex < 0 && endIndex < 0) {
      null
    } else {
      inputSequence.subSequence(startIndex, endIndex).toString
    }
  }

  def group(name: String): String = group(groupIndex(name))

  private def groupIndex(name: String): Int =
    Zone { implicit z =>
      val pos = cre2.findNamedCapturingGroups(regex, toCString(name))
      if (pos == -1) {
        throw new IllegalArgumentException(s"No group with name <$name>")
      }
      pos
    }

  def groupCount: Int = groups.length - 1

  def start: Int = start(0)

  def start(group: Int): Int = {
    loadGroup(group)
    groups(group)._1
  }

  def start(name: String): Int = start(groupIndex(name))

  def end: Int = end(0)

  def end(group: Int): Int = {
    loadGroup(group)
    groups(group)._2
  }

  def end(name: String): Int = end(groupIndex(name))

  private def loadGroup(group: Int): Unit = {
    if (group < 0 || group > groupCount) {
      throw new IndexOutOfBoundsException(s"No group $group")
    }

    if (!hasMatch) {
      throw new IllegalStateException("No match found")
    }

    if (!(group == 0 || hasGroups)) {
      val ok = doMatch(
        start = groups(0)._1,
        end = groups(0)._2,
        nMatches = groups.length,
        anchor = lastAnchor.get
      )

      if (!ok) {
        throw new IllegalStateException("Cannot load groups")
      }

      hasGroups = true
    }
  }

  def pattern(): Pattern = this.pattern

  def reset(input: CharSequence): Matcher = {
    reset()
    inputSequence = input
    inputLength = input.length
    this
  }

  def reset(): Matcher = {
    appendPos = 0
    hasMatch = false
    hasGroups = false
    this
  }

  def region(start: Int, end: Int): Matcher = ???

  private[regex] def appendReplacement2(sb: StringBuffer,
                                        replacement: String,
                                        doGroups: Boolean): Matcher = {

    val s = start
    val e = end
    if (appendPos < s) {
      sb.append(inputSequence, appendPos, s)
    }
    appendPos = e

    if (doGroups) {
      val m =
        Pattern.compile("(\\$(\\d)|\\$\\{(\\w*)\\})").matcher(replacement)
      val sb2 = new StringBuffer()

      while (m.find()) {
        val digitGroup = m.group(2)
        val nameGroup  = m.group(3)

        if (digitGroup != null && !digitGroup.isEmpty) {
          m.appendReplacement2(sb2, group(digitGroup.toInt), doGroups = false)
        } else if (nameGroup != null && !nameGroup.isEmpty) {
          m.appendReplacement2(sb2, group(nameGroup), doGroups = false)
        }
      }
      m.appendTail(sb2)
      sb.append(sb2)
    } else {
      sb.append(replacement)
    }

    this
  }

  def appendReplacement(sb: StringBuffer, replacement: String): Matcher = {
    appendReplacement2(sb, replacement, doGroups = true)
  }

  def appendTail(sb: StringBuffer): StringBuffer = {
    sb.append(inputSequence, appendPos, inputLength)
  }

  private[regex] def substring(start: Int, end: Int): String =
    inputSequence.subSequence(start, end).toString

  private def noLookAhead(methodName: String): Nothing =
    throw new Exception(
      s"$methodName is not defined since we don't support lookaheads")

  def useTransparentBounds(b: Boolean): Matcher =
    noLookAhead("useTransparentBounds")
  def hasTransparentBounds(): Boolean = noLookAhead("hasTransparentBounds")
}
