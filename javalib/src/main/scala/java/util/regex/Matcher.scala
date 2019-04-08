package java.util
package regex

import cre2h._
import scalanative.native._
import scalanative.libc._, stdlib._, stdio._, string._

import scala.annotation.tailrec

// Inspired by: https://github.com/google/re2j/blob/master/java/com/google/re2j/Matcher.java

object Matcher {

  def quoteReplacement(s: String): String = {
    // Every backslash ('\') character becomes two, that is, quoted.
    // Do not quote dollar signs ($) or any other character
    // (dot, asterisk, etc).
    //
    // According to the cre2 documentation, only the backslash before
    // a digit (\0, \1, ..., \9) need be quoted. In practice, cre2
    // fails if there are any solo backslashes in the supposedly literal
    // replacement string. Quote them all and be done with it.

    // Build the return string manually, rather than using a replace
    // method from some other class, such as String or Regex, to avoid
    // any chance of unwanted recursion.

    @tailrec
    def qrLoop(s: String, index: Int, sb: StringBuilder): StringBuilder = {

      if (index < 0) {
        if (s.isEmpty) sb else sb.append(s) // final slice, if any.
      } else {
        val (prefix, suffix) = s.splitAt(index + 1)
        sb.append(prefix) += '\\'
        if (suffix.isEmpty) sb else qrLoop(suffix, suffix.indexOf('\\'), sb)
      }
    }

    val idx = s.indexOf('\\')

    if (idx < 0) {
      s
    } else {
      val sb = new StringBuilder(s.length + 1)
      qrLoop(s, idx, sb).toString
    }
  }
}

final class Matcher private[regex] (var _pattern: Pattern,
                                    var inputSequence: CharSequence)
    extends MatchResult {

  private def withRE2Regex[A](f: RE2RegExpOps => A): A =
    _pattern.withRE2Regex(f)

  private var hasMatch  = false
  private var hasGroups = false
  private var appendPos = 0

  private var groups = withRE2Regex { re2op =>
    val regex = re2op.ptr
    Array.ofDim[(Int, Int)](cre2.numCapturingGroups(regex) + 1)
  }

  private var lastAnchor: Option[cre2.anchor_t] = None

  private[regex] def inputLength = inputSequence.length

  def matches(): Boolean = genMatch(0, ANCHOR_BOTH)

  def lookingAt(): Boolean = genMatch(0, ANCHOR_START)

  def find(start: Int): Boolean = {

    if ((start < 0) || (start > inputLength)) {
      throw new IndexOutOfBoundsException("Illegal start index")
    }

    reset()
    genMatch(start, UNANCHORED)
  }

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
      val instr   = inputSequence.toString
      val inre2   = alloc[cre2.string_t]
      toRE2String(instr, inre2)
      // calculate byte-array indices from string indices
      val startpos = instr.take(start).getBytes().length
      val endpos   = instr.take(end).getBytes().length

      val ok = withRE2Regex { re2 =>
        val regex = re2.ptr
        cre2.matches(
          regex = regex,
          text = inre2.data,
          textlen = inre2.length,
          startpos = startpos,
          endpos = endpos,
          anchor = anchor,
          matches = matches,
          nMatches = nMatches
        ) == 1
      }

      if (ok) {
        var i = 0
        while (i < nMatches) {
          val m = matches + i
          groups(i) = if (m.data == null) {
            (-1, -1)
          } else if (m.length == 0) {
            (0, 0)
          } else {
            // Takes from inre2 until m...
            val before = alloc[cre2.string_t]
            before.data = inre2.data
            before.length = (m.data - inre2.data).toInt
            // ...to calculate `start` in String's index.
            val start = fromRE2String(before).length
            val end   = start + fromRE2String(m).length
            (start, end)
          }

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

      withRE2Regex { re2 =>
        val regex = re2.ptr
        if (global) cre2.globalReplace(regex, textAndTarget, rewrite)
        else cre2.replace(regex, textAndTarget, rewrite)
      }

      val res = fromRE2String(textAndTarget)

      res
    }

  def group(): String = group(0)

  def group(group: Int): String = {
    val startIndex = start(group)
    val endIndex   = end(group)

    if (startIndex < 0 && endIndex < 0) {
      null
    } else if (startIndex == 0 && endIndex == 0) {
      ""
    } else {
      inputSequence.subSequence(startIndex, endIndex).toString
    }
  }

  def group(name: String): String = group(groupIndex(name))

  private def groupIndex(name: String): Int =
    Zone { implicit z =>
      val pos = withRE2Regex { re2 =>
        val regex = re2.ptr
        cre2.findNamedCapturingGroups(regex, toCString(name))
      }
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

  def pattern(): Pattern = this._pattern

  def reset(input: CharSequence): Matcher = {
    reset()
    inputSequence = input
    this
  }

  def reset(): Matcher = {
    appendPos = 0
    hasMatch = false
    hasGroups = false
    this
  }

  @stub
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
