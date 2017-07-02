#ifdef _WIN32
#include "os_win_grp.h"
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#include <string>
#include <vector>

namespace {
struct groupInternal {
    groupInternal() : gr_gid(-1) {}
    std::string gr_name; /** The name of the group. */
    unsigned int gr_gid; /** Numerical group ID. */
    std::vector<std::string>
        gr_mem; /** Pointer to a null-terminated array of character
pointers to member names. */
    std::vector<const char *> gr_memChars;
    group *getGrp() {
        grp.gr_gid = gr_gid;
        grp.gr_name = gr_name.data();
        gr_memChars.clear();
        gr_memChars.reserve(gr_mem.size());
        for (auto &n : gr_mem) {
            gr_memChars.push_back(n.data());
        }
        grp.gr_mem = gr_memChars.data();
        return &grp;
    }

  private:
    group grp;
};

std::vector<groupInternal> &groups() {
    static std::vector<groupInternal> db;
    return db;
}

groupInternal &getInternalGroup(gid_t idx) {
    auto &db = groups();
    if (idx >= db.size()) {
        const bool first = db.size() == 0;
        db.resize(idx + 1);
        if (first) {
            db[0].gr_name = "none";
            db[0].gr_gid = 0;
            db[0].gr_mem.push_back("anon");
        }
    }
    return db[idx];
}

groupInternal &findInternalGroup(const char *groupname) {
    auto &db = groups();
    for (auto &g : db) {
        if (g.gr_name == groupname) {
            return g;
        }
    }
    return db[0];
}

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
}

extern "C" struct group *getgrgid(gid_t gid) {
    return getInternalGroup(gid).getGrp();
}
extern "C" struct group *getgrnam(const char *groupname) {
    return findInternalGroup(groupname).getGrp();
}

#endif