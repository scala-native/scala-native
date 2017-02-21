package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top

import nir._
import Inst.Let
import Bin._
import Comp._
import Conv._

import scala.None

/** For operations with constant operands and no side-effects, compute the resulting value */
class ConstantFolding extends Pass {
  import ConstantFolding._

  val emulate: PartialFunction[Op, Val] = {
    /*  BIN  */

    case Op.Bin(Iadd, ty, IVal(a), IVal(b)) =>
      IVal(a + b, ty)

    case Op.Bin(Isub, ty, IVal(a), IVal(b)) =>
      IVal(a - b, ty)

    case Op.Bin(Imul, ty, IVal(a), IVal(b)) =>
      IVal(a * b, ty)

    case Op.Bin(Sdiv, ty, _, IVal(0)) =>
      Val.Undef(ty)
    case Op.Bin(Sdiv, ty, IVal(a), IVal(b)) =>
      IVal(a / b, ty)

    case Op.Bin(Udiv, ty, _, IVal(0)) =>
      Val.Undef(ty)
    case Op.Bin(Udiv, _, Val.Byte(a), Val.Byte(b)) =>
      Val.Byte(Unsigned.Div(a, b))
    case Op.Bin(Udiv, _, Val.Short(a), Val.Short(b)) =>
      Val.Short(Unsigned.Div(a, b))
    case Op.Bin(Udiv, _, Val.Int(a), Val.Int(b)) =>
      Val.Int(Unsigned.Div(a, b))
    case Op.Bin(Udiv, _, Val.Long(a), Val.Long(b)) =>
      Val.Long(Unsigned.Div(a, b))

    case Op.Bin(Srem, ty, _, IVal(0)) =>
      Val.Undef(ty)
    case Op.Bin(Srem, ty, IVal(a), IVal(b)) =>
      IVal(a % b, ty)

    case Op.Bin(Urem, ty, _, IVal(0)) =>
      Val.Undef(ty)
    case Op.Bin(Urem, _, Val.Byte(a), Val.Byte(b)) =>
      Val.Byte(Unsigned.Rem(a, b))
    case Op.Bin(Urem, _, Val.Short(a), Val.Short(b)) =>
      Val.Short(Unsigned.Rem(a, b))
    case Op.Bin(Urem, _, Val.Int(a), Val.Int(b)) =>
      Val.Int(Unsigned.Rem(a, b))
    case Op.Bin(Urem, _, Val.Long(a), Val.Long(b)) =>
      Val.Long(Unsigned.Rem(a, b))

    case Op.Bin(Shl, ty, IVal(a), IVal(b)) =>
      IVal(a << b, ty)

    case Op.Bin(Lshr, ty, IVal(a), IVal(b)) =>
      IVal(a >>> b, ty)

    case Op.Bin(Ashr, ty, IVal(a), IVal(b)) =>
      IVal(a >> b, ty)

    case Op.Bin(And, ty, IVal(a), IVal(b)) =>
      IVal(a & b, ty)

    case Op.Bin(Or, ty, IVal(a), IVal(b)) =>
      IVal(a | b, ty)

    case Op.Bin(Xor, ty, IVal(a), IVal(b)) =>
      IVal(a ^ b, ty)

    /*  COMP  */

    case Op.Comp(Ieq, ty, IVal(a), IVal(b)) =>
      Val.Bool(a == b)
    case Op.Comp(Ine, ty, IVal(a), IVal(b)) =>
      Val.Bool(a != b)

    case Op.Comp(Ugt, ty, IVal(a), IVal(b)) =>
      Val.Bool(java.lang.Long.compareUnsigned(a, b) > 0)
    case Op.Comp(Uge, ty, IVal(a), IVal(b)) =>
      Val.Bool(java.lang.Long.compareUnsigned(a, b) >= 0)
    case Op.Comp(Ult, ty, IVal(a), IVal(b)) =>
      Val.Bool(java.lang.Long.compareUnsigned(a, b) < 0)
    case Op.Comp(Ule, ty, IVal(a), IVal(b)) =>
      Val.Bool(java.lang.Long.compareUnsigned(a, b) <= 0)

    case Op.Comp(Sgt, ty, IVal(a), IVal(b)) =>
      Val.Bool(a > b)
    case Op.Comp(Sge, ty, IVal(a), IVal(b)) =>
      Val.Bool(a >= b)
    case Op.Comp(Slt, ty, IVal(a), IVal(b)) =>
      Val.Bool(a < b)
    case Op.Comp(Sle, ty, IVal(a), IVal(b)) =>
      Val.Bool(a <= b)

    /*  CONV  */

    case Op.Conv(Trunc, ty, IVal(i)) =>
      IVal(i, ty)

    case Op.Conv(Zext, ty, Val.Byte(b)) =>
      IVal(java.lang.Byte.toUnsignedLong(b), ty)
    case Op.Conv(Zext, ty, Val.Short(s)) =>
      IVal(java.lang.Short.toUnsignedLong(s), ty)
    case Op.Conv(Zext, ty, Val.Int(i)) =>
      IVal(java.lang.Integer.toUnsignedLong(i), ty)
    case Op.Conv(Zext, ty, Val.Long(l)) =>
      IVal(l, ty)

    case Op.Conv(Sext, ty, IVal(i)) =>
      IVal(i, ty)

    /* Select */
    case Op.Select(Val.True, thenv, _) =>
      thenv
    case Op.Select(Val.False, _, elsev) =>
      elsev
  }

  override def onInst(inst: Inst): Inst = inst match {
    case inst @ Inst.Let(n, op) =>
      val newInst = emulate.lift(op) match {
        case Some(newVal) => Let(n, Op.Copy(newVal))
        case None         => inst
      }
      newInst

    case _ =>
      inst
  }
}

object ConstantFolding extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ConstantFolding

  object IVal {
    def unapply(v: Val): Option[Long] = {
      v match {
        case Val.Byte(b)  => Some(b)
        case Val.Short(s) => Some(s)
        case Val.Int(i)   => Some(i)
        case Val.Long(l)  => Some(l)

        case Val.False => Some(0)
        case Val.True  => Some(-1)

        case _ => None
      }
    }

    def apply(value: Long, ty: Type): Val = {
      ty match {
        case Type.Byte  => Val.Byte(value.toByte)
        case Type.Short => Val.Short(value.toShort)
        case Type.Char  => Val.Short(value.toShort)
        case Type.Int   => Val.Int(value.toInt)
        case Type.Long  => Val.Long(value)

        case Type.Bool if (value == 0)  => Val.False
        case Type.Bool if (value == -1) => Val.True
        case Type.Bool =>
          throw new IllegalArgumentException(
            s"Can't cast value $value to Boolean")

        case _ =>
          throw new IllegalArgumentException(
            s"Type ${nir.Show(ty)} is not an integer type")
      }
    }
  }

  class UnsignedOp(baseOp: (Long, Long) => Long) {

    def apply(a: Byte, b: Byte): Byte = {
      val ua = java.lang.Byte.toUnsignedLong(a)
      val ub = java.lang.Byte.toUnsignedLong(b)
      baseOp(ua, ub).toByte
    }

    def apply(a: Short, b: Short): Short = {
      val ua = java.lang.Short.toUnsignedLong(a)
      val ub = java.lang.Short.toUnsignedLong(b)
      baseOp(ua, ub).toShort
    }

    def apply(a: Int, b: Int): Int = {
      val ua = java.lang.Integer.toUnsignedLong(a)
      val ub = java.lang.Integer.toUnsignedLong(b)
      baseOp(ua, ub).toInt
    }

    def apply(a: Long, b: Long): Long = {
      baseOp(a, b)
    }
  }

  object Unsigned {

    val Div = new UnsignedOp((a, b) => java.lang.Long.divideUnsigned(a, b))
    val Rem = new UnsignedOp((a, b) => java.lang.Long.remainderUnsigned(a, b))

  }
}
