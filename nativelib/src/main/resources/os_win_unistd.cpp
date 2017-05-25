#ifdef _WIN32
#include "os_win_unistd.h"

extern "C" int symlink(char *path1, char *path2) { return 0; }

extern "C" int symlinkat(char *path1, int fd, char *path2) { return 0; }

extern "C" int link(char *oldpath, char *newpath) { return 0; }

extern "C" int linkat(int fd1, char *path1, int fd2, char *path2, int flag) {
    return 0;
}

extern "C" int chown(char *path, uid_t owner, gid_t group) { return 0; }

extern "C" int ftruncate(int fd, off_t length) { return _chsize(fd, length); }

#endif