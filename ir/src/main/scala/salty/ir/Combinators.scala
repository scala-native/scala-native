package salty.ir

object Combinators {
  implicit class RichInstr(val self: Instr) extends AnyVal {
    import self._

    def merge: Instr = self

    def chain(other: Instr): (Instr, Instr) = ???
  }

  implicit class RichInstrs(val blocks: Seq[Instr]) extends AnyVal {
    def chain: Seq[Instr] = ???
  }
}
