#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

// Needed to find symbols from UCRT - Windows Universal C Runtime
#pragma comment(lib, "legacy_stdio_definitions.lib")

unsigned int scalanative_win32_file_max_path() { return MAX_PATH; }
HANDLE scalanative_win32_invalid_handle_value() { return INVALID_HANDLE_VALUE; }
DWORD scalanative_win32_invalid_file_attributes() {
    return INVALID_FILE_ATTRIBUTES;
}
DWORD scalanative_win32_infinite() { return INFINITE; }

size_t scalanative_win32_winnt_empty_priviliges_size() {
    PRIVILEGE_SET privileges = {0};
    return sizeof(privileges);
}

LCID scalanative_win32_datetime_locale_user_default() {
    return LOCALE_USER_DEFAULT;
}

LANGID scalanative_win32_default_language() { return LANG_USER_DEFAULT; }

#endif // defined(Win32)
