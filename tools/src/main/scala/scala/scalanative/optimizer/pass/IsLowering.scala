package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._, Inst.Let

/** Translates instance checks to range checks on type ids. */
class IsLowering(implicit fresh: Fresh, top: Top) extends Pass {

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val buf = mutable.UnrolledBuffer.empty[Inst]

    insts.foreach {
      case Let(n, Op.Is(_, Val.Zero(_))) =>
        buf += Let(n, Op.Copy(Val.False))

      case Let(n, Op.Is(ty, obj)) =>
        val isNullV = Val.Local(fresh(), Type.Bool)
        val isIsV   = Val.Local(fresh(), Type.Bool)

        val thenL = fresh()
        val elseL = fresh()
        val contL = fresh()

        val thenResultV = Val.Local(fresh(), Type.Bool)
        val elseResultV = Val.Local(fresh(), Type.Bool)

        // if
        buf += Let(isNullV.name,
                   Op.Comp(Comp.Ieq, Type.Ptr, obj, Val.Zero(Type.Ptr)))
        buf += Inst.If(isNullV, Next(thenL), Next(elseL))
        // then
        buf += Inst.Label(thenL, Seq.empty)
        buf += Let(thenResultV.name, Op.Copy(Val.False))
        buf += Inst.Jump(Next.Label(contL, Seq(thenResultV)))
        // else
        buf += Inst.Label(elseL, Seq.empty)
        genIs(buf, elseResultV.name, ty, obj)
        buf += Inst.Jump(Next.Label(contL, Seq(elseResultV)))
        // cont
        buf += Inst.Label(contL, Seq(isIsV))
        buf += Let(n, Op.Copy(isIsV))

      case inst =>
        buf += inst
    }

    buf
  }

  private def genIs(buf: mutable.UnrolledBuffer[Inst],
                    n: Local,
                    ty: Type,
                    obj: Val): Unit =
    ty match {
      case ClassRef(cls) if cls.range.length == 1 =>
        val typeptr = Val.Local(fresh(), Type.Ptr)

        buf += Let(typeptr.name, Op.Load(Type.Ptr, obj))
        buf += Let(n, Op.Comp(Comp.Ieq, Type.Ptr, typeptr, cls.typeConst))

      case ClassRef(cls) =>
        val typeptr = Val.Local(fresh(), Type.Ptr)
        val idptr   = Val.Local(fresh(), Type.Ptr)
        val id      = Val.Local(fresh(), Type.I32)
        val ge      = Val.Local(fresh(), Type.Bool)
        val le      = Val.Local(fresh(), Type.Bool)

        buf += Let(typeptr.name, Op.Load(Type.Ptr, obj))
        buf += Let(idptr.name,
                   Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0))))
        buf += Let(id.name, Op.Load(Type.I32, idptr))
        buf += Let(ge.name,
                   Op.Comp(Comp.Sle, Type.I32, Val.I32(cls.range.start), id))
        buf += Let(le.name,
                   Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end)))
        buf += Let(n, Op.Bin(Bin.And, Type.Bool, ge, le))

      case TraitRef(trt) =>
        val typeptr = Val.Local(fresh(), Type.Ptr)
        val idptr   = Val.Local(fresh(), Type.Ptr)
        val id      = Val.Local(fresh(), Type.I32)
        val boolptr = Val.Local(fresh(), Type.Ptr)

        buf += Let(typeptr.name, Op.Load(Type.Ptr, obj))
        buf += Let(idptr.name,
                   Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0))))
        buf += Let(id.name, Op.Load(Type.I32, idptr))
        buf += Let(boolptr.name,
                   Op.Elem(top.instanceTy,
                           top.instanceVal,
                           Seq(Val.I32(0), id, Val.I32(trt.id))))
        buf += Let(n, Op.Load(Type.Bool, boolptr))

      case _ =>
        util.unsupported(s"is[$ty] $obj")
    }
}

object IsLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new IsLowering()(top.fresh, top)
}
