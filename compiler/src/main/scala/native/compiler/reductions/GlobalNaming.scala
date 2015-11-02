package native
package compiler
package reductions

import native.ir._, Reduction._

object GlobalNaming extends Reduction {
  var i = 0
  def reduce = {
    case n if n.name == Name.No && (n ne Empty) =>
      val newn = n.copy(attrs = n.attrs :+ Name.Local(s"_$i"))
      i += 1
      replaceAll(newn)
  }
}
