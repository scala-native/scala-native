package scala.scalanative.unsafe

import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics.{castRawSizeToInt as toInt, *}
import scala.compiletime.*

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
  inline def alloc[T](inline n: CSize)(using zone: Zone): Ptr[T] =
    alloc[T](toRawSize(n))

  /** Heap allocate and zero-initialize n values using current implicit
   *  allocator.
   */
  inline def alloc[T](inline n: Int)(using zone: Zone): Ptr[T] =
    alloc[T](validateSize(n))

  private[UnsafePackageCompat] inline def alloc[T](
      inline elements: RawSize
  )(using zone: Zone): Ptr[T] = {
    val elemSize = Intrinsics.sizeOf[T]
    val rawSize = castIntToRawSizeUnsigned(toInt(elemSize) * toInt(elements))
    val size = unsignedOf(rawSize)
    val ptr = zone.alloc(size)
    libc.memset(ptr.rawptr, 0, rawSize)
    ptr.asInstanceOf[Ptr[T]]
  }

  /** Stack allocate and zero-initialize value of given type */
  inline def stackalloc[T](): Ptr[T] = {
    val ptr = Intrinsics.stackalloc[T]()
    fromRawPtr[T](ptr)
  }

  /** Stack allocate and zero-initialize n values of given type */
  inline def stackalloc[T](inline n: CSize): Ptr[T] =
    stackalloc[T](toRawSize(n))

  /** Stack allocate and zero-initialize n values of given type */
  inline def stackalloc[T](inline n: Int): Ptr[T] =
    stackalloc[T](validateSize(n))

  private[UnsafePackageCompat] inline def stackalloc[T](
      inline size: RawSize
  ): Ptr[T] = {
    val ptr = Intrinsics.stackalloc[T](size)
    fromRawPtr[T](ptr)
  }

  /** Scala Native unsafe extensions to the standard Byte. */
  extension (inline value: Byte) {
    inline def toSize: Size = Size.valueOf(castIntToRawSize(value))
  }

  /** Scala Native unsafe extensions to the standard Short. */
  extension (inline value: Short) {
    inline def toSize: Size = Size.valueOf(castIntToRawSize(value))
  }

  /** Scala Native unsafe extensions to the standard Int. */
  extension (inline value: Int) {
    inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castIntToRawPtr(value))
    inline def toSize: Size = Size.valueOf(castIntToRawSize(value))
  }

  /** Scala Native unsafe extensions to the standard Long. */
  extension (inline value: Long) {
    inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castLongToRawPtr(value))
    inline def toSize: Size = Size.valueOf(castLongToRawSize(value))
  }

  // Use macro instead of constValueOpt which would allocate Option instance
  private[UnsafePackageCompat] inline def validateSize(
      inline size: Int
  ): RawSize = ${
    UnsafePackageCompat.validatedSize('size)
  }

}

private object UnsafePackageCompat {
  import scala.quoted.*
  def validatedSize(size: Expr[Int])(using Quotes): Expr[RawSize] = {
    import quotes.*
    import quotes.reflect.*
    val validatedSize = size.asTerm match {
      case lit @ Literal(IntConstant(n)) =>
        if n == 0 then
          report.errorAndAbort("Allocation of size 0 is fruitless", size)
        else if n < 0 then
          report.errorAndAbort("Cannot allocate memory of negative size", size)
        else size
      case _ =>
        '{
          if ($size < 0)
            throw new IllegalArgumentException(
              "Cannot allocate memory of negative size"
            )
          else $size
        }
    }
    '{ Intrinsics.castIntToRawSizeUnsigned($validatedSize) }
  }
}
