#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

DWORD scalanative_win32_security_impersonation_anonymous() {
    return SecurityAnonymous;
}
DWORD scalanative_win32_security_impersonation_identification() {
    return SecurityIdentification;
}
DWORD scalanative_win32_security_impersonation_impersonation() {
    return SecurityImpersonation;
}
DWORD scalanative_win32_security_impersonation_delegation() {
    return SecurityDelegation;
}

#endif // defined(_WIN32)
