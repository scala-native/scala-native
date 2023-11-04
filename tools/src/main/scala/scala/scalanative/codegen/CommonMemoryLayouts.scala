package scala.scalanative
package codegen

class CommonMemoryLayouts(implicit meta: Metadata) {

  sealed abstract class Layout(types: List[nir.Type]) {
    def this(types: nir.Type*) = this(types.toList)

    val layout: nir.Type.StructValue = nir.Type.StructValue(types.toList)
    def fields: Int = layout.tys.size
  }

  private object Common {
    final val RttiIdx = 0
    final val LockWordIdx =
      if (meta.usesLockWords) RttiIdx + 1
      else -1
  }

  object Rtti
      extends Layout(
        nir.Type.Ptr :: // ClassRtti
          meta.lockWordType.toList ::: // optional, multithreading only
          nir.Type.Int :: // ClassId
          nir.Type.Int :: // Traitid
          nir.Type.Ptr :: // ClassName
          Nil
      ) {
    final val RttiIdx = Common.RttiIdx
    final val LockWordIdx = Common.LockWordIdx
    final val ClassIdIdx =
      if (meta.usesLockWords) LockWordIdx + 1
      else RttiIdx + 1
    final val TraitIdIdx = ClassIdIdx + 1
    final val ClassNameIdx = TraitIdIdx + 1
  }

  // RTTI specific for classess, see class RuntimeTypeInformation
  object ClassRtti extends Layout() {
    val usesDynMap = meta.analysis.dynsigs.nonEmpty
    private val dynMapType = if (usesDynMap) Some(DynamicHashMap.ty) else None
    // Common layout not including variable-sized virtual table
    private val baseLayout =
      Rtti.layout ::
        nir.Type.Int :: // class size
        nir.Type.Int :: // id range
        FieldLayout.referenceOffsetsTy :: // reference offsets
        dynMapType.toList

    override val layout =
      genLayout(vtable = nir.Type.ArrayValue(nir.Type.Ptr, 0))

    def genLayout(vtable: nir.Type): nir.Type.StructValue =
      nir.Type.StructValue(
        baseLayout ::: vtable :: Nil
      )

    final val RttiIdx = Common.RttiIdx
    final val SizeIdx = RttiIdx + 1
    final val IdRangeIdx = SizeIdx + 1
    final val ReferenceOffsetsIdx = IdRangeIdx + 1
    final val DynmapIdx =
      if (usesDynMap) ReferenceOffsetsIdx + 1 else -1
    final val VtableIdx =
      (if (usesDynMap) DynmapIdx else ReferenceOffsetsIdx) + 1
  }

  object ObjectHeader
      extends Layout(
        nir.Type.Ptr :: // RTTI
          meta.lockWordType.toList // optional, multithreading only
      ) {
    final val RttiIdx = Common.RttiIdx
    final val LockWordIdx = Common.LockWordIdx
  }

  object Object
      extends Layout(
        ObjectHeader.layout,
        nir.Type.ArrayValue(nir.Type.Ptr, 0)
      ) {
    final val ObjectHeaderIdx = 0
    final val ValuesOffset = ObjectHeaderIdx + 1
  }

  object ArrayHeader
      extends Layout(
        nir.Type.Ptr :: // RTTI
          meta.lockWordType.toList ::: // optional, multithreading only
          nir.Type.Int :: // length
          nir.Type.Int :: // stride (used only by GC)
          Nil
      ) {
    final val RttiIdx = Common.RttiIdx
    final val LockWordIdx = Common.LockWordIdx
    final val LengthIdx =
      if (meta.usesLockWords) LockWordIdx + 1
      else RttiIdx + 1
    final val StrideIdx = LengthIdx + 1
  }

  object Array
      extends Layout(
        ArrayHeader.layout,
        nir.Type.ArrayValue(nir.Type.Nothing, 0)
      ) {
    final val ArrayHeaderIdx = 0
    final val ValuesIdx = ArrayHeaderIdx + 1
  }

}
