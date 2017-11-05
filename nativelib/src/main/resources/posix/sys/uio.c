#include <sys/types.h>
#ifndef _WIN32
#include <sys/uio.h>
#else
#include "../../os_win_winsock2.h"
#endif

struct scalanative_iovec {
    void *iov_base; /** Base address of a memory region for input or output. */
    size_t iov_len; /** The size of the memory pointed to by iov_base. */
};

void iovec_to_scalanative_iovec(struct iovec *orig,
                                struct scalanative_iovec *buf) {
    buf->iov_base = orig->iov_base;
    buf->iov_len = orig->iov_len;
}

ssize_t scalanative_readv(int d, struct scalanative_iovec *buf, int iovcnt) {
    struct iovec copy;
    ssize_t result = readv(d, &copy, iovcnt);
    iovec_to_scalanative_iovec(&copy, buf);
    return result;
}

ssize_t scalanative_writev(int fildes, struct scalanative_iovec *buf,
                           int iovcnt) {
    struct iovec copy;
    ssize_t result = writev(fildes, &copy, iovcnt);
    iovec_to_scalanative_iovec(&copy, buf);
    return result;
}
