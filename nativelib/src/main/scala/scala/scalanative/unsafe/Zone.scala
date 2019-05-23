package scala.scalanative
package unsafe

import scala.annotation.implicitNotFound
import scalanative.runtime.{libc, RawPtr, fromRawPtr}

/** Zone allocator which manages memory allocations. */
@implicitNotFound("Given method requires an implicit zone.")
trait Zone {

  /** Allocates memory of given size. */
  def alloc(size: CSize): Ptr[Byte]

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean = !isClosed

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean

}

object Zone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: Zone => T): T = {
    val zone = Zone.open()
    try f(zone)
    finally zone.close()
  }

  /** Create a new zone allocator. Use Zone#close to free allocations. */
  final def open(): Zone = new ZoneImpl

  /** Minimalistic zone allocator that uses underlying
   *  system allocator for allocations, and frees all of
   *  the allocations once the zone is closed.
   */
  private class ZoneImpl extends Zone {
    final class Node(val head: RawPtr, val tail: Node)

    private var node: Node      = null
    private var closed: Boolean = false

    final override def isClosed: Boolean = closed

    final def alloc(size: CSize): Ptr[Byte] = {
      if (isClosed) {
        throw new IllegalStateException("zone allocator is closed")
      }
      val rawptr = libc.malloc(size)
      node = new Node(rawptr, node)
      fromRawPtr[Byte](rawptr)
    }

    final override def close(): Unit = {
      while (node != null) {
        libc.free(node.head)
        node = node.tail
      }
      closed = true
    }
  }
}
