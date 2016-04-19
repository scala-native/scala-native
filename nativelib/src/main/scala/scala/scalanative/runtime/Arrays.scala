package scala.scalanative
package runtime

sealed abstract class Array[T] extends java.lang.Cloneable {
  def apply(i: Int): T
  def update(i: Int, value: T): Unit
  def length: Int
}

final class BooleanArray private () extends Array[Boolean] {
  def apply(i: Int): Boolean = undefined
  def update(i: Int, value: Boolean): Unit = undefined
  def length: Int = undefined
}

object BooleanArray {
  def alloc(size: Int): BooleanArray = undefined
}

final class CharArray private () extends Array[Char] {
  def apply(i: Int): Char = undefined
  def update(i: Int, value: Char): Unit = undefined
  def length: Int = undefined
}

object CharArray {
  def alloc(size: Int): CharArray = undefined
}

final class ByteArray private () extends Array[Byte] {
  def apply(i: Int): Byte = undefined
  def update(i: Int, value: Byte): Unit = undefined
  def length: Int = undefined
}

object ByteArray {
  def alloc(size: Int): ByteArray = undefined
}

final class ShortArray private () extends Array[Short] {
  def apply(i: Int): Short = undefined
  def update(i: Int, value: Short): Unit = undefined
  def length: Int = undefined
}

object ShortArray {
  def alloc(size: Int): ShortArray = undefined
}

final class IntArray private () extends Array[Int] {
  def apply(i: Int): Int = undefined
  def update(i: Int, value: Int): Unit = undefined
  def length: Int = undefined
}

object IntArray {
  def alloc(size: Int): IntArray = undefined
}

final class LongArray private () extends Array[Long] {
  def apply(i: Int): Long = undefined
  def update(i: Int, value: Long): Unit = undefined
  def length: Int = undefined
}

object LongArray {
  def alloc(size: Int): LongArray = undefined
}

final class FloatArray private () extends Array[Float] {
  def apply(i: Int): Float = undefined
  def update(i: Int, value: Float): Unit = undefined
  def length: Int = undefined
}

object FloatArray {
  def alloc(size: Int): FloatArray = undefined
}

final class DoubleArray private () extends Array[Double] {
  def apply(i: Int): Double = undefined
  def update(i: Int, value: Double): Unit = undefined
  def length: Int = undefined
}

object DoubleArray {
  def alloc(size: Int): DoubleArray = undefined
}

final class RefArray private () extends Array[AnyRef] {
  def apply(i: Int): AnyRef = undefined
  def update(i: Int, value: AnyRef): Unit = undefined
  def length: Int = undefined
}

object RefArray {
  def alloc(size: Int): RefArray = undefined
}
