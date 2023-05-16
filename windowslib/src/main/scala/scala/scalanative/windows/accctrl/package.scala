package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._

import AclApi._
import SecurityBaseApi._

package object accctrl {
  type AccessMode = CInt
  type MultipleTruteeOperation = CInt
  type TrusteeForm = CInt
  type TrusteeType = CInt
  type ObjectsPresent = DWord
  type ExplicitAccessW = CStruct4[DWord, AccessMode, DWord, TrusteeW]
  type TrusteeW = CStruct6[
    Ptr[Byte],
    MultipleTruteeOperation,
    TrusteeForm,
    TrusteeType,
    Ptr[Byte],
    CWString
  ]
  type GUID = CStruct4[UInt, UShort, UShort, CArray[UByte, Nat._8]]
  type ObjectsAndSid = CStruct4[ObjectsPresent, GUID, GUID, SIDPtr]
  type ObjectsAndNameW =
    CStruct5[ObjectsPresent, SecurityObjectType, CWString, CWString, CWString]

  // ObjectsPresents
  final val ACE_OBJECT_TYPE_PRESENT = 0x01.toUInt
  final val ACE_INHERITED_OBJECT_TYPE_PRESENT = 0x02.toUInt

  // InheritFlags
  final val OBJECT_INHERIT_ACE = 0x01.toUInt
  final val CONTAINER_INHERIT_ACE = 0x02.toUInt
  final val NO_PROPAGATE_INHERIT_ACE = 0x04.toUInt
  final val INHERIT_ONLY_ACE = 0x08.toUInt
  final val INHERITED_ACE = 0x10.toUInt
  final val VALID_INHERIT_FLAGS = 0x1f.toUInt
}
