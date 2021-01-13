package scala.scalanative.build

/**
 * Link Time Optimization (LTO) mode to be used when during a release build.
 */
sealed abstract class LTO private (val name: String) {
  override def toString: String = name
}

object LTO {

  /** LTO disabled */
  private[scalanative] final case object None extends LTO("none")

  /** LTO mode uses ThinLTO */
  private[scalanative] final case object Thin extends LTO("thin")

  /** LTO mode uses standard LTO compilation */
  private[scalanative] final case object Full extends LTO("full")

  def none: LTO = None
  def thin: LTO = Thin
  def full: LTO = Full

  /** Default LTO mode. */
  def default: LTO = None

  def apply(name: String): LTO = name.toLowerCase match {
    case "none" => None
    case "thin" => Thin
    case "full" => Full
    case value =>
      throw new IllegalArgumentException(s"Unknown LTO: '$value'")
  }
}
