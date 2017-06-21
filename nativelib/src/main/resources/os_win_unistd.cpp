#ifdef _WIN32
#include "os_win_unistd.h"
#include "os_win_dirent.h"
#include <exception>
#include <cstdio>
#include <../ucrt/corecrt_io.h>
#include <../ucrt/direct.h>
#include "os_win_descriptor_guard.h"
#include "os_win_winsock2.h"

extern "C" mode_t getAccessMode(const char *path);

extern "C" int symlink(char *path1, char *path2) { return 0; }

extern "C" int symlinkat(char *path1, int fd, char *path2) { return 0; }

extern "C" int link(char *oldpath, char *newpath) { return 0; }

extern "C" int linkat(int fd1, char *path1, int fd2, char *path2, int flag) {
    return 0;
}

extern "C" int chown(char *path, uid_t owner, gid_t group) {
    throw std::exception("`chown` not implemented.");
    return 0;
}

extern "C" int scalanative_recv(int socket, void *buffer, size_t length,
                                int flags);
extern "C" int scalanative_send(int socket, void *buffer, size_t length,
                                int flags);

extern "C" int __imp_write(int fildes, void *buf, uint32_t nbyte) {
    // if (fildes>2) printf("Write: %i, bytes = %i\n", fildes, nbyte);
    const auto result = descriptorGuard().get(fildes);
    if (result.type == DescriptorGuard::SOCKET) {
        scalanative_send(fildes, buf, nbyte, 0);
    } else if (result.type == DescriptorGuard::FILE) {
        return _write(fildes, buf, nbyte);
    }
    return -1;
}
extern "C" int __imp_read(int fildes, void *buf, uint32_t nbyte) {
    // if (fildes>2) printf("Read: %i, bytes = %i\n", fildes, nbyte);
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
    errno_t err =
        _sopen_s(&fildes, pathname, flags, _SH_DENYNO, _S_IREAD | _S_IWRITE);
    pchmod(pathname, mode);
    if (fildes >= 0)
        descriptorGuard().openFile(fildes, pathname);
    // printf("Open: %s, %x, %x, %i, err = %i\n", pathname, flags, mode, fildes,
    // err);
    return fildes;
}
extern "C" int __imp_close(int fildes) {
    // printf("Close: %i\n", fildes);
    const auto result = descriptorGuard().close(fildes);
    if (result == DescriptorGuard::SOCKET) {
        return os_win_closesocket(fildes);
    } else if (result == DescriptorGuard::FILE) {
        return _close(fildes);
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

extern "C" int os_win_unistd_unlink(const char *path) { return _unlink(path); }

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

extern "C" int os_win_unistd_fsync(int fildes) {
    throw std::exception("not implemented.");
    return 0;
}

extern "C" off_t os_win_unistd_lseek(int fildes, off_t offset, int whence) {
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_ftruncate(int fildes, off_t length) {
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_truncate(const char *path, off_t length) {
    throw std::exception("not implemented.");
    return 0;
}

#endif