package scala.scalanative.windows.winnt

import scalanative.unsafe._

@link("Advapi32")
@extern
object TokenInformationClass {
  @name("scalanative_tokenuser")
  def TokenUser: TokenInformationClass = extern

  @name("scalanative_tokengroups")
  def TokenGroups: TokenInformationClass = extern

  @name("scalanative_tokenprivileges")
  def TokenPrivileges: TokenInformationClass = extern

  @name("scalanative_tokenowner")
  def TokenOwner: TokenInformationClass = extern

  @name("scalanative_tokenprimarygroup")
  def TokenPrimaryGroup: TokenInformationClass = extern

  @name("scalanative_tokendefaultdacl")
  def TokenDefaultDacl: TokenInformationClass = extern

  @name("scalanative_tokensource")
  def TokenSource: TokenInformationClass = extern

  @name("scalanative_tokentype")
  def TokenTokenInformationClass: TokenInformationClass = extern

  @name("scalanative_tokenimpersonationlevel")
  def TokenImpersonationLevel: TokenInformationClass = extern

  @name("scalanative_tokenstatistics")
  def TokenStatistics: TokenInformationClass = extern

  @name("scalanative_tokenrestrictedsids")
  def TokenRestrictedSids: TokenInformationClass = extern

  @name("scalanative_tokensessionid")
  def TokenSessionId: TokenInformationClass = extern

  @name("scalanative_tokengroupsandprivileges")
  def TokenGroupsAndPrivileges: TokenInformationClass = extern

  @name("scalanative_tokensessionreference")
  def TokenSessionReference: TokenInformationClass = extern

  @name("scalanative_tokensandboxinert")
  def TokenSandBoxInert: TokenInformationClass = extern

  @name("scalanative_tokenauditpolicy")
  def TokenAuditPolicy: TokenInformationClass = extern

  @name("scalanative_tokenorigin")
  def TokenOrigin: TokenInformationClass = extern

  @name("scalanative_tokenelevationtype")
  def TokenElevationTokenInformationClass: TokenInformationClass = extern

  @name("scalanative_tokenlinkedtoken")
  def TokenLinkedToken: TokenInformationClass = extern

  @name("scalanative_tokenelevation")
  def TokenElevation: TokenInformationClass = extern

  @name("scalanative_tokenhasrestrictions")
  def TokenHasRestrictions: TokenInformationClass = extern

  @name("scalanative_tokenaccessinformation")
  def TokenAccessInformation: TokenInformationClass = extern

  @name("scalanative_tokenvirtualizationallowed")
  def TokenVirtualizationAllowed: TokenInformationClass = extern

  @name("scalanative_tokenvirtualizationenabled")
  def TokenVirtualizationEnabled: TokenInformationClass = extern

  @name("scalanative_tokenintegritylevel")
  def TokenIntegrityLevel: TokenInformationClass = extern

  @name("scalanative_tokenuiaccess")
  def TokenUIAccess: TokenInformationClass = extern

  @name("scalanative_tokenmandatorypolicy")
  def TokenMandatoryPolicy: TokenInformationClass = extern

  @name("scalanative_tokenlogonsid")
  def TokenLogonSid: TokenInformationClass = extern

  @name("scalanative_tokenisappcontainer")
  def TokenIsAppContainer: TokenInformationClass = extern

  @name("scalanative_tokencapabilities")
  def TokenCapabilities: TokenInformationClass = extern

  @name("scalanative_tokenappcontainersid")
  def TokenAppContainerSid: TokenInformationClass = extern

  @name("scalanative_tokenappcontainernumber")
  def TokenAppContainerNumber: TokenInformationClass = extern

  @name("scalanative_tokenuserclaimattributes")
  def TokenUserClaimAttributes: TokenInformationClass = extern

  @name("scalanative_tokendeviceclaimattributes")
  def TokenDeviceClaimAttributes: TokenInformationClass = extern

  @name(
    "scalanative_tokenrestricteduserclaimattributes"
  )
  def TokenRestrictedUserClaimAttributes: TokenInformationClass = extern

  @name(
    "scalanative_tokenrestricteddeviceclaimattributes"
  )
  def TokenRestrictedDeviceClaimAttributes: TokenInformationClass = extern

  @name("scalanative_tokendevicegroups")
  def TokenDeviceGroups: TokenInformationClass = extern

  @name("scalanative_tokenrestricteddevicegroups")
  def TokenRestrictedDeviceGroups: TokenInformationClass = extern

  @name("scalanative_tokensecurityattributes")
  def TokenSecurityAttributes: TokenInformationClass = extern

  @name("scalanative_tokenisrestricted")
  def TokenIsRestricted: TokenInformationClass = extern

  @name("scalanative_tokenprocesstrustlevel")
  def TokenProcessTrustLevel: TokenInformationClass = extern

  @name("scalanative_tokenprivatenamespace")
  def TokenPrivateNameSpace: TokenInformationClass = extern

  @name("scalanative_tokensingletonattributes")
  def TokenSingletonAttributes: TokenInformationClass = extern

  @name("scalanative_tokenbnoisolation")
  def TokenBnoIsolation: TokenInformationClass = extern

  @name("scalanative_tokenchildprocessflags")
  def TokenChildProcessFlags: TokenInformationClass = extern

  @name("scalanative_maxtokeninfoclass")
  def MaxTokenInfoClass: TokenInformationClass = extern

}
