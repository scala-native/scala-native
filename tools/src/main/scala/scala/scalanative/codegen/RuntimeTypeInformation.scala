package scala.scalanative
package codegen

import scalanative.util.unreachable
import scalanative.linker.{ScopeInfo, Class, Trait}

private[codegen] class RuntimeTypeInformation(info: ScopeInfo)(implicit
    meta: Metadata
) {
  import meta.platform

  import RuntimeTypeInformation._
  private def isScalaNativeRuntimeType =
    info.name.id.startsWith("scala.scalanative.runtime.")
  private def isPrimitiveType = isScalaNativeRuntimeType && {
    val id = info.name.id
    id.startsWith("scala.scalanative.runtime.Primitive") ||
      id == "scala.scalanative.runtime.RawSize" ||
      id == "scala.scalanative.runtime.RawPtr"
  }

  private def typeName = {
    if (isScalaNativeRuntimeType) info.name.id match {
      case "scala.scalanative.runtime.PrimitiveByte"    => "byte"
      case "scala.scalanative.runtime.PrimitiveShort"   => "short"
      case "scala.scalanative.runtime.PrimitiveInt"     => "int"
      case "scala.scalanative.runtime.PrimitiveLong"    => "long"
      case "scala.scalanative.runtime.PrimitiveChar"    => "char"
      case "scala.scalanative.runtime.PrimitiveFloat"   => "float"
      case "scala.scalanative.runtime.PrimitiveDouble"  => "double"
      case "scala.scalanative.runtime.PrimitiveBoolean" => "boolean"
      case "scala.scalanative.runtime.PrimitiveUnit"    => "void"
      case "scala.scalanative.runtime.RawSize"          => "size"
      case "scala.scalanative.runtime.RawPtr"           => "pointer"
      case "scala.scalanative.runtime.BoxedUnit$" => "scala.runtime.BoxedUnit"
      case name                                   => name
    }
    else info.name.id
  }

  private def typeSize = {
    if (isPrimitiveType) info.name.id match {
      case "scala.scalanative.runtime.PrimitiveByte"    => 1
      case "scala.scalanative.runtime.PrimitiveShort"   => 2
      case "scala.scalanative.runtime.PrimitiveInt"     => 4
      case "scala.scalanative.runtime.PrimitiveLong"    => 8
      case "scala.scalanative.runtime.PrimitiveChar"    => 4
      case "scala.scalanative.runtime.PrimitiveFloat"   => 4
      case "scala.scalanative.runtime.PrimitiveDouble"  => 8
      case "scala.scalanative.runtime.PrimitiveBoolean" => 1
      case "scala.scalanative.runtime.PrimitiveUnit"    => 8
      case "scala.scalanative.runtime.RawSize" =>
        MemoryLayout.sizeOf(nir.Type.Size).toInt
      case "scala.scalanative.runtime.RawPtr" =>
        MemoryLayout.sizeOf(nir.Type.Ptr).toInt
    }
    else
      info match {
        case cls: Class => meta.layout(cls).size.toInt
        case _          => util.unreachable
      }
  }

  val name: nir.Global.Member = info.name.member(nir.Sig.Generated("type"))
  val const: nir.Val.Global = nir.Val.Global(name, nir.Type.Ptr)
  val struct: nir.Type.StructValue = info match {
    case cls: Class =>
      meta.layouts.ClassRtti
        .genLayout(vtable = meta.vtable(cls).ty)
    case _ => meta.layouts.Rtti.layout
  }
  lazy val value: nir.Val.StructValue = {
    val typeId = nir.Val.Int(meta.ids(info))
    val typeStr = nir.Val.String(typeName)
    val traits = info.linearized
      .collect { case cls: Trait if cls != info => cls }
      .sortBy(meta.ids(_))
    val interfacesCount = nir.Val.Int(traits.size)
    val interfaces = nir.Val.Const(
      nir.Val.ArrayValue(nir.Type.Ptr, traits.map(meta.rtti(_).const))
    )

    val base = nir.Val.StructValue(
      classConst :: meta.lockWordVals :::
        typeId ::
        interfacesCount ::
        interfaces ::
        typeStr :: Nil
    )
    info match {
      case cls: Class =>
        val dynmap =
          if (!meta.layouts.ClassRtti.usesDynMap) Nil
          else List(meta.dynmap(cls).value)
        val range = meta.ranges(cls)
        val itable = meta.itable(cls)
        val itablesSize = {
          // we want to have compile-time computed mask for fast selction
          // negative size to mark requirement of using slow-path binary search
          if (itable.useFastITables) (itable.size - 1).max(0)
          else -itable.size
        }
        val superClass =
          cls.parent.map(meta.rtti(_).const).getOrElse(nir.Val.Null)
        nir.Val.StructValue(
          base ::
            nir.Val.Int(typeSize) :: // size
            nir.Val.Int(range.last) :: // idRangeUntil
            meta.layout(cls).referenceOffsetsValue :: // refFieldOffsets
            // Free slot for additional Int32 to be used in the future
            nir.Val.Int(itablesSize) ::
            itable.const ::
            superClass ::
            dynmap :::
            meta.vtable(cls).value ::
            Nil
        )
      case _ =>
        base
    }
  }

  lazy val canUseFastITables: Boolean = info match {
    case cls: Class => meta.itable(cls).useFastITables
    case cls: Trait => cls.implementors.forall(meta.rtti(_).canUseFastITables)
  }
}

private[codegen] object RuntimeTypeInformation {

  private val classConst =
    nir.Val.Global(
      nir.Rt.Class.name.member(nir.Sig.Generated("type")),
      nir.Type.Ptr
    )

}
