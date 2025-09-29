package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern()
object AclApi {
  import accctrl._

  import SecurityBaseApi._
  import WinBaseApi._

  type SecurityObjectType = CInt

  def SetEntriesInAclW(
      countOfExplicitEntries: ULong,
      listOfExplicitEntries: Ptr[ExplicitAccessW],
      oldAcl: ACLPtr,
      newAcl: Ptr[ACLPtr]
  ): DWord = extern
  def GetNamedSecurityInfoA(
      objectName: CString,
      objectType: SecurityObjectType,
      securityInfo: SecurityInformation,
      sidOwner: Ptr[SIDPtr],
      sidGroup: Ptr[SIDPtr],
      dacl: Ptr[ACLPtr],
      sacl: Ptr[ACLPtr],
      securityDescriptor: Ptr[Ptr[SecurityDescriptor]]
  ): DWord = extern

  def GetNamedSecurityInfoW(
      objectName: CWString,
      objectType: SecurityObjectType,
      securityInfo: SecurityInformation,
      sidOwner: Ptr[SIDPtr],
      sidGroup: Ptr[SIDPtr],
      dacl: Ptr[ACLPtr],
      sacl: Ptr[ACLPtr],
      securityDescriptor: Ptr[Ptr[SecurityDescriptor]]
  ): DWord = extern

  def SetNamedSecurityInfoA(
      objectName: CString,
      objectType: SecurityObjectType,
      securityInfo: SecurityInformation,
      sidOwner: SIDPtr,
      sidGroup: SIDPtr,
      dacl: ACLPtr,
      sacl: ACLPtr
  ): DWord = extern

  def SetNamedSecurityInfoW(
      objectName: CWString,
      objectType: SecurityObjectType,
      securityInfo: SecurityInformation,
      sidOwner: SIDPtr,
      sidGroup: SIDPtr,
      dacl: ACLPtr,
      sacl: ACLPtr
  ): DWord = extern

  // SecurityObjectType enum
  @name("scalanative_se_unknown_object_type")
  def SE_UNKNOWN_OBJECT_TYPE: SecurityObjectType = extern
  @name("scalanative_se_file_object")
  def SE_FILE_OBJECT: SecurityObjectType = extern
  @name("scalanative_se_service")
  def SE_SERVICE: SecurityObjectType = extern
  @name("scalanative_se_printer")
  def SE_PRINTER: SecurityObjectType = extern
  @name("scalanative_se_registry_key")
  def SE_REGISTRY_KEY: SecurityObjectType = extern
  @name("scalanative_se_lmshare")
  def SE_LMSHARE: SecurityObjectType = extern
  @name("scalanative_se_kernel_object")
  def SE_KERNEL_OBJECT: SecurityObjectType = extern
  @name("scalanative_se_window_object")
  def SE_WINDOW_OBJECT: SecurityObjectType = extern
  @name("scalanative_se_ds_object")
  def SE_DS_OBJECT: SecurityObjectType = extern
  @name("scalanative_se_ds_object_all")
  def SE_DS_OBJECT_ALL: SecurityObjectType = extern
  @name("scalanative_se_provider_defined_object")
  def SE_PROVIDER_DEFINED_OBJECT: SecurityObjectType = extern
  @name("scalanative_se_wmiguid_object")
  def SE_WMIGUID_OBJECT: SecurityObjectType = extern
  @name("scalanative_se_registry_wow64_32key")
  def SE_REGISTRY_WOW64_32KEY: SecurityObjectType = extern
  @name("scalanative_se_registry_wow64_64key")
  def SE_REGISTRY_WOW64_64KEY: SecurityObjectType = extern
}
