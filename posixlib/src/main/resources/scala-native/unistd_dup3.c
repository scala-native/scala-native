#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_UNISTD)
#if defined(__unix__) || defined(__unix) || defined(unix)
// dup3() is not in POSIX, glibc declares it only with _GNU_SOURCE.
#define _GNU_SOURCE
#include <unistd.h>

// Calls the header-selected symbol, e.g. NetBSD's __dup3100.
int scalanative_dup3(int fildes, int fildesnew, int flags) {
    return dup3(fildes, fildesnew, flags);
}

#endif
#endif
