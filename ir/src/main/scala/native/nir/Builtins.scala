package native
package nir

sealed trait Builtin
object Builtin {
  final case object Add  extends Builtin
  final case object Sub  extends Builtin
  final case object Mul  extends Builtin
  final case object Div  extends Builtin
  final case object Mod  extends Builtin
  final case object Shl  extends Builtin
  final case object Lshr extends Builtin
  final case object Ashr extends Builtin
  final case object And  extends Builtin
  final case object Or   extends Builtin
  final case object Xor  extends Builtin
  final case object Eq   extends Builtin
  final case object Neq  extends Builtin
  final case object Lt   extends Builtin
  final case object Lte  extends Builtin
  final case object Gt   extends Builtin
  final case object Gte  extends Builtin

  final case object Trunc    extends Builtin
  final case object Zext     extends Builtin
  final case object Sext     extends Builtin
  final case object Fptrunc  extends Builtin
  final case object Fpext    extends Builtin
  final case object Fptoui   extends Builtin
  final case object Fptosi   extends Builtin
  final case object Uitofp   extends Builtin
  final case object Sitofp   extends Builtin
  final case object Ptrtoint extends Builtin
  final case object Inttoptr extends Builtin
  final case object Bitcast  extends Builtin
}
