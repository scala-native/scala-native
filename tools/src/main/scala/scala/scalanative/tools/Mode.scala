package scala.scalanative
package tools

sealed abstract class Mode
object Mode {
  final case object Debug   extends Mode
  final case object Release extends Mode
}
