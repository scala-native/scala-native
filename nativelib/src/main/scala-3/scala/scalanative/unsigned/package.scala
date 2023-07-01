package scala.scalanative

import scala.scalanative.runtime.Intrinsics._

package object unsigned {

  /** Scala Native unsigned extensions to the standard Byte. */
  extension (inline value: Byte) {
    inline def toUByte: UByte = unsignedOf(value)
    inline def toUShort: UShort = unsignedOf(value.toShort)
    inline def toUInt: UInt = unsignedOf(byteToUInt(value))
    inline def toULong: ULong = unsignedOf(byteToULong(value))
  }

  /** Scala Native unsigned extensions to the standard Short. */
  extension (inline value: Short) {
    inline def toUByte: UByte = unsignedOf(value.toByte)
    inline def toUShort: UShort = unsignedOf(value)
    inline def toUInt: UInt = unsignedOf(shortToUInt(value))
    inline def toULong: ULong = unsignedOf(shortToULong(value))
  }

  /** Scala Native unsigned extensions to the standard Int. */
  extension (inline value: Int) {
    inline def toUByte: UByte = unsignedOf(value.toByte)
    inline def toUShort: UShort = unsignedOf(value.toShort)
    inline def toUInt: UInt = unsignedOf(value)
    inline def toULong: ULong = unsignedOf(intToULong(value))
    inline def toUSize: USize = unsignedOf(castIntToRawSizeUnsigned(value))
  }

  /** Scala Native unsigned extensions to the standard Long. */
  extension (inline value: Long) {
    inline def toUByte: UByte = unsignedOf(value.toByte)
    inline def toUShort: UShort = unsignedOf(value.toShort)
    inline def toUInt: UInt = unsignedOf(value.toInt)
    inline def toULong: ULong = unsignedOf(value)
    inline def toUSize: USize = unsignedOf(castLongToRawSize(value))
  }
}
