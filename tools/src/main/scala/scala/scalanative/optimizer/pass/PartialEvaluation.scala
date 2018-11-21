package scala.scalanative
package optimizer
package pass

import nir._
import Inst.Let
import Bin._
import Comp._

import scala.None

/** Simplifies single instruction patterns */
class PartialEvaluation extends Pass {
  import PartialEvaluation._
  import ConstantFolding._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = insts.map {

    /* Iadd */
    case Let(n, Op.Bin(Iadd, ty, lhs, IVal(0)), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Iadd, ty, lhs, rhs), unwind) if lhs == rhs =>
      Let(n, Op.Bin(Imul, ty, lhs, IVal(2, ty)), unwind)

    /* Isub */
    case Let(n, Op.Bin(Isub, ty, lhs, IVal(0)), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Isub, ty, lhs, IVal(i)), unwind) if i < 0 =>
      Let(n, Op.Bin(Iadd, ty, lhs, IVal(-i, ty)), unwind)

    case Let(n, Op.Bin(Isub, ty, lhs, rhs), unwind) if lhs == rhs =>
      copy(n, IVal(0, ty), unwind)

    /* Imul */
    case Let(n, Op.Bin(Imul, ty, lhs, IVal(0)), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Imul, ty, lhs, IVal(1)), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Imul, ty, lhs, IVal(-1)), unwind) =>
      Let(n, Op.Bin(Isub, ty, IVal(0, ty), lhs), unwind)

    case Let(n, Op.Bin(Imul, ty, lhs, PowerOf2(shift)), unwind) =>
      Let(n, Op.Bin(Shl, ty, lhs, shift), unwind)

    /* Sdiv */
    case Let(n, Op.Bin(Sdiv, ty, _, IVal(0)), unwind) =>
      copy(n, Val.Undef(ty), unwind)

    case Let(n, Op.Bin(Sdiv, ty, lhs, IVal(1)), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Sdiv, ty, lhs, IVal(-1)), unwind) =>
      Let(n, Op.Bin(Isub, ty, IVal(0, ty), lhs), unwind)

    case Let(n, Op.Bin(Sdiv, ty, IVal(0), _), unwind) =>
      copy(n, IVal(0, ty), unwind)

    /* Udiv */
    case Let(n, Op.Bin(Udiv, ty, _, IVal(0)), unwind) =>
      copy(n, Val.Undef(ty), unwind)

    case Let(n, Op.Bin(Udiv, ty, lhs, IVal(1)), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Udiv, ty, IVal(0), _), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Udiv, ty, lhs, PowerOf2(shift)), unwind) =>
      Let(n, Op.Bin(Lshr, ty, lhs, shift), unwind)

    /* Srem */
    case Let(n, Op.Bin(Srem, ty, lhs, IVal(0)), unwind) =>
      copy(n, Val.Undef(ty), unwind)

    case Let(n, Op.Bin(Srem, ty, lhs, IVal(1)), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Srem, ty, lhs, IVal(-1)), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Srem, ty, IVal(0), rhs), unwind) =>
      copy(n, IVal(0, ty), unwind)

    /* Urem */
    case Let(n, Op.Bin(Urem, ty, lhs, IVal(0)), unwind) =>
      copy(n, Val.Undef(ty), unwind)

    case Let(n, Op.Bin(Urem, ty, lhs, IVal(1)), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Urem, ty, IVal(0), rhs), unwind) =>
      copy(n, IVal(0, ty), unwind)

    /* Shl */
    case Let(n, Op.Bin(Shl, Type.Int, lhs, Val.Int(a)), unwind)
        if (a & 31) == 0 =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Shl, Type.Long, lhs, Val.Long(a)), unwind)
        if (a & 63) == 0 =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Shl, ty, IVal(0), _), unwind) =>
      copy(n, IVal(0, ty), unwind)

    /* Lshr */
    case Let(n, Op.Bin(Lshr, Type.Int, lhs, Val.Int(a)), unwind)
        if (a & 31) == 0 =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Lshr, Type.Long, lhs, Val.Long(a)), unwind)
        if (a & 63) == 0 =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Lshr, ty, IVal(0), rhs), unwind) =>
      copy(n, IVal(0, ty), unwind)

    /* Ashr */
    case Let(n, Op.Bin(Ashr, Type.Int, lhs, Val.Int(a)), unwind)
        if (a & 31) == 0 =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Ashr, Type.Long, lhs, Val.Long(a)), unwind)
        if (a & 63) == 0 =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Ashr, ty, IVal(0), rhs), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Ashr, ty, IVal(-1), rhs), unwind) =>
      copy(n, IVal(-1, ty), unwind)

    /* And */
    case Let(n, Op.Bin(And, ty, lhs, rhs), unwind) if lhs == rhs =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(And, ty, lhs, IVal(0)), unwind) =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(And, ty, lhs, IVal(-1)), unwind) =>
      copy(n, lhs, unwind)

    /* Or */
    case Let(n, Op.Bin(Or, ty, lhs, rhs), unwind) if lhs == rhs =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Or, ty, lhs, IVal(0)), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Bin(Or, ty, lhs, IVal(-1)), unwind) =>
      copy(n, IVal(-1, ty), unwind)

    /* Xor */
    case Let(n, Op.Bin(Xor, ty, lhs, rhs), unwind) if lhs == rhs =>
      copy(n, IVal(0, ty), unwind)

    case Let(n, Op.Bin(Xor, ty, lhs, IVal(0)), unwind) =>
      copy(n, lhs, unwind)

    /* Ieq */
    case Let(n, Op.Comp(Ieq, ty, lhs, rhs), unwind) if lhs == rhs =>
      copy(n, Val.True, unwind)

    case Let(n, Op.Comp(Ieq, ty, lhs, Val.True), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Comp(Ieq, ty, lhs, Val.False), unwind) =>
      neg(n, lhs, unwind)

    /* Ine */
    case Let(n, Op.Comp(Ine, ty, lhs, rhs), unwind) if lhs == rhs =>
      copy(n, Val.False, unwind)

    case Let(n, Op.Comp(Ine, ty, lhs, Val.False), unwind) =>
      copy(n, lhs, unwind)

    case Let(n, Op.Comp(Ine, ty, lhs, Val.True), unwind) =>
      neg(n, lhs, unwind)

    /* Ugt */
    case Let(n, Op.Comp(Ugt, ty, lhs, IVal(a)), unwind) if a == umaxValue(ty) =>
      copy(n, Val.False, unwind)

    /* Uge */
    case Let(n, Op.Comp(Uge, ty, lhs, IVal(a)), unwind) if a == uminValue(ty) =>
      copy(n, Val.True, unwind)

    /* Ult */
    case Let(n, Op.Comp(Ult, ty, lhs, IVal(a)), unwind) if a == uminValue(ty) =>
      copy(n, Val.False, unwind)

    /* Ule */
    case Let(n, Op.Comp(Ule, ty, lhs, IVal(a)), unwind) if a == umaxValue(ty) =>
      copy(n, Val.True, unwind)

    /* Sgt */
    case Let(n, Op.Comp(Sgt, ty, lhs, IVal(a)), unwind) if a == maxValue(ty) =>
      copy(n, Val.False, unwind)

    /* Sge */
    case Let(n, Op.Comp(Sge, ty, lhs, IVal(a)), unwind) if a == minValue(ty) =>
      copy(n, Val.True, unwind)

    /* Slt */
    case Let(n, Op.Comp(Slt, ty, lhs, IVal(a)), unwind) if a == minValue(ty) =>
      copy(n, Val.False, unwind)

    /* Sle */
    case Let(n, Op.Comp(Sle, ty, lhs, IVal(a)), unwind) if a == maxValue(ty) =>
      copy(n, Val.True, unwind)

    case inst =>
      inst
  }

  private def copy(n: Local, value: Val, unwind: Next): Inst =
    Let(n, Op.Copy(value), unwind)

  private def neg(n: Local, value: Val, unwind: Next): Inst =
    Let(n, Op.Bin(Xor, Type.Bool, value, Val.True), unwind)
}

object PartialEvaluation extends PassCompanion {
  override def apply(config: build.Config, linked: linker.Result) =
    new PartialEvaluation

  object PowerOf2 {
    def unapply(v: Val): Option[Val] = {
      v match {
        case Val.Byte(b) =>
          extractPow2(b).map(p => Val.Byte(p.toByte))

        case Val.Short(s) =>
          extractPow2(s).map(p => Val.Short(p.toShort))

        case Val.Int(i) =>
          extractPow2(i).map(p => Val.Int(p.toInt))

        case Val.Long(l) =>
          extractPow2(l).map(p => Val.Long(p.toLong))

        case _ => None
      }
    }

    def log2(x: Double): Double =
      math.log(x) / math.log(2)

    def extractPow2(x: Double): Option[Double] = {
      if (x < 1)
        None
      else {
        val log = log2(x)
        if (math.floor(log) == log)
          Some(log)
        else
          None
      }
    }
  }

  private def minValue(ty: Type): Long = ty match {
    case Type.Byte  => Byte.MinValue
    case Type.Short => Short.MinValue
    case Type.Int   => Int.MinValue
    case Type.Long  => Long.MinValue
  }

  private def maxValue(ty: Type): Long = ty match {
    case Type.Byte  => Byte.MaxValue
    case Type.Short => Short.MaxValue
    case Type.Int   => Int.MaxValue
    case Type.Long  => Long.MaxValue
  }

  private def uminValue(ty: Type): Long =
    0L

  private def umaxValue(ty: Type): Long =
    minValue(ty)

}
