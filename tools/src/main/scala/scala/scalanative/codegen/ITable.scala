package scala.scalanative
package codegen

/** Interface table of a class: an open-addressing hash table of
 *  `{trait rtti, methods}` entries for every trait the class implements.
 *
 *  A lookup for trait T starts at `(StableHash.traitKey(T) >>> shift) & mask`
 *  and probes linearly until it finds T's rtti, or an empty slot when the class
 *  does not implement T. Tables are at most half full, so every lookup ends at
 *  an empty slot or earlier. Entries are compared by rtti pointer, so equal
 *  keys never make a lookup wrong, only longer. The table depends only on the
 *  class and its ancestors.
 */
private[codegen] case class ITable(
    mask: Int,
    // Selects which bits of the trait keys index the table
    shift: Int,
    value: nir.Val,
    // Traits not stored in the slot where their lookup starts, with the
    // number of slots between
    displaced: Seq[(linker.Trait, Int)]
)

private[codegen] object ITable {

  /** Tables grow up to this factor above the number of traits to find a
   *  placement in which every trait is found on the first probe.
   */
  private final val MaxGrowth = 4

  /** Failing that, tables grow up to this factor to keep every trait in the
   *  slot where its lookup starts or the next one, see Lower.genItableProbe.
   */
  private final val MaxBoundedGrowth = 64

  /** java.lang.Class reads the low KeyBits bits of trait keys from RTTI flags,
   *  so shift + log2(size) must not exceed it.
   */
  private final val KeyBits = 24

  private val emptyEntry =
    nir.Val.StructValue(nir.Val.Null :: nir.Val.Null :: Nil)

  /** Shared by classes that implement no traits: lookups read one empty slot.
   */
  val emptyName: nir.Global.Member =
    nir.Global
      .Top("__scalanative_metadata")
      .member(nir.Sig.Generated("empty_itable"))
  def emptyDefn(implicit meta: Metadata): nir.Defn.Const =
    nir.Defn.Const(
      nir.Attrs.None,
      emptyName,
      meta.layouts.ITable.layout,
      emptyEntry
    )(nir.SourcePosition.NoPosition)

  def build(cls: linker.Class)(implicit meta: Metadata): ITable = {
    val traits = meta.interfaces(cls)
    if (traits.isEmpty)
      ITable(
        mask = 0,
        shift = 0,
        nir.Val.Global(emptyName, nir.Type.Ptr),
        Nil
      )
    else {
      val keys = traits.map(meta.traitKey)
      val minSize = OpenAddressing.nextPowerOfTwo(traits.size * 2)
      def start(key: Int, size: Int, shift: Int) = (key >>> shift) & (size - 1)
      def collisionFree(size: Int, shift: Int) =
        keys.map(start(_, size, shift)).distinct.size == keys.size
      // The smallest table, then the smallest shift, in which every trait is
      // found on the first probe
      val candidates = for {
        size <- Iterator
          .iterate(minSize)(_ * 2)
          .takeWhile(_ <= minSize * MaxGrowth)
        bits = Integer.numberOfTrailingZeros(size)
        shift <- Iterator.range(0, KeyBits - bits + 1)
      } yield (size, shift)
      // Otherwise the smallest table and shift keeping every trait in the
      // slot where its lookup starts or the next one
      def maxDisplacement(size: Int, shift: Int): Int = {
        val used = new Array[Boolean](size)
        keys.foldLeft(0) { (max, key) =>
          var pos = start(key, size, shift)
          var distance = 0
          while (used(pos)) { pos = (pos + 1) & (size - 1); distance += 1 }
          used(pos) = true
          max.max(distance)
        }
      }
      def bounded = for {
        size <- Iterator
          .iterate(minSize)(_ * 2)
          .takeWhile(_ <= minSize * MaxBoundedGrowth)
        bits = Integer.numberOfTrailingZeros(size)
        shift <- Iterator.range(0, KeyBits - bits + 1)
      } yield (size, shift)
      val (size, shift) =
        candidates
          .find { case (size, shift) => collisionFree(size, shift) }
          .orElse(bounded.find {
            case (size, shift) => maxDisplacement(size, shift) <= 1
          })
          .getOrElse((minSize, 0))
      val (slots, displacement) = OpenAddressing.place(size, emptyEntry)(
        traits.zip(keys).map {
          case (trt, key) =>
            start(key, size, shift) -> nir.Val.StructValue(
              meta.rtti(trt).const :: methods(cls, trt) :: Nil
            )
        }
      )
      ITable(
        mask = size - 1,
        shift,
        nir.Val.Const(nir.Val.ArrayValue(meta.layouts.ITable.layout, slots)),
        traits.zip(displacement).filter(_._2 != 0)
      )
    }
  }

  private def methods(cls: linker.Class, trt: linker.Trait): nir.Val =
    if (trt.methods.isEmpty) nir.Val.Null
    else
      nir.Val.Const(
        nir.Val.ArrayValue(
          nir.Type.Ptr,
          trt.methods.map { sig =>
            cls
              .resolve(sig)
              .map(nir.Val.Global(_, nir.Type.Ptr))
              .getOrElse(nir.Val.Null)
          }
        )
      )
}
