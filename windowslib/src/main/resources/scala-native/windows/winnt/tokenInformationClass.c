#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <WinNT.h>

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
int scalanative_tokensingletonattributes() { return TokenSingletonAttributes; }
int scalanative_tokenbnoisolation() { return TokenBnoIsolation; }
int scalanative_tokenchildprocessflags() { return TokenChildProcessFlags; }
int scalanative_tokenislessprivilegedappcontainer() {
    return TokenIsLessPrivilegedAppContainer;
}
int scalanative_tokenissandboxed() { return TokenIsSandboxed; }
int scalanative_maxtokeninfoclass() { return MaxTokenInfoClass; }

#endif // defined(_WIN32)
