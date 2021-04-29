#if defined(_POSIX_VERSION)
#include <utime.h>

int scalanative_utime(char *path, struct utimbuf *times) {
    return utime(path, times);
}
#endif //_POSIX_VERSION
