package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Translates high-level object-oriented method calls into
 *  low-level dispatch based on vtables for classes
 *  and dispatch tables for interfaces.
 */
class MethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  override def preInst = {
    case Inst(n, Op.Method(sig, obj, MethodRef(cls: Class, meth)))
        if meth.isVirtual =>
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(methptrptr.name,
               Op.Elem(cls.typeStruct,
                       typeptr,
                       Seq(Val.I32(0),
                           Val.I32(2), // index of vtable in type struct
                           Val.I32(meth.vindex)))),
          Inst(n, Op.Load(Type.Ptr, methptrptr))
      )

    case Inst(n, Op.Method(sig, obj, MethodRef(_: Class, meth)))
        if meth.isStatic =>
      Seq(
          Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr)))
      )

    case Inst(n, Op.Method(sig, obj, MethodRef(trt: Trait, meth))) =>
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val idptr      = Val.Local(fresh(), Type.Ptr)
      val id         = Val.Local(fresh(), Type.I32)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptr.name, Op.Load(Type.Ptr, obj)),
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr)),
          Inst(methptrptr.name,
               Op.Elem(top.dispatchTy,
                       top.dispatchVal,
                       Seq(Val.I32(0), id, Val.I32(meth.id)))),
          Inst(n, Op.Load(Type.Ptr, methptrptr))
      )
  }
}

object MethodLowering extends PassCompanion {
  def apply(ctx: Ctx) = new MethodLowering()(ctx.fresh, ctx.top)
}
