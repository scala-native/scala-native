package scala.scalanative.unsafe

import scala.scalanative.runtime.{Intrinsics, fromRawPtr, toRawPtr, libc}
import scala.scalanative.unsigned._

private[scalanative] trait UnsafePackageCompat {
  private[scalanative] given reflect.ClassTag[Array[?]] =
    reflect.classTag[Array[AnyRef]].asInstanceOf[reflect.ClassTag[Array[?]]]

  /** Heap allocate and zero-initialize a value using current implicit
   *  allocator.
   */
  @deprecated(
    "In Scala 3 alloc[T](n) can be confused with alloc[T].apply(n) leading to runtime erros, use alloc[T]() instead",
    since = "0.5.0"
  )
  inline def alloc[T](using tag: Tag[T], zone: Zone): Ptr[T] = {
    alloc[T](1.toULong)
  }

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  inline def alloc[T](
      inline n: CSize = 1.toULong
  )(using tag: Tag[T], zone: Zone): Ptr[T] = {
    val size = sizeof[T] * n.toULong
    val ptr = zone.alloc(size)
    val rawPtr = toRawPtr(ptr)
    libc.memset(rawPtr, 0, size)
    ptr.asInstanceOf[Ptr[T]]
  }

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator. This method takes argument of type `CSSize` for easier interop,
   *  but it' always converted into `CSize`
   */
  @deprecated(
    "alloc with signed type is deprecated, convert size to unsigned value",
    since = "0.4.0"
  )
  inline def alloc[T](inline n: CSSize)(using Tag[T], Zone): Ptr[T] =
    alloc[T](n.toUInt)

  @deprecated(
    "In Scala 3 stackalloc[T](n) can be confused with stackalloc[T].apply(n) leading to runtime erros, use stackalloc[T]() instead",
    since = "0.5.0"
  )
  inline def stackalloc[T](implicit tag: Tag[T]): Ptr[T] =
    stackalloc[T](1.toULong)

  /** Stack allocate n values of given type */
  inline def stackalloc[T](
      inline n: CSize = 1.toULong
  )(using Tag[T]): Ptr[T] = {
    val size = sizeof[T] * n.toULong
    val rawPtr = Intrinsics.stackalloc(size)
    libc.memset(rawPtr, 0, size)
    fromRawPtr[T](rawPtr)
  }

  /** Stack allocate n values of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized. This method takes
   *  argument of type `CSSize` for easier interop, but it's always converted
   *  into `CSize`
   */
  @deprecated(
    "alloc with signed type is deprecated, convert size to unsigned value",
    since = "0.4.0"
  )
  inline def stackalloc[T](inline n: CSSize)(using Tag[T]): Ptr[T] =
    stackalloc[T](n.toULong)
}
