package native
package compiler
package pass

import native.nir._

/** Lowers classes, methods and fields down to
 *  structs with accompanying vtables.
 *
 *  For example a class w:
 *
 *      class $name() {
 *        .. var $fname: $fty = $fvalue
 *        .. declare $mname: $mty
 *        .. define $mname: $mty = $body
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
 *      .. define $mname: $mty = $body
 *
 *  Eliminates:
 *  - Type.{ObjectClass, Class}
 *  - Defn.Class
 *  - Op.Obj*
 */
trait ObjectLowering extends Pass {
  private val i8_*   = Type.Ptr(Type.I8)
  private val vtable = Global.Atom("vtable")
  private val data   = Global.Atom("data")
  private val vconst = Global.Atom("vconst")

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

      val classStruct = Defn.Struct(Seq(), name, Seq(Type.Ptr(vtableStructTy), dataStructTy))

      val vtableConstName = Global.Tagged(name, vconst)
      val vtableConstVal  = Val.Struct(vtableStructName, declareVals ++ defineVals)
      val vtableConst     = Defn.Var(Seq(), vtableConstName, vtableStructTy, vtableConstVal)

      (Seq(vtableStruct, dataStruct, classStruct, vtableConst) ++ defines).flatMap(onDefn)

    case _ =>
      super.onDefn(defn)
  }

  override def onType(ty: Type) = super.onType(ty match {
    case Type.Class(_)
       | Type.ObjectClass => i8_*
    case _                => ty
  })
}
