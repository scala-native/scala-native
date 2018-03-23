package scala.scalanative.build

sealed abstract class Mode(val name: String)
object Mode {
  final case object Debug   extends Mode("debug")
  final case object Release extends Mode("release")

  /** The default mode. */
  def default: Mode = Debug

  /** Gets the corresponding `Mode`, given its name. */
  def apply(name: String): Mode = name match {
    case "debug"   => Debug
    case "release" => Release
    case value     => throw new IllegalArgumentException(s"Unknown mode: '$value'")
  }

}
