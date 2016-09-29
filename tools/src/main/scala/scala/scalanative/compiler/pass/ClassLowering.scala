package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import util.{sh, unsupported}
import nir._, Shows._

/** Lowers classes, methods and fields down to
 *  structs with accompanying vtables.
 *
 *  For example a class w:
 *
 *      class $name: $parent, .. $ifaces
 *      .. var $name::$fldname: $fldty = $fldinit
 *      .. def $name::$declname: $declty
 *      .. def $name::$defnname: $defnty = $body
 *
 *  Gets lowered to:
 *
 *      struct class.$name {
 *        ptr        // pointer to rtti
 *        .. $fldty,
 *      }
 *
 *      .. def $name::$defnname: $defnty = $body
 *
 *  Eliminates:
 *  - Type.Class
 *  - Defn.Class
 *  - Op.{Alloc, Field, Method}
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
        Inst(n, Op.Elem(classty, obj, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )

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

    case Inst(n, Op.Is(ClassRef(cls), obj)) =>
      val typeptr = Val.Local(fresh(), Type.Ptr)

      val cond = if (cls.range.length == 1) {
        Seq(Inst(n, Op.Comp(Comp.Ieq, Type.Ptr, typeptr, cls.typeConst)))
      } else {
        val idptr = Val.Local(fresh(), Type.Ptr)
        val id    = Val.Local(fresh(), Type.I32)
        val ge    = Val.Local(fresh(), Type.Bool)
        val le    = Val.Local(fresh(), Type.Bool)

        Seq(
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr)),
          Inst(ge.name,
               Op.Comp(Comp.Sle, Type.I32, Val.I32(cls.range.start), id)),
          Inst(le.name,
               Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end))),
          Inst(n, Op.Bin(Bin.And, Type.Bool, ge, le))
        )
      }

      Inst(typeptr.name, Op.Load(Type.Ptr, obj)) +: cond
  }

  override def preType = {
    case ty: Type.RefKind if ty != Type.Unit => Type.Ptr
  }
}

object ClassLowering extends PassCompanion {
  def apply(ctx: Ctx) = new ClassLowering()(ctx.top, ctx.fresh)

  val allocName = Global.Top("scalanative_alloc")
  val allocSig  = Type.Function(Seq(Type.Ptr, Type.I64), Type.Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  override val injects = Seq(Defn.Declare(Attrs.None, allocName, allocSig))
}
