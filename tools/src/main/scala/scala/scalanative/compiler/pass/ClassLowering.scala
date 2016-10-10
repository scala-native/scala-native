package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import util.{sh, unsupported}
import nir._, Shows._

/** Lowers class definitions, and field accesses to structs
 *  and corresponding derived pointer computation.
 */
class ClassLowering(implicit top: Top, fresh: Fresh) extends Pass {
  import ClassLowering._

  def classStruct(cls: Class): Type.Struct = {
    val data            = cls.allfields.map(_.ty)
    val classStructName = cls.name tag "class"
    val classStructBody = Type.Ptr +: data
    val classStructTy   = Type.Struct(classStructName, classStructBody)

    Type.Struct(classStructName, classStructBody)
  }

  override def preDefn = {
    case Defn.Class(_, name @ ClassRef(cls), _, _) =>
      val classStructTy = classStruct(cls)
      val classStructDefn =
        Defn.Struct(Attrs.None, classStructTy.name, classStructTy.tys)

      Seq(classStructDefn)

    case Defn.Declare(_, MethodRef(_: Class, _), _) =>
      Seq()

    case Defn.Var(_, FieldRef(_: Class, _), _, _) =>
      Seq()
  }

  override def preInst = {
    case Inst(n, Op.Classalloc(ClassRef(cls))) =>
      val size = Val.Local(fresh(), Type.I64)

      Seq(
          Inst(size.name, Op.Sizeof(classStruct(cls))),
          Inst(n, Op.Call(allocSig, alloc, Seq(cls.typeConst, size)))
      )

    case Inst(n, Op.Field(ty, obj, FieldRef(cls: Class, fld))) =>
      val classty = classStruct(cls)

      Seq(
          Inst(n,
               Op.Elem(classty, obj, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )
  }

  override def preType = {
    case ty: Type.RefKind if ty != Type.Unit => Type.Ptr
  }
}

object ClassLowering extends PassCompanion {
  def apply(ctx: Ctx) = new ClassLowering()(ctx.top, ctx.fresh)

  val allocName = Global.Top("scalanative_alloc")
  val allocSig  = Type.Function(Seq(Arg(Type.Ptr), Arg(Type.I64)), Type.Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  override val injects = Seq(Defn.Declare(Attrs.None, allocName, allocSig))
}
