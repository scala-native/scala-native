package scala.scalanative.windows

import scala.scalanative.unsafe.*
import scala.scalanative.windows.SecurityBaseApi.*

package object winnt {
  type AccessRights = DWord
  type AccessToken = DWord
  type TokenInformationClass = DWord
  type SidAndAttributes = CStruct2[SIDPtr, DWord]
  type SidNameUse = CInt

  implicit class SidAndAttributesOps(ref: Ptr[SidAndAttributes]) {
    def sid: SIDPtr = ref._1
    def attributes: DWord = ref._2

    def size_=(v: SIDPtr) = ref._1 = v
    def attributes_=(v: DWord) = ref._2 = v
  }

}
