package native
package nir

final case class Inst(name: Local, op: Op)
object Inst {
  def apply(op: Op): Inst = new Inst(Local.empty, op)
}
