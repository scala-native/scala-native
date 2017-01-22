package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import util.unsupported
import nir._

/** Lowers class definitions, and field accesses to structs
 *  and corresponding derived pointer computation.
 */
class ClassLowering(implicit top: Top, fresh: Fresh) extends Pass {
  import ClassLowering._

  override def preDefn = {
    case Defn.Class(_, name @ ClassRef(cls), _, _) =>
      val classStructTy = cls.classStruct
      val classStructDefn =
        Defn.Struct(Attrs.None, classStructTy.name, classStructTy.tys)

      Seq(classStructDefn)

    case Defn.Declare(_, MethodRef(_: Class, _), _) =>
      Seq()

    case Defn.Var(_, FieldRef(_: Class, _), _, _) =>
      Seq()
  }

  override def preInst = {
    case Inst.Let(n, Op.Field(obj, FieldRef(cls: Class, fld))) =>
      val classty = cls.classStruct

      Seq(
        Inst.Let(
          n,
          Op.Elem(classty, obj, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )
  }

  override def preType = {
    case ty: Type.RefKind if ty != Type.Unit => Type.Ptr
  }
}

object ClassLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ClassLowering()(top, top.fresh)
}
