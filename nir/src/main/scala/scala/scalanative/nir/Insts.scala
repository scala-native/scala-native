package scala.scalanative
package nir

sealed abstract class Inst
object Inst {
  sealed abstract class Cf extends Inst

  // empty instruction
  final case object None extends Inst

  // basic block label
  final case class Label(name: Local, params: Seq[Val.Local]) extends Inst

  // single static assignment
  final case class Let(name: Local, op: Op) extends Inst
  object Let {
    def apply(op: Op)(implicit fresh: Fresh): Let = Let(fresh(), op)
  }

  // low-level control-flow
  final case object Unreachable extends Cf
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

  // high-level control-flow
  final case class Throw(value: Val)           extends Cf
  final case class Try(succ: Next, fail: Next) extends Cf
}
