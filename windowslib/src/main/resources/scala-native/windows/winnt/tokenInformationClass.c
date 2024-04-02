#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <winnt.h>
#include <sdkddkver.h>

int scalanative_tokenuser() { return TokenUser; }
int scalanative_tokengroups() { return TokenGroups; }
int scalanative_tokenprivileges() { return TokenPrivileges; }
int scalanative_tokenowner() { return TokenOwner; }
int scalanative_tokenprimarygroup() { return TokenPrimaryGroup; }
int scalanative_tokendefaultdacl() { return TokenDefaultDacl; }
int scalanative_tokensource() { return TokenSource; }
int scalanative_tokentype() { return TokenType; }
int scalanative_tokenimpersonationlevel() { return TokenImpersonationLevel; }
int scalanative_tokenstatistics() { return TokenStatistics; }
int scalanative_tokenrestrictedsids() { return TokenRestrictedSids; }
int scalanative_tokensessionid() { return TokenSessionId; }
int scalanative_tokengroupsandprivileges() { return TokenGroupsAndPrivileges; }
int scalanative_tokensessionreference() { return TokenSessionReference; }
int scalanative_tokensandboxinert() { return TokenSandBoxInert; }
int scalanative_tokenauditpolicy() { return TokenAuditPolicy; }
int scalanative_tokenorigin() { return TokenOrigin; }
int scalanative_tokenelevationtype() { return TokenElevationType; }
int scalanative_tokenlinkedtoken() { return TokenLinkedToken; }
int scalanative_tokenelevation() { return TokenElevation; }
int scalanative_tokenhasrestrictions() { return TokenHasRestrictions; }
int scalanative_tokenaccessinformation() { return TokenAccessInformation; }
int scalanative_tokenvirtualizationallowed() {
    return TokenVirtualizationAllowed;
}
int scalanative_tokenvirtualizationenabled() {
    return TokenVirtualizationEnabled;
}
int scalanative_tokenintegritylevel() { return TokenIntegrityLevel; }
int scalanative_tokenuiaccess() { return TokenUIAccess; }
int scalanative_tokenmandatorypolicy() { return TokenMandatoryPolicy; }
int scalanative_tokenlogonsid() { return TokenLogonSid; }
int scalanative_tokenisappcontainer() { return TokenIsAppContainer; }
int scalanative_tokencapabilities() { return TokenCapabilities; }
int scalanative_tokenappcontainersid() { return TokenAppContainerSid; }
int scalanative_tokenappcontainernumber() { return TokenAppContainerNumber; }
int scalanative_tokenuserclaimattributes() { return TokenUserClaimAttributes; }
int scalanative_tokendeviceclaimattributes() {
    return TokenDeviceClaimAttributes;
}
int scalanative_tokenrestricteduserclaimattributes() {
    return TokenRestrictedUserClaimAttributes;
}
int scalanative_tokenrestricteddeviceclaimattributes() {
    return TokenRestrictedDeviceClaimAttributes;
}
int scalanative_tokendevicegroups() { return TokenDeviceGroups; }
int scalanative_tokenrestricteddevicegroups() {
    return TokenRestrictedDeviceGroups;
}
int scalanative_tokensecurityattributes() { return TokenSecurityAttributes; }
int scalanative_tokenisrestricted() { return TokenIsRestricted; }
int scalanative_tokenprocesstrustlevel() { return TokenProcessTrustLevel; }
int scalanative_tokenprivatenamespace() { return TokenPrivateNameSpace; }

// The following enums exist only since some SDK version
// Since they're enums and not constants we cannot use prepropocessor
// definitions to check if they're defined. Instead we check if used
// SDK is the same or later then the one defined in sdkddkver.h
// Later versions of Windows SDK might contain additional enums, but it is
// not well documented when given enum value was added or removed.

#ifdef NTDDI_WIN10_RS1 // since 10.0.14393
int scalanative_tokensingletonattributes() { return TokenSingletonAttributes; }
#endif

#ifdef NTDDI_WIN10_RS3 // since 10.0.16299
int scalanative_tokenbnoisolation() { return TokenBnoIsolation; }
int scalanative_tokenchildprocessflags() { return TokenChildProcessFlags; }
#endif

int scalanative_maxtokeninfoclass() { return MaxTokenInfoClass; }

#endif // defined(_WIN32)
