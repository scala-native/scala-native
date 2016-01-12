package native
package nir

final case class Instr(name: Option[Local], attrs: Seq[Attr], op: Op)
object Instr {
  def apply(op: Op): Instr = Instr(None, Seq(), op)
  def apply(name: Local, op: Op): Instr = Instr(Some(name), Seq(), op)
}
