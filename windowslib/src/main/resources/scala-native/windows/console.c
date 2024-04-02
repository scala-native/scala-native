#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

DWORD scalanative_std_input_handle() { return STD_INPUT_HANDLE; }
DWORD scalanative_std_output_handle() { return STD_OUTPUT_HANDLE; }
DWORD scalanative_std_error_handle() { return STD_ERROR_HANDLE; }
#endif // defined(Win32)
