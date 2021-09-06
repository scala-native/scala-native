#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#pragma comment(lib, "Advapi32.lib")

BOOL scalanative_win32_winnt_setupUsersGroupSid(PSID *ref) {
    SID_IDENTIFIER_AUTHORITY authNt = SECURITY_NT_AUTHORITY;
    return AllocateAndInitializeSid(&authNt, 2, SECURITY_BUILTIN_DOMAIN_RID,
                                    DOMAIN_ALIAS_RID_USERS, 0, 0, 0, 0, 0, 0,
                                    ref);
}
#endif // defined(_WIN32)
