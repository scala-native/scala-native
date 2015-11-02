package native
package compiler
package reductions

import native.ir._, Reduction._

/** Lowers slices and operations on them down to pointers and structs.
 */
object SliceLowering extends Reduction {
  def reduce = {
    case Defn.Slice(ty) =>
      replaceAll {
        Defn.Struct(Seq(Prim.I32, Defn.Ptr(ty)))
      }
    case SliceElem(_, _, _) =>
      ???
    case SliceAlloc(_, _, _) =>
      ???
    case SliceLength(_) =>
      ???
  }
}
