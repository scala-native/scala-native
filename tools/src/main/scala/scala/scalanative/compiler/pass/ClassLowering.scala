package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import util.{sh, unsupported}
import nir._, Shows._

/** Lowers classes, methods and fields down to
  * structs with accompanying vtables.
  *
  * For example a class w:
  *
  *     class $name: $parent, .. $ifaces
  *     .. var $name::$fldname: $fldty = $fldinit
  *     .. def $name::$declname: $declty
  *     .. def $name::$defnname: $defnty = $body
  *
  * Gets lowered to:
  *
  *     struct info.$name {
  *       struct #ssnr.Type,
  *       .. ptr $declty,
  *     }
  *
  *     struct class.$name {
  *       ptr info.$name_type,
  *       .. $fldty,
  *     }
  *
  *     const const.$name: struct type.$name =
  *       struct type.$name {
  *         struct #ssnr.Type {
  *           ${cls.id},
  *           ${cls.name},
  *         }
  *         .. $name::$defnname,
  *       }
  *
  *     .. def $name::$defnname: $defnty = $body
  *
  * Eliminates:
  * - Type.{Class, Null}
  * - Defn.{Class}
  * - Op.{Alloc, Field, Method, As, Is}
  * - Val.{Null, Class}
  */
class ClassLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  def infoStruct(cls: ClassHierarchy.Class): Type.Struct = {
    val vtable         = cls.vtable.map(_.ty)
    val infoStructName = cls.name tag "info"
    val infoStructBody = Rt.Type +: vtable

    Type.Struct(infoStructName, infoStructBody)
  }

  def classStruct(cls: ClassHierarchy.Class): Type.Struct = {
    val data            = cls.fields.map(_.ty)
    val classStructName = cls.name tag "class"
    val classStructBody = Type.Ptr +: data
    val classStructTy   = Type.Struct(classStructName, classStructBody)

    Type.Struct(classStructName, classStructBody)
  }

  override def preDefn = {
    case Defn.Class(_, name @ ClassRef(cls), _, _) =>
      val infoStructTy   = infoStruct(cls)
      val infoStructDefn = Defn.Struct(Seq(), infoStructTy.name, infoStructTy.tys)

      val classStructTy   = classStruct(cls)
      val classStructDefn = Defn.Struct(Seq(), classStructTy.name, classStructTy.tys)

      val typeId   = Val.I32(cls.id)
      val typeName = Val.String(cls.name.id)
      val typeVal  = Val.Struct(Rt.Type.name, Seq(typeId, typeName))

      val classConstName = name tag "const"
      val classConstVal  = Val.Struct(infoStructTy.name, typeVal +: cls.vtable)
      val classConstDefn =
        Defn.Const(Seq(), classConstName, infoStructTy, classConstVal)

      Seq(infoStructDefn, classStructDefn, classConstDefn)

    case Defn.Declare(_, VirtualClassMethodRef(_) | StaticClassMethodRef(_), _) =>
      Seq()

    case Defn.Var(_, ClassFieldRef(_), _, _) =>
      Seq()
  }

  override def preInst = {
    case Inst(n, Op.Alloc(ClassRef(cls))) =>
      val clstype = Val.Global(cls.name tag "const", Type.Ptr)
      val cast    = Val.Local(fresh(), Type.Ptr)
      val size    = Val.Local(fresh(), Type.Size)
      Seq(
          Inst(cast.name, Op.Conv(Conv.Bitcast, Type.Ptr, clstype)),
          Inst(size.name, Op.SizeOf(classStruct(cls))),
          Inst(n, Op.Call(Rt.allocSig, Rt.alloc, Seq(cast, size)))
      )

    case Inst(n, Op.Field(ty, obj, ClassFieldRef(fld))) =>
      val cast = Val.Local(fresh(), Type.Size)
      Seq(
          Inst(cast.name, Op.Conv(Conv.Bitcast, Type.Ptr, obj)),
          Inst(n, Op.Elem(ty, cast, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )

    case Inst(n, Op.Method(sig, obj, VirtualClassMethodRef(meth))) =>
      val cast       = Val.Local(fresh(), Type.Ptr)
      val typeptrptr = Val.Local(fresh(), Type.Ptr)
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val methptrptr = Val.Local(fresh(), Type.Ptr)
      Seq(
          Inst(cast.name, Op.Conv(Conv.Bitcast, Type.Ptr, obj)),
          Inst(typeptrptr.name,
               Op.Elem(Type.Ptr, cast, Seq(Val.I32(0), Val.I32(0)))),
          Inst(typeptr.name, Op.Load(typeptr.ty, typeptrptr)),
          Inst(methptrptr.name,
               Op.Elem(
                   Type.Ptr, typeptr, Seq(Val.I32(0), Val.I32(meth.vindex)))),
          Inst(n, Op.Load(Type.Ptr, methptrptr))
      )

    case Inst(n, Op.Method(sig, obj, StaticClassMethodRef(meth))) =>
      Seq(
          Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr)))
      )

    case Inst(n, Op.As(_, v)) =>
      Seq(
          Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(ClassRef(cls), v)) =>
      val ty = Val.Local(fresh(), Type.Ptr)
      val id = Val.Local(fresh(), Type.I32)
      val cond =
        if (cls.range.length == 1)
          Seq(
              Inst(n, Op.Comp(Comp.Ieq, Type.I32, id, Val.I32(cls.id)))
          )
        else {
          val ge = Val.Local(fresh(), Type.Bool)
          val le = Val.Local(fresh(), Type.Bool)

          Seq(
              Inst(ge.name,
                   Op.Comp(Comp.Sge, Type.I32, Val.I32(cls.range.start), id)),
              Inst(le.name,
                   Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end))),
              Inst(n, Op.Bin(Bin.And, Type.Bool, ge, le))
          )
        }

      // Seq(
      //   Inst(ty.name, Rt.call(Rt.Object_getType, v)),
      //   Inst(id.name, Rt.call(Rt.Type_getId, ty))
      // ) ++ cond
      ???

    case Inst(n, Op.TypeOf(ClassRef(cls))) =>
      val const = Val.Global(cls.name tag "const", Type.Ptr)

      Seq(
          Inst(n, Op.Conv(Conv.Bitcast, Type.Ptr, const))
      )
  }

  override def preType = {
    case _: Type.RefKind => Type.Ptr
  }
}
