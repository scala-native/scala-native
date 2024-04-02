#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

DWORD scalanative_generic_all() { return GENERIC_ALL; }

DWORD scalanative_generic_execute() { return FILE_GENERIC_EXECUTE; }
DWORD scalanative_execute() { return FILE_EXECUTE; }
DWORD scalanative_standard_rights_execute() { return STANDARD_RIGHTS_EXECUTE; }

DWORD scalanative_generic_read() { return FILE_GENERIC_READ; }
DWORD scalanative_read_attributes() { return FILE_READ_ATTRIBUTES; }
DWORD scalanative_read_data() { return FILE_READ_DATA; }
DWORD scalanative_read_ea() { return FILE_READ_EA; }
DWORD scalanative_standard_rights_read() { return STANDARD_RIGHTS_READ; }

DWORD scalanative_generic_write() { return FILE_GENERIC_WRITE; }
DWORD scalanative_append_data() { return FILE_APPEND_DATA; }
DWORD scalanative_write_attributes() { return FILE_WRITE_ATTRIBUTES; }
DWORD scalanative_write_data() { return FILE_WRITE_DATA; }
DWORD scalanative_write_ea() { return FILE_WRITE_EA; }
DWORD scalanative_standard_rights_write() { return STANDARD_RIGHTS_WRITE; }
DWORD scalanative_synchronize() { return SYNCHRONIZE; }

#endif // defined(Win32)
