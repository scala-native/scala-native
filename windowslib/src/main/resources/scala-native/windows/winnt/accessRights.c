#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

DWORD scalanative_winnt_access_rights_file_generic_all() { return GENERIC_ALL; }

DWORD scalanative_winnt_access_rights_file_generic_execute() {
    return FILE_GENERIC_EXECUTE;
}
DWORD scalanative_winnt_access_rights_file_execute() { return FILE_EXECUTE; }
DWORD scalanative_winnt_access_rights_standard_rights_execute() {
    return STANDARD_RIGHTS_EXECUTE;
}

DWORD scalanative_winnt_access_rights_file_generic_read() {
    return FILE_GENERIC_READ;
}
DWORD scalanative_winnt_access_rights_file_read_attributes() {
    return FILE_READ_ATTRIBUTES;
}
DWORD scalanative_winnt_access_rights_file_read_data() {
    return FILE_READ_DATA;
}
DWORD scalanative_winnt_access_rights_file_read_ea() { return FILE_READ_EA; }
DWORD scalanative_winnt_access_rights_standard_rights_read() {
    return STANDARD_RIGHTS_READ;
}

DWORD scalanative_winnt_access_rights_file_generic_write() {
    return FILE_GENERIC_WRITE;
}
DWORD scalanative_winnt_access_rights_file_append_data() {
    return FILE_APPEND_DATA;
}
DWORD scalanative_winnt_access_rights_file_write_attributes() {
    return FILE_WRITE_ATTRIBUTES;
}
DWORD scalanative_winnt_access_rights_file_write_data() {
    return FILE_WRITE_DATA;
}
DWORD scalanative_winnt_access_rights_file_write_ea() { return FILE_WRITE_EA; }
DWORD scalanative_winnt_access_rights_standard_rights_write() {
    return STANDARD_RIGHTS_WRITE;
}
DWORD scalanative_winnt_access_rights_synchronize() { return SYNCHRONIZE; }

#endif // defined(Win32)