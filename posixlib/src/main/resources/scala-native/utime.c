#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <utime.h>

int scalanative_utime(char *path, struct utimbuf *times) {
    return utime(path, times);
}
#endif // Unix or Mac OS
