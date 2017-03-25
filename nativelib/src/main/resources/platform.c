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

char* scalanative_windows_get_user_lang() {
#ifdef _WIN32
	char* dest = malloc(3);
	GetLocaleInfo(LOCALE_USER_DEFAULT, LOCALE_SISO639LANGNAME, dest, 3);
	return dest;
#endif
	return "";
}
