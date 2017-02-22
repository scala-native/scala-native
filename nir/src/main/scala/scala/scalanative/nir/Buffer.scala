package scala.scalanative
package nir

import scala.collection.mutable

final class Buffer {
  private val buffer =
    mutable.UnrolledBuffer.empty[Inst]
  def label(name: Local): Unit =
    label(name, Buffer.emptyParamSeq)
  def label(name: Local, params: Seq[Val.Local]): Unit =
    buffer += Inst.Label(name, params)
  def let(name: Local, op: Op): Unit =
    buffer += Inst.Let(name, op)
  def let(op: Op)(implicit fresh: Fresh): Val.Local = {
    val name = fresh()
    buffer += Inst.Let(name, op)
    Val.Local(name, op.resty)
  }
  def unreachable: Unit =
    buffer += Inst.Unreachable
  def ret(value: Val): Unit =
    buffer += Inst.Ret(value)
  def jump(next: Next): Unit =
    buffer += Inst.Jump(next)
  def branch(value: Val, thenp: Next, elsep: Next): Unit =
    buffer += Inst.If(value, thenp, elsep)
  def switch(value: Val, default: Next, cases: Seq[Next]): Unit =
    buffer += Inst.Switch(value, default, cases)
  def raise(value: Val, unwind: Next): Unit =
    buffer += Inst.Throw(value, unwind)
  def +=(inst: Inst): Unit =
    buffer += inst
  def ++=(insts: Seq[Inst]): Unit =
    buffer ++= insts
  def ++=(other: Buffer): Unit =
    buffer ++= other.buffer
  def toSeq: Seq[Inst] =
    buffer
}

object Buffer {
  private val emptyParamSeq = Seq.empty[Val.Local]
}
