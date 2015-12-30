package native
package nir

final case class Instr(name: Option[Local], attrs: Seq[Attr], op: Op)
