package scala.scalanative
package native

/** Zone allocator that automatically frees allocations whenever
 *  syntactic boundary of the zone is over.
 */
trait Zone extends Alloc

object Zone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: Zone => T): T = {
    val zone = new ZoneImpl
    try f(zone)
    finally zone.close()
  }

  /** Minimalistic zone allocator that uses underlying
   *  system allocator for allocations, and frees all of
   *  the allocations once the zone is closed.
   */
  private class ZoneImpl extends Zone {
    final class Node(val head: Ptr[Byte], val tail: Node)

    private var node: Node = null

    final def alloc(size: CSize): Ptr[Byte] = {
      val ptr = stdlib.malloc(size)
      node = new Node(ptr, node)
      ptr
    }

    final def realloc(ptr: Ptr[Byte], newSize: CSize): Ptr[Byte] =
      throw new UnsupportedOperationException("Zones do not support realloc")

    final def free(ptr: Ptr[Byte]): Unit =
      throw new UnsupportedOperationException("Zones do not support free")

    final def close(): Unit = {
      while (node != null) {
        stdlib.free(node.head)
        node = node.tail
      }
    }
  }
}
