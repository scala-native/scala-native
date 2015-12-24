package native
package nir

final case class Block(name: Name, params: Seq[Param], instrs: Seq[Instr])
final case class Instr(name: Name, attrs: Seq[Attr], op: Op)
final case class Param(name: Name, ty: Type)
final case class Next(name: Name, args: Seq[Val])
final case class Case(value: Val, next: Next)
