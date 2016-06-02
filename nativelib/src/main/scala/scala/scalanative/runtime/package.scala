package scala.scalanative

import scala.reflect.ClassTag
import native._

package object runtime {
  /** Used as a stub right hand of intrinsified methods. */
  def undefined: Nothing = throw new UndefinedBehaviorError

  /** Allocate memory in gc heap using given info pointer. */
  def alloc(info: Ptr[_], size: CSize): Ptr[_] = {
    val ptr = GC.malloc(size).cast[Ptr[Ptr[_]]]
    !ptr = info
    ptr
  }

  /** 
   * Allocate memory in gc heap using given info pointer.
   *
   * The allocated memory cannot be used to store pointers.
   */
  def allocAtomic(info: Ptr[_], size: CSize): Ptr[_] = {
    val ptr = GC.malloc_atomic(size).cast[Ptr[Ptr[_]]]
    // initialize to 0
    clib_string.memset(ptr, 0, size)
    !ptr = info
    ptr
  }

  /** Returns info pointer for given type. */
  def infoof[T](implicit ct: ClassTag[T]): Ptr[_] = undefined

  /** Initialize runtime with given arguments and return the
    * rest as Java-style array.
    */
  def init(argc: Int, argv: Ptr[Ptr[Byte]]): ObjectArray = {
    GC.init()
    null
  }
}
