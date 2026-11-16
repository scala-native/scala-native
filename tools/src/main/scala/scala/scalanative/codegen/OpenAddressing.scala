package scala.scalanative
package codegen

/** Placement of entries in open-addressing tables with linear probing and a
 *  power-of-two size, shared by itables and the reflective proxy table. Lookups
 *  are emitted by Lower and implemented by the runtime.
 */
private[codegen] object OpenAddressing {

  def nextPowerOfTwo(n: Int): Int =
    if (n <= 1) 1 else Integer.highestOneBit(n - 1) << 1

  /** Places each `(start, value)` entry, in order, in its start slot or the
   *  first free slot after it, wrapping around. Returns the slots, `empty`
   *  where free, and for each entry how many slots after its start slot it is.
   */
  def place(size: Int, empty: nir.Val)(
      entries: Seq[(Int, nir.Val)]
  ): (Seq[nir.Val], Seq[Int]) = {
    val mask = size - 1
    val slots = new Array[nir.Val](size)
    val displacement = entries.map {
      case (start, value) =>
        var pos = start
        while (slots(pos) != null) pos = (pos + 1) & mask
        slots(pos) = value
        (pos - start) & mask
    }
    (
      slots.toIndexedSeq.map(slot => if (slot == null) empty else slot),
      displacement
    )
  }
}
