package scala.scalanative
package build

/** Compilation mode. Either of the two:
 *
 *  * Debug mode. Most optimizations are turned off to get the best linking
 *  speed. This mode is the default and is preferred for iterative development.
 *
 *  * Release mode. Runs all the optimizations but may take substantially longer
 *  to link the application.
 *
 *  Additional compilation modes might be added in the future.
 *
 *  @param name
 *    name of the compilation mode
 */
sealed abstract class Mode private (val name: String) {
  override def toString: String = name
}
object Mode {
  private[scalanative] case object Debug extends Mode("debug")
  private[scalanative] sealed abstract class Release(name: String)
      extends Mode(name)
  private[scalanative] case object ReleaseFast extends Release("release-fast")
  private[scalanative] case object ReleaseSize extends Release("release-size")
  private[scalanative] case object ReleaseFull extends Release("release-full")

  /** Debug compilation mode. */
  def debug: Mode = Debug

  /** Release compilation mode. */
  def release: Mode = ReleaseFull

  /** Release compilation mode that's still fast to compile. */
  def releaseFast: Mode = ReleaseFast

  /** Release compilation mode optimize for reduced size that's still fast to
   *  compile.
   */
  def releaseSize: Mode = ReleaseSize

  /** Release compilation mode that's uses full set of optimizations. */
  def releaseFull: Mode = ReleaseFull

  /** Default compilation mode. */
  def default: Mode = Debug

  /** Get a compilation mode with given name. */
  def apply(name: String): Mode = name match {
    case "debug" =>
      Debug
    case "release" =>
      ReleaseFull
    case "release-fast" =>
      ReleaseFast
    case "release-size" =>
      ReleaseSize
    case "release-full" =>
      ReleaseFull
    case value =>
      throw new IllegalArgumentException(s"Unknown mode: '$value'")
  }
}
