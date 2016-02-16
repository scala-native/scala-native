package native
package nir

final case class Inst(name: Option[Local], op: Op)
object Inst {
  def apply(op: Op): Inst = Inst(None, op)
  def apply(name: Local, op: Op): Inst = Inst(Some(name), op)
}
