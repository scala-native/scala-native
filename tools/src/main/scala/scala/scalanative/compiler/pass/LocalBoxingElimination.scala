package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import nir._, Inst.Let

/** Eliminates redundant box/unbox operations within
 *  a single basic block. This is quite simplistic approach
 *  but we need this to remove boxing around pointer operations
 *  that happen to have generic signatures.
 */
class LocalBoxingElimination extends Pass {
  import LocalBoxingElimination._

  override def preDefn = {
    case defn: Defn.Define =>
      val records = mutable.UnrolledBuffer.empty[Record]

      val newinsts = defn.insts.map {
        case inst @ Let(to, op @ Op.Call(_, BoxRef(code), Seq(_, from))) =>
          records.collectFirst {
            // if a box for given value already exists, re-use the box
            case Box(rcode, rfrom, rto) if rcode == code && from == rfrom =>
              Let(to, Op.Copy(rto))

            // if we re-box previously unboxed value, re-use the original box
            case Unbox(rcode, rfrom, rto) if rcode == code && from == rto =>
              Let(to, Op.Copy(rfrom))
          }.getOrElse {
            // otherwise do actual boxing
            records += Box(code, from, Val.Local(to, op.resty))
            inst
          }

        case inst @ Let(to, op @ Op.Call(_, UnboxRef(code), Seq(_, from))) =>
          records.collectFirst {
            // if we unbox previously boxed value, return original value
            case Box(rcode, rfrom, rto) if rcode == code && from == rto =>
              Let(to, Op.Copy(rfrom))

            // if an unbox for this value already exists, re-use unbox
            case Unbox(rcode, rfrom, rto) if rcode == code && from == rfrom =>
              Let(to, Op.Copy(rto))
          }.getOrElse {
            // otherwise do actual unboxing
            records += Unbox(code, from, Val.Local(to, op.resty))
            inst
          }

        case inst =>
          inst
      }

      Seq(defn.copy(insts = newinsts))
  }
}

object LocalBoxingElimination extends PassCompanion {
  private sealed abstract class Record
  private final case class Box(code: Char, from: nir.Val, to: nir.Val)
      extends Record
  private final case class Unbox(code: Char, from: nir.Val, to: nir.Val)
      extends Record

  private val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  private val RuntimeBoxes = Global.Top("scala.scalanative.runtime.Boxes$")

  private val BoxTo: Map[Global, Char] = Seq(
      ('B', BoxesRunTime, "boxToBoolean_bool_class.java.lang.Boolean"),
      ('C', BoxesRunTime, "boxToCharacter_i16_class.java.lang.Character"),
      ('z', RuntimeBoxes, "boxToUByte_i8_class.java.lang.Object"),
      ('Z', BoxesRunTime, "boxToByte_i8_class.java.lang.Byte"),
      ('s', RuntimeBoxes, "boxToUShort_i16_class.java.lang.Object"),
      ('S', BoxesRunTime, "boxToShort_i16_class.java.lang.Short"),
      ('i', RuntimeBoxes, "boxToUInt_i32_class.java.lang.Object"),
      ('I', BoxesRunTime, "boxToInteger_i32_class.java.lang.Integer"),
      ('l', RuntimeBoxes, "boxToULong_i64_class.java.lang.Object"),
      ('L', BoxesRunTime, "boxToLong_i64_class.java.lang.Long"),
      ('F', BoxesRunTime, "boxToFloat_f32_class.java.lang.Float"),
      ('D', BoxesRunTime, "boxToDouble_f64_class.java.lang.Double")
  ).map {
    case (code, module, id) =>
      Global.Member(module, id) -> code
  }.toMap

  private val UnboxTo: Map[Global, Char] = Seq(
      ('B', BoxesRunTime, "unboxToBoolean_class.java.lang.Object_bool"),
      ('C', BoxesRunTime, "unboxToChar_class.java.lang.Object_i16"),
      ('z', RuntimeBoxes, "unboxToUByte_class.java.lang.Object_i8"),
      ('Z', BoxesRunTime, "unboxToByte_class.java.lang.Object_i8"),
      ('s', RuntimeBoxes, "unboxToUShort_class.java.lang.Object_i16"),
      ('S', BoxesRunTime, "unboxToShort_class.java.lang.Object_i16"),
      ('i', RuntimeBoxes, "unboxToUInt_class.java.lang.Object_i32"),
      ('I', BoxesRunTime, "unboxToInt_class.java.lang.Object_i32"),
      ('l', RuntimeBoxes, "unboxToULong_class.java.lang.Object_i64"),
      ('L', BoxesRunTime, "unboxToLong_class.java.lang.Object_i64"),
      ('F', BoxesRunTime, "unboxToFloat_class.java.lang.Object_f32"),
      ('D', BoxesRunTime, "unboxToDouble_class.java.lang.Object_f64")
  ).map {
    case (code, module, id) =>
      Global.Member(module, id) -> code
  }.toMap

  object BoxRef {
    def unapply(value: Val): Option[Char] = value match {
      case Val.Global(n, _) => BoxTo.get(n)
      case _                => None
    }
  }
  object UnboxRef {
    def unapply(value: Val): Option[Char] = value match {
      case Val.Global(n, _) => UnboxTo.get(n)
      case _                => None
    }
  }

  def apply(ctx: Ctx) = new LocalBoxingElimination
}
