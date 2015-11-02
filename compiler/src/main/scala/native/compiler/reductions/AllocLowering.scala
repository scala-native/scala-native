package native
package compiler
package reductions

import native.ir._, Reduction._

object AllocLowering extends Reduction {
  val malloc =
    Defn.Declare(Defn.Ptr(Prim.I8), Seq(Param(Prim.I64)), Name.Local("malloc"))

  def reduce = {
    case Alloc(n) =>
      val call = Call(Empty, malloc, Seq(Lit.Size(n)))
      val cast = Bitcast(call, Defn.Ptr(n))

      replaceAll(cast)
  }
}
