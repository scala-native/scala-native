package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import nir._

/** Eliminates redundant box/unbox operations within
 *  a single basic block. This is quite simplistic approach
 *  but we need this to remove boxing around pointer operations
 *  that happen to have generic signatures.
 */
class LocalBoxingElimination extends Pass {
  import LocalBoxingElimination._

  override def preBlock = {
    case block =>
      val records = mutable.UnrolledBuffer.empty[Record]

      val newInsts = block.insts.map {
        case inst @ Inst(to, op @ Op.Call(_, BoxRef(code), Seq(_, from))) =>
          records.collectFirst {
            // if a box for given value already exists, re-use the box
            case Box(rcode, rfrom, rto) if rcode == code && from == rfrom =>
              Inst(to, Op.Copy(rto))

            // if we re-box previously unboxed value, re-use the original box
            case Unbox(rcode, rfrom, rto) if rcode == code && from == rto =>
              Inst(to, Op.Copy(rfrom))
          }.getOrElse {
            // otherwise do actual boxing
            records += Box(code, from, Val.Local(to, op.resty))
            inst
          }

        case inst @ Inst(to, op @ Op.Call(_, UnboxRef(code), Seq(_, from))) =>
          records.collectFirst {
            // if we unbox previously boxed value, return original value
            case Box(rcode, rfrom, rto) if rcode == code && from == rto =>
              Inst(to, Op.Copy(rfrom))

            // if an unbox for this value already exists, re-use unbox
            case Unbox(rcode, rfrom, rto) if rcode == code && from == rfrom =>
              Inst(to, Op.Copy(rto))
          }.getOrElse {
            // otherwise do actual unboxing
            records += Unbox(code, from, Val.Local(to, op.resty))
            inst
          }

        case inst =>
          inst
      }

      Seq(block.copy(insts = newInsts))
  }
}

object LocalBoxingElimination extends PassCompanion {
  private sealed abstract class Record
  private final case class Box(code: Char, from: nir.Val, to: nir.Val)
      extends Record
  private final case class Unbox(code: Char, from: nir.Val, to: nir.Val)
      extends Record

  private val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  private val BoxTo: Map[Global, Char] = Seq(
      'B' -> "boxToBoolean_bool_class.java.lang.Boolean",
      'C' -> "boxToCharacter_i16_class.java.lang.Character",
      'Z' -> "boxToByte_i8_class.java.lang.Byte",
      'S' -> "boxToShort_i16_class.java.lang.Short",
      'I' -> "boxToInteger_i32_class.java.lang.Integer",
      'L' -> "boxToLong_i64_class.java.lang.Long",
      'F' -> "boxToFloat_f32_class.java.lang.Float",
      'D' -> "boxToDouble_f64_class.java.lang.Double"
  ).map {
    case (code, id) =>
      Global.Member(BoxesRunTime, id) -> code
  }.toMap

  private val UnboxTo: Map[Global, Char] = Seq(
      'B' -> "unboxToBoolean_class.java.lang.Object_bool",
      'C' -> "unboxToChar_class.java.lang.Object_i16",
      'Z' -> "unboxToByte_class.java.lang.Object_i8",
      'S' -> "unboxToShort_class.java.lang.Object_i16",
      'I' -> "unboxToInt_class.java.lang.Object_i32",
      'L' -> "unboxToLong_class.java.lang.Object_i64",
      'F' -> "unboxToFloat_class.java.lang.Object_f32",
      'D' -> "unboxToDouble_class.java.lang.Object_f64"
  ).map {
    case (code, id) =>
      Global.Member(BoxesRunTime, id) -> code
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
