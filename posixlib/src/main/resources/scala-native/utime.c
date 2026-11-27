#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_UTIME)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <utime.h>

// Calls the header-selected symbol, e.g. NetBSD's 64-bit time_t __utime50.
int scalanative_utime(const char *path, const struct utimbuf *times) {
    return utime(path, times);
}

#endif // Unix or Mac OS
#endif
