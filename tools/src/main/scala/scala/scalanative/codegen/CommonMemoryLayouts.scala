package scala.scalanative
package codegen

private[codegen] object CommonMemoryLayouts {

  /** Length of the lineage stored inline in class RTTI. Type tests against
   *  classes with more superclasses walk the superclass chain. Keep in sync
   *  with SCALANATIVE_RTTI_LINEAGE_SIZE in ObjectHeader.h.
   */
  final val LineageSize = 8
}

private[codegen] class CommonMemoryLayouts(implicit meta: Metadata) {
  import CommonMemoryLayouts.LineageSize

  sealed abstract class Layout(types: List[nir.Type]) {
    def this(types: nir.Type*) = this(types.toList)

    val layout: nir.Type.StructValue = nir.Type.StructValue(types.toList)
    def size: Long = MemoryLayout.sizeOf(layout)(meta.platform)
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
          nir.Type.Int :: // Flags, see RuntimeTypeInformation.Flags
          nir.Type.Int :: // InterfacesCount
          nir.Type.Ptr :: // Interfaces
          nir.Type.Ptr :: // ClassName
          Nil
      ) {
    final val RttiIdx = Common.RttiIdx
    final val LockWordIdx = Common.LockWordIdx
    final val FlagsIdx =
      if (meta.usesLockWords) LockWordIdx + 1
      else RttiIdx + 1
    final val InterfacesCountIdx = FlagsIdx + 1
    final val InterfacesIdx = InterfacesCountIdx + 1
    final val ClassNameIdx = InterfacesIdx + 1
  }

  /* RTTI of classes, see class RuntimeTypeInformation. Its layout does not
   * depend on the rest of the program, apart from the variable-sized virtual
   * table at the end.
   */
  object ClassRtti extends Layout() {
    // Common layout not including variable-sized virtual table
    private val baseLayout =
      Rtti.layout ::
        nir.Type.Int :: // class size
        nir.Type.Int :: // number of superclasses
        nir.Type.Ptr :: // reference offsets
        nir.Type.Int :: // itable mask
        nir.Type.Int :: // itable shift
        nir.Type.Ptr :: // itables
        nir.Type.Ptr :: // superClass
        // superclasses from the root, then the class itself, then nulls
        nir.Type.ArrayValue(nir.Type.Ptr, LineageSize) :: // lineage
        DynamicHashMap.ty :: // dynmap, null if the class has no proxies
        Nil

    override val layout =
      genLayout(vtable = nir.Type.ArrayValue(nir.Type.Ptr, 0))

    def genLayout(vtable: nir.Type): nir.Type.StructValue =
      nir.Type.StructValue(
        baseLayout ::: vtable :: Nil
      )

    final val RttiIdx = Common.RttiIdx
    final val SizeIdx = RttiIdx + 1
    final val SuperclassCountIdx = SizeIdx + 1
    final val ReferenceOffsetsIdx = SuperclassCountIdx + 1
    final val ITableMaskIdx = ReferenceOffsetsIdx + 1
    final val ITableShiftIdx = ITableMaskIdx + 1
    final val ItablesIdx = ITableShiftIdx + 1
    final val SuperClassIdx = ItablesIdx + 1
    final val LineageIdx = SuperClassIdx + 1
    final val DynmapIdx = LineageIdx + 1
    final val VtableIdx = DynmapIdx + 1
  }

  object ITable extends Layout() {
    // {trait rtti, methods}, a null trait rtti marks an empty slot
    override val layout: nir.Type.StructValue =
      nir.Type.StructValue(nir.Type.Ptr :: nir.Type.Ptr :: Nil)
    final val TraitIdx = 0
    final val MethodsIdx = 1
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
