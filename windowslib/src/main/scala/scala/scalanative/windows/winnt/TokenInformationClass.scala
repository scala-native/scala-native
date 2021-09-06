package scala.scalanative.windows.winnt

import scalanative.unsafe._
import scalanative.windows.DWord

@link("Advapi32")
@extern
object TokenInformationClass {
  @name("scalanative_win32_winnt_token_info_class_user")
  def TokenUser: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_groups")
  def TokenGroups: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_privileges")
  def TokenPrivileges: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_owner")
  def TokenOwner: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_primarygroup")
  def TokenPrimaryGroup: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_defaultdacl")
  def TokenDefaultDacl: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_source")
  def TokenSource: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_type")
  def TokenTokenInformationClass: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_impersonationlevel")
  def TokenImpersonationLevel: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_statistics")
  def TokenStatistics: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_restrictedsids")
  def TokenRestrictedSids: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_sessionid")
  def TokenSessionId: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_groupsandprivileges")
  def TokenGroupsAndPrivileges: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_sessionreference")
  def TokenSessionReference: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_sandboxinert")
  def TokenSandBoxInert: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_auditpolicy")
  def TokenAuditPolicy: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_origin")
  def TokenOrigin: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_elevationtype")
  def TokenElevationTokenInformationClass: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_linkedtoken")
  def TokenLinkedToken: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_elevation")
  def TokenElevation: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_hasrestrictions")
  def TokenHasRestrictions: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_accessinformation")
  def TokenAccessInformation: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_virtualizationallowed")
  def TokenVirtualizationAllowed: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_virtualizationenabled")
  def TokenVirtualizationEnabled: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_integritylevel")
  def TokenIntegrityLevel: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_uiaccess")
  def TokenUIAccess: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_mandatorypolicy")
  def TokenMandatoryPolicy: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_logonsid")
  def TokenLogonSid: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_isappcontainer")
  def TokenIsAppContainer: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_capabilities")
  def TokenCapabilities: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_appcontainersid")
  def TokenAppContainerSid: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_appcontainernumber")
  def TokenAppContainerNumber: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_userclaimattributes")
  def TokenUserClaimAttributes: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_deviceclaimattributes")
  def TokenDeviceClaimAttributes: TokenInformationClass = extern

  @name(
    "scalanative_win32_winnt_token_info_class_restricteduserclaimattributes"
  )
  def TokenRestrictedUserClaimAttributes: TokenInformationClass = extern

  @name(
    "scalanative_win32_winnt_token_info_class_restricteddeviceclaimattributes"
  )
  def TokenRestrictedDeviceClaimAttributes: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_devicegroups")
  def TokenDeviceGroups: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_restricteddevicegroups")
  def TokenRestrictedDeviceGroups: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_securityattributes")
  def TokenSecurityAttributes: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_isrestricted")
  def TokenIsRestricted: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_processtrustlevel")
  def TokenProcessTrustLevel: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_privatenamespace")
  def TokenPrivateNameSpace: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_singletonattributes")
  def TokenSingletonAttributes: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_bnoisolation")
  def TokenBnoIsolation: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_childprocessflags")
  def TokenChildProcessFlags: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_islessprivilegedappcontainer")
  def TokenIsLessPrivilegedAppContainer: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_issandboxed")
  def TokenIsSandboxed: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_originatingprocesstrustlevel")
  def TokenOriginatingProcessTrustLevel: TokenInformationClass = extern

  @name("scalanative_win32_winnt_token_info_class_infoclass_max")
  def MaxTokenInfoClass: TokenInformationClass = extern

}
