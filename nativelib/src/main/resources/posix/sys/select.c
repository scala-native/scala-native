#include <sys/select.h>
#include <stdbool.h>
#include <errno.h>
#include <stddef.h>
#include <string.h>

#define FDBITS (8 * sizeof(long))

struct scalanative_timeval {
    time_t tv_sec;
    suseconds_t tv_usec;
};

struct scalanative_fd_set {
    long fd_bits[FD_SETSIZE / FDBITS];
};

int scalanative_FD_SETSIZE() { return FD_SETSIZE; }

void convert_scalanative_timeval(struct scalanative_timeval *in,
                                 struct timeval *out) {
    out->tv_sec = in->tv_sec;
    out->tv_usec = in->tv_usec;
}

void scalanative_FD_ZERO(struct scalanative_fd_set *set) {
    FD_ZERO((fd_set *)set);
}

void scalanative_FD_CLR(int fd, struct scalanative_fd_set *set) {
    FD_CLR(fd, (fd_set *)set);
}

void scalanative_FD_SET(int fd, struct scalanative_fd_set *set) {
    FD_SET(fd, (fd_set *)set);
}

int scalanative_FD_ISSET(int fd, struct scalanative_fd_set *set) {
    return FD_ISSET(fd, (fd_set *)set);
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
