package scala.scalanative
package compiler
package pass

import nir._

/** Translates high-level casts to corresponding low-level instructions. */
class AsLowering extends Pass {
  override def preInst = {
    case Inst(n, Op.As(ty1, Of(v, ty2))) if ty1 == ty2 =>
      Seq(Inst(n, Op.Copy(v)))
    case Inst(n, Op.As(_: Type.RefKind, Of(v, _: Type.RefKind))) =>
      Seq(Inst(n, Op.Copy(v)))
    case Inst(n, Op.As(to @ Type.I(w1), Of(v, Type.I(w2)))) if w1 > w2 =>
      Seq(Inst(n, Op.Conv(Conv.Sext, to, v)))
    case Inst(n, Op.As(to @ Type.I(w1), Of(v, Type.I(w2)))) if w1 < w2 =>
      Seq(Inst(n, Op.Conv(Conv.Trunc, to, v)))
    case Inst(n, Op.As(to @ Type.I(_), Of(v, Type.F(_)))) =>
      Seq(Inst(n, Op.Conv(Conv.Fptosi, to, v)))
    case Inst(n, Op.As(to @ Type.F(_), Of(v, Type.I(_)))) =>
      Seq(Inst(n, Op.Conv(Conv.Sitofp, to, v)))
    case Inst(n, Op.As(to @ Type.F(w1), Of(v, Type.F(w2)))) if w1 > w2 =>
      Seq(Inst(n, Op.Conv(Conv.Fpext, to, v)))
    case Inst(n, Op.As(to @ Type.F(w1), Of(v, Type.F(w2)))) if w1 < w2 =>
      Seq(Inst(n, Op.Conv(Conv.Fptrunc, to, v)))
    case Inst(n, Op.As(Type.Ptr, Of(v, _: Type.RefKind))) =>
      Seq(Inst(n, Op.Conv(Conv.Bitcast, Type.Ptr, v)))
    case Inst(n, Op.As(to @ (_: Type.RefKind), Of(v, Type.Ptr))) =>
      Seq(Inst(n, Op.Conv(Conv.Bitcast, to, v)))
    case inst @ Inst(n, Op.As(to, Of(v, from))) =>
      util.unsupported(inst)
  }

  object Of {
    def unapply(v: Val): Some[(Val, Type)] = Some((v, v.ty))
  }
}

object AsLowering extends PassCompanion {
  def apply(ctx: Ctx) = new AsLowering
}
