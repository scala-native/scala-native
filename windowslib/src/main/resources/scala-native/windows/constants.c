#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

// Needed to find symbols from UCRT - Windows Universal C Runtime
#pragma comment(lib, "legacy_stdio_definitions.lib")

unsigned int scalanative_win32_file_max_path() { return MAX_PATH; }
HANDLE scalanative_win32_invalid_handle_value() { return INVALID_HANDLE_VALUE; }
LANGID scalanative_win32_default_language() { return LANG_USER_DEFAULT; }

#endif // defined(Win32)
