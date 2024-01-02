package scala.scalanative
package nir

sealed abstract class Inst {
  final def show: String = nir.Show(this)
  def pos: SourcePosition
}

object Inst {
  final case class Label(id: Local, params: Seq[Val.Local])(implicit
      val pos: SourcePosition
  ) extends Inst
  final case class Let(id: Local, op: Op, unwind: Next)(implicit
      val pos: SourcePosition,
      val scopeId: ScopeId
  ) extends Inst
  object Let {
    def apply(op: Op, unwind: Next)(implicit
        fresh: Fresh,
        pos: SourcePosition,
        scopeId: ScopeId
    ): Let =
      Let(fresh(), op, unwind)
  }

  sealed abstract class Cf extends Inst
  final case class Ret(value: Val)(implicit val pos: SourcePosition) extends Cf
  final case class Jump(next: Next)(implicit val pos: SourcePosition) extends Cf
  final case class If(value: Val, thenp: Next, elsep: Next)(implicit
      val pos: SourcePosition
  ) extends Cf
  final case class Switch(value: Val, default: Next, cases: Seq[Next])(implicit
      val pos: SourcePosition
  ) extends Cf
  final case class Throw(value: Val, unwind: Next)(implicit
      val pos: SourcePosition
  ) extends Cf
  final case class Unreachable(unwind: Next)(implicit val pos: SourcePosition)
      extends Cf

  sealed trait LinktimeCf extends Cf
  final case class LinktimeIf(
      cond: LinktimeCondition,
      thenp: Next,
      elsep: Next
  )(implicit val pos: SourcePosition)
      extends LinktimeCf
}
