package salty.tools.compiler.reductions

import salty.ir._, Reduction._

object AllocLowering extends Reduction {
  def reduce = {
    case Alloc(n) =>
      ???
  }
}
