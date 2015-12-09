package native
package nir

final case class Block(name: Name, params: Seq[Param], instrs: Seq[Instr])
final case class Instr(name: Name, op: Op, ty: Type)
final case class Param(name: Name, ty: Type)
final case class Next(value: Val, name: Name, args: Seq[Val])
