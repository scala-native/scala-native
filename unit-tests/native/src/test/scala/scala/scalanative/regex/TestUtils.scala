package scala.scalanative
package regex

// Provide simple implementations for missing test method(s).
// This routine might be in Scalatest but I, @LeeTibbert could
// not find it during a hasty search.

object TestUtils {

  def matcher(regex: String, text: String): Matcher = {
    Pattern.compile(regex).matcher(text)
  }
}
