package native
package nir

final case class Inst(name: Option[Local], attrs: Seq[Attr], op: Op)
object Inst {
  def apply(op: Op): Inst = Inst(None, Seq(), op)
  def apply(name: Local, op: Op): Inst = Inst(Some(name), Seq(), op)
}
