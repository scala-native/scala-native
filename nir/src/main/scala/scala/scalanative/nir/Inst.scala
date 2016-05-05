package scala.scalanative
package nir

final case class Inst(name: Local, op: Op)
object Inst {
  def apply(op: Op)(implicit fresh: Fresh): Inst = new Inst(fresh(), op)
}
