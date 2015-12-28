package native
package compiler
package reductions

import native.gir._, Reduction._

/** Lowers slices and operations on them down to pointers to structs.
 */
object ArrayClassLowering extends Reduction {
  def reduce = {
    case Defn.ArrayClass(ty) =>
      replaceAll(Defn.Ptr(Prim.I8))
    case ArrayClassAlloc(_, _, _) =>
      ???
    case ArrayClassElem(_, _, _) =>
      ???
    case ArrayClassLength(_) =>
      ???
  }
}
