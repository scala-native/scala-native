package salty.tools.compiler.reductions

import salty.ir._, Reduction._

object SizeLowering extends Reduction {
  def reduce = {
    case Lit.Size(n) =>
      val offset = Elem(Lit.Zero(Defn.Ptr(n)), Seq(Lit.I32(1)))
      val cast = Ptrtoint(offset, Prim.I64)

      replaceAll(cast)
  }
}
