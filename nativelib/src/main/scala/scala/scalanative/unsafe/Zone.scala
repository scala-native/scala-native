package scala.scalanative
package unsafe

import scala.annotation.implicitNotFound
import scala.collection.mutable
import scalanative.runtime.{RawPtr, fromRawPtr, libc}

/** A handle that traces references counter to an object inside zone. */
trait ZonedHandle[T] {
  implicit val zone: Zone

  // increase counters each time when object is created
  zone.ref(that = this)

  /** Mark that this object aren't used. */
  def closeHandle(): Unit =
    zone.unref(that = this)
}

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

  /** Increase reference counter for specified Handle.
   * This method returns true when object was added to zone. */
  def ref[T](that: ZonedHandle[T]): Boolean

  /** Decrease reference counter for specified Handle.
   *  This method returns true when object was removed. */
  def unref[T](that: ZonedHandle[T]): Boolean
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

  /** Minimalistic zone allocator that uses underlying system allocator for
   *  allocations, and frees all of the allocations once the zone is closed.
   */
  private class ZoneImpl extends Zone {
    final class Node(val head: RawPtr, val tail: Node)

    private var node: Node = null
    private var closed: Boolean = false

    final override def isClosed: Boolean = closed

    final def alloc(size: CSize): Ptr[Byte] = {
      if (isClosed) {
        throw new IllegalStateException("zone allocator is closed")
      }
      val rawptr = libc.malloc(size)
      if (rawptr == null) {
        throw new OutOfMemoryError(s"Unable to allocate $size bytes")
      }
      node = new Node(rawptr, node)
      fromRawPtr[Byte](rawptr)
    }

    final override def close(): Unit = {
      if (closed) {
        throw new IllegalStateException("zone allocator is closed")
      }
      closed = true
      while (node != null) {
        val head = node.head
        node = node.tail
        libc.free(head)
      }
      references.clear()
    }

    private val references = new mutable.HashMap[ZonedHandle[_], Int]()

    final def ref[T](that: ZonedHandle[T]): Boolean = {
      references.get(that) match {
        case None =>
          references.put(that, 1)
          true
        case Some(counters) =>
          references.put(that, counters + 1)
          false
      }
    }

    final def unref[T](that: ZonedHandle[T]): Boolean =
      references.get(that) match {
        case Some(counters) if counters <= 1 =>
          references.remove(that)
          true
        case Some(counters) =>
          references.put(that, counters - 1)
          false
        case None =>
          true
      }
  }
}
