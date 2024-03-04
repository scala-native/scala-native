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
import scalanative.annotation.alwaysinline
import scala.scalanative.memory.SafeZone
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

sealed abstract class Array[T]
    extends java.io.Serializable
    with java.lang.Cloneable {

  /** Number of elements of the array. */
  @inline def length: Int = {
    val rawptr = castObjectToRawPtr(this)
    val lenptr = elemRawPtr(rawptr, MemoryLayout.Array.LengthOffset)
    loadInt(lenptr)
  }

  /** Size between elements in the array. */
  def stride: Int

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
    } else if (len == 0) {
      ()
    } else if (fromPos < 0 || fromPos + len > from.length) {
      throwOutOfBounds(fromPos, from.length)
    } else if (toPos < 0 || toPos + len > to.length) {
      throwOutOfBounds(toPos, to.length)
    } else {
      val fromPtr = from.atRawUnsafe(fromPos)
      val toPtr   = to.atRawUnsafe(toPos)
      val size    = to.stride * len
      ffi.memmove(toPtr, fromPtr, castIntToRawSizeUnsigned(size))
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
      throwOutOfBounds(leftPos, left.length)
    } else if (rightPos < 0 || rightPos + len > right.length) {
      throwOutOfBounds(rightPos, right.length)
    } else if (len == 0) {
      0
    } else {
      val leftPtr  = left.atRaw(leftPos)
      val rightPtr = right.atRaw(rightPos)
      ffi.memcmp(leftPtr, rightPtr, castIntToRawSizeUnsigned(len * left.stride))
    }
  }
}


final class BooleanArray private () extends Array[Boolean] {

  @inline def stride: Int = 1

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 1 * i)
  }

  @inline def apply(i: Int): Boolean = loadBoolean(atRaw(i))

  @inline def update(i: Int, value: Boolean): Unit = storeBoolean(atRaw(i), value)

  @inline override def clone(): BooleanArray = {
    val arrcls  = classOf[BooleanArray]
    val arr     = GC.alloc_array(arrcls, length, 1)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(1 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[BooleanArray]
    array
  }
}

object BooleanArray {

  @inline def alloc(length: Int): BooleanArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BooleanArray]
    val arr = GC.alloc_array(arrcls, length, 1) 
    val array = castRawPtrToObject(arr).asInstanceOf[BooleanArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): BooleanArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BooleanArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[BooleanArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 1)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): BooleanArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(1 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class CharArray private () extends Array[Char] {

  @inline def stride: Int = 2

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 2 * i)
  }

  @inline def apply(i: Int): Char = loadChar(atRaw(i))

  @inline def update(i: Int, value: Char): Unit = storeChar(atRaw(i), value)

  @inline override def clone(): CharArray = {
    val arrcls  = classOf[CharArray]
    val arr     = GC.alloc_array(arrcls, length, 2)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(2 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[CharArray]
    array
  }
}

object CharArray {

  @inline def alloc(length: Int): CharArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[CharArray]
    val arr = GC.alloc_array(arrcls, length, 2) 
    val array = castRawPtrToObject(arr).asInstanceOf[CharArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): CharArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[CharArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 2 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[CharArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 2)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): CharArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(2 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class ByteArray private () extends Array[Byte] {

  @inline def stride: Int = 1

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 1 * i)
  }

  @inline def apply(i: Int): Byte = loadByte(atRaw(i))

  @inline def update(i: Int, value: Byte): Unit = storeByte(atRaw(i), value)

  @inline override def clone(): ByteArray = {
    val arrcls  = classOf[ByteArray]
    val arr     = GC.alloc_array(arrcls, length, 1)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(1 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[ByteArray]
    array
  }
}

object ByteArray {

  @inline def alloc(length: Int): ByteArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ByteArray]
    val arr = GC.alloc_array(arrcls, length, 1) 
    val array = castRawPtrToObject(arr).asInstanceOf[ByteArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): ByteArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ByteArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[ByteArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 1)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): ByteArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(1 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class ShortArray private () extends Array[Short] {

  @inline def stride: Int = 2

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 2 * i)
  }

  @inline def apply(i: Int): Short = loadShort(atRaw(i))

  @inline def update(i: Int, value: Short): Unit = storeShort(atRaw(i), value)

  @inline override def clone(): ShortArray = {
    val arrcls  = classOf[ShortArray]
    val arr     = GC.alloc_array(arrcls, length, 2)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(2 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[ShortArray]
    array
  }
}

object ShortArray {

  @inline def alloc(length: Int): ShortArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ShortArray]
    val arr = GC.alloc_array(arrcls, length, 2) 
    val array = castRawPtrToObject(arr).asInstanceOf[ShortArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): ShortArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ShortArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 2 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[ShortArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 2)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): ShortArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(2 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class IntArray private () extends Array[Int] {

  @inline def stride: Int = 4

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 4 * i)
  }

  @inline def apply(i: Int): Int = loadInt(atRaw(i))

  @inline def update(i: Int, value: Int): Unit = storeInt(atRaw(i), value)

  @inline override def clone(): IntArray = {
    val arrcls  = classOf[IntArray]
    val arr     = GC.alloc_array(arrcls, length, 4)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(4 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[IntArray]
    array
  }
}

object IntArray {

  @inline def alloc(length: Int): IntArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[IntArray]
    val arr = GC.alloc_array(arrcls, length, 4) 
    val array = castRawPtrToObject(arr).asInstanceOf[IntArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): IntArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[IntArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 4 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[IntArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 4)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): IntArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(4 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class LongArray private () extends Array[Long] {

  @inline def stride: Int = 8

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 8 * i)
  }

  @inline def apply(i: Int): Long = loadLong(atRaw(i))

  @inline def update(i: Int, value: Long): Unit = storeLong(atRaw(i), value)

  @inline override def clone(): LongArray = {
    val arrcls  = classOf[LongArray]
    val arr     = GC.alloc_array(arrcls, length, 8)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(8 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[LongArray]
    array
  }
}

object LongArray {

  @inline def alloc(length: Int): LongArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[LongArray]
    val arr = GC.alloc_array(arrcls, length, 8) 
    val array = castRawPtrToObject(arr).asInstanceOf[LongArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): LongArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[LongArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 8 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[LongArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 8)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): LongArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(8 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class FloatArray private () extends Array[Float] {

  @inline def stride: Int = 4

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 4 * i)
  }

  @inline def apply(i: Int): Float = loadFloat(atRaw(i))

  @inline def update(i: Int, value: Float): Unit = storeFloat(atRaw(i), value)

  @inline override def clone(): FloatArray = {
    val arrcls  = classOf[FloatArray]
    val arr     = GC.alloc_array(arrcls, length, 4)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(4 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[FloatArray]
    array
  }
}

object FloatArray {

  @inline def alloc(length: Int): FloatArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[FloatArray]
    val arr = GC.alloc_array(arrcls, length, 4) 
    val array = castRawPtrToObject(arr).asInstanceOf[FloatArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): FloatArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[FloatArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 4 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[FloatArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 4)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): FloatArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(4 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class DoubleArray private () extends Array[Double] {

  @inline def stride: Int = 8

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 8 * i)
  }

  @inline def apply(i: Int): Double = loadDouble(atRaw(i))

  @inline def update(i: Int, value: Double): Unit = storeDouble(atRaw(i), value)

  @inline override def clone(): DoubleArray = {
    val arrcls  = classOf[DoubleArray]
    val arr     = GC.alloc_array(arrcls, length, 8)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(8 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[DoubleArray]
    array
  }
}

object DoubleArray {

  @inline def alloc(length: Int): DoubleArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[DoubleArray]
    val arr = GC.alloc_array(arrcls, length, 8) 
    val array = castRawPtrToObject(arr).asInstanceOf[DoubleArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): DoubleArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[DoubleArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 8 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[DoubleArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), 8)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): DoubleArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(8 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

final class ObjectArray private () extends Array[Object] {

  @inline def stride: Int = castRawSizeToInt(Intrinsics.sizeOf[RawPtr])

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + castRawSizeToInt(Intrinsics.sizeOf[RawPtr]) * i)
  }

  @inline def apply(i: Int): Object = loadObject(atRaw(i))

  @inline def update(i: Int, value: Object): Unit = storeObject(atRaw(i), value)

  @inline override def clone(): ObjectArray = {
    val arrcls  = classOf[ObjectArray]
    val arr     = GC.alloc_array(arrcls, length, castRawSizeToInt(Intrinsics.sizeOf[RawPtr]))
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(castRawSizeToInt(Intrinsics.sizeOf[RawPtr]) * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[ObjectArray]
    array
  }
}

object ObjectArray {

  @inline def alloc(length: Int): ObjectArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ObjectArray]
    val arr = GC.alloc_array(arrcls, length, castRawSizeToInt(Intrinsics.sizeOf[RawPtr])) 
    val array = castRawPtrToObject(arr).asInstanceOf[ObjectArray]
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): ObjectArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[ObjectArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + castRawSizeToInt(Intrinsics.sizeOf[RawPtr]) * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[ObjectArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    storeInt(elemRawPtr(arr, MemoryLayout.Array.StrideOffset), castRawSizeToInt(Intrinsics.sizeOf[RawPtr]))
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): ObjectArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(castRawSizeToInt(Intrinsics.sizeOf[RawPtr]) * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}

/** Implementation of Array[Byte] potentially containing pointers to other GC allocated objects. Unlike [[ByteArray]] it is conservatively scanned. When running with Immix or Commix GC allows to set [[scannableLimit]] of maximal number of bytes to scan.  */
final class BlobArray private () extends Array[Byte] {
  @alwaysinline private def limitPtr: RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.StrideOffset)
  }
  /** Maximal number of elements to scan by the garbage collector (best effort) */
  @inline def scannableLimit: Int = -loadInt(limitPtr)
  /** Set maximal number of elements to scan by the garbage collector (best effort), new limit needs to smaller or equal to length of array  */
  @inline def scannableLimit_=(v: Int): Unit = {
    if(v < 0 || v > length) throwOutOfBounds(v, length)
    else setScannableLimitUnsafe(v)
  }
  /** Set maximal number of elements to scan by the garbage collector (best effort), new limit needs to smaller or equal to length of array. This version of scannableLimit setter is not checking the bound of argument. */
  @inline def setScannableLimitUnsafe(v: Int): Unit = storeInt(limitPtr, -v)

  /** Set maximal number of elements to scan by the garbage collector (best effort), new limit needs to smaller or equal to length of array  */
  @inline def withScannableLimit(v: Int): this.type = {
    scannableLimit = v
    this
  }

  @inline def stride: Int = 1

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwOutOfBounds(i, length)
    } else {
      atRawUnsafe(i)
    }

  @inline def atRawUnsafe(i: Int): RawPtr = {
    val rawptr = castObjectToRawPtr(this)
    elemRawPtr(rawptr, MemoryLayout.Array.ValuesOffset + 1 * i)
  }

  @inline def apply(i: Int): Byte = loadByte(atRaw(i))

  @inline def update(i: Int, value: Byte): Unit = storeByte(atRaw(i), value)

  @inline override def clone(): BlobArray = {
    val arrcls  = classOf[BlobArray]
    val arr     = GC.alloc_array(arrcls, length, 1)
    val src     = castObjectToRawPtr(this)
    ffi.memcpy(
      elemRawPtr(arr, MemoryLayout.Array.ValuesOffset),
      elemRawPtr(src, MemoryLayout.Array.ValuesOffset),
      castIntToRawSizeUnsigned(1 * length)
    )
    val array = castRawPtrToObject(arr).asInstanceOf[BlobArray]
    array.setScannableLimitUnsafe(this.scannableLimit)
    array
  }
}

object BlobArray {

  @inline def alloc(length: Int): BlobArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BlobArray]
    val arr = GC.alloc_array(arrcls, length, 1) 
    val array = castRawPtrToObject(arr).asInstanceOf[BlobArray]
    array.setScannableLimitUnsafe(length)
    array
  }

  @inline def alloc(length: Int, zone: SafeZone): BlobArray = {
    if (length < 0) {
      throw new NegativeArraySizeException
    }
    val arrcls  = classOf[BlobArray]
    val arrsize = castIntToRawSizeUnsigned(MemoryLayout.Array.ValuesOffset + 1 * length)
    val arr = zone.allocImpl(castObjectToRawPtr(arrcls), arrsize)
    val array = castRawPtrToObject(arr).asInstanceOf[BlobArray]
    storeInt(elemRawPtr(arr, MemoryLayout.Array.LengthOffset), length)
    array.setScannableLimitUnsafe(length)
    array
  }

  @inline def snapshot(length: Int, data: RawPtr): BlobArray = {
    val arr  = alloc(length)
    if(length > 0) {
      val dst  = arr.atRawUnsafe(0)
      val src  = data
      val size = castIntToRawSizeUnsigned(1 * length)
      ffi.memcpy(dst, src, size)
    }
    arr
  }
}
