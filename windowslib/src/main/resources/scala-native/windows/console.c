#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

DWORD scalanative_win32_console_std_in_handle() { return STD_INPUT_HANDLE; }
DWORD scalanative_win32_console_std_out_handle() { return STD_OUTPUT_HANDLE; }
DWORD scalanative_win32_console_std_err_handle() { return STD_ERROR_HANDLE; }
#endif // defined(Win32)
