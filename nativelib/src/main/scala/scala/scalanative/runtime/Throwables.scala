// scalafmt: { maxColumn = 120}

package scala.scalanative
package runtime

/** An exception that is thrown whenever an undefined behavior happens in a checked mode.
 */
final class UndefinedBehaviorError(message: String) extends java.lang.Error(message) {
  def this() = this(null)
}
