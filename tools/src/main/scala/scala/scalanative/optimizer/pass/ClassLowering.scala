package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import util.unsupported
import nir._

/** Lowers class definitions, and field accesses to structs
 *  and corresponding derived pointer computation.
 */
class ClassLowering(implicit top: Top, fresh: Fresh) extends Pass {
  import ClassLowering._

  override def onDefns(defns: Seq[Defn]): Seq[Defn] = {
    val buf = mutable.UnrolledBuffer.empty[Defn]

    defns.foreach {
      case Defn.Class(_, name @ ClassRef(cls), _, _) =>
        val classStructTy = cls.classStruct
        val classStructDefn =
          Defn.Struct(Attrs.None, classStructTy.name, classStructTy.tys)

        buf += super.onDefn(classStructDefn)

      case Defn.Declare(_, MethodRef(_: Class, _), _) =>
        ()

      case Defn.Var(_, FieldRef(_: Class, _), _, _) =>
        ()

      case defn =>
        buf += super.onDefn(defn)
    }

    buf
  }

  override def onInst(inst: Inst) = super.onInst {
    inst match {
      case Inst.Let(n, Op.Field(obj, FieldRef(cls: Class, fld))) =>
        val classty = cls.classStruct

        Inst.Let(
          n,
          Op.Elem(classty, obj, Seq(Val.I32(0), Val.I32(fld.index + 1))))

      case _ =>
        inst
    }
  }

  override def onType(ty: Type): Type = ty match {
    case ty: Type.RefKind if ty != Type.Unit =>
      Type.Ptr
    case _ =>
      super.onType(ty)
  }
}

object ClassLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ClassLowering()(top, top.fresh)
}
