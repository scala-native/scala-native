package native
package nir

sealed abstract class Advice
object Advice {
  final case object No   extends Advice
  final case object Hint extends Advice
  final case object Must extends Advice
}
