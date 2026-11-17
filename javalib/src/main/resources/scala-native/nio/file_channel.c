#if (defined(SCALANATIVE_COMPILE_ALWAYS) ||                                    \
     defined(__SCALANATIVE_JAVALIB_NIO_FILE_CHANNEL)) &&                       \
    !defined(_WIN32)

// 64-bit offsets on 32-bit targets, where off_t is 32 bits by default
#define _FILE_OFFSET_BITS 64
#include <stdint.h>
#include <sys/types.h>
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

#endif
