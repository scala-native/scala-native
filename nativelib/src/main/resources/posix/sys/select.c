#include <sys/select.h>
#include <stdbool.h>
#include <errno.h>
#include <stddef.h>
#include <string.h>

struct scalanative_timeval {
    time_t tv_sec;
    int tv_usec;
};

struct scalanative_fd_set {
    long fd_bits[FD_SETSIZE / sizeof(long)];
};

int scalanative_FD_SETSIZE() { return FD_SETSIZE; }

void convert_scalanative_timeval(struct scalanative_timeval *in,
                                 struct timeval *out) {
    out->tv_sec = in->tv_sec;
    out->tv_usec = in->tv_usec;
}

void scalanative_FD_ZERO(struct scalanative_fd_set *set) {
    memset(set->fd_bits, '\0', FD_SETSIZE / sizeof(long));
}

void scalanative_FD_CLR(int fd, struct scalanative_fd_set *set) {
    set->fd_bits[fd / sizeof(long)] &= ~((long)(1 << (fd % sizeof(long))));
}

void scalanative_FD_SET(int fd, struct scalanative_fd_set *set) {
    set->fd_bits[fd / sizeof(long)] |= ((long)(1 << (fd % sizeof(long))));
}

int scalanative_FD_ISSET(int fd, struct scalanative_fd_set *set) {
    return ((set->fd_bits[fd / sizeof(long)] &
             ((long)(1 << (fd % sizeof(long))))) != 0);
}

int scalanative_select(int nfds, struct scalanative_fd_set *readfds,
                       struct scalanative_fd_set *writefds,
                       struct scalanative_fd_set *exceptfds,
                       struct scalanative_timeval *scalanative_timeout) {
    fd_set *rfds = (fd_set *)readfds;
    fd_set *wfds = (fd_set *)writefds;
    fd_set *efds = (fd_set *)exceptfds;

    int status;
    if (scalanative_timeout != NULL) {
        struct timeval timeout;
        convert_scalanative_timeval(scalanative_timeout, &timeout);
        status = select(nfds, rfds, wfds, efds, &timeout);
    } else {
        status = select(nfds, rfds, wfds, efds, NULL);
    }

    return status;
}
