package scala.scalanative.build

/** Base `trait` representing an Application, or a Static or a Dynamic Library
 */
sealed trait BuildTarget

/** Utility to create a [[BuildTarget]] object */
object BuildTarget {
  private[scalanative] case object Application extends BuildTarget
  private[scalanative] sealed trait Library extends BuildTarget
  private[scalanative] case object LibraryDynamic extends Library
  private[scalanative] case object LibraryStatic extends Library

  /** Link code as application */
  def application: BuildTarget = Application

  /** Link code as shared/dynamic library */
  def libraryDynamic: BuildTarget = LibraryDynamic

  /** Link code as static library */
  def libraryStatic: BuildTarget = LibraryStatic

  /** The default build target. */
  def default: BuildTarget = application

}
