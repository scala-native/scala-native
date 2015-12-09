package native
package nir

sealed trait Bin
object Bin {
  final case object Add  extends Bin
  final case object Sub  extends Bin
  final case object Mul  extends Bin
  final case object Div  extends Bin
  final case object Mod  extends Bin
  final case object Shl  extends Bin
  final case object Lshr extends Bin
  final case object Ashr extends Bin
  final case object And  extends Bin
  final case object Or   extends Bin
  final case object Xor  extends Bin
  final case object Eq   extends Bin
  final case object Neq  extends Bin
  final case object Lt   extends Bin
  final case object Lte  extends Bin
  final case object Gt   extends Bin
  final case object Gte  extends Bin
}
