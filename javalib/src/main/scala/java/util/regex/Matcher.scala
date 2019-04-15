package java.util
package regex

import scala.annotation.tailrec

import scalanative.re2s.RE2

// Inspired & informed by:
// https://github.com/google/re2j/blob/master/java/com/google/re2j/Matcher.java

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
                                    var _inputSequence: CharSequence)
    extends MatchResult {

  private var currentPattern = _pattern

  private var hasMatch  = false
  private var hasGroups = false
  private var appendPos = 0

  private var _regionStart = 0
  private var _regionEnd   = _inputSequence.length

  private var _groupCount = currentPattern.compiled.groupCount()

  // This default differs from JVM but describes actual SN behavior.
  // That behavior is a bug, awaiting a fix.
  private var anchoringBoundsInUse = false

  private def clearGroup0(g: Array[Int]) = {
    g(0) = -1
    g(1) = -1
  }

  private def createGroups(nGroups: Int): Array[Int] = {

    // +1 is for groups(0). It holds full match, but is not part of groupCount.
    // "groups" is an old-style 2-D row-major array, where Array(index) holds
    // start for group number index and Array(index + 1) holds the
    // corresponding end. In SN, 1-D arrays are much more efficient, albeit
    // much less elegant.

    val result = new Array[Int](2 * (nGroups + 1))

    // No lastmatch at creation, that is group(0) is coded as null,
    // not null string.
    // groups n > 0 have undefined values until after a loadGroup() executes.
    clearGroup0(result)

    result
  }

  private def clearGroups(g: Array[Int]) = {
    Arrays.fill(groups, 0, g.length, -1) // Now, all groups return null.
  }

  // The group indexes, in [start, end) pairs.  Zeroth pair is overall match.
  private var groups = createGroups(_groupCount)

  private def groupStartIndex(groupNum: Int) = groups(groupNum * 2)
  private def groupEndIndex(groupNum: Int)   = groups((groupNum * 2) + 1)

  private var lastAnchor: Option[RE2.anchor_t] = None

  private[regex] def inputLength = _inputSequence.length

  private[regex] def appendReplacement2(sb: StringBuffer,
                                        replacement: String,
                                        doGroups: Boolean): Matcher = {
    val s = start
    val e = end
    if (appendPos < s) {
      sb.append(_inputSequence, appendPos, s)
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

  private def doMatch(start: Int,
                      end: Int,
                      nMatches: Int,
                      anchor: RE2.anchor_t): Boolean = {

    currentPattern.compiled.re2
      .match_(_inputSequence, start, end, anchor, groups, nMatches)
  }

  private def genMatch(start: Int, end: Int, anchor: RE2.anchor_t): Boolean = {

    val ok = doMatch(start, end, 1, anchor)

    // On error, group(0) will have been set to -1, -1
    // meaning null (not null string). toString() must be able to
    // convert null to null String.
    //
    // On success group(0) holds the indices for the match.

    hasGroups = false // drop groups on both  happy or sad paths.

    if (ok) {
      hasMatch = true
      lastAnchor = Some(anchor)
      // Do not touch  appendPos. It is handled elsewhere.
    }

    ok
  }

  private def groupIndex(name: String): Int = {
    currentPattern.compiled.re2.findNamedCapturingGroups(name)
  }

  private def lastMatchString(s: Int, e: Int): String = {
    // s & e are start and end indices of groups indexed data structure.
    if (s < 0 && e < 0) {
      null
    } else if (s == 0 && e == 0) {
      ""
    } else {
      substring(s, e).toString
    }
  }

  private def loadGroup(group: Int): Unit = {

    if (group < 0 || group > groupCount) {
      throw new IndexOutOfBoundsException(s"No group $group")
    }

    if (!hasMatch) {
      throw new IllegalStateException("No match found")
    }

    // See comments in re2s.Matcher.scala method loadGroups
    // about the particularly tricky handling needed here
    // so that pattern "(a)(b$)?(b)?" will succeed.

    if (!(group == 0 || hasGroups)) {

      var end = groups(1) + 1

      if (end > inputLength) {
        end = inputLength
      }

      val ok = doMatch(
        start = groups(0),
        end = end,
        nMatches = _groupCount + 1,
        anchor = lastAnchor.get
      )

      if (!ok) {
        throw new IllegalStateException("Can not load groups")
      }

      hasGroups = true
    }
  }

  private def noLookAhead(methodName: String): Nothing =
    throw new UnsupportedOperationException(
      s"$methodName is not supported due to unsupported lookaheads.")

  private[regex] def substring(start: Int, end: Int): String =
    _inputSequence.subSequence(start, end).toString

// Public interface

  def appendReplacement(sb: StringBuffer, replacement: String): Matcher = {
    appendReplacement2(sb, replacement, doGroups = true)
  }

  def appendTail(sb: StringBuffer): StringBuffer = {
    sb.append(_inputSequence, appendPos, inputLength)
  }

  def end(): Int = end(0)

  def end(group: Int): Int = {
    loadGroup(group)
    groupEndIndex(group)
  }

  def end(name: String): Int = end(groupIndex(name))

  def find(): Boolean = {

    var startIndex = _regionStart

    if (hasMatch) {
      startIndex = end
      if (start == end) {
        startIndex += 1
      }
    }

    genMatch(startIndex, _regionEnd, RE2.UNANCHORED)
  }

  def find(start: Int): Boolean = {

    if ((start < 0) || (start > inputLength)) {
      throw new IndexOutOfBoundsException("Illegal start index")
    }

    reset()

    // If start < _regionStart it is known at this point that match will fail.
    // Do the match anyway to properly set Matcher data structures as failed.

    genMatch(start, _regionEnd, RE2.UNANCHORED)
  }

  def group(): String = group(0)

  def group(group: Int): String = {
    lastMatchString(start(group), end(group))
  }

  def group(name: String): String = group(groupIndex(name))

  def groupCount: Int = _groupCount

  def hasAnchoringBounds(): Boolean = anchoringBoundsInUse

  def hasTransparentBounds(): Boolean = noLookAhead("hasTransparentBounds")

  def hitEnd(): Boolean =
    throw new UnsupportedOperationException("hitEnd is not supported.")

  def lookingAt(): Boolean =
    genMatch(_regionStart, _regionEnd, RE2.ANCHOR_START)

  def matches(): Boolean = {

    def getAnchor(): RE2.anchor_t = {

      val startAnchor = if (_regionStart == 0) {
        RE2.ANCHOR_START
      } else {
        RE2.UNANCHORED
      }

      val anchor =
        if ((startAnchor == RE2.ANCHOR_START)
            && (_regionEnd == _inputSequence.length)) {
          RE2.ANCHOR_BOTH
        } else {
          RE2.UNANCHORED
        }

      anchor
    }

    def checkMatch(anchor: RE2.anchor_t): Boolean = {
      ((anchor == RE2.ANCHOR_BOTH) ||
      ((groups(0) == _regionStart) && (groups(1) == _regionEnd)))
    }

    val anchor = getAnchor()

    val ok = genMatch(_regionStart, _regionEnd, anchor)

    ok && checkMatch(anchor)
  }

  def pattern(): Pattern = this.currentPattern

  def region(start: Int, end: Int): Matcher = {

    val inLength = inputLength

    if ((start < 0) || (start > inLength)) {
      throw new IndexOutOfBoundsException("start")
    }

    if ((end < 0) || (end > inLength)) {
      throw new IndexOutOfBoundsException("end")
    }

    if (start > end) {
      throw new IndexOutOfBoundsException("start > end")
    }

    _regionStart = start
    _regionEnd = end

    this
  }

  def regionEnd(): Int = _regionEnd

  def regionStart(): Int = _regionStart

  def replaceAll(replacement: String): String = {
    currentPattern.compiled
      .matcher(_inputSequence)
      .replaceAll(replacement)
  }

  def replaceFirst(replacement: String): String = {
    currentPattern.compiled
      .matcher(_inputSequence)
      .replaceFirst(replacement)
  }

  def requireEnd(): Boolean =
    throw new UnsupportedOperationException("requireEnd is not supported.")

  def reset(): Matcher = {

    // does not change re behavior, resets its memory
    currentPattern.compiled.reset()

    appendPos = 0
    hasMatch = false
    hasGroups = false

    _regionStart = 0
    _regionEnd = _inputSequence.length

    this
  }

  def reset(input: CharSequence): Matcher = {
    reset()
    _inputSequence = input
    this
  }

  def start(): Int = start(0)

  def start(group: Int): Int = {
    loadGroup(group)
    groupStartIndex(group)
  }

  def start(name: String): Int = start(groupIndex(name))

  def toMatchResult(): MatchResult =
    throw new UnsupportedOperationException("toMatchResult is not supported.")

  override def toString = {

    val lastmatch = {
      val lm = lastMatchString(groups(0), groups(1))
      if (lm == null) "" else lm
    }

    // The hideous string interpolation results in JVM look-alike:
    // Pattern.compile("needle").matcher("haystack").toString
    // java.util.regex.Matcher[pattern=needle region=0,8 lastmatch=]

    s"java.util.regex.Matcher[pattern=${currentPattern}" +
      s" region=${regionStart},${regionEnd}" +
      s" lastmatch=${lastmatch}]"
  }

  def useAnchoringBounds(b: Boolean): Matcher =
    throw new UnsupportedOperationException(
      "useAnchoringBounds is not supported.")

  def usePattern(newPattern: Pattern): Matcher = {

    if (newPattern == null) {
      throw new IllegalArgumentException()
    }

    // Per JVM documentation, current position, last append position, &
    // do not change. region behavior is not mentioned, but JVM preserves
    // the region.
    //
    // Info on groups from lastmatch is lost.

    currentPattern = newPattern

    val oldGroupCount = _groupCount
    _groupCount = currentPattern.compiled.groupCount()

    // Need new "groups" data structure if size changed. Else reuse current.
    if (_groupCount != oldGroupCount) {
      groups = createGroups(_groupCount)
      clearGroups(groups)
    }

    this
  }

  def useTransparentBounds(b: Boolean): Matcher =
    noLookAhead("useTransparentBounds")
}
