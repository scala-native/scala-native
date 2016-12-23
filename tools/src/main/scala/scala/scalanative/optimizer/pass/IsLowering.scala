package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._, Inst.Let

/** Translates instance checks to range checks on type ids. */
class IsLowering(implicit fresh: Fresh, top: Top) extends Pass {

  override def preInst = eliminateIs

  private def eliminateIs: PartialFunction[Inst, Seq[Inst]] = {
    case Let(n, Op.Is(_, Val.Zero(_))) =>
      Seq(Let(n, Op.Copy(Val.False)))

    case isInst @ Let(n, Op.Is(ty, obj)) =>
      val isNullV = Val.Local(fresh(), Type.Bool)
      val isIsV   = Val.Local(fresh(), Type.Bool)

      val thenL = fresh()
      val elseL = fresh()
      val contL = fresh()

      val thenResultV = Val.Local(fresh(), Type.Bool)
      val elseResultV = Val.Local(fresh(), Type.Bool)

      Seq(
        // if
        Let(isNullV.name,
            Op.Comp(Comp.Ieq, Type.Ptr, obj, Val.Zero(Type.Ptr))),
        Inst.If(isNullV, Next(thenL), Next(elseL)),
        // then
        Inst.Label(thenL, Seq.empty),
        Let(thenResultV.name, Op.Copy(Val.False)),
        Inst.Jump(Next.Label(contL, Seq(thenResultV))),
        // else
        Inst.Label(elseL, Seq.empty)
      ) ++
        doEliminateIs(Let(elseResultV.name, Op.Is(ty, obj))) ++
        Seq(Inst.Jump(Next.Label(contL, Seq(elseResultV))),
            // cont
            Inst.Label(contL, Seq(isIsV)),
            Let(n, Op.Copy(isIsV)))
    case other => Seq(other)
  }

  private def doEliminateIs: PartialFunction[Inst, Seq[Inst]] = {
    case Let(n, Op.Is(ClassRef(cls), obj)) if cls.range.length == 1 =>
      val typeptr = Val.Local(fresh(), Type.Ptr)

      Seq(
        Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Let(n, Op.Comp(Comp.Ieq, Type.Ptr, typeptr, cls.typeConst))
      )

    case Let(n, Op.Is(ClassRef(cls), obj)) =>
      val typeptr = Val.Local(fresh(), Type.Ptr)
      val idptr   = Val.Local(fresh(), Type.Ptr)
      val id      = Val.Local(fresh(), Type.I32)
      val ge      = Val.Local(fresh(), Type.Bool)
      val le      = Val.Local(fresh(), Type.Bool)

      Seq(
        Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Let(idptr.name,
            Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
        Let(id.name, Op.Load(Type.I32, idptr)),
        Let(ge.name,
            Op.Comp(Comp.Sle, Type.I32, Val.I32(cls.range.start), id)),
        Let(le.name, Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end))),
        Let(n, Op.Bin(Bin.And, Type.Bool, ge, le))
      )

    case Let(n, Op.Is(TraitRef(trt), obj)) =>
      val typeptr = Val.Local(fresh(), Type.Ptr)
      val idptr   = Val.Local(fresh(), Type.Ptr)
      val id      = Val.Local(fresh(), Type.I32)
      val boolptr = Val.Local(fresh(), Type.Ptr)

      Seq(
        Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Let(idptr.name,
            Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
        Let(id.name, Op.Load(Type.I32, idptr)),
        Let(boolptr.name,
            Op.Elem(top.instanceTy,
                    top.instanceVal,
                    Seq(Val.I32(0), id, Val.I32(trt.id)))),
        Let(n, Op.Load(Type.Bool, boolptr))
      )
    case other => Seq(other)
  }
}

object IsLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new IsLowering()(top.fresh, top)
}
