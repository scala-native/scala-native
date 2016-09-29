package scala.scalanative
package nir

sealed abstract class Cf
object Cf {
  // low-level control
  final case object Unreachable                             extends Cf
  final case class Ret(value: Val)                          extends Cf
  final case class Jump(next: Next)                         extends Cf
  final case class If(value: Val, thenp: Next, elsep: Next) extends Cf
  final case class Switch(value: Val, default: Next, cases: Seq[Next])
      extends Cf
  final case class Invoke(ty: Type,
                          ptr: Val,
                          args: Seq[Val],
                          succ: Next,
                          fail: Next)
      extends Cf

  // high-level control
  final case class Throw(value: Val)           extends Cf
  final case class Try(succ: Next, fail: Next) extends Cf
}
