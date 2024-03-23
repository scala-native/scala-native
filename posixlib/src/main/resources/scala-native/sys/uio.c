#if defined(__SCALANATIVE_POSIX_SYS_UIO)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <sys/types.h>
#include <sys/uio.h>

#include <stddef.h>

struct scalanative_iovec {
    void *iov_base; /** Base address of a memory region for input or output. */
    size_t iov_len; /** The size of the memory pointed to by iov_base. */
};

_Static_assert(sizeof(struct scalanative_iovec) == sizeof(struct iovec),
               "Unexpected size: iovec");

_Static_assert(offsetof(struct scalanative_iovec, iov_base) ==
                   offsetof(struct iovec, iov_base),
               "Unexpected offset: iov_base");

_Static_assert(offsetof(struct scalanative_iovec, iov_len) ==
                   offsetof(struct iovec, iov_len),
               "Unexpected offset: iov_len");

#endif // Unix or Mac OS
#endif