package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import util.{unreachable, ScopedVar}, ScopedVar.scoped
import analysis.ClassHierarchy.Top
import nir._

/** Eliminates returns of Unit values and replaces them with void. */
class UnitLowering(implicit fresh: Fresh) extends Pass {
  import UnitLowering._

  private var defnRetty: Type = _

  override def onDefn(defn: Defn) = super.onDefn {
    defn match {
      case defn @ Defn.Define(_, _, Type.Function(_, retty), blocks) =>
        defnRetty = retty
      case _ =>
        ()
    }
    defn
  }

  override def onInsts(insts: Seq[Inst]) = {
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case inst @ Inst.Let(n, op) if op.resty == Type.Unit =>
        let(super.onOp(op))
        let(n, Op.Copy(unit))

      case Inst.Ret(_) if defnRetty == Type.Unit =>
        ret(Val.None)

      case inst =>
        buf += super.onInst(inst)
    }

    buf.toSeq
  }

  override def onVal(value: Val) = value match {
    case Val.Unit =>
      unit

    case _ =>
      super.onVal(value)
  }

  override def onType(ty: Type) = ty match {
    case Type.Unit =>
      Type.Ptr

    case Type.Function(params, Type.Unit) =>
      Type.Function(params, Type.Void)

    case _ =>
      super.onType(ty)
  }
}

object UnitLowering extends PassCompanion {
  val unitName  = Global.Top("scala.scalanative.runtime.BoxedUnit$")
  val unit      = Val.Global(unitName, Type.Ptr)
  val unitTy    = Type.Struct(unitName member "layout", Seq(Type.Ptr))
  val unitConst = Val.Global(unitName member "type", Type.Ptr)
  val unitValue = Val.Struct(unitTy.name, Seq(unitConst))
  val unitDefn  = Defn.Const(Attrs.None, unitName, unitTy, unitValue)

  override val depends =
    Seq(unitName)

  override val injects =
    Seq(unitDefn)

  override def apply(config: tools.Config, top: Top) =
    new UnitLowering()(top.fresh)
}
