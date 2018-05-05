#ifdef _WIN32
#include <Windows.h>
#endif

int scalanative_platform_is_windows() {
#ifdef _WIN32
    return 1;
#else
    return 0;
#endif
}

char *scalanative_windows_get_user_lang() {
#ifdef _WIN32
    char *dest = malloc(9);
    GetLocaleInfo(LOCALE_USER_DEFAULT, LOCALE_SISO639LANGNAME, dest, 9);
    return dest;
#endif
    return "";
}

char *scalanative_windows_get_user_country() {
#ifdef _WIN32
    char *dest = malloc(9);
    GetLocaleInfo(LOCALE_USER_DEFAULT, LOCALE_SISO3166CTRYNAME, dest, 9);
    return dest;
#endif
    return "";
}

// See http://stackoverflow.com/a/4181991
int scalanative_little_endian() {
    int n = 1;
    return (*(char *)&n);
}
