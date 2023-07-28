package scala.scalanative
package unsafe

import scala.annotation.implicitNotFound
import scalanative.runtime.{MemoryPool, MemoryPoolZone}

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
    val zone = open()
    try f(zone)
    finally zone.close()
  }

  /** Create a new zone allocator. Use Zone#close to free allocations. */
  final def open(): Zone = MemoryPoolZone.open(MemoryPool.defaultMemoryPool)
}
