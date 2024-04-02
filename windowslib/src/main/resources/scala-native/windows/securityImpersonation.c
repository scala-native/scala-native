#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

DWORD scalanative_securityanonymous() { return SecurityAnonymous; }
DWORD scalanative_securityidentification() { return SecurityIdentification; }
DWORD scalanative_securityimpersonation() { return SecurityImpersonation; }
DWORD scalanative_securitydelegation() { return SecurityDelegation; }

#endif // defined(_WIN32)
