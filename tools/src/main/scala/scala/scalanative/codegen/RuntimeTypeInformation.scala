package scala.scalanative
package codegen

import scalanative.linker.{Class, ScopeInfo, Trait}
import scalanative.util.unreachable

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
      case "scala.scalanative.runtime.RawSize"          =>
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
    val flags = nir.Val.Int(Flags(info))
    val typeStr = nir.Val.String(typeName)
    val traits = meta.interfaces(info)
    val interfacesCount = nir.Val.Int(traits.size)
    val interfaces = nir.Val.Const(
      nir.Val.ArrayValue(nir.Type.Ptr, traits.map(meta.rtti(_).const))
    )

    val base = nir.Val.StructValue(
      classConst :: meta.lockWordVals :::
        flags ::
        interfacesCount ::
        interfaces ::
        typeStr :: Nil
    )
    info match {
      case cls: Class =>
        val itable = meta.itable(cls)
        val superClass =
          cls.parent.map(meta.rtti(_).const).getOrElse(nir.Val.Null)
        nir.Val.StructValue(
          base ::
            nir.Val.Int(typeSize) :: // size
            nir.Val.Int(meta.superclassCount(cls)) ::
            meta.layout(cls).referenceOffsetsValue :: // refFieldOffsets
            nir.Val.Int(itable.mask) ::
            nir.Val.Int(itable.shift) ::
            itable.value ::
            superClass ::
            lineage(cls) ::
            meta.dynmap(cls).value ::
            meta.vtable(cls).value ::
            Nil
        )
      case _ =>
        base
    }
  }

  /* Superclasses from the root, then the class itself, padded with nulls.
   * lineage(C)(superclassCount(C)) is C, so an object is an instance of C
   * exactly when lineage(superclassCount(C)) of its class is C.
   */
  private def lineage(cls: Class): nir.Val = {
    val rootFirst = meta.ancestors(cls).reverse.map(meta.rtti(_).const)
    nir.Val.ArrayValue(
      nir.Type.Ptr,
      rootFirst
        .take(CommonMemoryLayouts.LineageSize)
        .padTo(CommonMemoryLayouts.LineageSize, nir.Val.Null)
    )
  }
}

private[codegen] object RuntimeTypeInformation {

  /** Bits of the RTTI flags word. Keep in sync with ObjectHeader.h and
   *  scala.scalanative.runtime.Class.
   */
  object Flags {
    final val IsInterface = 1 << 0
    final val IsPrimitive = 1 << 1
    final val IsArray = 1 << 2
    final val IsObjectArray = 1 << 3
    final val IsBlobArray = 1 << 4
    final val IsBoxedPtr = 1 << 5
    final val IsWeakReference = 1 << 6
    // Traits store the low 24 bits of their itable key in bits 8-31, enough
    // for java.lang.Class to start lookups in itables up to 2^24 slots
    // Traits only: every class holds the trait in the slot where its lookup
    // starts, see Metadata.alwaysAtHomeSlot
    final val IsAtHomeSlot = 1 << 7
    final val TraitKeyShift = 8
    final val TraitKeyMask = 0xffffff

    private val ObjectArray =
      nir.Global.Top("scala.scalanative.runtime.ObjectArray")
    private val WeakReference = nir.Global.Top("java.lang.ref.WeakReference")

    /** Computed from the type and its ancestors only. */
    def apply(info: ScopeInfo)(implicit meta: Metadata): Int = info match {
      case trt: Trait =>
        IsInterface |
          (if (meta.alwaysAtHomeSlot(trt)) IsAtHomeSlot else 0) |
          (StableHash.traitKey(trt) & TraitKeyMask) << TraitKeyShift
      case cls: Class =>
        val names = meta.ancestors(cls).map(_.name)
        def flag(cond: Boolean, bit: Int) = if (cond) bit else 0
        flag(nir.Rt.PrimitiveTypes.contains(cls.name), IsPrimitive) |
          flag(names.contains(nir.Rt.GenericArray.name), IsArray) |
          flag(cls.name == ObjectArray, IsObjectArray) |
          flag(cls.name == nir.Rt.BlobArray.name, IsBlobArray) |
          flag(cls.name == nir.Rt.BoxedPtr.name, IsBoxedPtr) |
          flag(names.contains(WeakReference), IsWeakReference)
    }
  }

  private val classConst =
    nir.Val.Global(
      nir.Rt.Class.name.member(nir.Sig.Generated("type")),
      nir.Type.Ptr
    )

}
