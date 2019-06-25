// Copyright 2010 Google Inc. All Rights Reserved.

package scala.scalanative
package regex

// A stateful iterator that interprets a regex {@code Pattern} on a
// specific input.  Its interface mimics the JDK 1.4.2
// {@code java.util.regex.Matcher}.
//
// <p>Conceptually, a Matcher consists of four parts:
// <ol>
//   <li>A compiled regular expression {@code Pattern}, set at
//   construction and fixed for the lifetime of the matcher.</li>
//
//   <li>The remainder of the input string, set at construction or
//   {@link #reset()} and advanced by each match operation such as
//   {@link #find}, {@link #matches} or {@link #lookingAt}.</li>
//
//   <li>The current match information, accessible via {@link #start},
//   {@link #end}, and {@link #group}, and updated by each match
//   operation.</li>
//
//   <li>The append position, used and advanced by
//   {@link #appendReplacement} and {@link #appendTail} if performing a
//   search and replace from the input to an external {@code StringBuffer}.
//
// </ol>
//
// <p>See the <a href="package.html">package-level
// documentation</a> for an overview of how to use this API.</p>
//
// @author rsc@google.com (Russ Cox)
final class Matcher private (private var _pattern: Pattern) {
  if (_pattern == null) {
    throw new NullPointerException("pattern is null")
  }

  // The number of submatches (groups) in the pattern.
  private var _groupCount: Int = _pattern.re2.numberOfCapturingGroups()

  private def clearAllGroups() = {
    // All groups will be set to indicate no or null match.
    //
    // Amortized _groupCount is likely to be small, say 1, 2, or n < 5, so
    // calling out to memset() is unlikely to be worth the overhead.

    for (i <- 0 until _groups.length) {
      _groups(i) = -1
    }
  }

  private def createGroups(nGroups: Int): Array[Int] = {
    Array.fill(2 * (nGroups + 1))(-1) // All groups now return null.
  }

  // The group indexes, in [start, end) pairs.	Zeroth pair is overall match.
  // By convention a pair (-1, -1) indicates no or null match.
  private var _groups: Array[Int] = createGroups(_groupCount)

  private var _inputSequence: CharSequence = ""

  // The input length in UTF16 codes.
  private var _inputLength: Int = _

  // The append position: where the next append should start.
  private var _appendPos: Int = _

  // Is there a current match?
  private var _hasMatch: Boolean = _

  // Have we found the submatches (groups) of the current match?
  // group[0], group[1] are set regardless.
  private var _hasGroups: Boolean = _

  // The anchor flag to use when repeating the match to find subgroups.
  private var _anchorFlag: Int = _

  private var _lastMatchStart = 0
  private var _lastMatchEnd   = 0

  private var _regionStart = 0
  private var _regionEnd   = 0

  // Creates a new {@code Matcher} with the given pattern and input.
  def this(pattern: Pattern, input: CharSequence) = {
    this(pattern)
    _inputSequence = input
    _inputLength = input.length()
    _regionEnd = _inputSequence.length
  }

  // Returns the {@code Pattern} associated with this {@code Matcher}.
  def pattern(): Pattern = _pattern

  // Resets the {@code Matcher}, rewinding input and
  // discarding any match information.
  //
  // @return the {@code Matcher} itself, for chained method calls
  def reset(): Matcher = {
    _appendPos = 0
    _hasMatch = false
    _hasGroups = false
    _lastMatchStart = 0
    _lastMatchEnd = 0
    _regionStart = 0
    _regionEnd = _inputSequence.length
    this
  }

  // Resets the {@code Matcher} and changes the input.
  //
  // @param input the new input string
  // @return the {@code Matcher} itself, for chained method calls
  def reset(input: CharSequence): Matcher = {
    if (input == null) {
      throw new NullPointerException("input is null")
    }

    _inputSequence = input
    _inputLength = input.length()
    reset()
    this
  }

  // Returns the start position of the most recent match.
  //
  // @throws IllegalStateException if there is no match
  def start(): Int = start(0)

  // Returns the end position of the most recent match.
  //
  // @throws IllegalStateException if there is no match
  def end(): Int = end(0)

  // Returns the start position of a subgroup of the most recent match.
  //
  // @param group the group index 0 is the overall match
  // @throws IllegalStateException if there is no match
  // @throws IndexOutOfBoundsException
  //   if {@code group < 0} or {@code group > groupCount()}
  def start(group: Int): Int = {
    loadGroup(group)
    _groups(2 * group)
  }

  // Returns the end position of a subgroup of the most recent match.
  //
  // @param group the group index 0 is the overall match
  // @throws IllegalStateException if there is no match
  // @throws IndexOutOfBoundsException
  //   if {@code group < 0} or {@code group > groupCount()}
  def end(group: Int): Int = {
    loadGroup(group)
    _groups(2 * group + 1)
  }

  def start(name: String): Int    = start(groupIndex(name))
  def end(name: String): Int      = end(groupIndex(name))
  def group(name: String): String = group(groupIndex(name))

  private def groupIndex(name: String): Int = {
    val pos = _pattern.re2.findNamedCapturingGroups(name)
    if (pos == -1) {
      throw new IllegalArgumentException(s"No group with name <$name>")
    }
    pos
  }

  def region(start: Int, end: Int): Matcher = {

    val inLength = _inputSequence.length

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

  // Returns the most recent match.
  //
  // @throws IllegalStateException if there is no match
  def group(): String = group(0)

  // Returns the subgroup of the most recent match.
  //
  // @throws IllegalStateException if there is no match
  // @throws IndexOutOfBoundsException if {@code group < 0}
  //   or {@code group > groupCount()}
  def group(group: Int): String = {

    if ((group > groupCount()) || (group < 0)) {
      throw new IndexOutOfBoundsException(s"No group ${group}")
    }

    val start = this.start(group)
    val end   = this.end(group)
    if (start < 0 && end < 0) {
      // Means the subpattern didn't get matched at all.
      return null
    }
    substring(start, end)
  }

  // Returns the number of subgroups in this pattern.
  //
  // @return the number of subgroups the overall match (group 0) does not count
  def groupCount(): Int = _groupCount

  // Helper: finds subgroup information if needed for group.
  private def loadGroup(group: Int): Unit = {

    if (!_hasMatch) {
      throw new IllegalStateException("No match found")
    }

    if ((group < 0) || (group > _groupCount)) {
      throw new IndexOutOfBoundsException(s"No group ${group}")
    }

    if (group == 0 || _hasGroups) {
      return
    }

    // Include the character after the matched text (if there is one).
    // This is necessary in the case of inputSequence abc and pattern
    // (a)(b$)?(b)? . If we do pass in the trailing c,
    // the groups evaluate to new String[] {"ab", "a", null, "b" }
    // If we don't, they evaluate to new String[] {"ab", "a", "b", null}
    // We know it won't affect the total matched because the previous call
    // to match included the extra character, and it was not matched then.
    var end = _groups(1) + 1
    if (end > _inputLength) {
      end = _inputLength
    }

    val ok = _pattern.re2.match_(_inputSequence,
                                 _groups(0),
                                 end,
                                 _anchorFlag,
                                 _groups,
                                 1 + _groupCount)
    // Must match - hasMatch says that the last call with these
    // parameters worked just fine.
    if (!ok) {
      throw new IllegalStateException("inconsistency in matching group data")
    } else {
      _hasGroups = true
    }
  }

  // Matches the entire input against the pattern (anchored start and end).
  // If there is a match, {@code matches} sets the match state to describe it.
  //
  // @return true if the entire input matches the pattern
  def matches(): Boolean = genMatch(0, RE2.ANCHOR_BOTH)

  // Matches the beginning of input against the pattern (anchored start).
  // If there is a match, {@code lookingAt} sets the match state to describe it.
  //
  // @return true if the beginning of the input matches the pattern
  def lookingAt(): Boolean = genMatch(0, RE2.ANCHOR_START)

  // Matches the input against the pattern (unanchored).
  // The search begins at the end of the last match, or else the beginning
  // of the input.
  // If there is a match, {@code find} sets the match state to describe it.
  //
  // @return true if it finds a match
  def find(): Boolean = {
    var start = 0
    if (_hasMatch) {
      start = _groups(1)
      if (_groups(0) == _groups(1)) { // empty match - nudge forward
        start += 1
      }
    }
    genMatch(start, RE2.UNANCHORED)
  }

  // Matches the input against the pattern (unanchored),
  // starting at a specified position.
  // If there is a match, {@code find} sets the match state to describe it.
  //
  // @param start the input position where the search begins
  // @return true if it finds a match
  // @throws IndexOutOfBoundsException if start is not a valid input position
  def find(start: Int): Boolean = {
    if (start < 0 || start > _inputLength) {
      throw new IndexOutOfBoundsException("start index out of bounds: " + start)
    }
    reset()
    genMatch(start, 0)
  }

  // Helper: does match starting at start, with RE2 anchor flag.
  private def genMatch(startByte: Int, anchor: Int): Boolean = {
    val ok = _pattern.re2.match_(_inputSequence,
                                 startByte,
                                 _inputLength,
                                 anchor,
                                 _groups,
                                 1)
    if (!ok) {
      false
    } else {
      _hasMatch = true
      _hasGroups = false
      _anchorFlag = anchor
      _lastMatchStart = start()
      _lastMatchEnd = end()
      true
    }
  }

  // Helper: return substring for [start, end).
  def substring(start: Int, end: Int): String =
    // This is fast for both StringBuilder and String.
    _inputSequence.subSequence(start, end).toString()

  // Helper for Pattern: return input length.
  def inputLength(): Int =
    _inputLength

  // Appends to {@code sb} two strings: the text from the append position up
  // to the beginning of the most recent match, and then the replacement with
  // submatch groups substituted for references of the form {@code $n}, where
  // {@code n} is the group number in decimal.	It advances the append position
  // to the position where the most recent match ended.
  //
  // <p>To embed a literal {@code $}, use \$ (actually {@code "\\$"} with string
  // escapes).	The escape is only necessary when {@code $} is followed by a
  // digit, but it is always allowed.  Only {@code $} and {@code \} need
  // escaping, but any character can be escaped.
  //
  // <p>The group number {@code n} in {@code $n} is always at least one digit
  // and expands to use more digits as long as the resulting number is a
  // valid group number for this pattern.  To cut it off earlier, escape the
  // first digit that should not be used.
  //
  // @param sb the {@link StringBuffer} to append to
  // @param replacement the replacement string
  // @return the {@code Matcher} itself, for chained method calls
  // @throws IllegalStateException if there was no most recent match
  // @throws IndexOutOfBoundsException if replacement refers to an invalid group
  // @throws IllegalArgumentException if replacement has unclosed named group
  def appendReplacement(sb: StringBuffer, replacement: String): Matcher = {
    val s = _lastMatchStart
    val e = _lastMatchEnd

    if (_appendPos < s) {
      sb.append(substring(_appendPos, s))
    }
    _appendPos = e
    var last = 0
    var i    = 0
    val m    = replacement.length()
    while (i < m - 1) {
      if (replacement.charAt(i) == '\\') {
        if (last < i) {
          sb.append(replacement.substring(last, i))
        }
        i += 1
        last = i
      } else if (replacement.charAt(i) == '$') {
        var c = replacement.charAt(i + 1)
        if ('0' <= c && c <= '9') {
          var n = c - '0'
          if (last < i) {
            sb.append(replacement.substring(last, i))
          }
          i += 2
          var break = false
          while (!break && i < m) {
            c = replacement.charAt(i)
            if (c < '0' || c > '9' || n * 10 + c - '0' > _groupCount) {
              break = true
            } else {
              n = n * 10 + c - '0'
              i += 1
            }
          }
          if (n > _groupCount) {
            throw new IndexOutOfBoundsException("n > number of groups: " + n)
          }
          val group = this.group(n)
          if (group != null) {
            sb.append(group)
          }
          last = i
          i -= 1
        } else if (c == '{') {
          if (last < i) {
            sb.append(replacement.substring(last, i))
          }
          i += 1 // '{'
          var j = i + 1
          while (j < replacement.length && replacement.charAt(j) != '}' && replacement
                   .charAt(j) != ' ') {
            j += 1
          }
          if (j == replacement.length || replacement.charAt(j) == ' ') {
            throw new IllegalArgumentException(
              "named capturing group is missing trailing '}'")
          }
          val groupName = replacement.substring(i + 1, j)
          sb.append(this.group(groupName))
          i += 1 // '}'
          last = j + 1
        }
      }
      i += 1
    }
    if (last < m) {
      sb.append(replacement.substring(last, m))
    }
    this
  }

  // Appends to {@code sb} the substring of the input from the
  // append position to the end of the input.
  //
  // @param sb the {@link StringBuffer} to append to
  // @return the argument {@code sb}, for method chaining
  def appendTail(sb: StringBuffer): StringBuffer = {
    sb.append(substring(_appendPos, _inputLength))
    sb
  }

  private def noLookAhead(methodName: String): Nothing =
    throw new Exception(
      s"$methodName is not defined since we don't support lookaheads")

  def useTransparentBounds(b: Boolean): Matcher =
    noLookAhead("useTransparentBounds")
  def hasTransparentBounds(): Boolean = noLookAhead("hasTransparentBounds")

  // Returns the input with all matches replaced by {@code replacement},
  // interpreted as for {@code appendReplacement}.
  //
  // @param replacement the replacement string
  // @return the input string with the matches replaced
  // @throws IndexOutOfBoundsException if replacement refers to an invalid group
  def replaceAll(replacement: String): String =
    replace(replacement, true)

  // Returns the input with the first match replaced by {@code replacement},
  // interpreted as for {@code appendReplacement}.
  //
  // @param replacement the replacement string
  // @return the input string with the first match replaced
  // @throws IndexOutOfBoundsException if replacement refers to an invalid group
  def replaceFirst(replacement: String): String =
    replace(replacement, false)

  // Helper: replaceAll/replaceFirst hybrid.
  private def replace(replacement: String, all: Boolean): String = {
    reset()
    val sb    = new StringBuffer()
    var break = false
    while (!break && find()) {
      appendReplacement(sb, replacement)
      if (!all) {
        break = true
      }
    }
    appendTail(sb)
    sb.toString()
  }

  def usePattern(newPattern: Pattern): Matcher = {
    // Per JVM documentation, current search position & last append position
    // do not change. region behavior is not mentioned, but JVM preserves
    // the region.
    //
    // Info on groups from lastmatch is lost.

    if (newPattern == null) {
      throw new IllegalArgumentException()
    }

    _pattern = newPattern

    val oldGroupCount = _groupCount
    _groupCount = _pattern.re2.numberOfCapturingGroups()

    // Reuse existing _groups if _groupCount stayed the same
    // or decreased. Otherwise, a larger _groups is required.

    if (_groupCount <= oldGroupCount) {
      clearAllGroups()
    } else {
      _groups = createGroups(_groupCount)
    }

    this
  }

}

object Matcher {

  // Quotes '\' and '$' in {@code s}, so that the returned string could be
  // used in {@link #appendReplacement} as a literal replacement of {@code s}.
  //
  // @param s the string to be quoted
  // @return the quoted string
  def quoteReplacement(s: String): String = {
    if (s.indexOf('\\') < 0 && s.indexOf('$') < 0) {
      return s
    }
    val sb = new StringBuilder()
    var i  = 0
    while (i < s.length()) {
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
