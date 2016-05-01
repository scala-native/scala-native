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
  *       ptr #ssnr.Type,
  *       .. ptr $declty,
  *     }
  *
  *     const info.$name: struct info.$name =
  *       struct info.$name {
  *         type.class.$name,
  *         .. $name::$defnname,
  *       }
  *
  *     struct class.$name {
  *       ptr vtable.$name,
  *       .. $fldty,
  *     }
  *
  *     .. def $name::$defnname: $defnty = $body
  *
  * Eliminates:
  * - Type.Class
  * - Defn.Class
  * - Op.{Alloc, Field, Method}
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
      val infoStructTy = infoStruct(cls)
      val infoStructDefn =
        Defn.Struct(Seq(), infoStructTy.name, infoStructTy.tys)

      val classStructTy = classStruct(cls)
      val classStructDefn =
        Defn.Struct(Seq(), classStructTy.name, classStructTy.tys)

      val typeId   = Val.I32(cls.id)
      val typeName = Val.String(cls.name.id)
      val typeVal  = Val.Struct(Rt.Type.name, Seq(typeId, typeName))

      val classConstName = name tag "const"
      val classConstVal  = Val.Struct(infoStructTy.name, typeVal +: cls.vtable)
      val classConstDefn =
        Defn.Const(Seq(), classConstName, infoStructTy, classConstVal)

      Seq(infoStructDefn, classStructDefn, classConstDefn)

    case Defn.Declare(_, MethodRef(ClassRef(_), _), _) =>
      Seq()

    case Defn.Var(_, FieldRef(ClassRef(_), _), _, _) =>
      Seq()
  }

  override def preInst = {
    case Inst(n, Op.Alloc(ClassRef(cls))) =>
      val classTy = classStruct(cls)
      val size    = Val.Local(fresh(), Type.Size)
      val const   = Val.Global(cls.name tag "const", Type.Ptr)

      Seq(
          Inst(size.name, Op.Sizeof(classTy)),
          Inst(n, Op.Call(Rt.allocSig, Rt.alloc, Seq(const, size)))
      )

    case Inst(n, Op.Field(ty, obj, FieldRef(ClassRef(cls), fld))) =>
      val classTy = classStruct(cls)

      Seq(
          Inst(n,
               Op.Elem(classTy, obj, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )

    case Inst(n, Op.Method(sig, obj, MethodRef(ClassRef(cls), meth)))
        if meth.isVirtual =>
      val classTy    = classStruct(cls)
      val typeptrptr = Val.Local(fresh(), Type.Ptr)
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(typeptrptr.name,
               Op.Elem(classTy, obj, Seq(Val.I32(0), Val.I32(0)))),
          Inst(typeptr.name, Op.Load(typeptr.ty, typeptrptr)),
          Inst(methptrptr.name,
               Op.Elem(
                   Type.Ptr, typeptr, Seq(Val.I32(0), Val.I32(meth.vindex)))),
          Inst(n, Op.Load(Type.Ptr, methptrptr))
      )

    case Inst(n, Op.Method(sig, obj, MethodRef(ClassRef(_), meth)))
        if meth.isStatic =>
      Seq(
          Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr)))
      )
  }

  override def preType = {
    case _: Type.RefKind => Type.Ptr
  }
}
