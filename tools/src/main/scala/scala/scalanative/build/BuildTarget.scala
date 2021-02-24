package scala.scalanative.build

sealed trait BuildTarget

object BuildTarget {
  private[scalanative] case object Application    extends BuildTarget
  private[scalanative] case object LibraryDynamic extends BuildTarget

  /** Link code as application */
  def application: BuildTarget = Application

  /** Link code as shared/dynamic library */
  def libraryDynamic: BuildTarget = LibraryDynamic

  /** The default build target. */
  def default: BuildTarget = application

}
