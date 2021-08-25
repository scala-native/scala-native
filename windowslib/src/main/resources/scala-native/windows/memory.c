#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#import <windows.h>

DWORD scalanative_file_map_all_access() { FILE_MAP_ALL_ACCESS }
DWORD scalanative_file_map_read() { FILE_MAP_READ }
DWORD scalanative_file_map_write() { FILE_MAP_WRITE }
DWORD scalanative_file_map_copy() { FILE_MAP_COPY }
DWORD scalanative_file_map_execute() { FILE_MAP_EXECUTE }
DWORD scalanative_file_map_large_pages() { FILE_MAP_LARGE_PAGES }
DWORD scalanative_file_map_targets_invalid() { FILE_MAP_TARGETS_INVALID }

#endif // defined(Win32)