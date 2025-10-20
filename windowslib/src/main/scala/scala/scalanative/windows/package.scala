package scala.scalanative

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

package object windows {
  type Word = UShort // uint_16
  type DWord = UInt // uint_32
  type DWordLong = ULong // uint_64
  /* Actually large integers are union types with size of 64-bits
   * When the compiler does not support large integer types it allows to store
   * low and high parts of number in its structure fields. In all other cases
   * it is recommended to use its QuadPart field directly.
   */
  type LargeInteger = Long
  type ULargeInteger = ULong
  type WChar = CChar16
  type CWString =
    Ptr[WChar] // In Windows wide string are always encoded using UTF-16LE
  type CTString =
    CWString // if UNICODE is defined equals to CWString, otherwise its CString

  @extern
  object Constants {
    @name("scalanative_infinite")
    def Infinite: DWord = extern
  }

}
