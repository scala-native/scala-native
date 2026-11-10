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
            referenceMapOrComponentType(cls) ::
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

  /* Reference map of instances, or for array classes the RTTI of the
   * element type: the GC scans arrays according to flags instead.
   */
  private def referenceMapOrComponentType(cls: Class): nir.Val =
    if (!nir.Type.isArray(cls.name) && cls.name != nir.Rt.GenericArray.name)
      meta.layout(cls).referenceOffsetsValue
    else
      arrayComponentTypes
        .get(cls.name.top)
        .flatMap(name => meta.analysis.infos.get(name))
        .map(info => meta.rtti(info).const)
        .getOrElse(nir.Val.Null)

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

    // Arrays only, bits 3-4: how the GC scans the elements
    final val ArrayScanShift = 3
    final val ArrayScanNone = 0 << ArrayScanShift // primitive values
    final val ArrayScanReferences = 1 << ArrayScanShift // references
    final val ArrayScanConservative = 2 << ArrayScanShift // raw bytes

    // Bits 5-6: java.lang.ref.Reference subclasses
    final val ReferenceKindShift = 5
    final val WeakReferenceKind = 1 << ReferenceKindShift
    final val SoftReferenceKind = 2 << ReferenceKindShift
    final val PhantomReferenceKind = 3 << ReferenceKindShift

    // Traits only: every class holds the trait in the slot where its lookup
    // starts, see Metadata.alwaysAtHomeSlot
    final val IsAtHomeSlot = 1 << 7
    // Traits only, bits 8-31: low 24 bits of the itable key, enough for
    // java.lang.Class to start lookups in itables up to 2^24 slots
    final val TraitKeyShift = 8
    final val TraitKeyMask = 0xffffff
    // Arrays only, bits 8-15: number of dimensions
    final val ArrayDimensionsShift = 8

    private val ObjectArray =
      nir.Global.Top("scala.scalanative.runtime.ObjectArray")
    private val WeakReference = nir.Global.Top("java.lang.ref.WeakReference")
    private val SoftReference = nir.Global.Top("java.lang.ref.SoftReference")
    private val PhantomReference =
      nir.Global.Top("java.lang.ref.PhantomReference")

    /** Computed from the type and its ancestors only. */
    def apply(info: ScopeInfo)(implicit meta: Metadata): Int = info match {
      case trt: Trait =>
        IsInterface |
          (if (meta.alwaysAtHomeSlot(trt)) IsAtHomeSlot else 0) |
          (StableHash.traitKey(trt) & TraitKeyMask) << TraitKeyShift
      case cls: Class =>
        val names = meta.ancestors(cls).map(_.name)
        def flag(cond: Boolean, bits: Int) = if (cond) bits else 0
        val isArray = names.contains(nir.Rt.GenericArray.name)
        val arrayFlags =
          if (!isArray) 0
          else
            IsArray | (1 << ArrayDimensionsShift) | {
              if (cls.name == ObjectArray) ArrayScanReferences
              else if (cls.name == nir.Rt.BlobArray.name) ArrayScanConservative
              else ArrayScanNone
            }
        val referenceKind =
          if (names.contains(WeakReference)) WeakReferenceKind
          else if (names.contains(SoftReference)) SoftReferenceKind
          else if (names.contains(PhantomReference)) PhantomReferenceKind
          else 0
        flag(nir.Rt.PrimitiveTypes.contains(cls.name), IsPrimitive) |
          arrayFlags |
          referenceKind
    }
  }

  /** Element type of the array classes: its RTTI is stored in place of the
   *  reference field offsets, which arrays do not use. The abstract
   *  runtime.Array has none.
   */
  private val arrayComponentTypes: Map[nir.Global.Top, nir.Global.Top] = {
    def rt(name: String) = nir.Global.Top("scala.scalanative.runtime." + name)
    Map(
      rt("BooleanArray") -> rt("PrimitiveBoolean"),
      rt("CharArray") -> rt("PrimitiveChar"),
      rt("ByteArray") -> rt("PrimitiveByte"),
      rt("ShortArray") -> rt("PrimitiveShort"),
      rt("IntArray") -> rt("PrimitiveInt"),
      rt("LongArray") -> rt("PrimitiveLong"),
      rt("FloatArray") -> rt("PrimitiveFloat"),
      rt("DoubleArray") -> rt("PrimitiveDouble"),
      rt("BlobArray") -> rt("PrimitiveByte"),
      rt("ObjectArray") -> nir.Rt.Object.name
    )
  }

  private val classConst =
    nir.Val.Global(
      nir.Rt.Class.name.member(nir.Sig.Generated("type")),
      nir.Type.Ptr
    )

}
