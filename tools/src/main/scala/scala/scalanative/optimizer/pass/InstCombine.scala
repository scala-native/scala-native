package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import analysis.ControlFlow

import nir._
import Inst._
import Bin._
import Comp._

import scala.None
import scala.collection.mutable

class InstCombine()(implicit fresh: Fresh) extends Pass {
  import InstCombine._
  import ConstantFolding._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    // This stores the encountered definitions for non-params locals
    val buf      = new nir.Buffer
    val defop    = mutable.HashMap.empty[Local, Op]
    val resolver = new DefOp(defop)
    val cfg      = ControlFlow.Graph(insts)

    /* Because of the pre-order traversal of the CFG, the definitions will be
     * seen before their use
     */
    cfg.foreach { b =>
      buf += b.label
      b.insts.foreach { inst =>
        val simplifiedSeq = simplify(inst, resolver)
        simplifiedSeq.foreach {
          case Let(n, op) => defop += (n -> op)
          case _          =>
        }
        buf ++= simplifiedSeq
      }
    }

    buf.toSeq
  }

  private def simplify(inst: Inst, defop: DefOp): Seq[Inst] = {
    val singleSimp = inst match {

      case Let(z, Op.Bin(Iadd, ty, y, IVal(a))) =>
        defop(y) match {
          // (x + b) + a = x + (a + b)
          case Some(Op.Bin(Iadd, _, x, IVal(b))) =>
            Let(z, Op.Bin(Iadd, ty, x, IVal(a + b, ty)))

          // (x - b) + a = x + (a - b)
          case Some(Op.Bin(Isub, _, x, IVal(b))) =>
            Let(z, Op.Bin(Iadd, ty, x, IVal(a - b, ty)))

          case _ => inst
        }

      case Let(z, Op.Bin(Isub, ty, y, IVal(a))) =>
        defop(y) match {
          // (x - b) - a = x - (a + b)
          case Some(Op.Bin(Isub, _, x, IVal(b))) =>
            Let(z, Op.Bin(Isub, ty, x, IVal(a + b, ty)))

          // (x + b) - a = x - (a - b)
          case Some(Op.Bin(Iadd, _, x, IVal(b))) =>
            Let(z, Op.Bin(Isub, ty, x, IVal(a - b, ty)))

          case _ => inst
        }

      case Let(z, Op.Bin(Imul, ty, y, IVal(a))) =>
        defop(y) match {
          // (x * b) * a = x * (a * b)
          case Some(Op.Bin(Imul, _, x, IVal(b))) =>
            Let(z, Op.Bin(Imul, ty, x, IVal(a * b, ty)))

          // (x << b) * a = x * (a * 2^b)
          case Some(Op.Bin(Shl, _, x, IVal(b))) =>
            Let(z, Op.Bin(Imul, ty, x, IVal(a * math.pow(2, b).toLong, ty)))

          case _ => inst
        }

      case Let(n, Op.Comp(Ieq, tyIeq, compared, IVal(0))) =>
        defop(compared) match {
          // ((x xor y) == 0) = (x == y)
          case Some(Op.Bin(Xor, tyXor, x, y)) if (tyIeq == tyXor) =>
            Let(n, Op.Comp(Ieq, tyIeq, x, y))

          case _ => inst
        }

      case Let(n, Op.Comp(Ine, tyIne, compared, IVal(0))) =>
        defop(compared) match {
          // ((x xor y) != 0) = (x != y)
          case Some(Op.Bin(Xor, tyXor, x, y)) if (tyIne == tyXor) =>
            Let(n, Op.Comp(Ine, tyIne, x, y))

          case _ => inst
        }

      case Let(n, Op.Select(cond, thenv, elsev)) =>
        defop(cond) match {
          // select (c xor true) then a else b  =  select c then b else a
          case Some(Op.Bin(Xor, _, negCond, Val.True)) =>
            Let(n, Op.Select(negCond, elsev, thenv))

          case _ => inst
        }

      case If(cond, thenp, elsep) =>
        defop(cond) match {
          // if (c xor true) then a else b  =  if c then b else a
          case Some(Op.Bin(Xor, _, negCond, Val.True)) =>
            If(negCond, elsep, thenp)

          case _ => inst
        }

      case Let(n, op @ Op.Bin(_, _, lhs, rhs)) =>
        Let(n, simplifyBin(defop(lhs), defop(rhs), op))

      // Nothing
      case _ => inst
    }

    simplifyExt(singleSimp, defop)
  }

  def simplifyBin(lhsDef: Option[Op], rhsDef: Option[Op], op: Op.Bin): Op = {
    (lhsDef, rhsDef, op) match {

      // (x << a) * b = x * (2^a * b)  [i32]
      case (Some(Op.Bin(Shl, Type.Int, x, Val.Int(a))),
            _,
            Op.Bin(Imul, Type.Int, _, Val.Int(b))) =>
        Op.Bin(Imul, Type.Int, x, Val.Int(math.pow(2, a & 31).toInt * b))

      // (x << a) * b = x * (2^a * b)  [i64]
      case (Some(Op.Bin(Shl, Type.Long, x, Val.Long(a))),
            _,
            Op.Bin(Imul, Type.Long, _, Val.Long(b))) =>
        Op.Bin(Imul, Type.Long, x, Val.Long(math.pow(2, a & 63).toLong * b))

      // (x * a) << b = x * (a * 2^b)  [i32]
      case (Some(Op.Bin(Imul, Type.Int, x, Val.Int(a))),
            _,
            Op.Bin(Shl, Type.Int, _, Val.Int(b))) =>
        Op.Bin(Imul, Type.Int, x, Val.Int(a * math.pow(2, b & 31).toInt))

      // (x * a) << b = x * (a * 2^b)  [i64]
      case (Some(Op.Bin(Imul, Type.Long, x, Val.Long(a))),
            _,
            Op.Bin(Shl, Type.Long, _, Val.Long(b))) =>
        Op.Bin(Imul, Type.Long, x, Val.Long(a * math.pow(2, b & 63).toLong))

      // x + (0 - y) = x - y
      case (_, Some(Op.Bin(Isub, _, IVal(0), y)), Op.Bin(Iadd, ty, x, _)) =>
        Op.Bin(Isub, ty, x, y)

      // x - (0 - y) = x + y
      case (_, Some(Op.Bin(Isub, _, IVal(0), y)), Op.Bin(Isub, ty, x, _)) =>
        Op.Bin(Iadd, ty, x, y)

      // (0 - x) + y = y - x
      case (Some(Op.Bin(Isub, _, IVal(0), x)), _, Op.Bin(Iadd, ty, _, y)) =>
        Op.Bin(Isub, ty, y, x)

      // (x - y) + y = x
      case (Some(Op.Bin(Isub, _, x, y)), _, Op.Bin(Iadd, _, _, c))
          if (y == c) =>
        Op.Copy(x)

      // (x + y) - y = x
      case (Some(Op.Bin(Iadd, _, x, y)), _, Op.Bin(Isub, _, _, c))
          if (y == c) =>
        Op.Copy(x)

      case _ => op
    }
  }

  def simplifyExt(inst: Inst, defop: DefOp)(implicit fresh: Fresh): Seq[Inst] = {
    inst match {
      // (x * z) + (y * z) = (x + y) * z
      case Let(n,
               Op.Bin(Iadd,
                      ty,
                      defop(Op.Bin(Imul, _, x, z)),
                      defop(Op.Bin(Imul, _, y, d)))) if (z == d) =>
        val tmp = fresh()
        Seq(Let(tmp, Op.Bin(Iadd, ty, x, y)),
            Let(n, Op.Bin(Imul, ty, Val.Local(tmp, ty), z)))

      // unbox (box x) = x
      case Let(n, Op.Unbox(_, defop(Op.Box(_, x)))) =>
        Seq(Let(n, Op.Copy(x)))

      // (x or a) or b = x or (a or b)
      case Let(n, Op.Bin(Or, ty, defop(Op.Bin(Or, _, x, IVal(a))), IVal(b))) =>
        Seq(Let(n, Op.Bin(Or, ty, x, IVal(a | b, ty))))

      // (x and a) and b = x and (a and b)
      case Let(n,
               Op.Bin(And, ty, defop(Op.Bin(And, _, x, IVal(a))), IVal(b))) =>
        Seq(Let(n, Op.Bin(And, ty, x, IVal(a & b, ty))))

      // (x xor a) xor b = x xor (a xor b)
      case Let(n,
               Op.Bin(Xor, ty, defop(Op.Bin(Xor, _, x, IVal(a))), IVal(b))) =>
        Seq(Let(n, Op.Bin(Xor, ty, x, IVal(a ^ b, ty))))

      // (x << a) << b = x << (a + b)  [i32]
      case Let(n,
               Op.Bin(Shl,
                      Type.Int,
                      defop(Op.Bin(Shl, Type.Int, x, Val.Int(a))),
                      Val.Int(b))) =>
        val totShift = (a & 31) + (b & 31)
        if (totShift >= 32)
          Seq(Let(n, Op.Copy(Val.Int(0))))
        else
          Seq(Let(n, Op.Bin(Shl, Type.Int, x, Val.Int(totShift))))

      // (x << a) << b = x << (a + b)  [i64]
      case Let(n,
               Op.Bin(Shl,
                      Type.Long,
                      defop(Op.Bin(Shl, Type.Long, x, Val.Long(a))),
                      Val.Long(b))) =>
        val totShift = (a & 63) + (b & 63)
        if (totShift >= 64)
          Seq(Let(n, Op.Copy(Val.Long(0))))
        else
          Seq(Let(n, Op.Bin(Shl, Type.Long, x, Val.Long(totShift))))

      // (x >>> a) >>> b = x >>> (a + b)  [i32]
      case Let(n,
               Op.Bin(Lshr,
                      Type.Int,
                      defop(Op.Bin(Lshr, Type.Int, x, Val.Int(a))),
                      Val.Int(b))) =>
        val totShift = (a & 31) + (b & 31)
        if (totShift >= 32)
          Seq(Let(n, Op.Copy(Val.Int(0))))
        else
          Seq(Let(n, Op.Bin(Lshr, Type.Int, x, Val.Int(totShift))))

      // (x >>> a) >>> b = x >>> (a + b)  [i64]
      case Let(n,
               Op.Bin(Lshr,
                      Type.Long,
                      defop(Op.Bin(Lshr, Type.Long, x, Val.Long(a))),
                      Val.Long(b))) =>
        val totShift = (a & 63) + (b & 63)
        if (totShift >= 64)
          Seq(Let(n, Op.Copy(Val.Long(0))))
        else
          Seq(Let(n, Op.Bin(Lshr, Type.Long, x, Val.Long(totShift))))

      // (x >> a) >> b = x >> (a + b)  [i32]
      case Let(n,
               Op.Bin(Lshr,
                      Type.Int,
                      defop(Op.Bin(Lshr, Type.Int, x, Val.Int(a))),
                      Val.Int(b))) =>
        val shiftSum = (a & 31) + (b & 31)
        val totShift = math.min(shiftSum, 31)
        Seq(Let(n, Op.Bin(Lshr, Type.Int, x, Val.Int(totShift))))

      // (x >> a) >> b = x >> (a + b)  [i64]
      case Let(n,
               Op.Bin(Lshr,
                      Type.Long,
                      defop(Op.Bin(Lshr, Type.Long, x, Val.Long(a))),
                      Val.Long(b))) =>
        val shiftSum = (a & 63) + (b & 63)
        val totShift = math.min(shiftSum, 63)
        Seq(Let(n, Op.Bin(Lshr, Type.Long, x, Val.Long(totShift))))

      // ((x + a) == b) = (x == (b - a))
      case Let(
          n,
          Op.Comp(Ieq, ty, defop(Op.Bin(Iadd, _, x, IVal(a))), IVal(b))) =>
        Seq(Let(n, Op.Comp(Ieq, ty, x, IVal(b - a, ty))))

      // ((x - a) == b) = (x == (a + b))
      case Let(
          n,
          Op.Comp(Ieq, ty, defop(Op.Bin(Isub, _, x, IVal(a))), IVal(b))) =>
        Seq(Let(n, Op.Comp(Ieq, ty, x, IVal(a + b, ty))))

      // ((a - x) == b) = (x == (a - b))
      case Let(
          n,
          Op.Comp(Ieq, ty, defop(Op.Bin(Isub, _, IVal(a), x)), IVal(b))) =>
        Seq(Let(n, Op.Comp(Ieq, ty, x, IVal(a - b, ty))))

      // ((x xor a) == b) = (x == (a xor b))
      case Let(n,
               Op.Comp(Ieq, ty, defop(Op.Bin(Xor, _, x, IVal(a))), IVal(b))) =>
        Seq(Let(n, Op.Comp(Ieq, ty, x, IVal(a ^ b, ty))))

      // ((x xor true) == y) = (x != y)
      case Let(n,
               Op.Comp(Ieq,
                       Type.Bool,
                       defop(Op.Bin(Xor, Type.Bool, x, Val.True)),
                       y)) =>
        Seq(Let(n, Op.Comp(Ine, Type.Bool, x, y)))

      // (x == (y xor true)) = (x != y)
      case Let(n,
               Op.Comp(Ieq,
                       Type.Bool,
                       x,
                       defop(Op.Bin(Xor, Type.Bool, y, Val.True)))) =>
        Seq(Let(n, Op.Comp(Ine, Type.Bool, x, y)))

      case _ => Seq(inst)
    }
  }

}

object InstCombine extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new InstCombine()(top.fresh)

  class DefOp(val defops: mutable.HashMap[Local, Op]) {

    def apply(value: Val): Option[Op] = {
      unapply(value)
    }

    def unapply(value: Val): Option[Op] = {
      value match {
        case Val.Local(l, _) => defops.get(l)
        case _               => None
      }
    }

  }
}
