package java.nio.file

import java.nio.file.glob.GlobPattern
import java.util.regex.Pattern

object PathMatcherImpl {
  def apply(syntaxAndPattern: String): PathMatcher = {
    val colonIndex = syntaxAndPattern.indexOf(':')
    if (colonIndex == -1) {
      throw new IllegalArgumentException()
    }
    val syntax = syntaxAndPattern.substring(0, colonIndex)
    val pattern =
      syntaxAndPattern.substring(colonIndex + 1, syntaxAndPattern.length())

    if (syntax == "regex") new RegexPathMatcher(Pattern.compile(pattern))
    else if (syntax == "glob") new GlobPathMatcher(pattern)
    else throw new UnsupportedOperationException()
  }
}

private class RegexPathMatcher(pattern: Pattern) extends PathMatcher {
  override def matches(p: Path): Boolean =
    pattern.matcher(p.toString).matches()
}

private class GlobPathMatcher(pattern: String) extends PathMatcher {
  val globPattern = GlobPattern.compile(pattern)
  override def matches(p: Path): Boolean =
    globPattern.matcher(p.toString).matches()
}
