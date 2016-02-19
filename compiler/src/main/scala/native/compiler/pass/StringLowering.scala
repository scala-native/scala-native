package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

/** Lowers strings values into intrinsified global constant.
 *
 *  Every string-of op:
 *
 *      %n = string-of "..."
 *
 *  Becomes a reference to constants:
 *
 *      const @_str_$N_data: [i8 x ${str.length}] =
 *        c"..."
 *
 *      const @_str_$N: struct #string =
 *        struct #string { #type_of_string, ${str.length}, @_str_$N_data }
 *
 *  And the value itself is replaced with:
 *
 *      %n = bitcast[ptr i8] @_str_$N
 *
 *  Eliminates:
 *  - Val.String
 */
class StringLowering extends Pass {
  override def preInst = {
    case Inst(n, Op.StringOf(v)) =>
      ???
  }
}
