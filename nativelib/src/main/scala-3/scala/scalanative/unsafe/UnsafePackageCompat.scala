package scala.scalanative.unsafe

import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics.{castRawSizeToInt as toInt}

private[scalanative] trait UnsafePackageCompat {
  private[scalanative] given reflect.ClassTag[Array[?]] =
    reflect.classTag[Array[AnyRef]].asInstanceOf[reflect.ClassTag[Array[?]]]

  /** The Scala equivalent of C 'alignmentof', but always returns 32-bit integer
   */
  inline def alignmentOf[T]: Int = toInt(Intrinsics.alignmentOf[T])

  /** The C 'alignmentof' operator. */
  inline def alignmentof[T]: CSize = fromRawUSize(Intrinsics.alignmentOf[T])

  /** The Scala equivalent of C 'ssizeof', but always returns 32-bit integer */
  inline def sizeOf[T]: Int = toInt(Intrinsics.sizeOf[T])

  /** The C 'sizeof' operator. */
  inline def sizeof[T]: CSize = fromRawUSize(Intrinsics.sizeOf[T])

  /** The C 'sizeof' operator. */
  inline def ssizeof[T]: CSSize = fromRawSize(Intrinsics.sizeOf[T])

  /** Heap allocate and zero-initialize value using current implicit allocator.
   */
  inline def alloc[T]()(using zone: Zone): Ptr[T] = {
    val size = sizeof[T]
    val ptr = zone.alloc(size)
    libc.memset(ptr, 0, size)
    ptr.asInstanceOf[Ptr[T]]
  }

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  inline def alloc[T](inline n: CSize)(using zone: Zone): Ptr[T] = {
    val size = sizeof[T] * n
    val ptr = zone.alloc(size)
    libc.memset(ptr, 0, size)
    ptr.asInstanceOf[Ptr[T]]
  }

  /** Stack allocate and zero-initialize value of given type */
  inline def stackalloc[T](): Ptr[T] = {
    val size = Intrinsics.sizeOf[T]
    val ptr = Intrinsics.stackalloc(size)
    libc.memset(ptr, 0, size)
    fromRawPtr[T](ptr)
  }

  /** Stack allocate and zero-initialize n values of given type */
  inline def stackalloc[T](inline n: CSize): Ptr[T] = {
    val size = sizeof[T] * n
    val ptr = fromRawPtr[T](Intrinsics.stackalloc(size))
    libc.memset(ptr, 0, size)
    ptr
  }
}
