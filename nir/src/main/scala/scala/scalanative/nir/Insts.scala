package scala.scalanative
package nir

sealed abstract class Inst {
  final def show: String = nir.Show(this)
}

object Inst {
  final case class Label(name: Local, params: Seq[Val.Local]) extends Inst
  final case class Let(name: Local, op: Op, unwind: Next)     extends Inst
  object Let {
    def apply(op: Op, unwind: Next)(implicit fresh: Fresh): Let =
      Let(fresh(), op, unwind)
  }

  sealed abstract class Cf                                  extends Inst
  final case class Ret(value: Val)                          extends Cf
  final case class Jump(next: Next)                         extends Cf
  final case class If(value: Val, thenp: Next, elsep: Next) extends Cf
  final case class Switch(value: Val, default: Next, cases: Seq[Next])
      extends Cf
  final case class Throw(value: Val, unwind: Next) extends Cf
  final case class Unreachable(unwind: Next)       extends Cf
}
