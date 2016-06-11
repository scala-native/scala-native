package scala.scalanative
package compiler
package pass

import util.unreachable
import nir._

/** Eliminates returns of Unit values and replaces them with void. */
class UnitLowering(implicit fresh: Fresh) extends Pass {
  import UnitLowering._

  override def preInst = {
    case inst @ Inst(n, op) if op.resty == Type.Unit =>
      Seq(
          Inst(op),
          Inst(n, Op.Copy(Val.Unit))
      )
  }

  override def preCf = {
    case Cf.Ret(v) if v.ty == Type.Unit =>
      Cf.Ret(Val.None)
  }

  override def preVal = {
    case Val.Unit => unit
  }

  override def preType = {
    case Type.Unit =>
      Type.Ptr

    case Type.Function(params, Type.Unit) =>
      Type.Function(params, Type.Void)
  }
}

object UnitLowering extends PassCompanion {
  def apply(ctx: Ctx) = new UnitLowering()(ctx.fresh)

  val unitName = Global.Top("scala.scalanative.runtime.BoxedUnit$")
  val unit     = Val.Global(unitName, Type.Ptr)
  val unitTy   = Type.Struct(unitName tag "module" tag "class", Seq(Type.Ptr))
  val unitConst =
    Val.Global(unitName tag "module" tag "class" tag "type", Type.Ptr)
  val unitValue = Val.Struct(unitTy.name, Seq(unitConst))
  val unitDefn  = Defn.Const(Attrs.None, unitName, unitTy, unitValue)

  override val depends = Seq(unitName)
  override val injects = Seq(unitDefn)
}
