package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
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
 *      struct info.$name {
 *        ptr,       // to struct #ssnr.Type
 *        .. ptr     // to $declty
 *      }
 *
 *      const info.$name: struct info.$name =
 *        struct info.$name {
 *          type.class.$name,
 *          .. $name::$defnname,
 *        }
 *
 *      struct class.$name {
 *        ptr        // to struct info.$name
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
class ClassLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  def infoStruct(cls: ClassHierarchy.Class): Type.Struct = {
    val vtable         = cls.vtable.map(_.ty)
    val infoStructName = cls.name tag "info"
    val infoStructBody = Type.Ptr +: vtable

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
        Defn.Struct(Attrs.None, infoStructTy.name, infoStructTy.tys)

      val classStructTy = classStruct(cls)
      val classStructDefn =
        Defn.Struct(Attrs.None, classStructTy.name, classStructTy.tys)

      val typeName  = name tag "class" tag "type"
      val typeConst = Val.Global(typeName, Type.Ptr)

      val classConstName = name tag "const"
      val classConstVal =
        Val.Struct(infoStructTy.name, typeConst +: cls.vtable)
      val classConstDefn =
        Defn.Const(Attrs.None, classConstName, infoStructTy, classConstVal)

      Seq(infoStructDefn, classStructDefn, classConstDefn)

    case Defn.Declare(_, MethodRef(ClassRef(_), _), _) =>
      Seq()

    case Defn.Var(_, FieldRef(ClassRef(_), _), _, _) =>
      Seq()
  }

  override def preInst = {
    case Inst(n, Op.Classalloc(ClassRef(cls))) =>
      val classty = classStruct(cls)
      val size    = Val.Local(fresh(), Type.I64)
      val const   = Val.Global(cls.name tag "const", Type.Ptr)

      Seq(
          Inst(size.name, Op.Sizeof(classty)),
          Inst(n, Op.Call(Rt.allocSig, Rt.alloc, Seq(const, size)))
      )

    case Inst(n, Op.Field(ty, obj, FieldRef(ClassRef(cls), fld))) =>
      val classty = classStruct(cls)

      Seq(
          Inst(n,
               Op.Elem(classty, obj, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )

    case Inst(n, Op.Method(sig, obj, MethodRef(ClassRef(cls), meth)))
        if meth.isVirtual =>
      val infoty     = infoStruct(cls)
      val infoptr    = Val.Local(fresh(), Type.Ptr)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      Seq(
          Inst(infoptr.name, Op.Load(Type.Ptr, obj)),
          Inst(
              methptrptr.name,
              Op.Elem(infoty, infoptr, Seq(Val.I32(0), Val.I32(meth.vindex)))),
          Inst(n, Op.Load(Type.Ptr, methptrptr))
      )

    case Inst(n, Op.Method(sig, obj, MethodRef(ClassRef(_), meth)))
        if meth.isStatic =>
      Seq(
          Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr)))
      )

    case Inst(n, Op.Infoof(name)) =>
      Seq(
          Inst(n, Op.Copy(Val.Global(name tag "const", Type.Ptr)))
      )

    case Inst(n, Op.Is(ClassRef(cls), obj)) =>
      val infoptr = Val.Local(fresh(), Type.Ptr)
      val typeptr = Val.Local(fresh(), Type.Ptr)
      val idptr   = Val.Local(fresh(), Type.Ptr)
      val id      = Val.Local(fresh(), Type.I32)

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

      Seq(
          Inst(infoptr.name, Op.Load(Type.Ptr, obj)),
          Inst(typeptr.name, Op.Load(Type.Ptr, infoptr)),
          Inst(idptr.name,
               Op.Elem(Rt.Type, typeptr, Seq(Val.I32(0), Val.I32(0)))),
          Inst(id.name, Op.Load(Type.I32, idptr))
      ) ++ cond
  }

  override def preType = {
    case ty: Type.RefKind if ty != Type.Unit => Type.Ptr
  }
}
