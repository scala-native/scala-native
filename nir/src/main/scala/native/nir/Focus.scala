package native
package nir

import scala.collection.mutable

final case class Focus(
  val blocks:     Seq[Block],
  val name:       Local,
  val params:     Seq[Param],
  val instrs:     Seq[Instr],
  val value:      Val,
  val isComplete: Boolean
)(implicit _fresh: Fresh) {
  def fresh = _fresh

  def appendBlocks(blocks: Seq[Block]) =
    copy(blocks = this.blocks ++ blocks)

  def prependBlocks(blocks: Seq[Block]) =
    copy(blocks = blocks ++ this.blocks)

  def withValue(newvalue: Val): Focus = {
    assert(!isComplete)
    copy(value = newvalue)
  }

  def withOp(op: Op): Focus =
    withOp(Seq(), op)

  def withOp(attrs: Seq[Attr], op: Op): Focus = {
    assert(!isComplete)
    val name = fresh()
    copy(instrs = instrs :+ Instr(Some(name), attrs, op), value = Val.Local(name, op.resty))
  }

  def finish(op: Op): Focus =
    finish(Instr(None, Seq(), op))

  def finish(instr: Instr): Focus =
    if (isComplete) this
    else Focus.complete(blocks :+ Block(name, params, instrs :+ instr))

  private def wrapBranch(merge: Local, f: Focus => Focus) = {
    val entry = Focus.entry
    val end = f(entry)
    val finalized =
      if (end.isComplete) end
      else end.finish(Op.Jump(Next(merge, Seq(end.value))))
    (entry.name, end.isComplete, finalized.blocks)
  }

  def branchIf(cond: Val, retty: Type,
               thenf: Focus => Focus, elsef: Focus => Focus): Focus = {
    val merge = fresh()
    val param = Param(fresh(), retty)
    val (thenname, thencompl, thenblocks) = wrapBranch(merge, thenf)
    val (elsename, elsecompl, elseblocks) = wrapBranch(merge, elsef)
    val blocks =
      finish(Op.If(cond,
        Next(thenname, Seq()),
        Next(elsename, Seq()))).blocks
    if (thencompl && elsecompl)
      Focus.complete(blocks ++ thenblocks ++ elseblocks)
    else
      Focus(blocks ++ thenblocks ++ elseblocks,
            merge, Seq(param), Seq(),
            Val.Local(param.name, retty), isComplete = false)
  }

  def branchSwitch(scrut: Val, retty: Type,
                   defaultf: Focus => Focus,
                   casevalues: Seq[Val], casefs: Seq[Focus => Focus]): Focus = {
    val merge = fresh()
    val param = Param(fresh(), retty)
    val (defaultname, defaultcompl, defaultblocks) = wrapBranch(merge, defaultf)
    val cases = casefs.map(wrapBranch(merge, _))
    val casenames = cases.map(_._1)
    val casecompl = cases.map(_._2)
    val caseblockss = cases.map(_._3)
    val blocks =
      finish(Op.Switch(scrut,
        Next(defaultname, Seq()),
        casevalues.zip(casenames).map { case (v, n) => Case(v, Next(n, Seq())) })).blocks
    Focus(blocks ++ defaultblocks ++ caseblockss.flatten,
          merge, Seq(param), Seq(),
          Val.Local(param.name, retty), isComplete = false)
  }

  def branchBlock(n: Local): Focus = {
    val blocks = finish(Op.Jump(Next(n, Seq()))).blocks
    Focus(blocks, n, Seq(), Seq(), Val.Unit, isComplete = false)
  }
}
object Focus {
  final case class NotMergeable(focus: Focus) extends Exception

  def entry(implicit fresh: Fresh): Focus =
    entry(fresh(), Seq())

  def entry(params: Seq[Param])(implicit fresh: Fresh): Focus =
    entry(fresh(), params)

  def entry(name: Local, params: Seq[Param])(implicit fresh: Fresh): Focus =
    Focus(Seq(), name, params, Seq(), Val.Unit, isComplete = false)

  def complete(blocks: Seq[Block])(implicit fresh: Fresh)=
    Focus(blocks, Local("", -1), Seq(), Seq(), Val.Unit, isComplete = true)

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
