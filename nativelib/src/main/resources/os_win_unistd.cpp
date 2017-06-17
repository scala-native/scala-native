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

extern "C" int os_win_unistd_access(const char *path, int amode)
{
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

extern "C" int os_win_unistd_sleep(uint32_t seconds)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_usleep(uint32_t usecs)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_unlink(const char* path)
{
    return _unlink(path);
}

extern "C" int os_win_unistd_readlink(const char* path, const char* buf, size_t bufsize)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" const char* os_win_unistd_getcwd(char* buf, size_t size)
{
    return _getcwd(buf, size);
}

extern "C" int os_win_unistd_write(int fildes, void *buf, size_t nbyte) {
    switch (fildes) {
    case 1: {
        fwrite(buf, nbyte, 1, stdout);
        return nbyte;
    }
    case 2: {
        fwrite(buf, nbyte, 1, stderr);
        return nbyte;
    }
    default:
        return _write(fildes, buf, nbyte);
        break;
    }
    return 0;
}

extern "C" int os_win_unistd_read(int fildes, void* buf, size_t nbyte)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_close(int fildes)
{
    if (descriptorGuard().closeIfSocket(fildes))
    {
        return os_win_closesocket(fildes);
    }
    else
    {
        return _close(fildes);
    }
}

extern "C" int os_win_unistd_fsync(int fildes)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" off_t os_win_unistd_lseek(int fildes, off_t offset, int whence)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_ftruncate(int fildes, off_t length)
{
    throw std::exception("not implemented.");
    return 0;
}

extern "C" int os_win_unistd_truncate(const char* path, off_t length)
{
    throw std::exception("not implemented.");
    return 0;
}

#endif