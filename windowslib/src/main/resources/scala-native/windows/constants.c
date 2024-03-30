#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

// Needed to find symbols from UCRT - Windows Universal C Runtime
#pragma comment(lib, "legacy_stdio_definitions.lib")

DWORD scalanative_infinite() { return INFINITE; }
LANGID scalanative_lang_user_default() { return LANG_USER_DEFAULT; }

#endif // defined(Win32)
