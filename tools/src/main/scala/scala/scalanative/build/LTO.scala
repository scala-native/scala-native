package scala.scalanative
package build

/** Link Time Optimization (LTO) mode to be used during the build linking phase.
 *
 *  @param name
 *    the name of the [[LTO]] mode
 */
sealed abstract class LTO private (val name: String) {

  /** The name of the [[LTO]] object
   *
   *  @return
   *    the [[LTO]] name
   */
  override def toString: String = name
}

/** Utility to create [[LTO]] objects to control Link Time Optimization (LTO)
 *  which is used to pass the correct option to the linker in the `link` phase.
 */
object LTO {

  /** LTO disabled */
  private[scalanative] case object None extends LTO("none")

  /** LTO mode uses ThinLTO */
  private[scalanative] case object Thin extends LTO("thin")

  /** LTO mode uses standard LTO compilation */
  private[scalanative] case object Full extends LTO("full")

  /** LTO disabled mode [[#none]] */
  def none: LTO = None

  /** LTO `thin` mode [[#thin]] */
  def thin: LTO = Thin

  /** LTO `full` mode [[#full]] */
  def full: LTO = Full

  /** Default LTO mode, [[#none]]. */
  def default: LTO = None

  /** Create an [[LTO]] object
   *
   *  @param name
   *    the [[LTO]] as a string
   *  @return
   *    the [[LTO]] object
   */
  def apply(name: String): LTO = name.toLowerCase match {
    case "none" => None
    case "thin" => Thin
    case "full" => Full
    case value =>
      throw new IllegalArgumentException(s"Unknown LTO: '$value'")
  }
}
