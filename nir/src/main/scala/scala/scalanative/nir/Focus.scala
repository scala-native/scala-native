package scala.scalanative
package nir

import scala.collection.mutable
import Shows._
import util.sh

final class Focus private (
    private val _labeled: Boolean = false,
    private val _insts: Seq[Inst] = Seq(),
    private val _value: Val = Val.Unit)(implicit fresh: Fresh) {
  private def copy(_labeled: Boolean = this._labeled,
                   _insts: Seq[Inst] = this._insts,
                   _value: Val = this._value) =
    new Focus(_labeled, _insts, _value)

  def value: Val = _value

  def finish(end: Inst.Cf): Seq[Inst] =
    withInst(end)._insts

  def withValue(v: Val): Focus =
    copy(_value = v)

  def withOp(op: Op): Focus = {
    val name = fresh()
    withInst(Inst.Let(name, op)).withValue(Val.Local(name, op.resty))
  }

  def withRet(value: Val): Focus =
    withInst(Inst.Ret(value))

  def withThrow(value: Val): Focus =
    withInst(Inst.Throw(value))

  def withJump(to: Local, values: Val*): Focus =
    withInst(Inst.Jump(Next.Label(to, values)))

  def withIf(cond: Val, thenp: Next, elsep: Next): Focus =
    withInst(Inst.If(cond, thenp, elsep))

  def withLabel(name: Local, params: Val.Local*): Focus =
    withInst(Inst.Label(name, params))

  def withSwitch(scrut: Val,
                 default: Local,
                 casevals: Seq[Val],
                 casetos: Seq[Local]): Focus = {
    val cases = casevals.zip(casetos).map {
      case (v, to) =>
        Next.Case(v, to)
    }
    withInst(Inst.Switch(scrut, Next(default), cases))
  }

  def withTry(succ: Local, fail: Local) =
    withInst(Inst.Try(Next.Succ(succ), Next.Fail(fail)))

  def withInst(inst: Inst): Focus = inst match {
    case _: Inst.Label =>
      if (_labeled) {
        copy(_labeled = true, _insts = _insts :+ Inst.Unreachable :+ inst)
      } else {
        copy(_labeled = true, _insts = _insts :+ inst)
      }
    case _: Inst.Cf =>
      if (_labeled) {
        copy(_labeled = false, _insts = _insts :+ inst)
      } else {
        val label = Inst.Label(fresh(), Seq())
        copy(_labeled = false, _insts = _insts :+ label :+ inst)
      }
    case _ =>
      if (_labeled) {
        copy(_labeled = true, _insts = _insts :+ inst)
      } else {
        val label = Inst.Label(fresh(), Seq())
        copy(_labeled = true, _insts = _insts :+ label :+ inst)
      }
  }
}

object Focus {
  def start()(implicit fresh: Fresh) = new Focus()

  def sequenced[T](elems: Seq[T], focus: Focus)(
      f: (T, Focus) => Focus): Seq[Focus] = {
    val focs = new mutable.ListBuffer[Focus]
    elems.foldLeft(focus) { (accfoc, elem) =>
      val efoc = f(elem, accfoc)
      focs += efoc
      efoc
    }
    focs.toSeq
  }

  def merged(ty: nir.Type, focus: Focus, branches: Seq[Focus => Focus])(
      implicit fresh: Fresh): Focus = {
    val mergen = fresh()
    var lastfocus = focus
    branches.foreach { branch =>
      lastfocus = branch(lastfocus)
      lastfocus = lastfocus.withJump(mergen, lastfocus.value)
    }

    val mergev = Val.Local(fresh(), ty)
    lastfocus.withLabel(mergen, mergev).withValue(mergev)
  }
}
