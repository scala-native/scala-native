#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <winioctl.h>
#include <stddef.h>
#include <string.h>

// User-mode layout of a symbolic-link REPARSE_DATA_BUFFER (ntifs.h).
typedef struct {
    ULONG tag;
    USHORT dataLength;
    USHORT reserved;
    USHORT substituteOffset;
    USHORT substituteLength;
    USHORT printOffset;
    USHORT printLength;
    ULONG flags;
    WCHAR path[1];
} SymbolicLinkReparseData;

int scalanative_read_symbolic_link(HANDLE handle, WCHAR *buffer,
                                   DWORD capacity) {
    union {
        ULONG alignment;
        BYTE bytes[MAXIMUM_REPARSE_DATA_BUFFER_SIZE];
    } storage;
    DWORD bytesReturned;
    if (!DeviceIoControl(handle, FSCTL_GET_REPARSE_POINT, NULL, 0,
                         storage.bytes, sizeof(storage.bytes), &bytesReturned,
                         NULL)) {
        return -1;
    }

    const SymbolicLinkReparseData *data =
        (const SymbolicLinkReparseData *)storage.bytes;
    const size_t headerSize = offsetof(SymbolicLinkReparseData, path);
    if (bytesReturned < headerSize || data->tag != IO_REPARSE_TAG_SYMLINK) {
        SetLastError(ERROR_NOT_A_REPARSE_POINT);
        return -1;
    }

    // Preserve the stored target, including relative paths and short names.
    // Resolving the handle's final path would follow links and reject dangling
    // targets, and may change path spelling.
    size_t offset =
        data->printLength ? data->printOffset : data->substituteOffset;
    size_t length =
        data->printLength ? data->printLength : data->substituteLength;
    if ((offset % sizeof(WCHAR)) != 0 || (length % sizeof(WCHAR)) != 0 ||
        headerSize + offset + length > bytesReturned) {
        SetLastError(ERROR_INVALID_REPARSE_DATA);
        return -1;
    }

    const WCHAR *target = (const WCHAR *)((const BYTE *)data->path + offset);
    size_t chars = length / sizeof(WCHAR);
    if (!data->printLength && !(data->flags & 1) && chars >= 4 &&
        target[0] == L'\\' && target[1] == L'?' && target[2] == L'?' &&
        target[3] == L'\\') {
        target += 4;
        chars -= 4;
        // NT UNC targets use \??\UNC\server\share.
        if (chars >= 4 && target[0] == L'U' && target[1] == L'N' &&
            target[2] == L'C' && target[3] == L'\\') {
            target += 2;
            chars -= 2;
            if (chars + 1 > capacity) {
                SetLastError(ERROR_INSUFFICIENT_BUFFER);
                return -1;
            }
            memcpy(buffer, target, chars * sizeof(WCHAR));
            buffer[0] = L'\\';
            buffer[chars] = L'\0';
            return (int)chars;
        }
    }
    if (chars + 1 > capacity) {
        SetLastError(ERROR_INSUFFICIENT_BUFFER);
        return -1;
    }
    memcpy(buffer, target, chars * sizeof(WCHAR));
    buffer[chars] = L'\0';
    return (int)chars;
}
#endif
