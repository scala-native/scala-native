package scala.scalanative.build

/** Compilation mode. Either of the two:
 *
 *  * Debug mode. Most optimizations are turned off
 *    to get the best linking speed. This mode is the
 *    default and is preferred for iterative development.
 *
 *  * Release mode. Runs all the optimizations but
 *    may take substantially longer to link the application.
 *
 *  Additional compilation modes might be added in the future.
 *
 *  @param name name of the compilation mode
 */
sealed abstract class Mode private (val name: String) {
  override def toString: String = name
}
object Mode {
  private[scalanative] final case object Debug   extends Mode("debug")
  private[scalanative] final case object Release extends Mode("release")

  /** Debug compilation mode. */
  def debug: Mode = Debug

  /** Release compilation mode. */
  def release: Mode = Release

  /** Default compilation mode. */
  def default: Mode = Debug

  /** Get a compilation mode with given name. */
  def apply(name: String): Mode = name match {
    case "debug" =>
      Debug
    case "release" =>
      Release
    case value =>
      throw new IllegalArgumentException(s"Unknown mode: '$value'")
  }
}
