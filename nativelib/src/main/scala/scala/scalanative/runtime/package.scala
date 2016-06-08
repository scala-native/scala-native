package scala.scalanative

import scala.reflect.ClassTag
import native._
import runtime.Intrinsics._

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
    /* workaround for issue 149 */
    def allocAtomicAndInit(size: CSize): Ptr[Byte] = {
      val ptr = GC.malloc_atomic(size).cast[Ptr[Byte]]
      // initialize to 0
      `llvm.memset.p0i8.i64`(ptr, 0, size, 1, false) 
      ptr
    }

    val ptr = allocAtomicAndInit(size).cast[Ptr[Ptr[_]]]
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
