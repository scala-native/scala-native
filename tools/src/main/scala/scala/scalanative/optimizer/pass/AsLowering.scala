package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import nir._, Inst.Let

/** Translates high-level casts to corresponding low-level instructions. */
class AsLowering extends Pass {
  override def preInst = {
    case Let(n, Op.As(_: Type.RefKind, Of(v, _: Type.RefKind))) =>
      Seq(Let(n, Op.Copy(v)))
    case inst @ Let(n, Op.As(to, Of(v, from))) =>
      util.unsupported(s"can't cast from $from to $to")
  }

  object Of {
    def unapply(v: Val): Some[(Val, Type)] = Some((v, v.ty))
  }
}

object AsLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new AsLowering
}
