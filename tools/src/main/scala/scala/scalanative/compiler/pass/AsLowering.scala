package scala.scalanative
package compiler
package pass

import nir._, Inst.Let

/** Translates high-level casts to corresponding low-level instructions. */
class AsLowering extends Pass {
  override def preInst = {
    case Let(n, Op.As(ty1, Of(v, ty2))) if ty1 == ty2 =>
      Seq(Let(n, Op.Copy(v)))
    case Let(n, Op.As(_: Type.RefKind, Of(v, _: Type.RefKind))) =>
      Seq(Let(n, Op.Copy(v)))
    case Let(n, Op.As(to @ Type.I(w1), Of(v, Type.I(w2)))) if w1 > w2 =>
      Seq(Let(n, Op.Conv(Conv.Sext, to, v)))
    case Let(n, Op.As(to @ Type.I(w1), Of(v, Type.I(w2)))) if w1 < w2 =>
      Seq(Let(n, Op.Conv(Conv.Trunc, to, v)))
    case Let(n, Op.As(to @ Type.I(_), Of(v, Type.F(_)))) =>
      Seq(Let(n, Op.Conv(Conv.Fptosi, to, v)))
    case Let(n, Op.As(to @ Type.F(_), Of(v, Type.I(_)))) =>
      Seq(Let(n, Op.Conv(Conv.Sitofp, to, v)))
    case Let(n, Op.As(to @ Type.F(w1), Of(v, Type.F(w2)))) if w1 > w2 =>
      Seq(Let(n, Op.Conv(Conv.Fpext, to, v)))
    case Let(n, Op.As(to @ Type.F(w1), Of(v, Type.F(w2)))) if w1 < w2 =>
      Seq(Let(n, Op.Conv(Conv.Fptrunc, to, v)))
    case Let(n, Op.As(Type.Ptr, Of(v, _: Type.RefKind))) =>
      Seq(Let(n, Op.Conv(Conv.Bitcast, Type.Ptr, v)))
    case Let(n, Op.As(to @ (_: Type.RefKind), Of(v, Type.Ptr))) =>
      Seq(Let(n, Op.Conv(Conv.Bitcast, to, v)))
    case inst @ Let(n, Op.As(to, Of(v, from))) =>
      util.unsupported(inst)
  }

  object Of {
    def unapply(v: Val): Some[(Val, Type)] = Some((v, v.ty))
  }
}

object AsLowering extends PassCompanion {
  def apply(ctx: Ctx) = new AsLowering
}
