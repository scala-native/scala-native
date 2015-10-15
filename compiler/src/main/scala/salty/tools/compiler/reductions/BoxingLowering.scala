package salty.tools.compiler
package reductions

import salty.ir._, Reduction._

object LowerBoxing extends Reduction {
  def reduce = {
    case Box(value, Slot(Ref(ty))) => Replace.all(Store(Empty, Alloc(ty), value))
    case Unbox(value, ty)          => Replace.all(Load(Empty, As(value, Ref(ty))))
  }
}

