#if (defined(SCALANATIVE_COMPILE_ALWAYS) ||                                    \
     defined(__SCALANATIVE_JAVALIB_NIO_FILE_CHANNEL)) &&                       \
    !defined(_WIN32)

// 64-bit offsets on 32-bit targets, where off_t is 32 bits by default
#define _FILE_OFFSET_BITS 64
#include <errno.h>
#include <fcntl.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>

_Static_assert(sizeof(off_t) == 8, "off_t must be 64 bits");

ssize_t scalanative_javalib_pread(int fd, void *buf, size_t count,
                                  int64_t offset) {
    return pread(fd, buf, count, (off_t)offset);
}

ssize_t scalanative_javalib_pwrite(int fd, const void *buf, size_t count,
                                   int64_t offset) {
    return pwrite(fd, buf, count, (off_t)offset);
}

int64_t scalanative_javalib_lseek(int fd, int64_t offset, int whence) {
    return lseek(fd, (off_t)offset, whence);
}

int scalanative_javalib_ftruncate(int fd, int64_t length) {
    return ftruncate(fd, (off_t)length);
}

/* Record lock on [start, start + len); len 0 reaches any end of file.
 * mode: 0 unlock, 1 shared, 2 exclusive. Never waits. Returns 0 or errno.
 */
int scalanative_javalib_lock(int fd, int mode, int64_t start, int64_t len) {
    struct flock fl;
    memset(&fl, 0, sizeof(fl));
    fl.l_type = mode == 0 ? F_UNLCK : mode == 1 ? F_RDLCK : F_WRLCK;
    fl.l_whence = SEEK_SET;
    fl.l_start = (off_t)start;
    fl.l_len = (off_t)len;
    int res;
    do {
        res = fcntl(fd, F_SETLK, &fl);
    } while (res == -1 && errno == EINTR);
    return res == 0 ? 0 : errno;
}

#endif
