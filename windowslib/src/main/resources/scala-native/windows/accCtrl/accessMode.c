#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <accctrl.h>

int scalanative_not_used_access() { return NOT_USED_ACCESS; }
int scalanative_grant_access() { return GRANT_ACCESS; }
int scalanative_set_access() { return SET_ACCESS; }
int scalanative_deny_access() { return DENY_ACCESS; }
int scalanative_revoke_access() { return REVOKE_ACCESS; }
int scalanative_set_audit_success() { return SET_AUDIT_SUCCESS; }
int scalanative_set_audit_failure() { return SET_AUDIT_FAILURE; }

#endif // defined(_WIN32)
