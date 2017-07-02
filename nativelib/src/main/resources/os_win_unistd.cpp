#ifdef _WIN32
#include "os_win_unistd.h"
#include "os_win_dirent.h"
#include <exception>
#include <cstdio>
#include <../ucrt/corecrt_io.h>
#include <../ucrt/direct.h>
#include <../ucrt/fcntl.h>
#include "os_win_descriptor_guard.h"
#include "os_win_winsock2.h"

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>

//#define FILESYSTEM_VERBOSE

extern "C" mode_t getAccessMode(const char *path);
std::string formatSystemError(DWORD error);

std::string getFullPath(const char *fname, DWORD *attributesOut = nullptr) {
    std::string result;
    const int cSize = 4096;
    wchar_t buf[cSize] = L"";
    wchar_t **lppPart = {nullptr};
    char pathc[cSize];
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, fname, cSize);
    auto retval = GetFullPathNameW(pathw, cSize, buf, lppPart);

    if (retval != 0) {
        std::wstring wide(buf);

        if (attributesOut) {
            *attributesOut = GetFileAttributesW(wide.c_str());
        }

        wcstombs_s(&outLength, pathc, wide.data(), cSize);
        result.assign(pathc);

        if ((lppPart != nullptr) && (*lppPart != 0)) {
            // lppPart is file extension
        }
    }

    return result;
}

extern "C" int os_win_libc_remove(const char *fname) {
    const int cSize = 1024;
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, fname, cSize);

    DWORD dwAttrib = GetFileAttributesW(pathw);

    if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
        if (dwAttrib & FILE_ATTRIBUTE_DIRECTORY) {
            return RemoveDirectoryW(pathw) ? 0 : -1;
        } else {
            return DeleteFileW(pathw) ? 0 : -1;
        }
    }
    return -1;
}

extern "C" int os_win_unistd_symlink(char *path1, char *path2) {
    const int cSize = 1024;
    DWORD dwRet;
    wchar_t pathw1[cSize];
    wchar_t pathw2[cSize];
    size_t outLength1 = 0;
    size_t outLength2 = 0;
    mbstowcs_s(&outLength1, pathw1, cSize, path1, cSize);
    mbstowcs_s(&outLength2, pathw2, cSize, path2, cSize);

    DWORD dwAttrib = GetFileAttributesW(pathw1);
    if (!CreateSymbolicLinkW(pathw2, pathw1,
                             (dwAttrib != INVALID_FILE_ATTRIBUTES &&
                              (dwAttrib & FILE_ATTRIBUTE_DIRECTORY))
                                 ? SYMBOLIC_LINK_FLAG_DIRECTORY
                                 : 0)) {
        printf("Error: %s", formatSystemError(GetLastError()).c_str());
        return -1;
    }
    return 0;
}

extern "C" int os_win_unistd_symlinkat(char *path1, int fd, char *path2) {
    return 0;
}

extern "C" int os_win_unistd_link(char *oldpath, char *newpath) {
    const int cSize = 1024;
    DWORD dwRet;
    wchar_t pathw1[cSize];
    wchar_t pathw2[cSize];
    size_t outLength1 = 0;
    size_t outLength2 = 0;
    mbstowcs_s(&outLength1, pathw1, cSize, oldpath, cSize);
    mbstowcs_s(&outLength2, pathw2, cSize, newpath, cSize);

    if (!CreateHardLinkW(pathw2, pathw1, nullptr)) {
        printf("Error: %s", formatSystemError(GetLastError()).c_str());
        return -1;
    }
    return 0;
}

extern "C" int os_win_unistd_linkat(int fd1, char *path1, int fd2, char *path2,
                                    int flag) {
    throw std::exception("`linkat` not implemented.");
    return 0;
}

extern "C" int os_win_unistd_chown(char *path, uid_t owner, gid_t group) {
    throw std::exception("`chown` not implemented.");
    return 0;
}

extern "C" int scalanative_recv(int socket, void *buffer, size_t length,
                                int flags);
extern "C" int scalanative_send(int socket, void *buffer, size_t length,
                                int flags);

extern "C" const void *__imp_memchr(const void *s, int c, size_t n) {
    return memchr(s, c, n);
}
extern "C" int __imp_write(int fildes, void *buf, uint32_t nbyte) {
#ifdef FILESYSTEM_VERBOSE
    if (fildes > 2)
        printf("Write: %i, bytes = %i\n", fildes, nbyte);
#endif
    const auto result = descriptorGuard().get(fildes);
    if (result.type == DescriptorGuard::SOCKET) {
        scalanative_send(fildes, buf, nbyte, 0);
    } else if (result.type == DescriptorGuard::FILE) {
        switch (fildes) {
        case STDOUT_FILENO:
            return fwrite(buf, nbyte, 1, stdout);
        case STDERR_FILENO:
            return fwrite(buf, nbyte, 1, stderr);
        default:
            return _write(fildes, buf, nbyte);
        }
    }
    return -1;
}
extern "C" int __imp_read(int fildes, void *buf, uint32_t nbyte) {
#ifdef FILESYSTEM_VERBOSE
    if (fildes > 2)
        printf("Read: %i, bytes = %i\n", fildes, nbyte);
#endif
    const auto result = descriptorGuard().get(fildes);
    if (result.type == DescriptorGuard::SOCKET) {
        scalanative_recv(fildes, buf, nbyte, 0);
    } else if (result.type == DescriptorGuard::FILE) {
        return _read(fildes, buf, nbyte);
    }
    return -1;
}

extern "C" int pchmod(const char *path, mode_t mode);

extern "C" int __imp_open(const char *pathname, int flags, mode_t mode) {
    int fildes = -1;
    DWORD attributes;
    auto &db = descriptorGuard();
    std::string fullPath = getFullPath(pathname, &attributes);
    bool isFolder =
        (attributes != -1) && (attributes & FILE_ATTRIBUTE_DIRECTORY);
    DWORD winPermission = _S_IREAD | _S_IWRITE;
    errno_t err;
    if (isFolder && std::string(pathname) == ".") {
        err = 0;
        fildes = descriptorGuard().getEmpty();
    } else {
        const auto fileMode =
            (attributes != -1) ? getAccessMode(fullPath.data()) : mode;
        if (((flags & _O_WRONLY) || (flags & _O_RDWR)) &&
            !(fileMode & S_IWUSR)) {
            err = EACCES;
        } else {
            err = _sopen_s(&fildes, fullPath.data(), flags, _SH_DENYNO,
                           winPermission);
        }
    }

    if (err == 0) {
        // if ((flags & _O_APPEND)==0 && ((flags & _O_CREAT) || (flags &
        // _O_TEMPORARY))) {
        if (attributes == -1) {
            pchmod(pathname, mode);
        }
        if (fildes >= 0)
            descriptorGuard().openFile(fildes, std::string(fullPath), isFolder);
    }

#ifdef FILESYSTEM_VERBOSE
    printf("Open: %s, %x, %x, %i, err = %i\n", pathname, flags, mode, fildes,
           err);
#endif
    return fildes;
}
extern "C" int __imp_close(int fildes) {
#ifdef FILESYSTEM_VERBOSE
    printf("Close: %i\n", fildes);
#endif
    const auto result = descriptorGuard().close(fildes);
    if (result == DescriptorGuard::SOCKET) {
        return os_win_closesocket(fildes);
    } else if (result == DescriptorGuard::FILE) {
        return _close(fildes);
    } else if (result == DescriptorGuard::FOLDER) {
        return 0;
    } else
        return -1;
}

char *__imp_strerror(const char *strErrMsg) {
    static char buf[1024];
    _strerror_s(buf, 1024, strErrMsg);
    return buf;
}

extern "C" int os_win_unistd_access(const char *path, int amode) {
    if (path == 0 || strlen(path) == 0) {
        return -1;
    }

    mode_t mode = getAccessMode(path);

    if (amode == F_OK) {
        return mode != -1 ? 0 : -1;
    }

    if (((amode & R_OK) == R_OK) && ((mode & S_IRUSR) != S_IRUSR)) {
        return -1;
    }

    if (((amode & W_OK) == W_OK) && ((mode & S_IWUSR) != S_IWUSR)) {
        return -1;
    }

    if (((amode & X_OK) == X_OK) && ((mode & S_IXUSR) != S_IXUSR)) {
        return -1;
    }
    return 0;
}

extern "C" int os_win_unistd_sleep(uint32_t seconds) {
    Sleep(seconds * 1000);
    return 0;
}

extern "C" int os_win_unistd_usleep(uint32_t usecs) {
    Sleep(usecs);
    return 0;
}

extern "C" int os_win_unistd_unlink(const char *path) {
    int result = _unlink(path);

    if (result != 0) // if somebody keeps file open here we force close it
    {
        const auto fildes = descriptorGuard().getFile(getFullPath(path));
        __imp_close(fildes);
        result = _unlink(path);
    }

    return result;
}

extern "C" int os_win_unistd_readlink(const char *path, const char *buf,
                                      size_t bufsize) {
    throw std::exception("not implemented.");
    return 0;
}

extern "C" const char *os_win_unistd_getcwd(char *buf, size_t size) {
    return _getcwd(buf, size);
}

extern "C" int os_win_unistd_write(int fildes, void *buf, size_t nbyte) {
    return __imp_write(fildes, buf, nbyte);
}

extern "C" int os_win_unistd_read(int fildes, void *buf, size_t nbyte) {
    return __imp_read(fildes, buf, nbyte);
}

extern "C" int os_win_unistd_close(int fildes) { return __imp_close(fildes); }

extern "C" int os_win_unistd_fsync(int fildes) { return 0; }

extern "C" scalanative_off_t
os_win_unistd_lseek(int fildes, scalanative_off_t offset, int whence) {

    return static_cast<scalanative_off_t>(_lseeki64(fildes, offset, whence));
}

extern "C" int os_win_unistd_ftruncate(int fildes, scalanative_off_t length) {
    return _chsize_s(fildes, length);
}

extern "C" int os_win_unistd_truncate(const char *path,
                                      scalanative_off_t length) {
    auto fildes = __imp_open(path, O_APPEND | O_RDWR, 0x666);

    int result = -1;
    if (fildes >= 0) {
        result = os_win_unistd_ftruncate(fildes, length);
    } else {
        const auto fildes = descriptorGuard().getFile(getFullPath(path));
        result = os_win_unistd_ftruncate(fildes, length);
    }
    return result;
}

#endif