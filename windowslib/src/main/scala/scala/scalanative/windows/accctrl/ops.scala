package scala.scalanative.windows.accctrl

import scala.scalanative.unsafe._
import scala.scalanative.windows._

import AclApi._
import SecurityBaseApi._
import WinBaseApi._

object ops {
  implicit class ExplicitAccessOps(ref: Ptr[ExplicitAccessW]) {
    def accessPermisions: DWord = ref._1
    def accessMode: AccessMode = ref._2
    def inheritence: DWord = ref._3
    def trustee: Ptr[TrusteeW] = ref.at4

    def accessPermisions_=(v: DWord): Unit = ref._1 = v
    def accessMode_=(v: AccessMode): Unit = ref._2 = v
    def inheritence_=(v: DWord): Unit = ref._3 = v
    def trustee_=(v: Ptr[TrusteeW]): Unit = !ref.at4 = v
  }

  implicit class TrusteeWOps(ref: Ptr[TrusteeW]) {
    def multipleTrustee: Ptr[TrusteeW] = ref._1.asInstanceOf[Ptr[TrusteeW]]
    def multipleTrusteeOperation: MultipleTruteeOperation = ref._2
    def trusteeForm: TrusteeForm = ref._3
    def trusteeType: TrusteeType = ref._4
    // union type
    def strName: CWString = ref._5.asInstanceOf[CWString]
    def sid: SIDPtr = ref._5.asInstanceOf[SIDPtr]
    def objectsAndSid: Ptr[ObjectsAndSid] =
      ref._5.asInstanceOf[Ptr[ObjectsAndSid]]
    def objectsAndName: Ptr[ObjectsAndNameW] =
      ref._5.asInstanceOf[Ptr[ObjectsAndNameW]]

    def multipleTrustee_=(v: Ptr[TrusteeW]): Unit = {
      ref._1 = v.asInstanceOf[Ptr[Byte]]
    }
    def multipleTrusteeOperation_=(v: MultipleTruteeOperation): Unit = {
      ref._2 = v
    }
    def trusteeForm_=(v: TrusteeForm): Unit = { ref._3 = v }
    def trusteeType_=(v: TrusteeType): Unit = { ref._4 = v }
    def strName_=(v: CWString): Unit = { ref._5 = v.asInstanceOf[Ptr[Byte]] }
    def sid_=(v: SIDPtr): Unit = { ref._5 = v.asInstanceOf[Ptr[Byte]] }
    def objectsAndSid_=(v: Ptr[ObjectsAndSid]): Unit = {
      ref._5 = v.asInstanceOf[Ptr[Byte]]
    }
    def objectsAndName_=(v: Ptr[ObjectsAndNameW]): Unit = {
      ref._5 = v.asInstanceOf[Ptr[Byte]]
    }
  }

  implicit class ObjectsAndSidOps(ref: Ptr[ObjectsAndSid]) {
    def objectsPresent: ObjectsPresent = ref._1
    def objectTypeGuid: Ptr[GUID] = ref.at2
    def inheritedObjectTypeGuid: Ptr[GUID] = ref.at3
    def sid: SIDPtr = ref._4

    def objectsPresent_=(v: ObjectsPresent): Unit = ref._1 = v
    def objectTypeGuid_=(v: Ptr[GUID]): Unit = !ref.at2 = v
    def inheritedObjectTypeGuid_=(v: Ptr[GUID]): Unit = !ref.at3 = v
    def sid_=(v: SIDPtr): Unit = ref._4 = v
  }

  implicit class ObjectsAndNameWOps(ref: Ptr[ObjectsAndNameW]) {
    def objectsPresent: ObjectsPresent = ref._1
    def objectType: SecurityObjectType = ref._2
    def objectTypeName: CWString = ref._3
    def inheritedObjectTypeName: CWString = ref._4
    def strName: CWString = ref._5

    def objectsPresent_=(v: ObjectsPresent): Unit = ref._1 = v
    def objectType_=(v: SecurityObjectType): Unit = ref._2 = v
    def objectTypeName_=(v: CWString): Unit = ref._3 = v
    def inheritedObjectTypeName_=(v: CWString): Unit = ref._4 = v
    def strName_=(v: CWString): Unit = ref._5 = v
  }
}
