// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 1)
package scala.scalanative
package runtime

import scalanative.native._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.LLVMIntrinsics._

sealed abstract class Array[T]
    extends java.io.Serializable
    with java.lang.Cloneable {

  /** Number of elements of the array. */
  @inline def length: Int = {
    val rawptr = castObjectToRawPtr(this)
    val lenptr = elemRawPtr(rawptr, sizeof[Ptr[Byte]])
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
  final val HeaderSize = 16

  @noinline def throwIndexOutOfBoundsException(i: Int): Nothing =
    throw new IndexOutOfBoundsException(i.toString)

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
    } else if (getType(from) != getType(to)) {
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
    } else if (getType(left) != getType(right)) {
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

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 138)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class UnitArray private () extends Array[Unit] {
  import Array._

  @inline def stride: CSize =
    sizeof[Unit]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 168)
      loadObject(ith).asInstanceOf[Unit]
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Unit): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 181)
      storeObject(ith, value.asInstanceOf[Object])
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): UnitArray = {
    val arrinfo = typeof[UnitArray]
    val arrsize = HeaderSize + sizeof[Unit] * length
    val arr     = GC.alloc(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[UnitArray]
  }
}

object UnitArray {
  import Array._

  @inline def alloc(length: Int): UnitArray = {
    val arrinfo = typeof[UnitArray]
    val arrsize = HeaderSize + sizeof[Unit] * length
    val arr     = GC.alloc(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Unit].toInt)
    castRawPtrToObject(arr).asInstanceOf[UnitArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): UnitArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Unit] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class BooleanArray private () extends Array[Boolean] {
  import Array._

  @inline def stride: CSize =
    sizeof[Boolean]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Boolean =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadBoolean(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Boolean): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeBoolean(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): BooleanArray = {
    val arrinfo = typeof[BooleanArray]
    val arrsize = HeaderSize + sizeof[Boolean] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }
}

object BooleanArray {
  import Array._

  @inline def alloc(length: Int): BooleanArray = {
    val arrinfo = typeof[BooleanArray]
    val arrsize = HeaderSize + sizeof[Boolean] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Boolean].toInt)
    castRawPtrToObject(arr).asInstanceOf[BooleanArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): BooleanArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Boolean] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class CharArray private () extends Array[Char] {
  import Array._

  @inline def stride: CSize =
    sizeof[Char]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Char =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadChar(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Char): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeChar(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): CharArray = {
    val arrinfo = typeof[CharArray]
    val arrsize = HeaderSize + sizeof[Char] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }
}

object CharArray {
  import Array._

  @inline def alloc(length: Int): CharArray = {
    val arrinfo = typeof[CharArray]
    val arrsize = HeaderSize + sizeof[Char] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Char].toInt)
    castRawPtrToObject(arr).asInstanceOf[CharArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): CharArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Char] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class ByteArray private () extends Array[Byte] {
  import Array._

  @inline def stride: CSize =
    sizeof[Byte]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Byte =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadByte(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Byte): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeByte(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): ByteArray = {
    val arrinfo = typeof[ByteArray]
    val arrsize = HeaderSize + sizeof[Byte] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }
}

object ByteArray {
  import Array._

  @inline def alloc(length: Int): ByteArray = {
    val arrinfo = typeof[ByteArray]
    val arrsize = HeaderSize + sizeof[Byte] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Byte].toInt)
    castRawPtrToObject(arr).asInstanceOf[ByteArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ByteArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Byte] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class ShortArray private () extends Array[Short] {
  import Array._

  @inline def stride: CSize =
    sizeof[Short]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Short =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadShort(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Short): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeShort(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): ShortArray = {
    val arrinfo = typeof[ShortArray]
    val arrsize = HeaderSize + sizeof[Short] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }
}

object ShortArray {
  import Array._

  @inline def alloc(length: Int): ShortArray = {
    val arrinfo = typeof[ShortArray]
    val arrsize = HeaderSize + sizeof[Short] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Short].toInt)
    castRawPtrToObject(arr).asInstanceOf[ShortArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ShortArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Short] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class IntArray private () extends Array[Int] {
  import Array._

  @inline def stride: CSize =
    sizeof[Int]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Int =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadInt(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Int): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeInt(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): IntArray = {
    val arrinfo = typeof[IntArray]
    val arrsize = HeaderSize + sizeof[Int] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }
}

object IntArray {
  import Array._

  @inline def alloc(length: Int): IntArray = {
    val arrinfo = typeof[IntArray]
    val arrsize = HeaderSize + sizeof[Int] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Int].toInt)
    castRawPtrToObject(arr).asInstanceOf[IntArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): IntArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Int] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class LongArray private () extends Array[Long] {
  import Array._

  @inline def stride: CSize =
    sizeof[Long]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Long =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadLong(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Long): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeLong(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): LongArray = {
    val arrinfo = typeof[LongArray]
    val arrsize = HeaderSize + sizeof[Long] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }
}

object LongArray {
  import Array._

  @inline def alloc(length: Int): LongArray = {
    val arrinfo = typeof[LongArray]
    val arrsize = HeaderSize + sizeof[Long] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Long].toInt)
    castRawPtrToObject(arr).asInstanceOf[LongArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): LongArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Long] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class FloatArray private () extends Array[Float] {
  import Array._

  @inline def stride: CSize =
    sizeof[Float]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Float =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadFloat(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Float): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeFloat(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): FloatArray = {
    val arrinfo = typeof[FloatArray]
    val arrsize = HeaderSize + sizeof[Float] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }
}

object FloatArray {
  import Array._

  @inline def alloc(length: Int): FloatArray = {
    val arrinfo = typeof[FloatArray]
    val arrsize = HeaderSize + sizeof[Float] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Float].toInt)
    castRawPtrToObject(arr).asInstanceOf[FloatArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): FloatArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Float] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class DoubleArray private () extends Array[Double] {
  import Array._

  @inline def stride: CSize =
    sizeof[Double]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Double =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadDouble(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Double): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeDouble(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): DoubleArray = {
    val arrinfo = typeof[DoubleArray]
    val arrsize = HeaderSize + sizeof[Double] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }
}

object DoubleArray {
  import Array._

  @inline def alloc(length: Int): DoubleArray = {
    val arrinfo = typeof[DoubleArray]
    val arrsize = HeaderSize + sizeof[Double] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Double].toInt)
    castRawPtrToObject(arr).asInstanceOf[DoubleArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): DoubleArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Double] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 144)

final class ObjectArray private () extends Array[Object] {
  import Array._

  @inline def stride: CSize =
    sizeof[Object]

  @inline def atRaw(i: Int): RawPtr =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      elemRawPtr(rawptr, HeaderSize + stride * i)
    }

  @inline def apply(i: Int): Object =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 166)
      loadObject(ith)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 170)
    }

  @inline def update(i: Int, value: Object): Unit =
    if (i < 0 || i >= length) {
      throwIndexOutOfBoundsException(i)
    } else {
      val rawptr = castObjectToRawPtr(this)
      val ith    = elemRawPtr(rawptr, HeaderSize + stride * i)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 179)
      storeObject(ith, value)
// ###sourceLocation(file: "/home/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 183)
    }

  @inline override def clone(): ObjectArray = {
    val arrinfo = typeof[ObjectArray]
    val arrsize = HeaderSize + sizeof[Object] * length
    val arr     = GC.alloc(arrinfo, arrsize)
    val src     = castObjectToRawPtr(this)
    libc.memcpy(arr, src, arrsize)
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }
}

object ObjectArray {
  import Array._

  @inline def alloc(length: Int): ObjectArray = {
    val arrinfo = typeof[ObjectArray]
    val arrsize = HeaderSize + sizeof[Object] * length
    val arr     = GC.alloc(arrinfo, arrsize)
    storeInt(elemRawPtr(arr, 8), length)
    storeInt(elemRawPtr(arr, 12), sizeof[Object].toInt)
    castRawPtrToObject(arr).asInstanceOf[ObjectArray]
  }

  @inline def snapshot(length: Int, data: RawPtr): ObjectArray = {
    val arr  = alloc(length)
    val dst  = arr.atRaw(0)
    val src  = castObjectToRawPtr(data)
    val size = sizeof[Object] * length
    libc.memcpy(dst, src, size)
    arr
  }
}
