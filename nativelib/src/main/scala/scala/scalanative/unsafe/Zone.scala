package scala.scalanative
package unsafe

import scala.annotation.implicitNotFound
import scala.scalanative.runtime.Intrinsics.{
  unsignedOf,
  castIntToRawSizeUnsigned
}
import scala.scalanative.runtime.{MemoryPool, MemoryPoolZone}
import scala.scalanative.unsigned._

/** Zone allocator which manages memory allocations. */
@implicitNotFound("Given method requires an implicit zone.")
trait Zone {

  /** Allocates memory of given size. */
  def alloc(size: CSize): Ptr[Byte]

  /** Allocates memory of given size. */
  def alloc(size: Int): Ptr[Byte] =
    alloc(unsignedOf(castIntToRawSizeUnsigned(size)))

    /** Allocates memory of given size. */
  def alloc(size: UInt): Ptr[Byte] =
    alloc(size.toUSize)

  /** Allocates memory of given size. */
  def alloc(size: ULong): Ptr[Byte] =
    alloc(size.toUSize)

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean = !isClosed

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean

}

object Zone extends ZoneCompanionScalaVersionSpecific {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def acquire[T](f: Zone => T): T = {
    val zone = open()
    try f(zone)
    finally zone.close()
  }

  /** Create a new zone allocator. Use Zone#close to free allocations. */
  final def open(): Zone = MemoryPoolZone.open(MemoryPool.defaultMemoryPool)
}
