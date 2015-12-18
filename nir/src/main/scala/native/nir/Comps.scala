package native
package nir

sealed abstract class Comp
object Comp {
  final case object Eq   extends Comp
  final case object Neq  extends Comp
  final case object Lt   extends Comp
  final case object Lte  extends Comp
  final case object Gt   extends Comp
  final case object Gte  extends Comp
}
