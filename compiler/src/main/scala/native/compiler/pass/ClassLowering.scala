package native
package compiler
package pass

import native.nir._, Shows._
import native.util.sh
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
 *      var $name_const: struct $name_type =
 *        struct[$name_type](
 *          struct[#type](#type_of_type, ${cls.name}, ${cls.size})
 *          ..$mname)
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
      val methods   = members.collect { case defn: Defn.Define => defn }
      val cls       = cha(name).asInstanceOf[ClassHierarchy.Node.Class]
      val data      = cls.fields.map(_.ty)
      val vtable    = cls.vtable
      val vtableTys = vtable.map(_.ty)

      val classTypeStructName = name + "type"
      val classTypeStructTy   = Type.Struct(classTypeStructName)
      val classTypeStruct     = Defn.Struct(Seq(), classTypeStructName, Intr.type_ +: vtableTys)

      val classStructTy   = Type.Struct(name)
      val classStruct     = Defn.Struct(Seq(), name, Type.Ptr(classTypeStructTy) +: data)

      val className = Val.String(name.parts.head)
      val classSize = Val.Size(classStructTy)
      val classInfo = Val.Struct(Intr.type_.name, Seq(Intr.type_of_type, className, classSize))

      val classConstName = name + "const"
      val classConstVal  = Val.Struct(classTypeStructName, classInfo +: vtable)
      val classConst     = Defn.Var(Seq(), classConstName, classTypeStructTy, classConstVal)

      Seq(classTypeStruct, classStruct, classConst) ++ methods

    case _: Defn.Interface =>
      ???
  }

  override def preInst =  {
    case Inst(Some(n), Op.Alloc(Type.Class(clsname))) =>
      val typeVal = Val.Global(clsname + "const", Type.Ptr(Type.Struct(clsname + "type")))
      val castVal = Val.Cast(Type.Ptr(Intr.type_), typeVal)
      val sizeVal = Val.Size(Type.Struct(clsname))
      Seq(Inst(n, Intr.call(Intr.alloc, castVal, sizeVal)))

    case Inst(Some(n), Op.Field(ty, obj, ExField(fld))) =>
      val castVal = Val.Cast(Type.Ptr(Type.Struct(fld.in.name)), obj)
      Seq(Inst(n, Op.Elem(ty, castVal, Seq(Val.I32(0), Val.I32(fld.index + 1)))))

    case Inst(Some(n), Op.Method(sig, obj, ExVirtualMethod(meth))) =>
      val sigptr    = Type.Ptr(sig)
      val clsptr    = Type.Ptr(Type.Struct(meth.in.name))
      val vtableptr = Type.Ptr(Type.Struct(meth.in.name + "vtable"))
      val vtable_** = fresh()
      val vtable_*  = fresh()
      val meth_**   = fresh()
      Seq(
        Inst(vtable_**, Op.Elem(vtableptr, Val.Cast(clsptr, obj),
                                           Seq(Val.I32(0), Val.I32(0)))),
        Inst(vtable_*,  Op.Load(vtableptr, Val.Local(vtable_**, Type.Ptr(vtableptr)))),
        Inst(meth_**,   Op.Elem(sigptr, Val.Local(vtable_*, vtableptr),
                                        Seq(Val.I32(0), Val.I32(meth.vindex)))),
        Inst(n,         Op.Load(sigptr, Val.Local(meth_**, Type.Ptr(sigptr))))
      )

    case Inst(Some(n), Op.Method(sig, obj, ExStaticMethod(meth))) =>
      Seq(Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr(sig)))))

    case Inst(n, _: Op.As) =>
      ???

    case Inst(n, _: Op.Is) =>
      ???
  }

  override def preType = {
    case _: Type.ClassKind => i8_*
  }

  override def preVal = {
    case Val.Null =>
      zero_i8_*

    case Val.Type(ty) =>
      ty match {
        case _ if Intr.type_of_intrinsic.contains(ty) =>
          Intr.type_of_intrinsic(ty)
        case Type.Class(name) =>
          Val.Global(name + "type", Type.Ptr(Intr.type_))
        case _ =>
          ???
      }
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
