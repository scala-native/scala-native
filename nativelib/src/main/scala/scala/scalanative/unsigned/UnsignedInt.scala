package scala.scalanative
package unsigned
import scala.scalanative.unsigned.NewUInt

final class UnsignedInt(private[scalanative] val value: NewUInt)
    extends Number
    with Comparable[UnsignedInt] {

  override def intValue(): Int = value.toInt
  override def longValue(): Long = value.toLong
  override def floatValue(): Float = value.toFloat
  override def doubleValue(): Double = value.toDouble
  override def compareTo(o: UnsignedInt): Int =
    Integer.compareUnsigned(this.intValue, o.intValue())

   override def toString(): String = Integer.toUnsignedString(intValue())
}

// object UInt {

//   /** The smallest value representable as a UInt. */
//   final val MinValue

//   /** The largest value representable as a UInt. */
//   final val MaxValue

//   /** The String representation of the scala.UInt companion object. */
//   override def toString(): String

//   /** Language mandated coercions from UInt to "wider" types. */
//   import scala.language.implicitConversions
//   implicit def uint2ulong(x: NewUInt): ULong
// }
