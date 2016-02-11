package native
package nir

import scala.collection.mutable

final case class Focus(
  preceding: Seq[Block],
  name:      Local,
  params:    Seq[Param],
  instrs:    Seq[Instr],
  value:     Val,
  complete:  Boolean
)(implicit _fresh: Fresh) {
  def fresh = _fresh

  def withValue(newvalue: Val): Focus = {
    assert(!complete)
    copy(value = newvalue)
  }

  def withOp(op: Op): Focus =
    withOp(Seq(), op)

  def withOp(attrs: Seq[Attr], op: Op): Focus = {
    assert(!complete)
    val name = fresh()
    copy(instrs = instrs :+ Instr(Some(name), attrs, op), value = Val.Local(name, op.resty))
  }

  def finish(op: Op): Focus =
    finish(Instr(None, Seq(), op))

  def finish(instr: Instr): Focus =
    if (complete) this
    else Focus.complete(preceding :+ Block(name, params, instrs :+ instr))

  def blocks: Seq[Block] = {
    assert(complete)
    preceding
  }

  private def wrapBranch(merge: Local, f: Focus => Focus) = {
    val entry = Focus.entry
    val end = f(entry)
    val finalized =
      if (end.complete) end
      else end.finish(Op.Jump(Next(merge, Seq(end.value))))
    (entry.name, end.complete, finalized.blocks)
  }

  def branchIf(cond: Val, retty: Type,
               thenf: Focus => Focus, elsef: Focus => Focus): Focus = {
    val merge = fresh()
    val param = Param(fresh(), retty)
    val (thenname, thencompl, thenprec) = wrapBranch(merge, thenf)
    val (elsename, elsecompl, elseprec) = wrapBranch(merge, elsef)
    val prec =
      finish(Op.If(cond,
        Next(thenname, Seq()),
        Next(elsename, Seq()))).blocks
    if (thencompl && elsecompl)
      Focus.complete(prec ++ thenprec ++ elseprec)
    else
      Focus(prec ++ thenprec ++ elseprec,
            merge, Seq(param), Seq(),
            Val.Local(param.name, retty), complete = false)
  }

  def branchSwitch(scrut: Val, retty: Type,
                   defaultf: Focus => Focus,
                   casevalues: Seq[Val], casefs: Seq[Focus => Focus]): Focus = {
    val merge = fresh()
    val param = Param(fresh(), retty)
    val (defaultname, defaultcompl, defaultprec) = wrapBranch(merge, defaultf)
    val cases = casefs.map(wrapBranch(merge, _))
    val casenames = cases.map(_._1)
    val casecompl = cases.map(_._2)
    val caseprecs = cases.map(_._3)
    val prec =
      finish(Op.Switch(scrut,
        Next(defaultname, Seq()),
        casevalues.zip(casenames).map { case (v, n) => Case(v, Next(n, Seq())) })).blocks
    Focus(prec ++ defaultprec ++ caseprecs.flatten,
          merge, Seq(param), Seq(),
          Val.Local(param.name, retty), complete = false)
  }

  def branchBlock(n: Local): Focus = {
    val blocks = finish(Op.Jump(Next(n, Seq()))).blocks
    Focus(blocks, n, Seq(), Seq(), Val.Unit, complete = false)
  }
}
object Focus {
  final case class NotMergeable(focus: Focus) extends Exception

  def entry(implicit fresh: Fresh): Focus =
    entry(fresh(), Seq())

  def entry(params: Seq[Param])(implicit fresh: Fresh): Focus =
    entry(fresh(), params)

  def entry(name: Local, params: Seq[Param])(implicit fresh: Fresh): Focus =
    Focus(Seq(), name, params, Seq(), Val.Unit, complete = false)

  def complete(blocks: Seq[Block])(implicit fresh: Fresh)=
    Focus(blocks, Local("", -1), Seq(), Seq(), Val.Unit, complete = true)

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
