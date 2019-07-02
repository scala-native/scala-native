package scala.scalanative
package runtime

import scala.runtime.BoxedUnit
import scalanative.unsafe._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.LLVMIntrinsics._

sealed abstract class Array[T]
    extends java.io.Serializable
    with java.lang.Cloneable {

  /** Number of elements of the array. */
  @inline def length: Int = {
    val rawptr = castObjectToRawPtr(this)
    val lenptr = elemRawPtr(rawptr, castIntToRawWord(8))
    loadInt(lenptr)
  }

  /** Size between elements in the array. */
  def stride: CSize

  /** Pointer to the element. */
  @inline def at(i: Int): Ptr[T] = fromRawPtr[T](atRaw(i))

  /** Raw pointer to the element. */
  def atRaw(i: Int): RawPtr

  /** Loads element at i, throws IndexOutOfBoundsException. */
  def apply(i: Int): T

  /** Stores value to element i, throws IndexOutOfBoundsException. */
  def update(i: Int, value: T): Unit

  /** Create a shallow copy of given array. */
  override def clone(): Array[T] = ??? // overriden in concrete classes
}

object Array {
  def copy(from: AnyRef,
           fromPos: Int,
           to: AnyRef,
           toPos: Int,
           len: Int): Unit = {
    if (from == null || to == null) {
      throw new NullPointerException()
    } else if (!from.isInstanceOf[Array[_]]) {
      throw new IllegalArgumentException("from argument must be an array")
    } else if (!to.isInstanceOf[Array[_]]) {
      throw new IllegalArgumentException("to argument must be an array")
    } else {
      copy(from.asInstanceOf[Array[_]],
           fromPos,
           to.asInstanceOf[Array[_]],
           toPos,
           len)
    }
  }

  def copy(from: Array[_],
           fromPos: Int,
           to: Array[_],
           toPos: Int,
           len: Int): Unit = {
    if (from == null || to == null) {
      throw new NullPointerException()
    } else if (getRawType(from) != getRawType(to)) {
      throw new ArrayStoreException("Invalid array copy.")
    } else if (len < 0) {
      throw new IndexOutOfBoundsException("length is negative")
    } else if (fromPos < 0 || fromPos + len > from.length) {
      throw new IndexOutOfBoundsException(fromPos.toString)
    } else if (toPos < 0 || toPos + len > to.length) {
      throw new IndexOutOfBoundsException(toPos.toString)
    } else if (len == 0) {
      ()
    } else {
      val fromPtr = from.atRaw(fromPos)
      val toPtr   = to.atRaw(toPos)
      val size    = to.stride * len
      libc.memmove(toPtr, fromPtr, size)
    }
  }

  def compare(left: AnyRef,
              leftPos: Int,
              right: AnyRef,
              rightPos: Int,
              len: Int): Int = {
    if (left == null || right == null) {
      throw new NullPointerException()
    } else if (!left.isInstanceOf[Array[_]]) {
      throw new IllegalArgumentException("left argument must be an array")
    } else if (!right.isInstanceOf[Array[_]]) {
      throw new IllegalArgumentException("right argument must be an array")
    } else {
      compare(left.asInstanceOf[Array[_]],
              leftPos,
              right.asInstanceOf[Array[_]],
              rightPos,
              len)
    }
  }

  def compare(left: Array[_],
              leftPos: Int,
              right: Array[_],
              rightPos: Int,
              len: Int): Int = {
    if (left == null || right == null) {
      throw new NullPointerException()
    } else if (getRawType(left) != getRawType(right)) {
      throw new ArrayStoreException("Invalid array copy.")
    } else if (len < 0) {
      throw new IndexOutOfBoundsException("length is negative")
    } else if (leftPos < 0 || leftPos + len > left.length) {
      throw new IndexOutOfBoundsException(leftPos.toString)
    } else if (rightPos < 0 || rightPos + len > right.length) {
      throw new IndexOutOfBoundsException(rightPos.toString)
    } else if (len == 0) {
      0
    } else {
      val leftPtr  = left.atRaw(leftPos)
      val rightPtr = right.atRaw(rightPos)
      libc.memcmp(leftPtr, rightPtr, len * left.stride)
    }
  }
}

final class CharArray private () extends Array[Char] {
  import Array._

  @inline def stride: CSize =
    2

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 2 * i))
    }

  @inline def apply(i: Int): Char =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 2 * i))
      loadChar(ith)
    }

  @inline def update(i: Int, value: Char): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 2 * i))
      storeChar(ith, value)
    }

  @inline override def clone(): CharArray = {
    val arrty   = toRawType(classOf[CharArray])
    val arrsize = 16 + 2 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }
}

object CharArray {
  import Array._

  @inline def alloc(length: Int): CharArray = {
    val arrty   = toRawType(classOf[CharArray])
    val arrsize = 16 + 2 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 2.toInt)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): CharArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 2 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class ObjectArray private () extends Array[Object] {
  import Array._

  @inline def stride: CSize =
    8

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
    }

  @inline def apply(i: Int): Object =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      loadObject(ith)
    }

  @inline def update(i: Int, value: Object): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      storeObject(ith, value)
    }

  @inline override def clone(): ObjectArray = {
    val arrty   = toRawType(classOf[ObjectArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }
}

object ObjectArray {
  import Array._

  @inline def alloc(length: Int): ObjectArray = {
    val arrty   = toRawType(classOf[ObjectArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 8.toInt)
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ObjectArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 8 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class BoxedUnitArray private () extends Array[BoxedUnit] {
  import Array._

  @inline def stride: CSize =
    8

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
    }

  @inline def apply(i: Int): BoxedUnit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      loadObject(ith).asInstanceOf[BoxedUnit]
    }

  @inline def update(i: Int, value: BoxedUnit): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      storeObject(ith, value.asInstanceOf[Object])
    }

  @inline override def clone(): BoxedUnitArray = {
    val arrty   = toRawType(classOf[BoxedUnitArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[BoxedUnitArray]
  }
}

object BoxedUnitArray {
  import Array._

  @inline def alloc(length: Int): BoxedUnitArray = {
    val arrty   = toRawType(classOf[BoxedUnitArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 8.toInt)
    castRawPtrToObject(arr).asInstanceOf[BoxedUnitArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): BoxedUnitArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 8 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class ShortArray private () extends Array[Short] {
  import Array._

  @inline def stride: CSize =
    2

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 2 * i))
    }

  @inline def apply(i: Int): Short =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 2 * i))
      loadShort(ith)
    }

  @inline def update(i: Int, value: Short): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 2 * i))
      storeShort(ith, value)
    }

  @inline override def clone(): ShortArray = {
    val arrty   = toRawType(classOf[ShortArray])
    val arrsize = 16 + 2 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }
}

object ShortArray {
  import Array._

  @inline def alloc(length: Int): ShortArray = {
    val arrty   = toRawType(classOf[ShortArray])
    val arrsize = 16 + 2 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 2.toInt)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ShortArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 2 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class IntArray private () extends Array[Int] {
  import Array._

  @inline def stride: CSize =
    4

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 4 * i))
    }

  @inline def apply(i: Int): Int =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 4 * i))
      loadInt(ith)
    }

  @inline def update(i: Int, value: Int): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 4 * i))
      storeInt(ith, value)
    }

  @inline override def clone(): IntArray = {
    val arrty   = toRawType(classOf[IntArray])
    val arrsize = 16 + 4 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }
}

object IntArray {
  import Array._

  @inline def alloc(length: Int): IntArray = {
    val arrty   = toRawType(classOf[IntArray])
    val arrsize = 16 + 4 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 4.toInt)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): IntArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 4 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class DoubleArray private () extends Array[Double] {
  import Array._

  @inline def stride: CSize =
    8

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
    }

  @inline def apply(i: Int): Double =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      loadDouble(ith)
    }

  @inline def update(i: Int, value: Double): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      storeDouble(ith, value)
    }

  @inline override def clone(): DoubleArray = {
    val arrty   = toRawType(classOf[DoubleArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }
}

object DoubleArray {
  import Array._

  @inline def alloc(length: Int): DoubleArray = {
    val arrty   = toRawType(classOf[DoubleArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 8.toInt)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): DoubleArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 8 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class ByteArray private () extends Array[Byte] {
  import Array._

  @inline def stride: CSize =
    1

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 1 * i))
    }

  @inline def apply(i: Int): Byte =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 1 * i))
      loadByte(ith)
    }

  @inline def update(i: Int, value: Byte): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 1 * i))
      storeByte(ith, value)
    }

  @inline override def clone(): ByteArray = {
    val arrty   = toRawType(classOf[ByteArray])
    val arrsize = 16 + 1 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }
}

object ByteArray {
  import Array._

  @inline def alloc(length: Int): ByteArray = {
    val arrty   = toRawType(classOf[ByteArray])
    val arrsize = 16 + 1 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 1.toInt)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ByteArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 1 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class FloatArray private () extends Array[Float] {
  import Array._

  @inline def stride: CSize =
    4

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 4 * i))
    }

  @inline def apply(i: Int): Float =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 4 * i))
      loadFloat(ith)
    }

  @inline def update(i: Int, value: Float): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 4 * i))
      storeFloat(ith, value)
    }

  @inline override def clone(): FloatArray = {
    val arrty   = toRawType(classOf[FloatArray])
    val arrsize = 16 + 4 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }
}

object FloatArray {
  import Array._

  @inline def alloc(length: Int): FloatArray = {
    val arrty   = toRawType(classOf[FloatArray])
    val arrsize = 16 + 4 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 4.toInt)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): FloatArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 4 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class LongArray private () extends Array[Long] {
  import Array._

  @inline def stride: CSize =
    8

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
    }

  @inline def apply(i: Int): Long =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      loadLong(ith)
    }

  @inline def update(i: Int, value: Long): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 8 * i))
      storeLong(ith, value)
    }

  @inline override def clone(): LongArray = {
    val arrty   = toRawType(classOf[LongArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }
}

object LongArray {
  import Array._

  @inline def alloc(length: Int): LongArray = {
    val arrty   = toRawType(classOf[LongArray])
    val arrsize = 16 + 8 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 8.toInt)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): LongArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 8 * length
    libc.memcpy(dst, src, size)
    arr
  }
}

final class BooleanArray private () extends Array[Boolean] {
  import Array._

  @inline def stride: CSize =
    1

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, castIntToRawWord(16 + 1 * i))
    }

  @inline def apply(i: Int): Boolean =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 1 * i))
      loadBoolean(ith)
    }

  @inline def update(i: Int, value: Boolean): Unit =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, castIntToRawWord(16 + 1 * i))
      storeBoolean(ith, value)
    }

  @inline override def clone(): BooleanArray = {
    val arrty   = toRawType(classOf[BooleanArray])
    val arrsize = 16 + 1 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }
}

object BooleanArray {
  import Array._

  @inline def alloc(length: Int): BooleanArray = {
    val arrty   = toRawType(classOf[BooleanArray])
    val arrsize = 16 + 1 * length
    val arr     = GC.alloc_atomic(arrty, arrsize)
    storeInt(elemRawPtr(arr, castIntToRawWord(8)), length)
    storeInt(elemRawPtr(arr, castIntToRawWord(12)), 1.toInt)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): BooleanArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = 1 * length
    libc.memcpy(dst, src, size)
    arr
  }
}
