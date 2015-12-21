package native
package nir

import scala.collection.mutable

final case class Focus(
  preceding: Seq[Block],
  name:      Name,
  params:    Seq[Param],
  instrs:    Seq[Instr],
  value:     Val,
  complete:  Boolean
) {
  def withValue(newvalue: Val): Focus = {
    assert(!complete)
    copy(value = newvalue)
  }

  def withOp(op: Op)(implicit fresh: Fresh): Focus = {
    assert(!complete)
    val name = fresh()
    copy(instrs = instrs :+ Instr(name, op), value = Val.Name(op.resty, name))
  }

  def finish(op: Op): Focus =
    Focus.complete(preceding :+ Block(name, params, instrs :+ Instr(Name.None, op)))

  def blocks: Seq[Block] = {
    assert(complete)
    preceding
  }

  def branchIf(cond: Val, thenf: Focus => Focus, elsef: Focus => Focus)(implicit fresh: Fresh): Focus = {
    val merge = fresh()
    val param = Param(fresh(), Type.None)
    def wrap(f: Focus => Focus) = {
      val end = f(Focus.entry(Seq()))
      val finalized = end.finish(Op.Jump(Next(merge, Seq(end.value))))
      finalized.blocks
    }
    val thenprec = wrap(elsef)
    val thenname = thenprec.last.name
    val elseprec = wrap(thenf)
    val elsename = elseprec.last.name
    val prec =
      finish(Op.If(cond,
        Next(thenname, Seq()),
        Next(elsename, Seq()))).blocks
    Focus(prec ++ thenprec ++ elseprec,
          merge, Seq(param), Seq(),
          Val.Name(???, param.name), complete = false)
  }
}
object Focus {
  final case class NotMergeable(focus: Focus) extends Exception

  def entry(params: Seq[Param])(implicit fresh: Fresh): Focus =
    Focus(Seq(), fresh(), params, Seq(), Val.Unit, complete = false)

  def complete(blocks: Seq[Block]) =
    Focus(blocks, Name.None, Seq(), Seq(), Val.Unit, complete = true)

  def sequenced[T](elems: Seq[T], focus: Focus)
                  (f: (T, Focus) => Focus): Seq[Focus] = {
    val focs = new mutable.ListBuffer[Focus]
    elems.foldLeft(focus) { (accfoc, elem) =>
      val efoc = f(elem, accfoc)
      focs += efoc
      efoc
    }
    focs.toSeq
  }
}
