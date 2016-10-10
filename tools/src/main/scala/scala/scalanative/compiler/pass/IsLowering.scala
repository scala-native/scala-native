package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Translates instance checks to range checks on type ids. */
class IsLowering(implicit fresh: Fresh, top: Top) extends Pass {
  override def preInst = {
    case Inst(n, Op.Is(ClassRef(cls), obj)) if cls.range.length == 1 =>
      val typeptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(n, Op.Comp(Comp.Ieq, Type.Ptr, typeptr, cls.typeConst))
      )

    case Inst(n, Op.Is(ClassRef(cls), obj)) =>
      val typeptr = Val.Local(fresh(), Type.Ptr)
      val idptr   = Val.Local(fresh(), Type.Ptr)
      val id      = Val.Local(fresh(), Type.I32)
      val ge      = Val.Local(fresh(), Type.Bool)
      val le      = Val.Local(fresh(), Type.Bool)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr)),
          Inst(ge.name,
               Op.Comp(Comp.Sle, Type.I32, Val.I32(cls.range.start), id)),
          Inst(le.name,
               Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end))),
          Inst(n, Op.Bin(Bin.And, Type.Bool, ge, le))
      )

    case Inst(n, Op.Is(TraitRef(trt), obj)) =>
      val typeptr = Val.Local(fresh(), Type.Ptr)
      val idptr   = Val.Local(fresh(), Type.Ptr)
      val id      = Val.Local(fresh(), Type.I32)
      val boolptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr)),
          Inst(boolptr.name,
               Op.Elem(top.instanceTy,
                       top.instanceVal,
                       Seq(Val.I32(0), id, Val.I32(trt.id)))),
          Inst(n, Op.Load(Type.Bool, boolptr))
      )
  }
}

object IsLowering extends PassCompanion {
  def apply(ctx: Ctx) = new IsLowering()(ctx.fresh, ctx.top)
}
