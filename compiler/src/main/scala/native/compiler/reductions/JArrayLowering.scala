package native
package compiler
package reductions

import native.ir._, Reduction._

/** Lowers slices and operations on them down to pointers to structs.
 */
object JArrayLowering extends Reduction {
  def reduce = {
    case Defn.JArray(ty) =>
      replaceAll(Defn.Ptr(Prim.I8))
    case JArrayAlloc(_, _, _) =>
      ???
    case JArrayElem(_, _, _) =>
      ???
    case JArrayLength(_) =>
      ???
  }
}
