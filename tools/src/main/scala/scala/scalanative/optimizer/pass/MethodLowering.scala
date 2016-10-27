package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._, Inst.Let

/** Translates high-level object-oriented method calls into
 *  low-level dispatch based on vtables for classes
 *  and dispatch tables for interfaces.
 */
class MethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  override def preInst = {
    case Let(n, Op.Method(obj, MethodRef(cls: Class, meth)))
        if meth.isVirtual =>
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
        Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Let(methptrptr.name,
            Op.Elem(cls.typeStruct,
                    typeptr,
                    Seq(Val.I32(0),
                        Val.I32(2), // index of vtable in type struct
                        Val.I32(meth.vindex)))),
        Let(n, Op.Load(Type.Ptr, methptrptr))
      )

    case Let(n, Op.Method(obj, MethodRef(_: Class, meth))) if meth.isStatic =>
      Seq(
        Let(n, Op.Copy(Val.Global(meth.name, Type.Ptr)))
      )

    case Let(n, Op.Method(obj, MethodRef(trt: Trait, meth))) =>
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val idptr      = Val.Local(fresh(), Type.Ptr)
      val id         = Val.Local(fresh(), Type.I32)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
        Let(typeptr.name, Op.Load(Type.Ptr, obj)),
        Let(idptr.name,
            Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
        Let(id.name, Op.Load(Type.I32, idptr)),
        Let(methptrptr.name,
            Op.Elem(top.dispatchTy,
                    top.dispatchVal,
                    Seq(Val.I32(0), id, Val.I32(meth.id)))),
        Let(n, Op.Load(Type.Ptr, methptrptr))
      )
  }
}

object MethodLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new MethodLowering()(top.fresh, top)
}
