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
 *  - Type.*Class
 *  - Defn.Class
 *  - Op.Obj*
 *  - Val.{Null, Class}
 */
trait ObjectLowering extends Pass { self: Lowering =>
  private val i8_*      = Type.Ptr(Type.I8)
  private val zero_i8_* = Val.Zero(i8_*)

  private val vtableTag = Global.Atom("vtable")
  private val vconstTag = Global.Atom("vconst")
  private val clsTag    = Global.Atom("cls")

  private val nrtClassName = Global.Atom("nrt_class_t")
  private val nrtClassTy   = Type.Struct(nrtClassName)

  override def onDefn(defn: Defn) = defn match {
    case Defn.Class(_, name, _, _, members) =>
      val methods    = members.collect { case defn: Defn.Define => defn }
      val cls        = cha(name).asInstanceOf[Node.Class]
      val data       = cls.data.map(_.ty)
      val vtable     = cls.vtable
      val vtableTys  = vtable.map { case (ty, _) => ty }
      val vtableVals = vtable.map { case (_, v) => v }

      val vtableStructName = Global.Tagged(name, vtableTag)
      val vtableStructTy   = Type.Struct(vtableStructName)

      val vtableStruct    = Defn.Struct(Seq(), vtableStructName, vtableTys)
      val vtableConstName = Global.Tagged(name, vconstTag)
      val vtableConstVal  = Val.Struct(vtableStructName, vtableVals)
      val vtableConst     = Defn.Var(Seq(), vtableConstName, vtableStructTy, vtableConstVal)

      val classConstName = Global.Tagged(name, clsTag)
      val classConstTy   = Type.I8
      val classConstVal  = Val.Zero(Type.I8)
      val classConst     = Defn.Var(Seq(), classConstName, classConstTy, classConstVal)

      val classStruct   = Defn.Struct(Seq(), name, Type.Ptr(vtableStructTy) +: data)
      val classStructTy = Type.Struct(name)

      (Seq(vtableStruct, classStruct, classConst, vtableConst) ++ methods).flatMap(onDefn)

    case _ =>
      super.onDefn(defn)
  }

  override def onInstr(instr: Instr) = instr match {
    case Instr(Some(n), Seq(), Op.ObjAlloc(Type.Class(clsname))) =>
      val clsValue = Val.Global(Global.Tagged(clsname, clsTag), Type.Ptr(Type.I8))
      val sizeValue = Val.Size(Type.Struct(clsname))
      onInstr(Instr(n, Intrinsic.call(Intrinsic.alloc, clsValue, sizeValue)))

    case Instr(Some(n), Seq(), Op.ObjFieldElem(ty, obj, fldname)) =>
      val fld    = cha(fldname).asInstanceOf[Node.Field]
      val cast   = fresh()
      val clsptr = Type.Ptr(Type.Struct(fld.in.name))
      Seq(
        Instr(cast, Op.Conv(Conv.Bitcast, clsptr, obj)),
        Instr(n,    Op.Elem(ty, Val.Local(cast, clsptr), Seq(Val.I32(0), Val.I32(fld.index))))
      ).flatMap(onInstr)

    // case Instr(n, attrs, Op.ObjMethodElem(Type.Class(clsname), methname, obj)) =>
    //   ???

    // case Instr(n, attrs, Op.ObjAs(Type.Class(clsname), obj)) =>
    //   ???

    // case Instr(n, attrs, Op.ObjIs(Type.Class(clsname), obj)) =>
    //   ???

    case _ =>
      super.onInstr(instr)
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
        case builtin: Type.BuiltinClassKind =>
          Intrinsic.builtin_class(builtin)
        case Type.Class(name) =>
          Val.Global(Global.Tagged(name, clsTag), Type.Ptr(Type.I8))
        case _ =>
          ???
      }
    case _ =>
      value
  })
}
