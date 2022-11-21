package scala.scalanative.safe

import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.unsafe.Tag

@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Return an object of type T allocated in zone. T can be primitive types,
   *  struct, class.
   *
   *  ```
   *  Expected usage:
   *  	1. T is primitive type, e.g. Int
   *  		val x: Int = sz.alloc[Int](10)
   *  	2. T is struct, e.g. @struct class A(v0: Int, v1: Long) {}
   *  		val x: A = sz.alloc[A](0, 0L)
   *  	3. T is class. e.g. class A(v0: Int, v1: Long) {}
   *  		val x: A = sz.alloc[A](0, 0L)
   *  		Note that Array[_] is a special class.
   *  		val x: Array[Int] = sz.alloc[Array[Int]](10) // x.length = 10
   *  ```
   *  Currently it's a mock interface which doesn't accept constructor
   *  parameters.
   */
  // def alloc[T]()(using tag: Tag[T]): T

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean = !isClosed

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean
}

object SafeZone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: SafeZone => T): T = {
    val safeZone = open()
    try f(safeZone)
    finally safeZone.close()
  }

  final def open(): SafeZone =
    MemoryPoolSafeZone.open(MemoryPool.defaultMemoryPoolHandle)
}
