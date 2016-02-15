package native
package compiler
package pass

import native.nir._, Shows._
import native.util.sh
import native.compiler.analysis.ClassHierarchy.Node

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
 *      struct $name_vtable { .. ptr $allvirtmty }
 *      struct $name { $name_vtable*, .. $allfty }
 *
 *      var $name_cls: i8 = 0
 *      var $name_vconst: struct $name_vtable = struct[$name_vtable](..$mname)
 *
 *      .. def $mname: $mty = $body
 *
 *  Eliminates:
 *  - Type.{Class, InterfaceClass, Null}
 *  - Defn.{Class, InterfaceClass}
 *  - Op.{Alloc, Field, Method, As, Is}
 *  - Val.{Null, Class}
 */
trait ClassLowering extends Pass { self: EarlyLowering =>
  private val i8_*      = Type.Ptr(Type.I8)
  private val zero_i8_* = Val.Zero(i8_*)

  override def onDefn(defn: Defn) = defn match {
    case Defn.Class(_, name, _, _, members) =>
      val methods   = members.collect { case defn: Defn.Define => defn }
      val cls       = cha(name).asInstanceOf[Node.Class]
      val data      = cls.fields.map(_.ty)
      val vtable    = cls.vtable
      val vtableTys = vtable.map(_.ty)

      val vtableStructName = name + "vtable"
      val vtableStructTy   = Type.Struct(vtableStructName)

      val vtableConstName = name + "vconst"
      val vtableStruct    = Defn.Struct(Seq(), vtableStructName, vtableTys)
      val vtableConstVal  = Val.Struct(vtableStructName, vtable)
      val vtableConst     = Defn.Var(Seq(), vtableConstName, vtableStructTy, vtableConstVal)

      val classConstName = name + "cls"
      val classConstTy   = Type.I8
      val classConstVal  = Val.Zero(Type.I8)
      val classConst     = Defn.Var(Seq(), classConstName, classConstTy, classConstVal)

      val classStruct   = Defn.Struct(Seq(), name, Type.Ptr(vtableStructTy) +: data)
      val classStructTy = Type.Struct(name)

      (Seq(vtableStruct, classStruct, classConst, vtableConst) ++ methods).flatMap(onDefn)

    case _: Defn.Interface =>
      ???

    case _ =>
      super.onDefn(defn)
  }

  override def onInst(inst: Inst) = inst match {
    case Inst(Some(n), Seq(), Op.Alloc(Type.Class(clsname))) =>
      val clsValue = Val.Global(clsname + "cls", Type.Ptr(Type.I8))
      val sizeValue = Val.Size(Type.Struct(clsname))
      onInst(Inst(n, Intr.call(Intr.alloc, clsValue, sizeValue)))

    case Inst(Some(n), Seq(), Op.Field(ty, obj, ExField(fld))) =>
      val clsptr = Type.Ptr(Type.Struct(fld.in.name))
      val cast   = fresh()
      Seq(
        Inst(cast, Op.Conv(Conv.Bitcast, clsptr, obj)),
        Inst(n,    Op.Elem(ty, Val.Local(cast, clsptr), Seq(Val.I32(0), Val.I32(fld.index + 1))))
      ).flatMap(onInst)

    // Virtual method elems
    //
    //     %$n = method-elem[$sig] $instance, $method
    //
    // Lowered to:
    //
    //     %cast      = bitcast[struct $name*] %instance
    //     %vtable_** = elem[struct $name.vtable*] %cast, 0i32, 0i32
    //     %vtable_*  = load[struct $name.vtable*] vtable_**
    //     %meth_**   = elem[$sig*] %vtable_*, 0i32, ${meth.index + 1}
    //     %$n        = load[$sig*] %meth_**
    //
    case Inst(Some(n), Seq(), Op.Method(sig, obj, ExVirtualMethod(meth))) =>
      val sigptr    = Type.Ptr(sig)
      val clsptr    = Type.Ptr(Type.Struct(meth.in.name))
      val vtableptr = Type.Ptr(Type.Struct(meth.in.name + "vtable"))
      val cast      = fresh()
      val vtable_** = fresh()
      val vtable_*  = fresh()
      val meth_**   = fresh()
      Seq(
        Inst(cast,      Op.Conv(Conv.Bitcast, clsptr, obj)),
        Inst(vtable_**, Op.Elem(vtableptr, Val.Local(cast, clsptr),
                                            Seq(Val.I32(0), Val.I32(0)))),
        Inst(vtable_*,  Op.Load(vtableptr, Val.Local(vtable_**, Type.Ptr(vtableptr)))),
        Inst(meth_**,   Op.Elem(sigptr, Val.Local(vtable_*, vtableptr),
                                         Seq(Val.I32(0), Val.I32(meth.vindex)))),
        Inst(n,         Op.Load(sigptr, Val.Local(meth_**, Type.Ptr(sigptr))))
      ).flatMap(onInst)

    // Static method elems
    //
    //    %$n = method-elem[$sig] $instance, $method
    //
    // Lowered to
    //
    //    %$n = copy @${method.name}: $sig*
    //
    case Inst(Some(n), Seq(), Op.Method(sig, obj, ExStaticMethod(meth))) =>
      onInst(Inst(n, Op.Copy(Val.Global(meth.name, Type.Ptr(sig)))))

    case Inst(n, attrs, _: Op.As) =>
      ???

    case Inst(n, attrs, _: Op.Is) =>
      ???

    case _ =>
      super.onInst(inst)
  }

  override def onType(ty: Type) = super.onType(ty match {
    case _: Type.ClassKind => i8_*
    case _                 => ty
  })

  override def onVal(value: Val) = super.onVal(value match {
    case Val.Null =>
      zero_i8_*
    case Val.Class(ty) =>
      ty match {
        case _ if Intr.intrinsic_class.contains(ty) =>
          Intr.intrinsic_class(ty)
        case Type.Null =>
          Intr.null_class
        case Type.Class(name) =>
          Val.Global(name + "cls", Type.Ptr(Type.I8))
        case _ =>
          ???
      }
    case _ =>
      value
  })

  object ExVirtualMethod {
    def unapply(name: Global): Option[Node.Method] =
      cha.get(name).collect {
        case meth: Node.Method if meth.isVirtual => meth
      }
  }

  object ExStaticMethod {
    def unapply(name: Global): Option[Node.Method] =
      cha.get(name).collect {
        case meth: Node.Method if meth.isStatic => meth
      }
  }

  object ExField {
    def unapply(name: Global): Option[Node.Field] =
      cha.get(name).collect {
        case fld: Node.Field => fld
      }
  }
}
