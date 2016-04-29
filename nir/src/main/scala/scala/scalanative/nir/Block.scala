package scala.scalanative
package nir

final case class Block(
    name: Local, params: Seq[Val.Local], insts: Seq[Inst], cf: Cf)
