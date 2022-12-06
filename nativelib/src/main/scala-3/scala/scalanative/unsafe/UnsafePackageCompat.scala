package scala.scalanative.unsafe

import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr, libc}
import scala.scalanative.unsigned._

private[scalanative] trait UnsafePackageCompat {
  private[scalanative] given reflect.ClassTag[Array[?]] =
    reflect.classTag[Array[AnyRef]].asInstanceOf[reflect.ClassTag[Array[?]]]

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  inline def alloc[T](
      inline n: CSize = 1.toUSize
  )(using tag: Tag[T], zone: Zone): Ptr[T] = {
    val size = sizeof[T] * n
    val ptr = zone.alloc(size)
    val rawPtr = toRawPtr(ptr)
    libc.memset(rawPtr, 0, size)
    ptr.asInstanceOf[Ptr[T]]
  }

  /** Stack allocate and zero-initialize n values of given type */
  inline def stackalloc[T](
      inline n: CSize = 1.toUSize
  )(using Tag[T]): Ptr[T] = {
    val size = sizeof[T] * n
    val rawPtr = Intrinsics.stackalloc(size)
    libc.memset(rawPtr, 0, size)
    fromRawPtr[T](rawPtr)
  }
}
