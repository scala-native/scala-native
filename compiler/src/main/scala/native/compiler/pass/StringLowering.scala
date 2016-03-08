package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

/** Lowers strings values into intrinsified global constant.
 *
 *  Every string value:
 *
 *      "..."
 *
 *  Generate unique constants per string value:
 *
 *      const @__str_$idx_data: [i8 x ${str.length}] =
 *        c"..."
 *
 *      const @__str_$idx: struct #string =
 *        struct #string { #string_type, ${str.length}, bitcast[ptr i8] @__str_$idx_data }
 *
 *  And the value itself is replaced with:
 *
 *      bitcast[ptr i8] @__str_$idx
 *
 *  Eliminates:
 *  - Val.String
 */
class StringLowering extends Pass {
  private val strings = mutable.UnrolledBuffer.empty[String]

  override def postAssembly = { case defns =>
    defns ++ strings.zipWithIndex.flatMap { case (v, idx) =>
      val data      = Global("__str", idx.toString, "data")
      val dataTy    = Type.Array(Type.I8, v.length)
      val dataVal   = Val.Chars(v)
      val dataConst = Defn.Const(Seq(), data, dataTy, dataVal)
      val dataPtr   = Val.Bitcast(Type.Ptr(Type.I8), Val.Global(data, Type.Ptr(dataTy)))

      val str      = Global("__str", idx.toString)
      val strTy    = Type.Struct(Nrt.String.name)
      val strVal   = Val.Struct(Nrt.String.name, Seq(Nrt.String_type, Val.I32(v.length), dataPtr))
      val strConst = Defn.Const(Seq(), str, strTy, strVal)

      Seq(dataConst, strConst)
    }
  }

  override def preVal = {
    case Val.String(v) =>
      val idx =
        if (strings.contains(v))
          strings.indexOf(v)
        else {
          strings += v
          strings.length - 1
        }

      Val.Bitcast(Type.Ptr(Type.I8),
        Val.Global(Global("__str", idx.toString), Type.Ptr(Type.Struct(Nrt.String.name))))
  }
}
