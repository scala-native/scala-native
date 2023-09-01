// format: off

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// To generate this file manually execute the python scripts/gyb.py
// script under the project root. For example, from the project root:
//
//   scripts/gyb.py \
//     nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb \
//     --line-directive '' \
//     -o nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala
//
//  After executing the script, you may want to edit this file to remove
//  personally or build-system specific identifiable information.
//
//  The order elements in the output file depends upon the Python version
//  used to execute the gyb.py. Arrays.scala.gyb has a BEWARE: comment near
//  types.items() which gives details.
//
//  Python >= 3.6 should give a reproducible output order and reduce trivial
//  & annoying git differences.

package scala.scalanative
package runtime

import scalanative.unsafe._
import scalanative.unsigned._
import scala.scalanative.memory.SafeZone
import scalanative.runtime.Intrinsics.{castIntToRawSizeUnsigned => intToUSize, _}

sealed abstract class Array[T]
    extends java.io.Serializable
    with java.lang.Cloneable {

  /** Number of elements of the array. */
  @inline def length: Int = {
    val rawptr = castObjectToRawPtr(this)
    val lenptr = elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.LengthOffset))
    loadInt(lenptr)
  }

  /** Size between elements in the array. */
  def stride: CSize

  /** Pointer to the element. */
  @inline def at(i: Int): Ptr[T] = fromRawPtr[T](atRaw(i))

  /** Pointer to the element without a bounds check. */
  @inline def atUnsafe(i: Int): Ptr[T] = fromRawPtr[T](atRawUnsafe(i))

  /** Raw pointer to the element. */
  def atRaw(i: Int): RawPtr

  /** Raw pointer to the element without a bounds check. */
  def atRawUnsafe(i: Int): RawPtr

  /** Loads element at i, throws ArrayIndexOutOfBoundsException. */
  def apply(i: Int): T

  /** Stores value to element i, throws ArrayIndexOutOfBoundsException. */
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
    } else if (from.getClass != to.getClass) {
      throw new ArrayStoreException("Invalid array copy.")
    } else if (len < 0) {
      throw new ArrayIndexOutOfBoundsException("length is negative")
    } else if (fromPos < 0 || fromPos + len > from.length) {
      throwOutOfBounds(fromPos)
    } else if (toPos < 0 || toPos + len > to.length) {
      throwOutOfBounds(toPos)
    } else if (len == 0) {
      ()
    } else {
      val fromPtr = from.atRaw(fromPos)
      val toPtr   = to.atRaw(toPos)
      val size    = to.stride * len.toUSize
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
    } else if (left.getClass != right.getClass) {
      throw new ArrayStoreException("Invalid array copy.")
    } else if (len < 0) {
      throw new ArrayIndexOutOfBoundsException("length is negative")
    } else if (leftPos < 0 || leftPos + len > left.length) {
      throwOutOfBounds(leftPos)
    } else if (rightPos < 0 || rightPos + len > right.length) {
      throwOutOfBounds(rightPos)
    } else if (len == 0) {
      0
    } else {
      val leftPtr  = left.atRaw(leftPos)
      val rightPtr = right.atRaw(rightPos)
      libc.memcmp(leftPtr, rightPtr, len.toUSize * left.stride)
    }
  }
}


final class BooleanArray private () extends Array[Boolean] {

  @inline def stride: CSize =
    1.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 1 * i))
  }

  @inline def apply(i: Int): Boolean = loadBoolean(atRaw(i))

  @inline def update(i: Int, value: Boolean): Unit = storeBoolean(atRaw(i), value)

  @inline override def clone(): BooleanArray = {
    val arrcls  = classOf[BooleanArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 1 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }
}

object BooleanArray {

  @inline def alloc(length: Int): BooleanArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BooleanArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 1)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): BooleanArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BooleanArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 1)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): BooleanArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(1 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class CharArray private () extends Array[Char] {

  @inline def stride: CSize =
    2.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 2 * i))
  }

  @inline def apply(i: Int): Char = loadChar(atRaw(i))

  @inline def update(i: Int, value: Char): Unit = storeChar(atRaw(i), value)

  @inline override def clone(): CharArray = {
    val arrcls  = classOf[CharArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 2 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }
}

object CharArray {

  @inline def alloc(length: Int): CharArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[CharArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 2 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 2)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): CharArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[CharArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 2 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 2)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): CharArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(2 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class ByteArray private () extends Array[Byte] {

  @inline def stride: CSize =
    1.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 1 * i))
  }

  @inline def apply(i: Int): Byte = loadByte(atRaw(i))

  @inline def update(i: Int, value: Byte): Unit = storeByte(atRaw(i), value)

  @inline override def clone(): ByteArray = {
    val arrcls  = classOf[ByteArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 1 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }
}

object ByteArray {

  @inline def alloc(length: Int): ByteArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ByteArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 1)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): ByteArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ByteArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 1)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ByteArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(1 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class ShortArray private () extends Array[Short] {

  @inline def stride: CSize =
    2.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 2 * i))
  }

  @inline def apply(i: Int): Short = loadShort(atRaw(i))

  @inline def update(i: Int, value: Short): Unit = storeShort(atRaw(i), value)

  @inline override def clone(): ShortArray = {
    val arrcls  = classOf[ShortArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 2 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }
}

object ShortArray {

  @inline def alloc(length: Int): ShortArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ShortArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 2 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 2)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): ShortArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ShortArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 2 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 2)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ShortArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(2 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class IntArray private () extends Array[Int] {

  @inline def stride: CSize =
    4.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 4 * i))
  }

  @inline def apply(i: Int): Int = loadInt(atRaw(i))

  @inline def update(i: Int, value: Int): Unit = storeInt(atRaw(i), value)

  @inline override def clone(): IntArray = {
    val arrcls  = classOf[IntArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 4 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }
}

object IntArray {

  @inline def alloc(length: Int): IntArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[IntArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 4 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 4)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): IntArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[IntArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 4 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 4)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): IntArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(4 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class LongArray private () extends Array[Long] {

  @inline def stride: CSize =
    8.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 8 * i))
  }

  @inline def apply(i: Int): Long = loadLong(atRaw(i))

  @inline def update(i: Int, value: Long): Unit = storeLong(atRaw(i), value)

  @inline override def clone(): LongArray = {
    val arrcls  = classOf[LongArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 8 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }
}

object LongArray {

  @inline def alloc(length: Int): LongArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[LongArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 8 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 8)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): LongArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[LongArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 8 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 8)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): LongArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(8 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class FloatArray private () extends Array[Float] {

  @inline def stride: CSize =
    4.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 4 * i))
  }

  @inline def apply(i: Int): Float = loadFloat(atRaw(i))

  @inline def update(i: Int, value: Float): Unit = storeFloat(atRaw(i), value)

  @inline override def clone(): FloatArray = {
    val arrcls  = classOf[FloatArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 4 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }
}

object FloatArray {

  @inline def alloc(length: Int): FloatArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[FloatArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 4 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 4)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): FloatArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[FloatArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 4 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 4)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): FloatArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(4 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class DoubleArray private () extends Array[Double] {

  @inline def stride: CSize =
    8.toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + 8 * i))
  }

  @inline def apply(i: Int): Double = loadDouble(atRaw(i))

  @inline def update(i: Int, value: Double): Unit = storeDouble(atRaw(i), value)

  @inline override def clone(): DoubleArray = {
    val arrcls  = classOf[DoubleArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + 8 * length))
    val arr     = GC.alloc_atomic(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }
}

object DoubleArray {

  @inline def alloc(length: Int): DoubleArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[DoubleArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 8 * length)
    val arr = GC.alloc_atomic(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 8)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): DoubleArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[DoubleArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + 8 * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), 8)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): DoubleArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(8 * length))
    libc.memcpy(dst, src, size)
    arr
  }
}

final class ObjectArray private () extends Array[Object] {

  @inline def stride: CSize =
    castRawSizeToInt(sizeOfPtr).toUSize

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, intToUSize(MemoryLayout.Array.ValuesOffset + castRawSizeToInt(sizeOfPtr) * i))
  }

  @inline def apply(i: Int): Object = loadObject(atRaw(i))

  @inline def update(i: Int, value: Object): Unit = storeObject(atRaw(i), value)

  @inline override def clone(): ObjectArray = {
    val arrcls  = classOf[ObjectArray]
    val arrsize = USize.valueOf(intToUSize(MemoryLayout.Array.ValuesOffset + castRawSizeToInt(sizeOfPtr) * length))
    val arr     = GC.alloc(arrcls, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }
}

object ObjectArray {

  @inline def alloc(length: Int): ObjectArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ObjectArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + castRawSizeToInt(sizeOfPtr) * length)
    val arr = GC.alloc(arrcls, USize.valueOf(arrsize)) 
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), castRawSizeToInt(sizeOfPtr))
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }

  @inline def alloc(length: Int, zone: SafeZone): ObjectArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ObjectArray]
    val arrsize = intToUSize(MemoryLayout.Array.ValuesOffset + castRawSizeToInt(sizeOfPtr) * length)
    val arr = zone.allocImpl(Intrinsics.castObjectToRawPtr(arrcls), arrsize)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.LengthOffset)), length)
    storeInt(elemRawPtr(arr, intToUSize(MemoryLayout.Array.StrideOffset)), castRawSizeToInt(sizeOfPtr))
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ObjectArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = data
    val size = USize.valueOf(intToUSize(castRawSizeToInt(sizeOfPtr) * length))
    libc.memcpy(dst, src, size)
    arr
  }
}
