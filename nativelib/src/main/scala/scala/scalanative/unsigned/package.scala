package scala.scalanative

import scala.scalanative.runtime.Intrinsics._

package object unsigned {

  // Scala 3 inlined extensions have higher priority

  /** Scala Native unsigned extensions to the standard Byte. */
  implicit class UnsignedRichByte(val value: Byte) extends AnyVal {
    @inline def toUByte: UByte = unsignedOf(value)
    @inline def toUShort: UShort = unsignedOf(value.toShort)
    @inline def toUInt: UInt = unsignedOf(byteToUInt(value))
    @inline def toULong: ULong = unsignedOf(byteToULong(value))
  }

  /** Scala Native unsigned extensions to the standard Short. */
  implicit class UnsignedRichShort(val value: Short) extends AnyVal {
    @inline def toUByte: UByte = unsignedOf(value.toByte)
    @inline def toUShort: UShort = unsignedOf(value)
    @inline def toUInt: UInt = unsignedOf(shortToUInt(value))
    @inline def toULong: ULong = unsignedOf(shortToULong(value))
  }

  /** Scala Native unsigned extensions to the standard Int. */
  implicit class UnsignedRichInt(val value: Int) extends AnyVal {
    @inline def toUByte: UByte = unsignedOf(value.toByte)
    @inline def toUShort: UShort = unsignedOf(value.toShort)
    @inline def toUInt: UInt = unsignedOf(value)
    @inline def toULong: ULong = unsignedOf(intToULong(value))
    @inline def toUSize: USize = unsignedOf(castIntToRawSizeUnsigned(value))
  }

  /** Scala Native unsigned extensions to the standard Long. */
  implicit class UnsignedRichLong(val value: Long) extends AnyVal {
    @inline def toUByte: UByte = unsignedOf(value.toByte)
    @inline def toUShort: UShort = unsignedOf(value.toShort)
    @inline def toUInt: UInt = unsignedOf(value.toInt)
    @inline def toULong: ULong = unsignedOf(value)
    @inline def toUSize: USize = unsignedOf(castLongToRawSize(value))
  }
}
