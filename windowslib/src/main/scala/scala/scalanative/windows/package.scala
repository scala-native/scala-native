package scala.scalanative

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

package object windows {
  type Word      = UShort //uint_16
  type DWord     = UInt   //uint_32
  type DWordLong = ULong  //uint_64
  /* Actually large integers are union types with size of 64-bits
    When compiler does not support large integer types it allows to store
    low and hight parts of number in its structure fields. In all other cases
    its recommended to use its QuadPart field directly.
   */
  type LargeInteger  = Long
  type ULargeInteger = ULong
  type WChar         = CChar16
  type CWString      = Ptr[WChar] // In Windows wide string are always encoded using UTF-16LE
  type CTString      = CWString // if UNICODE is defined equals to CWString, otherwise its CString

  type SecurityAttributes = CStruct3[DWord, Ptr[Byte], Boolean]
  implicit class SecurityAttributesOps(val ref: Ptr[SecurityAttributes])
      extends AnyVal {
    def length: DWord                 = ref._1
    def securityDescriptor: Ptr[Byte] = ref._2
    def inheritHandle: Boolean        = ref._3

    def length_=(v: DWord): Unit                 = ref._1 = v
    def securityDescriptor_=(v: Ptr[Byte]): Unit = ref._2 = v
    def inheritHandle_=(v: Boolean): Unit        = ref._3 = v
  }

}
