package scala.scalanative
package posix

import scalanative.native.{UByte, UInt, ULong, UShort}

object inttypes {

  type uint8_t  = UByte
  type uint16_t = UShort
  type uint32_t = UInt
  type uint64_t = ULong

}
