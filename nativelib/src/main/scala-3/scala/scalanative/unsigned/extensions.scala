package scala.scalanative.unsigned

import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsafe.CSize

/** Scala Native unsigned extensions to the standard Byte. */
extension (inline value: Byte) {
  inline def toUByte: UByte = unsignedOf(value)
  inline def toUShort: UShort = unsignedOf(value.toShort)
  inline def toUInt: UInt = unsignedOf(byteToUInt(value))
  inline def toULong: ULong = unsignedOf(byteToULong(value))
  inline def toUSize: CSize = unsignedOf(
    castIntToRawSizeUnsigned(byteToUInt(value))
  )
  inline def toCSize: CSize = toUSize
}

/** Scala Native unsigned extensions to the standard Short. */
extension (inline value: Short) {
  inline def toUByte: UByte = unsignedOf(value.toByte)
  inline def toUShort: UShort = unsignedOf(value)
  inline def toUInt: UInt = unsignedOf(shortToUInt(value))
  inline def toULong: ULong = unsignedOf(shortToULong(value))
  inline def toUSize: USize = unsignedOf(
    castIntToRawSizeUnsigned(shortToUInt((value)))
  )
  inline def toCSize: CSize = toUSize
}

/** Scala Native unsigned extensions to the standard Int. */
extension (inline value: Int) {
  inline def toUByte: UByte = unsignedOf(value.toByte)
  inline def toUShort: UShort = unsignedOf(value.toShort)
  inline def toUInt: UInt = unsignedOf(value)
  inline def toULong: ULong = unsignedOf(intToULong(value))
  inline def toUSize: USize = unsignedOf(castIntToRawSizeUnsigned(value))
  inline def toCSize: CSize = toUSize
}

/** Scala Native unsigned extensions to the standard Long. */
extension (inline value: Long) {
  inline def toUByte: UByte = unsignedOf(value.toByte)
  inline def toUShort: UShort = unsignedOf(value.toShort)
  inline def toUInt: UInt = unsignedOf(value.toInt)
  inline def toULong: ULong = unsignedOf(value)
  inline def toUSize: USize = unsignedOf(castLongToRawSize(value))
  inline def toCSize: CSize = toUSize
}
