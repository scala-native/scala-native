package salty.tools.compiler
package reductions

import salty.ir._, Node.Slot, Reduction._

object LowerBoxing extends Reduction {
  def reduce = {
    case Box(value, Slot(Ref(ty))) => Replace(Store(NoEf, Alloc(ty), value))
    case Unbox(value, ty)          => Replace(Load(NoEf, As(value, Ref(ty))))
  }
}

