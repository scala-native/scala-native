#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include "Windows.h"

int scalanative_win32_LocaleName_Invariant() { return LOCALE_NAME_INVARIANT; }
int scalanative_win32_LocaleName_SystemDefault() {
    return LOCALE_NAME_SYSTEM_DEFAULT;
}
int scalanative_win32_LocaleName_UserDefault() {
    return LOCALE_NAME_USER_DEFAULT;
}

int scalanative_win32_Locale_SISO_LangName() { return LOCALE_SISO639LANGNAME; }

int scalanative_win32_Locale_SISO_LangName2() {
    return LOCALE_SISO639LANGNAME2;
}

int scalanative_win32_Locale_SISO_CountryName() {
    return LOCALE_SISO639LANGNAME;
}

int scalanative_win32_Locale_SISO_CountryName2() {
    return LOCALE_SISO639LANGNAME2;
}

#endif