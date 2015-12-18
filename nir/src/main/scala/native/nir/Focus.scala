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
    val newvalue = fresh()
    copy(instrs = instrs :+ Instr(newvalue.name, op), value = newvalue)
  }

  def finish(op: Op): Focus =
    Focus.complete(preceding :+ Block(name, params, instrs :+ Instr(Name.None, op)))

  def blocks: Seq[Block] = {
    assert(complete)
    preceding
  }

  def branchIf(cond: Val, thenf: Focus => Focus, elsef: Focus => Focus): Focus = ???
}
object Focus {
  final case class NotMergeable(focus: Focus) extends Exception

  def entry(params: Seq[Param])(implicit fresh: Fresh): Focus =
    Focus(Seq(), fresh().name, params, Seq(), Val.Unit, complete = false)

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
