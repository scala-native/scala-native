// ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 1)

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// See nativelib runtime/Arrays.scala.gyb for details.

package scala.scalanative
package unsafe

import scala.reflect.ClassTag
import scalanative.annotation.alwaysinline
import scalanative.unsigned._
import scalanative.runtime._
import scalanative.runtime.Intrinsics._

sealed abstract class Tag[T] {
  def size: CSize
  def alignment: CSize
  @noinline def offset(idx: CSize): CSize                 = throwUndefined()
  @noinline def load(ptr: unsafe.Ptr[T]): T               = throwUndefined()
  @noinline def store(ptr: unsafe.Ptr[T], value: T): Unit = throwUndefined()
}

object Tag {

  final case class Ptr[T](of: Tag[T]) extends Tag[unsafe.Ptr[T]] {
    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.Ptr[T]]): unsafe.Ptr[T] =
      fromRawPtr[T](loadRawPtr(toRawPtr(ptr)))

    @alwaysinline override def store(ptr: unsafe.Ptr[unsafe.Ptr[T]],
                                     value: unsafe.Ptr[T]): Unit =
      storeRawPtr(toRawPtr(ptr), toRawPtr(value))
  }

  final case class Class[T <: AnyRef](of: java.lang.Class[T]) extends Tag[T] {
    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[T]): T =
      loadObject(toRawPtr(ptr)).asInstanceOf[T]

    @alwaysinline override def store(ptr: unsafe.Ptr[T], value: T): Unit =
      storeObject(toRawPtr(ptr), value.asInstanceOf[Object])
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Unit extends Tag[scala.Unit] {
    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Unit]): scala.Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 68)
      loadObject(toRawPtr(ptr)).asInstanceOf[Unit]

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Unit],
                                     value: scala.Unit): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 77)
      storeObject(toRawPtr(ptr), value.asInstanceOf[Object])

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Boolean extends Tag[scala.Boolean] {
    @alwaysinline def size: CSize = 1.toULong

    @alwaysinline def alignment: CSize = 1.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[scala.Boolean]): scala.Boolean =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadBoolean(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Boolean],
                                     value: scala.Boolean): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeBoolean(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Char extends Tag[scala.Char] {
    @alwaysinline def size: CSize = 2.toULong

    @alwaysinline def alignment: CSize = 2.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Char]): scala.Char =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadChar(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Char],
                                     value: scala.Char): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeChar(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Byte extends Tag[scala.Byte] {
    @alwaysinline def size: CSize = 1.toULong

    @alwaysinline def alignment: CSize = 1.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Byte]): scala.Byte =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadByte(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Byte],
                                     value: scala.Byte): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeByte(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object UByte extends Tag[unsigned.UByte] {
    @alwaysinline def size: CSize = 1.toULong

    @alwaysinline def alignment: CSize = 1.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsigned.UByte]): unsigned.UByte =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 71)
      loadByte(toRawPtr(ptr)).toUByte

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[unsigned.UByte],
                                     value: unsigned.UByte): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 80)
      storeByte(toRawPtr(ptr), value.toByte)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Short extends Tag[scala.Short] {
    @alwaysinline def size: CSize = 2.toULong

    @alwaysinline def alignment: CSize = 2.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Short]): scala.Short =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadShort(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Short],
                                     value: scala.Short): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeShort(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object UShort extends Tag[unsigned.UShort] {
    @alwaysinline def size: CSize = 2.toULong

    @alwaysinline def alignment: CSize = 2.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsigned.UShort]): unsigned.UShort =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 71)
      loadShort(toRawPtr(ptr)).toUShort

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[unsigned.UShort],
                                     value: unsigned.UShort): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 80)
      storeShort(toRawPtr(ptr), value.toShort)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Int extends Tag[scala.Int] {
    @alwaysinline def size: CSize = 4.toULong

    @alwaysinline def alignment: CSize = 4.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Int]): scala.Int =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadInt(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Int],
                                     value: scala.Int): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeInt(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object UInt extends Tag[unsigned.UInt] {
    @alwaysinline def size: CSize = 4.toULong

    @alwaysinline def alignment: CSize = 4.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsigned.UInt]): unsigned.UInt =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 71)
      loadInt(toRawPtr(ptr)).toUInt

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[unsigned.UInt],
                                     value: unsigned.UInt): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 80)
      storeInt(toRawPtr(ptr), value.toInt)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Long extends Tag[scala.Long] {
    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Long]): scala.Long =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadLong(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Long],
                                     value: scala.Long): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeLong(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object ULong extends Tag[unsigned.ULong] {
    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsigned.ULong]): unsigned.ULong =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 71)
      loadLong(toRawPtr(ptr)).toULong

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[unsigned.ULong],
                                     value: unsigned.ULong): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 80)
      storeLong(toRawPtr(ptr), value.toLong)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Float extends Tag[scala.Float] {
    @alwaysinline def size: CSize = 4.toULong

    @alwaysinline def alignment: CSize = 4.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[scala.Float]): scala.Float =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadFloat(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Float],
                                     value: scala.Float): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeFloat(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 62)

  object Double extends Tag[scala.Double] {
    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(
        ptr: unsafe.Ptr[scala.Double]): scala.Double =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 73)
      loadDouble(toRawPtr(ptr))

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 75)
    @alwaysinline override def store(ptr: unsafe.Ptr[scala.Double],
                                     value: scala.Double): Unit =
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 82)
      storeDouble(toRawPtr(ptr), value)

    // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 84)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 87)

  private[scalanative] sealed trait NatTag {
    def toInt: Int

    def toUInt: UInt = toInt.toUInt
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat0 extends Tag[unsafe.Nat._0] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 0
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat1 extends Tag[unsafe.Nat._1] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 1
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat2 extends Tag[unsafe.Nat._2] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 2
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat3 extends Tag[unsafe.Nat._3] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 3
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat4 extends Tag[unsafe.Nat._4] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 4
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat5 extends Tag[unsafe.Nat._5] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 5
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat6 extends Tag[unsafe.Nat._6] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 6
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat7 extends Tag[unsafe.Nat._7] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 7
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat8 extends Tag[unsafe.Nat._8] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 8
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 94)

  object Nat9 extends Tag[unsafe.Nat._9] with NatTag {
    @noinline def size: CSize = throwUndefined()

    @noinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = 9
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 102)

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit2[N1 <: Nat.Base, N2 <: Nat.Base](_1: Tag[N1],
                                                          _2: Tag[N2])
      extends Tag[unsafe.Nat.Digit2[N1, N2]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit3[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base](
      _1: Tag[N1],
      _2: Tag[N2],
      _3: Tag[N3])
      extends Tag[unsafe.Nat.Digit3[N1, N2, N3]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit4[N1 <: Nat.Base,
                          N2 <: Nat.Base,
                          N3 <: Nat.Base,
                          N4 <: Nat.Base](_1: Tag[N1],
                                          _2: Tag[N2],
                                          _3: Tag[N3],
                                          _4: Tag[N4])
      extends Tag[unsafe.Nat.Digit4[N1, N2, N3, N4]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _4.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit5[N1 <: Nat.Base,
                          N2 <: Nat.Base,
                          N3 <: Nat.Base,
                          N4 <: Nat.Base,
                          N5 <: Nat.Base](_1: Tag[N1],
                                          _2: Tag[N2],
                                          _3: Tag[N3],
                                          _4: Tag[N4],
                                          _5: Tag[N5])
      extends Tag[unsafe.Nat.Digit5[N1, N2, N3, N4, N5]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _4.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _5.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit6[N1 <: Nat.Base,
                          N2 <: Nat.Base,
                          N3 <: Nat.Base,
                          N4 <: Nat.Base,
                          N5 <: Nat.Base,
                          N6 <: Nat.Base](_1: Tag[N1],
                                          _2: Tag[N2],
                                          _3: Tag[N3],
                                          _4: Tag[N4],
                                          _5: Tag[N5],
                                          _6: Tag[N6])
      extends Tag[unsafe.Nat.Digit6[N1, N2, N3, N4, N5, N6]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _4.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _5.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _6.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit7[N1 <: Nat.Base,
                          N2 <: Nat.Base,
                          N3 <: Nat.Base,
                          N4 <: Nat.Base,
                          N5 <: Nat.Base,
                          N6 <: Nat.Base,
                          N7 <: Nat.Base](_1: Tag[N1],
                                          _2: Tag[N2],
                                          _3: Tag[N3],
                                          _4: Tag[N4],
                                          _5: Tag[N5],
                                          _6: Tag[N6],
                                          _7: Tag[N7])
      extends Tag[unsafe.Nat.Digit7[N1, N2, N3, N4, N5, N6, N7]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _4.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _5.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _6.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _7.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit8[N1 <: Nat.Base,
                          N2 <: Nat.Base,
                          N3 <: Nat.Base,
                          N4 <: Nat.Base,
                          N5 <: Nat.Base,
                          N6 <: Nat.Base,
                          N7 <: Nat.Base,
                          N8 <: Nat.Base](_1: Tag[N1],
                                          _2: Tag[N2],
                                          _3: Tag[N3],
                                          _4: Tag[N4],
                                          _5: Tag[N5],
                                          _6: Tag[N6],
                                          _7: Tag[N7],
                                          _8: Tag[N8])
      extends Tag[unsafe.Nat.Digit8[N1, N2, N3, N4, N5, N6, N7, N8]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _4.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _5.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _6.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _7.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _8.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 109)

  final case class Digit9[N1 <: Nat.Base,
                          N2 <: Nat.Base,
                          N3 <: Nat.Base,
                          N4 <: Nat.Base,
                          N5 <: Nat.Base,
                          N6 <: Nat.Base,
                          N7 <: Nat.Base,
                          N8 <: Nat.Base,
                          N9 <: Nat.Base](_1: Tag[N1],
                                          _2: Tag[N2],
                                          _3: Tag[N3],
                                          _4: Tag[N4],
                                          _5: Tag[N5],
                                          _6: Tag[N6],
                                          _7: Tag[N7],
                                          _8: Tag[N8],
                                          _9: Tag[N9])
      extends Tag[unsafe.Nat.Digit9[N1, N2, N3, N4, N5, N6, N7, N8, N9]]
      with NatTag {
    @alwaysinline def size: CSize = throwUndefined()

    @alwaysinline def alignment: CSize = throwUndefined()

    @alwaysinline def toInt: Int = {
      var res = 0
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _1.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _2.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _3.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _4.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _5.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _6.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _7.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _8.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 119)
      res = res * 10 + _9.asInstanceOf[NatTag].toInt
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 121)
      res
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 126)

  final case class CArray[T, N <: unsafe.Nat](of: Tag[T], n: Tag[N])
      extends Tag[unsafe.CArray[T, N]] {
    @alwaysinline def size: CSize = of.size * n.asInstanceOf[NatTag].toUInt

    @alwaysinline def alignment: CSize = of.alignment

    @alwaysinline override def offset(idx: CSize): CSize = of.size * idx.toUInt

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CArray[T, N]]): unsafe.CArray[T, N] = {
      new unsafe.CArray[T, N](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[unsafe.CArray[T, N]],
                                     value: unsafe.CArray[T, N]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size)
    }
  }

  private[scalanative] sealed trait StructTag

  @alwaysinline private[scalanative] def align(offset: CSize,
                                               alignment: CSize) = {
    val alignmentMask = alignment - 1.toULong
    val zeroUL        = 0.toULong
    val padding =
      if ((offset & alignmentMask) == zeroUL) zeroUL
      else alignment - (offset & alignmentMask)
    offset + padding
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct0() extends Tag[unsafe.CStruct0] with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct0]): unsafe.CStruct0 = {
      new unsafe.CStruct0(ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[unsafe.CStruct0],
                                     value: unsafe.CStruct0): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct1[T1](_1: Tag[T1])
      extends Tag[unsafe.CStruct1[T1]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct1[T1]]): unsafe.CStruct1[T1] = {
      new unsafe.CStruct1[T1](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[unsafe.CStruct1[T1]],
                                     value: unsafe.CStruct1[T1]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct2[T1, T2](_1: Tag[T1], _2: Tag[T2])
      extends Tag[unsafe.CStruct2[T1, T2]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct2[T1, T2]]): unsafe.CStruct2[T1, T2] = {
      new unsafe.CStruct2[T1, T2](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[unsafe.CStruct2[T1, T2]],
                                     value: unsafe.CStruct2[T1, T2]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct3[T1, T2, T3](_1: Tag[T1], _2: Tag[T2], _3: Tag[T3])
      extends Tag[unsafe.CStruct3[T1, T2, T3]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct3[T1, T2, T3]])
        : unsafe.CStruct3[T1, T2, T3] = {
      new unsafe.CStruct3[T1, T2, T3](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct3[T1, T2, T3]],
        value: unsafe.CStruct3[T1, T2, T3]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct4[T1, T2, T3, T4](_1: Tag[T1],
                                            _2: Tag[T2],
                                            _3: Tag[T3],
                                            _4: Tag[T4])
      extends Tag[unsafe.CStruct4[T1, T2, T3, T4]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct4[T1, T2, T3, T4]])
        : unsafe.CStruct4[T1, T2, T3, T4] = {
      new unsafe.CStruct4[T1, T2, T3, T4](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct4[T1, T2, T3, T4]],
        value: unsafe.CStruct4[T1, T2, T3, T4]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct5[T1, T2, T3, T4, T5](_1: Tag[T1],
                                                _2: Tag[T2],
                                                _3: Tag[T3],
                                                _4: Tag[T4],
                                                _5: Tag[T5])
      extends Tag[unsafe.CStruct5[T1, T2, T3, T4, T5]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct5[T1, T2, T3, T4, T5]])
        : unsafe.CStruct5[T1, T2, T3, T4, T5] = {
      new unsafe.CStruct5[T1, T2, T3, T4, T5](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct5[T1, T2, T3, T4, T5]],
        value: unsafe.CStruct5[T1, T2, T3, T4, T5]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct6[T1, T2, T3, T4, T5, T6](_1: Tag[T1],
                                                    _2: Tag[T2],
                                                    _3: Tag[T3],
                                                    _4: Tag[T4],
                                                    _5: Tag[T5],
                                                    _6: Tag[T6])
      extends Tag[unsafe.CStruct6[T1, T2, T3, T4, T5, T6]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct6[T1, T2, T3, T4, T5, T6]])
        : unsafe.CStruct6[T1, T2, T3, T4, T5, T6] = {
      new unsafe.CStruct6[T1, T2, T3, T4, T5, T6](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct6[T1, T2, T3, T4, T5, T6]],
        value: unsafe.CStruct6[T1, T2, T3, T4, T5, T6]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct7[T1, T2, T3, T4, T5, T6, T7](_1: Tag[T1],
                                                        _2: Tag[T2],
                                                        _3: Tag[T3],
                                                        _4: Tag[T4],
                                                        _5: Tag[T5],
                                                        _6: Tag[T6],
                                                        _7: Tag[T7])
      extends Tag[unsafe.CStruct7[T1, T2, T3, T4, T5, T6, T7]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct7[T1, T2, T3, T4, T5, T6, T7]])
        : unsafe.CStruct7[T1, T2, T3, T4, T5, T6, T7] = {
      new unsafe.CStruct7[T1, T2, T3, T4, T5, T6, T7](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct7[T1, T2, T3, T4, T5, T6, T7]],
        value: unsafe.CStruct7[T1, T2, T3, T4, T5, T6, T7]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct8[T1, T2, T3, T4, T5, T6, T7, T8](_1: Tag[T1],
                                                            _2: Tag[T2],
                                                            _3: Tag[T3],
                                                            _4: Tag[T4],
                                                            _5: Tag[T5],
                                                            _6: Tag[T6],
                                                            _7: Tag[T7],
                                                            _8: Tag[T8])
      extends Tag[unsafe.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]])
        : unsafe.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8] = {
      new unsafe.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]],
        value: unsafe.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9](_1: Tag[T1],
                                                                _2: Tag[T2],
                                                                _3: Tag[T3],
                                                                _4: Tag[T4],
                                                                _5: Tag[T5],
                                                                _6: Tag[T6],
                                                                _7: Tag[T7],
                                                                _8: Tag[T8],
                                                                _9: Tag[T9])
      extends Tag[unsafe.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]])
        : unsafe.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9] = {
      new unsafe.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[unsafe.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]],
        value: unsafe.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
      _1: Tag[T1],
      _2: Tag[T2],
      _3: Tag[T3],
      _4: Tag[T4],
      _5: Tag[T5],
      _6: Tag[T6],
      _7: Tag[T7],
      _8: Tag[T8],
      _9: Tag[T9],
      _10: Tag[T10])
      extends Tag[unsafe.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]])
        : unsafe.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] = {
      new unsafe.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[
          unsafe.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]],
        value: unsafe.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10])
        : Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
      _1: Tag[T1],
      _2: Tag[T2],
      _3: Tag[T3],
      _4: Tag[T4],
      _5: Tag[T5],
      _6: Tag[T6],
      _7: Tag[T7],
      _8: Tag[T8],
      _9: Tag[T9],
      _10: Tag[T10],
      _11: Tag[T11])
      extends Tag[
        unsafe.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]])
        : unsafe.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11] = {
      new unsafe.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
        ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[
          unsafe.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]],
        value: unsafe.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11])
        : Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
      _1: Tag[T1],
      _2: Tag[T2],
      _3: Tag[T3],
      _4: Tag[T4],
      _5: Tag[T5],
      _6: Tag[T6],
      _7: Tag[T7],
      _8: Tag[T8],
      _9: Tag[T9],
      _10: Tag[T10],
      _11: Tag[T11],
      _12: Tag[T12])
      extends Tag[
        unsafe.CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[unsafe.CStruct12[T1,
                                         T2,
                                         T3,
                                         T4,
                                         T5,
                                         T6,
                                         T7,
                                         T8,
                                         T9,
                                         T10,
                                         T11,
                                         T12]]): unsafe.CStruct12[T1,
                                                                  T2,
                                                                  T3,
                                                                  T4,
                                                                  T5,
                                                                  T6,
                                                                  T7,
                                                                  T8,
                                                                  T9,
                                                                  T10,
                                                                  T11,
                                                                  T12] = {
      new unsafe.CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
        ptr.rawptr)
    }

    @alwaysinline override def store(
        ptr: unsafe.Ptr[
          unsafe.CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]],
        value: unsafe.CStruct12[T1,
                                T2,
                                T3,
                                T4,
                                T5,
                                T6,
                                T7,
                                T8,
                                T9,
                                T10,
                                T11,
                                T12]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct13[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13])
      extends Tag[
        unsafe.CStruct13[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(ptr: unsafe.Ptr[
      unsafe.CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]])
        : unsafe.CStruct13[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13] = {
      new unsafe.CStruct13[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct13[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13]],
                                     value: unsafe.CStruct13[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct14[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14])
      extends Tag[
        unsafe.CStruct14[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct14[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14]]): unsafe.CStruct14[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14] = {
      new unsafe.CStruct14[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct14[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14]],
                                     value: unsafe.CStruct14[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct15[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15])
      extends Tag[
        unsafe.CStruct15[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct15[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15]]): unsafe.CStruct15[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15] = {
      new unsafe.CStruct15[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct15[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15]],
                                     value: unsafe.CStruct15[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct16[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16])
      extends Tag[
        unsafe.CStruct16[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct16[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16]]): unsafe.CStruct16[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16] = {
      new unsafe.CStruct16[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct16[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16]],
                                     value: unsafe.CStruct16[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct17[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17])
      extends Tag[
        unsafe.CStruct17[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _17.alignment) + _17.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 16 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct17[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17]]): unsafe.CStruct17[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16,
                                                    T17] = {
      new unsafe.CStruct17[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct17[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16,
                                                        T17]],
                                     value: unsafe.CStruct17[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16,
                                                             T17]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct18[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17,
                             T18](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18])
      extends Tag[
        unsafe.CStruct18[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17,
                         T18]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _17.alignment) + _17.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _18.alignment) + _18.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 16 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 17 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct18[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18]]): unsafe.CStruct18[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16,
                                                    T17,
                                                    T18] = {
      new unsafe.CStruct18[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct18[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16,
                                                        T17,
                                                        T18]],
                                     value: unsafe.CStruct18[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16,
                                                             T17,
                                                             T18]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct19[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17,
                             T18,
                             T19](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19])
      extends Tag[
        unsafe.CStruct19[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17,
                         T18,
                         T19]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _17.alignment) + _17.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _18.alignment) + _18.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _19.alignment) + _19.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 16 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 17 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 18 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct19[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19]]): unsafe.CStruct19[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16,
                                                    T17,
                                                    T18,
                                                    T19] = {
      new unsafe.CStruct19[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct19[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16,
                                                        T17,
                                                        T18,
                                                        T19]],
                                     value: unsafe.CStruct19[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16,
                                                             T17,
                                                             T18,
                                                             T19]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct20[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17,
                             T18,
                             T19,
                             T20](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19],
                                  _20: Tag[T20])
      extends Tag[
        unsafe.CStruct20[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17,
                         T18,
                         T19,
                         T20]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _17.alignment) + _17.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _18.alignment) + _18.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _19.alignment) + _19.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _20.alignment) + _20.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_20.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 16 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 17 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 18 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 19 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _19.alignment) + _19.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _20.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct20[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19,
                           T20]]): unsafe.CStruct20[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16,
                                                    T17,
                                                    T18,
                                                    T19,
                                                    T20] = {
      new unsafe.CStruct20[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19,
                           T20](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct20[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16,
                                                        T17,
                                                        T18,
                                                        T19,
                                                        T20]],
                                     value: unsafe.CStruct20[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16,
                                                             T17,
                                                             T18,
                                                             T19,
                                                             T20]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct21[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17,
                             T18,
                             T19,
                             T20,
                             T21](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19],
                                  _20: Tag[T20],
                                  _21: Tag[T21])
      extends Tag[
        unsafe.CStruct21[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17,
                         T18,
                         T19,
                         T20,
                         T21]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _17.alignment) + _17.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _18.alignment) + _18.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _19.alignment) + _19.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _20.alignment) + _20.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _21.alignment) + _21.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_20.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_21.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 16 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 17 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 18 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 19 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _19.alignment) + _19.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _20.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 20 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _19.alignment) + _19.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _20.alignment) + _20.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _21.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct21[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19,
                           T20,
                           T21]]): unsafe.CStruct21[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16,
                                                    T17,
                                                    T18,
                                                    T19,
                                                    T20,
                                                    T21] = {
      new unsafe.CStruct21[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19,
                           T20,
                           T21](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct21[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16,
                                                        T17,
                                                        T18,
                                                        T19,
                                                        T20,
                                                        T21]],
                                     value: unsafe.CStruct21[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16,
                                                             T17,
                                                             T18,
                                                             T19,
                                                             T20,
                                                             T21]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 159)

  final case class CStruct22[T1,
                             T2,
                             T3,
                             T4,
                             T5,
                             T6,
                             T7,
                             T8,
                             T9,
                             T10,
                             T11,
                             T12,
                             T13,
                             T14,
                             T15,
                             T16,
                             T17,
                             T18,
                             T19,
                             T20,
                             T21,
                             T22](_1: Tag[T1],
                                  _2: Tag[T2],
                                  _3: Tag[T3],
                                  _4: Tag[T4],
                                  _5: Tag[T5],
                                  _6: Tag[T6],
                                  _7: Tag[T7],
                                  _8: Tag[T8],
                                  _9: Tag[T9],
                                  _10: Tag[T10],
                                  _11: Tag[T11],
                                  _12: Tag[T12],
                                  _13: Tag[T13],
                                  _14: Tag[T14],
                                  _15: Tag[T15],
                                  _16: Tag[T16],
                                  _17: Tag[T17],
                                  _18: Tag[T18],
                                  _19: Tag[T19],
                                  _20: Tag[T20],
                                  _21: Tag[T21],
                                  _22: Tag[T22])
      extends Tag[
        unsafe.CStruct22[T1,
                         T2,
                         T3,
                         T4,
                         T5,
                         T6,
                         T7,
                         T8,
                         T9,
                         T10,
                         T11,
                         T12,
                         T13,
                         T14,
                         T15,
                         T16,
                         T17,
                         T18,
                         T19,
                         T20,
                         T21,
                         T22]]
      with StructTag {
    @alwaysinline def size: CSize = {
      var res = 0.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _1.alignment) + _1.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _2.alignment) + _2.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _3.alignment) + _3.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _4.alignment) + _4.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _5.alignment) + _5.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _6.alignment) + _6.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _7.alignment) + _7.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _8.alignment) + _8.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _9.alignment) + _9.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _10.alignment) + _10.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _11.alignment) + _11.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _12.alignment) + _12.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _13.alignment) + _13.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _14.alignment) + _14.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _15.alignment) + _15.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _16.alignment) + _16.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _17.alignment) + _17.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _18.alignment) + _18.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _19.alignment) + _19.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _20.alignment) + _20.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _21.alignment) + _21.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 167)
      res = align(res, _22.alignment) + _22.size
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 169)
      align(res, alignment)
    }

    @alwaysinline def alignment: CSize = {
      var res = 1.toULong
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_20.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_21.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 174)
      res = res.max(_22.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 176)
      res
    }

    @alwaysinline override def offset(idx: CSize): CSize = idx.toInt match {
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 0 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _1.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 1 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _2.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 2 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _3.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 3 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _4.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 4 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _5.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 5 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _6.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 6 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _7.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 7 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _8.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 8 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _9.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 9 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _10.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 10 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _11.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 11 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _12.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 12 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _13.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 13 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _14.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 14 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _15.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 15 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _16.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 16 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _17.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 17 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _18.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 18 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _19.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 19 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _19.alignment) + _19.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _20.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 20 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _19.alignment) + _19.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _20.alignment) + _20.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _21.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 180)
      case 21 =>
        var res = 0.toULong
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _1.alignment) + _1.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _2.alignment) + _2.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _3.alignment) + _3.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _4.alignment) + _4.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _5.alignment) + _5.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _6.alignment) + _6.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _7.alignment) + _7.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _8.alignment) + _8.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _9.alignment) + _9.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _10.alignment) + _10.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _11.alignment) + _11.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _12.alignment) + _12.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _13.alignment) + _13.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _14.alignment) + _14.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _15.alignment) + _15.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _16.alignment) + _16.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _17.alignment) + _17.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _18.alignment) + _18.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _19.alignment) + _19.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _20.alignment) + _20.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 183)
        res = align(res, _21.alignment) + _21.size
        // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 185)
        align(res, _22.alignment)
      // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 187)
      case _ =>
        throwUndefined()
    }

    @alwaysinline override def load(
        ptr: unsafe.Ptr[
          unsafe.CStruct22[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19,
                           T20,
                           T21,
                           T22]]): unsafe.CStruct22[T1,
                                                    T2,
                                                    T3,
                                                    T4,
                                                    T5,
                                                    T6,
                                                    T7,
                                                    T8,
                                                    T9,
                                                    T10,
                                                    T11,
                                                    T12,
                                                    T13,
                                                    T14,
                                                    T15,
                                                    T16,
                                                    T17,
                                                    T18,
                                                    T19,
                                                    T20,
                                                    T21,
                                                    T22] = {
      new unsafe.CStruct22[T1,
                           T2,
                           T3,
                           T4,
                           T5,
                           T6,
                           T7,
                           T8,
                           T9,
                           T10,
                           T11,
                           T12,
                           T13,
                           T14,
                           T15,
                           T16,
                           T17,
                           T18,
                           T19,
                           T20,
                           T21,
                           T22](ptr.rawptr)
    }

    @alwaysinline override def store(ptr: unsafe.Ptr[
                                       unsafe.CStruct22[T1,
                                                        T2,
                                                        T3,
                                                        T4,
                                                        T5,
                                                        T6,
                                                        T7,
                                                        T8,
                                                        T9,
                                                        T10,
                                                        T11,
                                                        T12,
                                                        T13,
                                                        T14,
                                                        T15,
                                                        T16,
                                                        T17,
                                                        T18,
                                                        T19,
                                                        T20,
                                                        T21,
                                                        T22]],
                                     value: unsafe.CStruct22[T1,
                                                             T2,
                                                             T3,
                                                             T4,
                                                             T5,
                                                             T6,
                                                             T7,
                                                             T8,
                                                             T9,
                                                             T10,
                                                             T11,
                                                             T12,
                                                             T13,
                                                             T14,
                                                             T15,
                                                             T16,
                                                             T17,
                                                             T18,
                                                             T19,
                                                             T20,
                                                             T21,
                                                             T22]): Unit = {
      val dst = ptr.rawptr
      val src = value.rawptr
      libc.memcpy(dst, src, size.toULong)
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 201)

  abstract class CFuncPtrTag[F <: unsafe.CFuncPtr] private[unsafe] ()
      extends Tag[F] {

    /** Internal method used to cast Ptr[_] to CFuncPtr using its underlying RawPtr
     * Takes RawPtr instead Ptr[_] to skip extra boxing
     */
    private[unsafe] def fromRawPtr(rawptr: RawPtr): F

    @alwaysinline def size: CSize = 8.toULong

    @alwaysinline def alignment: CSize = 8.toULong

    @alwaysinline override def load(ptr: unsafe.Ptr[F]): F =
      fromRawPtr(loadRawPtr(ptr.rawptr))

    @alwaysinline override def store(ptr: unsafe.Ptr[F], value: F): Unit =
      storeRawPtr(toRawPtr(ptr), value.rawptr)
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr0[R] extends CFuncPtrTag[unsafe.CFuncPtr0[R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr1[T1, R] extends CFuncPtrTag[unsafe.CFuncPtr1[T1, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr2[T1, T2, R]
      extends CFuncPtrTag[unsafe.CFuncPtr2[T1, T2, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr3[T1, T2, T3, R]
      extends CFuncPtrTag[unsafe.CFuncPtr3[T1, T2, T3, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr4[T1, T2, T3, T4, R]
      extends CFuncPtrTag[unsafe.CFuncPtr4[T1, T2, T3, T4, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr5[T1, T2, T3, T4, T5, R]
      extends CFuncPtrTag[unsafe.CFuncPtr5[T1, T2, T3, T4, T5, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr6[T1, T2, T3, T4, T5, T6, R]
      extends CFuncPtrTag[unsafe.CFuncPtr6[T1, T2, T3, T4, T5, T6, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R]
      extends CFuncPtrTag[unsafe.CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R]
      extends CFuncPtrTag[unsafe.CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr12[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr13[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr13[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr14[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr14[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr15[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr15[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr16[
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr16[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr17[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr17[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr18[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr18[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr19[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr19[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr20[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr20[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          T20,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr21[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      T21,
      R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr21[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          T20,
                          T21,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 221)
  abstract class CFuncPtr22[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      T21,
      T22,
      R]
      extends CFuncPtrTag[
        unsafe.CFuncPtr22[T1,
                          T2,
                          T3,
                          T4,
                          T5,
                          T6,
                          T7,
                          T8,
                          T9,
                          T10,
                          T11,
                          T12,
                          T13,
                          T14,
                          T15,
                          T16,
                          T17,
                          T18,
                          T19,
                          T20,
                          T21,
                          T22,
                          R]]

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 224)

  @alwaysinline implicit def materializePtrTag[T](
      implicit tag: Tag[T]): Tag[unsafe.Ptr[T]] =
    Tag.Ptr(tag)

  @alwaysinline implicit def materializeClassTag[T <: AnyRef: ClassTag]
      : Tag[T] =
    Tag.Class(
      implicitly[ClassTag[T]].runtimeClass.asInstanceOf[java.lang.Class[T]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeUnitTag: Tag[scala.Unit] =
    Unit

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeBooleanTag: Tag[scala.Boolean] =
    Boolean

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeCharTag: Tag[scala.Char] =
    Char

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeByteTag: Tag[scala.Byte] =
    Byte

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeUByteTag: Tag[unsigned.UByte] =
    UByte

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeShortTag: Tag[scala.Short] =
    Short

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeUShortTag: Tag[unsigned.UShort] =
    UShort

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeIntTag: Tag[scala.Int] =
    Int

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeUIntTag: Tag[unsigned.UInt] =
    UInt

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeLongTag: Tag[scala.Long] =
    Long

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeULongTag: Tag[unsigned.ULong] =
    ULong

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeFloatTag: Tag[scala.Float] =
    Float

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 230)
  @alwaysinline implicit def materializeDoubleTag: Tag[scala.Double] =
    Double

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat0Tag: Tag[unsafe.Nat._0] =
    Nat0

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat1Tag: Tag[unsafe.Nat._1] =
    Nat1

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat2Tag: Tag[unsafe.Nat._2] =
    Nat2

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat3Tag: Tag[unsafe.Nat._3] =
    Nat3

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat4Tag: Tag[unsafe.Nat._4] =
    Nat4

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat5Tag: Tag[unsafe.Nat._5] =
    Nat5

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat6Tag: Tag[unsafe.Nat._6] =
    Nat6

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat7Tag: Tag[unsafe.Nat._7] =
    Nat7

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat8Tag: Tag[unsafe.Nat._8] =
    Nat8

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 234)
  @alwaysinline implicit def materializeNat9Tag: Tag[unsafe.Nat._9] =
    Nat9

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit2Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag]
      : Tag.Digit2[N1, N2] =
    Tag.Digit2(implicitly[Tag[N1]], implicitly[Tag[N2]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit3Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag]
      : Tag.Digit3[N1, N2, N3] =
    Tag.Digit3(implicitly[Tag[N1]], implicitly[Tag[N2]], implicitly[Tag[N3]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit4Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag,
                                                     N4 <: Nat.Base: Tag]
      : Tag.Digit4[N1, N2, N3, N4] =
    Tag.Digit4(implicitly[Tag[N1]],
               implicitly[Tag[N2]],
               implicitly[Tag[N3]],
               implicitly[Tag[N4]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit5Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag,
                                                     N4 <: Nat.Base: Tag,
                                                     N5 <: Nat.Base: Tag]
      : Tag.Digit5[N1, N2, N3, N4, N5] =
    Tag.Digit5(implicitly[Tag[N1]],
               implicitly[Tag[N2]],
               implicitly[Tag[N3]],
               implicitly[Tag[N4]],
               implicitly[Tag[N5]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit6Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag,
                                                     N4 <: Nat.Base: Tag,
                                                     N5 <: Nat.Base: Tag,
                                                     N6 <: Nat.Base: Tag]
      : Tag.Digit6[N1, N2, N3, N4, N5, N6] =
    Tag.Digit6(implicitly[Tag[N1]],
               implicitly[Tag[N2]],
               implicitly[Tag[N3]],
               implicitly[Tag[N4]],
               implicitly[Tag[N5]],
               implicitly[Tag[N6]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit7Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag,
                                                     N4 <: Nat.Base: Tag,
                                                     N5 <: Nat.Base: Tag,
                                                     N6 <: Nat.Base: Tag,
                                                     N7 <: Nat.Base: Tag]
      : Tag.Digit7[N1, N2, N3, N4, N5, N6, N7] =
    Tag.Digit7(implicitly[Tag[N1]],
               implicitly[Tag[N2]],
               implicitly[Tag[N3]],
               implicitly[Tag[N4]],
               implicitly[Tag[N5]],
               implicitly[Tag[N6]],
               implicitly[Tag[N7]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit8Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag,
                                                     N4 <: Nat.Base: Tag,
                                                     N5 <: Nat.Base: Tag,
                                                     N6 <: Nat.Base: Tag,
                                                     N7 <: Nat.Base: Tag,
                                                     N8 <: Nat.Base: Tag]
      : Tag.Digit8[N1, N2, N3, N4, N5, N6, N7, N8] =
    Tag.Digit8(
      implicitly[Tag[N1]],
      implicitly[Tag[N2]],
      implicitly[Tag[N3]],
      implicitly[Tag[N4]],
      implicitly[Tag[N5]],
      implicitly[Tag[N6]],
      implicitly[Tag[N7]],
      implicitly[Tag[N8]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 242)
  @alwaysinline implicit def materializeNatDigit9Tag[N1 <: Nat.Base: Tag,
                                                     N2 <: Nat.Base: Tag,
                                                     N3 <: Nat.Base: Tag,
                                                     N4 <: Nat.Base: Tag,
                                                     N5 <: Nat.Base: Tag,
                                                     N6 <: Nat.Base: Tag,
                                                     N7 <: Nat.Base: Tag,
                                                     N8 <: Nat.Base: Tag,
                                                     N9 <: Nat.Base: Tag]
      : Tag.Digit9[N1, N2, N3, N4, N5, N6, N7, N8, N9] =
    Tag.Digit9(
      implicitly[Tag[N1]],
      implicitly[Tag[N2]],
      implicitly[Tag[N3]],
      implicitly[Tag[N4]],
      implicitly[Tag[N5]],
      implicitly[Tag[N6]],
      implicitly[Tag[N7]],
      implicitly[Tag[N8]],
      implicitly[Tag[N9]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct0Tag: Tag.CStruct0 =
    Tag.CStruct0()

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct1Tag[T1: Tag]: Tag.CStruct1[T1] =
    Tag.CStruct1(implicitly[Tag[T1]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct2Tag[T1: Tag, T2: Tag]
      : Tag.CStruct2[T1, T2] =
    Tag.CStruct2(implicitly[Tag[T1]], implicitly[Tag[T2]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct3Tag[T1: Tag, T2: Tag, T3: Tag]
      : Tag.CStruct3[T1, T2, T3] =
    Tag.CStruct3(implicitly[Tag[T1]], implicitly[Tag[T2]], implicitly[Tag[T3]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct4Tag[T1: Tag,
                                                    T2: Tag,
                                                    T3: Tag,
                                                    T4: Tag]
      : Tag.CStruct4[T1, T2, T3, T4] =
    Tag.CStruct4(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct5Tag[T1: Tag,
                                                    T2: Tag,
                                                    T3: Tag,
                                                    T4: Tag,
                                                    T5: Tag]
      : Tag.CStruct5[T1, T2, T3, T4, T5] =
    Tag.CStruct5(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]],
                 implicitly[Tag[T5]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct6Tag[T1: Tag,
                                                    T2: Tag,
                                                    T3: Tag,
                                                    T4: Tag,
                                                    T5: Tag,
                                                    T6: Tag]
      : Tag.CStruct6[T1, T2, T3, T4, T5, T6] =
    Tag.CStruct6(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]],
                 implicitly[Tag[T5]],
                 implicitly[Tag[T6]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct7Tag[T1: Tag,
                                                    T2: Tag,
                                                    T3: Tag,
                                                    T4: Tag,
                                                    T5: Tag,
                                                    T6: Tag,
                                                    T7: Tag]
      : Tag.CStruct7[T1, T2, T3, T4, T5, T6, T7] =
    Tag.CStruct7(implicitly[Tag[T1]],
                 implicitly[Tag[T2]],
                 implicitly[Tag[T3]],
                 implicitly[Tag[T4]],
                 implicitly[Tag[T5]],
                 implicitly[Tag[T6]],
                 implicitly[Tag[T7]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct8Tag[T1: Tag,
                                                    T2: Tag,
                                                    T3: Tag,
                                                    T4: Tag,
                                                    T5: Tag,
                                                    T6: Tag,
                                                    T7: Tag,
                                                    T8: Tag]
      : Tag.CStruct8[T1, T2, T3, T4, T5, T6, T7, T8] =
    Tag.CStruct8(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct9Tag[T1: Tag,
                                                    T2: Tag,
                                                    T3: Tag,
                                                    T4: Tag,
                                                    T5: Tag,
                                                    T6: Tag,
                                                    T7: Tag,
                                                    T8: Tag,
                                                    T9: Tag]
      : Tag.CStruct9[T1, T2, T3, T4, T5, T6, T7, T8, T9] =
    Tag.CStruct9(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct10Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag]
      : Tag.CStruct10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] =
    Tag.CStruct10(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct11Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag]
      : Tag.CStruct11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11] =
    Tag.CStruct11(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct12Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag]
      : Tag.CStruct12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12] =
    Tag.CStruct12(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct13Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag]
      : Tag.CStruct13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13] =
    Tag.CStruct13(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct14Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag]
      : Tag.CStruct14[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14] =
    Tag.CStruct14(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct15Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag]
      : Tag.CStruct15[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15] =
    Tag.CStruct15(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct16Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag]
      : Tag.CStruct16[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16] =
    Tag.CStruct16(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct17Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag,
                                                     T17: Tag]
      : Tag.CStruct17[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17] =
    Tag.CStruct17(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct18Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag,
                                                     T17: Tag,
                                                     T18: Tag]
      : Tag.CStruct18[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18] =
    Tag.CStruct18(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct19Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag,
                                                     T17: Tag,
                                                     T18: Tag,
                                                     T19: Tag]
      : Tag.CStruct19[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19] =
    Tag.CStruct19(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct20Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag,
                                                     T17: Tag,
                                                     T18: Tag,
                                                     T19: Tag,
                                                     T20: Tag]
      : Tag.CStruct20[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      T20] =
    Tag.CStruct20(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]],
      implicitly[Tag[T20]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct21Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag,
                                                     T17: Tag,
                                                     T18: Tag,
                                                     T19: Tag,
                                                     T20: Tag,
                                                     T21: Tag]
      : Tag.CStruct21[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      T20,
                      T21] =
    Tag.CStruct21(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]],
      implicitly[Tag[T20]],
      implicitly[Tag[T21]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 250)
  @alwaysinline implicit def materializeCStruct22Tag[T1: Tag,
                                                     T2: Tag,
                                                     T3: Tag,
                                                     T4: Tag,
                                                     T5: Tag,
                                                     T6: Tag,
                                                     T7: Tag,
                                                     T8: Tag,
                                                     T9: Tag,
                                                     T10: Tag,
                                                     T11: Tag,
                                                     T12: Tag,
                                                     T13: Tag,
                                                     T14: Tag,
                                                     T15: Tag,
                                                     T16: Tag,
                                                     T17: Tag,
                                                     T18: Tag,
                                                     T19: Tag,
                                                     T20: Tag,
                                                     T21: Tag,
                                                     T22: Tag]
      : Tag.CStruct22[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      T20,
                      T21,
                      T22] =
    Tag.CStruct22(
      implicitly[Tag[T1]],
      implicitly[Tag[T2]],
      implicitly[Tag[T3]],
      implicitly[Tag[T4]],
      implicitly[Tag[T5]],
      implicitly[Tag[T6]],
      implicitly[Tag[T7]],
      implicitly[Tag[T8]],
      implicitly[Tag[T9]],
      implicitly[Tag[T10]],
      implicitly[Tag[T11]],
      implicitly[Tag[T12]],
      implicitly[Tag[T13]],
      implicitly[Tag[T14]],
      implicitly[Tag[T15]],
      implicitly[Tag[T16]],
      implicitly[Tag[T17]],
      implicitly[Tag[T18]],
      implicitly[Tag[T19]],
      implicitly[Tag[T20]],
      implicitly[Tag[T21]],
      implicitly[Tag[T22]]
    )

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 253)
  @alwaysinline implicit def materializeCArrayTag[T: Tag, N <: unsafe.Nat: Tag]
      : Tag.CArray[T, N] =
    Tag.CArray(implicitly[Tag[T]], implicitly[Tag[N]])

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr0[R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr0[R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr0[R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr0[R] = {
        unsafe.CFuncPtr0.fromRawPtr[R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr1[T1: Tag, R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr1[T1, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr1[T1, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr1[T1, R] = {
        unsafe.CFuncPtr1.fromRawPtr[T1, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr2[T1: Tag, T2: Tag, R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr2[T1, T2, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr2[T1, T2, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr2[T1, T2, R] = {
        unsafe.CFuncPtr2.fromRawPtr[T1, T2, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr3[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr3[T1, T2, T3, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr3[T1, T2, T3, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr3[T1, T2, T3, R] = {
        unsafe.CFuncPtr3.fromRawPtr[T1, T2, T3, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr4[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  T4: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr4[T1, T2, T3, T4, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr4[T1, T2, T3, T4, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr4[T1, T2, T3, T4, R] = {
        unsafe.CFuncPtr4.fromRawPtr[T1, T2, T3, T4, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr5[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  T4: Tag,
                                                  T5: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr5[T1, T2, T3, T4, T5, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr5[T1, T2, T3, T4, T5, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr5[T1, T2, T3, T4, T5, R] = {
        unsafe.CFuncPtr5.fromRawPtr[T1, T2, T3, T4, T5, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr6[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  T4: Tag,
                                                  T5: Tag,
                                                  T6: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr6[T1, T2, T3, T4, T5, T6, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr6[T1, T2, T3, T4, T5, T6, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr6[T1, T2, T3, T4, T5, T6, R] = {
        unsafe.CFuncPtr6.fromRawPtr[T1, T2, T3, T4, T5, T6, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr7[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  T4: Tag,
                                                  T5: Tag,
                                                  T6: Tag,
                                                  T7: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr7[T1, T2, T3, T4, T5, T6, T7, R] = {
        unsafe.CFuncPtr7.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr8[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  T4: Tag,
                                                  T5: Tag,
                                                  T6: Tag,
                                                  T7: Tag,
                                                  T8: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(rawptr: RawPtr)
          : unsafe.CFuncPtr8[T1, T2, T3, T4, T5, T6, T7, T8, R] = {
        unsafe.CFuncPtr8.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr9[T1: Tag,
                                                  T2: Tag,
                                                  T3: Tag,
                                                  T4: Tag,
                                                  T5: Tag,
                                                  T6: Tag,
                                                  T7: Tag,
                                                  T8: Tag,
                                                  T9: Tag,
                                                  R: Tag]
      : CFuncPtrTag[unsafe.CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]] = {
    new CFuncPtrTag[unsafe.CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(rawptr: RawPtr)
          : unsafe.CFuncPtr9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R] = {
        unsafe.CFuncPtr9.fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](
          rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr10[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(rawptr: RawPtr)
          : unsafe.CFuncPtr10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R] = {
        unsafe.CFuncPtr10
          .fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr11[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr11[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             R] = {
        unsafe.CFuncPtr11
          .fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr12[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr12[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             R] = {
        unsafe.CFuncPtr12
          .fromRawPtr[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](
            rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr13[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr13[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr13[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr13[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             R] = {
        unsafe.CFuncPtr13.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr14[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr14[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr14[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr14[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             R] = {
        unsafe.CFuncPtr14.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr15[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr15[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr15[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr15[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             R] = {
        unsafe.CFuncPtr15.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr16[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr16[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr16[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr16[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             R] = {
        unsafe.CFuncPtr16.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr17[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   T17: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr17[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr17[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        T17,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr17[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             T17,
                                             R] = {
        unsafe.CFuncPtr17.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     T17,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr18[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   T17: Tag,
                                                   T18: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr18[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr18[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        T17,
                        T18,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr18[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             T17,
                                             T18,
                                             R] = {
        unsafe.CFuncPtr18.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     T17,
                                     T18,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr19[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   T17: Tag,
                                                   T18: Tag,
                                                   T19: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr19[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr19[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        T17,
                        T18,
                        T19,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr19[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             T17,
                                             T18,
                                             T19,
                                             R] = {
        unsafe.CFuncPtr19.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     T17,
                                     T18,
                                     T19,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr20[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   T17: Tag,
                                                   T18: Tag,
                                                   T19: Tag,
                                                   T20: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr20[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      T20,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr20[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        T17,
                        T18,
                        T19,
                        T20,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr20[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             T17,
                                             T18,
                                             T19,
                                             T20,
                                             R] = {
        unsafe.CFuncPtr20.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     T17,
                                     T18,
                                     T19,
                                     T20,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr21[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   T17: Tag,
                                                   T18: Tag,
                                                   T19: Tag,
                                                   T20: Tag,
                                                   T21: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr21[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      T20,
                      T21,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr21[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        T17,
                        T18,
                        T19,
                        T20,
                        T21,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr21[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             T17,
                                             T18,
                                             T19,
                                             T20,
                                             T21,
                                             R] = {
        unsafe.CFuncPtr21.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     T17,
                                     T18,
                                     T19,
                                     T20,
                                     T21,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 262)

  @alwaysinline implicit def materializeCFuncPtr22[T1: Tag,
                                                   T2: Tag,
                                                   T3: Tag,
                                                   T4: Tag,
                                                   T5: Tag,
                                                   T6: Tag,
                                                   T7: Tag,
                                                   T8: Tag,
                                                   T9: Tag,
                                                   T10: Tag,
                                                   T11: Tag,
                                                   T12: Tag,
                                                   T13: Tag,
                                                   T14: Tag,
                                                   T15: Tag,
                                                   T16: Tag,
                                                   T17: Tag,
                                                   T18: Tag,
                                                   T19: Tag,
                                                   T20: Tag,
                                                   T21: Tag,
                                                   T22: Tag,
                                                   R: Tag]: CFuncPtrTag[
    unsafe.CFuncPtr22[T1,
                      T2,
                      T3,
                      T4,
                      T5,
                      T6,
                      T7,
                      T8,
                      T9,
                      T10,
                      T11,
                      T12,
                      T13,
                      T14,
                      T15,
                      T16,
                      T17,
                      T18,
                      T19,
                      T20,
                      T21,
                      T22,
                      R]] = {
    new CFuncPtrTag[
      unsafe.CFuncPtr22[T1,
                        T2,
                        T3,
                        T4,
                        T5,
                        T6,
                        T7,
                        T8,
                        T9,
                        T10,
                        T11,
                        T12,
                        T13,
                        T14,
                        T15,
                        T16,
                        T17,
                        T18,
                        T19,
                        T20,
                        T21,
                        T22,
                        R]] {
      @alwaysinline override private[unsafe] def fromRawPtr(
          rawptr: RawPtr): unsafe.CFuncPtr22[T1,
                                             T2,
                                             T3,
                                             T4,
                                             T5,
                                             T6,
                                             T7,
                                             T8,
                                             T9,
                                             T10,
                                             T11,
                                             T12,
                                             T13,
                                             T14,
                                             T15,
                                             T16,
                                             T17,
                                             T18,
                                             T19,
                                             T20,
                                             T21,
                                             T22,
                                             R] = {
        unsafe.CFuncPtr22.fromRawPtr[T1,
                                     T2,
                                     T3,
                                     T4,
                                     T5,
                                     T6,
                                     T7,
                                     T8,
                                     T9,
                                     T10,
                                     T11,
                                     T12,
                                     T13,
                                     T14,
                                     T15,
                                     T16,
                                     T17,
                                     T18,
                                     T19,
                                     T20,
                                     T21,
                                     T22,
                                     R](rawptr)
      }
    }
  }

  // ###sourceLocation(file: "/home/wmazur/projects/scalacenter/scala-native/scala-native/nativelib/src/main/scala/scala/scalanative/unsafe/Tag.scala.gyb", line: 271)
}
