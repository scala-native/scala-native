package java.util
package regex

import scalanative.regex.{Matcher => rMatcher}

// Inspired & informed by:
// https://github.com/google/re2j/blob/master/java/com/google/re2j/Matcher.java

object Matcher {

  def quoteReplacement(s: String): String = rMatcher.quoteReplacement(s)
}

final class Matcher private[regex] (
    var _pattern: Pattern,
    var _inputSequence: CharSequence
) extends MatchResult {

  private val underlying = new rMatcher(_pattern.compiled, _inputSequence)

  private var anchoringBoundsInUse = true

  private def noLookAhead(methodName: String): Nothing =
    throw new UnsupportedOperationException(
      s"$methodName is not supported due to unsupported lookaheads."
    )

// Public interface

  def appendReplacement(sb: StringBuffer, replacement: String): Matcher = {
    underlying.appendReplacement(sb, replacement)
    this
  }

  def appendTail(sb: StringBuffer): StringBuffer = underlying.appendTail(sb)

  def end(): Int = end(0)

  def end(group: Int): Int = underlying.end(group)

  def end(name: String): Int = underlying.end(name)

  def find(): Boolean = underlying.find()

  def find(start: Int): Boolean = underlying.find(start)

  def group(): String = group(0)

  def group(group: Int): String = underlying.group(group)

  def group(name: String): String = underlying.group(name)

  def groupCount(): Int = underlying.groupCount()

  def hasAnchoringBounds(): Boolean = anchoringBoundsInUse

  def hasTransparentBounds(): Boolean = noLookAhead("hasTransparentBounds")

  def hitEnd(): Boolean = {
    throw new UnsupportedOperationException("hitEnd is not supported.")
  }

  def lookingAt(): Boolean = underlying.lookingAt()

  def matches(): Boolean = underlying.matches()

  def pattern(): Pattern = this._pattern

  def region(start: Int, end: Int): Matcher = {
    underlying.region(start, end)
    this
  }

  def regionEnd(): Int = underlying.regionEnd()

  def regionStart(): Int = underlying.regionStart()

  def replaceAll(replacement: String): String = {
    underlying.replaceAll(replacement)
  }

  def replaceFirst(replacement: String): String = {
    underlying.replaceFirst(replacement)
  }

  def requireEnd(): Boolean = {
    throw new UnsupportedOperationException("requireEnd is not supported.")
  }

  def reset(): Matcher = {
    underlying.reset()
    this
  }

  def reset(input: CharSequence): Matcher = {
    reset()
    _inputSequence = input
    this
  }

  def start(): Int = start(0)

  def start(group: Int): Int = underlying.start(group)

  def start(name: String): Int = underlying.start(name)

  def toMatchResult(): MatchResult = this.clone.asInstanceOf[MatchResult]

  override def toString = {

    val regStart = regionStart()
    val regEnd = regionEnd()

    val last =
      try {
        group()
      } catch {
        case e: IllegalStateException => ""
      }

    // Provide the same result as if running next line on the JVM.
    //   Pattern.compile("needle").matcher("haystack").toString
    // result:  java.util.regex.Matcher[pattern=needle region=0,8 lastmatch=]

    s"java.util.regex.Matcher[pattern=${_pattern}" +
      s" region=${regStart},${regEnd}" +
      s" lastmatch=${last}]"
  }

  def useAnchoringBounds(b: Boolean): Matcher =
    throw new UnsupportedOperationException(
      "useAnchoringBounds is not supported."
    )

  def usePattern(newPattern: Pattern): Matcher = {

    if ((newPattern == null) || (newPattern.compiled == null)) {
      throw new IllegalArgumentException(s"Pattern cannot be null")
    }

    underlying.usePattern(newPattern.compiled)
    _pattern = newPattern

    this
  }

  def useTransparentBounds(b: Boolean): Matcher =
    noLookAhead("useTransparentBounds")
}
