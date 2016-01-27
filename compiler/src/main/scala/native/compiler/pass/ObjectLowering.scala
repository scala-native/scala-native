package native
package compiler
package pass

import native.nir._, Shows._
import native.util.sh

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
 *      struct $name_vtable { .. ptr $mty }
 *      struct $name_data { .. $fty }
 *      struct $name { $name_vtable*, $name_data }
 *
 *      var $name_vconst: struct $name_vtable = struct[$name_vtable](..$mname)
 *
 *      .. def $mname: $mty = $body
 *
 *  Eliminates:
 *  - Type.{ObjectClass, Class}
 *  - Defn.Class
 *  - Op.Obj*
 */
trait ObjectLowering extends Pass {
  private val i8_*         = Type.Ptr(Type.I8)

  private val vtable = Global.Atom("vtable")
  private val data   = Global.Atom("data")
  private val vconst = Global.Atom("vconst")
  private val cls    = Global.Atom("cls")

  private val nrtClassName = Global.Atom("nrt_class_t")
  private val nrtClassTy   = Type.Struct(nrtClassName)

  override def onDefn(defn: Defn) = defn match {
    case Defn.Class(attrs, name, None, Seq(), members) =>
      val fields      = members.collect { case defn: Defn.Var     => defn }
      val declares    = members.collect { case defn: Defn.Declare => defn }
      val defines     = members.collect { case defn: Defn.Define  => defn }
      val declareVals = declares.map { decl => Val.Zero(Type.Ptr(decl.ty)) }
      val defineVals  = defines.map  { defn => Val.Global(defn.name, Type.Ptr(defn.ty)) }
      val sigs        = declares.map(_.ty) ++ defines.map(_.ty)

      val vtableStructName = Global.Tagged(name, vtable)
      val vtableStructTy   = Type.Struct(vtableStructName)
      val vtableStruct     = Defn.Struct(Seq(), vtableStructName, sigs.map(Type.Ptr))

      val dataStructName = Global.Tagged(name, data)
      val dataStructTy   = Type.Struct(dataStructName)
      val dataStruct     = Defn.Struct(Seq(), dataStructName, fields.map(_.ty))

      val classStruct   = Defn.Struct(Seq(), name, Seq(Type.Ptr(vtableStructTy), dataStructTy))
      val classStructTy = Type.Struct(name)

      val vtableConstName = Global.Tagged(name, vconst)
      val vtableConstVal  = Val.Struct(vtableStructName, declareVals ++ defineVals)
      val vtableConst     = Defn.Var(Seq(), vtableConstName, vtableStructTy, vtableConstVal)

      (Seq(vtableStruct, dataStruct, classStruct, vtableConst) ++ defines).flatMap(onDefn)

    case _ =>
      super.onDefn(defn)
  }

  override def onInstr(instr: Instr) = instr match {
    case Instr(n, attrs, Op.ObjAlloc(ty)) =>
      val cls = fresh()
      val clsValue = Val.Local(cls, Type.ClassClass)
      Seq(Instr(cls, Op.ClassOf(ty)),
          Instr(n, attrs, Intrinsic.call(Intrinsic.object_alloc, clsValue))).flatMap(onInstr)
    case Instr(n, attrs, Op.ClassOf(ty)) =>
      (ty match {
        case builtin: Type.BuiltinClassKind =>
          Seq(Instr(n, attrs, Intrinsic.call(Intrinsic.builtin_class(builtin))))
        case Type.Class(name) =>
          Seq(Instr(n, attrs, Intrinsic.call(Intrinsic.class_for_name, name.stringValue)))
        case _ =>
          ???
      }).flatMap(onInstr)
    case _ =>
      super.onInstr(instr)
  }

  override def onType(ty: Type) = super.onType(ty match {
    case _: Type.ClassKind => i8_*
    case _                 => ty
  })
}
