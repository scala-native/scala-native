#ifdef _WIN32

#include "os_win_statvfs.h"

extern "C" int statvfs(const char *path, struct statvfs *buf) { return 0; }

extern int fstatvfs(int fd, struct statvfs *buf) { return 0; }

#endif