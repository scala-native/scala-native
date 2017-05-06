package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._, Inst.Let

/** Translates high-level object-oriented method calls into
 *  low-level dispatch based on vtables for classes
 *  and dispatch tables for interfaces.
 */
class MethodLowering(implicit fresh: Fresh, top: Top) extends Pass {
  override def onInsts(insts: Seq[Inst]) = {
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case Let(n, Op.Method(obj, MethodRef(cls: Class, meth)))
          if meth.isVirtual =>
        val vindex  = cls.vtable.index(meth)
        val typeptr = let(Op.Load(Type.Ptr, obj))
        val methptrptr = let(
          Op.Elem(cls.rtti.struct,
                  typeptr,
                  Seq(Val.Int(0),
                      Val.Int(5), // index of vtable in type struct
                      Val.Int(vindex))))

        let(n, Op.Load(Type.Ptr, methptrptr))

      case Let(n, Op.Method(obj, MethodRef(_: Class, meth)))
          if meth.isStatic =>
        let(n, Op.Copy(Val.Global(meth.name, Type.Ptr)))

      case Let(n, Op.Method(obj, MethodRef(trt: Trait, meth))) =>
        val typeptr = let(Op.Load(Type.Ptr, obj))
        val idptr   = let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))))
        val id      = let(Op.Load(Type.Int, idptr))
        val rowptr = let(
          Op.Elem(Type.Ptr,
                  top.tables.dispatchVal,
                  Seq(Val.Int(top.tables.dispatchOffset(meth.id)))))
        val methptrptr =
          let(Op.Elem(Type.Ptr, rowptr, Seq(id)))
        let(n, Op.Load(Type.Ptr, methptrptr))

      case inst =>
        buf += inst
    }

    buf.toSeq
  }
}

object MethodLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new MethodLowering()(top.fresh, top)
}
