package scala.scalanative
package codegen

private[codegen] case class ITable(
    useFastITables: Boolean,
    size: Int,
    value: nir.Val.ArrayValue
) {
  val const = nir.Val.Const(value)
}

private[codegen] object ITable {
  val availableITableSizes =
    1.to(TraitsUniverse.ColorBits).map(math.pow(2, _).toInt)

  val ITableEntry = nir.Type.StructValue(nir.Type.Int :: nir.Type.Ptr :: Nil)
  val emptyItable = nir.Val.StructValue(nir.Val.Int(0) :: nir.Val.Null :: Nil)

  def build(cls: linker.Class)(implicit meta: Metadata): ITable = {
    val implementedTraits = cls.linearized.collect {
      case cls: linker.Trait =>
        cls -> TraitsUniverse.TraitId.unsafe(meta.ids(cls))
    }.distinct

    val fastItableSize =
      if (implementedTraits.isEmpty) Some(0)
      else if (implementedTraits.size > TraitsUniverse.MaxColor) None
      else
        availableITableSizes.find { size =>
          // Find a minimial size of itable at which there are no colisions based on trait ids
          val occupiedPositions = Array.fill[Boolean](size)(false)
          implementedTraits.forall {
            case (_, id) =>
              val position = id.itablePosition(size)
              try !occupiedPositions(position)
              finally occupiedPositions(position) = true
          }
        }

    val useFastITables = fastItableSize.isDefined
    val itablesSize = fastItableSize.getOrElse(implementedTraits.size)
    val itables = Array.fill(itablesSize)(emptyItable)
    implementedTraits
      .sortBy(_._2.value) // sort by id, required for fallback binary search
      .zipWithIndex
      .foreach {
        case ((trt, traitId), idx) =>
          val position = fastItableSize
            .map(traitId.itablePosition(_))
            .getOrElse(idx)
          itables(position) = nir.Val.StructValue(
            nir.Val.Int(traitId.value)
              :: {
                if (trt.methods.isEmpty) nir.Val.Null
                else
                  nir.Val.Const(
                    nir.Val.ArrayValue(
                      nir.Type.Ptr,
                      for (sig <- trt.methods)
                        yield cls
                          .resolve(sig)
                          .map(nir.Val.Global(_, nir.Type.Ptr))
                          .getOrElse(nir.Val.Null)
                    )
                  )
              } :: Nil
          )
      }

    ITable(
      useFastITables,
      itables.size,
      nir.Val.ArrayValue(ITableEntry, itables)
    )
  }
}
