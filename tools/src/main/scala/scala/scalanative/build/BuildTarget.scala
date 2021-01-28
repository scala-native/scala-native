package scala.scalanative.build

sealed trait BuildTarget

object BuildTarget {
  private[scalanative] case object Application   extends BuildTarget
  private[scalanative] case object SharedLibrary extends BuildTarget

  /** Link code as application */
  def application: BuildTarget = Application

  /** Link code as shared library */
  def sharedLibrary: BuildTarget = SharedLibrary

  /** The default build target. */
  def default: BuildTarget = application

}
