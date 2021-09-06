#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <WinNT.h>

int scalanative_win32_winnt_token_info_class_user() { return TokenUser; }
int scalanative_win32_winnt_token_info_class_groups() { return TokenGroups; }
int scalanative_win32_winnt_token_info_class_privileges() {
    return TokenPrivileges;
}
int scalanative_win32_winnt_token_info_class_owner() { return TokenOwner; }
int scalanative_win32_winnt_token_info_class_primarygroup() {
    return TokenPrimaryGroup;
}
int scalanative_win32_winnt_token_info_class_defaultdacl() {
    return TokenDefaultDacl;
}
int scalanative_win32_winnt_token_info_class_source() { return TokenSource; }
int scalanative_win32_winnt_token_info_class_type() { return TokenType; }
int scalanative_win32_winnt_token_info_class_impersonationlevel() {
    return TokenImpersonationLevel;
}
int scalanative_win32_winnt_token_info_class_statistics() {
    return TokenStatistics;
}
int scalanative_win32_winnt_token_info_class_restrictedsids() {
    return TokenRestrictedSids;
}
int scalanative_win32_winnt_token_info_class_sessionid() {
    return TokenSessionId;
}
int scalanative_win32_winnt_token_info_class_groupsandprivileges() {
    return TokenGroupsAndPrivileges;
}
int scalanative_win32_winnt_token_info_class_sessionreference() {
    return TokenSessionReference;
}
int scalanative_win32_winnt_token_info_class_sandboxinert() {
    return TokenSandBoxInert;
}
int scalanative_win32_winnt_token_info_class_auditpolicy() {
    return TokenAuditPolicy;
}
int scalanative_win32_winnt_token_info_class_origin() { return TokenOrigin; }
int scalanative_win32_winnt_token_info_class_elevationtype() {
    return TokenElevationType;
}
int scalanative_win32_winnt_token_info_class_linkedtoken() {
    return TokenLinkedToken;
}
int scalanative_win32_winnt_token_info_class_elevation() {
    return TokenElevation;
}
int scalanative_win32_winnt_token_info_class_hasrestrictions() {
    return TokenHasRestrictions;
}
int scalanative_win32_winnt_token_info_class_accessinformation() {
    return TokenAccessInformation;
}
int scalanative_win32_winnt_token_info_class_virtualizationallowed() {
    return TokenVirtualizationAllowed;
}
int scalanative_win32_winnt_token_info_class_virtualizationenabled() {
    return TokenVirtualizationEnabled;
}
int scalanative_win32_winnt_token_info_class_integritylevel() {
    return TokenIntegrityLevel;
}
int scalanative_win32_winnt_token_info_class_uiaccess() {
    return TokenUIAccess;
}
int scalanative_win32_winnt_token_info_class_mandatorypolicy() {
    return TokenMandatoryPolicy;
}
int scalanative_win32_winnt_token_info_class_logonsid() {
    return TokenLogonSid;
}
int scalanative_win32_winnt_token_info_class_isappcontainer() {
    return TokenIsAppContainer;
}
int scalanative_win32_winnt_token_info_class_capabilities() {
    return TokenCapabilities;
}
int scalanative_win32_winnt_token_info_class_appcontainersid() {
    return TokenAppContainerSid;
}
int scalanative_win32_winnt_token_info_class_appcontainernumber() {
    return TokenAppContainerNumber;
}
int scalanative_win32_winnt_token_info_class_userclaimattributes() {
    return TokenUserClaimAttributes;
}
int scalanative_win32_winnt_token_info_class_deviceclaimattributes() {
    return TokenDeviceClaimAttributes;
}
int scalanative_win32_winnt_token_info_class_restricteduserclaimattributes() {
    return TokenRestrictedUserClaimAttributes;
}
int scalanative_win32_winnt_token_info_class_restricteddeviceclaimattributes() {
    return TokenRestrictedDeviceClaimAttributes;
}
int scalanative_win32_winnt_token_info_class_devicegroups() {
    return TokenDeviceGroups;
}
int scalanative_win32_winnt_token_info_class_restricteddevicegroups() {
    return TokenRestrictedDeviceGroups;
}
int scalanative_win32_winnt_token_info_class_securityattributes() {
    return TokenSecurityAttributes;
}
int scalanative_win32_winnt_token_info_class_isrestricted() {
    return TokenIsRestricted;
}
int scalanative_win32_winnt_token_info_class_processtrustlevel() {
    return TokenProcessTrustLevel;
}
int scalanative_win32_winnt_token_info_class_privatenamespace() {
    return TokenPrivateNameSpace;
}
int scalanative_win32_winnt_token_info_class_singletonattributes() {
    return TokenSingletonAttributes;
}
int scalanative_win32_winnt_token_info_class_bnoisolation() {
    return TokenBnoIsolation;
}
int scalanative_win32_winnt_token_info_class_childprocessflags() {
    return TokenChildProcessFlags;
}
int scalanative_win32_winnt_token_info_class_islessprivilegedappcontainer() {
    return TokenIsLessPrivilegedAppContainer;
}
int scalanative_win32_winnt_token_info_class_issandboxed() {
    return TokenIsSandboxed;
}
int scalanative_win32_winnt_token_info_class_originatingprocesstrustlevel() {
    return TokenOriginatingProcessTrustLevel;
}
int scalanative_win32_winnt_token_info_class_infoclass_max() {
    return MaxTokenInfoClass;
}

#endif // defined(_WIN32)
