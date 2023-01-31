package scala.scalanative.codegen

import scala.scalanative.nir._

class CommonMemoryLayouts(implicit meta: Metadata) {
  sealed trait Layout {
    val layout: Type.StructValue
    lazy val fields: Int = layout.tys.size
  }

  object Rtti extends Layout {
    val layout = Type.StructValue(
      Type.Ptr :: // ClassRtti
        Type.Int :: // ClassId
        Type.Int :: // Traitid
        Type.Ptr :: // ClassName
        Nil
    )
    final val RttiIdx = 0
    final val ClassIdIdx = RttiIdx + 1
    final val TraitIdIdx = ClassIdIdx + 1
    final val ClassNameIdx = TraitIdIdx + 1
  }

  // RTTI specific for classess, see class RuntimeTypeInformation
  object ClassRtti extends Layout {
    val usesDynMap = meta.linked.dynsigs.nonEmpty
    private val dynMapType = if (usesDynMap) Some(DynamicHashMap.ty) else None
    // Common layout not including variable-sized virtual table
    private val baseLayout =
      Rtti.layout ::
        Type.Int :: // class size
        Type.Int :: // id range
        FieldLayout.referenceOffsetsTy :: // reference offsets
        dynMapType.toList

    val layout = genLayout(vtable = Type.ArrayValue(Type.Ptr, 0))

    def genLayout(vtable: Type): Type.StructValue = Type.StructValue(
      baseLayout ::: vtable :: Nil
    )

    final val RttiIdx = 0
    final val SizeIdx = RttiIdx + 1
    final val IdRangeIdx = SizeIdx + 1
    final val ReferenceOffsetsIdx = IdRangeIdx + 1
    final val DynmapIdx =
      if (usesDynMap) ReferenceOffsetsIdx + 1 else -1
    final val VtableIdx =
      (if (usesDynMap) DynmapIdx else ReferenceOffsetsIdx) + 1
  }

  object ObjectHeader extends Layout {
    val layout = Type.StructValue(
      Type.Ptr :: // RTTI
        Nil
    )
  }

  object ArrayHeader extends Layout {
    val layout = Type.StructValue(
      Type.Ptr :: // RTTI
        Type.Int :: // length
        Type.Int :: // padding
        Nil
    )

    final val RttiIdx = 0
    final val LengthIdx = RttiIdx + 1
    final val PaddingIdx = LengthIdx + 1
  }
}
