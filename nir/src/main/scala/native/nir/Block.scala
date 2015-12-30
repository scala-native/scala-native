package native
package nir

final case class Block(name: Local, params: Seq[Param], instrs: Seq[Instr])
