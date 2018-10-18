// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 1)
package scala.scalanative
package runtime

import scalanative.native._
import scalanative.runtime.Intrinsics._

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
  override def clone(): Array[T] = ??? // overriden in concrete classes
}

object Array {
  type Header = CStruct3[Ptr[Type], Int, Int]

  implicit class HeaderOps(val self: Ptr[Header]) extends AnyVal {
    @inline def info: Ptr[Type]                = !(self._1)
    @inline def info_=(value: Ptr[Type]): Unit = !(self._1) = value
    @inline def length: Int                    = !(self._2)
    @inline def length_=(value: Int): Unit     = !(self._2) = value
    @inline def stride: CSize                  = (!(self._3)).toLong.asInstanceOf[CSize]
    @inline def stride_=(value: CSize): Unit =
      !(self._3) = value.toInt
  }

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
      val size    = to.stride * len

      `llvm.memmove.p0i8.p0i8.i64`(toPtr, fromPtr, size, 1, false)
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
      val leftPtr  = left.at(leftPos).cast[Ptr[Byte]]
      val rightPtr = right.at(rightPos).cast[Ptr[Byte]]
      libc.memcmp(leftPtr, rightPtr, len * left.stride)
    }
  }
}

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 140)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class UnitArray private () extends Array[Unit] {
  import Array._

  @inline def stride: CSize =
    sizeof[Unit]

  @inline def at(i: Int): Ptr[Unit] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Unit]]
    }
  @inline def apply(i: Int): Unit =
    !at(i)

  @inline def update(i: Int, value: Unit): Unit =
    !at(i) = value

  @inline override def clone(): UnitArray = {
    val arrinfo = typeof[UnitArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Unit] * length
    val arr     = GC.alloc(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[UnitArray]
  }
}

object UnitArray {
  import Array._

  @inline def alloc(length: Int): UnitArray = {
    val arrinfo = typeof[UnitArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Unit] * length
    val arr     = GC.alloc(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Unit]
    arr.cast[UnitArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Unit]): UnitArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Unit] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class BooleanArray private () extends Array[Boolean] {
  import Array._

  @inline def stride: CSize =
    sizeof[Boolean]

  @inline def at(i: Int): Ptr[Boolean] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Boolean]]
    }
  @inline def apply(i: Int): Boolean =
    !at(i)

  @inline def update(i: Int, value: Boolean): Unit =
    !at(i) = value

  @inline override def clone(): BooleanArray = {
    val arrinfo = typeof[BooleanArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Boolean] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[BooleanArray]
  }
}

object BooleanArray {
  import Array._

  @inline def alloc(length: Int): BooleanArray = {
    val arrinfo = typeof[BooleanArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Boolean] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Boolean]
    arr.cast[BooleanArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Boolean]): BooleanArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Boolean] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class CharArray private () extends Array[Char] {
  import Array._

  @inline def stride: CSize =
    sizeof[Char]

  @inline def at(i: Int): Ptr[Char] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Char]]
    }
  @inline def apply(i: Int): Char =
    !at(i)

  @inline def update(i: Int, value: Char): Unit =
    !at(i) = value

  @inline override def clone(): CharArray = {
    val arrinfo = typeof[CharArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Char] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[CharArray]
  }
}

object CharArray {
  import Array._

  @inline def alloc(length: Int): CharArray = {
    val arrinfo = typeof[CharArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Char] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Char]
    arr.cast[CharArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Char]): CharArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Char] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class ByteArray private () extends Array[Byte] {
  import Array._

  @inline def stride: CSize =
    sizeof[Byte]

  @inline def at(i: Int): Ptr[Byte] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Byte]]
    }
  @inline def apply(i: Int): Byte =
    !at(i)

  @inline def update(i: Int, value: Byte): Unit =
    !at(i) = value

  @inline override def clone(): ByteArray = {
    val arrinfo = typeof[ByteArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Byte] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[ByteArray]
  }
}

object ByteArray {
  import Array._

  @inline def alloc(length: Int): ByteArray = {
    val arrinfo = typeof[ByteArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Byte] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Byte]
    arr.cast[ByteArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Byte]): ByteArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Byte] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class ShortArray private () extends Array[Short] {
  import Array._

  @inline def stride: CSize =
    sizeof[Short]

  @inline def at(i: Int): Ptr[Short] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Short]]
    }
  @inline def apply(i: Int): Short =
    !at(i)

  @inline def update(i: Int, value: Short): Unit =
    !at(i) = value

  @inline override def clone(): ShortArray = {
    val arrinfo = typeof[ShortArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Short] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[ShortArray]
  }
}

object ShortArray {
  import Array._

  @inline def alloc(length: Int): ShortArray = {
    val arrinfo = typeof[ShortArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Short] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Short]
    arr.cast[ShortArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Short]): ShortArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Short] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class IntArray private () extends Array[Int] {
  import Array._

  @inline def stride: CSize =
    sizeof[Int]

  @inline def at(i: Int): Ptr[Int] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Int]]
    }
  @inline def apply(i: Int): Int =
    !at(i)

  @inline def update(i: Int, value: Int): Unit =
    !at(i) = value

  @inline override def clone(): IntArray = {
    val arrinfo = typeof[IntArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Int] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[IntArray]
  }
}

object IntArray {
  import Array._

  @inline def alloc(length: Int): IntArray = {
    val arrinfo = typeof[IntArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Int] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Int]
    arr.cast[IntArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Int]): IntArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Int] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class LongArray private () extends Array[Long] {
  import Array._

  @inline def stride: CSize =
    sizeof[Long]

  @inline def at(i: Int): Ptr[Long] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Long]]
    }
  @inline def apply(i: Int): Long =
    !at(i)

  @inline def update(i: Int, value: Long): Unit =
    !at(i) = value

  @inline override def clone(): LongArray = {
    val arrinfo = typeof[LongArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Long] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[LongArray]
  }
}

object LongArray {
  import Array._

  @inline def alloc(length: Int): LongArray = {
    val arrinfo = typeof[LongArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Long] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Long]
    arr.cast[LongArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Long]): LongArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Long] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class FloatArray private () extends Array[Float] {
  import Array._

  @inline def stride: CSize =
    sizeof[Float]

  @inline def at(i: Int): Ptr[Float] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Float]]
    }
  @inline def apply(i: Int): Float =
    !at(i)

  @inline def update(i: Int, value: Float): Unit =
    !at(i) = value

  @inline override def clone(): FloatArray = {
    val arrinfo = typeof[FloatArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Float] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[FloatArray]
  }
}

object FloatArray {
  import Array._

  @inline def alloc(length: Int): FloatArray = {
    val arrinfo = typeof[FloatArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Float] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Float]
    arr.cast[FloatArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Float]): FloatArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Float] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class DoubleArray private () extends Array[Double] {
  import Array._

  @inline def stride: CSize =
    sizeof[Double]

  @inline def at(i: Int): Ptr[Double] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Double]]
    }
  @inline def apply(i: Int): Double =
    !at(i)

  @inline def update(i: Int, value: Double): Unit =
    !at(i) = value

  @inline override def clone(): DoubleArray = {
    val arrinfo = typeof[DoubleArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Double] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[DoubleArray]
  }
}

object DoubleArray {
  import Array._

  @inline def alloc(length: Int): DoubleArray = {
    val arrinfo = typeof[DoubleArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Double] * length
    val arr     = GC.alloc_atomic(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Double]
    arr.cast[DoubleArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Double]): DoubleArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Double] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 142)

// ###sourceLocation(file: "/home/valdis/scala-native/nativelib/src/main/scala/scala/scalanative/runtime/Arrays.scala.gyb", line: 146)

final class ObjectArray private () extends Array[Object] {
  import Array._

  @inline def stride: CSize =
    sizeof[Object]

  @inline def at(i: Int): Ptr[Object] =
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException(i.toString)
    } else {
      val first = this.cast[Ptr[Byte]] + sizeof[Header]
      val ith   = first + stride * i

      ith.cast[Ptr[Object]]
    }
  @inline def apply(i: Int): Object =
    !at(i)

  @inline def update(i: Int, value: Object): Unit =
    !at(i) = value

  @inline override def clone(): ObjectArray = {
    val arrinfo = typeof[ObjectArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Object] * length
    val arr     = GC.alloc(arrinfo, arrsize)
    `llvm.memcpy.p0i8.p0i8.i64`(arr.cast[Ptr[Byte]],
                                this.cast[Ptr[Byte]],
                                arrsize,
                                1,
                                false)
    arr.cast[ObjectArray]
  }
}

object ObjectArray {
  import Array._

  @inline def alloc(length: Int): ObjectArray = {
    val arrinfo = typeof[ObjectArray].cast[Ptr[ClassType]]
    val arrsize = sizeof[Header] + sizeof[Object] * length
    val arr     = GC.alloc(arrinfo, arrsize).cast[Ptr[Header]]
    arr.length = length
    arr.stride = sizeof[Object]
    arr.cast[ObjectArray]
  }

  @inline def snapshot(length: Int, data: Ptr[Object]): ObjectArray = {
    val arr  = alloc(length)
    val dst  = arr.at(0).asInstanceOf[Ptr[Byte]]
    val src  = data.asInstanceOf[Ptr[Byte]]
    val size = sizeof[Object] * length
    `llvm.memcpy.p0i8.p0i8.i64`(dst, src, size, 1, false)
    arr
  }
}
