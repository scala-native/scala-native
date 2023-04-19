package scala.scalanative.windows

import scala.scalanative.unsafe.{Word => _, _}
import scala.scalanative.windows.HandleApi.Handle

@link("Advapi32")
@extern()
object SecurityBaseApi {
  import winnt.TokenInformationClass

  type AccessMask = DWord
  type SecurityDescriptorControl = Word
  type SecurityImpersonationLevel = CInt

  type SecurityDescriptor = CStruct7[
    Byte,
    Byte,
    SecurityDescriptorControl,
    SIDPtr,
    SIDPtr,
    ACLPtr,
    ACLPtr
  ]
  type GenericMapping = CStruct4[AccessMask, AccessMask, AccessMask, AccessMask]

  // Internal Windows structures, might have variable size and should not be modifed by the user
  type SIDPtr = Ptr[Byte]
  type ACLPtr = Ptr[Byte]
  type PrivilegeSetPtr = Ptr[Byte]

  def AccessCheck(
      securityDescriptor: Ptr[SecurityDescriptor],
      clientToken: Handle,
      desiredAccess: DWord,
      genericMapping: Ptr[GenericMapping],
      privilegeSet: PrivilegeSetPtr,
      privilegeSetLength: Ptr[DWord],
      grantedAccess: Ptr[DWord],
      accessStatus: Ptr[Boolean]
  ): DWord =
    extern

  def DuplicateToken(
      existingToken: Handle,
      impersonationLevel: CInt,
      duplicateTokenHandle: Ptr[Handle]
  ): Boolean = extern

  def FreeSid(sid: SIDPtr): Ptr[Byte] = extern
  def GetTokenInformation(
      handle: Handle,
      informationClass: TokenInformationClass,
      information: Ptr[Byte],
      informationLength: DWord,
      returnLength: Ptr[DWord]
  ): Boolean = extern

  def MapGenericMask(
      accessMask: Ptr[DWord],
      genericMapping: Ptr[GenericMapping]
  ): Unit = extern

  // SecurityImpersonationLevel enum
  @name("scalanative_securityanonymous")
  def SecurityAnonymous: SecurityImpersonationLevel = extern

  @name("scalanative_securityidentification")
  def SecurityIdentification: SecurityImpersonationLevel = extern

  @name("scalanative_securityimpersonation")
  def SecurityImpersonation: SecurityImpersonationLevel = extern

  @name("scalanative_securitydelegation")
  def SecurityDelegation: SecurityImpersonationLevel = extern

  // utils
  @name("scalanative_winnt_empty_priviliges_size")
  def emptyPriviligesSize: CSize = extern

}

object SecurityBaseApiOps {
  import SecurityBaseApi._
  implicit class SecurityDescriptorOps(ref: Ptr[SecurityDescriptor]) {
    def revision: Byte = ref._1
    def sbz1: Byte = ref._2
    def control: SecurityDescriptorControl = ref._3
    def owner: SIDPtr = ref._4
    def group: SIDPtr = ref._5
    def sAcl: ACLPtr = ref._6
    def dAcl: ACLPtr = ref._7

    def revision_=(v: Byte): Unit = ref._1 = v
    def sbz1_=(v: Byte): Unit = ref._2 = v
    def control_=(v: SecurityDescriptorControl): Unit = ref._3 = v
    def owner_=(v: SIDPtr): Unit = ref._4 = v
    def group_=(v: SIDPtr): Unit = ref._5 = v
    def sAcl_=(v: ACLPtr): Unit = ref._6 = v
    def dAcl_=(v: ACLPtr): Unit = ref._7 = v
  }

  implicit class GenericMappingOps(ref: Ptr[GenericMapping]) {
    def genericRead: AccessMask = ref._1
    def genericWrite: AccessMask = ref._2
    def genericExecute: AccessMask = ref._3
    def genericAll: AccessMask = ref._4

    def genericRead_=(v: AccessMask): Unit = ref._1 = v
    def genericWrite_=(v: AccessMask): Unit = ref._2 = v
    def genericExecute_=(v: AccessMask): Unit = ref._3 = v
    def genericAll_=(v: AccessMask): Unit = ref._4 = v
  }

}
