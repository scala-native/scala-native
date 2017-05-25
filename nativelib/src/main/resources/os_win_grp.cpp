#ifdef _WIN32
#include "os_win_grp.h"
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>

extern "C" long long getFileSID(const char *path) {
    /*PSID pSidOwner = NULL;
    PSECURITY_DESCRIPTOR pSD = NULL;
    const int cSize = 1024;
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, path, cSize);

    HANDLE hFile = CreateFileW(
        pathw,
        GENERIC_READ,
        FILE_SHARE_READ,
        NULL,
        OPEN_EXISTING,
        FILE_ATTRIBUTE_NORMAL,
        NULL);

    // Check GetLastError for CreateFile error code.
    if (hFile == INVALID_HANDLE_VALUE) {
        DWORD dwErrorCode = 0;

        dwErrorCode = GetLastError();
        printf("CreateFile error = %d\n", dwErrorCode);
        return -1;
    }

    // Get the owner SID of the file.
    DWORD dwRtnCode = GetSecurityInfo(
        hFile,
        SE_FILE_OBJECT,
        OWNER_SECURITY_INFORMATION,
        &pSidOwner,
        NULL,
        NULL,
        NULL,
        &pSD);

    // Check GetLastError for GetSecurityInfo error condition.
    if (dwRtnCode != ERROR_SUCCESS) {
        DWORD dwErrorCode = 0;

        dwErrorCode = GetLastError();
        printf("GetSecurityInfo error = %d\n", dwErrorCode);
        return -1;
    }
    return (long long)pSidOwner;*/
    return 0;
}

extern "C" struct group *getgrgid(gid_t gid) { return 0; }
extern "C" struct group *getgrnam(const char *groupname) { return 0; }

#endif