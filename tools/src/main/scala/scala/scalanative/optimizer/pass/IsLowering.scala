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
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case Let(n, Op.Is(_, Val.Zero(_))) =>
        let(n, Op.Copy(Val.False))

      case Let(n, Op.Is(ty, obj)) =>
        val result = Val.Local(fresh(), Type.Bool)

        val thenL, elseL, contL = fresh()

        // check if obj is null
        val isnull = let(Op.Comp(Comp.Ieq, Type.Ptr, obj, Val.Zero(Type.Ptr)))
        branch(isnull, Next(thenL), Next(elseL))
        // in case it's null, result is always false
        label(thenL)
        val res1 = let(Op.Copy(Val.False))
        jump(Next.Label(contL, Seq(res1)))
        // otherwise, do an actual instance check
        label(elseL)
        val res2 = is(buf, ty, obj)
        jump(Next.Label(contL, Seq(res2)))
        // merge the result of two branches
        label(contL, Seq(result))
        let(n, Op.Copy(result))

      case inst =>
        buf += inst
    }

    buf.toSeq
  }

  private def is(buf: nir.Buffer, ty: Type, obj: Val): Val.Local = {
    import buf._

    ty match {
      case ClassRef(cls) if cls.range.length == 1 =>
        val typeptr = let(Op.Load(Type.Ptr, obj))
        let(Op.Comp(Comp.Ieq, Type.Ptr, typeptr, cls.typeConst))

      case ClassRef(cls) =>
        val typeptr = let(Op.Load(Type.Ptr, obj))
        val idptr   = let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))))
        val id      = let(Op.Load(Type.Int, idptr))
        val ge      = let(Op.Comp(Comp.Sle, Type.Int, Val.Int(cls.range.start), id))
        val le      = let(Op.Comp(Comp.Sle, Type.Int, id, Val.Int(cls.range.end)))
        let(Op.Bin(Bin.And, Type.Bool, ge, le))

      case TraitRef(trt) =>
        val typeptr = let(Op.Load(Type.Ptr, obj))
        val idptr   = let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))))
        val id      = let(Op.Load(Type.Int, idptr))
        val boolptr = let(
          Op.Elem(top.instanceTy,
                  top.instanceVal,
                  Seq(Val.Int(0), id, Val.Int(trt.id))))
        let(Op.Load(Type.Bool, boolptr))

      case _ =>
        util.unsupported(s"is[$ty] $obj")
    }
  }
}

object IsLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new IsLowering()(top.fresh, top)
}
