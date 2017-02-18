package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import nir._, Inst.Let

/** Moves constant operands in Op.Bin and Op.Comp to the right. */
class Canonicalization extends Pass {

  override def onInst(inst: Inst): Inst = inst match {
    case Let(n, Op.Bin(bin, ty, lhs, rhs))
        if (commutativeBin(bin) && scalarValue(lhs)) =>
      Let(n, Op.Bin(bin, ty, rhs, lhs))

    case Let(n, Op.Comp(comp, ty, lhs, rhs))
        if (commutativeComp(comp) && scalarValue(lhs)) =>
      Let(n, Op.Comp(comp, ty, rhs, lhs))

    case _ =>
      inst
  }

  // We DO NOT touch floating point operations at all
  private def commutativeBin(bin: Bin): Boolean = {
    import Bin._
    bin match {
      case Iadd | Imul | And | Or | Xor => true
      case Fadd | Isub | Fsub | Fmul | Sdiv | Udiv | Fdiv | Srem | Urem |
          Frem | Shl | Lshr | Ashr =>
        false
    }
  }

  private def commutativeComp(comp: Comp): Boolean = {
    import Comp._
    comp match {
      case Ieq | Ine => true
      case _         => false
    }
  }

  private def scalarValue(value: Val): Boolean = {
    import Val._
    value match {
      case _: Struct | _: Array | _: Local | _: Global | _: Const => false
      case _                                                      => true
    }
  }

}

object Canonicalization extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new Canonicalization
}
