package native
package compiler
package pass

import native.nir._, Shows._
import native.util.{sh, unsupported}
import native.compiler.analysis.ClassHierarchy

/** Lowers classes, methods and fields down to
 *  structs with accompanying vtables.
 *
 *  For example a class w:
 *
 *      class $name() {
 *        .. var $fname: $fty = $fvalue
 *        .. def $mname: $mty
 *        .. def $mname: $mty = $body
 *      }
 *
 *  Gets lowered to:
 *
 *      struct $name_type { #type, .. ptr $allvirtmty }
 *      struct $name { ptr $name_type, .. $allfty }
 *
 *      const $name_const: struct $name_type =
 *        struct[$name_type](#type_type, ..$mname)
 *
 *      .. def $mname: $mty = $body
 *
 *  Eliminates:
 *  - Type.{Class, InterfaceClass, Null}
 *  - Defn.{Class, InterfaceClass}
 *  - Op.{Alloc, Field, Method, As, Is}
 *  - Val.{Null, Class}
 */
class ClassLowering(implicit cha: ClassHierarchy.Result, fresh: Fresh) extends Pass {
  private val i8_*      = Type.Ptr(Type.I8)
  private val zero_i8_* = Val.Zero(i8_*)

  override def preDefn = {
    case Defn.Class(_, name, _, _, members) =>
      val cls       = cha(name).asInstanceOf[ClassHierarchy.Node.Class]
      val data      = cls.fields.map(_.ty)
      val vtable    = cls.vtable
      val vtableTys = vtable.map(_.ty)

      val classTypeStructName = name + "type"
      val classTypeStructTy   = Type.Struct(classTypeStructName)
      val classTypeStruct     = Defn.Struct(Seq(), classTypeStructName,
                                            Type.Ptr(Intr.type_) +: vtableTys)

      val classStructTy = Type.Struct(name)
      val classStruct   = Defn.Struct(Seq(), name, Type.Ptr(classTypeStructTy) +: data)

      val classConstName = name + "const"
      val classConstVal  = Val.Struct(classTypeStructName, Intr.type_type +: vtable)
      val classConst     = Defn.Const(Seq(), classConstName, classTypeStructTy, classConstVal)

      val methods = members.collect { case defn: Defn.Define => defn }

      Seq(classTypeStruct, classStruct, classConst) ++ methods

    case _: Defn.Interface =>
      ???
  }

  override def preInst =  {
    case Inst(n, Op.Alloc(Type.Class(clsname))) =>
      val clstype = Val.Global(clsname + "const", Type.Ptr(Type.Struct(clsname + "type")))
      val typeptr = Type.Ptr(Intr.type_)
      val cast    = Val.Local(fresh(), typeptr)
      val size    = Val.Local(fresh(), Type.Size)
      Seq(
        Inst(cast.name, Op.Conv(Conv.Bitcast, typeptr, clstype)),
        Inst(size.name, Op.SizeOf(Type.Struct(clsname))),
        Inst(n,         Intr.call(Intr.alloc, cast, size))
      )

    case Inst(n, Op.Field(ty, obj, ExField(fld))) =>
      val cast = Val.Local(fresh(), Type.Size)
      Seq(
        Inst(cast.name, Op.Conv(Conv.Bitcast, Type.Ptr(Type.Struct(fld.in.name)), obj)),
        Inst(n,         Op.Elem(ty, cast, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )

    case Inst(n, Op.Method(sig, obj, ExVirtualMethod(meth))) =>
      val sigptr     = Type.Ptr(sig)
      val clsptr     = Type.Ptr(Type.Struct(meth.in.name))
      val typeptrty  = Type.Ptr(Type.Struct(meth.in.name + "type"))
      val cast       = Val.Local(fresh(), clsptr)
      val typeptrptr = Val.Local(fresh(), Type.Ptr(typeptrty))
      val typeptr    = Val.Local(fresh(), typeptrty)
      val methptrptr = Val.Local(fresh(), Type.Ptr(sigptr))
      Seq(
        Inst(cast.name,       Op.Conv(Conv.Bitcast, clsptr, obj)),
        Inst(typeptrptr.name, Op.Elem(typeptr.ty, cast, Seq(Val.I32(0), Val.I32(0)))),
        Inst(typeptr.name,    Op.Load(typeptr.ty, typeptrptr)),
        Inst(methptrptr.name, Op.Elem(sigptr, typeptr, Seq(Val.I32(0), Val.I32(meth.vindex)))),
        Inst(n,               Op.Load(sigptr, methptrptr))
      )

    case Inst(n, Op.Method(sig, obj, ExStaticMethod(meth))) =>
      Seq(
        Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr(sig))))
      )

    case Inst(n, Op.As(_, v)) =>
      Seq(
        Inst(n, Op.Copy(v))
      )

    case Inst(n, _: Op.Is) =>
      Seq(
        Inst(n, Op.Copy(Val.True))
      )

    case Inst(n, Op.TypeOf(ty)) if Intr.intrinsic_type.contains(ty) =>
      Seq(
        Inst(n, Op.Copy(Intr.intrinsic_type(ty)))
      )

    case Inst(n, Op.TypeOf(Type.Class(name))) =>
      val clstype  = Type.Struct(name + "type")
      val clsconst = Val.Global(name + "const", Type.Ptr(clstype))
      Seq(
        Inst(n, Op.Conv(Conv.Bitcast, Type.Ptr(Intr.type_), clsconst))
      )
  }

  override def preType = {
    case _: Type.ClassKind => i8_*
  }

  object ExVirtualMethod {
    def unapply(name: Global): Option[ClassHierarchy.Node.Method] =
      cha.get(name).collect {
        case meth: ClassHierarchy.Node.Method if meth.isVirtual => meth
      }
  }

  object ExStaticMethod {
    def unapply(name: Global): Option[ClassHierarchy.Node.Method] =
      cha.get(name).collect {
        case meth: ClassHierarchy.Node.Method if meth.isStatic => meth
      }
  }

  object ExField {
    def unapply(name: Global): Option[ClassHierarchy.Node.Field] =
      cha.get(name).collect {
        case fld: ClassHierarchy.Node.Field => fld
      }
  }
}
