package scala.scalanative.nir

/** The synchronization attributes of a NIR operation.
 *
 *  @param memoryOrder
 *    The memory ordering constraint on the operation.
 *  @param isVolatile
 *    `true` iff the memory access is marked "volatile".
 */
final case class SyncAttrs(
    memoryOrder: MemoryOrder,
    isVolatile: Boolean = true
)
