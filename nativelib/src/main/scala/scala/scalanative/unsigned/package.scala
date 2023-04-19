package scala.scalanative

import scala.language.experimental.macros

package object unsigned {

  /** Scala Native unsigned extensions to the standard Byte. */
  implicit class UnsignedRichByte(val value: Byte) extends AnyVal {
    @inline def toUByte: UByte = new UByte(value)
    @inline def toUShort: UShort = toUByte.toUShort
    @inline def toUInt: UInt = toUByte.toUInt
    @inline def toULong: ULong = toUByte.toULong
  }

  /** Scala Native unsigned extensions to the standard Short. */
  implicit class UnsignedRichShort(val value: Short) extends AnyVal {
    @inline def toUByte: UByte = toUShort.toUByte
    @inline def toUShort: UShort = new UShort(value)
    @inline def toUInt: UInt = toUShort.toUInt
    @inline def toULong: ULong = toUShort.toULong
  }

  /** Scala Native unsigned extensions to the standard Int. */
  implicit class UnsignedRichInt(val value: Int) extends AnyVal {
    @inline def toUByte: UByte = toUInt.toUByte
    @inline def toUShort: UShort = toUInt.toUShort
    @inline def toUInt: UInt = new UInt(value)
    @inline def toULong: ULong = toUInt.toULong
    @inline def toUSize: USize = toUInt.toUSize
  }

  /** Scala Native unsigned extensions to the standard Long. */
  implicit class UnsignedRichLong(val value: Long) extends AnyVal {
    @inline def toUByte: UByte = toULong.toUByte
    @inline def toUShort: UShort = toULong.toUShort
    @inline def toUInt: UInt = toULong.toUInt
    @inline def toULong: ULong = new ULong(value)
    @inline def toUSize: USize = toULong.toUSize
  }
}
