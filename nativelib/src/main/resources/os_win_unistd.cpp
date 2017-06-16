#ifdef _WIN32
#include "os_win_unistd.h"
#include <exception>
#include <cstdio>

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

extern "C" int ftruncate(int fd, off_t length) {
    throw std::exception("`ftruncate` not implemented.");
    return 0; //_chsize(fd, length);
}

extern "C" int write(int fildes, char *buf, size_t nbyte) {
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
        // throw std::exception("`write` not implemented.");
        break;
    }
    return 0;
}

#endif