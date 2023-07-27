package scala.scalanative
package nir

sealed abstract class Inst {
  final def show: String = nir.Show(this)
  def pos: Position
}

object Inst {
  final case class Label(name: Local, params: Seq[Val.Local])(implicit
      val pos: Position
  ) extends Inst
  final case class Let(id: Local, localName: LocalName, op: Op, unwind: Next)(
      implicit val pos: Position
  ) extends Inst
  object Let {
    def apply(id: Local, op: Op, unwind: Next)(implicit pos: Position): Let =
      Let(id, None, op, unwind)
    def apply(op: Op, unwind: Next)(implicit fresh: Fresh, pos: Position): Let =
      Let(fresh(), op, unwind)
  }

  sealed abstract class Cf extends Inst
  final case class Ret(value: Val)(implicit val pos: Position) extends Cf
  final case class Jump(next: Next)(implicit val pos: Position) extends Cf
  final case class If(value: Val, thenp: Next, elsep: Next)(implicit
      val pos: Position
  ) extends Cf
  final case class Switch(value: Val, default: Next, cases: Seq[Next])(implicit
      val pos: Position
  ) extends Cf
  final case class Throw(value: Val, unwind: Next)(implicit val pos: Position)
      extends Cf
  final case class Unreachable(unwind: Next)(implicit val pos: Position)
      extends Cf

  sealed trait LinktimeCf extends Cf
  final case class LinktimeIf(
      cond: LinktimeCondition,
      thenp: Next,
      elsep: Next
  )(implicit val pos: Position)
      extends LinktimeCf
}
