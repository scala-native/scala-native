package scala.scalanative
package nir

final class Fresh private (private var start: Long) {
  def apply(): Local = {
    start += 1
    val value = start
    Local(value)
  }
}

object Fresh {
  def apply(start: Long = 0L): Fresh =
    new Fresh(start)

  def apply(insts: Seq[Inst]): Fresh = {
    var max = -1L
    insts.foreach {
      case Inst.Let(local, _, Next.Unwind(Val.Local(exc, _), _)) =>
        max = Math.max(max, local.id)
        max = Math.max(max, exc.id)
      case Inst.Let(local, _, _) =>
        max = Math.max(max, local.id)
      case Inst.Label(local, params) =>
        max = Math.max(max, local.id)
        params.foreach { param => max = Math.max(max, param.name.id) }
      case Inst.Throw(_, Next.Unwind(Val.Local(exc, _), _)) =>
        max = Math.max(max, exc.id)
      case Inst.Unreachable(Next.Unwind(Val.Local(exc, _), _)) =>
        max = Math.max(max, exc.id)
      case _ =>
        ()
    }
    new Fresh(max)
  }
}
