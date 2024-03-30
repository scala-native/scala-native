#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <winbase.h>

DWORD scalanative_file_map_all_access() { return FILE_MAP_ALL_ACCESS; }
DWORD scalanative_file_map_read() { return FILE_MAP_READ; }
DWORD scalanative_file_map_write() { return FILE_MAP_WRITE; }
DWORD scalanative_file_map_copy() { return FILE_MAP_COPY; }
DWORD scalanative_file_map_execute() { return FILE_MAP_EXECUTE; }
DWORD scalanative_file_map_large_pages() { return FILE_MAP_LARGE_PAGES; }
DWORD scalanative_file_map_targets_invalid() {
    return FILE_MAP_TARGETS_INVALID;
}

#endif // defined(_WIN32)