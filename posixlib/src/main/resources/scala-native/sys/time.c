#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_SYS_TIME)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <sys/time.h>

// Calls the header-selected symbol, e.g. NetBSD's __gettimeofday50.
int scalanative_gettimeofday(struct timeval *tv, void *tz) {
    return gettimeofday(tv, tz);
}

#endif // Unix or Mac OS
#endif
