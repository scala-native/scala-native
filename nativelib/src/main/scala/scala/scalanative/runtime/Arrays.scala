// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 1)
package scala.scalanative
package runtime

// Note 1:
// Arrays.scala is currently implemented as textual templating that is expanded through project/gyb.py script.
// Update Arrays.scala.gyb and re-generate the source
// $ ./project/gyb.py \
//     nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb > \
//     nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala

// Note 2:
// Array of primitiveTypes don't contain pointers, runtime.allocAtomic() is called for memory allocation
// Array of Object do contain pointers. runtime.alloc() is called for memory allocation

// Note 3:
// PrimitiveArray.helperClone can allocate memory with GC.malloc_atomic() because
// it will overwrite all data (no need to call llvm.memset)

import scalanative.native._
import scalanative.runtime.Intrinsics._
import scala.annotation.unchecked.uncheckedStable

@struct class ArrayHeader(val info: Ptr[Byte], val length: Int)

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 29)

sealed abstract class Array[T]
    extends java.io.Serializable
    with java.lang.Cloneable {

  /** Number of elements of the array. */
  @inline def length: Int =
    // TODO: Update once we support ptr->field
    !(this.cast[Ptr[Byte]] + sizeof[Ptr[Byte]]).cast[Ptr[Int]]

  /** Size between elements in the array. */
  def stride: CSize

  /** Pointer to the element. */
  def at(i: Int): Ptr[T]

  /** Loads element at i, throws IndexOutOfBoundsException. */
  def apply(i: Int): T

  /** Stores value to element i, throws IndexOutOfBoundsException. */
  def update(i: Int, value: T): Unit

  /** Create a shallow of given array. */
  protected override def clone(): Array[T] =
    ??? // overriden in concrete classes
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
      val fromPtr = from.at(fromPos).cast[Ptr[Byte]]
      val toPtr   = to.at(toPos).cast[Ptr[Byte]]

      `llvm.memmove.p0i8.p0i8.i64`(toPtr, fromPtr, to.stride * len, 1, false)
    }
  }

  @inline private[runtime] def helperClone(from: Array[_],
                                           length: Int,
                                           stride: CSize): Ptr[Byte] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    val arr     = GC.malloc(arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                from.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr
  }

  @inline
  def alloc(length: Int, arrinfo: Ptr[Type], stride: CSize): Ptr[Byte] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    val arr     = runtime.alloc(arrinfo, arrsize)
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[Byte]]).cast[Ptr[Int]] = length
    arr
  }
}

object PrimitiveArray {
  @inline private[runtime] def helperClone(src: Array[_],
                                           length: Int,
                                           stride: CSize): Ptr[Byte] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    val arr     = GC.malloc_atomic(arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                src.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr
  }

  @inline
  def alloc(length: Int, arrinfo: Ptr[Type], stride: CSize): Ptr[Byte] = {
    val arrsize = sizeof[ArrayHeader] + stride * length
    // Primitive arrays don't contain pointers
    val arr = runtime.allocAtomic(arrinfo, arrsize)
    // set the length
    !(arr.cast[Ptr[Byte]] + sizeof[Ptr[Byte]]).cast[Ptr[Int]] = length
    arr
  }
}

final class ObjectArray private () extends Array[Object] {
  @inline def stride: CSize =
    sizeof[Object]

  @inline def at(i: Int): Ptr[Object] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Object]]
    }

  @inline def apply(i: Int): Object = !at(i)

  @inline def update(i: Int, value: Object): Unit = !at(i) = value

  @inline protected override def clone(): ObjectArray =
    Array.helperClone(this, length, sizeof[Object]).cast[ObjectArray]
}

object ObjectArray {
  @inline def alloc(length: Int): ObjectArray =
    Array.alloc(length, typeof[ObjectArray], sizeof[Object]).cast[ObjectArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class BooleanArray private () extends Array[Boolean] {
  @inline def stride: CSize =
    sizeof[Boolean]

  @inline def at(i: Int): Ptr[Boolean] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Boolean]]
    }

  @inline def apply(i: Int): Boolean = !at(i)

  @inline def update(i: Int, value: Boolean): Unit = !at(i) = value

  @inline protected override def clone(): BooleanArray =
    PrimitiveArray
      .helperClone(this, length, sizeof[Boolean])
      .cast[BooleanArray]
}

object BooleanArray {
  @inline def alloc(length: Int): BooleanArray =
    PrimitiveArray
      .alloc(length, typeof[BooleanArray], sizeof[Boolean])
      .cast[BooleanArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class CharArray private () extends Array[Char] {
  @inline def stride: CSize =
    sizeof[Char]

  @inline def at(i: Int): Ptr[Char] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Char]]
    }

  @inline def apply(i: Int): Char = !at(i)

  @inline def update(i: Int, value: Char): Unit = !at(i) = value

  @inline protected override def clone(): CharArray =
    PrimitiveArray.helperClone(this, length, sizeof[Char]).cast[CharArray]
}

object CharArray {
  @inline def alloc(length: Int): CharArray =
    PrimitiveArray
      .alloc(length, typeof[CharArray], sizeof[Char])
      .cast[CharArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class ByteArray private () extends Array[Byte] {
  @inline def stride: CSize =
    sizeof[Byte]

  @inline def at(i: Int): Ptr[Byte] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Byte]]
    }

  @inline def apply(i: Int): Byte = !at(i)

  @inline def update(i: Int, value: Byte): Unit = !at(i) = value

  @inline protected override def clone(): ByteArray =
    PrimitiveArray.helperClone(this, length, sizeof[Byte]).cast[ByteArray]
}

object ByteArray {
  @inline def alloc(length: Int): ByteArray =
    PrimitiveArray
      .alloc(length, typeof[ByteArray], sizeof[Byte])
      .cast[ByteArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class ShortArray private () extends Array[Short] {
  @inline def stride: CSize =
    sizeof[Short]

  @inline def at(i: Int): Ptr[Short] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Short]]
    }

  @inline def apply(i: Int): Short = !at(i)

  @inline def update(i: Int, value: Short): Unit = !at(i) = value

  @inline protected override def clone(): ShortArray =
    PrimitiveArray.helperClone(this, length, sizeof[Short]).cast[ShortArray]
}

object ShortArray {
  @inline def alloc(length: Int): ShortArray =
    PrimitiveArray
      .alloc(length, typeof[ShortArray], sizeof[Short])
      .cast[ShortArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class IntArray private () extends Array[Int] {
  @inline def stride: CSize =
    sizeof[Int]

  @inline def at(i: Int): Ptr[Int] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Int]]
    }

  @inline def apply(i: Int): Int = !at(i)

  @inline def update(i: Int, value: Int): Unit = !at(i) = value

  @inline protected override def clone(): IntArray =
    PrimitiveArray.helperClone(this, length, sizeof[Int]).cast[IntArray]
}

object IntArray {
  @inline def alloc(length: Int): IntArray =
    PrimitiveArray.alloc(length, typeof[IntArray], sizeof[Int]).cast[IntArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class LongArray private () extends Array[Long] {
  @inline def stride: CSize =
    sizeof[Long]

  @inline def at(i: Int): Ptr[Long] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Long]]
    }

  @inline def apply(i: Int): Long = !at(i)

  @inline def update(i: Int, value: Long): Unit = !at(i) = value

  @inline protected override def clone(): LongArray =
    PrimitiveArray.helperClone(this, length, sizeof[Long]).cast[LongArray]
}

object LongArray {
  @inline def alloc(length: Int): LongArray =
    PrimitiveArray
      .alloc(length, typeof[LongArray], sizeof[Long])
      .cast[LongArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class FloatArray private () extends Array[Float] {
  @inline def stride: CSize =
    sizeof[Float]

  @inline def at(i: Int): Ptr[Float] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Float]]
    }

  @inline def apply(i: Int): Float = !at(i)

  @inline def update(i: Int, value: Float): Unit = !at(i) = value

  @inline protected override def clone(): FloatArray =
    PrimitiveArray.helperClone(this, length, sizeof[Float]).cast[FloatArray]
}

object FloatArray {
  @inline def alloc(length: Int): FloatArray =
    PrimitiveArray
      .alloc(length, typeof[FloatArray], sizeof[Float])
      .cast[FloatArray]
}

// ###sourceLocation(file: "/Users/denys/.src/native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 152)

final class DoubleArray private () extends Array[Double] {
  @inline def stride: CSize =
    sizeof[Double]

  @inline def at(i: Int): Ptr[Double] =
    if (i < 0 || i >= length)
      throw new IndexOutOfBoundsException(i.toString)
    else {
      val first = this.cast[Ptr[Byte]] + sizeof[ArrayHeader]
      val ith   = first + stride * i

      ith.cast[Ptr[Double]]
    }

  @inline def apply(i: Int): Double = !at(i)

  @inline def update(i: Int, value: Double): Unit = !at(i) = value

  @inline protected override def clone(): DoubleArray =
    PrimitiveArray.helperClone(this, length, sizeof[Double]).cast[DoubleArray]
}

object DoubleArray {
  @inline def alloc(length: Int): DoubleArray =
    PrimitiveArray
      .alloc(length, typeof[DoubleArray], sizeof[Double])
      .cast[DoubleArray]
}
