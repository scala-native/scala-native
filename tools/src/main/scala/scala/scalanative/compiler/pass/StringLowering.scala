package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import util.ScopedVar, ScopedVar.scoped
import nir._

/** Lowers strings values into intrinsified global constants.
 *
 *  Eliminates:
 *  - Val.String
 */
class StringLowering(implicit chg: ClassHierarchy.Graph) extends Pass {
  private val strings = mutable.UnrolledBuffer.empty[String]

  /** Names of the fields of the java.lang.String in the memory layout order. */
  private val stringFieldNames = {
    val node  = ClassRef.unapply(Rt.String.name).get
    val names = node.fields.sortBy(_.index).map(_.name)
    assert(names.length == 4, "java.lang.String is expected to have 4 fields.")
    names
  }

  override def preVal = {
    case Val.String(v) =>
      val node = ClassRef.unapply(Rt.String.name).get

      val stringInfo  = Val.Global(Rt.String.name tag "const", Type.Ptr)
      val charArrInfo = Val.Global(Rt.CharArray.name tag "const", Type.Ptr)
      val chars       = v.toCharArray
      val charsLength = Val.I32(chars.length)
      val charsConst = Val.Const(
          Val.Struct(
              Global.None,
              Seq(charArrInfo,
                  charsLength,
                  Val.Array(Type.I16, chars.map(c => Val.I16(c.toShort))))))

      val fieldValues = stringFieldNames.map {
        case Global.Member(_, "field.value")  => charsConst
        case Global.Member(_, "field.offset") => Val.I32(0)
        case Global.Member(_, "field.count")  => charsLength
        case _                                => Val.I32(v.hashCode)
      }

      Val.Const(Val.Struct(Global.None, stringInfo +: fieldValues))
  }
}
