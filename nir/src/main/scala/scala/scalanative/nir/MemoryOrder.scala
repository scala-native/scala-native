package scala.scalanative
package nir

/** An atomic memory ordering constraints.
 *
 *  Atomic instructions take ordering parameters specifying with which other
 *  instructions they synchronize.
 *
 *  @see
 *    https://llvm.org/docs/LangRef.html#atomic-memory-ordering-constraints.
 */
sealed abstract class MemoryOrder(private[nir] val tag: Int) {

  /** A textual representation of `this`. */
  final def show: String = nir.Show(this)

}

object MemoryOrder {

  /** The set of values that can be read is governed by the happens-before
   *  partial order
   */
  case object Unordered extends MemoryOrder(0)

  /** In addition to the guarantees of `Unordered`, there is a single total
   *  order for modifications by monotonic operations on each address.
   */
  case object Monotonic extends MemoryOrder(1)

  /** In addition to the guarantees of `Monotonic`, a *synchronizes-with* edge
   *  may be formed with a release operation.
   */
  case object Acquire extends MemoryOrder(2)

  /** In addition to the guarantees of `Monotonic`, if this operation writes a
   *  value which is subsequently read by an acquire operation, it
   *  *synchronizes-with* that operation.
   */
  case object Release extends MemoryOrder(3)

  /** Acts as both an `Acquire` and `Release` operation on its address. */
  case object AcqRel extends MemoryOrder(4)

  /** In addition to the guarantees of `AcqRel`, there is a global total order
   *  on all sequentially-consistent operations on all addresses, which is
   *  consistent with the *happens-before* partial order and with the
   *  modification orders of all the affected addresses.
   */
  case object SeqCst extends MemoryOrder(5)

}
