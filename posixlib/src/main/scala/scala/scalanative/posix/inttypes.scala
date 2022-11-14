package scala.scalanative
package posix

import scala.scalanative.unsafe._
import scala.scalanative.unsigned.{UByte, UInt, ULong, UShort}

@extern object inttypes extends inttypes

@extern trait inttypes extends libc.inttypes {
  // should be in stdint, optional in C stdint
  type uint8_t = UByte
  type uint16_t = UShort
  type uint32_t = UInt
  type uint64_t = ULong
}
