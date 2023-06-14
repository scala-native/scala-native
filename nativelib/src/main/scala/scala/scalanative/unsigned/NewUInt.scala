package scala.scalanative
package unsigned

import scala.scalanative.runtime.intrinsic

final abstract class NewUInt private {
  def toByte: Byte
  def toShort: Short
  def toChar: Char
  def toInt: Int
  def toLong: Long
  def toFloat: Float
  def toDouble: Double

  def toUByte: UByte
  def toUShort: UShort
  def toUInt: NewUInt
  def toULong: ULong
  def toUSize: USize

  def unary_~ : NewUInt

  def <<(x: Int): NewUInt
  def <<(x: Long): NewUInt

  def >>>(x: Int): NewUInt
  def >>>(x: Long): NewUInt

  def >>(x: Int): NewUInt
  def >>(x: Long): NewUInt

  def ==(x: UByte): Boolean
  def ==(x: UShort): Boolean
  def ==(x: NewUInt): Boolean
  def ==(x: ULong): Boolean

  def !=(x: UByte): Boolean
  def !=(x: UShort): Boolean
  def !=(x: NewUInt): Boolean
  def !=(x: ULong): Boolean

  def <(x: UByte): Boolean
  def <(x: UShort): Boolean
  def <(x: NewUInt): Boolean
  def <(x: ULong): Boolean

  def <=(x: UByte): Boolean
  def <=(x: UShort): Boolean
  def <=(x: NewUInt): Boolean
  def <=(x: ULong): Boolean

  def >(x: UByte): Boolean
  def >(x: UShort): Boolean
  def >(x: NewUInt): Boolean
  def >(x: ULong): Boolean

  def >=(x: UByte): Boolean
  def >=(x: UShort): Boolean
  def >=(x: NewUInt): Boolean
  def >=(x: ULong): Boolean

  def |(x: UByte): NewUInt
  def |(x: UShort): NewUInt
  def |(x: NewUInt): NewUInt
  def |(x: ULong): ULong

  def &(x: UByte): NewUInt
  def &(x: UShort): NewUInt
  def &(x: NewUInt): NewUInt
  def &(x: ULong): ULong

  def ^(x: UByte): NewUInt
  def ^(x: UShort): NewUInt
  def ^(x: NewUInt): NewUInt
  def ^(x: ULong): ULong

  def +(x: UByte): NewUInt
  def +(x: UShort): NewUInt
  def +(x: NewUInt): NewUInt
  def +(x: ULong): ULong

  def -(x: UByte): NewUInt
  def -(x: UShort): NewUInt
  def -(x: NewUInt): NewUInt
  def -(x: ULong): ULong

  def *(x: UByte): NewUInt
  def *(x: UShort): NewUInt
  def *(x: NewUInt): NewUInt
  def *(x: ULong): ULong

  def /(x: UByte): NewUInt
  def /(x: UShort): NewUInt
  def /(x: NewUInt): NewUInt
  def /(x: ULong): ULong

  def %(x: UByte): NewUInt
  def %(x: UShort): NewUInt
  def %(x: NewUInt): NewUInt
  def %(x: ULong): ULong
  //  override def toString(): String
}

object NewUInt {
  def unsigned(v: Int): NewUInt = intrinsic

  // /** The smallest value representable as a UInt. */
  // final val MinValue

  // /** The largest value representable as a UInt. */
  // final val MaxValue

  // /** The String representation of the scala.UInt companion object. */
  // override def toString(): String

  // /** Language mandated coercions from UInt to "wider" types. */
  // import scala.language.implicitConversions
  // implicit def uint2ulong(x: NewUInt): ULong
}
