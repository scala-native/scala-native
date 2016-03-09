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
 *      class $name: $parent, .. $ifaces {
 *        .. var $fldname: $fldty = $fldinit
 *        .. def $declname: $declty
 *        .. def $defnname: $defnty = $body
 *      }
 *
 *  Gets lowered to:
 *
 *      struct $name_type {
 *        struct #type,
 *        .. ptr $alldeclty
 *      }
 *
 *      struct $name {
 *        ptr $name_type,
 *        .. $allfldty
 *      }
 *
 *      const $name_const: struct $name_type =
 *        struct $name_type {
 *          struct #Type {
 *            #Type_type,
 *            ${cls.name},
 *            ${cls.id},
 *          }
 *          .. $alldefnname
 *        }
 *
 *      .. def $defnname: $defnty = $body
 *
 *  Eliminates:
 *  - Type.{Class, Null}
 *  - Defn.{Class}
 *  - Op.{Alloc, Field, Method, As, Is}
 *  - Val.{Null, Class}
 */
class ClassLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh) extends Pass {
  private val i8_*      = Type.Ptr(Type.I8)
  private val zero_i8_* = Val.Zero(i8_*)

  override def preDefn = {
    case Defn.Class(_, name @ ClassRef(cls), _, _, members) =>
      val data      = cls.fields.map(_.ty)
      val vtable    = cls.vtable
      val vtableTys = vtable.map(_.ty)

      val classTypeStructName = name + "type"
      val classTypeStructTy   = Type.Struct(classTypeStructName)
      val classTypeStructBody = Nrt.Type +: vtableTys
      val classTypeStruct     = Defn.Struct(Seq(), classTypeStructName, classTypeStructBody)

      val classStructTy = Type.Struct(name)
      val classStruct   = Defn.Struct(Seq(), name, Type.Ptr(classTypeStructTy) +: data)

      val typeId   = Val.I32(cls.id)
      val typeName = Val.String(cls.name.parts.head)
      val typeVal  = Val.Struct(Nrt.Type.name, Seq(Nrt.Type_type, typeId, typeName))

      val classConstName = name + "const"
      val classConstVal  = Val.Struct(classTypeStructName, typeVal +: vtable)
      val classConst     = Defn.Const(Seq(), classConstName, classTypeStructTy, classConstVal)

      val methods = members.collect {
        case defn: Defn.Define =>
          defn.copy(attrs = defn.attrs.filterNot(_.isInstanceOf[Attr.Override]))
      }

      Seq(classTypeStruct, classStruct, classConst) ++ methods
  }

  override def preInst =  {
    case Inst(n, Op.Alloc(ClassRef(cls))) =>
      val clstype = Val.Global(cls.name + "const", Type.Ptr(Type.Struct(cls.name + "type")))
      val typeptr = Type.Ptr(Nrt.Type)
      val cast    = Val.Local(fresh(), typeptr)
      val size    = Val.Local(fresh(), Type.Size)
      Seq(
        Inst(cast.name, Op.Conv(Conv.Bitcast, typeptr, clstype)),
        Inst(size.name, Op.SizeOf(Type.Struct(cls.name))),
        Inst(n,         Nrt.call(Nrt.Object_alloc, cast, size))
      )

    case Inst(n, Op.Field(ty, obj, ClassFieldRef(fld))) =>
      val cast = Val.Local(fresh(), Type.Size)
      Seq(
        Inst(cast.name, Op.Conv(Conv.Bitcast, Type.Ptr(Type.Struct(fld.in.name)), obj)),
        Inst(n,         Op.Elem(ty, cast, Seq(Val.I32(0), Val.I32(fld.index + 1))))
      )

    case Inst(n, Op.Method(sig, obj, VirtualClassMethodRef(meth))) =>
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

    case Inst(n, Op.Method(sig, obj, StaticClassMethodRef(meth))) =>
      Seq(
        Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr(sig))))
      )

    case Inst(n, Op.As(_, v)) =>
      Seq(
        Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(ClassRef(cls), v)) =>
      val ty   = Val.Local(fresh(), Type.Ptr(Nrt.Type))
      val id   = Val.Local(fresh(), Type.I32)
      val cond =
        if (cls.range.length == 1)
          Seq(
            Inst(n, Op.Comp(Comp.Ieq, Type.I32, id, Val.I32(cls.id)))
          )
        else {
          val ge = Val.Local(fresh(), Type.Bool)
          val le = Val.Local(fresh(), Type.Bool)

          Seq(
            Inst(ge.name, Op.Comp(Comp.Sge, Type.I32, Val.I32(cls.range.start), id)),
            Inst(le.name, Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end))),
            Inst(n,       Op.Bin(Bin.And, Type.Bool, ge, le))
          )
        }

      Seq(
        Inst(ty.name, Nrt.call(Nrt.Object_getType, v)),
        Inst(id.name, Nrt.call(Nrt.Type_getId, ty))
      ) ++ cond

    case Inst(n, Op.TypeOf(ty)) if Nrt.types.contains(ty) =>
      Seq(
        Inst(n, Op.Copy(Nrt.types(ty)))
      )

    case Inst(n, Op.TypeOf(ClassRef(cls))) =>
      val clstype  = Type.Struct(cls.name + "type")
      val clsconst = Val.Global(cls.name + "const", Type.Ptr(clstype))
      Seq(
        Inst(n, Op.Conv(Conv.Bitcast, Type.Ptr(Nrt.Type), clsconst))
      )
  }

  override def preType = {
    case _: Type.RefKind       => i8_*
    case Type.ClassValue(name) => Type.Struct(name)
  }

  override def preVal = {
    case Val.ClassValue(clsname, values) =>
      val clstype = Val.Global(clsname + "const", Type.Ptr(Type.Struct(clsname + "type")))
      Val.Struct(clsname, clstype +: values)
  }

  object ClassRef {
    def unapply(ty: Type): Option[ClassHierarchy.Class] = ty match {
      case Type.Class(name) => unapply(name)
      case _                => None
    }
    def unapply(name: Global): Option[ClassHierarchy.Class] =
      chg.nodes.get(name).collect {
        case cls: ClassHierarchy.Class => cls
      }
  }

  object VirtualClassMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] =
      chg.nodes.get(name).collect {
        case meth: ClassHierarchy.Method
          if meth.isVirtual
          && meth.in.isInstanceOf[ClassHierarchy.Method] => meth
      }
  }

  object StaticClassMethodRef {
    def unapply(name: Global): Option[ClassHierarchy.Method] =
      chg.nodes.get(name).collect {
        case meth: ClassHierarchy.Method
          if meth.isStatic
          && meth.in.isInstanceOf[ClassHierarchy.Class] => meth
      }
  }

  object ClassFieldRef {
    def unapply(name: Global): Option[ClassHierarchy.Field] =
      chg.nodes.get(name).collect {
        case fld: ClassHierarchy.Field => fld
      }
  }
}
