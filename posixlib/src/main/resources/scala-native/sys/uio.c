#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include <stddef.h>
#include <sys/types.h>
#include <sys/uio.h>

struct scalanative_iovec {
    void *iov_base; /** Base address of a memory region for input or output. */
    size_t iov_len; /** The size of the memory pointed to by iov_base. */
};

_Static_assert(sizeof(struct scalanative_iovec) == sizeof(struct iovec),
               "size mismatch: struct scalanative_iovec");

#endif // Unix or Mac OS
