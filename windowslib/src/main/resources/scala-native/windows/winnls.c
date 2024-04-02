#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include "windows.h"

PCWSTR scalanative_locale_name_invariant() { return LOCALE_NAME_INVARIANT; }
PCWSTR scalanative_locale_name_system_default() {
    return LOCALE_NAME_SYSTEM_DEFAULT;
}
PCWSTR scalanative_locale_name_user_default() {
    return LOCALE_NAME_USER_DEFAULT;
}

LCTYPE scalanative_locale_siso639langname() { return LOCALE_SISO639LANGNAME; }

LCTYPE scalanative_locale_siso639langname2() { return LOCALE_SISO639LANGNAME2; }

LCTYPE scalanative_locale_siso3166ctryname() { return LOCALE_SISO3166CTRYNAME; }

LCTYPE scalanative_locale_siso3166ctryname2() {
    return LOCALE_SISO3166CTRYNAME2;
}

#endif
