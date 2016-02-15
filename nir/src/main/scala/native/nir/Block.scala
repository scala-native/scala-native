package native
package nir

final case class Block(name: Local, params: Seq[Val.Local], insts: Seq[Inst])
