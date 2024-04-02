#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <accctrl.h>

int scalanative_trustee_is_unknown() { return TRUSTEE_IS_UNKNOWN; }
int scalanative_trustee_is_user() { return TRUSTEE_IS_USER; }
int scalanative_trustee_is_group() { return TRUSTEE_IS_GROUP; }
int scalanative_trustee_is_domain() { return TRUSTEE_IS_DOMAIN; }
int scalanative_trustee_is_alias() { return TRUSTEE_IS_ALIAS; }
int scalanative_trustee_is_well_known_group() {
    return TRUSTEE_IS_WELL_KNOWN_GROUP;
}
int scalanative_trustee_is_deleted() { return TRUSTEE_IS_DELETED; }
int scalanative_trustee_is_invalid() { return TRUSTEE_IS_INVALID; }
int scalanative_trustee_is_computer() { return TRUSTEE_IS_COMPUTER; }

#endif // defined(_WIN32)
